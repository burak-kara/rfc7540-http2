package test;

import java.io.IOException;
import java.io.InputStream;
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

    public class ServerHandler implements Runnable{
        final Socket server;

        public ServerHandler(Socket server) {
            this.server = server;
        }
        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("Connection established");
                    InputStream stream = server.getInputStream();
                    byte[] data = new byte[2048];
                    int count = stream.read(data);
                    //count-> # bytes actually read, data->all data gathered
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
