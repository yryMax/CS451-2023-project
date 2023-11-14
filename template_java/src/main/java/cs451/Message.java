package cs451;

import java.io.*;

public class Message implements Serializable {
    private int senderId;
    private int message;
    private int seqNum;

    public Message(int senderId, int message, int seqNum) {
        this.senderId = senderId;
        this.message = message;
        this.seqNum = seqNum;
    }

    public Message(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Message message = (Message) in.readObject();
        this.senderId = message.getSenderId();
        this.message = message.getMessage();
        this.seqNum = message.getSeqNum();
    }

    public int getSenderId() {
        return senderId;
    }

    public int getMessage() {
        return message;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public String toString() {
        return "Sender ID: " + senderId + " Message: " + message + " SeqNum: " + seqNum + "\n";
    }
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }
}