package com.dparser;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * For building, runs through the AVP list
 * For parsing, it uses MessageGrammer and AVPDictionary
 * 
 * @author demiry
 *
 */
public class GenericDiameterMessage extends DiameterMessage {
   MessageDictionaryData dictionaryData;

   public GenericDiameterMessage(DiameterHeader header, MessageDictionaryData dictdata) {
      super(header);
      this.dictionaryData = dictdata;
   }

   public GenericDiameterMessage(byte flags, int commandcode,
                                 long applicationid, MessageDictionaryData data)
   {
      /* Seems that we have to create a DiameterHeader instance anyway.*/
//      DiameterHeader header = new DiameterHeader(flags, commandcode, applicationid, 
//                                                 -1 /* end-to-end-id; TODO; to be decided */);
      this(new DiameterHeader(DIAMETER_VERSION, flags, commandcode, applicationid, 
                              0, (long)-1 /* end-to-end-id; TODO; to be decided */),
           data);
      //this.dictionaryData = data;
   }

   @Override
   public byte[] buildMessageToRaw() throws DiameterBuildException {
      /* Assumes AVPList consists of all AVPs for the message building */
      try {
         this.buildRawData();
      } 
      catch (DiameterException e) {
         throw new DiameterBuildException("GenericDiameterMessage::buildMessageToRaw: ", e);
      }
      return this.rawMessage;
   }

   @Override
   public byte[] buildErrorAnswerToRaw(DiameterParseException errInfo)
                                                     throws DiameterBuildException {
      // If this method is invoked this means that we are processing a request 
      // and there is parsing problem with it
      
//      // Session-Identifier is available if could be parsed from request
//      //
//      DiameterMessage rcvMsg = errInfo.getReceivedMessage();
//      rcvMsg.header.isErrorBitSet();
//      
//      // TODO, get Session-ID from AVP list
////      DiameterMessageData rcvMsgData = null;
////      if (rcvMsg != null) {
////         rcvMsgData = rcvMsg.getMessageData();
////         if (rcvMsgData != null) {
////            try {
////               addUTF8Avp(AVP_SESSION_ID, AVP_Mflag, 0, rcvMsgData.getSessionIdentifier());
////            }
////            catch (DiameterException exp) {
////               throw new DiameterBuildException(exp.getErrorReason(),
////                                                "GenericDiameterMessage::buildErrorAnswerToRaw: " + exp.getMessage(),
////                                                rcvMsgData,
////                                                exp);
////            }
////         }
////      }
//      
//
//      try {
//         // Origin-Host AVP
//         addUTF8Avp(AVP_ORIGIN_HOST, AVP_Mflag, 0, DiameterCommonData.getOriginHost());
//
//         // Origin-Realm AVP
//         addUTF8Avp(AVP_ORIGIN_REALM, AVP_Mflag, 0, DiameterCommonData.getOriginRealm());
//
//         // Result-Code AVP
//         addUnsigned32Avp(AVP_RESULT_CODE, AVP_Mflag, 0, errInfo.getErrorReason());
//
//         // Failed-AVP AVPs
//         ArrayList<byte[]> failedAvpList = errInfo.getFailedAvpList();
//         Iterator<byte[]> iter = failedAvpList.iterator();
//         while (iter.hasNext()) {
//            addOctetStringAvp(AVP_FAILED_AVP, AVP_Mflag, 0, iter.next());
//         }
//
//         buildRawData();
//      }
//      catch (DiameterException exp) {
//         throw new DiameterBuildException(exp.getErrorReason(),
//                                          "GenericDiameterMessage::buildErrorAnswerToRaw: " + exp.getMessage(),
//                                          exp);
//      }
//      return rawMessage;

      return null;
   }

   @Override
   public long checkAndCollectAVPData(long avpCode, DiameterAVP avp, int sequence) {
      /* TODO: By using the grammar definition check:
       * 1. If a fixed AVP is positioned correctly
       * 2. If there is a multiple occurrence of a singular AVP
       */
//      if (avp.dictionaryData == null) {
//         if (!dictionaryData.isAnyAVP()) {
//            return DIAMETER_AVP_UNSUPPORTED;
//         }
//      }
//      else {
//         AVPOccurrence avpOccurrence = dictionaryData.getAVPOccurrenceList().get(sequence);
//         if (avpOccurrence.occtype == MessageGrammar.AVPOCC_FIXED) {
//            DiameterAVPKey key = AVPDictionary.getAvpIdByName(avpOccurrence.avpName);
//            //TODO think again for key=null case
//            if (key == null) {
//               return DIAMETER_AVP_UNSUPPORTED;
//            }
//            if (avp.code == key.getAvpCode() && avp.getVendorId() == key.getVendorId()) {
//               return DIAMETER_SUCCESS;
//            }
//            return DIAMETER_AVP_UNSUPPORTED;
//         }
//
//         ArrayList<DiameterAVP> listOfAVP = avpList.getInList(avp.code, avp.getVendorId());
//
//         if (listOfAVP.size() == 1) {
//            for (int i = 0; i < dictionaryData.getAVPOccurrenceList().size(); i++) {
//               AVPOccurrence avpOcc = dictionaryData.getAVPOccurrenceList().get(i);
//               DiameterAVPKey key = AVPDictionary.getAvpIdByName(avpOcc.avpName);
//               if (key.getAvpCode() == avp.code && key.getVendorId() == avp.getVendorId()) {
//                  if (avpOcc.multiple) {
//                     return DIAMETER_SUCCESS;
//                  }
//                  else {
//                     return DIAMETER_AVP_UNSUPPORTED;
//                  }
//               }
//            }
//         }
//      }
      
      return DIAMETER_SUCCESS;
   }

   @Override
   public void verifyOverallCompliancy() throws DiameterParseException {
      /* TODO: Verify all the message by checking whether mandatory AVPs are included in the message */
//      StringBuilder sb = null;
//      for (int i = 0; i < dictionaryData.getAVPOccurrenceList().size(); i++) {
//         AVPOccurrence avpOccurrence = dictionaryData.getAVPOccurrenceList().get(i);
//         if (avpOccurrence.occtype == MessageGrammar.AVPOCC_MANDATORY && avpList.get(avpOccurrence.avpName) == null) {
//            if (sb == null) {
//               sb = new StringBuilder();
//               sb.append("For message=" + this.getCommandName() + " Mandatory AVPs' name that missing according to dictionary:");
//            }
//            sb.append("\nAVP Name: " + avpOccurrence.avpName);
//         }
//      }
//
//      if (sb != null) {
//         throw new DiameterParseException(DIAMETER_MISSING_AVP, sb.toString());
//      }
   }

   @Override
   protected String getCommandName() {
      /* TODO: Check if the message is defined in the message dictionary, 
       * if it is return the name from this definition 
       */
      if (this.dictionaryData != null) {
         return dictionaryData.getName();
      }
      return "Unknown-Command-Name";
   }
   

}
