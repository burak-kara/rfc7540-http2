
package com.base;

//import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
//import java.security.KeyManagementException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.Properties;
//
//import javax.net.ssl.KeyManager;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSocket;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.TrustManagerFactory;
//import javax.net.ssl.X509TrustManager;

/**
 * Implements a minimal structure to create and own a stack which is capable
 * sending and receiving messages through a socket. In the case of connection
 * oriented messaging Acceptor is used to implement server behavior such
 * that after a connection established, a Receiver is created to receive
 * messages from that socket.
 * 
 * Note that, there is a need to create stack structure for each connection 
 * relation.
 * 
 * @author demiry
 */
public class StackCore implements StackCoreIF {
   /* return codes from the stack */
   public static final int CSR_SUCCESS      = 0;
   public static final int CSR_CONN_TIMEOUT = 1;
   public static final int CSR_UNKNOWN_HOST = 2;
   public static final int CSR_IO_EXCEPTION = 3;
   public static final int CSR_SEC_CONN_ERR = 4;
   
   public static final String[] csrText = {"Success", "Connetion-Timeout",
                                           "Unknown-Host", "IO-Exception",
                                           "Secure-Connection-Error"};
   
   /* stack working mode */
   public static final int SC_WM_NONE   = 0;
   public static final int SC_WM_SERVER = 1;
   public static final int SC_WM_CLIENT = 2;
   
   public static final String[] wmText = {"None", "test.Server", "Client"};
   
   protected int workingMode = SC_WM_NONE;
   
   protected int connTimeout = 2000;
   
   /* local IP */
   protected String        localAddressStr;
   protected int           listenPort;

   protected Acceptor      acceptor;
   protected Receiver      receiver;

   /* after connection establishment */
   protected Socket        connSocket;
   protected int           localPort;
   protected int           remotePort;
   protected InetAddress   localAddress;
   protected InetAddress   remoteAddress;
//   protected boolean       isSecure = false;
   protected ApplicationIF application;
   
//   protected String trustStorePath=null;
//   protected String keyStorePath=null; 
//   protected String keyStorePasswordPath=null;
//   
   protected boolean connected = false;
   
   /* Logger creates dependency to upper layers (TAF) but
    * currently there is now other way to have a logger functionality
    */
//   protected Logger logger;
   
//   private String[] cipherSuites = {
//                            "TLS_RSA_WITH_AES_128_CBC_SHA", // AES difficult to get with c++/Windows
//                            // "TLS_RSA_WITH_3DES_EDE_CBC_SHA", // Unsupported by Sun impl,
//                            "SSL_RSA_WITH_3DES_EDE_CBC_SHA", // For backwards comp., C++
//                            "TLS_DH_anon_WITH_AES_128_CBC_SHA",
//                            "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", };
//   private String[] enabledProtocols = {"SSLv3","SSLv2Hello","TLSv1"};

   private StreamMessageParser smp;
   
   public StackCore(String ipaddr, int listenport, ApplicationIF app) {
      this.localAddressStr = ipaddr;
      this.listenPort = listenport;
      this.application = app;
//      this.isSecure = secure;
//      this.logger = logger;    
   }

   public void setConnTimeout(int timeout) {
      this.connTimeout = timeout;
   }
   
   public int getConnTimeout() {
      return this.connTimeout;
   }
   
   public void shutdown() {
      if (this.acceptor != null) {
         this.acceptor.stop();
      }
      if (this.receiver != null) {
         this.receiver.stop();
      }
   }

   public void startWithListening() throws IOException {
      this.acceptor = new Acceptor(this.localAddressStr, this.listenPort, true, this);
      this.localPort = this.acceptor.bind();
      this.workingMode = SC_WM_SERVER;
      new Thread(this.acceptor, "Acceptor").start();
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
//            	 trustStorePathValue=props.getProperty(this.trustStorePath);
//            }
//            
//            if (this.keyStorePath!=null){
//            	 keyStorePathValue=props.getProperty(this.keyStorePath);
//            }
//            
//            if (this.keyStorePasswordPath!=null){
//            	keyStorePasswordPathValue=props.getProperty(this.keyStorePasswordPath);
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
         this.receiver = new Receiver(this.connSocket, this);
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

   /*
    * called from application to send message through the established connection/socket
    * established connection/socket
    */
   public int sendMessage(byte[] msg, int len) {
      int result = 0;

      if (this.connSocket == null) {
         result = startWithAttempt(this.remoteAddress.getHostAddress(), this.remotePort);
         if (result != 0) {
            Logger.Error("Cannot connect to remote with address=" + this.remoteAddress.getHostAddress() + "port=" + this.remotePort);
         }
      }
      try {
         OutputStream output = this.connSocket.getOutputStream();
         output.write(msg);
         /* TODO: Call back onMessageSend from owner/application */
         if (this.application != null) {
            this.application.onSendMessage();
         }
      }
      catch (IOException e) {
         Logger.Excep("IO Problem: " + e);
      }
      return result;
   }

   /**************************************************************************/
   /*
    * Callbacks from Acceptor and Receiver
    */
   /* called from Acceptor in the case of a connection accepted */
   public void connectionAccepted(Socket sock) {
      this.connSocket = sock;
      this.localPort = sock.getLocalPort();
      this.remotePort = sock.getPort();
      this.localAddress = sock.getLocalAddress();
      this.remoteAddress = sock.getInetAddress();
   
      Logger.Info("Connection is accepted: local-port=" + this.localPort + " local-addr=" +
                  this.localAddress + " rem-port=" + this.remotePort + " rem-addr=" + this.remoteAddress);
      if (this.application != null) {
         this.application.onConnectionSuccess(this.localAddressStr, this.listenPort, this.remoteAddress.getHostAddress(), this.remotePort);
      }
      this.receiver = new Receiver(this.connSocket, this);
      new Thread(this.receiver, "receiver").start();
   }

   /* called from receiver */
   //public byte[] messageReceived(byte[] msgb, int length) {
   public byte[] messageReceived(byte[] msgb, int length, Socket socket) {
      this.application.onReceivedMessage(msgb, length, socket);
      
//      /* TODO: call back onReceivedMessage from owner/application */
//      if (smp == null) {
//         smp = new StreamMessageParser(4096, application);
//      }
//      byte[] clone = new byte[length];
//      System.arraycopy(msgb, 0, clone, 0, length);
//      if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
//         Logger.Debug("MsgLength---> " + length);
//         Logger.Debug("Msg:" + new String(clone, 0, length));
//      }
//      try {
//         /* "addBytes" will call-back the application when a complete
//            message received */
//         smp.addBytes(clone);
//      }
//      catch (IOException io) {
//         Logger.Excep("IO problem: " + io);
//      }
      if (Logger.getLogLevel() >= Logger.LOGLEVEL_DEBUG) {
         Logger.Debug("Return to RECEIVER...");
      }
      return null;
   }

   public boolean receiveMessage(Socket sock) {
      return this.application.receiveMessage(sock);
   }
   
   /* called from receiver */
   public void connectionDisconnected(Socket sock) {
      Logger.Debug("Connection disconnected");
      if (this.application != null) {
         this.application.onDisconnect(0);
      }
   }

   public boolean isConnected() {
      return this.connected;
   }
   
   @Override
   public void setRemoteIp(String remIP) {
      try {
         this.remoteAddress = InetAddress.getByName(remIP);
      }
      catch (UnknownHostException e) {
         Logger.Excep("Unknown host problem: " + e);
      }
   }

   @Override
   public String getRemoteIP() {
      return this.remoteAddress.getHostAddress();
   }

   @Override
   public void setRemotePort(int port) {
      this.remotePort = port;
   }

   @Override
   public int getRemotePort() {
      return this.remotePort;
   }

   public int getLocalPort() {
      return localPort;
   }

   public Socket getConnSocket() {
      return connSocket;
   }
}
