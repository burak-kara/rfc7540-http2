package com.dparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//import com.dparser.MessageDefinitionParser;
//import com.dparser.MessageGrammar;
import com.dparser.DiameterDefinitions;
//import com.netas.tool.taf.utility.Utility;

/* This class is mainly assumed to encapsulate static information which
 * is common for all messages. Applications can create subclasses to keep
 * application specific static data for messages.
 *
 * Further, avpDictionary and messageDictionary is created here and owned
 * by this class to provide automatic initialisation of dictionaries. Since
 * it is assumed that, the application should invoke this class to set
 * working variables (such as originHost) at first step.
 */
public class DiameterCommonData
{
  protected static String originHost = null;
  protected static String originRealm = null;
  
  public static void setOriginHost(String origHost)
  {
    originHost = origHost;
  }

  public static String getOriginHost()
  {
    return originHost;
  }

  public static void setOriginRealm(String origRealm)
  {
    originRealm = origRealm;
  }

  public static String getOriginRealm()
  {
    return originRealm;
  }

  /*
   * avpDictionary and messageDictionary are maps to keep required info
   * for AVP and message handler creation, respectively. They mainly stores
   * maker class references to create appropriate AVP/message handlers.
   * The dictionary mechanism here is assumed as a "must" for received message
   * parsing. For message building to be sent, the application can invoke
   * the related maker directly for real-time purposes.
   */
  private static Map<DiameterAVPKey, AVPDictionaryData> avpDictionary;
  private static Map<DiameterMessageKey, DiameterMessageMaker> messageDictionary;

  /*
   * avpDictionary stores AVPDictionaryData which consists of the maker class
   * instance and some additional information. Dictionary data is indexed
   * with the key built on avp-code and vendor-id
   */
  public static AVPDictionaryData getAVPDictionaryData(long avpCode, long vendorId)
  {
    DiameterAVPKey key = new DiameterAVPKey(avpCode, vendorId);
    AVPDictionaryData data = avpDictionary.get(key);

    return data;
  }

  /*
   * The facility method to get AVPDictionary data by the name of an AVP as 
   * defined in RFCs. This method is assumed for debug/logging purposes
   */
  public static AVPDictionaryData getAVPDictionaryDataByName(String rfcName)
  {
    Collection<AVPDictionaryData> col = avpDictionary.values();
    Iterator<AVPDictionaryData> iterator = col.iterator();
    AVPDictionaryData data = null;
    while (iterator.hasNext())
    {
      data = iterator.next();
      if (data.avpName.equalsIgnoreCase(rfcName))
      {
        return data;
      }
    }
    return data;
  }
  
  /*
   * The facility method to get AVP-Code info by the name of an AVP as 
   * defined in RFCs. This method is assumed for debug/logging purposes
   */
  public static long getAVPCodeByName(String rfcName)
  {
    Set<DiameterAVPKey> s = avpDictionary.keySet();
    Iterator<DiameterAVPKey> it = s.iterator();
    while (it.hasNext())
    {
      DiameterAVPKey key = it.next();
      AVPDictionaryData data = avpDictionary.get(key);
      if (data != null)
      {
        if (data.avpName.equalsIgnoreCase(rfcName))
        {
          return key.getAvpCode();
        }
      }
    }
    return 0;
  }
  
  /*
   * The application can store own AVPs by using this method
   */
  public static boolean addAvpIntoDictionary(DiameterAVPKey key, AVPDictionaryData data)
  {
    return (avpDictionary.put(key, data) == null);
  }

  /*
   * getDiameterMessageHandler is used to create appropriate message handler.
   * messageDictionary is indexed by command-code and (R)equest flag. Actually
   * the passed parameters here are to pass into the created handler.
   */
  public static DiameterMessage getDiameterMessageHandler(DiameterHeader header, boolean parser,
                                                          DiameterMessageData data)
  {
    DiameterMessage msgHandler = null;
    DiameterMessageKey key = new DiameterMessageKey(header.commandCode, header.applicationId, 
                                                    ((header.flagsByte & DiameterDefinitions.HFLAGS_R_BIT_MASK) != 0));
    DiameterMessageMaker maker = messageDictionary.get(key);

    if (maker == null)
    {
       key = new DiameterMessageKey(0xFFFFFFFF, 0xFFFFFFFF, 
                                    ((header.flagsByte & DiameterDefinitions.HFLAGS_R_BIT_MASK) != 0));
       maker = messageDictionary.get(key);
    }
    
    if (maker != null)
    {
      msgHandler = maker.createMessageHandler(header, parser, data);
    }
   
    return msgHandler;
  }

  /*
   * The application can store own message makers by using this method
   */
  public static boolean addIntoMessageDictionary(DiameterMessageKey key, DiameterMessageMaker maker)
  {
    return (messageDictionary.put(key, maker) == null);
  }

  // Automatic initialisation block which runs at first invocation
  static
  {
    avpDictionary     = new HashMap<DiameterAVPKey, AVPDictionaryData>();
    messageDictionary = new HashMap<DiameterMessageKey, DiameterMessageMaker>();
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_SESSION_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Session-Id",
                                            UTF8StringAVPMaker.getInstance()));
//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_VENDOR_SPECIFIC_APPLICATION_ID, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Vendor-Specific-Application-Id",
//                                            VendorSpecificApplicationIdAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_VENDOR_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Vendor-Id",
                                            Unsigned32AVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_AUTH_APPLICATION_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Auth-Application-Id",
                                            Unsigned32AVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCT_APPLICATION_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Acct-Application-Id",
                                            Unsigned32AVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_AUTH_SESSION_STATE, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Auth-Session-State",
                                            Unsigned32AVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ORIGIN_HOST, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Origin-Host",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ORIGIN_REALM, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Origin-Realm",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_DESTINATION_HOST, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Destination-Host",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_DESTINATION_REALM, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Destination-Realm",
                                            UTF8StringAVPMaker.getInstance()));
/*    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_INFO, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Proxy-Info",
                                            ProxyInfoAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_HOST, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Proxy-Host",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_STATE, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Proxy-State",
                                            OctetStringAVPMaker.getInstance(), true));
*/
    // Proxy-Info is a Grouped-AVP
    AVPDictionaryData phDictData = new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, 
                                                         "Proxy-Host",
                                                         UTF8StringAVPMaker.getInstance());
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_HOST, 0), phDictData);
    AVPDictionaryData psDictData = new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, 
                                                         "Proxy-State",
                                                         OctetStringAVPMaker.getInstance(), 
                                                         true);
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_STATE, 0), psDictData);
    ArrayList<AVPDictionaryData> piList = new ArrayList<AVPDictionaryData>(2);
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_PROXY_INFO, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Proxy-Info",
                                            GenericGroupedAVPMaker.getInstance(), piList));
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ROUTE_RECORD, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Route-Record",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_RESULT_CODE, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Result-Code",
                                            Unsigned32AVPMaker.getInstance()));
//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_EXPERIMENTAL_RESULT, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Experimental-Result",
//                                            ExperimentalResultAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_EXPERIMENTAL_RESULT_CODE, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Experimental-Result-Code",
                                            Unsigned32AVPMaker.getInstance()));
    // TODO: Failed-AVP is handled as and AVP with Octet-String data type. Handling as a Grouped-AVP
    //       could be helpful to trace error cases (with no need for manual-parsing)
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_FAILED_AVP, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Failed-AVP",
                                            OctetStringAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ERROR_REPORTING_HOST, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_NOflag, "Error-Reporting-Host",
                                            UTF8StringAVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_SUBSCRIPTION_ID, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Subscription-Id",
//                                            SubscriptionIdAVPMaker.getInstance()));
//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_SUBSCRIPTION_ID_TYPE, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Subscription-Id-Type",
//                                            EnumeratedAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_SUBSCRIPTION_ID_DATA, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Subscription-Id-Data",
                                            UTF8StringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_FRAMED_IP_ADDRESS, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Framed-IP-Address",
                                            OctetStringAVPMaker.getInstance()));
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_FRAMED_IPv6_PREFIX, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Framed-IPv6-Prefix",
                                            OctetStringAVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_TERMINATION_CAUSE, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Termination-Cause",
//                                            EnumeratedAVPMaker.getInstance()));
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ORIGIN_STATE_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Origin-State-Id",
                                            Unsigned32AVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCOUNTING_RECORD_TYPE, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Accounting-Record-Type",
//                                            EnumeratedAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCOUNTING_RECORD_NUMBER, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Accounting-Record-Number",
                                            Unsigned32AVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_EVENT_TIMESTAMP, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Event-Timestamp", 
//                                            TimeAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_CLASS, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Class",
                                            OctetStringAVPMaker.getInstance()));
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_USER_NAME, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "User-Name",
                                            UTF8StringAVPMaker.getInstance()));
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCT_SESSION_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Acct-Session-Id",
                                            OctetStringAVPMaker.getInstance()));
    
//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCOUNTING_SUB_SESSION_ID, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Accounting-Sub-Session-Id",
//                                            Unsigned64AVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCT_MULTI_SESSION_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Acct-Multi-Session-Id",
                                            UTF8StringAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCT_INTERIM_INTERVAL, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Acct-Interim-Interval",
                                            Unsigned32AVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_ACCOUNTING_REALTIME_REQUIRED, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Accounting-Realtime-Required",
//                                            EnumeratedAVPMaker.getInstance()));
    
    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_SERVICE_CONTEXT_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Service-Context-Id",
                                            UTF8StringAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_FILTER_ID, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Filter-Id",
                                            UTF8StringAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_REDIRECT_HOST, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Redirect-Host",
                                            UTF8StringAVPMaker.getInstance()));

//    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_REDIRECT_HOST_USAGE, 0),
//                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Redirect-Host-Usage",
//                                            EnumeratedAVPMaker.getInstance()));

    avpDictionary.put(new DiameterAVPKey(DiameterDefinitions.AVP_REDIRECT_MAX_CACHE_TIME, 0),
                      new AVPDictionaryData(DiameterDefinitions.AVP_Mflag, "Redirect-Max-Cache-Time",
                                            Unsigned32AVPMaker.getInstance()));


    // We have only DiameterExperimentalMessageMaker for base which is used
    // for errored answers to received requests that no message handler is defined
    //
//    messageDictionary.put(new DiameterMessageKey(DiameterDefinitions.DIAMETER_EXPERIMENTAL_COMMANDCODE_1,
//                                                 DiameterDefinitions.DIAMETER_APPID_COMMON_MESSAGES, false), 
//                          DiameterExperimentalMessageMaker.getInstance());
    /* Requests */
    DiameterCommonData.addIntoMessageDictionary(new DiameterMessageKey(0xFFFFFFFF, 0xFFFFFFFF, true), 
                                                GenericDiameterMessageMaker.getInstance());

    /* Responses */
    DiameterCommonData.addIntoMessageDictionary(new DiameterMessageKey(0xFFFFFFFF, 0xFFFFFFFF, false), 
                                                GenericDiameterMessageMaker.getInstance());
  }
  
  public static void printAVPDictionary(StringBuilder strb) {
     strb.append("AVP Dictionary Dump");
     strb.append("\n--------------------------------------");
     Collection<AVPDictionaryData> coll = avpDictionary.values();
     Iterator<AVPDictionaryData> citer = coll.iterator();
     while (citer.hasNext()) 
     {
        AVPDictionaryData data = citer.next();
        strb.append("\nName : ").append(data.avpName);
     }
     strb.append("\n--------------------------------------");
  }
}
