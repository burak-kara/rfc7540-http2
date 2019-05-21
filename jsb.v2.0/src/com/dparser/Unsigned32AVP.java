package com.dparser;

public class Unsigned32AVP extends DiameterAVP 
{
  long uint32Data;

  public Unsigned32AVP(long code, byte flags, long vendor, AVPDictionaryData dictData)
  {
    this.code      = code;
    this.flagsByte = flags;
    this.length = DiameterUtilities.DIAMETER_AVP_HDR_SIZE(this.flagsByte);
    this.vendorId  = vendor;
    this.dictionaryData = dictData;
    this.dataType  = AVP_DATATYPE_UNSIGNEDINT32;
  }

  public int setData(long data)
  {
    // NOTE: "data" which have value bigger than 0xffffffff will be truncated
    //       during copyRawData
    //uint32Data = data;
    this.uint32Data = (data & 4294967295L);
    this.dataLength = 4;
    this.length    += this.dataLength;

    return length;
  }

  protected void copyRawData(byte[] data, int index)
  {
    DiameterUtilities.set4Bytes(data, index, this.uint32Data);
  }

  public void setDataFromRaw(byte[] data, int start, int len) throws DiameterException
  {
    if (len != 4)
      throw new DiameterException(DiameterDefinitions.DIAMETER_INVALID_AVP_VALUE,
                                  "Unsigned32AVP::setDataFromRaw: UnsupportedEncodingException");
    this.dataLength = len;
    this.length    += this.dataLength;
    this.uint32Data = DiameterUtilities.get4BytesAsUnsigned32(data, start);
  }


  public long getUint32Data()
  {
    return this.uint32Data;
  }

  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#printData(java.lang.StringBuilder)
   */
  @Override
  public void printData(StringBuilder buffer)
  {
    buffer.append(this.uint32Data + "\n");
  }
}
