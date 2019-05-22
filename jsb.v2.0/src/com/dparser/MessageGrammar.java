package com.dparser;

import java.util.HashMap;

//import com.netas.tool.taf.protocol.diameter.agent.DiameterAVPKey;

public class MessageGrammar {
   public static final int MSGTYPE_UNKNOWN  = 0;
   public static final int MSGTYPE_DIAMETER = 1;
     
   public static final int AVPOCC_OPTIONAL  = 0;
   public static final int AVPOCC_MANDATORY = 1;
   public static final int AVPOCC_FIXED     = 2;
   
   /*
     command-def      = command-name "::=" diameter-message
     command-name     = diameter-name
     diameter-name    = ALPHA *(ALPHA / DIGIT / "-")
    */
   public String shortName;
   public String messageName; // RFC defines own messages as <CER> instead of Capabilities-Exchange-Request

   /* Message header part */
   /* header = "<" Diameter-Header:" command-id [r-bit] [p-bit] [e-bit] [application-id]">"
    */
   public int type;
   public int commandCode;
   public boolean REQ;
   public boolean PXY;
   public boolean ERR;
   public int applicationId;
   public boolean isContainsAny;
   
   public boolean force; // TODO: make it tag in XML file
   
  // public ArrayList<AVPOccurrence> avpList;
   public HashMap<DiameterAVPKey, AVPOccurrence> avpList;
   
   public MessageGrammar() {
    //  avpList = new ArrayList<AVPOccurrence>();
	   avpList = new HashMap<DiameterAVPKey, AVPOccurrence>();
   }
}
