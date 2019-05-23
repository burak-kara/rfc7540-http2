package test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private static int PORT = 9090;
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.printf("Started serverSocket on PORT: %d\n", PORT);
        System.out.println("Server established");
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                Thread serverThread = new Thread(new ServerHandler(clientSocket));
                serverThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ServerHandler implements Runnable {
        final Socket server;

        public ServerHandler(Socket server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                System.out.println("Connection established");
                System.out.println("Message: ");
                while (true) {
                    InputStream stream = server.getInputStream();
                    DataInputStream in = new DataInputStream(new BufferedInputStream(stream));
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());



                    /*
                    TestCreator tc = new TestCreator();
                    String responseHeader = tc.getResponseHeader();
                    System.out.println(responseHeader);
                    */

                    //out.writeBytes(responseHeader);

                    //System.out.println(new Converter().binaryStringToString(binaryString));


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


                    TestCreator tc = new TestCreator();
                    String responseHeader = tc.getResponseHeader();
                    out.writeBytes(" PLAIN_TEXT");




/*
                    byte[] data = new byte[2048];
                    int count = stream.read(data);
                    //count-> # bytes actually read, data->all data gathered
                    System.out.println(""+count);

                    String str = "";
                    for(byte i:data){
                        str+= i;
                    }
                    System.out.println(""+str);
*/

                }
            } catch (IOException ioe) {
                System.out.print("Error on connection!!\n");
            }
        }

    }


    public static void main(String args[]) {
        Thread server = new Thread(new Server());
        server.start();
    }
}