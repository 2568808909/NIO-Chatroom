package box;

import core.Packet;
import core.ReceivePacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceivePacket extends ReceivePacket<FileOutputStream, File> {

    private File file;

    public FileReceivePacket(long length,File file) {
        super(length);
        this.file=file;
    }

    @Override
    protected FileOutputStream createStream() {
        try {
            return new FileOutputStream(file);
        }catch (IOException e){
            return null;
        }
    }

    @Override
    public byte type() {
        return Packet.TYPE_STREAM_FILE;
    }

    @Override
    protected File buildEntity(FileOutputStream stream) {
        return file;
    }
}
