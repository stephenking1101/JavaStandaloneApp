Start the Java RMI registry:
set CLASSPATH=C:\TEMP\codetest\Test\target\classes
start rmiregistry

Start the server:
start java -classpath C:\TEMP\codetest\Test\target\classes -Djava.rmi.server.codebase=file:C:\TEMP\codetest\Test\target\classes/ com.hsbc.alm.test.rmi.Server

Run the client:
java  -classpath C:\TEMP\codetest\Test\target\classes com.hsbc.alm.test.rmi.Client