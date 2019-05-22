package com.dparser;

import com.dparser.DiameterMessageData;

/*
 * To handle problems on Diameter message build
 */
public class DiameterBuildException extends DiameterException {
   // Super class requires this
   private static final long   serialVersionUID = 1L;

   // long errorReason = 0;

   /*
    * ========================================================================= 
    * Constructors for any situation
    * =========================================================================
    */
   public DiameterBuildException(long error, String message) {
      super(error, message, DiameterException.DIAMETER_BUILD_EXCEPTION);
   }

   public DiameterBuildException(long error, String message, Throwable cause) {
      super(error, message, cause, DiameterException.DIAMETER_BUILD_EXCEPTION);
   }

   public DiameterBuildException(String message, DiameterException exp) {
      super(exp.getErrorReason(), message + exp.getMessage(), exp, DiameterException.DIAMETER_BUILD_EXCEPTION);
   }

}