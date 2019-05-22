package com.sip_server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceivedMessageQueue {

   BlockingQueue<ReceivedMessage> messageQueue;
   
   //long maxQueueSize;
   
   static final int DEF_QUEUE_SIZE = 100000;
   
   private static ReceivedMessageQueue instance = new ReceivedMessageQueue();
   
   private ReceivedMessageQueue() {
      this.messageQueue = new LinkedBlockingQueue<ReceivedMessage>(DEF_QUEUE_SIZE);
      this.messageQueue.size();
      //this.maxQueueSize = DEF_QUEUE_SIZE;
   }
   
   public static ReceivedMessageQueue getInstance() {
      return instance;
   }
   
//   public void setMaxQueueSize(long qsize) {
//      this.maxQueueSize = qsize;
//   }
   
   public boolean enqueue(ReceivedMessage event) {
      return this.messageQueue.offer(event);
   }
   
   public ReceivedMessage dequeue() {
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
   
   public ReceivedMessage tryDequeue() {
      return this.messageQueue.poll();
   }

}
