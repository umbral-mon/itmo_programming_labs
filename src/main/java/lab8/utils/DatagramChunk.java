package lab8.utils;

import java.util.Arrays;

public class DatagramChunk {

    private int transactionId, packetNumber, packetCount;
    private byte[] data;

    public DatagramChunk(int transactionId, int packetNumber, int packetCount, byte[] data){
        this.transactionId = transactionId;
        this.packetNumber = packetNumber;
        this.packetCount = packetCount;
        this.data = data;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getPacketCount() {
        return packetCount;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public byte[] getData() {
        return data;
    }

    public static DatagramChunk[] split(String message){
        byte[] messageBytes = message.getBytes();
        int size = (int)Math.ceil(messageBytes.length * 1.0 / Config.getInstance().getDataByteSize());
        DatagramChunk[] ret = new DatagramChunk[size];
        int transactionId = 0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new DatagramChunk(transactionId, i, ret.length, Arrays.copyOfRange(messageBytes, i * Config.getInstance().getDataByteSize(), (i+1)* Config.getInstance().getDataByteSize()));
        }
        return ret;
    }

    @Override
    public String toString() {
        return "DatagramChunk{" +
                "transactionId=" + transactionId +
                ", packetNumber=" + packetNumber +
                ", packetCount=" + packetCount +
                ", data=" + new String(data) +
                '}';
    }
}
