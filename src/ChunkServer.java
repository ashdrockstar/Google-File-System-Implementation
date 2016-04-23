import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChunkServer {

	static Object clientLock=new Object();
	static Object masterLock=new Object();
	public static void main(String[] args) throws UnknownHostException, IOException {
		// MasterSever connection IP and Port
		System.out.println("Connection Stage 1");
		Socket socket=new Socket(args[2],5000);
		socket.close();

		System.out.println("Ready to accept request from Master & Clients");

		Thread clientThread=new Thread()
		{
			clientHelper clienthelper=new clientHelper(Integer.parseInt(args[1]));
			public void run() {
				try {
					clienthelper.helpClient();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};

		};
		clientThread.start();

		Thread masterThread=new Thread()
		{
			masterHelper masterhelper=new masterHelper(Integer.parseInt(args[0]));
			public void run() {
				try {
					masterhelper.helpMaster();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};

		};
		masterThread.start();
	}


	public static class masterHelper
	{
		// Master Connection Port
		static ServerSocket masterServerSocket;
		static Socket masterSocket;
		StringBuffer strBuff=new StringBuffer();
		public masterHelper(int masterPort) throws IOException {
			masterServerSocket=new ServerSocket(masterPort);
		}

		public void helpMaster() throws IOException
		{
			while(true)
			{
				System.out.println("Connection Stage 2");
				masterSocket=masterServerSocket.accept();
				System.out.println("Connected to Master:"+ masterSocket.getInetAddress().getHostAddress());
				synchronized (masterLock) {
					try {
						DataOutputStream masterOut=new DataOutputStream(masterSocket.getOutputStream());
						DataInputStream masterIn=new DataInputStream(masterSocket.getInputStream());
						if(masterIn.readUTF().equalsIgnoreCase("Make chunk"))
						{
							System.out.println("Making chunk");
							//FileOutputStream fout=new FileOutputStream(new File(masterIn.readUTF()+".txt"));
							BufferedWriter out = new BufferedWriter(new FileWriter(new File(masterIn.readUTF()+".txt")));
							strBuff.append(masterIn.readUTF());
							System.out.println(strBuff);
							out.write(strBuff.toString());
							masterIn.readUTF();
							//fout.write(strBuff.toString().getBytes());
						}
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		}
	}

	public static class clientHelper
	{
		// Client Connection Port
		static ServerSocket serverSocket;
		static Socket clientSocket;
		public clientHelper(int clientPort) throws IOException {
			serverSocket=new ServerSocket(clientPort);
		}

		public void helpClient() throws IOException
		{
			while(true)
			{
				System.out.println("Connection Stage 3");
				clientSocket=serverSocket.accept();
				System.out.println("Connected to Client:"+ clientSocket.getInetAddress().getHostAddress());
				synchronized (clientLock) {
					DataOutputStream clientOut=new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream clientIn=new DataInputStream(clientSocket.getInputStream());
					String[] str=new String[3];
					str=(clientIn.readUTF()).split(" ");
					// change it when chunks  get generated
					BufferedReader buffReader=new BufferedReader(new FileReader(str[0]+str[2]+".txt"));
					int ctrLine=0;
					int lineNo=Integer.parseInt(str[1]);
					switch(Integer.parseInt(str[2]))
					{
					case 1:
						lineNo=Integer.parseInt(str[1]);
						break;
					case 2:
						lineNo=Integer.parseInt(str[1])-800;
						break;
					case 3:
						lineNo=Integer.parseInt(str[1])-1600;
						break;
					case 4:
						lineNo=Integer.parseInt(str[1])-2400;
						break;
					case 5:
						lineNo=Integer.parseInt(str[1])-3200;
						break;
					case 6:
						lineNo=Integer.parseInt(str[1])-4000;
						break;
					}
					while(ctrLine<(lineNo-1))
					{
						ctrLine++;
						buffReader.readLine();
					}
					clientOut.writeUTF(buffReader.readLine());
				}
			}
		}
	}
}

