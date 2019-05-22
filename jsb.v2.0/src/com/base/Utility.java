package com.base;

import java.util.concurrent.atomic.AtomicInteger;

public class Utility {
   /* Max buffer size for UDP received and sender sockets */
   public static int MAX_BUFFER_SIZE = 64 * 1024;
  
   public static AtomicInteger rcvrCounter = new AtomicInteger(0);
}
