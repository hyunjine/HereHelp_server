package Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

public class Server extends Thread {
	public static final int PORT = 3000;
	
	// Ŭ���̾�Ʈ ����
	// id, ClientData
	public static HashMap<String, ClientData> clientInfo;
	// ���� ����
	public static HashMap<String, Socket> clientSocket;
	private PrintWriter out;	

	public void run() {
		try {
			clientSocket= new HashMap<String, Socket>();
			clientInfo = new HashMap<String, ClientData>();
			// 1. ���� ���� ����
			ServerSocket serverSocket = new ServerSocket();

			// 2. ���ε�
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress(hostAddress, PORT));
			
			Mainform.setTf_currentStatus("���� ��ٸ� - " + hostAddress + ":" + PORT);
			
			// �׽�Ʈ�� ��Ŀ			
			clientInfo.put("bbbbbb", new ClientData("�׽�Ʈ", "36.3418131#127.393267", "�׽�Ʈ�Դϴ�", "10000", 1));
			// 3. ��û ���
			while (true) {
				Socket socket = serverSocket.accept();				
				// ������ ����
				new serverClientThread(socket).start();
			}
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		} 		
	}
	/*
	 * ������ ��ü Ŭ���̾�Ʈ���� ����
	 */
	public static void sendToAll(JSONObject obj) {
		try {
			for (String id : clientSocket.keySet()) {
				Socket socket = clientSocket.get(id);
				
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				out.println(obj);
				out.flush();
			}
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
}