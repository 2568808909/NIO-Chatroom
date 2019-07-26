package box;

import core.Packet;
import core.SendPacket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileSendPacket extends SendPacket<FileInputStream> {

    private File file;

    public FileSendPacket(File file) {
        this.length = file.length();
        this.file = file;
    }

    @Override
    protected FileInputStream createStream() {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte type() {
        return Packet.TYPE_STREAM_FILE;
    }
}
