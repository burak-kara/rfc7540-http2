package com.dparser;

/* C-like structure to be used in the avpList below */
public class AVPOccurrence {
   public String  avpName;                 // "AVP" is reserved to indicate 'any-AVP' 
   public boolean anyAVP;                  // quick accessible info for the case [AVP] in message definition
   public int     occtype;                 // AVPOCC_xxx
   public boolean multiple;                // to indicate '*'
   public int     min;                     // default 0
   public int     max = Integer.MAX_VALUE; // shall be infinity   
}