package com.dparser;

public class MessageDictionaryData {
   /*
    * reference to an instance of DiameterHeader which keeps Command-Code and
    * Application-Id info.
    */
   int commandCode;

   long applicationId;

   String messageName;
   String shortName;

   public MessageDictionaryData(int cmndCode, long appId, String name) {
      this.commandCode = cmndCode;
      this.applicationId = appId;
      this.messageName = name;
   }

   String getName() {
      return this.messageName;
   }

   public int getCommandCode() {
      return commandCode;
   }

   public long getApplicationId() {
      return applicationId;
   }

}