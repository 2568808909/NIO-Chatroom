package box;

import core.ReceivePacket;

import java.io.FileOutputStream;

public class FileReceivePacket extends ReceivePacket<FileOutputStream> {
    @Override
    protected FileOutputStream createStream() {
        return null;
    }
}
