package com.dparser;

import java.util.ArrayList;
import java.util.Iterator;

public class GenericGroupedAVP extends DiameterAVP 
{
   
  public final static int OPTIONAL = 0;
  public final static int MANDATORY = 1;
  // list of AVPs in group  
  protected ArrayList<DiameterAVP> groupList;

 
  public GenericGroupedAVP(long code, byte flags, long vendor, AVPDictionaryData dictData)
  {
    this.code      = code;
    this.flagsByte = flags;
    this.length    = DiameterUtilities.DIAMETER_AVP_HDR_SIZE(this.flagsByte);
    this.vendorId  = vendor;
    if(dictData == null){
       this.dictionaryData = DiameterCommonData.getAVPDictionaryData(code, vendor);
    }else{
       this.dictionaryData = dictData;
    }
    this.setDataType(AVP_DATATYPE_GROUPED);
    groupList = new ArrayList<DiameterAVP>();

  }

  @Override
  public int setData(ArrayList<DiameterAVP> avpList) throws DiameterException
  {
    this.groupList = avpList;

    
    //DiameterAVP avp;
    Iterator<DiameterAVP> iterator = avpList.iterator();
    while (iterator.hasNext())
    {
      DiameterAVP avp = iterator.next();
      this.dataLength += avp.getLength() + DiameterUtilities.calculatePadding(avp.length); 
    }    
    this.length += this.dataLength;
    return this.length;
  }

  public int setData(GenericGroupedAVP grpAvp) throws DiameterException
  {
     this.groupList = grpAvp.getAllAVPs();
    
    //DiameterAVP avp;
    Iterator<DiameterAVP> iterator = groupList.iterator();
    while (iterator.hasNext())
    {
      DiameterAVP avp = iterator.next();
      this.dataLength += avp.getLength() + DiameterUtilities.calculatePadding(avp.length); 
    }    
    this.length += this.dataLength;
    return this.length;
  }
  
  public int addAVP(DiameterAVP avp) throws DiameterException
  {
   
    this.groupList.add(avp);  
    long dLen = avp.getLength() + DiameterUtilities.calculatePadding(avp.length);

    this.length += dLen;
    this.dataLength += dLen;
    return this.length;
  }
  
  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#copyRawData(byte[], int)
   */
  @Override
  protected void copyRawData(byte[] data, int index) throws DiameterException
  {
    // Assumed that AVP header is already converted into raw format.
    // Since data type of this AVP is grouped, this should handle
    // converting raw format of all AVPs in group.
    DiameterAVP avp;
    Iterator<DiameterAVP> iterator = this.groupList.iterator();
    while (iterator.hasNext())
    {
      avp = iterator.next();
      index = avp.buildAvpIntoRawData(data, index);
    }
  }
  

  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#setDataFromRaw(byte[], int, int)
   */
  @Override
  public void setDataFromRaw(byte[] data, int start, int len) throws DiameterException
  {
    /* Data part should start from point that Vendor-ID AVP placed in raw format
    */
    this.dataLength = len;
    this.length    += this.dataLength;

    long avpCode   = 0;
    int  avpLength = 0;
    byte avpFlags  = 0;
    long vendorId1  = 0;
    int  avpDataLength = 0;
    int  index = start;
    int sequence   = 0;

    while (index < (start+len))
    {
       sequence++;
       // Could be check if remaining bytes are enough to for a possible AVP
      avpCode = DiameterUtilities.get4BytesAsUnsigned32(data, index);
      index += AVP_CODE_SIZE;//4;
      avpFlags = (byte)(data[index] & 0xFF);
      index++;
      avpLength = DiameterUtilities.get3Bytes(data, index);
      index += AVP_LENGTH_SIZE; //3;

      if (avpCode == 868) 
      {
         System.out.println("avlLengh=" + avpLength);
      }
      if ((avpFlags & AVPFLAGS_V_BIT_MASK) != 0 )
      {
        vendorId1 = DiameterUtilities.get4BytesAsUnsigned32(data, index);
        index += 4;
        avpDataLength = avpLength - AVP_HEADER_SIZE_WITH_VENDORID;
      }
      else
      {
        avpDataLength = avpLength - AVP_HEADER_SIZE_NO_VENDORID;
        vendorId1 = 0;
      }

      int padding1 = DiameterUtilities.calculatePadding(avpLength);
      AVPDictionaryData dictData = DiameterCommonData.getAVPDictionaryData(avpCode, vendorId1);
      
      DiameterAVP avp = null;
      if (dictData != null)
      {
        avp = dictData.getAvpMaker().createAVPHandler(avpCode, vendorId1, avpFlags, dictData);
        if (avpCode == 1266)
        {
           System.out.println("ENVELOPE is found");
        }
        
        avp.setDataFromRaw(data,index,avpDataLength);
        checkAndCollectInnerAVP(avp, sequence);
      }
      else
      {
        throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
              "GenericGroupedAVP:Unkown inner AVP encountered with code=" + avpCode 
              + " in AVP=" + this.code + " vendorId : " + vendorId1);
      }
      index += avpDataLength + padding1;
      this.groupList.add(avp);
      
    } // while
    // All inner AVPs are parsed. Invoke the handler for overall compliance
    //verifyOverallGroupedAVP();
  }
  

  /*
   * -------------------------------------------------------------------------
   * checkAndCollectInnerAVP : The pseudo abstract method that should be 
   *                           implemented by subclasses which have information 
   *                           about the structure of the AVP content. It is 
   *                           invoked for each inner-AVP during parse which 
   *                           could be used to collect grouped AVP specific 
   *                           data.
   * Parameters : avp - DiameterAVP object on processing
   *              sequence - the sequence number of AVP in message
   * May throw DiameterParseException on check-fail
   * IMPORTANT: If any non-compliance is detected during the check, it is not
   *            suggested for handler/implementor to throw an exception if 
   *            the AVP is not set as mandatory in the message (i.e. "M" bit
   *            is not set). Since it will cause interrupting the message 
   *            parsing while a problem with a non-mandatory AVP. However, this
   *            situation can be logged.
   * -------------------------------------------------------------------------
   */
  public void checkAndCollectInnerAVP(DiameterAVP avp, int sequence)
                                               throws DiameterException
  {
   

  }
  
  /*
   * -------------------------------------------------------------------------
   * verifyOverallGroupedAVP : The pseudo abstract method that should be 
   *                           implemented by subclasses which have information
   *                           about the structure of the AVP content. It is 
   *                           invoked when all inner avps are parsed, to check 
   *                           overall compliance of grouped-avp's definition
   * Parameters : None. 
   * May throw DiameterParseException on check-fail
   * IMPORTANT: If any non-compliance is detected during the check, it is not
   *            suggested for handler/implementor to throw an exception if 
   *            the AVP is not set as mandatory in the message (i.e. "M" bit
   *            is not set). Since it will cause interrupting the message 
   *            parsing while a problem with a non-mandatory AVP. However, this
   *            situation can be logged.
   * -------------------------------------------------------------------------
   */
  
// TODO
  /*
  public void verifyOverallGroupedAVP() throws DiameterException
  {
     boolean isExist;
     AVPDictionaryData dictData = null;
     Iterator<AVPDictionaryData> it = this.dictionaryData.defaultAvpList.keySet().iterator();
     //Bir grup AVP ye ait subAVP ler datalari eklenirken mandatory olup olmadigi bilinmektedir. Sirayla kontrolu yapilacaktir
     while(it.hasNext()){
        dictData = it.next();
        isExist = false;
        for(DiameterAVP avp : groupList){
           if((avp.dictionaryData.avpName).equals(dictData.avpName)){
              isExist = true;
              if(avp.getDataType() == AVP_DATATYPE_GROUPED){
                 
                 if(!(avp instanceof GenericGroupedAVP)){
                    avp = GenericGroupedAVP.convertToGenericGroup(avp);
                 }
                 ((GenericGroupedAVP) avp).verifyOverallGroupedAVP();
              }
              break;
           }
        }
        if(!isExist){
           if(this.dictionaryData.defaultAvpList.get(dictData)){
              throw new DiameterParseException(DIAMETER_MISSING_AVP,
                    "The AVP (name : " + dictData.getAvpName() + ") is mandatory for groupAVP( name : " 
                     + this.dictionaryData.avpName + ")" );
              
           }          
        }
     }
  }
  */
  public DiameterAVP getAVP(long avpcode){
     
     DiameterAVP resultAVP = null;
     for(DiameterAVP avp : groupList){
        if(avp.code == avpcode){
           resultAVP = avp;
           break;
        }
     }
     return resultAVP;
     
  }
  
  public ArrayList<DiameterAVP> getAllAVPs(){
     if(groupList.size() != 0)
        return this.groupList;
     return null;
  }
  
  /*
   * (non-Javadoc)
   * @see com.nortelnetworks.ims.cap.prtcl.diameter.Base.DiameterAVP#printData(java.lang.StringBuilder)
   */
  @Override
  public void printData(StringBuilder buffer)
  {
    // increase indentation for proper output. Each grouped AVP will increase
    // it and take back when it is completed
    String saveIndent = indentation;
    indentation = indentation + "      ";
    buffer.append("\n");
    DiameterAVP avp;
    Iterator<DiameterAVP> iterator = this.groupList.iterator();
    while (iterator.hasNext())
    {
      avp = iterator.next();
      avp.printContent(buffer, indentation);
    }
    // Take back the indentation
    indentation = saveIndent;
  }

   
}