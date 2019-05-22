package com.sip_server;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerWorker implements Runnable {
   int _index;
   // Pipe commandPipe;
   // AtomicBoolean cont;
   AtomicBoolean _contWork = new AtomicBoolean(true);

   public ServerWorker(int index) {
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
      System.out.println("SWorker-" + this._index + ": Started with thread-id: " + Thread.currentThread().getId());

      boolean contWork = true;

      try {
         while (contWork == true && this.getContWork() == true) {

            System.out.println("SWorker-" + this._index + ": Going to read event");
            
            ReceivedMessage message = ReceivedMessageQueue.getInstance().dequeue();
            System.out.println("SWorker-" + this._index + ": Got a message with keyword  --> " + message.keyWord);
            if (message != null) {
               int result = message.keyWord.compareTo("SUBSCRIBE");
               if (result == 0) {
                  // message is SUBSCRIBE. Then send "200 OK"
                  ServerTestMain.Send200OK(message.isTCP, message.rcvSock, message.sourceAddress, message.sourcePort);
               }
               else
               {
                  // Assumed "200 OK". Ignore it.
                  ServerTestMain.stopTransactionTimer();
               }                     
            }
         } // while(1)
      }
      catch (Exception e) {
         System.err.println("SWorker-" + this._index + ": Exception occurred --> " + e.getMessage());
      }

      // We are out of the job-loop
      System.out.println("SWorker-" + this._index + ": is completed the JOB");
   }

}
