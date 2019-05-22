package com.dparser;

import com.base.Logger;

public class OctetStringAVP extends DiameterAVP
{
  public OctetStringAVP(long code, byte flags, long vendor, AVPDictionaryData dictData)
  {
    this.code       = code;
    this.flagsByte  = flags;
    this.length = DiameterUtilities.DIAMETER_AVP_HDR_SIZE(this.flagsByte);
    this.vendorId   = vendor;
    this.dictionaryData = dictData;
    this.dataType   = AVP_DATATYPE_OCTETSTRING;
  }

  public int setData(String data)
  {
    // Assume that the data comes in String object
    try
    {
      this.rawData = data.getBytes("ISO-8859-1");
      this.dataLength = this.rawData.length;
      this.length    += this.dataLength;

      return this.length;
    }
    catch (Exception e)
    {    
      Logger.Error("Error occurred during conversion.-->" + e);
      return 0; 
    }
  }
  
  public int setData(byte[] data)
  {
    this.rawData = data;
    this.dataLength = data.length;
    this.length += dataLength;
    
    return this.length;
  }

  public int setData(Object data)
  {
    try
    {
      this.rawData = ((String)data).getBytes("ISO-8859-1");
      this.dataLength = this.rawData.length;
      this.length += this.dataLength;
      return this.length;
    }
    catch (Exception e)
    {        
      Logger.Error("Error occurred during conversion.-->" + e);
      return 0;        
    }
  }

  protected void copyRawData(byte[] data, int index)
  {
    System.arraycopy(this.rawData, 0, data, index, this.rawData.length);
  }

  public void setDataFromRaw(byte[] data, int start, int len)
  {
    this.rawData = new byte[len];
    System.arraycopy(data, start, this.rawData, 0, len);
    this.dataLength = len;
    this.length    += this.dataLength;
  }

  public String getStringData() throws DiameterException
  {
    try
    {
      return new String(this.rawData, "ISO-8859-1");
    }
    catch (Exception e)
    {
      throw new DiameterException(
                   DiameterDefinitions.DIAMETER_INVALID_AVP_VALUE,
                   "OctetStringAVP::getStringData: UnsupportedEncodingException",e);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#printData(java.lang.StringBuilder)
   */
  @Override
  public void printData(StringBuilder buffer)
  {
    if (this.dictionaryData == null)
    {
      this.dictionaryData = DiameterCommonData.getAVPDictionaryData(this.code, this.vendorId);
    }
    if (this.dictionaryData.getUseStringRepresentation())
    {
      try
      {
        buffer.append(new String(this.rawData,"ISO-8859-1") + "\n");
      }
      catch (Exception e)
      {
        buffer.append("UnsupportedEncoding\n");
      }
    }
    else
    {
      buffer.append(DiameterUtilities.toHexString(this.rawData,0,this.rawData.length) + "\n");
    }
  }
}
