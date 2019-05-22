package com.base;

public class Logger {
   public static final int LOGLEVEL_EXCEP   = 0;
   public static final int LOGLEVEL_ERROR   = 1;
   public static final int LOGLEVEL_WARN    = 2;
   public static final int LOGLEVEL_INFO    = 3;
   public static final int LOGLEVEL_DEBUG   = 4;
   public static final int LOGLEVEL_VERBOSE = 5;
   
   static int logLevel;
   
   public static void setLevel(int level) {
      logLevel = level;
   }
   
   public static int getLogLevel() {
      return logLevel;
   }

   public static void Verbose(String data) {
      if (logLevel >= LOGLEVEL_VERBOSE) {
         System.out.println("[VERBOSE]" + /* TODO: add date&time */ ": " + data);
      }
   }

   public static void Debug(String data) {
      if (logLevel >= LOGLEVEL_DEBUG) {
         System.out.println("[DEBUG]" + /* TODO: add date&time */ ": " + data);
      }
   }
   
   public static void Info(String data) {
      if (logLevel >= LOGLEVEL_INFO) {
         System.out.println("[INFO]" + /* TODO: add date&time */ ": " + data);
      }
   }

   public static void Warn(String data) {
      if (logLevel >= LOGLEVEL_WARN) {
         System.out.println("[WARN]" + /* TODO: add date&time */ ": " + data);
      }
   }
   
   public static void Error(String data) {
      if (logLevel >= LOGLEVEL_ERROR) {
         System.err.println("[ERROR]" + /* TODO: add date&time */ ": " + data);
      }
   }

   public static void Excep(String data) {
      if (logLevel >= LOGLEVEL_EXCEP) {
         System.err.println("[EXCEP]" + /* TODO: add date&time */ ": " + data);
      }
   }
}
