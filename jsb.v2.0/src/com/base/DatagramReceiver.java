package com.base;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receiver for datagram sockets which is mainly applicable for UDP
 * 
 * @author demiry
 *
 */
public class DatagramReceiver  implements Runnable {
   static final int buffer_size = 4096;
   
   DatagramSocket sock;
   DatagramStackCore core;
      
   AtomicBoolean stopWorking = new AtomicBoolean(false);
   
   byte[] datagramPacket;
   
//   Logger logger;
   
   public DatagramReceiver(DatagramSocket socket, DatagramStackCore stack) {
      this.sock = socket;
      this.core = stack;
      this.datagramPacket = new byte[buffer_size];
//      this.logger = logger;
   }

   @Override
   public void run() {
      try {
         while(! this.stopWorking.get()) {
            DatagramPacket packet = new DatagramPacket(this.datagramPacket, buffer_size);               
            this.sock.receive(packet);
            int length = packet.getLength();
//            System.out.println("Received packet size = " + length);
//            
//            System.out.println("Remote address: " + packet.getAddress() + " and post: " + packet.getPort());
                        
            //byte[] data = new byte[packet.getLength()];
            //System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
            
            //core.messageReceived(this.datagramPacket, length);
            //core.messageReceived(data, length);
            core.messageReceived(this.datagramPacket, length, packet.getAddress(), packet.getPort());
         }
      } 
      catch (IOException e) {
         if (! this.stopWorking.get()) {
            Logger.Excep("Exception on DatagramReceiver.../n" + e);
         }
      }
      Logger.Info("DatagramReceiver Stopped.");
      this.sock.close();
   }
   
   public synchronized void stop() {
      this.stopWorking.set(true); 
      Logger.Info("Stoping the datagram-receiver ... " + this.stopWorking.get());
      if ((this.sock != null) && (!this.sock.isClosed())) {
         this.sock.close();
      }
   }
}

