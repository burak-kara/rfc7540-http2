package com.dparser;

/*
 * Interface for Message Makers which are mainly formed as based on command
 * codes and "request-flag" data. Message Makers are created to bind into
 * Message Dictionary to be invoked to create appropriate Message Handler,
 * especially during message parsing (message building can use diferent logic).
 */

public interface DiameterMessageMaker
{
  public abstract DiameterMessage createMessageHandler(DiameterHeader header,
                                                       boolean parser,
                                                       DiameterMessageData data);


  public DiameterMessage createMessageHandler(byte flags, int commandcode,
                                              long applicationid, 
                                              MessageDictionaryData data);
}
