package com.sip_server;

import java.net.Socket;

public class ReceivedMessage 
{
  /* For received messages through UDP */
  public ReceivedMessage(String srcAddr, short srcPort, String msg, String keyword)
  {
     this.isTCP = false;
     this.rcvSock = null;
     this.sourceAddress = srcAddr;
     this.sourcePort = srcPort;
     this.message = msg;
     this.keyWord = keyword;
  }

  /* For received messages through TCP */
  public ReceivedMessage(Socket sock, String msg, String keyword)
  {
     this.isTCP = true;
     this.rcvSock = sock;
     this.sourceAddress = "";
     this.sourcePort = 0;
     this.message = msg;
     this.keyWord = keyword;
  }

  boolean isTCP;
  Socket rcvSock; /* meaningful for TCP case */
  /* Meaningful for UDP case */
  String sourceAddress;
  short sourcePort;
  
  String message;
  String keyWord;
}

