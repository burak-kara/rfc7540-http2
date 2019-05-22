package com.dparser;

public class UnknownAVPMaker implements DiameterAVPMaker {
   private UnknownAVPMaker()
   {}

   public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData)
   {
     return new UnknownAVP(code, flags, vendor, dictData);
   }

   public static DiameterAVPMaker getInstance()
   {
     return Unknown_Maker;
   }

   private static final UnknownAVPMaker Unknown_Maker = new UnknownAVPMaker();
}
