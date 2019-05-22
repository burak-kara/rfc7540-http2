package com.dparser;

import java.util.ArrayList;
//import java.util.Map;

/*
 * AVPDictionaryData encapsulates AVP specific data for AVP-Dictionary mechanism
 * which is used during message/AVP parsing especially. The dictionary data is
 * stored in a map against the key which is based on AVP code and vendor-id
 * (refer to avpDictionary map at DiameterCommonData). The main part of data is
 * the maker class reference which is based on AVP's data type and used to
 * create appropriate AVP handler for processing. The others are generally for
 * debug purposed data.
 */
public class AVPDictionaryData
{
  // avpFlags is to store AVP flags from definition (e.g. from the related RFC).
  // No direct usage is assumed. Since, for parsing, flags are already received
  // in message, and, for building, it is assumed that the code which creates
  // the AVP, knows about the required flag settings. Therefore, avpFlags, here
  // could be used for sanity purposes; i.e. to compare flags settings from
  // flags received in the message for the related AVP.
  byte avpFlags;

  // avpName keeps the name of the AVP and used for debug purposes.
  String avpName;

  // avpMaker is the instance of the related AVP maker which is used to create
  // the related AVP handler.
  DiameterAVPMaker avpMaker;

  // useStringRepresentation is also used for debug purposes. Currently used
  // only by OctetStringAVP handlers to indicate the AVP data will be
  // represented as byte stream or string during content print-out.
  boolean useStringRepresentation;

  // Consists of AVPDictionaryData instances for AVPs building a grouped-AVP,
  // which is valid in the case of that the dictionary data is for a grouped-AVP
  // case.
  ArrayList<AVPDictionaryData> avpDictionaryList;

  public AVPDictionaryData(byte flags, String name, DiameterAVPMaker maker)
  {
    this.avpFlags = flags;
    this.avpName  = name;
    this.avpMaker = maker;
  }

  public AVPDictionaryData(byte flags, String name, DiameterAVPMaker maker, boolean useStringRep)
  {
    this.avpFlags = flags;
    this.avpName  = name;
    this.avpMaker = maker;
    this.useStringRepresentation = useStringRep;
  }

  public AVPDictionaryData(byte flags, String name, DiameterAVPMaker maker,
                           ArrayList<AVPDictionaryData> dictList)
  {
    this.avpFlags = flags;
    this.avpName  = name;
    this.avpMaker = maker;
    this.avpDictionaryList = dictList;
  }

  public byte getAvpFlags()
  {
    return this.avpFlags;
  }

  public String getAvpName()
  {
    return this.avpName;
  }

  public DiameterAVPMaker getAvpMaker()
  {
    return this.avpMaker;
  }

  public boolean getUseStringRepresentation()
  {
    return this.useStringRepresentation;
  }

}
