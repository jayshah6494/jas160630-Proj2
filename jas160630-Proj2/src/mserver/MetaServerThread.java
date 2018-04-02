package mserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import commons.sendingObject;

public class MetaServerThread implements Runnable {
	private static final int CHUNK_SIZE = 8192;
	private ServerSocket serverSocket;
	private Integer port = null;

	public MetaServerThread(Integer port) {
		this.port = port;
	}

	public void run() {
		try {
			ServerSocket serv = new ServerSocket(port);
			while (true) {
				new Thread(new MultiServer(serv.accept())).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class MultiServer extends Thread implements Serializable {
		private static final long serialVersionUID = 1L;
		// private MetaServer ms = new MetaServer();
		private Socket clientSocket;
		private OutputStream outStream;
		private ObjectOutputStream objOutStream;
		private InputStream inStream;
		private ObjectInputStream objInStream;
		static sendingObject sO = new sendingObject();
		static HashMap<String, ArrayList<sendingObject>> mapObject = new HashMap<>();
		String hostname = null;

		public MultiServer(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				hostname = clientSocket.getInetAddress().getHostName();
				System.out.println(hostname);
				outStream = clientSocket.getOutputStream();
				objOutStream = new ObjectOutputStream(outStream);
				objOutStream.flush();
				inStream = clientSocket.getInputStream();
				objInStream = new ObjectInputStream(inStream);
				Object obj = objInStream.readObject();

				if (obj instanceof sendingObject) {
					System.out.println("Object received of type: sendingObject");
					sO = (sendingObject) obj;
					System.out.println("Type is: " + sO.getType() + " Filename is: " + sO.getFileName());
					String type = sO.getType().toUpperCase();
					switch (type) {

					case "READ":
						System.out.println("\nInside " + type + " case");
						if (!MetaServer.map.containsKey(sO.getFileName())) {
							System.out.println("File " + sO.getFileName() + " does not exist");
							sO.setMessage("File " + sO.getFileName() + " does not exist");
							objOutStream.writeObject(sO);
						} else {
							int chunkNo = sO.getReadBytesFrom() / CHUNK_SIZE;
							sendingObject sONew = new sendingObject();
							try {
								// sendingObject OBJECT which needs to be
								// accessed by metaserver to get
								// server name, chunkNo/chunkName,
								String servername = MetaServer.map.get(sO.getFileName()).get(chunkNo).getServerName();
								if (!MetaServer.liveFlag.get(servername)) {
									sO.setMessage("Server is Down, Cannot Access file");
									objOutStream.writeObject(sO);
								} else {
									System.out.println("The chunk name to be accessed is "
											+ MetaServer.map.get(sO.getFileName()).get(chunkNo).getChunkName());
									sONew.setChunkName(
											MetaServer.map.get(sO.getFileName()).get(chunkNo).getChunkName());
									sONew.setServerName(servername);
									sONew.setUpdatedReadBytesFrom(sO.getReadBytesFrom() % CHUNK_SIZE);
									objOutStream.writeObject(sONew);
								}
							} catch (Exception e) {
								System.out.println("Chunk not found while accessing the read.");
							}
						}
						break;

					case "APPEND":
						System.out.println("\nInside " + type + " case");
						if (!MetaServer.map.containsKey(sO.getFileName())) {
							System.out.println("File " + sO.getFileName() + " does not exist");
							System.out
									.println("Create a new file and chunk for the requested file: " + sO.getFileName());
							String servername = selectServer();
							ArrayList<sendingObject> list = new ArrayList<>();
							sendingObject sOCreate = new sendingObject();
							create(list, sOCreate, sO, servername);

							makeConnection(sOCreate);

							objOutStream.writeObject(sOCreate);
						} else {
							sendingObject sONew = new sendingObject();
							int listSize = MetaServer.map.get(sO.getFileName()).size();
							System.out.println(sO.getWriteNumOfBytes());
							int size = CHUNK_SIZE
									- MetaServer.map.get(sO.getFileName()).get(listSize - 1).getFileSize();
							if (sO.getWriteNumOfBytes() > size) {
								// Sending the object to server containting
								// last
								// chunk of the file
								// Integer size = CHUNK_SIZE
								// -
								// MetaServer.map.get(sO.getFileName()).get(listSize
								// - 1).getFileSize();
								sONew.setType("append");
								sONew.setChunkName(
										MetaServer.map.get(sO.getFileName()).get(listSize - 1).getChunkName());
								sONew.setWriteNumOfBytes(size);
								String server = MetaServer.map.get(sO.getFileName()).get(listSize - 1).getServerName();
								if (!MetaServer.liveFlag.get(server)) {
									sO.setMessage("Server is Down, Cannot Access file");
									objOutStream.writeObject(sO);
								} else {
									Integer port = MetaServer.servers.get(server);
									Socket sock = new Socket(server, port);
									OutputStream outStream1 = sock.getOutputStream();
									ObjectOutputStream objOutStream1 = new ObjectOutputStream(outStream1);
									objOutStream1.flush();
									objOutStream1.writeObject(sONew);
									objOutStream1.flush();

									// accepting connection from that server and
									// doing nothing.
									InputStream inStream1 = sock.getInputStream();
									ObjectInputStream objInStream1 = new ObjectInputStream(inStream1);
									Object ob = objInStream1.readObject();

									// Choosing a random server to send the
									// create
									// chunk command
									String servername = selectServer();

									// generating a new object to be sent to
									// that
									// newly chosen server
									ArrayList<sendingObject> list = MetaServer.map.get(sO.getFileName());
									sendingObject sOCreate = new sendingObject();
									sOCreate = create(list, sOCreate, sO, servername);

									// creating a socket connection to the new
									// server and sending the object
									makeConnection(sOCreate);

									// Sending object to client with servers
									// name
									// and chunk name
									objOutStream.writeObject(sOCreate);
									objOutStream.flush();
								}
							} else {
								String server = MetaServer.map.get(sO.getFileName()).get(listSize - 1).getServerName();
								if (!MetaServer.liveFlag.get(server)) {
									sO.setMessage("Server is Down, Cannot Access file");
									objOutStream.writeObject(sO);
								} else {
									sONew.setChunkName(
											MetaServer.map.get(sO.getFileName()).get(listSize - 1).getChunkName());
									sONew.setServerName(
											MetaServer.map.get(sO.getFileName()).get(listSize - 1).getServerName());
									objOutStream.writeObject(sONew);
								}
							}
						}
						break;

					case "CREATE":
						System.out.println("\nInside " + type + " case");
						break;

					case "UPDATE":
						System.out.println("\nInside " + type + " case");
						break;

					default:
						System.out.println("ERROR: Request type not found.");
						break;
					}
				} else if (obj instanceof HashMap) {
					System.out.println();
					// System.out.println("Printing thread name: " +
					// Thread.currentThread().getName());
					System.out.println("Object received of type: Hashmap from: " + hostname);
					// System.out.println("Heartbeat received from: " +
					// hostname);
					MetaServer.time.put(hostname, System.currentTimeMillis());
					System.out.println(System.currentTimeMillis());
					MetaServer.liveFlag.put(hostname, true);
					mapObject = (HashMap<String, ArrayList<sendingObject>>) obj;
					System.out.println("Before: " + MetaServer.map);
					for (Entry<String, ArrayList<sendingObject>> entry : mapObject.entrySet()) {
						ArrayList<sendingObject> listed = MetaServer.map.get(entry.getKey());
						for (sendingObject object : mapObject.get(entry.getKey())) {
							for (int i = 0; i < listed.size() - 1; i++) {
								// if(O.getChunkName().equals(object.getChunkName()))
								// {
								// O = object;
								// sendingObject oo = listed.get(i);
								String og = listed.get(i).getChunkName();
								String dup = object.getChunkName();
								if (og.equals(dup)) {
									/*
									 * System.out.
									 * println("changing the map to mapobject"
									 * ); oo = object;
									 */
									MetaServer.map.get(entry.getKey()).set(i, object);
								}
							}
						}
					}
					System.out.println("After: " + MetaServer.map);
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		private void makeConnection(sendingObject sOCreate) throws UnknownHostException, IOException {
			Integer port = MetaServer.servers.get(sOCreate.getServerName());
			Socket sock = null;
			try {
				sock = new Socket(sOCreate.getServerName(), port);
				OutputStream outStream = sock.getOutputStream();
				ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
				objOutStream.flush();
				objOutStream.writeObject(sOCreate);
				objOutStream.flush();
			} finally {
				sock.close();
			}
		}

		private String selectServer() {
			Random random = new Random();
			List<String> keys = new ArrayList<String>(MetaServer.servers.keySet());
			String servername = keys.get(random.nextInt(keys.size()));
			return servername;
		}

		private sendingObject create(ArrayList<sendingObject> list, sendingObject sOCreate, sendingObject sO,
				String servername) {
			sOCreate.setType("create");
			sOCreate.setFileSize(sO.getWriteNumOfBytes());
			sOCreate.setFileName(sO.getFileName());
			sOCreate.setChunkName(new SimpleDateFormat("yyyyMMddHHmmss_SSS'.txt'").format(new Date()));
			sOCreate.setServerName(servername);
			list.add(sOCreate);
			System.out.println("Server selected is: " + servername);
			System.out.println("New chunk created for file: " + sO.getFileName() + " is: " + sOCreate.getChunkName());
			MetaServer.map.put(sO.getFileName(), list);
			return sOCreate;
		}
	}
}
