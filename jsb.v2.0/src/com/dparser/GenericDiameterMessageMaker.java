package com.dparser;

public class GenericDiameterMessageMaker implements DiameterMessageMaker {
   // Implemented as a singleton
   private static final GenericDiameterMessageMaker Generic_Maker = new GenericDiameterMessageMaker();

   // Private constructor
   private GenericDiameterMessageMaker() {
   }

   /**
    * Creates an GenericDiameter message.
    * 
    * @param header
    *           The Diameter header
    * @param parser
    *           True if the message is to be used for parsing
    * @param data
    *           not in use
    */
   public DiameterMessage createMessageHandler(DiameterHeader header, MessageDictionaryData dictdata) {
      return new GenericDiameterMessage(header, dictdata);
   }

   @Override
   public DiameterMessage createMessageHandler(byte flags, int commandcode,
                                               long applicationid, MessageDictionaryData data) {
      
      return new GenericDiameterMessage(flags, commandcode, applicationid, data);
   }
   
   /**
    * Gets the instance of this maker.
    * 
    * @return the instance
    */
   public static DiameterMessageMaker getInstance() {
      return Generic_Maker;
   }

   @Override
   public DiameterMessage createMessageHandler(DiameterHeader header, boolean parser, DiameterMessageData data) {
      return new GenericDiameterMessage(header.flagsByte, header.commandCode, header.applicationId, null);
   }
}

