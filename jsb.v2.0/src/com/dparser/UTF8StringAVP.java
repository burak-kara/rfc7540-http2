package com.dparser;

import java.io.UnsupportedEncodingException;

import com.base.Logger;

public class UTF8StringAVP extends DiameterAVP
{
  String stringData;

  int rawDataStart;

  public UTF8StringAVP(long code, byte flags, long vendor, AVPDictionaryData dictData)
  {
    this.code       = code;
    this.flagsByte  = flags;
    this.length = DiameterUtilities.DIAMETER_AVP_HDR_SIZE(this.flagsByte);
    this.vendorId   = vendor;
    this.dictionaryData = dictData;
    this.dataType   = AVP_DATATYPE_UTF8STRING;
  }

  public int setData(String data)
  {
    this.stringData = data;
    this.dataLength = data.length();
    this.length    += this.dataLength;

    //return (length + DiameterUtilities.calculatePadding(length));
    return length;
  }

  protected void copyRawData(byte[] data, int index) throws DiameterException
  {
    try
    {
      byte[] buffer = this.stringData.getBytes("UTF-8");
      System.arraycopy(buffer, 0, data, index, buffer.length);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new DiameterException(
                   DiameterDefinitions.DIAMETER_UNABLE_TO_COMPLY,
                   "UTF8StringAVP::copyRawData: UnsupportedEncodingException", e);
    }
  }

  public void setDataFromRaw(byte[] data, int start, int len)
                                           throws DiameterException
  {
    this.dataLength = len;
    this.length    += this.dataLength;
    try
    {
      this.stringData = new String(data, start, len, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      throw new DiameterException(
                   DiameterDefinitions.DIAMETER_UNABLE_TO_COMPLY,
                   "UTF8StringAVP::setDataFromRaw: UnsupportedEncodingException", e);
    }

    this.rawDataStart = start;
  }

  public String getStringData()
  {
    return this.stringData;
  }

  public String getStringData(byte[] dataArray) {
      try {
         return new String(dataArray, this.rawDataStart, this.dataLength, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
         Logger.Error("UTF8StringAVP - getStringData - Thrown UnsupportedEncodingException:" + e);
         return null;
      }
   }

  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#printData(java.lang.StringBuilder)
   */
  @Override
  public void printData(StringBuilder buffer)
  {
    buffer.append(this.stringData + "\n");
  }

}
