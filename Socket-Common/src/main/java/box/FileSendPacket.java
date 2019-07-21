package box;

import core.SendPacket;

import java.io.File;
import java.io.FileInputStream;

public class FileSendPacket extends SendPacket<FileInputStream> {

    private File file;

    public FileSendPacket(File file){
        this.length=file.length();
        this.file=file;
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
