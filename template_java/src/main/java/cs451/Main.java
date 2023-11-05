package cs451;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class Main {

    private static List<String>logs;
    private static Parser parserOutput;

    private static FileWriter writer;
    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        System.out.println("Writing output.");
        try {
            for (String log : logs) {
                writer.write(log + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static boolean checkValid(String s){
        String[] parts = s.split("ccc");
        if(parts.length != 3){
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();
        parserOutput = parser;
        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");
        logs =  new CopyOnWriteArrayList<String>();

        File fileConfig = new File(parser.config());
        Scanner sc = new Scanner(fileConfig);
        int m = sc.nextInt();
        int receiverId = sc.nextInt();

        System.out.println("Number of messages: " + m);
        System.out.println("Receiver ID: " + receiverId);

        int n = parser.hosts().size();

        Host receiver = null;
        Host sender = null;

        Map<Integer, Integer> seq = new HashMap<>();
        for(Host host: parser.hosts()){
            if(host.getId() == receiverId){
                receiver = host;
            }
            if(host.getId() == parser.myId()){
                sender = host;
            }
            seq.put(host.getId(), 0);
        }

        String path = parserOutput.output();
        File file = new File(path);
        file.createNewFile();
        writer = new FileWriter(file, false);

        DatagramSocket socket = new DatagramSocket(sender.getPort());


        if(parser.myId() == receiverId){
            System.out.println("I am the receiver");
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + receivedMessage);
                String[] parts = receivedMessage.split("bbb");
                for(int i=0; i<parts.length; i++){
                    System.out.println("Received message part: " + parts[i]);
                    if(!checkValid(parts[i])){
                        continue;
                    }
                    System.out.println();
                    String[] d = parts[i].split("ccc");
                    int seq_num = Integer.parseInt(d[2]);
                    int sender_id = Integer.parseInt(d[0]);
                    int message = Integer.parseInt(d[1]);
                    if(seq_num == seq.get(sender_id) + 1){
                        seq.put(sender_id, seq_num);
                        logs.add("d " + sender_id + " " + message);
                        if(logs.size() >= 10000){
                            for (String log : logs) {
                                writer.write(log + "\n");
                            }
                            logs.clear();
                        }
                    }
                }
                // Clear the buffer after processing each packet
                packet.setLength(buffer.length);
            }

        }
        else {
            System.out.println("I am one of the senders");
            // Set up the address for the receiver
            InetSocketAddress receiverAddress = new InetSocketAddress(receiver.getIp(), receiver.getPort());
            // Send messages
            while (true) {
                boolean flag = true;
                for(int i = 1; i <= m; i++) {
                    System.out.println("Sending message: " + i);
                    if(flag)logs.add("b " + i);
                    String msg = parser.myId() + "ccc" + i + "ccc" + i + "bbb";
                    byte[] messageBytes = msg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, receiverAddress);
                    socket.send(sendPacket); // Send the packet
                    System.out.println("Sent message: " + msg);
                }
                flag = false;
            }
        }
    }
}
