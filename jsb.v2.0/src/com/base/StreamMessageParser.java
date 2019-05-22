package com.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This class is inspired (and partly copied from) JAIN-SIP to parse streamed
 * text based messages with bodies. It accumulates bytes until the end of 
 * message. It reports the message to the ApplicationIF bound it. Currently
 * used SIP through TCP/TLS only and (I hope) can be easily modified to work
 * for HTTP and MSRP (and for some other text-based message structures)
 *  
 * @author demiry
 *
 */
public class StreamMessageParser {
   
   protected ApplicationIF application;
   
   private static final String CRLF = "\r\n";

   private int maxMessageSize;
   private int sizeCounter;
   
   boolean isRunning = false;
   boolean currentStreamEnded = false;
   boolean readingMessageBodyContents = false;
   boolean readingHeaderLines = true;
   // if we didn't receive enough bytes for a full line we expect the line to 
   // end in the next batch of bytes
   boolean partialLineRead = false; 
   String partialLine = "";
   String callId;
   
   ArrayList<String> cloneMessage = new ArrayList<String>();
   StringBuffer message = new StringBuffer();
   byte[] mb; 
   byte[] messageBody = null;
   int contentLength = 0;
   int contentReadSoFar = 0;

//   Logger logger;
   /*
    * This is where we receive the bytes from the stream and we analyze the 
    * through message structure. For TCP the key things to identify are message
    * lines for the headers, parse the Content-Length header and then read the 
    * message body/message content. For TCP, the Content-Length must be 100% accurate.
    */
   private void readStream(InputStream inputStream) throws IOException {
      boolean isPreviousLineCRLF = false;
      while (true) {
         if (currentStreamEnded) {
            // The stream ends when we have read all bytes passed 
            break; 
         }
         if (readingHeaderLines) {
            // We are in state to read header lines right now
            isPreviousLineCRLF = readMessageSipHeaderLines(inputStream, isPreviousLineCRLF);
         }
         if (readingMessageBodyContents) { 
            // We've already read the headers an now we are reading the Contents of
            // the message (which doesn't generally have lines)
            readMessageBody(inputStream);
         }
      }
   }

   private boolean readMessageSipHeaderLines(InputStream inputStream, boolean isPreviousLineCRLF) throws IOException {
      boolean crlfReceived = false;
      String line = readLine(inputStream);
      // This gives us a full line or if it didn't fit in the byte check it may
      // give us part of the line
      if (partialLineRead) {
         // If we are reading partial line again we must concatenate it with the
         // previous partial line to reconstruct the full line
         partialLine = partialLine + line; 
      } 
      else {
         // If we reach the end of the line in this chunk we concatenate it with the
         // partial line from the previous buffer to have a full line
         line = partialLine + line;
         // Reset the partial line so next time we will concatenate empty string instead of the
         // obsolete partial line that we just took care of
         partialLine = "";       
         // CRLF indicates END of message headers by RFC
         if (!line.equals(CRLF)) {
            // Collect the line so far in the message buffer (line by line)
            message.append(line); 
            cloneMessage.add(line);
            String lineIgnoreCase = line.toLowerCase();
            // compare to lower case as RFC 3261 states (7.3.1 Header Field
            // Format) states that header fields are case-insensitive
            //if (lineIgnoreCase.startsWith(ContentLengthHeader.NAME.toLowerCase())) {
//            if (lineIgnoreCase.startsWith("content-length")) {
            if (((lineIgnoreCase.length()==1) && lineIgnoreCase.startsWith("l")) ||
                lineIgnoreCase.startsWith("content-length")) {
               // naive Content-Length header parsing to figure out how much 
               // bytes of message body must be read after the SIP headers
               //contentLength = Integer.parseInt(line.substring(ContentLengthHeader.NAME.length() + 1).trim());
               contentLength = Integer.parseInt(line.substring("content-length".length() + 1).trim());
            } 
            //else if (lineIgnoreCase.startsWith(CallIdHeader.NAME.toLowerCase())) {
            else if (lineIgnoreCase.startsWith("call-id".toLowerCase())) {
               // Not sure we need to collect call-id
               //callId = line.substring(CallIdHeader.NAME.length() + 1).trim();
               callId = line.substring("call-id".length() + 1).trim();
            }
         } 
         else {
            if (isPreviousLineCRLF) {
               // Handling keepalive ping (double CRLF) as defined per RFC 5626 Section 4.4.1
               // sending pong (single CRLF)
               Logger.Debug("StreamMessageParser: KeepAlive Double CRLF received, sending single CRLF as defined per RFC 5626 Section 4.4.1");
               Logger.Debug("StreamMessageParser: ~~~ setting isPreviousLineCRLF=false");

               crlfReceived = false;

               /* TODO: The following code should be activated to support keepalive-CRLF */
               try {
                  //sipMessageListener.sendSingleCLRF();
               } 
               catch (Exception e) {
                  //logger.error("A problem occured while trying to send a single CLRF in response to a double CLRF",
                  //    e);
               }
            } 
            else {
               crlfReceived = true;
               Logger.Debug("StreamMessageParser: Received CRLF");
            }
            if (message.length() > 0) { // if we havent read any headers yet we are between messages and ignore
                                        // CRLFs
               readingMessageBodyContents = true;
               readingHeaderLines = false;
               partialLineRead = false;
               message.append(CRLF); // the parser needs CRLF at the end, otherwise fails TODO: Is that a bug?
               cloneMessage.add(CRLF);
               Logger.Debug("StreamMessageParser: Content Length parsed is " + contentLength);

               contentReadSoFar = 0;
               readingMessageBodyContents = true;
               messageBody = new byte[contentLength];
            }
         }
      }
      return crlfReceived;
   }
   
   // This method must be called repeatedly until the inputStream returns -1 or some error conditions is triggered
   private void readMessageBody(InputStream inputStream) throws IOException {
      int bytesRead = 0;
      if (contentLength > 0) {
         bytesRead = readChunk(inputStream, messageBody, contentReadSoFar, contentLength - contentReadSoFar);
         if (bytesRead == -1) {
            currentStreamEnded = true;
            bytesRead = 0; // avoid passing by a -1 for a one-off bug when contentReadSoFar gets wrong
         }
      }
      contentReadSoFar += bytesRead;
      if (contentReadSoFar == contentLength) { 
         // We have read the full message headers + body
         sizeCounter = maxMessageSize;
         readingHeaderLines = true;
         readingMessageBodyContents = false;
         //final String msgLines = message.toString();
         //message = new StringBuffer();
         final byte[] msgBodyBytes = messageBody;
         /* Note that current 'message' consists of only headers part of message */
         this.application.onReceivedMessage(message.toString(), cloneMessage, msgBodyBytes);
         this.cloneMessage = new ArrayList<String>();
         message = new StringBuffer();
      }
   }

   
   public synchronized void addBytes(byte[] bytes) throws IOException {
      currentStreamEnded = false;
      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
      readStream(inputStream);
   }
   
   public void setMessageBody(byte[] b) {
      mb = b;
   }
   
   public ArrayList<String> getMessage() {
      return cloneMessage;
   }
   
   public byte[] getMessageBody() {
      return mb;
   }
   
   /**
    * This is the constructor for stackless mode.
    * 
    * @param mhandler
    *            a SIPMessageListener implementation that provides the message handlers to handle correctly and
    *            incorrectly parsed messages.
    * @param maxMsgSize
    *            The maximum allowed size of a SIP message.
    */
   public StreamMessageParser(int maxMsgSize, ApplicationIF application) {
      this.application = application;
      this.maxMessageSize = maxMsgSize;
      this.sizeCounter = this.maxMessageSize;
//      this.logger = logger;
   }

   private int readChunk(InputStream inputStream, byte[] where, int offset, int length) throws IOException {
      int read = inputStream.read(where, offset, length);
      sizeCounter -= read;
      checkLimits();
      return read;
   }
   
   private int readSingleByte(InputStream inputStream) throws IOException {
      sizeCounter--;
      checkLimits();
      return inputStream.read();
   }

   private void checkLimits() {
      if (maxMessageSize > 0 && sizeCounter < 0) {
         throw new RuntimeException("Max Message Size Exceeded " + maxMessageSize);
      }
   }

   /**
    * read a line of input. Note that we encode the result in UTF-8
    */
   private String readLine(InputStream inputStream) throws IOException {
      partialLineRead = false;
      int counter = 0;
      int increment = 1024;
      int bufferSize = increment;
      byte[] lineBuffer = new byte[bufferSize];
      // handles RFC 5626 CRLF keepalive mechanism
      byte[] crlfBuffer = new byte[2];
      int crlfCounter = 0;
      while (true) {
         char ch;
         int i = readSingleByte(inputStream);
         if (i == -1) {
            partialLineRead = true;
            currentStreamEnded = true;
            break;
         } 
         else {
            ch = (char) (i & 0xFF);
         }

         if (ch != '\r') {
            lineBuffer[counter++] = (byte) (i & 0xFF);
         }
         else if (counter == 0) {
            crlfBuffer[crlfCounter++] = (byte) '\r';
         }
         if (ch == '\n') {
            if (counter == 1 && crlfCounter > 0) {
               crlfBuffer[crlfCounter++] = (byte) '\n';
            }
            break;
         }

         if (counter == bufferSize) {
            byte[] tempBuffer = new byte[bufferSize + increment];
            System.arraycopy((Object) lineBuffer, 0, (Object) tempBuffer, 0, bufferSize);
            bufferSize = bufferSize + increment;
            lineBuffer = tempBuffer;

         }
      }
      if (counter == 1 && crlfCounter > 0) {
         return new String(crlfBuffer, 0, crlfCounter, "UTF-8");
      } 
      else {
         return new String(lineBuffer, 0, counter, "UTF-8");
      }
   }
   
   public static void testForSIPMessage()
   {
      String msgStr = "SUBSCRIBE sip:fenar@techtrial.com SIP/2.0\r\n" +
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
                      "l: 0\r\n";

      StreamMessageParser smp = new StreamMessageParser(4096, null);
      try {
         smp.addBytes(msgStr.getBytes());
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public static void main(String[] args) 
   {
      testForSIPMessage();
   }
}
