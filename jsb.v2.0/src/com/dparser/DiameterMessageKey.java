package com.dparser;

/**
 * 
 * @author demiry
 *
 *         DiameterMessageKey based on Command-Code and Application-ID, so same
 *         Command-Code could be used by different applications in different
 *         ways. "isRequest" is to distinguish request and answer.
 */
public class DiameterMessageKey {
   private int commandCode;
   private long applicationId;
   private boolean isRequest;

   public DiameterMessageKey(int code, long appId, boolean request) {
      this.commandCode = code;
      this.applicationId = appId;
      this.isRequest = request;
   }

   public boolean equals(Object obj) {
      if (obj == null)
         return false;

      if (this == obj)
         return true;

      try {
         DiameterMessageKey key = (DiameterMessageKey) obj;
         if (this.commandCode == key.getCommandCode() && this.applicationId == key.getApplicationId()
               && this.isRequest == key.getIsRequest()) {
            return true;
         }
      }
      catch (ClassCastException e) {
         return false;
      }
      return false;
   }

   public int hashCode() {
      // With the logic below, different commandCode and applicationIds can
      // produce same hash-code. It is assumed the used map-type supports
      // multiple key occurrences and that "equals" method can distinguish
      // those
      return (int) (this.commandCode + this.applicationId + ((this.isRequest) ? 555 : 0));
   }

   public int getCommandCode() {
      return this.commandCode;
   }

   public long getApplicationId() {
      return this.applicationId;
   }

   public boolean getIsRequest() {
      return this.isRequest;
   }
}
