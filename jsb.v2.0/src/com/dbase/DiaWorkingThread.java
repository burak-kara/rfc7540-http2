package com.dbase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.base.ApplicationIF;

public class DiaWorkingThread implements Runnable {
   int _index;
   // Pipe commandPipe;
   // AtomicBoolean cont;
   AtomicBoolean _contWork = new AtomicBoolean(true);
   ApplicationIF application;

   //public DiaWorkingThread(int index) {
   public DiaWorkingThread(int index, ApplicationIF app) {
      this._index = index;
      this.application = app;
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
      System.out.println("DiaWorker-" + this._index + ": Started with thread-id: " + Thread.currentThread().getId());

      boolean contWork = true;

      try {
         while (contWork == true && this.getContWork() == true) {

            System.out.println("DiaWorker-" + this._index + ": Going to read event");
            
            ReceivedRawMessage message = ReceivedRawMessageQueue.getInstance().dequeue();
            System.out.println("DiaWorker-" + this._index + ": Got a message");
            if (message != null) {
               this.application.onReceivedMessage(message.message, message.length, message.rcvSock);
               
//               int result = message.keyWord.compareTo("SUBSCRIBE");
//               if (result == 0) {
//                  // message is SUBSCRIBE. Then send "200 OK"
//                  ServerTestMain.Send200OK(message.isTCP, message.rcvSock, message.sourceAddress, message.sourcePort);
//               }
//               else
//               {
//                  // Assumed "200 OK". Ignore it.
//                  ServerTestMain.stopTransactionTimer();
//               }                     
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
