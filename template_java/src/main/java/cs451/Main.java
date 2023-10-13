package cs451;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

class connectThread extends Thread{
    private Socket socket;
    private int id;
    private int port;
    private String ip;
    public connectThread(int id, int port, String ip){
        this.id = id;
        this.port = port;
        this.ip = ip;
    }
    public void run(){
        System.out.println("Trying to connect to " + id + " at " + ip + ":" + port);
        while(true){
            try {
                socket = new Socket(ip, port);
                break;
            } catch (IOException e) {
                continue;
            }
        }
        System.out.println("Connected to " + id);
    }
    public Socket getSocket(){
        return socket;
    }
}
public class Main {

    private static List<String>logs;
    private static Parser parserOutput;
    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        System.out.println("Writing output.");
        try {
            String path = parserOutput.output();
            File file = new File(path);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
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
        logs = new ArrayList<>();

        File fileConfig = new File(parser.config());
        Scanner sc = new Scanner(fileConfig);
        int m = sc.nextInt();
        int receiverId = sc.nextInt();

        System.out.println("Number of messages: " + m);
        System.out.println("Receiver ID: " + receiverId);



        if(parser.myId() == receiverId){
            System.out.println("I am the receiver");
            // listen for messages, if message is received, print it to output file
            try {
                ServerSocket serverSocket = new ServerSocket(parser.hosts().get(parser.myId()).getPort());
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String receivedMessage = in.readLine();
                        String[] parts = receivedMessage.split("#");
                        System.out.println("Received message: " + parts[0] + " " + parts[1]);
                        logs.add("d " + parts[0] + " " + parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("I am one of the sender");
            int n = parser.hosts().size();
            //create n thread to try to connect to all other hosts blockingly, just keep trying until success
            connectThread[] connectThreads = new connectThread[n];
            for(int i = 0; i < n; i++){
                connectThreads[i] = new connectThread(i, parser.hosts().get(i).getPort(), parser.hosts().get(i).getIp());
            }
            for(int i = 0; i < n; i++){
                connectThreads[i].start();
            }
            for(int i = 0; i < n; i++){
                connectThreads[i].join();
            }
            for(int i = 1; i <= m; i++){
                logs.add("b " + i);
                for(int j=0;j<n;i++){
                    String message = i + "#" + parser.myId();
                    System.out.println("Sending message: " + message);
                    connectThreads[j].getSocket().getOutputStream().write(message.getBytes());
                }
            }
        }
    }
}
