import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

//import javax.mail.internet.NewsAddress;

public class Monitor {

	static Socket client_socket;
	static ServerSocket monitor_socket;
	static Socket master1_socket;
	static Socket master2_socket;
	static Socket master3_socket;
	static DataInputStream client_in;
	static DataOutputStream client_out;
	static DataInputStream master1_in;
	static DataOutputStream master1_out;
	static DataInputStream master2_in;
	static DataOutputStream master2_out;
	static DataInputStream master3_in;
	static DataOutputStream master3_out;
	static Socket[] Master_sockets = new Socket[3];
	static DataInputStream[] Master_inputstreams = new DataInputStream[3];
	static DataOutputStream[] Master_outputstreams = new DataOutputStream[3];
	static String master_info = "";
	static int min = Integer.MAX_VALUE;
	static int current_count = 0;
	static int prev_count = 0;
	static HashMap<String, Integer> MASTERS_HEALTH = new HashMap<>();

	static Masters[] masters = new Masters[3];

	public void open_client_connection() throws Exception {
		Clients client = new Clients();
		Thread t = new Thread(client);
		t.start();
	}

	public void open_Master() throws Exception {

		Master_sockets[0] = master1_socket;
		Master_sockets[1] = master2_socket;
		Master_sockets[2] = master3_socket;
		Thread[] t = new Thread[3];
		for (int i = 0; i < 3; i++) {
			Master_sockets[i] = monitor_socket.accept();
			// System.out.println(master_socket.getInetAddress().getHostAddress());
			Master_inputstreams[i] = new DataInputStream(Master_sockets[i].getInputStream());
			Master_outputstreams[i] = new DataOutputStream(Master_sockets[i].getOutputStream());
			System.out.println("Waiting for master to connect");
			String[] add = Master_inputstreams[i].readUTF().split(" ");
			MASTERS_HEALTH.put(add[0] + " " + add[1], 0);
			System.out.println("Connected to " + add[0] + " " + add[1]);
			masters[i] = new Masters(Master_outputstreams[i], Master_inputstreams[i], add[0], Integer.parseInt(add[1]),
					i);
			// masters[i] = new Masters(master_out, master_in, add[0], 7777);
			t[i] = new Thread(masters[i]);
			t[i].start();
		}
		System.out.println(MASTERS_HEALTH.toString());
	}

	public static void main(String[] args) throws Exception {
		// monitor_socket = new ServerSocket(Integer.parseInt(args[0]));

		monitor_socket = new ServerSocket(7777);

		Monitor monitor = new Monitor();
		System.out.println("Monitor started");
		monitor.open_Master();
		monitor.open_client_connection();
	}

	class Masters implements Runnable {

		DataOutputStream out;
		DataInputStream in;
		String IP;
		int Port;
		String msg = "";
		boolean received = false;
		int i;
		boolean while_flag = true;

		public Masters(DataOutputStream out, DataInputStream in, String IP, int Port, int i) {
			this.out = out;
			this.in = in;
			this.IP = IP;
			this.Port = Port;
			this.i = i;
		}

		public void spawn_new_master(int i) throws Exception {
			Master_sockets[i] = monitor_socket.accept();
			Master_inputstreams[i] = new DataInputStream(Master_sockets[i].getInputStream());
			Master_outputstreams[i] = new DataOutputStream(Master_sockets[i].getOutputStream());
			this.in = Master_inputstreams[i];
			this.out = Master_outputstreams[i];
			System.out.println("Spawned!");
		}

		public void receive_heartbeat() throws Exception {

			int load;
			if (received == false) {
				try {
					load = Integer.parseInt(in.readUTF());
					// received = true;
					System.out.println("Received load from " + IP + " " + Port + " " + load);
					MASTERS_HEALTH.put(IP + " " + Port, load);
				} catch (Exception e) {
					System.out.println("Did not receive heartbeat from " + IP + " " + Port);
//					SendEmail sendEmail=new SendEmail();
//					sendEmail.send("SYSTEM ALERT!!! - Master Server "+IP+" Down","This is an Alert to Notify "
//							+ "the Following Failure- "
//							+ "\nFailure: Master Server Down\nIP: "+IP
//							+ "\nPORT: "+Port
//							+ "\nFailure Time: "+System.currentTimeMillis()+"milliseconds"
//							+ "\nRestart the Master Server to Resolve this issue!");
					System.out.println("Spawning new Master");
					Master_sockets[i].close();
					received = true;
					while_flag = false;
					String command = "#!/bin/sh";
					String command1 = "javac MasterServer.java";
					String command2 = "java MasterServer";
					Runtime.getRuntime().exec(command);
					Runtime.getRuntime().exec(command1);
					Runtime.getRuntime().exec(command2);
					MASTERS_HEALTH.remove(IP+" " + Port);
					System.out.println("New master has been spawned");
					Master_sockets[i] = monitor_socket.accept();

					// System.out.println(master_socket.getInetAddress().getHostAddress());
					Master_inputstreams[i] = new DataInputStream(Master_sockets[i].getInputStream());
					Master_outputstreams[i] = new DataOutputStream(Master_sockets[i].getOutputStream());
					System.out.println("Waiting for master to connect");
					String[] add = Master_inputstreams[i].readUTF().split(" ");
					MASTERS_HEALTH.put(add[0] + " " + add[1], 0);
					System.out.println("Connected to " + add[0] + " " + add[1]);
					masters[i] = new Masters(Master_outputstreams[i], Master_inputstreams[i], add[0],
							Integer.parseInt(add[1]), i);
					
				}
			}
		}

		@Override
		public void run() {
			while (while_flag) {
				try {
					Thread.sleep(3000);
					receive_heartbeat();
				} catch (Exception e) {
				}

			}
		}

	}

	class Clients implements Runnable {

		public Clients() {

		}

		public void run() {
			// int i = 0;
			while (true) {
				try {
					// Thread.sleep(5000);
					client_socket = monitor_socket.accept();
					client_out = new DataOutputStream(client_socket.getOutputStream());
					client_in = new DataInputStream(client_socket.getInputStream());
					Client_Connection C = new Client_Connection(client_out, client_in);
					Thread clients = new Thread(C);
					clients.start();
					System.out.println("Clients started");
				} catch (Exception e) {
					e.printStackTrace();
				}
				// i = 1;
			}
		}
	}

	class Client_Connection implements Runnable {

		DataOutputStream out;
		DataInputStream in;
		Client_Connection[] user = new Client_Connection[4];
		String minload_master;
		int minload = Integer.MAX_VALUE;

		public Client_Connection(DataOutputStream out, DataInputStream in) {
			this.out = out;
			this.in = in;
		}

		@Override
		public void run() {
			try {
				if (client_in.readUTF().equals("Request Connection to Master")) {
					for (String key : MASTERS_HEALTH.keySet()) {
						if (minload >= MASTERS_HEALTH.get(key)) {
							minload_master = key;
							minload = MASTERS_HEALTH.get(key);
						}
					}
					out.writeUTF(minload_master);
					System.out.println("Sent master info to client of master " + minload_master);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
