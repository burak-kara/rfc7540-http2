package com.dparser;

import java.util.HashMap;

import com.dparser.DiameterAVPKey;

public class GroupedAVPGrammar extends AVPGrammar {   

//   public ArrayList<AVPOccurrence> avpList;
   public HashMap<DiameterAVPKey, AVPOccurrence> avpList;
   
   public GroupedAVPGrammar() {
//      avpList = new ArrayList<AVPOccurrence>();
      avpList = new HashMap<DiameterAVPKey, AVPOccurrence>();
   }
   
   public GroupedAVPGrammar(AVPGrammar avp) {
      this.name = avp.name;
      this.code = avp.code;
      this.type = avp.type;
      this.flagsByte = avp.flagsByte;
      this.vendorId = avp.vendorId; 

//      avpList = new ArrayList<AVPOccurrence>();
      avpList = new HashMap<DiameterAVPKey, AVPOccurrence>();
   }
}
