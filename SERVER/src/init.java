import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.ImageIcon;

public class init extends Frame {
	
	public static String PROFILE_NAME = "";
	private static boolean btn_clicked = false;
	private static final int MESSAGE_DEFAULT= 0;   // use lowercase
	  private static final int MESSAGE_IDENTIFY= 1;   // use lowercase
	  private static final int MESSAGE_SUCCESS= 2;   // use lowercase
	  private static final int MESSAGE_FAILIURE= 3;   // use lowercase
	  private static final int MESSAGE_PENDING= 4;   // use lowercase
	
	 static GridBagLayout gBag;
	 static TextArea tx = new TextArea();
	 static TextArea tx2 = new TextArea();
	 static Button bt1 = new Button("SEND");
	 private static Frame frame = new Frame("Strider");
	 static TrayIcon trayIcon = null;
	 static ImageIcon img = new ImageIcon("StrideCon.gif");
		
	public static OutputStream out;	 
	 
	 public init() {
		 init_set();
	 }

	 public static void init_set(){
		 WindowHandler listener = new WindowHandler();
		 gBag = new GridBagLayout(); 
		 
		 gbinsert(tx,0,0,2,1,200,300);
		 gbinsert(tx2,0,1,1,1,25,200);
		 //gbinsert(tx2,0,2,1,1,50,200);
		 gbinsert(bt1,0,2,1,1,50,100);
		 frame.setIconImage(img.getImage());
		
		 
		 bt1.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					
					if(!btn_clicked && e.getActionCommand().equals("SEND")) {
						try {
							inputState();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
					btn_clicked = !btn_clicked;
				}	 
			 });
		 
		 frame.setLayout(gBag);
		 frame.setBackground(Color.red);
		 frame.addWindowListener(listener);
		 frame.setResizable(false);
		 frame.setSize(300,500);
		 frame.setVisible(true);
		 
		 screenInit(frame);
	 }
	 
	 public static void outputState(String st){
		 tx.append(st+"\r\n");
	 }
	 
	 public static void inputState() throws IOException{
		 String msg = tx2.getText();
		 tx2.setText("");
		 
		 if(msg.equals("S"))
				writeData(msg, MESSAGE_SUCCESS);
		else if (msg.equals("F"))
				writeData(msg, MESSAGE_FAILIURE);
		else if (msg.equals("P")) 
				writeData(msg, MESSAGE_PENDING);
		else if (msg.startsWith("EXECUTE")) {
			String chk = Character.toString(msg.charAt(8));
			// EXECUTE docker
			String[] execute = {"Striderpy.exe", PROFILE_NAME, chk};
			Runtime.getRuntime().exec(execute);


		} else
			writeData(msg, MESSAGE_DEFAULT);
	 }
	 
	 public static void gbinsert(Component c, int x, int y, int w, int h, int wx, int wy){
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.fill= GridBagConstraints.BOTH;
	        gbc.gridx = x;
	        gbc.gridy = y;
	        gbc.gridwidth = w;
	        gbc.gridheight = h;
	        gbc.weightx =wx;
	        gbc.weighty =wy;
	        gBag.setConstraints(c,gbc);
	        frame.add(c);
	    }
	 
	 public static void screenInit(Frame frame){
		 Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		 
		 int xpos=(int)(screen.getWidth()/2)-frame.getWidth()/2;
		 int ypos=(int)(screen.getHeight()/2)-frame.getHeight()/2;
		 
		 frame.setLocation(xpos,ypos);
	 }
	 
	 
	 public static void writeData(String msg, int type) throws IOException
		/*
		 * Read a message in the form "<length> msg". The length allows us to know
		 * exactly how many bytes to read to get the complete message. Only the
		 * message part (msg) is returned, or null if there's been a problem.
		 */
		{		
		 	if(out == null)
		 		outputState("NULL CONNECTION");
		 	
			msg = type + "`" + msg;
			byte[] data = msg.getBytes();
			
			try {
				out.write(data);
			} catch (IOException e) {
				outputState("writeDate(): " + e);
			}
		}
	 
}

class WindowHandler implements ActionListener, WindowListener{

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(300);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Runtime.getRuntime().exec("taskkill /F /IM Strider.exe");
			Runtime.getRuntime().exec("taskkill /F /IM javaw.exe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.exit(0);		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("windows closing");
		/*try {
			Thread.sleep(300);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		try {
			Runtime.getRuntime().exec("taskkill /F /IM Stirder.exe");
			Runtime.getRuntime().exec("taskkill /F /IM javaw.exe");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
		
}

class Action implements ActionListener{
	public void actionPerformed(ActionEvent e){
		System.out.println("getSource()"+e.getSource());
		System.out.println("toString()"+e.toString());
		System.out.println("getID()"+e.getID());
		System.out.println("paramString()"+e.paramString());
	}
}



