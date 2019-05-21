package com.dparser;

import java.util.ArrayList;

import com.dparser.DiameterDefinitions;
import com.dparser.ProxyInfo;

/*
 * The abstract class to provide a base for message specific data blocks which
 * are to be used on message building (to build the message) and message parsing
 * (to contain the information from message). It mainly tries to consist of
 * variables for data that possibly common for all messages
 */
public class DiameterMessageData {
   protected String sessionIdentifier;
   protected String originHost;
   protected String originRealm;
   protected String destinationHost;
   protected String destinationRealm;

   // To be used in Auth-Session-State AVP
   // The default value should be STATE_MAINTEAINED according to RFC 3588,
   // which indicates session state is maintained, and the access device
   // MUST issue a session termination message (STR) when service to the
   // user is terminated. Currently Diameter support is purposed 3GPP Sh
   // interface that no session assumed in messaging.
   protected int authSessionState = DiameterDefinitions.NO_STATE_MAINTAINED;
   // TODO: It is to be moved into message specific data part.
   // Currently effectively used by Rx
   protected long applicationId = -1;

   // Origin-State-Id could be used in messages defined by Base Protocol
   // (such as STR/STA, RAR/RAA, ACR/ACA)
   protected long originStateId = -1;

   // Result-Code to be used for response/answer messages
   protected int resultType; // AVP_RESULT_CODE or AVP_EXPERIMENTAL_RESULT
   protected long resultCode = -1;

   // The following information could not be processed internally. But when they
   // received in a Diameter request, they should be included in related
   // Diameter answer
   // The Proxy-Info AVP is used by proxies that do not want to maintain local
   // state
   // information for a request, and instead wish to add the state information
   // to the
   // request. The state info AVP is guaranteed to be present in the answer
   // message,
   // so the proxy can retrieve the state information.
   // The Route-Record is added at each hop, and contains the path of the
   // message.
   // The server/peer can use this information to determine the reverse path of
   // messages,
   // and responses follow the reverse path.
   protected ArrayList<ProxyInfo> proxyInfoList = new ArrayList<ProxyInfo>();
   protected ArrayList<String> routeRecordList = new ArrayList<String>();

   // To be used to control whether message data has been synced for answering
   // It is not accessible by subclasses for setting and set internally by
   // performDataSync method. Assumed it is set for answer message data only
   private boolean dataSyncedForAnswer = false;

   public DiameterMessageData() {
   }

   public DiameterMessageData(int resultType, long resultCode) {
      this.resultType = resultType;
      this.resultCode = resultCode;
   };

   public void setSessionIdentifier(String sessionId) {
      sessionIdentifier = sessionId;
   }

   public String getSessionIdentifier() {
      return sessionIdentifier;
   }

   public boolean setSessionIdentifierIfNull(String sessionId) {
      if (sessionIdentifier == null) {
         sessionIdentifier = sessionId;
         return true;
      }

      return false;
   }

   public void setOriginHost(String origHost) {
      originHost = origHost;
   }

   public String getOriginHost() {
      return originHost;
   }

   public boolean setOriginHostIfNull(String origHost) {
      if (originHost == null) {
         originHost = origHost;
         return true;
      }

      return false;
   }

   public void setOriginRealm(String origRealm) {
      originRealm = origRealm;
   }

   public String getOriginRealm() {
      return originRealm;
   }

   public boolean setOriginRealmIfNull(String origRealm) {
      if (originRealm == null) {
         originRealm = origRealm;
         return true;
      }

      return false;
   }

   public void setDestinationHost(String destHost) {
      destinationHost = destHost;
   }

   public String getDestinationHost() {
      return destinationHost;
   }

   public boolean setDestinationHostIfNull(String destHost) {
      if (destinationHost == null) {
         destinationHost = destHost;
         return true;
      }

      return false;
   }

   public void setDestinationRealm(String destRealm) {
      destinationRealm = destRealm;
   }

   public String getDestinationRealm() {
      return destinationRealm;
   }

   public boolean setDestinationRealmIfNull(String destRealm) {
      if (destinationRealm == null) {
         destinationRealm = destRealm;
         return true;
      }

      return false;
   }

   public void setAuthSessionState(int authSessState) {
      authSessionState = authSessState;
   }

   public int getAuthSessionState() {
      return authSessionState;
   }

   public void setApplicationId(long appId) {
      applicationId = appId;
   }

   public boolean setApplicationIdIfNotSet(long appId) {
      if (applicationId != -1) {
         applicationId = appId;
         return true;
      }
      return false;
   }

   public long getApplicationId() {
      return applicationId;
   }

   public long getOriginStateId() {
      return originStateId;
   }

   public void setOriginStateId(long originStateId) {
      this.originStateId = originStateId;
   }

   public boolean setOriginStateIdIfNotSet(long originStateId) {
      if (this.originStateId == -1) {
         this.originStateId = originStateId;
         return true;
      }
      return false;
   }

   public void setResult(int resultType, long resultCode) {
      this.resultType = resultType;
      this.resultCode = resultCode;
   }

   // Assumes received AVP_RESULT_CODE not with AVP_EXPERIMENTAL_RESULT
   public void setResult(long resultCode) {
      this.resultType = DiameterDefinitions.AVP_RESULT_CODE;
      this.resultCode = resultCode;
   }

   public int getResultType() {
      return resultType;
   }

   public long getResultCode() {
      return resultCode;
   }

   public boolean setResultCodeIfNotSet(long resultCode) {
      if (this.resultCode == -1) {
         this.resultCode = resultCode;
         return true;
      }
      return false;
   }

   public void setProxyInfo(ProxyInfo pInfo) {
      this.proxyInfoList.add(pInfo);
   }

   public ArrayList<ProxyInfo> getProxyInfoList() {
      return this.proxyInfoList;
   }

   public void setRouteRecord(String rRecord) {
      this.routeRecordList.add(rRecord);
   }

   public ArrayList<String> getRouteRecordList() {
      return this.routeRecordList;
   }

   public void performDataSync(DiameterMessageData reqData) {
      this.sessionIdentifier = reqData.getSessionIdentifier();
      this.proxyInfoList = reqData.getProxyInfoList();
      this.routeRecordList = reqData.getRouteRecordList();
      // set that data is synced
      this.dataSyncedForAnswer = true;
   }

   public boolean isDataSyncedForAnswer() {
      return this.dataSyncedForAnswer;
   }

   public void copyTo(DiameterMessageData data) {
      this.sessionIdentifier = data.getSessionIdentifier();
      this.originHost = data.getOriginHost();
      this.originRealm = data.getOriginRealm();
      this.destinationHost = data.getDestinationHost();
      this.destinationRealm = data.getDestinationRealm();

      this.authSessionState = data.getAuthSessionState();
      this.applicationId = data.getApplicationId();
      this.originStateId = data.getOriginStateId();
      this.resultType = data.getResultType();
      this.resultCode = data.getResultCode();
      this.proxyInfoList = data.getProxyInfoList();
      this.routeRecordList = data.getRouteRecordList();

      this.dataSyncedForAnswer = data.isDataSyncedForAnswer();
   }

   // Assumed the subclass which part of information included by abstract class
   // is valid for that specific message. Therefore the implementation of this
   // method should care about them.
   // public abstract void printMessageData(StringBuilder buffer);

}
