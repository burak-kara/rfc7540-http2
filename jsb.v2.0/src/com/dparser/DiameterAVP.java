package com.dparser;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;

import com.dparser.DiameterDefinitions;
import com.dparser.DiameterException;
import com.dparser.DiameterUtilities;

/*
 * DiameterAVP abstracts functions for both Diameter AVP parser and
 * builder behaviours. Subclasses are obtained as based on data type
 * encapsulated in AVP; such as Unsigned32AVP, UTF8StringAVP. It is assumed
 * that subclasses own room to keep data (the abstract class consists
 * of rawData attribute to keep data in raw-format (currently used
 * only by OctetStringAVP effectively).
 */
public abstract class DiameterAVP implements DiameterDefinitions
{
  // AVP Header
  public long code;
  protected byte flagsByte;
  protected int  length;    // overall length of the avp
  protected long vendorId;

  protected int dataType;   // No effective usage at the moment
  protected int dataLength; // length of data part only
  protected int padding;    // padding byte count if necessary
  protected boolean mandatory;

  // rawData is purposed to keep data in unformatted form (as received in
  // message. Currently it is used by OctetStringAVP to keep the data for both
  // parse and build purposes. Since the other AVP handlers own data attributes
  // no usage is assumed for this time, for real-time purposes
  //
  protected byte[] rawData;

  public AVPDictionaryData dictionaryData;

  // Dynamically set during AVP content print-out.
  // It is requeired for proper indentation of grouped AVPs if a grouped AVP
  // consists a grouped AVP cases, especially
  protected String indentation;

  public long getAvpCode()
  {
    return this.code;
  }
  
  public long getVendorId()
  {
    return this.vendorId;
  }

  public byte getFlagsByte()
  {
    return this.flagsByte;
  }
  
  /* =========================================================================
   * Builder methods
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * setData Methods : Pseudo Abstract methods. The expected behaviour is to set
   *                   data into local data area the object (perform any further
   *                   encoding, if required), set dataLength, increase the avp
   *                   length with this data length and return the final length
   *                   of AVP. The implementor/extending class should overwrite
   *                   the appropriate one to perform specific behavior.
   *                   The handlers implementing grouped AVPs may have own
   *                   setData methods for extended input parameters.
   * Parameters : Data in appropriate format.
   * Returns : The final length of the AVP when the dataLength is added into.
   * -------------------------------------------------------------------------
   */
  public int setData(String data) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(String):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public int setData(long data) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(long):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public int setData(int data) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(int):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public int setData(byte[] data) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(byte[]):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public int setData(Object data) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(Object):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }


  public int setData(ArrayList<DiameterAVP> avpList) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_INVALID_AVP_VALUE,
          "DiameterAVP::setData(Vector<DiameterAVP>):Unexpected call for " +
                       "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  /*
   * -------------------------------------------------------------------------
   * copyRawData : Abstract method. The expected behaviour is to copy data
   *               content into raw data block after pre-decoding, if required
   *               (for example, for strings consist of Unicode chars, UTF8
   *               conversion is required).
   *               Grouped AVP handlers should build all AVP content into.
   * Parameters : buffer - raw data block that to copy the data into
   *              index - start index for copy
   * Returns : int - '0' for successful process. No effective usage assumed at
   *                the moment.
   *   Could throw DiameterException especially for problems on pre-decoding
   * -------------------------------------------------------------------------
   */
  protected abstract void copyRawData(byte[] buffer, int index) throws DiameterException;

  /*
   * -------------------------------------------------------------------------
   * buildAvpIntoRawData : A common implementation for building the content of
   *                       AVP into raw message block. It uses copyRawData.
   * Parameters : rawMessage - raw message block that to copy the data into
   *              index - start index for copy
   * Returns : the position of the raw message block after the AVP content
   *           is copied into.
   *   Could throw DiameterException which thrown by copyRawData
   * -------------------------------------------------------------------------
   */
  public int buildAvpIntoRawData(byte[] rawMessage, int index)
                                                      throws DiameterException
  {
    /*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                           AVP Code                            |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |V M P r r r r r|                  AVP Length                   |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                        Vendor-ID (opt)                        |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |    Data ...
      +-+-+-+-+-+-+-+-+

      AVP Flags
        V(endor-specific), M(andatory), P (need for end-to-end security)

      Data must be padded to align on a 32-bit boundary, while some AVP
      types align naturally.  A number of zero-valued bytes are added
      to the end of the AVP Data field till a word boundary is reached.
      The length of the padding is not reflected in the AVP Length field.

      The Data field of Grouped-AVPs is specified as a sequence of AVPs.
      Each of these AVPs follows - in the order in which they are
      specified - including their headers and padding.  The AVP Length
      field is set to 8 (12 if the 'V' bit is enabled) plus the total
      length of all included AVPs, including their headers and padding.
      Thus the AVP length field of an AVP of type Grouped is always a
      multiple of 4.
    */
    DiameterUtilities.set4Bytes(rawMessage, index, this.code);
    index += DiameterDefinitions.AVP_CODE_SIZE;
    rawMessage[index] = this.flagsByte;
    index += DiameterDefinitions.AVP_FLAGS_SIZE;
    DiameterUtilities.set3Bytes(rawMessage, index, this.length);
    index += DiameterDefinitions.AVP_LENGTH_SIZE;
    if ((this.flagsByte & AVPFLAGS_V_BIT_MASK) != 0)
    {
      DiameterUtilities.set4Bytes(rawMessage, index, this.vendorId);
      index += DiameterDefinitions.AVP_VENDOR_ID_SIZE;
    }

    this.copyRawData(rawMessage, index);

    index += this.dataLength + DiameterUtilities.calculatePadding(this.dataLength);

    return index;
  }

  /*
   * -------------------------------------------------------------------------
   * setAvpIntoRaw : A common implementation for building the content of
   *                 AVP into a raw data block. It uses buildAvpIntoRawData.
   *                 It is used on building example for missing AVPs in
   *                 received request messages.
   * Parameters : None.
   * Returns : Newly created byte block including encoded AVP content
   *   Could throw DiameterException which thrown by buildAvpIntoRawData
   * -------------------------------------------------------------------------
   */
  public byte[] setAvpIntoRaw() throws DiameterException
  {
    int index = 0;
    byte[] rawAvp = new byte[this.length];

    buildAvpIntoRawData(rawAvp, index);
    return rawAvp;
  }

  /* =========================================================================
   * Parser methods
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * setDataFromRaw : Abstract method. The expected behaviour is that, parse
   *                  AVP data from message block, perform decoding (if
   *                  required), save into data refence owned by implementor.
   *                  Grouped-AVP handlers should extract all inner-AVPs and
   *                  data related with them.
   * Parameters : data - Received data as byte stream
   *              start - start index for the AVP data
   *              len - the length of the AVP data
   * Returns : int - '0' for successful process. No effective usage assumed at
   *           the moment.
   *   Could throw DiameterException especially for problems on decoding
   * -------------------------------------------------------------------------
   */
  public abstract void setDataFromRaw(byte[] data, int start, int len)
                                                     throws DiameterException;

  /*
   * -------------------------------------------------------------------------
   * getData Methods : Pseudo abstract methods. The expected behaviour is to
   *                   provide formatted data to application (to reference at
   *                   application specific message-data object). The handler
   *                   should implement appropriate one, according the data
   *                   type of the AVP.
   *                   getRawData is implemented here, since DiameterAVP owns
   *                   data refernce for data in raw format. This is used
   *                   directly by OctetStringAVP implementation.
   * Parameters : None.
   * Returns : Reference to the data
   * -------------------------------------------------------------------------
   */
  public String getStringData() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getStringData:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public String getStringData(byte[] dataArray) throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getStringData:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public long getUint32Data() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getUint32Data:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public int getInt32Data() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getInt32Data:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public long getUint64Data() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getUint64Data:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }

  public long getInt64Data() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getInt64Data:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }
    
  public Object getObjectData() throws DiameterException
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "DiameterAVP::getObjectData:Unexpected call for " +
                "AVP=" + code + " (" + this.dictionaryData.getAvpName() + ")");
  }
  
  public Object getAVPData(DiameterAVP avp){
   return avp;
     
  }

  public byte[] getRawData()
  {
    return this.rawData;
  }

  public int getLength()
  {
    return length;
  }

  public int getDataLength()
  {
    return dataLength;
  }

  /* =========================================================================
   * Utility methods
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * printData : Abstract method. The expected behaviour is that, put AVP data
   *             into StringBuilder in human-readable format for monitoring
   *             purposes.
   * Parameters : buffer - StringBuilder to put data
   * Returns : None
   * -------------------------------------------------------------------------
   */
  public abstract void printData(StringBuilder buffer);

  /*
   * -------------------------------------------------------------------------
   * printContent : Common implementation to print-out AVP content in human-
   *                readable format. Uses printData for data part. It is
   *                used for debug/monitoring purposes.
   * Parameters : buffer - StringBuilder to put data
   *              indent - String for indentation (probably white-space) to
   *              increase readability (especially for Grouped-AVPs which
   *              have inner-AVPs).
   * Returns : None
   * -------------------------------------------------------------------------
   */
  public void printContent(StringBuilder buffer, String indent)
  {
    if (dictionaryData == null)
    {
      dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendorId);
    }
    indentation = indent;
    
    if (dictionaryData == null) {
       //System.err.println("dictionaryData is null for code = " + code + " vendorid = " + vendorId);
       buffer.append(indent + "Unregisted-AVP" + " AVP (" + code);
    }
    else {
       buffer.append(indent + dictionaryData.getAvpName() + " AVP (" + code);
    }
    if ((flagsByte & AVPFLAGS_V_BIT_MASK) != 0) buffer.append(", V");
    if ((flagsByte & AVPFLAGS_M_BIT_MASK) != 0) buffer.append(", M");
    if ((flagsByte & AVPFLAGS_P_BIT_MASK) != 0) buffer.append(", P");
    if ((flagsByte & AVPFLAGS_V_BIT_MASK) != 0) buffer.append(", Vendor-Id = " + vendorId);
    buffer.append(")\n");

    buffer.append(indentation + "   " + "Data Length = " + dataLength + "\n");
    buffer.append(indentation + "   " + "Data = ");

    printData(buffer);
  }

   public int getDataType() {
      return dataType;
   }
   
   public void setDataType(int dataType) {
      this.dataType = dataType;
   }
  
}
