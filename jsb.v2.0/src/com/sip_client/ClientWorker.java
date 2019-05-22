package com.sip_client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.base.Logger;
//import com.base.StackCoreIF;
//import com.base.StackCore;
//import com.base.DatagramStackCore;

public class ClientWorker implements Runnable {
   int _index;
   // Pipe commandPipe;
   // AtomicBoolean cont;
   AtomicBoolean _contWork = new AtomicBoolean(true);

   public ClientWorker(int index) {
      this._index = index;
      new Thread(this, ("Worker-" + this._index)).start();
   }

   public void setContWork(boolean c) {
      this._contWork.set(c);
   }

   public boolean getContWork() {
      return this._contWork.get();
   }

   @Override
   public void run() {
      System.out.println("Worker-" + this._index + ": Started with thread-id: " + Thread.currentThread().getId());

      boolean contWork = true;

      try {
         while (contWork == true && this.getContWork() == true) {

            System.out.println("Worker-" + this._index + ": Going to read event");
            
            ReceivedMessage message = ReceivedMessageQueue.getInstance().dequeue();
            System.out.println("Worker-" + this._index + ": Got a message with keyword  --> " + message.keyWord);
            if (message != null) {
               int result = message.keyWord.compareTo("NOTIFY");
               if (result == 0) {
                  // message is NOTIFY. Then send "200 OK"
                  ClientTestMain.Send200OK(message.isTCP, message.rcvSock, message.sourceAddress, message.sourcePort);
               }
               else
               {
                  // Assumed "200 OK". Ignore it.
                  ClientTestMain.stopTransactionTimer();
               }                     
            }
         } // while(1)
      }
      catch (Exception e) {
         System.err.println("Worker-" + this._index + ": Exception occurred --> " + e.getMessage());
      }

      // We are out of the job-loop
      System.out.println("Worker-" + this._index + ": is completed the JOB");
   }

}
