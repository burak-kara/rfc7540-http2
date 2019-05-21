package com.dparser;

public class Unsigned32AVPMaker implements DiameterAVPMaker
{
  private Unsigned32AVPMaker()
  {}

  public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData)
  {
    return new Unsigned32AVP(code, flags, vendor, dictData);
  }

  public static DiameterAVPMaker getInstance()
  {
    return U32_Maker;
  }

  private static final Unsigned32AVPMaker U32_Maker = new Unsigned32AVPMaker();

}