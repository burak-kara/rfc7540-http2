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
            /*
            InputStreamReader streamReader = new InputStreamReader(in);
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(server.getOutputStream());
            writer.flush();
            System.out.println(reader.readLine());
            */

            server = new Socket(IP, PORT);
            System.out.println("Connected");

            InputStream inStream = server.getInputStream();
            DataInputStream in = new DataInputStream(new BufferedInputStream(inStream));
            DataOutputStream out = new DataOutputStream(server.getOutputStream());

            TestCreator tc = new TestCreator();
            String responseHeader = tc.getResponseHeader();

            System.out.println("Message Sent: \n"+responseHeader);
            out.writeBytes(responseHeader);

            while(true) {
                byte[] line = new byte[2500];
                try {
                    in.read(line);
                    String str = "";
                    for (byte i : line) {
                        System.out.print((char) i);
                    }

                } catch (EOFException e) {
                    System.err.println("\nException/End of stream");
                }

            }


        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        Thread IncomingReader = new Thread(new IncomingReader());
        //IncomingReader.start();
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
