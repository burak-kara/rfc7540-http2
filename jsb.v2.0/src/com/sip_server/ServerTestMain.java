package com.sip_server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.base.ApplicationIF;
import com.base.DatagramStackCore;
import com.base.Logger;
import com.base.StackCore;
import com.base.StreamMessageParser;
//import com.sip_client.ReceivedMessage;
//import com.sip_client.ReceivedMessageQueue;
//import com.sip_client.ClientWorker;
import com.sip_client.ClientWorker;

public class ServerTestMain implements ApplicationIF {
   public static ServerTestMain instance = new ServerTestMain();
   
   // Do not use in the case of binding to all adapters
   static String localAddress = "127.0.0.1";
   // To be used in listening
   static short localPort = 1820;
   static short localTcpPort = 1827;

   static String imServerAddress = "127.0.0.1";
   static short imServerPort = 1812;

   static DatagramStackCore udpStack = null;
   static StackCore tcpStack = null;
   Socket clientConnSocket = null;
   
   public static Queue<Timer> transactionTimers = new ConcurrentLinkedQueue<Timer>();
   public static final int TRANSACTION_TIMEOUT = 5000;
   
   public static ReceivedMessageQueue rcvedMsgQueue = ReceivedMessageQueue.getInstance();
   public static ServerWorker worker = null;
   
   public static int streamParserActive = 0;
   public static StreamMessageParser parser = new StreamMessageParser(4096, instance);
   
   private static int menuLevel      = 0;
   
   public static AtomicInteger timerId = new AtomicInteger(0);
   
   public class WakeupTask extends TimerTask
   {
      int timerId;
      Timer timer;
      
      public WakeupTask(Timer timer, int timerid) {
         this.timerId = timerid;
         this.timer = timer;
         System.out.println("WakeupTask: Starting with id: " + this.timerId);
      }
    
      public void run()
      {
         this.timer.cancel();
         TransactionTimeroutHandler(System.currentTimeMillis(), this.timerId);
      }
   }
   
   public static synchronized void startTransactionTimer(int miliSeconds)
   {
      Timer timer = new Timer();
      WakeupTask ttask = instance.new WakeupTask(timer, timerId.incrementAndGet());
      transactionTimers.offer(timer);
      timer.schedule(ttask, miliSeconds);
      System.out.println("startTransactionTimer: Scheduled timer while size of queue is " + transactionTimers.size());
   }
   
   public static int stopTransactionTimer()
   {
      /* No mapping with the current transaction is considered. 
       * Just get the first item from the timer-queue to stop
       */
      Timer timer = transactionTimers.poll();
      if (timer != null) {
         System.out.println("Stopping the timer while the size of the queue is : " + transactionTimers.size());
         timer.cancel();
      }
      return 0;
   }
   
   public static synchronized void TransactionTimeroutHandler(long currentTimestamp, int id)
   {
      Timer timer = transactionTimers.poll();
      Logger.Error("TransactionTimeroutHandler: Transaction timer with Id = " + 
                   id + " is expired at time = " + currentTimestamp + 
                   " while size: " + transactionTimers.size() + "\n");
   }

   @Override
   //public int onReceivedMessage(byte[] rawMessage, int length) {
   public int onReceivedMessage(byte[] rawMessage, int length, Socket socket) {
      String msg = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
      System.out.println("RECEIVED(TCP) - " + msg);
      //stopTransactionTimer();
      if (streamParserActive > 0)
      {
         byte[] clone = new byte[length];
         System.arraycopy(rawMessage, 0, clone, 0, length);
         if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
            Logger.Debug("MsgLength---> " + length);
            Logger.Debug("Msg:" + new String(clone, 0, length));
         }
         try {
            /* "addBytes" will call-back the application when a complete
               message received */
            System.out.println("onReceivedMessage: Adding bytes to stream parser...");
            parser.addBytes(clone);
         }
         catch (IOException io) {
            Logger.Excep("IO problem: " + io);
         }
      }
      else
      {
         String rcved = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
         int index = rcved.indexOf(' ');
         String keyword = rcved.substring(0, index);
         ReceivedMessage rcvmsg = new ReceivedMessage(socket, rcved, keyword);
         rcvedMsgQueue.enqueue(rcvmsg);
      } 
      return 0;
   }

   @Override
   /* From UDP */
   public int onReceivedMessage(byte[] rawMessage, int length, String sourceAddress, short sourcePort)
   {
      String msg = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
      System.out.println("RECEIVED(UDP) - " + msg);
      stopTransactionTimer();
      String rcved = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
      int index = rcved.indexOf(' ');
      String keyword = rcved.substring(0, index);
      ReceivedMessage rcvmsg = new ReceivedMessage(sourceAddress, sourcePort, rcved, keyword);
      rcvedMsgQueue.enqueue(rcvmsg);
      
      return 0;
   }
   
   @Override
   /* From StreamMessageParser */
   /* Note that parameter 'message' consists of only headers part of message */
   public int onReceivedMessage(String message, ArrayList<String> messageHeader, byte[] messageBody) {
      System.out.println("HEADERS:");
      int vsize = messageHeader.size();
      for (int i = 0; i < vsize; i++)
      {
         System.out.print(messageHeader.get(i));
      }
      System.out.print("\n");
      System.out.print("MESSAGE BODY:\n");
      int blen = messageBody.length;
      for (int i = 0; i < blen; i++)
      {
         System.out.print(messageBody[i]);
      }
      System.out.print("\n");

      int posIdx;
      posIdx = messageHeader.get(0).indexOf(' ');
      System.out.println("posIdx = " + posIdx + "\n");
      String keyWord = messageHeader.get(0).substring(0, posIdx);
      System.out.println("keyWord = " + keyWord + "\n");

//      Socket* recvSocket = (Socket*)userdata;
      /* Note that current msgb consists of only headers part of message */
//      std::string rcvMsg((char*)msgb, length);
      ReceivedMessage rcvmsg = new ReceivedMessage(clientConnSocket, message, keyWord);
      rcvedMsgQueue.enqueue(rcvmsg);

      return 0;
   }
   @Override
   public int onResponseTimeout() {
      // TODO Auto-generated method stub
      return 0;
   }
   @Override
   public int onSendMessage() {
      // TODO Auto-generated method stub
      return 0;
   }
   @Override
   public int onConnectionSuccess(String locAddr, int locPort, String remAddr, int remPort) {
      // TODO Auto-generated method stub
      return 0;
   }
   @Override
   public int onConnectionFail(String remAddr, int remPort) {
      // TODO Auto-generated method stub
      return 0;
   }
   @Override
   public int onDisconnect(int failReason) {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public boolean receiveMessage(Socket sock) {
      // TODO Auto-generated method stub
      return false;
   }
   
   public static void Send200OK(boolean isTCP, Socket rcvsock, String targetAddr, short targetPort)
   {
     String msg = "SIP/2.0 200 OK\r\n" +
                  "From: \"Demir Yavas\"<sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n" +
                  "To: \"Fatih Nar\"<sip:fenar@techtrial.com>;tag=1153442018376\r\n" +
                  "Call-ID: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" +
                  "CSeq: 316 SUBSCRIBE\r\n" +
                  "Via: SIP/2.0/UDP 47.168.54.137:5060;branch=z9hG4bK45925f07-4\r\n" +
                  "contact: <sip:fenar@techtrial.com:5060;maddr=47.103.142.59>\r\n" +
                  "expires: 84210\r\n" +
                  "supported: x-nt-ss-otpp\r\n" +
                  "Content-Length: 0\r\n" +
                  "\r\n";

     Logger.Debug("SEND - To: " + targetAddr + ":" + targetPort + "\n");
     byte[] msgbytes = msg.getBytes();
     if (isTCP)
     {
        try {
           OutputStream output = rcvsock.getOutputStream();
           output.write(msgbytes);
        }
        catch (IOException e) {
           Logger.Excep("Send200OK::IO Problem: " + e);
        }
     }
     else
     {
        udpStack.sendMessage(msgbytes, msgbytes.length, targetAddr, targetPort);
     }
   }

   public static int SendNotify(boolean isTCP)
   {
      String msgBuf = "NOTIFY sip:demiry@47.168.54.137:5060;nt_info=proxy SIP/2.0\r\n" +
                      "From: \"fenar\"<sip:fenar@techtrial.com>;tag=1153442018376\r\n" +
                      "To: \"demiry\"<sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n" +
                      "Call-ID: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" +
                      "CSeq: 5168788 NOTIFY\r\n" +
                      "Via: SIP/2.0/UDP 47.103.142.59:5060;branch=z9hG4bK-b0500-2b0b89ae-5e4a2157\r\n" +
                      "content-type: application/cpim-pidf+xml\r\n" +
                      "max-forwards: 20\r\n" +
                      "supported: com.nortelnetworks.firewall,p-3rdpartycontrol,nosec,join,com.nortelnetworks.im.encryption\r\n" +
                      "subscription-expires: 84210\r\n" +
                      "event: presence\r\n" +
                      "subscription-state: active;expires=84210\r\n" +
                      "content-encoding: x-nortel-short\r\n" +
                      "Content-Length: 234\r\n" +
                      "\r\n" +
                      "<presence xmlns=\"urn:ietf:params:cpim-presence:\" compressed=\"true\">\r\n" +
                      "<presentity id=\"fenar@techtrial.com\"/>\r\n" +
                      "<t>s4:Available</t>\r\n" +
                      "<t>p4:Available</t>\r\n" +
                      "<t>w7:Offline</t>\r\n" +
                      "<t>i7:Offline</t>\r\n" +
                      "<t>d7:Offline</t>\r\n" +
                      "<t>u7:Offline</t>\r\n" +
                      "</presence>\r\n";
     int posIdx;
     posIdx = msgBuf.indexOf(" ");
     String keyWord = msgBuf.substring(0, posIdx);
     Logger.Debug("SEND - To: " + imServerAddress + ":" + imServerPort + "\n");
     byte[] msgbytes = msgBuf.getBytes();
     if (isTCP)
     {
        tcpStack.sendMessage(msgbytes, msgbytes.length);
     }
     else
     {
        udpStack.sendMessage(msgbytes, msgbytes.length, imServerAddress, imServerPort);
     }
     startTransactionTimer(TRANSACTION_TIMEOUT);

     return 0;
   }

   private static String input()
   {
     byte[] temp = new byte[128];
     try
     {
       System.out.print("> ");
       System.in.read(temp);
       return new String(temp).trim();
     }
     catch (IOException e)
     {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
     }
   }

   //***************************************************************************
   //                           Main Menu                                      *
   //***************************************************************************

   private static void mainMenu()
   {
     System.out.println("[*Main Menu*]");
     System.out.println("1)  Send Notify - UDP");
     System.out.println("2)  Send Notify - TCP");
     System.out.println("8)  Toggle Stream Parser Activation");
     System.out.println("9)  System stop.");
     System.out.println("99) Exit.");
     
     try
     {
       int selection = Integer.parseInt(input());
       //switch (Integer.parseInt(input()))
       switch (selection)
       {
          case 1:
             System.out.println("Sending NOTIFY - UDP...");
             SendNotify(false);
             break;

           case 2:
              System.out.println("Sending NOTIFY - TCP...");
              SendNotify(true);
              break;

           case 3:
              break;

           case 4:
              break;
              
           case 5:
              break;

           case 6:
              break;

           case 7:
              break;

           case 8:
              streamParserActive = (streamParserActive > 0) ? 0 : 1;
              System.out.println("**** Current Stream Parser Activation = " + streamParserActive);
              break;
              
           case 9:
              break;

           case 99:
              System.exit(0);
              break;
       }
     }
     catch (Exception e)
     {
       //e.printStackTrace();
       menu();
     }
   }

   private static void menu()
   {
     switch (menuLevel)
     {
       case 0: // main menu
         mainMenu();
         break;
         
       default:
         menu();
     }
   }

   public void run()
   {
      Logger.setLevel(Logger.LOGLEVEL_VERBOSE);
      worker = new ServerWorker(0);
      
      ServerTestMain.tcpStack = new StackCore(ServerTestMain.localAddress, ServerTestMain.localTcpPort, this);
      try {
         tcpStack.startWithListening();
      }
      catch (IOException e2) {
         // TODO Auto-generated catch block
         e2.printStackTrace();
      }
      try {
         ServerTestMain.udpStack = new DatagramStackCore(ServerTestMain.localAddress, ServerTestMain.localPort, this);
      }
      catch (UnknownHostException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      catch (SocketException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      ServerTestMain.udpStack.startReceiving();
      
      while(true)
      {
        try
        {
          Thread.sleep(100);

//          if (System.in.available() > 0)
//          {
            menu();
//          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      
   }
   public static void main(String[] args) 
   {
      instance.run();
   }

}
