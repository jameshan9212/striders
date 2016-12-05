/* EchoServer is a threaded RFCOMM service
   with the specified UUID and name. When a client
   connects, it's input is echoed back in uppercase until
   it sends "bye$". 
*/

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


public class EchoServer
{
	
  // UUID and name of the echo service
  private static final String UUID_STRING = "0000110100001000800000805F9B34FB";
       // 32 hex digits which will become a 128 bit ID
  private static final String SERVICE_NAME = "strider";   // use lowercase
  private static final String PROFILE_NAME = "strider_profile";   // use lowercase
  private static final String DEFAULT_PARAMS= "[NULL,NULL,NULL]";   // use lowercase
  
  private static final int MESSAGE_DEFAULT= 0;   // use lowercase
  private static final int MESSAGE_IDENTIFY= 1;   // use lowercase
  private static final int MESSAGE_SUCCESS= 2;   // use lowercase
  private static final int MESSAGE_FAILIURE= 3;   // use lowercase
  private static final int MESSAGE_PENDING= 4;   // use lowercase
  
  private StreamConnectionNotifier server;
  private ArrayList<ThreadedEchoHandler> handlers; 
  private ArrayList<String> profile_id;
  private int profile_index = 0;
  private volatile boolean isRunning = false;

  static init output = new init();
  
  public EchoServer() throws InterruptedException
  {
	  handlers = new ArrayList<ThreadedEchoHandler>();
	  profile_id = new ArrayList<String>();
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	      public void run() {  
	    	  closeDown(); 
	    	  }
	    });
	    initDevice();
	    createRFCOMMConnection();
	    processClients();
  }  	// end of EchoServer()
  
  private void initDevice()
  {
    try {  // make the server's device discoverable
      LocalDevice local = LocalDevice.getLocalDevice();
      System.out.println("Device name: " + local.getFriendlyName());
      System.out.println("Bluetooth Address: " + local.getBluetoothAddress());
      boolean res = local.setDiscoverable(DiscoveryAgent.GIAC);
      System.out.println("Discoverability set: " + res);
      
      output.outputState("Device name: " + local.getFriendlyName());
      output.outputState("Bluetooth Address: " + local.getBluetoothAddress());
      output.outputState("Discoverability set: " + res);
    }
    catch (BluetoothStateException e) {
      System.out.println(e);
      output.outputState(e.toString());
      System.exit(1);
    }
  }  // end of initDevice()



  private void createRFCOMMConnection()
  /* Create a RFCOMM connection notifier for the server, with 
     the given UUID and name. This also creates a service record. */
  {
    try {  
      System.out.println("Start advertising " + SERVICE_NAME + "...");
      output.outputState("\r\r\nStart advertising " + SERVICE_NAME + "...");
      server = (StreamConnectionNotifier) Connector.open(
                  "btspp://localhost:" + UUID_STRING + 
                  ";name=" + SERVICE_NAME + ";authenticate=false");
      /* for most devices, with authenticate=false there 
         should be no need for pin pairing */
    }
    catch (IOException e) {
      System.out.println(e);
      output.outputState(e.toString());
      System.exit(1);
    }
  }  // end of createRFCOMMConnection()



  private void processClients() throws InterruptedException
  // Wait for client connections, creating a handler for each one
  {
    isRunning = true;
    try {
      while (isRunning) {

        System.out.println("Waiting for incoming connection...");
        output.outputState("Waiting for incoming connection...");
        
        // wait for a client connection
        StreamConnection conn = server.acceptAndOpen(); 

        
        /* acceptAndOpen() also adds the service record to the 
           device's SDDB, making the service visible to clients */
        System.out.println("Connection requested...");
        output.outputState("Connection requested...");

        ThreadedEchoHandler hand = new ThreadedEchoHandler(conn);

        
        // create client handler
        handlers.add(hand);
        hand.start();
        
        /*
         * accept new device and make profile using 
         * search history
         * get and append id in database file	[id,	index,	parameter]
         * id to index
         */
        String profile_id_new = RemoteDevice.getRemoteDevice(conn).getBluetoothAddress();
        
        profile_id.add(profile_id_new);
        BufferedReader in = null;
        boolean profile_flag = true;
        String workingDir = System.getProperty("user.dir");
        
        System.out.printf("Loading device profile...\n");
        
        // search history
        try {
        	in = new BufferedReader(new FileReader(PROFILE_NAME));
        	
        } catch(FileNotFoundException e) {
        // Make new profile
        	System.out.printf("Found new device %s\n", profile_id_new);
        	profile_flag = false;
        	FileWriter fw = new FileWriter(workingDir + "\\" + PROFILE_NAME);
        	String profile_id_statement = profile_id + "`" + profile_index + "`" + DEFAULT_PARAMS;
        	// broadcast profile to client and strider_profile file
        	handlers.get(profile_index).writeData(profile_id_statement, MESSAGE_IDENTIFY);
			fw.append(profile_id_statement + System.getProperty("line.separator"));
			profile_index++;
			System.out.printf("Made profile of %s\n", profile_id_new);
	        fw.close();	        
        }
        
        // Load profile
        if(profile_flag) {
        	String s;
	        while ((s = in.readLine()) != null) {
	        	if(s.contains(profile_id_new)) {
	        		profile_flag = true;
	        		handlers.get(profile_index).writeData(s, MESSAGE_IDENTIFY);
	        		String subProfile[] = s.split("`");
	        		System.out.printf("Identified device %s as %s\n",subProfile[0], subProfile[1]);
	        		break;
	        	}
	        	profile_index += 1;
	        }
	        in.close();
        }
      }
    }
    catch (IOException e) {  
    	System.out.println(e);
    	output.outputState(e.toString());
    }
  }  // end of processClients()



  public void closeDown()
  /* Stop accepting any further client connections, and close down
     all the handlers. */
  {
    System.out.println("Closing down server");
    output.outputState("Closing down server");
    if (isRunning) {
      isRunning = false;
      try {
        server.close();  
           // close connection, and remove service record from SDDB
      }
      catch (IOException e) {
    	  System.out.println(e);  
    	  output.outputState(e.toString());
    	  }

      // close down all the handlers
      for (ThreadedEchoHandler hand : handlers)
         hand.closeDown();
      handlers.clear();
    }
  }  // end of closeDown();


  public static void main(String args[]) throws InterruptedException
  { 
			EchoServer start = new EchoServer();		
  }
} 
