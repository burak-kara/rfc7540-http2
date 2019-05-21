package com.base;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Receiver implements Runnable {
   static public final int buffer_size = 4096;
   
   protected Socket sock;
   protected StackCore core;
   
   protected AtomicBoolean stopWorking = new AtomicBoolean(false);
   
   protected InputStream inStream;
   
//   Logger logger;
   
   protected String name;
   
   public Receiver(Socket s, StackCore core) {
      this.sock = s;
      this.core = core;
//      this.logger = logger;
   }

   public Receiver(String name, Socket s, StackCore core) {
      this.name = name;
      this.sock = s;
      this.core = core;
//      this.logger = logger;
   }

   @Override
   public void run() {
      byte[] buff = new byte[buffer_size];
      int startOff = 0;
      try {
         this.inStream  = sock.getInputStream();
         while(! this.stopWorking.get()) {
            if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
               Logger.Debug("first--Receiver starting read with startOff = " + startOff );
            }
            int len = this.inStream.read(buff, startOff, (buffer_size-startOff));
            
            //System.out.println("Receiver read len = " + len);
            if (len < 0) {
               break;
            }
//            if (this.logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
//               String readBuff = new String(buff, 0, len+startOff);
//               this.logger.Debug("Received Message (len = " + readBuff.length() + ")\n" + readBuff);
//               this.logger.Debug("Received Message calling for length = " + (len+startOff));
//            }   
            byte[] newbuff = core.messageReceived(buff, len+startOff, sock);
            /* since TCP is a stream-based protocol no message boundaries are 
             * followed/provided. Therefore, after the processing there either
             *  - message is not completed yet with the received part, or
             *  - there are byte more than a message (that is related to the
             *    another message)
             *  In both cases, the message handler gives back all/some part
             *  of the message. We should put it into the buffer back to wait
             *  it to be completed.
             *  Note that, message handler is responsible for removing the
             *  bytes that are not related to a message that is expected
             *  (such as an unknown protocol message)
             */
            if (newbuff == null) {
               /* assume all part of buffer processed */ 
               startOff = 0;
               if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
                  Logger.Debug("Receiver starting read with startOff = " + startOff );
               }
            }
            else {
               /* there is a remaining part, put it into buffer and
                * wait for remaining part
                */
               System.arraycopy(newbuff, 0, buff, 0, newbuff.length);
               startOff = newbuff.length+1;
               if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
                  Logger.Debug("Receiver starting read with startOff = " + startOff );
               }
            }
         }
      } 
      catch (IOException e) {
       if (stopWorking.get() == false) {
          Logger.Excep("Exception on receiver: " + e + sock.toString());
          if (e instanceof SocketException) {
             this.core.connectionDisconnected(sock);
          }
       }
      }
      catch(StringIndexOutOfBoundsException e){
         Logger.Excep("Exception on receiver");
         e.printStackTrace();
      }
      try {
         this.sock.close();
      } 
      catch (IOException e) {
         throw new RuntimeException("Error closing receiver socket", e);
      }   
   }
   
   public synchronized void stop(){
      this.stopWorking.set(true); 
      Logger.Debug("Stoping the receiver ... " + this.stopWorking.get());
      Logger.Debug("Stoping the receiver ... " + this.sock.toString());
      if ((this.sock != null) && (!this.sock.isClosed())) {
         try {
            this.sock.close();
         } 
         catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

}
