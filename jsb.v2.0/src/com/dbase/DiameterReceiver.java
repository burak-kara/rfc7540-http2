package com.dbase;

import java.net.Socket;

import com.base.Logger;
import com.base.Receiver;
import com.base.StackCore;

public class DiameterReceiver extends Receiver {

   public DiameterReceiver(Socket s, StackCore core) {
      super(s, core);
   }

   @Override
   public void run() {
      //byte[] buff = new byte[buffer_size];
      int startOff = 0;
         
      //this.inStream  = sock.getInputStream();
      while (! this.stopWorking.get()) {
         if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
            Logger.Debug("first--Diameter-Receiver starting read with startOff = " + startOff );
         }
         this.stopWorking.set(core.receiveMessage(sock));
      }
   }
            
}
