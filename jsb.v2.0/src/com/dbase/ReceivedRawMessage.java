package com.dbase;

import java.net.Socket;

public class ReceivedRawMessage {
   /* For received messages through TCP */
   public ReceivedRawMessage(Socket sock, byte[] msg, int length)
   {
//      this.isTCP = true;
      this.rcvSock = sock;
//      this.sourceAddress = "";
//      this.sourcePort = 0;
      this.message = msg;
      this.length = length;
   }

//   boolean isTCP;
   Socket rcvSock; /* meaningful for TCP case */
   /* Meaningful for UDP case */
//   String sourceAddress;
//   short sourcePort;
   
   byte[] message;
   int    length;
}
