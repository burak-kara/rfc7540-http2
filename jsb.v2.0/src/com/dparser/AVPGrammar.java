package com.dparser;

public class AVPGrammar {

	   /* String representation of AVP data types (from DiameterDefinitions) */
	   public static final String[] typeNames = {"UnknownType",
	                                             "OctetString",
	                                             "Integer32",
	                                             "Integer64",
	                                             "Unsigned32",
	                                             "Unsigned64",
	                                             "Float32",
	                                             "Float64",
	                                             "Grouped",
	                                             "Address",
	                                             "Time",
	                                             "UTF8String",
	                                             "DiameterIdentity",
	                                             "DiameterURI",
	                                             "Enumerated",
	                                             "IPFilterRule",
	                                             "QoSFilterRule"};
	                                   
	   protected String name;
	   protected int code;
	   protected int type;
	   protected byte flagsByte;
	   protected int vendorId;
	   /* The current subclass GroupedAVPGrammer does not need these */
	   int rangeMin; 
	   int rangeMax; /* last both applicable for Enumerated types and could be for Integer, Unsigned types */
	   
	   protected boolean forced;
	   
	   public AVPGrammar() {
	      this.name = "undefined";
	      this.code = 0;
	      this.type = 0;
	      this.flagsByte = 0;
	      this.vendorId = 0; /* assume base protocol by default */
	      this.rangeMin = Integer.MIN_VALUE; /* to be check if range-check is provided with assumption MIN_VALUE never used*/
	      this.rangeMax = Integer.MAX_VALUE;
	      this.forced = false;
	   }
	   
	   /*
	    *  setters
	    */
	   public int setName(String name) {
	      this.name = name;
	      return 0;
	   }
	   
	   public int setCode(String code) {
	      try {
	         this.code = Integer.valueOf(code);
	      }
	      catch (NumberFormatException ex) {
	         System.err.println("Code " + code + " is not a number..!");
	         return 1;
	      }
	      return 0;
	   }
	   
	   public int setType(String type) {
	      /* TODO: Could be a map for quick access */
	      int idx = -1;
	      for (int i=0; i<typeNames.length; i++) {
	         if (typeNames[i].equalsIgnoreCase(type)) {
	            idx = i;
	            break;
	         }
	      }
	      if (idx > 0) {
	         this.type = idx;
	      }
	      else if (idx == 0) {
	         System.err.println("Unexpected usage of Type 'UnknownType'");
	         return 1;
	      }
	      else {
	         System.err.println("Unsupported AVP data type : " + type);
	         return 1;
	      }
	      return 0;
	   }
	   
	   public int setForsed(boolean forced) {
	      this.forced = forced;
	      return 0;
	   }
	   
	   int parseFlagsForFirstThreeBits(String flagsValue)
	   {
	      byte flags = 0x00;

	      for (int i = 0; i < flagsValue.length(); ++i) {
	         if (flagsValue.charAt(i) == 'V') {
	            if ((flags & 0x80) == 0x80) {
	               System.err.println("You cannot use 'V' more than one in flags attribute.\n");
	               return 1;
	            } 
	            else {
	               flags |= 0x80;
	            }
	         } 
	         else if (flagsValue.charAt(i) == 'M') {
	            if ((flags & 0x40) == 0x40) {
	               System.err.println("You cannot use 'M' more than one in flags attribute.\n");
	               return 1;
	            } 
	            else {
	               flags |= 0x40;
	            }
	         } 
	         else if (flagsValue.charAt(i) == 'P') {
	            if ((flags & 0x20) == 0x20) {
	               System.err.println("You cannot use 'P' more than one in flags attribute.\n");
	               return 1;
	            } 
	            else {
	               flags |= 0x20;
	            }
	         } 
	         else {
	            System.err.println("You can only use 'V', 'M', 'P' characters if you do not choose to provide flags in binary.\n");
	            return 1;
	         }
	      }
	      this.flagsByte = flags;
	      return 0;
	   }

	   byte convertToBinaryNumberFromString(String flagsValue) {
	      byte flags = 0x00;
	      int length = flagsValue.length();
	      for (int i = length - 1; i >= 0; --i) {
	         if (flagsValue.charAt(i) == '1') {
	            flags |= 0x01 << ((length - 1) - i);
	         }
	      }
	      return flags;
	   }
	   
	   boolean isBinary(String flagsValue) {
	     for (int i = 0; i < flagsValue.length(); ++i) {
	        if ((flagsValue.charAt(i) != '0') && (flagsValue.charAt(i) != '1')) {
	           return false;
	        }
	     }
	     return true;
	   }
	   
	   public int setFlags(String flagsValue) {
	      if (flagsValue.length() == 8) {
	         if (isBinary(flagsValue)) {
	            this.flagsByte = convertToBinaryNumberFromString(flagsValue);
	         }
	         else {
	            System.err.println("\"flags\" attribute's value must contain only 0s and 1s if its length is 8.\n");
	            return 1;
	         }
	      }
	      else if (flagsValue.length() >= 0 && flagsValue.length() <= 3) {
	          return parseFlagsForFirstThreeBits(flagsValue);
	      }
	      else
	      {
	         System.err.println("The value of \"flags\" attribute can have a length between 0-3 inclusively, or 8. Can't be between 4-7 inclusively.\n");
	         return 1;
	      }

	      return 0;
	   }
	   
	   public int setVendorId(String vId) {
	      try {
	         this.vendorId = Integer.valueOf(vId);
	      }
	      catch (NumberFormatException ex) {
	         System.err.println("Vendor-Id " + vId + " is not a number..!");
	         return 1;
	      }
	      return 0;
	   }
	   
	   public int setRange(String range) {
	      String[] parts = range.split("-");
	      if (parts.length != 2) {
	         System.err.println("Range setting: min and max expected in form 'min-max', but received " + range);
	         return 1;
	      }
	      try {
	         this.rangeMin = Integer.valueOf(parts[0]);
	         this.rangeMax = Integer.valueOf(parts[1]);
	      }
	      catch (NumberFormatException ex) {
	         System.err.println("Range setting: Seems that range setting has some non-number parts --> " + range);
	         return 1;
	      }
	      return 0;
	   }
	   
	   /* 
	    * Getters 
	    */
	   public String getName() {
	      return this.name;
	   }
	   
	   public int getCode() {
	      return this.code;
	   }
	   
	   public int getType() {
	      return this.type;
	   }
	   
	   public byte getFlagsByte() {
	      return this.flagsByte;
	   }
	   
	   public int getVendorId() {
	      return this.vendorId;
	   }
	   
	   int getRangeMin() {
	      return this.rangeMin;
	   }
	   
	   public int getRangeMax() {
	      return this.rangeMax;
	   }
	   
	   public boolean isForced() {
	      return this.forced;
	   }
	}
