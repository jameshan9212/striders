// ThreadedEchoHandler.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, February 2011

/* A threaded handler, called by EchoServer to deal with a client.
 When a message comes in, it is sent back converted to uppercase.
 closeDown() terminates the handler.
 ThreadedEchoHandler uses the same readData() and sendMessage()
 methods as EchoClient.
 */

import java.awt.AWTException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

public class ThreadedEchoHandler extends Thread {
	
	private String STRIDER_FILE_NAME = "wave.txt";
	private StreamConnection conn; // client connection
	private static InputStream in;
	private static OutputStream out;

	private volatile boolean isRunning = false;
	private String clientName; 
	
	static init output = new init();
		
	public ThreadedEchoHandler(StreamConnection conn) throws IOException {

		this.conn = conn;

		// Get an InputStream and OutputStream from the stream connection
		try {
			in = conn.openInputStream();
			out = conn.openOutputStream();
			
			// pass connection to view
			output.out = out;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			output.outputState(e.toString());
		}
		
		// store the name of the connected client
		clientName = reportDeviceName(conn);
		
		STRIDER_FILE_NAME = RemoteDevice.getRemoteDevice(conn).getBluetoothAddress();
		output.PROFILE_NAME = STRIDER_FILE_NAME;
		
		System.out.println("  Handler spawned for client: " + clientName);
		output.outputState("  Handler spawned for client: " + clientName);
	} // end of ThreadedEchoHandler()

	private String reportDeviceName(StreamConnection conn)
	/*
	 * Return the 'friendly' name of the device being examined, or "device ??"
	 */
	{
		String devName;
		try {
			RemoteDevice rd = RemoteDevice.getRemoteDevice(conn);
			devName = rd.getFriendlyName(false); // to reduce connections
		} catch (IOException e) {
			devName = "device ??";
		}
		return devName;
	}
	
	
	// start processing client messages. 
	public void run() {
		try {	
			
			processMsgs();

			System.out.println("  Closing " + clientName + " connection");
			output.outputState("  Closing " + clientName + " connection");
			
			if (conn != null) {
				in.close();
				out.close();
				conn.close();
			}
		} catch (IOException e) {
			
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processMsgs() throws AWTException, IOException {
		/*
		 * When a client message comes in, echo it back in uppercase. If the
		 * client sends "bye$$", or there's a problem, then terminate
		 * processing, and the handler.
		 */
		
		isRunning = true;
		String line = null;
		
		FileWriter fw = new FileWriter(STRIDER_FILE_NAME);
		
		while (isRunning) {
			
			// System.out.println("isRunning entered");
			
			if ((line = readData()) == null)
				isRunning = false;
			
			// there was some input
			else {

				// make line to delimite 000000X	F000000, acc packets
				if(line.contains("X") && line.contains("F"))
				{
					String subline_X[] = line.split("X");
					line = subline_X[1];
					
					String subline_F[] = line.split("F");
					line = subline_F[0];
					
					String submotion[] = line.split("D");
					
					// [acc_x] [acc_y] [acc_z] packing
					System.out.println("ACC : " + submotion[0] + ", " + submotion[1] + ", " + submotion[2]);
					fw.write(submotion[0]+ " " + submotion[1]+ " " + submotion[2] + System.getProperty("line.separator"));
				}
				
			}
		}
		fw.close();
	} 
	
	public void closeDown() {
		isRunning = false;
	}

	// --------------- IO methods ---------------------------
	// Same as the methods in EchoClient

	private String readData()
	/*
	 * Read a message in the form "<length> msg". The length allows us to know
	 * exactly how many bytes to read to get the complete message. Only the
	 * message part (msg) is returned, or null if there's been a problem.
	 */
	{
		byte[] data = null;

		try {

			int len = in.read(); // get the message length
			if (len <= 0) {
				System.out.println(clientName + ": Message Length Error");
				output.outputState(clientName + ": Message Length Error");
				return null;
			}

			data = new byte[50];
			len = 0;
			// read the message, perhaps requiring several read() calls
			while (len != data.length) {
				int ch = in.read(data, len, data.length - len);

				if (ch == -1) {
					System.out.println(clientName + ": Message Read Error");
					output.outputState(clientName + ": Message Read Error");
					return null;
				}
				len += ch;
			}
		} catch (IOException e) {
			System.out.println(clientName + " readData(): " + e);
			output.outputState(clientName + " readDate(): " + e);
			return null;
		}

		return new String(data).trim(); // convert byte[] to trimmed String

	}
	
	public void writeData(String msg, int type)
	/*
	 * Read a message in the form "<length> msg". The length allows us to know
	 * exactly how many bytes to read to get the complete message. Only the
	 * message part (msg) is returned, or null if there's been a problem.
	 */
	{		
		msg = type + "`" + msg;
		byte[] data = msg.getBytes();
		
		try {
			out.write(data);
		} catch (IOException e) {
			System.out.println(clientName + " writeData(): " + e);
			output.outputState(clientName + " writeDate(): " + e);
		}
	}

	public String getOnlyNumberString(String str) {
		if (str == null)
			return str;

		StringBuffer sb = new StringBuffer();
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char curChar = str.charAt(i);
			if (Character.isDigit(curChar))
				sb.append(curChar);
		}

		return sb.toString();
	}
}