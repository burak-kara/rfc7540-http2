package com.dbase;

import java.io.IOException;
import java.io.InputStream;
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
//import com.base.DatagramStackCore;
import com.base.Logger;
import com.base.StackCore;
import com.dparser.DiameterAVP;
import com.dparser.DiameterBuildException;
import com.dparser.DiameterCommonData;
import com.dparser.DiameterDefinitions;
import com.dparser.DiameterException;
import com.dparser.DiameterHeader;
import com.dparser.DiameterMessage;
//import com.base.StackCore;
//import com.base.StreamMessageParser;
//import com.sip_server.ReceivedMessage;
//import com.sip_server.ReceivedMessageQueue;
//import com.sip_server.ServerTestMain;
//import com.sip_server.ServerWorker;
//import com.sip_server.ServerTestMain.WakeupTask;
import com.dparser.DiameterUtilities;
import com.dparser.GenericDiameterMessage;
import com.sip_client.ClientTestMain;

public class DiaTestHead implements ApplicationIF {

   public static DiaTestHead instance = new DiaTestHead();

//   // Do not use in the case of binding to all adapters
//   static String localAddress = "127.0.0.1";
//   // To be used in listening
//   static short localPort = 1820;
//   static short localTcpPort = 1827;
//
//   static String imServerAddress = "127.0.0.1";
//   static short imServerPort = 1812;

    static boolean clientMode = false;

    static String clientModeLocalAddr = "127.0.0.1";
    static short clientModeLocalPort = 1812;
    static String clientModeRemoteAddr = "127.0.0.1";
    static short clientModeRemotePort = 1820;

    static String serverModeLocalAddr = "127.0.0.1";
    static short serverModeLocalPort = 1820;
    static String serverModeRemoteAddr = "127.0.0.1";
    static short serverModeRemotePort = 1812;

    // Use server ode settings by default
    // Do not use in the case of binding to all adapters
    static String localAddress = "127.0.0.1";
    // To be used in listening
    static short localPort = 1820;
    //unsigned short DiaTestHead::localTcpPort = 1827;

    static String remoteAddress = "127.0.0.1";
    static short remotePort = 1812;

    //static DatagramStackCore udpStack = null;
    static StackCore tcpStack = null;
    Socket clientConnSocket = null;

    public static Queue<Timer> transactionTimers = new ConcurrentLinkedQueue<>();
    public static final int TRANSACTION_TIMEOUT = 5000;

    public static ReceivedRawMessageQueue rcvedMsgQueue = ReceivedRawMessageQueue.getInstance();
    public static DiaWorkingThread worker = null;

    public static int streamParserActive = 0;
    //public static StreamMessageParser parser = new StreamMessageParser(4096, instance);

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
        /*
         * No mapping with the current transaction is considered. Just get the
         * first item from the timer-queue to stop
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
        Logger.Error("TransactionTimeroutHandler: Transaction timer with Id = " + id + " is expired at time = "
                + currentTimestamp + " while size: " + transactionTimers.size() + "\n");
    }

    @Override
    public boolean receiveMessage(Socket sock) {
        boolean result = false;

        int recvMsgSize;

        int ptr; /* indicates position in byte-buffer */
        long first4Bytes = 0;
        int requestedLen = 4;
        int firstTime = 1;
        int totalLen = 0;

        //unsigned char *message = NULL;
        //char *messagebuff = NULL;
        byte[] messagebuff;

        InputStream inStream;

        // Read the first 4 bytes which include the length of the message
        //ptr = (char*)&(first4Bytes);
        messagebuff = new byte[requestedLen];
        ptr = 0;

        try {
            inStream = sock.getInputStream();
            // int len = this.inStream.read(buff, startOff,
            // (buffer_size-startOff));
            // while ((recvMsgSize = recvSocket->Recv(ptr, requestedLen)) > 0) //
            // Zero means end of transmission
            while ((recvMsgSize = inStream.read(messagebuff, ptr, requestedLen)) > 0) {
                if (recvMsgSize < requestedLen) {
                    requestedLen -= recvMsgSize;
                    ptr += recvMsgSize;
                } else {
                    if (firstTime == 1) {
                        firstTime = 0;
                        // totalLen = ntohl(first4Bytes) & 0x00ffffff;
                        //totalLen = (int) (DiameterUtilities.get4BytesAsUnsigned32(messagebuff, ptr) & 0x00ffffff);
                        first4Bytes = DiameterUtilities.get4BytesAsUnsigned32(messagebuff, ptr); // & 0x00ffffff;
                        totalLen = (int) (first4Bytes & 0x00ffffff);
                        // message = new unsigned char[totalLen];
                        messagebuff = new byte[totalLen];
                        // *((unsigned int*)messagebuff) = first4Bytes;
                        //DiameterUtilities.set4Bytes(messagebuff, 0, totalLen);
                        DiameterUtilities.set4Bytes(messagebuff, 0, first4Bytes);
                        ptr += 4;
                        requestedLen = totalLen - 4;
                    } else {
                        // *msg = message;
                        // *len = totalLen;
                        ReceivedRawMessage message = new ReceivedRawMessage(sock, messagebuff, totalLen);
                        rcvedMsgQueue.enqueue(message);

                        return true; // return point for normal operation
                    }
                }
            } // while

            // recvMsgSize == 0 or recvMsgSize = -1
            if (recvMsgSize == 0) {
                Logger.Info("DiaTestHead::receiveMessage: 0 length received\n");
                return true; // DC_FINRECEIVED; TODO: Handle in logical way
            } else {
                // recvMsgSize == -1
                // Socket is closed by remote somehow (without DPR)
                Logger.Info("DiaTestHead::receiveMessage: -1 length received\n");
                return false; // DC_SOCKETERROR; TODO: Throw an exception
            }
        } catch (IOException e) {
            Logger.Excep("Exception on receiver: " + e + sock.toString());
            if (e instanceof SocketException) {
                //this.core.connectionDisconnected(sock);
            }
        }

        return result;
    }

    public DiameterMessage BuildResponse(DiameterMessage req) {
        DiameterHeader header = req.getHeader();
        byte flags = header.getFlagsByte();
        flags &= ~DiameterDefinitions.HFLAGS_R_BIT_MASK;
        header.setFlagsByte(flags);
        //DiameterHeader header = new DiameterHeader(req.getHeader()byte version, byte flags, int commandCode, long applicationId, long hbhId, long eteId)

        GenericDiameterMessage resp = new GenericDiameterMessage(header, null);

        DiameterAVP sessid = req.getAvp(DiameterDefinitions.AVP_SESSION_ID);
        if (sessid != null) {
            try {
                resp.addUTF8Avp(sessid.getAvpCode(), sessid.getFlagsByte(), sessid.getVendorId(), sessid.getStringData());
                resp.addUnsigned32Avp(DiameterDefinitions.AVP_RESULT_CODE, DiameterDefinitions.AVP_Mflag,
                        DiameterDefinitions.DIAMETER_APPID_COMMON_MESSAGES, DiameterDefinitions.DIAMETER_SUCCESS);
                resp.addUTF8Avp(DiameterDefinitions.AVP_ORIGIN_HOST, DiameterDefinitions.AVP_Mflag,
                        DiameterDefinitions.DIAMETER_APPID_COMMON_MESSAGES, DiameterCommonData.getOriginHost());
                resp.addUTF8Avp(DiameterDefinitions.AVP_ORIGIN_REALM, DiameterDefinitions.AVP_Mflag,
                        DiameterDefinitions.DIAMETER_APPID_COMMON_MESSAGES, DiameterCommonData.getOriginRealm());
            } catch (DiameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return resp;
    }

    @Override
    // public int onReceivedMessage(byte[] rawMessage, int length) {
    public int onReceivedMessage(byte[] rawMessage, int length, Socket socket) {
        //String msg = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
        //System.out.println("RECEIVED(TCP) - " + msg);
        StringBuilder stb = new StringBuilder();
        DiameterUtilities.printMessageBuffer(stb, rawMessage, 0, length);
        System.out.println(stb.toString());

        DiameterMessage msg = null;
        try {
            msg = DiameterMessage.parseRawDataForMessage(rawMessage);
        } catch (DiameterException e) {
            e.printStackTrace();
            return 1;
        }
        StringBuilder mstb = new StringBuilder();
        msg.printContent(mstb);
        System.out.println(mstb.toString());

        if (!msg.getHeader().isRequest()) {
            System.out.println("Received a response...");
            return 0;
        }
        /* Build response back */
        byte[] rawresp = null;
        DiameterMessage resp = BuildResponse(msg);
        try {
            rawresp = resp.buildMessageToRaw();
        } catch (DiameterBuildException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (rawresp != null) {
            OutputStream output;
            try {
                output = socket.getOutputStream();
                output.write(rawresp);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return 0;
    }

    @Override
    /* From UDP */
    public int onReceivedMessage(byte[] rawMessage, int length, String sourceAddress, short sourcePort) {
        //String msg = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
        System.out.println("RECEIVED(UDP) - Not expected to be invoked");

//      stopTransactionTimer();
//      String rcved = new String(rawMessage, 0, length, StandardCharsets.UTF_8);
//      int index = rcved.indexOf(' ');
//      String keyword = rcved.substring(0, index);
//      ReceivedMessage rcvmsg = new ReceivedMessage(sourceAddress, sourcePort, rcved, keyword);
//      rcvedMsgQueue.enqueue(rcvmsg);


        return 0;
    }

    @Override
    /* From StreamMessageParser */
    /* Note that parameter 'message' consists of only headers part of message */
    public int onReceivedMessage(String message, ArrayList<String> messageHeader, byte[] messageBody) {
        System.out.println("From StreamMessageParser - Not expected to be invoked");

//      System.out.println("HEADERS:");
//      int vsize = messageHeader.size();
//      for (int i = 0; i < vsize; i++) {
//         System.out.print(messageHeader.get(i));
//      }
//      System.out.print("\n");
//      System.out.print("MESSAGE BODY:\n");
//      int blen = messageBody.length;
//      for (int i = 0; i < blen; i++) {
//         System.out.print(messageBody[i]);
//      }
//      System.out.print("\n");
//
//      int posIdx;
//      posIdx = messageHeader.get(0).indexOf(' ');
//      System.out.println("posIdx = " + posIdx + "\n");
//      String keyWord = messageHeader.get(0).substring(0, posIdx);
//      System.out.println("keyWord = " + keyWord + "\n");
//
//      // Socket* recvSocket = (Socket*)userdata;
//      /* Note that current msgb consists of only headers part of message */
//      // std::string rcvMsg((char*)msgb, length);
//      ReceivedRawMessage rcvmsg = new ReceivedRawMessage(clientConnSocket, message, keyWord);
//      rcvedMsgQueue.enqueue(rcvmsg);

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

//   public static void Send200OK(boolean isTCP, Socket rcvsock, String targetAddr, short targetPort) {
//      String msg = "SIP/2.0 200 OK\r\n"
//            + "From: \"Demir Yavas\"<sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n"
//            + "To: \"Fatih Nar\"<sip:fenar@techtrial.com>;tag=1153442018376\r\n"
//            + "Call-ID: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" + "CSeq: 316 SUBSCRIBE\r\n"
//            + "Via: SIP/2.0/UDP 47.168.54.137:5060;branch=z9hG4bK45925f07-4\r\n"
//            + "contact: <sip:fenar@techtrial.com:5060;maddr=47.103.142.59>\r\n" + "expires: 84210\r\n"
//            + "supported: x-nt-ss-otpp\r\n" + "Content-Length: 0\r\n" + "\r\n";
//
//      Logger.Debug("SEND - To: " + targetAddr + ":" + targetPort + "\n");
//      byte[] msgbytes = msg.getBytes();
//      if (isTCP) {
//         try {
//            OutputStream output = rcvsock.getOutputStream();
//            output.write(msgbytes);
//         }
//         catch (IOException e) {
//            Logger.Excep("Send200OK::IO Problem: " + e);
//         }
//      }
//      else {
//         udpStack.sendMessage(msgbytes, msgbytes.length, targetAddr, targetPort);
//      }
//   }
//
//   public static int SendNotify(boolean isTCP) {
//      String msgBuf = "NOTIFY sip:demiry@47.168.54.137:5060;nt_info=proxy SIP/2.0\r\n"
//            + "From: \"fenar\"<sip:fenar@techtrial.com>;tag=1153442018376\r\n"
//            + "To: \"demiry\"<sip:demiry@techtrial.com>;tag=5cf6-cd4-fe0-a068744e\r\n"
//            + "Call-ID: 70b8-0cd4-0fe0-a068744e@47.165.156.95\r\n" + "CSeq: 5168788 NOTIFY\r\n"
//            + "Via: SIP/2.0/UDP 47.103.142.59:5060;branch=z9hG4bK-b0500-2b0b89ae-5e4a2157\r\n"
//            + "content-type: application/cpim-pidf+xml\r\n" + "max-forwards: 20\r\n"
//            + "supported: com.nortelnetworks.firewall,p-3rdpartycontrol,nosec,join,com.nortelnetworks.im.encryption\r\n"
//            + "subscription-expires: 84210\r\n" + "event: presence\r\n" + "subscription-state: active;expires=84210\r\n"
//            + "content-encoding: x-nortel-short\r\n" + "Content-Length: 234\r\n" + "\r\n"
//            + "<presence xmlns=\"urn:ietf:params:cpim-presence:\" compressed=\"true\">\r\n"
//            + "<presentity id=\"fenar@techtrial.com\"/>\r\n" + "<t>s4:Available</t>\r\n" + "<t>p4:Available</t>\r\n"
//            + "<t>w7:Offline</t>\r\n" + "<t>i7:Offline</t>\r\n" + "<t>d7:Offline</t>\r\n" + "<t>u7:Offline</t>\r\n"
//            + "</presence>\r\n";
//      int posIdx;
//      posIdx = msgBuf.indexOf(" ");
//      String keyWord = msgBuf.substring(0, posIdx);
//      Logger.Debug("SEND - To: " + imServerAddress + ":" + imServerPort + "\n");
//      byte[] msgbytes = msgBuf.getBytes();
//      if (isTCP) {
//         tcpStack.sendMessage(msgbytes, msgbytes.length);
//      }
//      else {
//         udpStack.sendMessage(msgbytes, msgbytes.length, imServerAddress, imServerPort);
//      }
//      startTransactionTimer(TRANSACTION_TIMEOUT);
//
//      return 0;
//   }


    static int SendUDR(int id) {
        final byte message[] = new byte[]
                {0x01, 0x00, 0x01, 0x18, (byte) 0xc0, 0x00, 0x01, 0x32, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x2b, 0x22, (byte) 0xa7, 0x00, 0x00, 0x01, 0x07, 0x40, 0x00, 0x00, 0x29, 0x6e, 0x65, 0x74, 0x61,
                        0x73, 0x2e, 0x63, 0x6f, 0x6d, 0x3a, 0x33, 0x33, 0x30, 0x34, 0x31, 0x3b, 0x32, 0x33, 0x34, 0x33,
                        0x32, 0x3b, 0x38, 0x39, 0x33, 0x3b, 0x30, 0x41, 0x46, 0x33, 0x42, 0x38, 0x31, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x01, 0x04, 0x40, 0x00, 0x00, 0x20, 0x00, 0x00, 0x01, 0x0a, 0x40, 0x00, 0x00, 0x0c,
                        0x00, 0x00, 0x28, (byte) 0xaf, 0x00, 0x00, 0x01, 0x02, 0x40, 0x00, 0x00, 0x0c, 0x01, 0x00, 0x00, 0x01,
                        0x00, 0x00, 0x01, 0x15, 0x40, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x01, 0x08,
                        0x40, 0x00, 0x00, 0x10, 0x62, 0x69, 0x73, 0x74, 0x6c, 0x31, 0x38, 0x35, 0x00, 0x00, 0x01, 0x28,
                        0x40, 0x00, 0x00, 0x11, 0x6e, 0x6f, 0x72, 0x74, 0x65, 0x6c, 0x2d, 0x41, 0x53, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x01, 0x25, 0x00, 0x00, 0x00, 0x15, 0x6e, 0x6f, 0x74, 0x65, 0x6c, 0x2d, 0x68, 0x73,
                        0x73, 0x2e, 0x63, 0x6f, 0x6d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x1b, 0x40, 0x00, 0x00, 0x12,
                        0x6e, 0x6f, 0x72, 0x74, 0x65, 0x6c, 0x2d, 0x68, 0x73, 0x73, 0x00, 0x00, 0x00, 0x00, 0x02, (byte) 0xbc,
                        (byte) 0xc0, 0x00, 0x00, 0x34, 0x00, 0x00, 0x28, (byte) 0xaf, 0x00, 0x00, 0x02, 0x59, (byte) 0xc0, 0x00, 0x00, 0x27,
                        0x00, 0x00, 0x28, (byte) 0xaf, 0x73, 0x69, 0x70, 0x3a, 0x75, 0x73, 0x65, 0x72, 0x31, 0x5f, 0x70, 0x75,
                        0x62, 0x6c, 0x69, 0x63, 0x31, 0x40, 0x68, 0x6f, 0x6d, 0x65, 0x31, 0x2e, 0x6e, 0x65, 0x74, 0x00,
                        0x00, 0x00, 0x02, (byte) 0xc0, (byte) 0xc0, 0x00, 0x00, 0x15, 0x00, 0x00, 0x28, (byte) 0xaf, 0x6d, 0x65, 0x73, 0x73,
                        0x65, 0x6e, 0x67, 0x65, 0x72, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte) 0xbf, (byte) 0xc0, 0x00, 0x00, 0x10,
                        0x00, 0x00, 0x28, (byte) 0xaf, 0x00, 0x00, 0x00, 0x00};

        if (tcpStack.isConnected()) {
            tcpStack.sendMessage(message, 280);
        } else {
            System.err.println("SendUDR: Stack has no connection...");
        }

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

    // ***************************************************************************
    // Main Menu *
    // ***************************************************************************

    private static void mainMenu() {
        System.out.println("[*Main Menu*]");
        System.out.println("1)  Send Request");
        System.out.println("2)  Send Response");
        System.out.println("9)  System stop.");
        System.out.println("99) Exit.");

        try {
            int selection = Integer.parseInt(input());
            // switch (Integer.parseInt(input()))
            switch (selection) {
                case 1:
                    System.out.println("Sending Request...");
//               SendNotify(false);
                    DiaTestHead.SendUDR(0);
                    break;

                case 2:
                    System.out.println("Sending Response...");
//               SendNotify(true);
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
        } catch (Exception e) {
            // e.printStackTrace();
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

    public void run(boolean clientmode) {
        clientMode = clientmode;
        Logger.setLevel(Logger.LOGLEVEL_VERBOSE);
        if (!clientMode) {
            DiameterCommonData.setOriginHost("DiaServer");
        } else {
            DiameterCommonData.setOriginHost("DiaClient");
        }
        DiameterCommonData.setOriginRealm("example.com.tr");

        worker = new DiaWorkingThread(0, this);

        if (clientMode) {
            //DiaTestHead.tcpStack = new StackCore(DiaTestHead.localAddress, DiaTestHead.localPort, this);
            DiaTestHead.tcpStack = new DiameterStackCore(DiaTestHead.localAddress, DiaTestHead.localPort, this);
            DiaTestHead.tcpStack.startWithAttempt(DiaTestHead.remoteAddress, DiaTestHead.remotePort);
        } else {
            //DiaTestHead.tcpStack = new StackCore(DiaTestHead.localAddress, DiaTestHead.localPort, this);
            DiaTestHead.tcpStack = new DiameterStackCore(DiaTestHead.localAddress, DiaTestHead.localPort, this);
            try {
                tcpStack.startWithListening();
            } catch (IOException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
        }
//      try {
//         ServerTestMain.udpStack = new DatagramStackCore(ServerTestMain.localAddress, ServerTestMain.localPort, this);
//      }
//      catch (UnknownHostException e1) {
//         // TODO Auto-generated catch block
//         e1.printStackTrace();
//      }
//      catch (SocketException e1) {
//         // TODO Auto-generated catch block
//         e1.printStackTrace();
//      }
//      ServerTestMain.udpStack.startReceiving();

        while (true) {
            try {
                Thread.sleep(100);

                // if (System.in.available() > 0)
                // {
                menu();
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        boolean clientmode = false;
        if (args.length >= 1) {
            String ch = args[0];
            if (ch.equals("1")) {
                clientmode = true;
                /* modify address and port settings */
                DiaTestHead.localAddress = DiaTestHead.clientModeLocalAddr;
                DiaTestHead.localPort = DiaTestHead.clientModeLocalPort;
                DiaTestHead.remoteAddress = DiaTestHead.clientModeRemoteAddr;
                DiaTestHead.remotePort = DiaTestHead.clientModeRemotePort;
            }
        }
        instance.run(clientmode);
    }

}
