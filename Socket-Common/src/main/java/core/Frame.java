package core;

import java.io.IOException;

public abstract class Frame {

    public static final int FRAME_HEADER_LENGTH = 6;  //头大小
    public static final int MAX_CAPACITY = (2 << 15) - 1; //最大帧长

    public static final byte TYPE_PACKET_HEADER = 11;
    public static final byte TYPE_PACKET_ENTITY = 12;
    public static final byte TYPE_COMMAND_SEND_CANCEL = 41;
    public static final byte TYPE_COMMAND_RECEIVE_REJCET = 41;

    public static final byte FLAG = 0;

    protected byte[] header = new byte[FRAME_HEADER_LENGTH];


    /**
     * @param length     帧数据长度 0~65535
     * @param type       帧类型
     * @param flag       帧标志信息
     * @param identifier 唯一标识0~255用于标识不同的包，以拆分/组装包数据
     */
    public Frame(int length, byte type, byte flag, short identifier) {
        if (length < 0 || length > MAX_CAPACITY) {
            throw new IllegalArgumentException("length can not be " + length);
        }
        header[0] = (byte) (length >> 8);
        header[1] = (byte) length;

        header[2] = type;

        header[3] = flag;

        header[4] = (byte) identifier;

        header[5] = 0;
    }

    public Frame(byte[] header) {
        System.arraycopy(header, 0, this.header, 0, FRAME_HEADER_LENGTH);
    }

    public int getBodyLength() {
        return ((int) header[0] & 0xFF << 0) | ((int) header[1] & 0xFF);
    }

    public byte getBodyType() {
        return header[2];
    }

    public byte getBodyFlag() {
        return header[3];
    }

    public short getBodyIdentifier() {
        return (short) (((short) header[4]) & 0XFF);
    }

    /**
     * 用于执行读取和写入
     */
    public abstract boolean handle(IoArgs args) throws IOException;

    /**
     *
     */
    public abstract Frame nextFrame();

}
