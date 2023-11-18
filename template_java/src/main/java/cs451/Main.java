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
    private static Set<Message> delivered;
    private static DatagramSocket socket;
    private static List<Host>hosts;



    static class Receiver implements Runnable{

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            Message message = null;
            while (true) {
                try {
                    socket.receive(packet);
                    try {
                        message = new Message(packet.getData());
                    } catch (Exception ClassNotFound) {
                        continue;
                    }
                    if (message != null) {
                        rbDeliver(message);
                    }
                    packet.setLength(buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

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

    public static void flp2pSend(Host receiver, Message message) throws IOException {
        InetSocketAddress receiverAddress = new InetSocketAddress(receiver.getIp(), receiver.getPort());
        byte[] messageBytes = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, receiverAddress);
        socket.send(sendPacket); // Send the packet
        System.out.println("Sent message: " + message.toString());
    }


    public static void bebBroadcast(Message message) throws IOException {
        for(Host h: hosts){
           flp2pSend(h, message);
        }
    }

    public static void rbBroadcast(Message message)  throws IOException {
        rbDeliver(message);
    }

    public static void finalDeliver(Message message) throws IOException {
        System.out.println("Delivered message: " + message.toString());
        logs.add("d " + message.getSenderId() + " " + message.getMessage());
        if(logs.size() >= 10000){
            for (String log : logs) {
                writer.write(log + "\n");
            }
            logs.clear();
        }
    }
    public static void rbDeliver(Message message)  throws IOException {
        System.out.println("Received message: " + message.toString() + "From p2plink");
        if(!delivered.contains(message)){
            delivered.add(message);
            finalDeliver(message);
            bebBroadcast(message);
        }
    }
    public static void main(String[] args) throws IOException {
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

        System.out.println("Number of messages: " + m);

        hosts = parser.hosts();

        for(Host host: hosts){
            if(host.getId() == parser.myId()) {
                socket = new DatagramSocket(host.getPort());
            }
        }

        String path = parserOutput.output();
        File file = new File(path);
        file.createNewFile();
        writer = new FileWriter(file, false);


        delivered = new HashSet<>();

        Receiver receiver = new Receiver();
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        while(true) {
            boolean flag = true;
            while (true) {
                int seq = 0;
                for(int i = 1; i <= m; i++) {
                    System.out.println("Sending message: " + i);
                    if(flag)logs.add("b " + i);
                    Message message = new Message(parser.myId(), i, seq++);
                    rbBroadcast(message);
                }
                flag = false;
            }
        }
    }
}
