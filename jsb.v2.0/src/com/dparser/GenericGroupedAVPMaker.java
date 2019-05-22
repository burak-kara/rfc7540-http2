package com.dparser;

public class GenericGroupedAVPMaker implements DiameterAVPMaker {
   private GenericGroupedAVPMaker()
   {}

   public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData)
   {
     return new GenericGroupedAVP(code, flags, vendor, dictData);
   }

   public static DiameterAVPMaker getInstance()
   {
     return Gg_Maker;
   }

   private static final GenericGroupedAVPMaker Gg_Maker = new GenericGroupedAVPMaker();
}
