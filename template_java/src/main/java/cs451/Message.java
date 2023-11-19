package cs451;

import java.io.*;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return senderId == message1.senderId && message == message1.message && seqNum == message1.seqNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, message, seqNum);
    }
}