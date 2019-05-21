package com.base;

/**
 * Provides generic message "interface" for supported protocols.
 * Unfortunately with the current architecture it should implement 
 * the delegation pattern for possible protocols (because of using 
 * JAIN-SIP therefore SIPMessage of it).
 * 
 * @author demiry
 *
 */
public abstract class GenericMessage {

   int protocolType = -1;
   
   public GenericMessage(int protocol) {
      this.protocolType = protocol;
   }

   public int getProtocolType() {
      return this.protocolType;
   }
   
   public abstract boolean isRequest(); // {
   
   /**
    *  returns String representation of the value part of 
    *  the message header in occurrence 'occurrence' 
    */
   public abstract String getMessageHeaderValueInOccurrence(int occurrence, String hdr);
   
   /* multiple occurrences should be separated with "\r\n" */ 
   public abstract String getHeaderStr(String hdrName);
   
   public abstract String encode(); // {
   
   public abstract String getBody();
}

