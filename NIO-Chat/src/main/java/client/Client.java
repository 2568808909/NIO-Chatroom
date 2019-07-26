package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import box.FileSendPacket;
import core.IoContext;
import foo.Foo;
import impl.IoSelectorProvider;

public class Client {

    public final static int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        File cacheFile = Foo.getCacheDir("client");
        IoContext.setup().ioProvider(new IoSelectorProvider()).start();
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server :" + info);
        if (info != null) {
            TCPClient client = null;
            try {
                client = TCPClient.startWith(info, cacheFile);
                if (client == null) {
                    return;
                }
                write(client);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    client.exit();
                }
            }
        }
        IoContext.close();
    }

    private static void write(TCPClient client) throws IOException {
        InputStream inputStream = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        do {
            String str = input.readLine();
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
            //--f url代表发送一个文件
            if (str.startsWith("--f")) {
                String[] array = str.split(" ");
                if (array.length >= 2) {
                    String filePath = array[1];
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        FileSendPacket fileSendPacket = new FileSendPacket(file);
                        client.send(fileSendPacket);
                        continue;
                    }
                }
            }
            //发送字符串
            client.send(str);

        } while (true);

    }


}
