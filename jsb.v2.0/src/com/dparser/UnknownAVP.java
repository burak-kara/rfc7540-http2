package com.dparser;

public class UnknownAVP extends DiameterAVP 
{
   public UnknownAVP(long code, byte flags, long vendor, AVPDictionaryData dictData)
   {
     this.code      = code;
     this.flagsByte = flags;
     this.length    = DiameterUtilities.DIAMETER_AVP_HDR_SIZE(this.flagsByte);
     this.vendorId  = vendor;
     /* Actually, we do not expect a non-null dictionary data */
     this.dictionaryData = dictData;
     /* Creation for a unknown AVP. OctetString is assumed as data format for any-avp */
     this.dataType  = AVP_DATATYPE_OCTETSTRING;
   }
   
   @Override
   public int setData(byte[] data)
   {
     this.rawData = data;
     this.dataLength = data.length;
     this.length += dataLength;
     
     return this.length;
   }
   
   @Override
   protected void copyRawData(byte[] data, int index) throws DiameterException 
   {
      System.arraycopy(this.rawData, 0, data, index, this.rawData.length);      
   }

   @Override
   public void setDataFromRaw(byte[] data, int start, int len) throws DiameterException 
   {
      this.rawData = new byte[len];
      System.arraycopy(data, start, this.rawData, 0, len);
      this.dataLength = len;
      this.length    += this.dataLength;
   }

   @Override
   public void printData(StringBuilder buffer) 
   {
      // We need special handling, since most probably data block includes
      // un-printable chars. Try print-out a hexadecimal bytes
      buffer.append(DiameterUtilities.toHexString(this.rawData,0,this.rawData.length) + "\n");
   }

}
