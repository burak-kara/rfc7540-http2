package com.dparser;

import java.util.StringTokenizer;

public class DiameterUtilities implements DiameterDefinitions {
	// Macro for calculating AVP header size as depending on existence of
	  // Vendor ID field in the AVP block.
	  //
	  public static int DIAMETER_AVP_HDR_SIZE(byte avpFlags)
	  {
	    return ((avpFlags & AVPFLAGS_V_BIT_MASK) != 0) ?
	                   AVP_HEADER_SIZE_WITH_VENDORID : AVP_HEADER_SIZE_NO_VENDORID;
	  }

	  public static boolean isProtocolError(long error)
	  {
	    return ((error < DIAMETER_COMMAND_UNSUPPORTED) ||
	            (error > DIAMETER_UNKNOWN_PEER)) ? false : true;
	  }

	  public static boolean isTransientFailure(long failure)
	  {
	     return ((failure >= DiameterDefinitions.DIAMETER_AUTHENTICATION_REJECTED)
	              && (failure <= DiameterDefinitions.DIAMETER_CREDIT_LIMIT_REACHED));
	  }

	  public static boolean isPermanentFailure(long failure)
	  {
	    return ((failure < DIAMETER_AVP_UNSUPPORTED) ||
	            (failure > DIAMETER_RATING_FAILED)) ?  false :  true;
	  }
	 
	  public static long get1BytesAsUnsigned8(byte[] buffer, int index)
	  {
		return (((long) buffer[index] & 0xFF));
	  }
	  
	  public static long get2BytesAsUnsigned16(byte[] buffer, int index)
	  {
	    return ((((long)buffer[index]   & 0xFF) << 8)  |
	              (long)buffer[index+1] & 0xFF);
	  }

	  public static long get3BytesAsUnsigned24(byte[] buffer, int index)
	  {
	    return ((((long)buffer[index]   & 0xFF) << 16) |
	            (((long)buffer[index+1] & 0xFF) << 8)  |
	             ((long)buffer[index+2] & 0xFF));
	  }
	  public static int get2Bytes(byte[] buffer, int index)
	  {
	    return  ((buffer[index]   & 0xFF) << 8)  |
	             (buffer[index+1] & 0xFF);
	  }
	  public static int get3Bytes(byte[] buffer, int index)
	  {
		return ((buffer[index]   & 0xFF) << 16) | 
			   ((buffer[index+1] & 0xFF) << 8)  | 
			    (buffer[index+2] & 0xFF);
	  }
	  public static int get4Bytes(byte[] buffer, int index)
	  {
	   return  (((buffer[index]   & 0xFF) << 24) |
	    		((buffer[index+1] & 0xFF) << 16) |
	            ((buffer[index+2] & 0xFF) << 8)  |
	             (buffer[index+3] & 0xFF));
	  }
	  public static long get5BytesAsUnsigned40(byte[] buffer, int index)
	  {
	    return  ((((long)buffer[index]  & 0xFF) << 32) |
	    		(((long)buffer[index+1] & 0xFF) << 24) |
	    		(((long)buffer[index+2] & 0xFF) << 16) |
	            (((long)buffer[index+3] & 0xFF) << 8)  |
	             ((long)buffer[index+4] & 0xFF));
	  }
	  public static long get6BytesAsUnsigned48(byte[] buffer, int index)
	  {
	    return  ((((long)buffer[index]   & 0xFF) << 40) |
	    	     (((long)buffer[index+1] & 0xFF) << 32) |
	    	     (((long)buffer[index+2] & 0xFF) << 24) |
	    		 (((long)buffer[index+3] & 0xFF) << 16) |
	             (((long)buffer[index+4] & 0xFF) << 8)  |
	              ((long)buffer[index+5] & 0xFF));
	  }
	  public static long get7BytesAsUnsigned56(byte[] buffer, int index)
	  {
	    return ((((long)buffer[index]   & 0xFF) << 48) |
	    	    (((long)buffer[index+1] & 0xFF) << 40) |
	    	    (((long)buffer[index+2] & 0xFF) << 32) |
	    		(((long)buffer[index+3] & 0xFF) << 24) |
	    		(((long)buffer[index+4] & 0xFF) << 16) |
	            (((long)buffer[index+5] & 0xFF) << 8)  |
	             ((long)buffer[index+6] & 0xFF));
	  }
	  public static long get8BytesAsUnsigned64(byte[] buffer, int index)
	  {
	    return 	((((long)buffer[index]   & 0xFF) << 56) |
	    		 (((long)buffer[index+1] & 0xFF) << 48) |
	    		 (((long)buffer[index+2] & 0xFF) << 40) |
	    		 (((long)buffer[index+3] & 0xFF) << 32) |
	    		 (((long)buffer[index+4] & 0xFF) << 24) |
	    		 (((long)buffer[index+5] & 0xFF) << 16) |
	             (((long)buffer[index+6] & 0xFF) << 8)  |
	              ((long)buffer[index+7] & 0xFF));
	  }

	  public static long get4BytesAsUnsigned32(byte[] buffer, int index)
	  {
	    return ((((long)buffer[index]   & 0xFF) << 24) |
	            (((long)buffer[index+1] & 0xFF) << 16) |
	            (((long)buffer[index+2] & 0xFF) << 8)  |
	             ((long)buffer[index+3] & 0xFF));
	  }

	  public static int get4BytesAsInteger32(byte[] buffer, int index)
	  {
	    return (((buffer[index]   & 0xFF) << 24) |
	            ((buffer[index+1] & 0xFF) << 16) |
	            ((buffer[index+2] & 0xFF) << 8)  |
	             (buffer[index+3] & 0xFF));
	  }

	  public static long get8BytesAsInteger64(byte[] buffer, int index)
	  {
	    return (((buffer[index]   & 0xFF) << 56) |
	            ((buffer[index+1] & 0xFF) << 48) |
	            ((buffer[index+2] & 0xFF) << 40) |
	            ((buffer[index+3] & 0xFF) << 32) |
	            ((buffer[index+4] & 0xFF) << 24) |
	            ((buffer[index+5] & 0xFF) << 16) |
	            ((buffer[index+6] & 0xFF) << 8)  |
	             (buffer[index+7] & 0xFF));
	  }
	  
	  public static long get8BytesAsUnsignedInteger64(byte[] buffer, int index)
	  {
	    return ((((long)buffer[index]   & 0xFF) << 56) |
	            (((long)buffer[index+1] & 0xFF) << 48) |
	            (((long)buffer[index+2] & 0xFF) << 40) |
	            (((long)buffer[index+3] & 0xFF) << 32) |
	            (((long)buffer[index+4] & 0xFF) << 24) |
	            (((long)buffer[index+5] & 0xFF) << 16) |
	            (((long)buffer[index+6] & 0xFF) << 8)  |
	             ((long)buffer[index+7] & 0xFF));
	  }

	  public static void set1Byte(byte[] buffer, int index, long data)
	  {
	    buffer[index] = (byte)(data & 0xFF);
	   
	  }
	  public static void set2Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+1] = (byte)(data & 0xFF);
	    buffer[index]   = (byte)((data & 0xFF00) >>> 8);
	  }
	  
	  public static void set3Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+2] = (byte) (data & 0xFF);
	    buffer[index+1] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index]   = (byte)((data & 0xFF0000) >>> 16);
	  }

	  public static void set4Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+3] = (byte) (data & 0xFF);
	    buffer[index+2] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index+1] = (byte)((data & 0xFF0000) >>> 16);
	    buffer[index]   = (byte)((data & 0xFF000000) >>> 24);
	  }
      
	  public static void set5Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+4] = (byte) (data & 0xFF);
	    buffer[index+3] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index+2] = (byte)((data & 0xFF0000) >>> 16);
	    buffer[index+1]   = (byte)((data & 0xFF000000) >>> 24);
	    buffer[index]   = (byte)((data & 0xFF00000000L) >>> 32);
	  }
      
	  public static void set6Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+5] = (byte) (data & 0xFF);
	    buffer[index+4] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index+3] = (byte)((data & 0xFF0000) >>> 16);
	    buffer[index+2]   = (byte)((data & 0xFF000000) >>> 24);
	    buffer[index+1] = (byte)((data & 0xFF00000000L) >>> 32);
	    buffer[index]   = (byte)((data & 0xFF0000000000L) >>> 40);
	  }

	  public static void set7Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+6] = (byte) (data & 0xFF);
	    buffer[index+5] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index+4] = (byte)((data & 0xFF0000) >>> 16);
	    buffer[index+3]  =(byte)((data & 0xFF000000) >>> 24);
	    buffer[index+2] = (byte)((data & 0xFF00000000L) >>> 32);
	    buffer[index+1] = (byte)((data & 0xFF0000000000L) >>> 40);
	    buffer[index]   = (byte)((data & 0xFF000000000000L) >>> 48);
	  }

	  
	  public static void set8Bytes(byte[] buffer, int index, long data)
	  {
	    buffer[index+7] = (byte) (data & 0xFF);
	    buffer[index+6] = (byte)((data & 0xFF00) >>> 8);
	    buffer[index+5] = (byte)((data & 0xFF0000) >>> 16);
	    buffer[index+4] = (byte)((data & 0xFF000000) >>> 24);
	    buffer[index+3] = (byte)((data & 0xFF00000000L) >>> 32);
	    buffer[index+2] = (byte)((data & 0xFF0000000000L) >>> 40);
	    buffer[index+1] = (byte)((data & 0xFF000000000000L) >>> 48);
	    buffer[index]   = (byte)((data & 0xFF00000000000000L) >>> 56);
	  }

	  public static int calculatePadding(int length)
	  {
	    return ( ( (length&3) != 0 ) ? ( 4-(length&3) ) : 0);
	  }

	  public static long obtainHopByHopId()
	  {
	    long time = System.currentTimeMillis();

	    return (time%10000);
	  }

	  public static long obtainEndToEndId(byte firstByte)
	  {
	    long e2eId = 0;
	    long time = System.currentTimeMillis();

	    e2eId |= ((firstByte & 0xFF) << 24);
	    e2eId |= (time & 0xFFFFFF);

	    return e2eId;
	  }

	  public static String toHexString(byte[] data, int start, int length)
	  {
	    final char[] DIGITS = {
	              '0', '1', '2', '3', '4', '5', '6', '7',
	              '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	    };
	    StringBuffer buffer = new StringBuffer();
	    for (int i=start; i < start+length; i++)
	    {
	      buffer.append(DIGITS[(0xF0 & data[i]) >>> 4 ]);
	      buffer.append(DIGITS[ 0x0F & data[i] ]);
	    }
	    return buffer.toString();
	  }
	  
	  public static String hexToAscii(String data) {
		  int n = data.length();
		  StringBuilder sb = new StringBuilder(n / 2);
		  for (int i = 0; i < n; i += 2) {
		    char a = data.charAt(i);
		    char b = data.charAt(i + 1);
		    sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
		  }
		  return sb.toString();
	  }
	  
	  public static int hexToInt(char ch) {
		  if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
		  if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
		  if ('0' <= ch && ch <= '9') { return ch - '0'; }
		  throw new IllegalArgumentException(String.valueOf(ch));
	  }

	  public static byte[] hexStringToByteArray(String s) {
	     int len = s.length();
	     byte[] data = new byte[len / 2];
	     for (int i = 0; i < len; i += 2) {
	         data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                              + Character.digit(s.charAt(i+1), 16));
	     }
	     return data;
	  }
	  
	  public static void printMessageBuffer(StringBuilder buffer, byte[] msg,
	                                        int start, int length)
	  {
	    final char[] DIGITS = {
	            '0', '1', '2', '3', '4', '5', '6', '7',
	            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	    };

	    buffer.append("Message Buffer Output :");
	    for (int i=start; i < start+length; i++)
	    {
	      if (i % 16 == 0)
	      {
	        buffer.append("\n");
	      }
	      else if (i % 4 == 0)
	      {
	        buffer.append(" ");
	      }

	      buffer.append(DIGITS[(0xF0 & msg[i]) >>> 4 ]);
	      buffer.append(DIGITS[ 0x0F & msg[i] ]);
	    }
	  }

	  public static byte[] dottedIpToBytes(String ipAddress)
	  {
	    byte[] addr = new byte[4];
	    StringTokenizer str = new StringTokenizer(ipAddress,".");

	    addr[0] = (byte)Integer.parseInt(str.nextToken());
	    addr[1] = (byte)Integer.parseInt(str.nextToken());
	    addr[2] = (byte)Integer.parseInt(str.nextToken());
	    addr[3] = (byte)Integer.parseInt(str.nextToken());

	    return addr;
	  }

//	@Override
//	public boolean isRequest() {
//		// TODO Auto-generated method stub
//		return false;
//	}

}
