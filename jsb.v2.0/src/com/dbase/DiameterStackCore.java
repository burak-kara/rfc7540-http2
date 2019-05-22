package com.dbase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.base.ApplicationIF;
import com.base.Logger;
import com.base.Receiver;
//import com.base.Receiver;
import com.base.StackCore;
import com.base.Utility;

public class DiameterStackCore extends StackCore {

   public DiameterStackCore(String ipaddr, int listenport, ApplicationIF app) {
      super(ipaddr, listenport, app);
   }

   public void connectionAccepted(Socket sock) {
      this.connSocket = sock;
      this.localPort = sock.getLocalPort();
      this.remotePort = sock.getPort();
      this.localAddress = sock.getLocalAddress();
      this.remoteAddress = sock.getInetAddress();
   
      Logger.Info("DiameterStackCore::Connection is accepted: local-port=" + this.localPort + " local-addr=" +
                  this.localAddress + " rem-port=" + this.remotePort + " rem-addr=" + this.remoteAddress);
      if (this.application != null) {
         this.application.onConnectionSuccess(this.localAddressStr, this.listenPort, this.remoteAddress.getHostAddress(), this.remotePort);
      }
      //this.receiver = new Receiver(this.connSocket, this);
      this.receiver = new DiameterReceiver(this.connSocket, this);
      new Thread(this.receiver, "receiver").start();
   }

   /**
    * Accepts remote-address and port Return 0 if success
    * Return 0 if success
    */
   public int startWithAttempt(String remAddr, int remPort) {
      int result = CSR_SUCCESS;
      if (this.connected) {
         return result;
      }
      
      long start = 0;
      try {
         this.remoteAddress = InetAddress.getByName(remAddr);
         this.remotePort = remPort;
         start = System.currentTimeMillis();  
         
//         if (this.isSecure) {
//            // TODO: Key/trust stores needs to be initialized only once
//            // TODO code duplicated in Acceptor
//            Properties props = StackProperties.getInstance();
//            
//            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
//            SSLContext context = null;
//            TrustManagerFactory tmf = null;
//            KeyManagerFactory kmFactory = null;
//            KeyStore trustStore = null;
//            KeyStore keyStore=null;
//            
//            String trustStorePathValue=null;
//            String keyStorePathValue=null;
//            String keyStorePasswordPathValue=null;
//            
//            if (this.trustStorePath!=null){
//              trustStorePathValue=props.getProperty(this.trustStorePath);
//            }
//            
//            if (this.keyStorePath!=null){
//              keyStorePathValue=props.getProperty(this.keyStorePath);
//            }
//            
//            if (this.keyStorePasswordPath!=null){
//             keyStorePasswordPathValue=props.getProperty(this.keyStorePasswordPath);
//            }
//           
//            KeyManager[] km=null;
//            
////            try {
//               kmFactory = KeyManagerFactory.getInstance(algorithm);
//               tmf = TrustManagerFactory.getInstance(algorithm);
//               trustStore = KeyStore.getInstance("JKS");
//               keyStore= KeyStore.getInstance("JKS");
//               
//               if (trustStorePathValue!=null)
//               {
//                  trustStore.load(new FileInputStream(trustStorePathValue),null);
//                  tmf.init(trustStore);
//                  tmf.getTrustManagers();
//               }
//               if (keyStorePathValue!=null)
//               {
//                  keyStore.load(new FileInputStream(keyStorePathValue) , keyStorePasswordPathValue.toCharArray());
//               }
//               if (keyStorePasswordPathValue!=null)
//               {
//                  kmFactory.init(keyStore, keyStorePasswordPathValue.toCharArray());
//                  km=kmFactory.getKeyManagers();
//               }
//               SecureRandom secureRandom = new SecureRandom();
//               secureRandom.nextInt();
//                  
//               
//               TrustManager ingnorTM = new X509TrustManager() {
//                  
//                  @Override
//                  public X509Certificate[] getAcceptedIssuers() {
//                     // TODO Auto-generated method stub
//                     return null;
//                  }
//                  
//                  @Override
//                  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                     // TODO Auto-generated method stub
//                     
//                  }
//                  
//                  @Override
//                  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                     // TODO Auto-generated method stub
//                     
//                  }
//               };
//               context = SSLContext.getInstance("TLS");
//               context.init(km, new TrustManager[]{ingnorTM}, secureRandom);
//               
//               SSLSocketFactory sf = context.getSocketFactory();
//               SSLSocket s = (SSLSocket) sf.createSocket(this.remoteAddress, this.remotePort);
//               s.setEnabledCipherSuites(cipherSuites);
//               s.setEnabledProtocols(enabledProtocols);
//               
//               s.startHandshake();
//
//               this.connSocket = s;
////            }
////            catch (Exception ex) {
////               ex.printStackTrace();
////            }
//         }
//         else
//         {
            SocketAddress sockaddr = new InetSocketAddress(this.remoteAddress, this.remotePort); 
            SocketAddress sockaddr2 = new InetSocketAddress(this.localAddressStr, this.localPort); 
            // Creates an unconnected socket
            // TODO: can we specify local IP:port here?
            this.connSocket = new Socket(); 
            this.connSocket.bind(sockaddr2);
            this.connSocket.connect(sockaddr, this.connTimeout);
            
            this.workingMode = SC_WM_CLIENT;
            this.connected = true;
//         }
         
         this.localPort = this.connSocket.getLocalPort();

         /* TODO: invoke onConnect from the owner/application */
         //this.logger.Info("Connection is attempted successfuly: local-port=" + this.localPort + " local-addr=" +
         Logger.Verbose("Connection is attempted successfuly: local-port=" + this.localPort + " local-addr=" +
                        this.localAddress + " rem-port=" + this.remotePort + " rem-addr=" + this.remoteAddress);
         if (this.application != null) {
            this.application.onConnectionSuccess(this.localAddressStr, this.localPort, this.remoteAddress.getHostAddress(), this.remotePort);
         }
         //this.receiver = new Receiver(this.connSocket, this);
         this.receiver = new DiameterReceiver(this.connSocket, this);
         //new Thread(this.receiver, "receiver").start();
         new Thread(this.receiver, ("receiver-"+ Utility.rcvrCounter.getAndIncrement())).start();
      }
      catch(SocketTimeoutException sot) {
         Logger.Error("ConnTimeout: Time = " + (System.currentTimeMillis() - start));
         result = CSR_CONN_TIMEOUT;
      }
      catch (UnknownHostException e) {
         if (this.application != null) {
            this.application.onConnectionFail(this.remoteAddress.getHostAddress(), this.remotePort);
         }
         Logger.Excep("Unknown-host --> " + e);
         result = CSR_UNKNOWN_HOST;
      }
      catch (IOException e) {
         if (this.application != null) {
            this.application.onConnectionFail(this.remoteAddress.getHostAddress(), this.remotePort);
         }
         Logger.Excep("IO Problem --> " + e);
         result = CSR_IO_EXCEPTION;
      }
//      catch (NoSuchAlgorithmException e) {
//         /* triggered from SSLContext.getInstance("TLS"); */
//         if (this.application != null) {
//            this.application.onConnectionFail(this.remoteAddress.getHostAddress(), this.remotePort);
//         }
//         Logger.Excep("Secure Connection Problem --> " + e);
//         result = CSR_IO_EXCEPTION;
//      }
//      catch (KeyStoreException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//      catch (CertificateException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//      catch (UnrecoverableKeyException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
//      catch (KeyManagementException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
      return result;
   }

}
