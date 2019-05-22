Simple Base v2.0 - Java
========================
This version of software consists of Diameter protocol addition, which includes 
an imlementation of Diameter protocol message parsing and building utilities 
and a test routine. For Diameter protocol parsing and building utilities, refer 
to the source files in 'src/dparser'. For test implementation, refer to 
'src/dbase'

To run client and server programs from a terminal or command-line, the following
commands can be used from "bin" directory for client and server behaviors, 
respectively:

java com.dbase.DiaTestHead 1
java com.dbase.DiaTestHead

or, for Windows, the following "bat" files can be invoked:

dclient.bat
dserver.bat

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Simple Base v1.0 - Java
========================

This version of software supports only high-level parsing of Session Initiation
Protocol (SIP) while base software also supports few preliminary components,
such as:
  - StackCore
  - DatagramStackCore
  - Acceptor
  - Receiver
  - DatagramReceiver
  - EventQueue
  - Logger
  - GenericMessage
  - Timer Service

Worker threads are considered at application middleware.

Eclise IDE can be used by importing the project. 

Classes under "com.sip_client" implement a middleware for SIP client behavior
and ClientTestMain.java consists of the "main" for this.

Similarly, classes under "com.sip_server" implement a middleware for SIP client
behavior and ServerTestMain.java consists of the "main" for this.

Both are based on number-controlled menu structure, such that to invoke an item
you need to enter the corresponding number. Press 'Enter' to see the available 
menu items. 

In the case of a mutual test, the 'server' shall be started before the 'client'.

To be able independent of Internet connectivity, the current software 
components (client and server) communicates over local-loop (127.0.01).
This situation may be replaced with real IP addresses.

To run client and server programs from a terminal or command-line, the following
commands can be used from "bin" directory for client and server behaviors, 
respectively:

java com.sip_client.ClientTestMain
java com.sip_client.ServerTestMain

or, for Windows, the following "bat" files can be invoked:

clntsip.bat
srvsip.bat

-------

For any further question and comment: demiry@netas.com.tr
