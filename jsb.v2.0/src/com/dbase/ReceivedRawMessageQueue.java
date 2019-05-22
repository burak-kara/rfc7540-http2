package com.dbase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//import com.sip_server.ReceivedMessage;
//import com.sip_server.ReceivedMessageQueue;

public class ReceivedRawMessageQueue {

   BlockingQueue<ReceivedRawMessage> messageQueue;
   
   //long maxQueueSize;
   
   static final int DEF_QUEUE_SIZE = 100000;
   
   private static ReceivedRawMessageQueue instance = new ReceivedRawMessageQueue();
   
   private ReceivedRawMessageQueue() {
      this.messageQueue = new LinkedBlockingQueue<ReceivedRawMessage>(DEF_QUEUE_SIZE);
      this.messageQueue.size();
      //this.maxQueueSize = DEF_QUEUE_SIZE;
   }
   
   public static ReceivedRawMessageQueue getInstance() {
      return instance;
   }
   
//   public void setMaxQueueSize(long qsize) {
//      this.maxQueueSize = qsize;
//   }
   
   public boolean enqueue(ReceivedRawMessage event) {
      return this.messageQueue.offer(event);
   }
   
   public ReceivedRawMessage dequeue() {
      try {
         return this.messageQueue.take();
      }
      catch (InterruptedException e) {
         e.printStackTrace();
         if (Thread.interrupted())  // Clears interrupted status!
            Thread.currentThread().interrupt();
      }
      return null;
   }
   
   public ReceivedRawMessage tryDequeue() {
      return this.messageQueue.poll();
   }

}
