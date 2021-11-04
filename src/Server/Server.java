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
	
	// 클라이언트 정보
	// id, ClientData
	public static HashMap<String, ClientData> clientInfo;
	// 소켓 관려
	public static HashMap<String, Socket> clientSocket;
	private PrintWriter out;	

	public void run() {
		try {
			clientSocket= new HashMap<String, Socket>();
			clientInfo = new HashMap<String, ClientData>();
			// 1. 서버 소켓 생성
			ServerSocket serverSocket = new ServerSocket();

			// 2. 바인딩
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress(hostAddress, PORT));
			
			Mainform.setTf_currentStatus("연결 기다림 - " + hostAddress + ":" + PORT);
			
			// 테스트용 마커			
			clientInfo.put("bbbbbb", new ClientData("테스트", "36.3418131#127.393267", "테스트입니다", "10000", 1));
			// 3. 요청 대기
			while (true) {
				Socket socket = serverSocket.accept();				
				// 스레드 생성
				new serverClientThread(socket).start();
			}
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		} 		
	}
	/*
	 * 접속한 전체 클라이언트에게 전송
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