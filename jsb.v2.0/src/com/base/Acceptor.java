package com.base;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements listening job for connection oriented protocols such as TCP. It can complete the job after a connection established or
 * continue to listen for further connection attempts
 * 
 * @author demiry
 */
public class Acceptor implements Runnable {
   StackCore     core;

   /* exit after a connection establishment or not */
   boolean       exitOnComplete;

   /* address and port to listen */
   String        ipAddrStr;
   int           listenPort;

   AtomicBoolean stopWorking = new AtomicBoolean(false);

   ServerSocket  serverSocket;
//   boolean       isSecure=false;
//   private String[] cipherSuites = {
//                            "TLS_RSA_WITH_AES_128_CBC_SHA", // AES difficult to get with c++/Windows
//                            // "TLS_RSA_WITH_3DES_EDE_CBC_SHA", // Unsupported by Sun impl,
//                            "SSL_RSA_WITH_3DES_EDE_CBC_SHA", // For backwards comp., C++
//                            "TLS_DH_anon_WITH_AES_128_CBC_SHA",
//                            "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA"};
//   
//   private String[] enabledProtocols = {"SSLv3","SSLv2Hello","TLSv1"};
   
//   Logger logger;
   
   public Acceptor(String ipAddr, int lport, boolean exitAfter, StackCore core) {
      this.ipAddrStr = ipAddr;
      this.listenPort = lport;
      this.exitOnComplete = exitAfter;
      this.core = core;
//      this.isSecure=core.isSecure;
//      this.logger = logger;
   }
   
   /**
    * Binds the socket and returns actual local port
    * @return
    * @throws IOException 
    * @throws Exception
    */
   public int bind() throws IOException {
      InetAddress inetaddr = InetAddress.getByName(this.ipAddrStr);
     
//      if (this.isSecure) {
//         // TODO: Key/trust stores needs to be initialized only once
//         Properties props = StackProperties.getInstance();
//         
//         String algorithm = KeyManagerFactory.getDefaultAlgorithm();
//         KeyManagerFactory kmFactory = null;
//         TrustManagerFactory tmf = null;
//         KeyStore keystore = null;
//         KeyStore trustStore = null;
//         SSLContext context = null;
//         String trustStorePath=props.getProperty("javax.net.ssl.msrp.server.trustStore");
//         String keyStorePath=props.getProperty("javax.net.ssl.msrp.server.keyStore");
//         String keyStorePassword=props.getProperty("javax.net.ssl.msrp.server.keyStorePassword");
//         KeyManager[] km=null;
//         TrustManager[] tm=null;
//                     
//         try {
//            kmFactory = KeyManagerFactory.getInstance(algorithm);
//            keystore = KeyStore.getInstance("JKS");
//            tmf = TrustManagerFactory.getInstance(algorithm);
//            trustStore = KeyStore.getInstance("JKS");
//            
//            if(trustStorePath!=null)
//            {
//               trustStore.load(new FileInputStream(trustStorePath),null);
//               tmf.init(trustStore);
//               tm=tmf.getTrustManagers();
//            }
//            if(keyStorePath!=null)
//            {
//               keystore.load(new FileInputStream(keyStorePath),keyStorePassword.toCharArray());
//            }
//            if(keyStorePassword!=null)
//            {
//               kmFactory.init(keystore, keyStorePassword.toCharArray());
//               km=kmFactory.getKeyManagers();
//            }
//            
//            SecureRandom secureRandom = new SecureRandom();
//            secureRandom.nextInt();
//                 
//            context = SSLContext.getInstance("TLS");
//            context.init(km, tm, secureRandom);
//
//            this.serverSocket = context.getServerSocketFactory().createServerSocket(this.listenPort, 0, inetaddr);
//         }
//         catch (Exception e) {
//            e.printStackTrace();
//            this.logger.Excep("Exception on TLS connection..." + e);
//         }
//
//      }
//      else {
         this.serverSocket = new ServerSocket(this.listenPort, 0, inetaddr);
//      }

      return this.serverSocket.getLocalPort();
   }

   @Override
   public void run() {
      while (!this.stopWorking.get()) {
         try {
//            if (this.isSecure) {
//               SSLSocket sslSock = null;
//               sslSock = (SSLSocket) this.serverSocket.accept();
//               sslSock.setEnabledCipherSuites(cipherSuites);
//               sslSock.setEnabledProtocols(enabledProtocols);
//               core.connectionAccepted(sslSock);
//            }
//            else {
               Socket clientSocket = null;
               clientSocket = this.serverSocket.accept();
               core.connectionAccepted(clientSocket);
//            }
         }
         catch (IOException e) {
            if (this.stopWorking.get()) {
               Logger.Info("Acceptor Stopped.");
               return;
            }
            throw new RuntimeException("Error accepting client connection", e);
         }
      }
      Logger.Info("Acceptor Stopped.");

      try {
         this.serverSocket.close();
      }
      catch (IOException e) {
         throw new RuntimeException("Error closing acceptor socket", e);
      }
   }

   public synchronized void stop() {
      this.stopWorking.set(true);
      if (this.serverSocket != null) {
         try {
            this.serverSocket.close();
         }
         catch (IOException e) {
            Logger.Excep("IOException on Acceptor.stop..." + e);
         }
      }
   }
}
