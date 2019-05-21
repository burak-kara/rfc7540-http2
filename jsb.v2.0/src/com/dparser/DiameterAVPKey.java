package com.dparser;

/*
 * DiameterAVPKey is used to create unique keys for AVP Dictionary which is
 * based on AVP code and vendor-id values from AVP definition.
 */
public class DiameterAVPKey {
   private long avpCode;
   private long vendorId;

   public DiameterAVPKey(long code, long vendor) {
      this.avpCode = code;
      this.vendorId = vendor;
   }

   public boolean equals(Object obj) {
      if (obj == null)
         return false;

      if (this == obj)
         return true;

      try {
         DiameterAVPKey key = (DiameterAVPKey) obj;
         if ((avpCode == key.avpCode) && vendorId == key.getVendorId())
            return true;
      }
      catch (ClassCastException cce) {
         return false;
      }

      return false;

   }

   public int hashCode() {
      return (int) (avpCode + vendorId);
   }

   public long getAvpCode() {
      return avpCode;
   }

   public long getVendorId() {
      return vendorId;
   }
}
