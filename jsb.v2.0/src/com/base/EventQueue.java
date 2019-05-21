package com.base;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {

//   BlockingQueue<Event> eventQueue;
//   
//   //long maxQueueSize;
//   
//   static final int DEF_QUEUE_SIZE = 100000;
//   
//   private static EventQueue instance = new EventQueue();
//   
//   private EventQueue() {
//      this.eventQueue = new LinkedBlockingQueue<Event>(DEF_QUEUE_SIZE);
//      this.eventQueue.size();
//      //this.maxQueueSize = DEF_QUEUE_SIZE;
//   }
//   
//   public static EventQueue getInstance() {
//      return instance;
//   }
//   
////   public void setMaxQueueSize(long qsize) {
////      this.maxQueueSize = qsize;
////   }
//   
//   public boolean enqueue(Event event) {
//      return this.eventQueue.offer(event);
//   }
//   
//   public Event dequeue() {
//      try {
//         return this.eventQueue.take();
//      }
//      catch (InterruptedException e) {
//         e.printStackTrace();
//         if (Thread.interrupted())  // Clears interrupted status!
//            Thread.currentThread().interrupt();
//      }
//      return null;
//   }
//   
//   public Event tryDequeue() {
//      return this.eventQueue.poll();
//   }
      
}
