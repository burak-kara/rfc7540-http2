package test;

import http2.HTTP2;
import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private final int PORT = 9090;
    private final String IP = "127.0.0.1";
    private BufferedReader reader;
    private Socket server;
    private PrintWriter writer;
    private String newField;

    public static void main(String[] args) {
        Client client = new Client();
        Thread tt = new Thread(client);
        tt.start();
    }

    @Override
    public void run() {
        try {
            server = new Socket(IP, PORT);
            InputStreamReader streamReader = new InputStreamReader(server.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(server.getOutputStream());
            writer.println("Connected");
            writer.flush();
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        Thread IncomingReader = new Thread(new IncomingReader());
        IncomingReader.start();
    }

    public void sendBoard(String message) {
        HTTP2 packet = (new TestCreator()).packetExp();
        String xx = packet.getPacketAsString();
        try {
            writer.println(xx);
            System.out.println("time to send");
            writer.flush();
        } catch (Exception exception) {
            System.out.println("Message was not sent");
        }
    }

    public String getMessage() {
        return newField;
    }

    public class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    newField = message;
                    System.out.println(newField);
                }
            } catch (Exception ex) {
                System.out.println("no new field");
            }
        }
    }
}