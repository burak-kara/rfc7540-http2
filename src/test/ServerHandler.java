package test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHandler implements Runnable{
    final int PORT;
    final String ROOT;
    final ServerSocket server;

    public ServerHandler(int PORT, String ROOT, ServerSocket server) {
        this.PORT = PORT;
        this.ROOT = ROOT;
        this.server = server;
    }
    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = server.accept();
                System.out.println("Connection established");

                InputStream stream = socket.getInputStream();
                byte[] data = new byte[2048];
                int count = stream.read(data);
                //count-> # bytes actually read, data->all data gathered
            }
        } catch (IOException ioe) {
            System.out.print("Error on connection!!\n");
        }
    }

}
