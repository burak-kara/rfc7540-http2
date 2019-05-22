package com.dparser;

import com.dparser.DiameterDefinitions;

/*
Diameter Header format
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|    Version    |                 Message Length                |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| command flags |                  Command-Code                 |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Application-ID                        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Hop-by-Hop Identifier                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      End-to-End Identifier                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|  AVPs ...
+-+-+-+-+-+-+-+-+-+-+-+-+-

Command Flags
0 1 2 3 4 5 6 7
+-+-+-+-+-+-+-+-+  R(equest), P(roxiable), E(rror)
|R P E T r r r r|  T(Potentially re-transmitted message), r(eserved)
+-+-+-+-+-+-+-+-+
* 
*/

/*
* Encapsulates Diameter Header information. It is used for both parse and build
* processing
*/
public class DiameterHeader implements DiameterDefinitions {

   byte version;
   byte flagsByte;
   int commandCode;
   int length;
   long applicationId;
   long hopByHopId;
   long endToEndId;
   boolean isRequest = false;

   public DiameterHeader(byte version, byte flags, int commandCode, long applicationId, long hbhId, long eteId) {
      this.version = version;
      this.length = DIAMETER_MSG_HDR_SIZE;
      this.flagsByte = flags;
      this.commandCode = commandCode;
      this.applicationId = applicationId;
      this.hopByHopId = hbhId;
      this.endToEndId = eteId;
      this.isRequest = isRequest();
   }

   public DiameterHeader(byte version, byte flags, int commandCode, long applicationId, long hbhId, long eteId,
                         int length) {
      this.version = version;
      this.length = length;
      this.flagsByte = flags;
      this.commandCode = commandCode;
      this.applicationId = applicationId;
      this.hopByHopId = hbhId;
      this.endToEndId = eteId;
      this.isRequest = isRequest();
   }

   public void addLength(int length) {
      this.length += length;
   }

   public int getLength() {
      return length;
   }

   public byte getVersion() {
      return version;
   }

   public byte getFlagsByte() {
      return flagsByte;
   }

   public void setFlagsByte(byte flags) {
      flagsByte = flags;
   }

   public int getCommandCode() {
      return commandCode;
   }

   public long getApplicationId() {
      return applicationId;
   }

   public long getHopByHopId() {
      return hopByHopId;
   }

   public void setHopByHopId(long hbh) {
      this.hopByHopId = hbh;
   }

   public long getEndToEndId() {
      return endToEndId;
   }
   
   public void setEndToEndId(long e2e) {
      this.endToEndId = e2e;
   }

   public boolean isRequest() {
      if ((flagsByte & HFLAGS_R_BIT_MASK) != 0) {
         this.isRequest = true;
      }
      return this.isRequest;
   }

   public void printContent(StringBuilder buffer) {
      buffer.append(" (Diameter Header: " + commandCode + ", ");
      if ((flagsByte & HFLAGS_R_BIT_MASK) != 0)
         buffer.append("REQ, ");
      if ((flagsByte & HFLAGS_P_BIT_MASK) != 0)
         buffer.append("PXY, ");
      if ((flagsByte & HFLAGS_E_BIT_MASK) != 0)
         buffer.append("ERR, ");
      if ((flagsByte & HFLAGS_T_BIT_MASK) != 0)
         buffer.append("RET, ");
      buffer.append(applicationId + ")\n");
      buffer.append("   Hop-by-Hop Identifier = " + hopByHopId + "\n");
      buffer.append("   End-to-End Identifier = " + endToEndId + "\n");
   }

}
