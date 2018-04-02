package mserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import commons.sendingObject;
import servers.ServersThread;

public class MetaServer implements Serializable {

	private static final long serialVersionUID = 1L;
	public static HashMap<String, Integer> servers = new HashMap<>();
	public static HashMap<String, Integer> clients = new HashMap<>();
	public static HashMap<String, Boolean> liveFlag = new HashMap<>();
	public static String selfName = null;
	public static ArrayList<sendingObject> list = new ArrayList<>();
	public static HashMap<String, ArrayList<sendingObject>> map = new HashMap<>();
	public static ConcurrentHashMap<String, Long> time = new ConcurrentHashMap<>();

	public static void main(String[] args) throws UnknownHostException, IOException {

		// TESTING PURPOSES:-----------------------------
		// sendingObject obj = new sendingObject();
		// obj.setFileName("file1");
		// obj.setChunkName("abc.txt");
		// obj.setServerName("dc01.utdallas.edu");
		// list.add(obj);
		// map.put("file1", list);
		// ----------------------------------------------

		String csvFile = "/home/010/j/ja/jas160630/AOS/jas160630_Proj2/connections.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		Scanner scan = new Scanner(System.in);
		Integer port = null;

		try {
			selfName = InetAddress.getLocalHost().getHostName();
			System.out.println("My Hostname is: " + selfName);
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] connections = line.split(cvsSplitBy);
				// get the port number to start listening on that port
				if (connections[1].equals(selfName))
					port = Integer.parseInt(connections[2]);
				System.out.println("After calling start method of MetaServerThread");
				// get the host name and port number from file to populate
				// hashmaps
				if (connections[0].equals("servers")) {
					servers.put(connections[1], Integer.parseInt(connections[2]));
					// liveFlag.put(connections[1], true);
				} else if (connections[0].equals("clients"))
					clients.put(connections[1], Integer.parseInt(connections[2]));
				else
					continue;
				System.out.println("keys in servers: "+servers.keySet()+" keys in clients: "+clients.keySet());
			}
			new Thread(new MetaServerThread(port)).start();
			new Thread(new CheckLiveness(45000)).start();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			br.close();
		}

		// ServerSocket serv = new ServerSocket(port);
		// try{
		// while(true){
		// ExecutorService thread = Executors.newSingleThreadExecutor();
		// Runnable mThreadRun = new MetaServerThread(serv.accept());
		// thread.execute(mThreadRun);
		// }
		// }catch(Exception e){
		// e.printStackTrace();
		// }

		// might have to delete the code below this command because
		// the above while(true) part will never allow the code to
		// come to this point
		while (true) {
			System.out.println("\nEnter Valid Commands: Enter EXIT to quit the program\n");
			String in = scan.nextLine();
			in = in.trim();
			String input[] = in.split(" ");
			switch (input[0].toUpperCase()) {

			case "SHOW_SERVERS":
				for (Map.Entry<String, Integer> entry : servers.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					StringBuilder sb = new StringBuilder();
					sb.append(key);
					sb.append(": ");
					sb.append(value);
					String peer = sb.toString();
					System.out.println(peer);
					sb.setLength(0);
				}
				break;

			case "SHOW_CLIENTS":
				for (Map.Entry<String, Integer> entry : clients.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					StringBuilder sb = new StringBuilder();
					sb.append(key);
					sb.append(": ");
					sb.append(value);
					String peer = sb.toString();
					System.out.println(peer);
					sb.setLength(0);
				}
				break;

			default:
				System.out.println("Please enter only Valid Cases! Thank You!");
				break;
			}
		}
	}
}
