package com.dparser;

//import java.io.UnsupportedEncodingException;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.Iterator;

import com.base.Logger;
//import com.netas.tool.taf.base.GenericMessage;
//import com.dparser.AVPOccurrence;
//import com.dparser.MessageGrammar;
import com.dparser.DiameterDefinitions;
import com.dparser.DiameterException;
import com.dparser.DiameterParseException;
import com.dparser.DiameterUtilities;
//import com.netas.tool.taf.utility.Utility;

/**
 * DiameterMessage is an abstract class which implements both Diameter Message
 * parser and builder behaviors. It encapsulates abstract functions to be 
 * implemented by application specific subclasses. 
 * 
 * @author demiry
 */
public abstract class DiameterMessage implements DiameterDefinitions
{
  /* Diameter messages encapsulate two main components: message header and
   * a list of AVPs (Attribute Value Pairs). An AVP consists of header
   * and data part which is used to encapsulate protocol-specific data.
   */
  protected DiameterHeader header;
  protected ArrayList<DiameterAVP> avpList;
  //protected AVPList avpList;
  //protected ArrayList<Integer> mandatoryAvpList;

  /* Diameter Message data is used to encapsulate structured information
   * included or to be included in Diameter message. The application using
   * Diameter protocol may only know about message data. For builder mode,
   * the application fills message data for the information to be carried on
   * Diameter message. Similarly, for parser mode, the application can access
   * the received information through message data
   */
//  protected DiameterMessageData messageData;

  /* Formatted Diameter message; just received for parsing or has been built
   * for transmit. Currently there is no direct need for parser mode
   */
  protected byte[] rawMessage;

//  /* Indicates that the instance is created for Rf or X2
//   */
//  private int acctAppSeperator;

//  /*
//   * Constructor
//   */
//  public DiameterMessage(DiameterHeader header)
//  {
//     this(header, APP_OTHER);
//  }

  /*
   * Constructor
   */
//  public DiameterMessage(DiameterHeader header, int seperator)
  public DiameterMessage(DiameterHeader header)
  {
    this.avpList = new ArrayList<DiameterAVP>();
    // this.avpList = new AVPList();
    //this.mandatoryAvpList = new ArrayList<Integer>();
    this.header = header;
    this.rawMessage = null;
//    this.acctAppSeperator = seperator;
  }
  
  /*
   * Default constructor
   */
  public DiameterMessage()
  {
    this.avpList = new ArrayList<DiameterAVP>();
    //this.avpList = new AVPList();
//    this.acctAppSeperator = APP_OTHER;
  }
  
//  public int addMandatoryAvp(int avpCode){ 
//     mandatoryAvpList.add(avpCode);
//     return 0; 
//  }
//  
//  public boolean isMandatory(int avpCode){
//     if(mandatoryAvpList.contains(avpCode)){
//        return true;
//     }
//     return false;
//  }

  /* =========================================================================
   * Parser methods
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * ParseRawDataForMessage : While the DiameterMessage class acting as
   *                          recevier; it parses the raw data and creates the
   *                          appropriate message handler to perform further,
   *                          application specific message processing
   * Parameters : Received data as byte stream
   * Returns : The reference to the message handler class instance. It throws
   *           an exception in the case of parse error.
   * -------------------------------------------------------------------------
   */
  public static DiameterMessage parseRawDataForMessage(byte[] rawMsg)
                                                  throws DiameterParseException
  {
    DiameterHeader diameterHeader = parseHeader(rawMsg);
    
    int commandCode = diameterHeader.getCommandCode();

    // Create message handler and continue parsing with AVP part
    //DiameterMessage message = DiameterCommonData.getDiameterMessageHandler(diameterHeader);
    DiameterMessage message = DiameterCommonData.getDiameterMessageHandler(diameterHeader,
                                                                           true, null);

    if (message == null)
    {
      // Probably an unsupported/unimplemented command code. With the current
      // architecture, we need a Message Handler to be able to send the errored
      // response back. DiameterExperimentalMessage, which uses Diameter
      // Experimental Command Code (0xFFFFFE), will be used for this purpose
      // (Refer to RFC 3588, Section 11.2.1).
      throw new DiameterParseException(DIAMETER_COMMAND_UNSUPPORTED,
                                       diameterHeader,
                                       "Unsupported Command Code = " +
                                       commandCode);
    }

    /*
     * We are in the successful path
     */
    // Set rawMessage just in case.
    message.rawMessage = rawMsg;
    int index = DIAMETER_MSG_HDR_SIZE;

    /*
     * Parse for AVPs
     *
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           AVP Code                            |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |V M P r r r r r|                  AVP Length                   |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                        Vendor-ID (opt)                        |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |    Data ...
     * +-+-+-+-+-+-+-+-+
     *
     * AVP Flags
     *   V(endor-specific), M(andatory), P (need for end-to-end security)
     */

    // Before going further check if there is enough data for an AVP
    if ((index + AVP_HEADER_SIZE_WITH_VENDORID) > (rawMsg.length - index))
    {
      byte[] failedAvpData = new byte[(rawMsg.length - index)];
      System.arraycopy(rawMsg,index,failedAvpData,0, (rawMsg.length - index));
      throw new DiameterParseException(DIAMETER_INVALID_AVP_LENGTH,
                                       failedAvpData, message,
                                       "No enough AVP data remaining");
    }

    long avpCode = 0;
    int  avpLength = 0;
    byte avpFlags = 0;
    long vendorId = 0;
    int  avpDataLength = 0;

    int sequence = 0;
    int avpStartIndex;

    while (index < rawMsg.length)
    {
      // AVP's start index is to be used for failure cases, to obtain failedAVP
      avpStartIndex = index;

      // TODO: Check if remaining bytes are enough to for a possible AVP

      avpCode = DiameterUtilities.get4BytesAsUnsigned32(rawMsg, index);
      index += AVP_CODE_SIZE;
      avpFlags = (byte)(rawMsg[index] & 0xFF);
      if ((avpFlags & AVPFLAGS_RESERVED_BIT_MASK) != 0)
      {
        // DIAMETER_INVALID_AVP_BITS (protocol error)
        // We do not need add a Failed-AVP AVP
        throw new DiameterParseException(DIAMETER_INVALID_AVP_BITS,
                                         message,
                                         "Invalid AVP bits for Command Code = "
                                         + commandCode + " and AVP = " +
                                         avpCode + " with " + avpFlags);
      }

      index += AVP_FLAGS_SIZE;
      avpLength = DiameterUtilities.get3Bytes(rawMsg, index);
      index += AVP_LENGTH_SIZE;

      if ((avpFlags & AVPFLAGS_V_BIT_MASK) != 0)
      {
        vendorId = DiameterUtilities.get4BytesAsUnsigned32(rawMsg, index);
        index += AVP_VENDOR_ID_SIZE;
        avpDataLength = avpLength - AVP_HEADER_SIZE_WITH_VENDORID;
      }
      else
      {
        avpDataLength = avpLength - AVP_HEADER_SIZE_NO_VENDORID;
        // reset vendorId that could consists of the value from previous AVP
        vendorId = 0;
      }

      // TODO: Check for avpLength=0?
      if ((avpDataLength > avpLength) || ((rawMsg.length - index) < avpDataLength))
      {
        // DIAMETER_INVALID_AVP_LENGTH (permanent failure)
        // Assume all data from index to rawMsg.length is related to this avp
        int len = ((rawMsg.length - index) < avpDataLength) ?
                   (rawMsg.length - index) : avpLength;
        byte[] failedAvpData = new byte[len];
        System.arraycopy(rawMsg,avpStartIndex,failedAvpData,0,len);
        throw new DiameterParseException(DIAMETER_INVALID_AVP_LENGTH,
                                         failedAvpData, message,
                                         "No enough AVP data remaining");
      }

      int padding = DiameterUtilities.calculatePadding(avpLength);

      AVPDictionaryData dictData = DiameterCommonData.getAVPDictionaryData(avpCode, vendorId);

      DiameterAVP avp = null;
      if (dictData != null)
      {
         avp = dictData.getAvpMaker().createAVPHandler(avpCode, vendorId, avpFlags, dictData);
      }
      else
      {
         avp = UnknownAVPMaker.getInstance().createAVPHandler(avpCode, vendorId, avpFlags, dictData);
         Logger.Error("An unimplemented AVP encountered at message " +
                       commandCode + " with AVP Code = " + avpCode);
      }
      
      try
      {
        avp.setDataFromRaw(rawMsg, index, avpDataLength);
      }
      catch (DiameterException e)
      {
        if (e.getExceptionType() == DiameterException.DIAMETER_PARSE_EXCEPTION)
        {
          throw (DiameterParseException)e;
        }
        else
        {
          byte[] failedAvpData = new byte[avpLength];
          System.arraycopy(rawMsg, avpStartIndex, failedAvpData, 0, avpLength);
          throw new DiameterParseException(e.getErrorReason(), failedAvpData,
                                           message, e.getMessage());
        }
/*
        Vector<byte[]> failedAvpList;
        if ((e.getExceptionType() == DiameterException.DIAMETER_PARSE_EXCEPTION) &&
            (((DiameterParseException)e).getFailedAvpList().size() > 0))
        {
          failedAvpList = ((DiameterParseException)e).getFailedAvpList();
        }
        else
        {
          failedAvpList = new Vector<byte[]>();
          byte[] failedAvpData = new byte[avpLength];
          System.arraycopy(rawMsg, avpStartIndex, failedAvpData, 0, avpLength);
          failedAvpList.add(failedAvpData);
        }
        throw new DiameterParseException(e.getErrorReason(), failedAvpList,
                                         message, e.getMessage());
*/
      }
      long result = DIAMETER_SUCCESS;
      result = message.checkAndCollectAVPData(avpCode, avp, sequence);

      if (result != DIAMETER_SUCCESS)
      {
        byte[] failedAvpData = new byte[avpLength];
        System.arraycopy(rawMsg, avpStartIndex, failedAvpData, 0, avpLength);
        throw new DiameterParseException(result, failedAvpData, message,
                            "checkAndCollectAVPData is failed for message=" +
                             commandCode + " and AVP=" + avpCode +"result returned is:"+result);
      }

      index += avpDataLength + padding;

      // At this point, we may have an unsupported AVP that discarded
      if (avp != null)
      {
        // avp.length information does not contain paddings
        message.avpList.add(avp);
      }
      sequence++;
    } // while

    // perform overall message compliancy
    //
    try
    {
      message.verifyOverallCompliancy();
    }
    catch (DiameterParseException exp)
    {
      throw exp;
    }

    return message;
  }
  
  public static DiameterHeader parseHeader(byte[] rawMsg) throws DiameterParseException 
  {
  // TODO: Do we need to check null data and length < header_size?
    /*
       Parse for header
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
    */
    byte version;
    int  length;
    byte flagsByte;
    int  commandCode;
    long applicationId;
    long hopByHopId;
    long endToEndId;
    int index = 0;

    version = (byte)(rawMsg[index] & 0xFF);
    index += VERSION_SIZE;
    length = DiameterUtilities.get3Bytes(rawMsg, index);
    index += MESSAGE_LENGTH_SIZE;
    flagsByte = (byte)(rawMsg[index] & 0xFF);
    index += FLAGS_SIZE;
    commandCode = DiameterUtilities.get3Bytes(rawMsg, index);
    index += COMMAND_CODE_SIZE;
    applicationId = DiameterUtilities.get4BytesAsUnsigned32(rawMsg, index);
    index += APPLICATION_ID_SIZE;
    hopByHopId = DiameterUtilities.get4BytesAsUnsigned32(rawMsg, index);
    index += HOP_BY_HOP_ID_SIZE;
    endToEndId = DiameterUtilities.get4BytesAsUnsigned32(rawMsg, index);

    /* ===================================================================== */
    /* Error checks for header                                               */
    /* 1. Version control (DIAMETER_UNSUPPORTED_VERSION)                     */
    /* 2. Message length consistency (DIAMETER_INVALID_MESSAGE_LENGTH)       */
    /* 3. "R" and "E" bit consistency (DIAMETER_INVALID_HDR_BITS)            */
    /* 4. Non zero reserved bits (DIAMETER_INVALID_HDR_BITS)                 */
    /*                                                                       */
    /* We have tried to parse till here to be able to obtain minimum data    */
    /* which is required for a possible errored response back, such as       */
    /* Command Code. Hopefully, Command Code (and others) encoded in right   */
    /* place even if there is a "unsupported version" error.                 */
    /*                                                                       */
    /* Although Session-Identifier AVP is required in an errored-answer, it  */
    /* is optional, and it is assumed that it is not possible to obtain info */
    /* for it, if there is an error in header.                               */
    /* Notes:                                                                */
    /* (1) If multiple errors are encountered, the first one is to be        */
    /*     included in error response (refer to RFC 3588; Section 7).        */
    /* (2) Error responses are produced only for errored requests but not    */
    /*     errored responses. But the following logic will not follow this   */
    /*     rule. The decision will be let to upper layer.                    */
    /* ===================================================================== */

    // Create header before, which could be needed on error case also
    //
    DiameterHeader diameterHeader = new DiameterHeader(version, flagsByte,
                                                       commandCode,
                                                       applicationId,
                                                       hopByHopId, endToEndId);
    if (version != DIAMETER_VERSION)
    {
      String errMessage = "Unsupported version : " + version;
      errMessage.indexOf(".");
      throw new DiameterParseException
                             (DIAMETER_UNSUPPORTED_VERSION,
                              diameterHeader, errMessage);
    }

    if (length > rawMsg.length)
    {
      String errMessage = "Message length inconsistency : " +
                          "Parsed Length = " + length +
                          "Raw Data Length = " + rawMsg.length;
      throw new DiameterParseException(DIAMETER_INVALID_MESSAGE_LENGTH,
                                       diameterHeader, errMessage);
    }

    if ((flagsByte & HFLAGS_RESERVED_BITS_MASK) != 0)
    {
      String errMessage = "No zero reserved bits in header : " + flagsByte;
      throw new DiameterParseException(DIAMETER_INVALID_HDR_BITS,
                                       diameterHeader, errMessage);
    }

    if (((flagsByte & HFLAGS_R_BIT_MASK) != 0) &&
        ((flagsByte & HFLAGS_E_BIT_MASK) != 0))
    {
      String errMessage = "'E' bit is set for a Request Message";
      throw new DiameterParseException(DIAMETER_INVALID_HDR_BITS,
                                       diameterHeader, errMessage);
    }

    return diameterHeader;
  }

  /*
   * -------------------------------------------------------------------------
   * checkAndCollectAVPData : The abstract method that should be implemented by
   *                          subclasses. It is invoked during AVP parsing,
   *                          for each AVP for ability verifying the received
   *                          AVP and collecting data for application.
   * Parameters : avpCode - Identifier for AVP
   *              avp - DiameterAVP object on processing
   *              sequence - the sequence number of AVP in message
   * Returns : Diameter results:
   *           DIAMETER_SUCCESS (successful path) or
   *           DIAMETER_INVALID_AVP_BIT_COMBO,
   *           DIAMETER_AVP_UNSUPPORTED,
   *           DIAMETER_AVP_OCCURS_TOO_MANY_TIMES
   *           DIAMETER_MISSING_AVP (for fixed position AVP error)
   * -------------------------------------------------------------------------
   */
  public abstract long checkAndCollectAVPData(long avpCode, DiameterAVP avp, int sequence);

  /*
   * -------------------------------------------------------------------------
   * verifyOverallCompliancy : The abstract method that should be implemented
   *                           by subclasses. It is invoked after all raw
   *                           message is parsed and all data is collected.
   *                           It is to provide verification of the received
   *                           message in overall view.
   * Parameters : None.
   *              Uses messageData object and AVPs
   * Returns : None.
   *           It throws an exception in the case of lack of compliancy.
   * -------------------------------------------------------------------------
   */
  public abstract void verifyOverallCompliancy() throws DiameterParseException;


  /* =========================================================================
   * Builder methods
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * buildMessageToRaw : The abstract method that should be implemented by
   *                     subclasses. It is invoked from applications to build
   *                     the Diameter message for transmit. It needs message
   *                     information provided in messageData. It provides
   *                     a Diameter Message formated output that ready for
   *                     transmit.
   * Parameters : None.
   *              Uses messageData object and AVPs
   * Returns : Raw formated message block, ready to transmit.
   *        It throws DiameterBuildException in the case of lack of compliancy.
   * -------------------------------------------------------------------------
   */
  public abstract byte[] buildMessageToRaw() throws DiameterBuildException;


  /*
   * -------------------------------------------------------------------------
   * buildErrorAnswerToRaw : The abstract method that should be implemented by
   *                         subclasses to handle error response back in the
   *                         case of a parse error on request messages. It is
   *                         invoked by handleParseException. Currently,
   *                         protocol error cases are implemented/handled by
   *                         DiameterExperimentalMessage subclass.
   * Parameters : DiameterParseException errInfo - The exception consists of
   *              error reason and received request information that parse
   *              error has bee occurred.
   * Returns : Raw formated error response message block, ready to transmit.
   *    It can throw DiameterBuildException in the case of problem on build
   * -------------------------------------------------------------------------
   */
  public abstract byte[] buildErrorAnswerToRaw(DiameterParseException errInfo) throws DiameterBuildException;


  /*
   * -------------------------------------------------------------------------
   * handleParseException : Handles the first step behavior upon a Parse
   *                        Exception. It should be invoked for parse errors
   *   for received request messages. The main purpose is to build an
   *   appropriate raw message block for immediate response back (without
   *   reporting to application). It mainly uses the received message/
   *   request to build response. For protocol errors, it provide creation of
   *   an experimental message handler to send response as "E" bit is set.
   *   Otherwise, it invokes buildErrorAnswerToRaw of response handler.
   *
   * Parameters : DiameterParseException exp
   * Returns : Raw formated error response message block, ready to transmit.
   *   It can throw DiameterBuildException in the case of problem on build
   * -------------------------------------------------------------------------
   */
  public static byte[] handleParseException(DiameterParseException exp) throws DiameterBuildException
  {
    DiameterHeader rcvHeader = exp.getReceivedHeader();

    // Check if it is invoked for an answer message
    //
    if ((rcvHeader.getFlagsByte() & HFLAGS_R_BIT_MASK) == 0)
    {
       throw new DiameterBuildException(DIAMETER_UNABLE_TO_COMPLY,
                                        "Error-Response is requested for an Answer message",
                                        exp);
    }

    // Clear 'R' bit to have and header for response back
    //rcvHeader.setFlagsByte((byte) (rcvHeader.getFlagsByte() & 0x7F));
    rcvHeader.flagsByte &= 0x7F;

    int savedCommandCode = -1;
    if (DiameterUtilities.isProtocolError(exp.getErrorReason()))
    {
      // Tricky way to create DaimeterExperimentalMessage which produces an
      // errored answer
      savedCommandCode = rcvHeader.getCommandCode();
      //rcvHeader.setCommandCode(DIAMETER_EXPERIMENTAL_COMMANDCODE_1);
      rcvHeader.commandCode = DIAMETER_EXPERIMENTAL_COMMANDCODE_1;
    }
    //DiameterMessage errMessage = DiameterCommonData.getDiameterMessageHandler(rcvHeader);
    DiameterMessage errMessage = DiameterCommonData.getDiameterMessageHandler(rcvHeader, false, null);
    if (errMessage == null)
    {
       // It should not be null. At least a handler for Experimental Comman Code
       // should be created. It is a software error.
       throw new DiameterBuildException(DIAMETER_UNABLE_TO_COMPLY,
                                        "Could not create handler for Command Code=" +
                                        rcvHeader.getCommandCode(), exp);
    }

    if (savedCommandCode != -1)
    {
      //rcvHeader.setCommandCode(savedCommandCode);
      rcvHeader.commandCode = savedCommandCode;
    }

    return errMessage.buildErrorAnswerToRaw(exp);
  }

  /*
   * -------------------------------------------------------------------------
   * addUTF8Avp : Creates an UTF8 AVP handler and sets the provided data into
   *              that avp, and adds into avpList.
   *              Additionally, it updates length info of Diameter message
   *              header which will be used to calculate the length of raw
   *              message block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - String data to be included in AVP.
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in the case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
  public int addUTF8Avp(long avpCode, byte avpFlags, long vendorId,
                           String data)               throws DiameterException
  {
    UTF8StringAVP utf8Avp = (UTF8StringAVP)UTF8StringAVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (utf8Avp == null)
    {
      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
          "Could not create handler for UTF8Avp with code " + avpCode);
    }
    int len = utf8Avp.setData(data);
    //header.setLength(header.getLength() + len + DiameterUtilities.calculatePadding(len));
    header.length += len + DiameterUtilities.calculatePadding(len);
    avpList.add(utf8Avp);

    return 0;
  }

  /*
   * -------------------------------------------------------------------------
   * addUnsigned32Avp : Creates an Unsigned32AVP handler and sets the provided
   *                    data into that avp, and adds into avpList. Additionally,
   *                    it updates length info of Diameter message header which
   *                    will be used to calculate the length of raw message
   *                    block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - unsigned integer data to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in tne case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
  public int addUnsigned32Avp(long avpCode, byte avpFlags, long vendorId,
                                 long data)           throws DiameterException
  {
    Unsigned32AVP u32Avp = (Unsigned32AVP)Unsigned32AVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (u32Avp == null)
    {
      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
          "Could not create handler for Unsigned32Avp with code " + avpCode);
    }
    //header.setLength(header.getLength() + u32Avp.setData(data));
    header.length += u32Avp.setData(data);
    avpList.add(u32Avp);

    return 0;
  }

  /*
   * -------------------------------------------------------------------------
   * addInteger32Avp : Creates an Integer32AVP handler and sets the provided
   *                   data into that avp, and adds into avpList. Additionally,
   *                   it updates length info of Diameter message header which
   *                   will be used to calculate the length of raw message
   *                   block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - unsigned integer data to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in tne case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
//  public int addInteger32Avp(long avpCode, byte avpFlags, long vendorId,
//                                int data)           throws DiameterException
//  {
//    Integer32AVP int32Avp = (Integer32AVP)Integer32AVPMaker.getInstance().
//                                          createAVPHandler(avpCode, vendorId, avpFlags, null);
//    if (int32Avp == null)
//    {
//      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
//          "Could not create handler for Integer32Avp with code " + avpCode);
//    }
//    header.setLength(header.getLength() + int32Avp.setData(data));
//    avpList.add(int32Avp);
//
//    return 0;
//  }

  /*
   * -------------------------------------------------------------------------
   * addEnumeratedAvp : Creates an EnumeratedAvp handler and sets the provided
   *                    data into that avp, and adds into avpList. Additionally,
   *                    it updates length info of Diameter message header which
   *                    will be used to calculate the length of raw message
   *                    block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - unsigned integer data to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in the case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
//  public int addEnumeratedAvp(long avpCode, byte avpFlags, long vendorId,
//                                 int data)           throws DiameterException
//  {
//    EnumeratedAVP enumAvp = (EnumeratedAVP)EnumeratedAVPMaker.getInstance().
//                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
//    if (enumAvp == null)
//    {
//      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
//          "Could not create handler for EnumeratedAvp with code " + avpCode);
//    }
//    header.setLength(header.getLength() + enumAvp.setData(data));
//    avpList.add(enumAvp);
//
//    return 0;
//  }

  /*
   * -------------------------------------------------------------------------
   * addOctetStringAvp : Creates an OctetStringAVP handler and sets the
   *                     provided data into that avp, and adds into avpList.
   *                     Additionally, it updates length info of Diameter
   *                     message header which will be used to calculate the
   *                     length of raw message block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - byte stream data to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in tne case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
  public int addOctetStringAvp(long avpCode, byte avpFlags, long vendorId,
                                  byte[] data)         throws DiameterException
  {
    OctetStringAVP osAvp = (OctetStringAVP)OctetStringAVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (osAvp == null)
    {
      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
          "Could not create handler for OctetStringAvp with code " + avpCode);
    }
    int len = osAvp.setData(data);
    //header.setLength(header.getLength() + len + DiameterUtilities.calculatePadding(len));
    header.length += len + DiameterUtilities.calculatePadding(len);
    avpList.add(osAvp);

    return 0;
  }


  /*
   * -------------------------------------------------------------------------
   * addOctetStringAvp : Creates an OctetStringAVP handler and sets the
   *                     provided data into that avp, and adds into avpList.
   *                     Additionally, it updates length info of Diameter
   *                     message header which will be used to calculate the
   *                     length of raw message block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - byte stream data encapsulated by String object,
   *                     to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in tne case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
  public int addOctetStringAvp(long avpCode, byte avpFlags, long vendorId,
                                  String data)         throws DiameterException
  {
    OctetStringAVP osAvp = (OctetStringAVP)OctetStringAVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (osAvp == null)
    {
      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
          "Could not create handler for OctetStringAvp with code " + avpCode);
    }
    int len;
    try
    {
      len = osAvp.setData(data.getBytes("ISO-8859-1"));
    }
    catch (Exception e)
    {
      throw new DiameterException(1,
          "addOctetStringAvp<string>:Problem on getting bytes - " + e);
    }
    //header.setLength(header.getLength() + len + DiameterUtilities.calculatePadding(len));
    header.length += len + DiameterUtilities.calculatePadding(len);
    avpList.add(osAvp);

    return 0;
  }

  public int addGroupedAvp(long avpCode, byte avpFlags, long vendorId,
                              ArrayList<DiameterAVP> data)  throws DiameterException
  {
    GenericGroupedAVP grpAvp = (GenericGroupedAVP)GenericGroupedAVPMaker.getInstance().
                                 createAVPHandler(avpCode, vendorId, avpFlags,null);
    if (grpAvp == null)
    {
      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
          "Could not create handler for addGroupedAvp with code " + avpCode);
    }
    
    //header.setLength(header.getLength() + grpAvp.setData(data));
    header.length += grpAvp.setData(data);
    avpList.add(grpAvp);

    return 0;
  }
  
//  public int addGroupedAvp(long avpCode, byte avpFlags, long vendorId,
//        GenericGroupedAVP data)  throws DiameterException
//   {
//      GenericGroupedAVP grpAvp = (GenericGroupedAVP)GenericAVPMaker.getInstance().
//                 createAVPHandler(avpCode, vendorId, avpFlags,null);
//      if (grpAvp == null)
//      {
//      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
//      "Could not create handler for addGroupedAvp with code " + avpCode);
//      }
//      
//      header.setLength(header.getLength() + grpAvp.setData(data));
//      avpList.add(grpAvp);
//   
//   return 0;
//   }

  /*
   * -------------------------------------------------------------------------
   * addUnsigned64Avp : Creates an Unsigned64AVP handler and sets the provided
   *                    data into that avp, and adds into avpList. Additionally,
   *                    it updates length info of Diameter message header which
   *                    will be used to calculate the length of raw message
   *                    block.
   * Parameters : avpCode - The definition code of the AVP to be created
   *              avpFlags - AVP flags byte to be set
   *              vendorId - Vendor-Id information to be used for AVP, if
   *                         "V" flag is set on avpFlags. Otherwise it is
   *                         not used.
   *              data - unsigned integer data to be set
   * Returns : '0' for successful case. Currently not used.
   *   It may throw DiameterException in tne case of an AVP handler could not
   *   be created
   * -------------------------------------------------------------------------
   */
//  public int addUnsigned64Avp(long avpCode, byte avpFlags, long vendorId,
//                                 long data)           throws DiameterException
//  {
//    Unsigned64AVP u64Avp = (Unsigned64AVP)Unsigned64AVPMaker.getInstance().
//                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
//    if (u64Avp == null)
//    {
//      throw new DiameterException(DIAMETER_AVP_UNSUPPORTED,
//          "Could not create handler for Unsigned64Avp with code " + avpCode);
//    }
//    header.setLength(header.getLength() + u64Avp.setData(data));
//    avpList.add(u64Avp);
//
//    return 0;
//  }

  public static UTF8StringAVP buildUTF8AVP(long avpCode, byte avpFlags,
                                              long vendorId, String data)
                                                      throws DiameterException
  {
    UTF8StringAVP utf8Avp = (UTF8StringAVP)UTF8StringAVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (utf8Avp == null)
    {
      throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "Could not create handler for UTF8StringAVP with code " + avpCode);
    }
    utf8Avp.setData(data);

    return utf8Avp;
  }

  public static Unsigned32AVP buildUnsigned32AVP(long avpCode,byte avpFlags,
                                                    long vendorId, long data)
                                                          throws DiameterException
  {
    Unsigned32AVP u32Avp = (Unsigned32AVP)Unsigned32AVPMaker.getInstance().
                                           createAVPHandler(avpCode, vendorId, avpFlags, null);
    if (u32Avp == null)
    {
      throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY,
          "Could not create handler for Unsigned32Avp with code " + avpCode);
    }
    u32Avp.setData(data);

    return u32Avp;
  }

   /**
    * Builds an Integer32AVP.
    * 
    * @param avpCode
    * @param avpFlags
    * @param vendorId
    * @param data
    * @return
    * @throws DiameterException
    */
//  public static Integer32AVP buildInteger32AVP(long avpCode, byte avpFlags, long vendorId, int data) throws DiameterException {
//      Integer32AVP i32Avp = (Integer32AVP) Integer32AVPMaker.getInstance().createAVPHandler(avpCode, vendorId, avpFlags, null);
//      if (i32Avp == null) {
//         throw new DiameterException(DIAMETER_UNABLE_TO_COMPLY, "Could not create handler for Integer32Avp with code " + avpCode);
//      }
//      i32Avp.setData(data);
//
//      return i32Avp;
//   }


   /*
    * -------------------------------------------------------------------------
    * addTimeAvp : Creates a TimeAVP handler and sets the provided
    *              data into that avp, and adds into avpList. Additionally,
    *              it updates length info of Diameter message header which
    *              will be used to calculate the length of raw message
    *              block.
    * Parameters : avpCode - The definition code of the AVP to be created
    *              avpFlags - AVP flags byte to be set
    *              vendorId - Vendor-Id information to be used for AVP, if
    *                         "V" flag is set on avpFlags. Otherwise it is
    *                         not used.
    *              data - time data to be set
    * Returns : '0' for successful case. Currently not used.
    *   It may throw DiameterException in the case of an AVP handler could not
    *   be created
    * -------------------------------------------------------------------------
    */
//   public int addTimeAvp(long avpCode, byte avpFlags, long vendorId, long data) throws DiameterException {
//      TimeAVP timeAvp = (TimeAVP) TimeAVPMaker.getInstance().createAVPHandler(avpCode, vendorId, avpFlags, null);
//      if (timeAvp == null) {
//         throw new DiameterException(DIAMETER_AVP_UNSUPPORTED, "Could not create handler for Time avp with code " + avpCode);
//      }
//      int len = timeAvp.setData(data);
//      header.setLength(header.getLength() + len + DiameterUtilities.calculatePadding(len));
//      avpList.add(timeAvp);
//
//      return 0;
//   }
//   
//   public int addAVP(DiameterAVP avp) {
//      this.avpList.add(avp);
//      return 0;
//   }
   
   
  
  /*
   * -------------------------------------------------------------------------
   * buildRawData : It is used internally, for building purposes, after
   *                high-level representation of Diameter message has been
   *                obtained, to construct the message in raw format.
   *
   * Parameters : None.
   *              Internally uses high-level data representation encapsulated
   *              into DiameterMessage object
   * Returns : None.
   *           Creates and updates rawMesage.
   * -------------------------------------------------------------------------
   */
  public void buildRawData() throws DiameterException
  {
    rawMessage = new byte[header.getLength()];
    int index = 0;
    /*=======================================================================*/
    /*  Diameter Header Part                                                 */
    /*=======================================================================*/
    rawMessage[index] = header.getVersion();
    index++;
    DiameterUtilities.set3Bytes(rawMessage, index, header.getLength());
    index += 3;
    rawMessage[index] = header.getFlagsByte();
    index++;
    DiameterUtilities.set3Bytes(rawMessage, index, header.getCommandCode());
    index += 3;
    DiameterUtilities.set4Bytes(rawMessage, index, header.getApplicationId());
    index += 4;
    DiameterUtilities.set4Bytes(rawMessage, index, header.getHopByHopId());
    index += 4;
    DiameterUtilities.set4Bytes(rawMessage, index, header.getEndToEndId());
    index += 4;

    /*=======================================================================*/
    /*  AVPs Part                                                            */
    /*=======================================================================*/
    DiameterAVP avp;
    Iterator<DiameterAVP> iterator = avpList.iterator();
    while (iterator.hasNext())
    {
      avp = iterator.next();
      index = avp.buildAvpIntoRawData(rawMessage, index);
    }
  }


  /*=========================================================================*/
  /*           Examples to be used in Failed-AVP AVP                         */
  /*=========================================================================*/
  protected DiameterAVP buildExampleSessionIdAvp() throws DiameterException
  {
    return buildUTF8AVP(AVP_SESSION_ID, AVP_Mflag, 0,
                                   "abcd.xy.com:33054;23561;2358;0AF3B82");
  }

  /* =========================================================================
   * Accessors
   * =========================================================================
   */
  public DiameterHeader getHeader()
  {
    return this.header;
  }

  /*
   * -------------------------------------------------------------------------
   * getAvpList : Simply returns the reference of avpList which keeps AVPs in
   *              the message as in DiameterAVP format. 
   * NOTE : The avpList may not include all AVPs in the message if the method
   *        is called before the parsing or building is completed. Assumed that
   *        the caller is responsible of this.
   *        
   * Parameters : None.
   * Returns : Vector of DiameterAVP
   * -------------------------------------------------------------------------
   */  
  public ArrayList<DiameterAVP> getAvpList()
  //public AVPList getAvpList()
  {
    return this.avpList;
    //return this.avpList.getAvpList();
  }

  /*
   * -------------------------------------------------------------------------
   * getAvp : Sequentially searches avpList of the message to find the  
   *          reference to the AVP requested by avpCode in DiameterAVP format.  
   * NOTE : The avpList may not include all AVPs in the message if the method
   *        is called before the parsing or building is completed. Assumed 
   *        that the caller is responsible of this.
   *        
   * Parameters : avpCode - The code for the AVP that requested
   * Returns : The reference of DiameterAVP representing AVP with code avpCode
   * -------------------------------------------------------------------------
   */  
  public DiameterAVP getAvp(long avpCode)
  {
    Iterator<DiameterAVP> iterator = avpList.iterator();
    DiameterAVP avp;
    while (iterator.hasNext())
    {
      avp = iterator.next();
      if (avp.code == avpCode)
      {
        return avp;
      }
    }
    return null;
  }

  public byte[] getRawMessage()
  {
    return this.rawMessage;
  }
  
//  public int getAcctAppSeperator() {
//   return acctAppSeperator;
//}

  /*protected String getCommandName()
  {
    return "Unimplemented Diameter Command";
  }*/
  protected abstract String getCommandName();

  /* =========================================================================
   * Utilities
   * =========================================================================
   */
  /*
   * -------------------------------------------------------------------------
   * printContent : Common implementation to print-out message content in
   *                human-readable format. Calls printContent of header and
   *                goes through avpList to invoke AVP's printData method.
   *                It is used for debug/monitoring purposes.
   * Parameters : buffer - StringBuilder to put data
   * Returns : None
   * -------------------------------------------------------------------------
   */
  public void printContent(StringBuilder buffer)
  {
    buffer.append("\n");
    buffer.append(getCommandName());
    header.printContent(buffer);
    DiameterAVP avp;
    String indent = "    ";
    Iterator<DiameterAVP> iterator = avpList.iterator();
    while (iterator.hasNext())
    {
      avp = iterator.next();
      avp.printContent(buffer, indent);
    }
  }

  public byte[] getRawAVPFromRawData(int start, int end, long code)
  {
    int  index = start;
    long avpCode = 0;
    int  avpLength = 0;
    byte avpFlags = 0;
    long vendorId = 0;
    int  avpDataLength = 0;

    //int sequence = 0;
    int avpStartIndex;
    
    // Assume that the "start" indicates a real start for an AVP
    while (index < end)
    {
      // AVP's start index is to be used for failure cases, to obtain failedAVP
      avpStartIndex = index;

      // TODO: Check if remaining bytes are enough to for a possible AVP

      avpCode = DiameterUtilities.get4BytesAsUnsigned32(this.rawMessage, index);
      index += AVP_CODE_SIZE;
      avpFlags = (byte)(this.rawMessage[index] & 0xFF);
      index += AVP_FLAGS_SIZE;
      avpLength = DiameterUtilities.get3Bytes(this.rawMessage, index);
      index += AVP_LENGTH_SIZE;

      if ((avpFlags & AVPFLAGS_V_BIT_MASK) != 0)
      {
        vendorId = DiameterUtilities.get4BytesAsUnsigned32(this.rawMessage, index);
        index += AVP_VENDOR_ID_SIZE;
        avpDataLength = avpLength - AVP_HEADER_SIZE_WITH_VENDORID;
      }
      else
      {
        avpDataLength = avpLength - AVP_HEADER_SIZE_NO_VENDORID;
        // reset vendorId that could consists of the value from previous AVP
        vendorId = 0;
      }

      // TODO: Check for avpLength=0?
      if ((avpDataLength > avpLength) || ((end - index) < avpDataLength))
      {
        // DIAMETER_INVALID_AVP_LENGTH (permanent failure)
        // Assume all data from index to rawMsg.length is related to this avp
        //int len = ((rawMsg.length - index) < avpDataLength) ?
        //           (rawMsg.length - index) : avpLength;
        //byte[] failedAvpData = new byte[len];
        //System.arraycopy(rawMsg,avpStartIndex,failedAvpData,0,len);
        //throw new DiameterParseException(DIAMETER_INVALID_AVP_LENGTH,
        //                                 failedAvpData, message,
        //                                 "No enough AVP data remaining");
      }

      int padding = DiameterUtilities.calculatePadding(avpLength);
      int completeLength = avpLength + padding;
      
      if (avpCode == code)
      {
        byte[] returnBytes = new byte[completeLength];
        System.arraycopy(this.rawMessage, avpStartIndex, returnBytes, 0, completeLength);
        return returnBytes;
      }
      else
      {
        // jump to other AVP
        index += avpDataLength + padding;
      }
    }
    return null; 
  }

}
