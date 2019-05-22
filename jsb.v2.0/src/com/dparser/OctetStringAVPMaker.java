package com.dparser;

public class OctetStringAVPMaker implements DiameterAVPMaker
{
  private OctetStringAVPMaker()
  {}

  public DiameterAVP createAVPHandler(long code, long vendor, byte flags, AVPDictionaryData dictData)
  {
    return new OctetStringAVP(code, flags, vendor, dictData);
  }

  public static DiameterAVPMaker getInstance()
  {
    return OStr_Maker;
  }

  private static final OctetStringAVPMaker OStr_Maker = new OctetStringAVPMaker();

}
