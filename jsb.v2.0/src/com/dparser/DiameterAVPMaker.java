package com.dparser;

/*
 * Interface class for AVP Makers which are mainly formed as based on AVP's data
 * type; such as Unsigned32AVPMaker. AVP Makers are created to bind into AVP
 * Dictionary to be invoked to create appropriate AVP Handler upon data type,
 * especially during message parsing (message building can use diferent logic).
 * Grouped AVP types may use specific makers for this purpose.
 */

public interface DiameterAVPMaker
{
  public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData);
  
}
