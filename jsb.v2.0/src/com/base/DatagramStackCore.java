package com.base;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Stack core for datagram based connections such as UDP
 * 
 * @author demiry
 *
 */
public class DatagramStackCore implements StackCoreIF {
   /* listen address */
   String ipaddr;
   
   /* listen port */
   int localPort;
   
   /* socket to be used for both sending and receiving */ 
   DatagramSocket socket;
   
   DatagramReceiver receiver;
   
   /* remote information for dedicated usage of the stack */
   String remoteIP;
   int    remotePort;
   
   ApplicationIF application;
   
//   Logger logger;
 
   public DatagramStackCore(String ipaddr, int listenport, ApplicationIF app) throws UnknownHostException, SocketException {
      this.ipaddr = ipaddr;
      InetAddress inetaddr = InetAddress.getByName(this.ipaddr);
      this.socket = new DatagramSocket(listenport, inetaddr);
      
      // get actual port in case listenport was 0 (any avaiable)
      this.localPort = socket.getLocalPort();
      this.application = app;
//      this.logger = logger;
      this.receiver = new DatagramReceiver(this.socket, this);
   }
   
   public void setBufferSizes(int bsize) {
      try {
         this.socket.setReceiveBufferSize(bsize);
         this.socket.setSendBufferSize(bsize);
      }
      catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public void setReceiveBufferSizes(int bsize) {
      try {
         this.socket.setReceiveBufferSize(bsize);
      }
      catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public void setSendBufferSizes(int bsize) {
      try {
         this.socket.setSendBufferSize(bsize);
      }
      catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public int getReceiveBufferSize() {
      try {
         return this.socket.getReceiveBufferSize();
      }
      catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return 0;
   }
   
   public int getSendBufferSize() {
      try {
         return this.socket.getSendBufferSize();
      }
      catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return 0;
   }
   
   public int startReceiving() {
      new Thread(this.receiver, "receiver").start();
      return 0;
   }
   
   public int sendMessage(byte[] msg, int len, String remoteIpAddr, int remotePort) {
      int result = 0;
      
      InetAddress inetaddr = null;;
      DatagramPacket packet = null;
      try {
         inetaddr = InetAddress.getByName(remoteIpAddr);
         packet = new DatagramPacket(msg, len, inetaddr, remotePort);
      }
      catch (UnknownHostException e1) {
         Logger.Excep("UnknownHostException on DatagramStackCore...\n" + e1);
         return -1;
      }
      
      try {
         this.socket.send(packet);
      }
      catch (IOException e) {
         e.printStackTrace();
         this.socket.close();
         result = -3;
      }
      return result;
   }
   
   public int sendMessage(byte[] msg, int len) {
      return this.sendMessage(msg, len, this.remoteIP, this.remotePort);
   }
   
//   /* called from receiver */
//   public byte[] messageReceived(byte[] msgb, int length) {
//      if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
//         Logger.Debug("Got message with length = " + length + " or " + msgb.length);
//         String readBuff = new String(msgb, 0, length);
//         Logger.Debug("Received Datagram Message\n" + readBuff);
//      }
//      this.application.onReceivedMessage(msgb, length);
//      return null;
//   }
   
   /* called from receiver */
   public byte[] messageReceived(byte[] msgb, int length, InetAddress address, int port) {
      /* update remote address information if not set previously */
      /* TODO: This approach restricts the request-response behavior if messages comes 
       * from different IPs, in the case that response will be send back where the 
       * request is received
       */
      if (this.remoteIP == null) {
         this.remoteIP = address.getHostAddress();
         this.remotePort = port;
      }
      if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
         Logger.Debug("Got message with length = " + length + " or " + msgb.length +
                           " from remoote==>" + address.getHostAddress() + ":" + port);
         String readBuff = new String(msgb, 0, length);
         Logger.Debug("Received Datagram Message\n" + readBuff);
      }
      
      //this.application.onReceivedMessage(msgb, length);
      this.application.onReceivedMessage(msgb, length, address.getHostAddress(), (short)port);
      return null;
   }
   
   public void setRemoteIp(String remIP) {
      this.remoteIP = remIP;
   }
   
   public String getRemoteIP() {
      return this.remoteIP;
   }
   
   public void setRemotePort(int port) {
      this.remotePort = port;
   }
   
   public int getRemotePort() {
      return this.remotePort;
   }
   
   public int getLocalPort() {
      return localPort;
   }
   
   public static void main(String[] args) {
      try {
         DatagramStackCore core = new DatagramStackCore("127.0.0.1", 5666, null);
         core.startReceiving();
         System.out.println("Receiving started...");
         while(true) {
            
         }
       }
      catch (UnknownHostException ue) { 
         // TODO Auto-generated catch block
         ue.printStackTrace();
      }
      catch ( SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void shutdown() {
      receiver.stop();
   }
}

