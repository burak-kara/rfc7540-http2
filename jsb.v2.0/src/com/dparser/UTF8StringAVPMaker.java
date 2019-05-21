package com.dparser;

public class UTF8StringAVPMaker implements DiameterAVPMaker
{
  private UTF8StringAVPMaker()
  {}

  public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData)
  {
    return new UTF8StringAVP(code, flags, vendor, dictData);
  }

  public static DiameterAVPMaker getInstance()
  {
    return UTF8_Maker;
  }

  private static final UTF8StringAVPMaker UTF8_Maker = new UTF8StringAVPMaker();
}
