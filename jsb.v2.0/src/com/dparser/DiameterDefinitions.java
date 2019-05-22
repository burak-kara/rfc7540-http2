package com.dparser;

public interface DiameterDefinitions {

	/*=========================================================================*/
	  /* Diameter Header Definitions                                             */
	  /*=========================================================================*/
	  public final static byte DIAMETER_VERSION = 1;

	  /* Header format/sizes
	  */
	  public final static int VERSION_SIZE        = 1;
	  public final static int MESSAGE_LENGTH_SIZE = 3;
	  public final static int FLAGS_SIZE          = 1;
	  public final static int COMMAND_CODE_SIZE   = 3;
	  public final static int APPLICATION_ID_SIZE = 4;
	  public final static int HOP_BY_HOP_ID_SIZE  = 4;
	  public final static int END_TO_END_ID_SIZE  = 4;

	  public final static int DIAMETER_MSG_HDR_SIZE = VERSION_SIZE        +
	                                                  MESSAGE_LENGTH_SIZE +
	                                                  FLAGS_SIZE          +
	                                                  COMMAND_CODE_SIZE   +
	                                                  APPLICATION_ID_SIZE +
	                                                  HOP_BY_HOP_ID_SIZE  +
	                                                  END_TO_END_ID_SIZE;
	  /* Diameter Header Flags
	   */
	  public final static byte HFLAGS_NONE  = (byte)0x00;
	  public final static byte HFLAGS_R     = (byte)0x80;
	  public final static byte HFLAGS_R_P   = (byte)0xC0;
	  public final static byte HFLAGS_R_T   = (byte)0x90;
	  public final static byte HFLAGS_R_P_T = (byte)0xD0;
	  public final static byte HFLAGS_P     = (byte)0x40;
	  public final static byte HFLAGS_P_E   = (byte)0x60;
	  public final static byte HFLAGS_E     = (byte)0x20;
	  /*
	   * Masks
	   */
	  public final static int HFLAGS_R_BIT_MASK = 0x80;
	  public final static int HFLAGS_P_BIT_MASK = 0x40;
	  public final static int HFLAGS_E_BIT_MASK = 0x20;
	  public final static int HFLAGS_T_BIT_MASK = 0x10;
	  public final static int HFLAGS_RESERVED_BITS_MASK = 0x0F;

	  /* Diameter Application Ids defined by Base Diameter Protocol (RFC 3588)
	   */
	  public final static int DIAMETER_APPID_COMMON_MESSAGES = 0;
	  public final static int DIAMETER_APPID_NASREQ          = 1;
	  public final static int DIAMETER_APPID_MOBILE_IP       = 2;
	  public final static int DIAMETER_APPID_BASE_ACCOUNTING = 3;
	  public final static int DIAMETER_APPID_RELAY           = 0xffffffff;
	  
	  /*
	   * Command Codes
	   */
	  public final static int CAPABILITIES_EXCHANGE_REQUEST = 257; // RFC 3588
	  public final static int CAPABILITIES_EXCHANGE_ANSWER  = 257; // RFC 3588
	  public final static int RE_AUTH_REQUEST               = 258; // RFC 3588
	  public final static int RE_AUTH_ANSWER                = 258; // RFC 3588
	  public final static int AA_REQUEST                    = 265; // RFC 4005
	  public final static int AA_ANSWER                     = 265; // RFC 4005
	  public final static int ACCOUNTING_REQUEST            = 271; // RFC 3588
	  public final static int ACCOUNTING_ANSWER             = 271; // RFC 3588
	  public final static int ABORT_SESSION_REQUEST         = 274; // RFC 3588
	  public final static int ABORT_SESSION_ANSWER          = 274; // RFC 3588
	  public final static int SESSION_TERMINATION_REQUEST   = 275; // RFC 3588
	  public final static int SESSION_TERMINATION_ANSWER    = 275; // RFC 3588
	  public final static int DEVICE_WATCHDOG_REQUEST       = 280; // RFC 3588
	  public final static int DEVICE_WATCHDOG_ANSWER        = 280; // RFC 3588
	  public final static int DISCONNECT_PEER_REQUEST       = 282; // RFC 3588
	  public final static int DISCONNECT_PEER_ANSWER        = 282; // RFC 3588

	  public final static int DIAMETER_EXPERIMENTAL_COMMANDCODE_1 = 0xFFFFFE; // RFC 3588
	  public final static int DIAMETER_EXPERIMENTAL_COMMANDCODE_2 = 0xFFFFFF; // RFC 3588

//	  /*
//	   * Application Seperator for Accounting
//	   */
//	  public final static int APP_OTHER = 0;
//	  public final static int APP_X2    = 1;
//	  public final static int APP_RF    = 2;
	  
	  /*=========================================================================*/
	  /* AVP (Attribute-Value Pair) Header Defintions                            */
	  /*=========================================================================*/
	  /* AVP Format/Sizes
	   */
	  public final static int AVP_CODE_SIZE      = 4;
	  public final static int AVP_FLAGS_SIZE     = 1;
	  public final static int AVP_LENGTH_SIZE    = 3;
	  public final static int AVP_VENDOR_ID_SIZE = 4;

	  /* Vendor-Id is an optional parameter for AVP header and existence is
	   * controlled via V flag bit. It determines the size of AVP element.
	   */
	  public final static int AVP_HEADER_SIZE_NO_VENDORID = AVP_CODE_SIZE  +
	                                                        AVP_FLAGS_SIZE +
	                                                        AVP_LENGTH_SIZE;

	  public final static int AVP_HEADER_SIZE_WITH_VENDORID = AVP_CODE_SIZE   +
	                                                          AVP_FLAGS_SIZE  +
	                                                          AVP_LENGTH_SIZE +
	                                                          AVP_VENDOR_ID_SIZE;

	  public final static int AVPFLAGS_V_BIT_MASK = 0x80;
	  public final static int AVPFLAGS_M_BIT_MASK = 0x40;
	  public final static int AVPFLAGS_P_BIT_MASK = 0x20;
	  public final static int AVPFLAGS_RESERVED_BIT_MASK = 0x1F;

	  // Constants for AVP Header Flags to be used by application
	  //
	  public final static byte AVP_NOflag  = (byte)0;
	  public final static byte AVP_Mflag   = (byte)0x40;
	  public final static byte AVP_VMflag  = (byte)0xC0;
	  public final static byte AVP_Vflag   = (byte)0x80;


	  /*=========================================================================*/
	  /* AVP data types                                                          */
	  /* Refer to sections 4.2 and 4.3 of RFC 3588                               */
	  /*.._OCTETSTRING is assumed as a default value for unknown data types      */
	  /*=========================================================================*/
	  public final static int
	    AVP_DATATYPE_NONE = 0,
	    /* Basic Types */
	    AVP_DATATYPE_OCTETSTRING = 1,
	    AVP_DATATYPE_INTEGER32 = 2,
	    AVP_DATATYPE_INTEGER64 = 3,
	    AVP_DATATYPE_UNSIGNEDINT32 =4,
	    AVP_DATATYPE_UNSIGNEDINT64 =5,
	    AVP_DATATYPE_FLOAT32 = 6,
	    AVP_DATATYPE_FLOAT64 = 7,
	    AVP_DATATYPE_GROUPED = 8,
	    /* Derived Types */
	    AVP_DATATYPE_ADDRESS = 9,
	    AVP_DATATYPE_TIME = 10,
	    AVP_DATATYPE_UTF8STRING = 11,
	    AVP_DATATYPE_DIAMETERIDENTITY = 12,
	    AVP_DATATYPE_DIAMETERURI = 13,
	    AVP_DATATYPE_ENUMERATED = 14,
	    AVP_DATATYPE_IPFILTERRULE = 15,
	    AVP_DATATYPE_QOSFILTERRULE = 16,
	    AVP_DATATYPE_HEX = 17;

	  /*=========================================================================*/
	  /* Constants for AVPs defined by IETF                                      */
	  /* (not all of them, only used ones, at the moment)                        */
	  /*=========================================================================*/
	  /*                                                                 *Data Type*
	   */
	  public final static int AVP_SESSION_ID                     = 263;// UTF8String
	  public final static int AVP_VENDOR_SPECIFIC_APPLICATION_ID = 260;// Grouped
	  public final static int AVP_VENDOR_ID                      = 266;// Unsigned32
	  public final static int AVP_HOST_IP_ADDRESS                = 257;// Address
	  public final static int AVP_AUTH_APPLICATION_ID            = 258;// Unsigned32
	  public final static int AVP_SESSION_BINDING                = 270;// Unsigned32
	  public final static int AVP_SESSION_SERVER_FAILOVER        = 271;// Enumerated
	  public final static int AVP_MULTI_ROUND_TIME_OUT           = 272;// Unsigend32
	  public final static int AVP_DISCONNECT_CAUSE               = 273;// Enumerated
	  public final static int AVP_AUTH_REQUEST_TYPE              = 274;// Enumerated
	  public final static int AVP_ACCT_APPLICATION_ID            = 259;// Unsigned32
	  public final static int AVP_AUTHORIZATION_LIFETIME         = 291;// Unsigned32
	  public final static int AVP_AUTH_GRACE_PERIOD              = 276;// Unsigned32
	  public final static int AVP_AUTH_SESSION_STATE             = 277;// Enumerated
	  public final static int AVP_ORIGIN_HOST                    = 264;// DiamIdent
	  public final static int AVP_SUPPORTED_VENDOR_ID            = 265;// Unsigned32
	  public final static int AVP_ORIGIN_REALM                   = 296;// DiamIdent
	  public final static int AVP_DESTINATION_HOST               = 293;// DiamIdent
	  public final static int AVP_DESTINATION_REALM              = 283;// DiamIdent
	  public final static int AVP_PROXY_INFO                     = 284;// Grouped
	  public final static int AVP_RE_AUTH_REQUEST_TYPE           = 285;// Enumerated
	  public final static int AVP_PROXY_HOST                     = 280;// DiamIdent
	  public final static int AVP_PROXY_STATE                    = 33; // OctetString
	  public final static int AVP_ROUTE_RECORD                   = 282;// DiamIdent
	  public final static int AVP_FIRMWARE_REVISION              = 267;// Unsigned32
	  public final static int AVP_RESULT_CODE                    = 268;// Unsigned32
	  public final static int AVP_PRODUCT_NAME                   = 269;// UTF8String
	  public final static int AVP_EXPERIMENTAL_RESULT            = 297;// Grouped
	  public final static int AVP_EXPERIMENTAL_RESULT_CODE       = 298;// Unsigned32
	  public final static int AVP_INBAND_SECURITY_ID             = 299;// Unsigned32
	  public final static int AVP_FAILED_AVP                     = 279;// Grouped
	  public final static int AVP_ERROR_REPORTING_HOST           = 294;// DiamIdent
	  public final static int AVP_ORIGIN_STATE_ID                = 278;// Unsigned32
	  public final static int AVP_ERROR_MESSAGE                  = 281;// UTF8String
	  public final static int AVP_REDIRECT_HOST_USAGE            = 261;// Enumerated
	  public final static int AVP_REDIRECT_MAX_CACHE_TIME        = 262;// Unsigned32
	  public final static int AVP_TERMINATION_CAUSE              = 295;// Enumerated

	  public final static int AVP_CLASS                          = 25; // OctetString
	  public final static int AVP_SESSION_TIMEOUT                = 27; // Unsigned32
	  public final static int AVP_USER_NAME                      = 1;  // UTF8String 
	  public final static int AVP_ACCT_SESSION_ID                = 44; // OctetString
	  public final static int AVP_ACCOUNTING_SUB_SESSION_ID      = 287;// Unsigned64
	  public final static int AVP_ACCT_MULTI_SESSION_ID          = 50; // UTF8String
	  public final static int AVP_ACCT_INTERIM_INTERVAL          = 85; // Unsigned32
	  public final static int AVP_ACCOUNTING_REALTIME_REQUIRED   = 483;// Enumerated
	  public static final int AVP_ACCOUNTING_RECORD_TYPE         = 480; // Enumerated
	  public static final int AVP_ACCOUNTING_RECORD_NUMBER       = 485; // Unsigned32
	  public static final int AVP_EVENT_TIMESTAMP                =  55; // Time
	  
	  /* Because of Rx Rel.7 Support
	   *
	   */
	  public final static int AVP_SUBSCRIPTION_ID      = 443; // Grouped    [RFC 4006]
	  public final static int AVP_SUBSCRIPTION_ID_TYPE = 450; // Enumerated [RFC 4006]
	  public final static int AVP_SUBSCRIPTION_ID_DATA = 444; // UTF8String [RFC 4006]
	  public final static int AVP_FRAMED_IP_ADDRESS    =   8; // OctetString [RFC 4005]
	  public final static int AVP_FRAMED_IPv6_PREFIX   =  97; // OctetString [RFC 4005]

	  /* Required fror Rf/Ro
	   */
	  public static final int AVP_SERVICE_CONTEXT_ID       = 461; // UTF8String
	  public static final int AVP_EAP_PAYLOAD              = 462; // OctetString [RFC 4072]
	  public static final int AVP_FILTER_ID                =  11; // UTF8String  [RFC 4005]
	  public static final int AVP_REDIRECT_HOST            = 292; // DiamURI
	  
	  public final long UNASSIGNED_LONG_VALUE = -1;

	  /* Values for Address Family to be used by Address type AVPs.
	   * Defined in http://www.iana.org/assignments/address-family-numbers.
	   * We have got only two of them at the moment (to be extended on demand).
	   */
	  public final static int
	    IPv4_ADDRESS = 1,
	    IPv6_ADDRESS = 2;

	  /* Enums for Auth-Session-State values (refer to section 8.11 of RFC 3588)
	   */
	  public final static int
	    STATE_MAINTAINED    = 0,
	    NO_STATE_MAINTAINED = 1;

	  /* Enums for Subscription-Id-Type values (refer to section 8.47 of RFC 4006)
	   */
	  public final static int
	    END_USER_E164    = 0, /* The identifier is in international E.164 format (e.g., MSISDN),
	                           * according to the ITU-T E.164 numbering plan defined in E.164. */
	    END_USER_IMSI    = 1, /* The identifier is in international IMSI format, according to the
	                           * ITU-T E.212 numbering plan */
	    END_USER_SIP_URI = 2, /* The identifier is in the form of a SIP URI, as defined in RFC 3261. */
	    END_USER_NAI     = 3, /* The identifier is in the form of a Network Access Identifier, as
	                           * defined in RFC 2486. */
	    END_USER_PRIVATE = 4; /* The Identifier is a credit-control server private identifier. */
	  public static final String[] SUBS_ID_TYPE_STR = {"END_USER_E164","END_USER_IMSI","END_USER_SIP_URI",
	                                                   "END_USER_NAI","END_USER_PRIVATE"};


	  /*=========================================================================*/
	  /* Result-Code AVP Values                                                  */
	  /*=========================================================================*/
	  // Informational
	  public final static int DIAMETER_MULTI_ROUND_AUTH  = 1001;    // RFC 3588
	  // Success
	  public final static int DIAMETER_SUCCESS           = 2001;    // RFC 3588
	  public final static int DIAMETER_LIMITED_SUCCESS   = 2002;    // RFC 3588
	  // Protocol Errors
	  public final static int DIAMETER_COMMAND_UNSUPPORTED     = 3001; // RFC 3588
	  public final static int DIAMETER_UNABLE_TO_DELIVER       = 3002; // RFC 3588
	  public final static int DIAMETER_REALM_NOT_SERVED        = 3003; // RFC 3588
	  public final static int DIAMETER_TOO_BUSY                = 3004; // RFC 3588
	  public final static int DIAMETER_LOOP_DETECTED           = 3005; // RFC 3588
	  public final static int DIAMETER_REDIRECT_INDICATION     = 3006; // RFC 3588
	  public final static int DIAMETER_APPLICATION_UNSUPPORTED = 3007; // RFC 3588
	  public final static int DIAMETER_INVALID_HDR_BITS        = 3008; // RFC 3588
	  public final static int DIAMETER_INVALID_AVP_BITS        = 3009; // RFC 3588
	  public final static int DIAMETER_UNKNOWN_PEER            = 3010; // RFC 3588
	  // Transient Failures
	  public final static int DIAMETER_AUTHENTICATION_REJECTED        = 4001;  // RFC 3588
	  public final static int DIAMETER_OUT_OF_SPACE                   = 4002;  // RFC 3588
	  public final static int ELECTION_LOST                           = 4003;  // RFC 3588
	  public final static int DIAMETER_ERROR_MIP_REPLY_FAILURE        = 4005;  // RFC 4004
	  public final static int DIAMETER_ERROR_HA_NOT_AVAILABLE         = 4006;  // RFC 4004
	  public final static int DIAMETER_ERROR_BAD_KEY                  = 4007;  // RFC 4004
	  public final static int DIAMETER_ERROR_MIP_FILTER_NOT_SUPPORTED = 4008;  // RFC 4004
	  public final static int DIAMETER_END_USER_SERVICE_DENIED        = 4010;  // RFC 4006
	  public final static int DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE  = 4011;  // RFC 4006
	  public final static int DIAMETER_CREDIT_LIMIT_REACHED           = 4012;  // RFC 4006
	  // Permanent Failures
	  public final static int DIAMETER_AVP_UNSUPPORTED              = 5001; // RFC 3588
	  public final static int DIAMETER_UNKNOWN_SESSION_ID           = 5002; // RFC 3588
	  public final static int DIAMETER_AUTHORIZATION_REJECTED       = 5003; // RFC 3588
	  public final static int DIAMETER_INVALID_AVP_VALUE            = 5004; // RFC 3588
	  public final static int DIAMETER_MISSING_AVP                  = 5005; // RFC 3588
	  public final static int DIAMETER_RESOURCES_EXCEEDED           = 5006; // RFC 3588
	  public final static int DIAMETER_CONTRADICTING_AVPS           = 5007; // RFC 3588
	  public final static int DIAMETER_AVP_NOT_ALLOWED              = 5008; // RFC 3588
	  public final static int DIAMETER_AVP_OCCURS_TOO_MANY_TIMES    = 5009; // RFC 3588
	  public final static int DIAMETER_NO_COMMON_APPLICATION        = 5010; // RFC 3588
	  public final static int DIAMETER_UNSUPPORTED_VERSION          = 5011; // RFC 3588
	  public final static int DIAMETER_UNABLE_TO_COMPLY             = 5012; // RFC 3588
	  public final static int DIAMETER_INVALID_BIT_IN_HEADER        = 5013; // RFC 3588
	  public final static int DIAMETER_INVALID_AVP_LENGTH           = 5014; // RFC 3588
	  public final static int DIAMETER_INVALID_MESSAGE_LENGTH       = 5015; // RFC 3588
	  public final static int DIAMETER_INVALID_AVP_BIT_COMBO        = 5016; // RFC 3588
	  public final static int DIAMETER_NO_COMMON_SECURITY           = 5017; // RFC 3588
	  public final static int DIAMETER_ERROR_NO_FOREIGN_HA_SERVICE         = 5024; // RFC 4004
	  public final static int DIAMETER_ERROR_END_TO_END_MIP_KEY_ENCRYPTION = 5025; // RFC 4004
	  public final static int DIAMETER_USER_UNKNOWN                 = 5030; // RFC 4006
	  public final static int DIAMETER_RATING_FAILED                = 5031; // RFC 4006

	  // To be used internally, as default value
	  public final static int DIAMETER_NO_RESULT                    = 0;


	  // baseline NTP time if bit-0=0 -> 7-Feb-2036 @ 06:28:16 UTC
	  public final static long msb0baseTime = 2085978496000L;

	  // baseline NTP time if bit-0=1 -> 1-Jan-1900 @ 01:00:00 UTC
	  public final static long msb1baseTime = -2208988800000L;
	  
	  // Termination-Cause AVP definitions
	  public final static int
	     TC_STR_NO_TYPE = 0,
	     TC_LOGOUT = 1,
	     TC_SERVICE_NOT_PROVIDED = 2,
	     TC_BAD_ANSWER = 3,
	     TC_ADMINISTRATIVE = 4,
	     TC_LINK_BROKEN = 5,
	     TC_AUTH_EXPIRED = 6,
	     TC_USER_MOVED = 7,
	     TC_SESSION_TIMEOUT =8;
}
