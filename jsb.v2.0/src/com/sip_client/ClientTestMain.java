package com.sip_client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
//import com.base.GenericMessage;
import com.base.Logger;
import com.base.StackCore;

public class ClientTestMain implements ApplicationIF {

    public static ClientTestMain instance = new ClientTestMain();

    // Do not use in the case of binding to all adapters
    static String localAddress = "127.0.0.1";
    // To be used in listening
    static short localPort = 1812;

    static String proxyAddress = "127.0.0.1";
    static short proxyPort = 1820;
    static short proxyTcpPort = 1827;

    static DatagramStackCore udpStack = null;
    static StackCore tcpStack = null;

    //public static Timer timer;
    public static Queue<Timer> transactionTimers = new ConcurrentLinkedQueue<Timer>();
    public static final int TRANSACTION_TIMEOUT = 5000;

    public static ReceivedMessageQueue rcvedMsgQueue = ReceivedMessageQueue.getInstance();
    public static ClientWorker worker = null;

    private static int menuLevel = 0;

    public static AtomicInteger timerId = new AtomicInteger(0);

    public class WakeupTask extends TimerTask {
        int timerId;
        Timer timer;

        public WakeupTask(Timer timer, int timerid) {
            this.timerId = timerid;
            this.timer = timer;
            System.out.println("WakeupTask: Starting with id: " + this.timerId);
        }

        public void run() {
            this.timer.cancel();
            TransactionTimeroutHandler(System.currentTimeMillis(), this.timerId);
        }
    }

    public static synchronized void startTransactionTimer(int miliSeconds) {
        Timer timer = new Timer();
        WakeupTask ttask = instance.new WakeupTask(timer, timerId.incrementAndGet());
        transactionTimers.offer(timer);
        timer.schedule(ttask, miliSeconds);
        System.out.println("startTransactionTimer: Scheduled timer while size of queue is " + transactionTimers.size());
    }

    public static int stopTransactionTimer() {
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

    public static synchronized void TransactionTimeroutHandler(long currentTimestamp, int id) {
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
        stopTransactionTimer();
        String rcved = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
        int index = rcved.indexOf(' ');
        String keyword = rcved.substring(0, index);
        ReceivedMessage rcvmsg = new ReceivedMessage(socket, rcved, keyword);
        rcvedMsgQueue.enqueue(rcvmsg);

        return 0;
    }

    @Override
    /* From UDP */
    public int onReceivedMessage(byte[] rawMessage, int length, String sourceAddress, short sourcePort) {
        String msg = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
        System.out.println("RECEIVED(UDP) - " + msg);
        //stopTransactionTimer();
        String rcved = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
        int index = rcved.indexOf(' ');
        String keyword = rcved.substring(0, index);
        ReceivedMessage rcvmsg = new ReceivedMessage(sourceAddress, sourcePort, rcved, keyword);
        rcvedMsgQueue.enqueue(rcvmsg);

        return 0;
    }

//   @Override
//   public int onReceivedMessage(GenericMessage sentMessage) {
//      // TODO Auto-generated method stub
//      return 0;
//   }

    @Override
    /* From StreamMessageParser */
    /* Note that parameter 'message' consists of only headers part of message */
    public int onReceivedMessage(String message, ArrayList<String> messageHeader, byte[] messageBody) {
        /* Message received through StreamMessageParser */
        System.out.println("RECEIVED - ");
        for (int i = 0; i < messageHeader.size(); i++) {
            String header = messageHeader.get(i);
            System.out.println(header);
        }
        /* Consider that message body is in readable form */
        String body = new String(messageBody, 0, messageBody.length, StandardCharsets.UTF_8);
        System.out.println(body);

        stopTransactionTimer();

        return 0;
    }

    @Override
    public int onResponseTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

//   @Override
//   public int onResponseTimeout(GenericMessage sentMessage) {
//      // TODO Auto-generated method stub
//      return 0;
//   }

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

    public static void Send200OK(boolean isTCP, Socket socket, String targetAddr, short targetPort) {

        String msg = "SIP/2.0 200 OK\r\n" +
                "v: SIP/2.0/UDP 47.103.142.59:5060;branch=z9hG4bK-b0500-2b0b89ae-5e4a2157\r\n" +
                "f: \"fenar\"<sip:fenar@techtrial.com>;tag=1153442018376\r\n" +
                "t: \"demiry\"<sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n" +
                "i: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" +
                "CSeq: 5168788 NOTIFY\r\n" +
                "User-Agent: Nortel PCC 4.1.432\r\n" +
                "l: 0\r\n" +
                "\r\n";

        Logger.Debug("SEND - To: " + targetAddr + ":" + targetPort + "\n");
        byte[] msgbytes = msg.getBytes();
        if (isTCP) {
            try {
                OutputStream output = socket.getOutputStream();
                output.write(msgbytes);
            } catch (IOException e) {
                Logger.Excep("Send200OK::IO Problem: " + e);
            }
        } else {
            udpStack.sendMessage(msgbytes, msgbytes.length, targetAddr, targetPort);
        }
    }

    public static int SendSubscribe(boolean isTCP) {
        String msgBuf = "SUBSCRIBE sip:fenar@techtrial.com SIP/2.0\r\n" +
                "v: SIP/2.0/UDP 47.168.54.137:5060;branch=z9hG4bK45925f07-4\r\n" +
                "i: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" +
                "t: <sip:fenar@techtrial.com>;tag=1153442018376\r\n" +
                "f: <sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n" +
                "CSeq: 316 SUBSCRIBE\r\n" +
                "m: <sip:demiry@47.168.54.137>\r\n" +
                "o: presence\r\n" +
                "Expires: 86400\r\n" +
                "k: https\r\n" +
                "User-Agent: Nortel PCC 4.1.432\r\n" +
                "Accept: application/cpim-pidf+xml\r\n" +
                "Accept-Encoding: x-nortel-short\r\n" +
                "x-nt-GUID: 00303d3e5b0ec310410108b56083f1a3e1fd50\r\n" +
                "Proxy-Authorization: Digest username=\"demiry\",realm=\"Realm\"," +
                "nonce=\"MTE2NzIyMDQ1NjAwNTFmNjRlNjJhZWVhY2MxYWQ5NDI4ZTgwYWI3MzI1ZjQw\"," +
                "uri=\"sip:fenar@techtrial.com\",response=\"6166350014f137e1b7636c3b7461d5ae\"," +
                "algorithm=MD5,cnonce=\"VG05eWRHVnNJRTVsZEhkdmNtdHpMVEV3TVRBMk1UWTFNREU9\",qop=auth-int,nc=00000001\r\n" +
                "l: 0\r\n" +
                "\r\n";

        int posIdx;
        posIdx = msgBuf.indexOf(" ");
        String keyWord = msgBuf.substring(0, posIdx);
        Logger.Debug("SEND - To: " + proxyAddress + ":" + proxyPort + "\n");
        byte[] msgbytes = msgBuf.getBytes();
        if (isTCP) {
            tcpStack.sendMessage(msgbytes, msgbytes.length);
        } else {
            udpStack.sendMessage(msgbytes, msgbytes.length, proxyAddress, proxyPort);
        }
        startTransactionTimer(TRANSACTION_TIMEOUT);

        return 0;
    }

    private static String input() {
        byte[] temp = new byte[128];
        try {
            System.out.print("> ");
            System.in.read(temp);
            return new String(temp).trim();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    //***************************************************************************
    //                           Main Menu                                      *
    //***************************************************************************

    private static void mainMenu() {
        System.out.println("[*Main Menu*]");
        System.out.println("1)  Send Subscribe - UDP");
        System.out.println("2)  Send Subscribe - TCP");
        System.out.println("3)  Send Multiple Subscribe - UDP");
        System.out.println("4)  Send Multiple Subscribe - TCP");
        System.out.println("9)  System stop.");
        System.out.println("99) Exit.");

        try {
            int selection = Integer.parseInt(input());
            //switch (Integer.parseInt(input()))
            switch (selection) {
                case 1:
                    System.out.println("Sending SUBSCRIBE - UDP...");
                    SendSubscribe(false);
                    break;

                case 2:
                    System.out.println("Sending SUBSCRIBE - TCP...");
                    SendSubscribe(true);
                    break;

                case 3:
                    System.out.println("Sending Multiple SUBSCRIBE - UDP...");
                    SendSubscribe(false);
                    SendSubscribe(false);
                    SendSubscribe(false);
                    break;

                case 4:
                    System.out.println("Sending Multiple SUBSCRIBE - TCP...");
                    SendSubscribe(true);
                    SendSubscribe(true);
                    SendSubscribe(true);
                    break;

                case 5:
                    break;

                case 6:
                    break;

                case 7:
                    break;

                case 9:
                    break;

                case 99:
                    System.exit(0);
                    break;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            menu();
        }
    }

    private static void menu() {
        switch (menuLevel) {
            case 0: // main menu
                mainMenu();
                break;

            default:
                menu();
        }
    }

    public void run() {
        Logger.setLevel(Logger.LOGLEVEL_VERBOSE);
        worker = new ClientWorker(0);

        ClientTestMain.tcpStack = new StackCore(ClientTestMain.localAddress, ClientTestMain.localPort, this);
        tcpStack.startWithAttempt(proxyAddress, proxyTcpPort);
        try {
            ClientTestMain.udpStack = new DatagramStackCore(ClientTestMain.localAddress, ClientTestMain.localPort, this);
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        ClientTestMain.udpStack.startReceiving();

        while (true) {
            try {
                Thread.sleep(100);

//          if (System.in.available() > 0)
//          {
                menu();
//          }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        //ClientTestMain client = new ClientTestMain();
        //client.run();
        instance.run();
    }
}