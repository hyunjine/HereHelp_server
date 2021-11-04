package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class serverClientThread extends Thread {
	private String id;
	private String nickname;
	private Socket socket;
	private DataBase db;

	public serverClientThread(Socket socket) {
		this.socket = socket;
		this.db = new DataBase();
	}	
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			String fromClient = "";
			JSONParser jsonParser = new JSONParser();
			
			while (socket.isConnected()) {
				fromClient = in.readLine();
				// ������ Ȯ��
				Mainform.setTf_currentStatus(fromClient);				

				JSONObject obj = (JSONObject) jsonParser.parse(fromClient);
				String flag = (String) obj.get("flag");				

				switch (flag) {
				case "idCheck":
					idCheck(obj);
					break;
				case "nicknameCheck":
					nicknameCheck(obj);
					break;
				case "createAccountCheck":
					createAccountCheck(obj);
					break;
				case "login":
					login(obj);
					break;
				case "init":
					init(obj);
					break;
				case "helpRequest":
					helpRequest(obj);
					break;
				case "markerClicked":
					markerClicked(obj);
					break;
				case "completeHelp":
					completeHelp();
					break;
				case "completeTransaction":
					completeTransaction(obj);
					break;
				case "chatting":
					chatting(obj);
					break;
				case "selectType":
					selectType(obj);
					break;
				case "selectPrice":
					selectPrice(obj);
					break;
				case "getContent":
					getContent(obj);
					break;					
				default:
					break;
				}
			}
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		} finally {
			removeSocket();
		}
	}
	/*
	 * idCheck �޼���
	 */
	private void idCheck(JSONObject obj) {
		try {
			String id = (String) obj.get("id");
			
			JSONObject data = new JSONObject();			
			data.put("flag", "idCheck");
			// �ߺ��� ���̵� ������
			if (db.checkID(id)) 				
				data.put("status", "success");			
			// �ߺ��� ���̵� ������
			else 
				data.put("status", "fail");
			
			echo(data);
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * nicknameCheck �޼���
	 */
	private void nicknameCheck(JSONObject obj) {
		try {
			String nickname = (String) obj.get("nickname");
			
			JSONObject data = new JSONObject();
			data.put("flag", "nicknameCheck");
			// �ߺ��� �г����� ������
			if (db.checkNickname(nickname)) 				
				data.put("status", "success");			
			// �ߺ��� �г����� ������
			else 
				data.put("status", "fail");
			
			echo(data);
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * createAccount �޼���
	 */
	private void createAccountCheck(JSONObject obj) {
		try {
			String id = (String) obj.get("id");			
			String name = (String) obj.get("name");			
			String password = (String) obj.get("password");
			String nickname = (String) obj.get("nickname");			
			String phonenumber = (String) obj.get("phonenumber");
			
			JSONObject data = new JSONObject();
			data.put("flag", "createAccountCheck");
			// ȸ������ ���� ��
			if (db.createAccount(id, name, password, nickname, phonenumber)) 
				data.put("status", "success");			
			// ȸ������ ���� ��		
			else
				data.put("status", "fail");
			
			echo(data);
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * login �޼���
	 */
	private void login(JSONObject obj) {
		try {			
			JSONObject data = new JSONObject();			
			String id = (String) obj.get("id");
			String password = (String) obj.get("password");		
			
			// ���� ��
			if (db.login(id, password)) {		
				// ID ����
				this.id = id;
				// �г��� ����
				this.nickname = db.getNickname(id);				
				// hashMap�� ID, socket ����
				Server.clientSocket.put(id, socket);
				// �ǳڿ� �α��� �޼���
				Mainform.setTf_currentStatus(nickname + "���� �����߽��ϴ�.");
				// �����ο� ��
				Mainform.setLb_currentClientNumber(Server.clientSocket.size());
				
				data.put("flag", "login");		
				data.put("activity", (String) obj.get("activity"));		
				data.put("loginStatus", "success");				
				data.put("nickname", nickname);
			}		
			else 			
				data.put("loginStatus", "fail");			
			
			echo(data);
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());		
		}
	}
	/*
	 * init �޼���
	 */
	private void init(JSONObject obj) {
		try {
			JSONObject data = new JSONObject();
			
			data.put("flag", "init");
			// ��Ŀ ��ġ ���			
			JSONArray arr = new JSONArray();
			for (String id : Server.clientInfo.keySet()) {
				JSONObject marker = new JSONObject();
				String location = Server.clientInfo.get(id).getLocation();
				if (location == null) 
					continue;
				marker.put("id", id);
				marker.put("location", location);
				
				arr.add(marker);
			}
			data.put("marker", arr);
			data.put("chatXML", new ChatXML().getChatXML(id));
			
			JSONObject recordXML = new JSONObject();
			recordXML.put("giveXML", new RecordXML().getRecordXML("give", id));
			recordXML.put("receiveXML", new RecordXML().getRecordXML("receive", id));
			
			data.put("recordXML", recordXML);
			
			echo(data);

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

	}
	/*
	 * helpRequest �޼���
	 */
	private void helpRequest(JSONObject obj) {
		JSONObject data = new JSONObject();
		// ��û�� �̹� ��� �Ǿ��ִ��� �˻�
		if (Server.clientInfo.containsKey(id)) {
			data.put("flag", "helpRequest");
			data.put("status", "fail");
			
			echo(data);
		}
		else {
			String location = (String) obj.get("location");		
			String content = (String) obj.get("content");
			String price = (String) obj.get("price");			
			int category = (int) Integer.parseInt(String.valueOf(obj.get("category")));			
		
			// ������ ����			 
			Server.clientInfo.put(id, new ClientData(nickname, location, content, price, category));			
			
			obj.put("status", "success");
			obj.put("id", id);
			
			Server.sendToAll(obj);
		}		
	}
	/*
	 * markerClicked �޼���
	 */
	private void markerClicked(JSONObject obj) {
		String opponent_id = (String) obj.get("opponent_id"); // ��� ���̵�
		String opponent_nickname = Server.clientInfo.get(opponent_id).getNickname(); // ��� �г���
		String content = Server.clientInfo.get(opponent_id).getContent(); // ��� ��û����
		String price = Server.clientInfo.get(opponent_id).getPrice(); // ����
		int category = Server.clientInfo.get(opponent_id).getCategory(); // ī�װ�
		
		JSONObject data = new JSONObject();
		
		data.put("flag", "markerClicked");
		data.put("opponent_id", opponent_id);	
		data.put("opponent_nickname", opponent_nickname);
		data.put("content", content);
		data.put("price", price);
		data.put("category", category);
		
		echo(data);
	}
	/*
	 * completeHelp �޼���
	 */
	private void completeHelp() {
		// ���� ����
		Server.clientInfo.remove(id);
		
		// ����
		JSONObject data = new JSONObject();
		data.put("flag", "completeHelp");
		data.put("id", id);
		
		Server.sendToAll(data);
	}
	/*
	 * completeTransaction �޼���
	 */
	private void completeTransaction(JSONObject obj) {		
		String opponent_id = (String) obj.get("opponent_id");
		String opponent_nickname = db.getNickname(opponent_id);
		
		int category = Server.clientInfo.get(id).getCategory();
		String price = Server.clientInfo.get(id).getPrice();
		
		RecordXML recordXML = new RecordXML();
		// ����
		recordXML.insertRecordXML("receive", id, opponent_id, category, price);
		// ����
		recordXML.insertRecordXML("give", opponent_id, id, category, price);		
		
		String time = recordXML.getCurrentTime();
		
		completeHelp();
		
		JSONObject data = new JSONObject();
		data.put("flag", "completeTransaction");		
		data.put("type", "receive");
		data.put("opponent_nickname", opponent_nickname);
		data.put("category", category);		
		data.put("price", price);
		data.put("time", time);	
		
		echo(data);
		// ���濡�� ���� ����
		data.remove("type");
		data.remove("opponent_nickname");
		
		data.put("type", "give");
		data.put("opponent_nickname", nickname);
		
		sendToOther(opponent_id, data);
	}
	/*
	 * chatting �޼���
	 */
	private void chatting(JSONObject obj) {
		String opponent_id = (String) obj.get("opponent_id");
		String msg = (String) obj.get("msg");		
		// ��ȭ ���� XML���Ϸ� ����
		String currentTime = new ChatXML().insertChatXML(id, opponent_id, msg);
		// ��ȭ ���� �� Ŭ���̾�Ʈ���� ����
		JSONObject data = new JSONObject();
		
		data.put("flag", "chatting");
		data.put("sender", id);
		data.put("sender_nickname", nickname);
		data.put("receiver", opponent_id);		
		data.put("receiver_nickname", new DataBase().getNickname(opponent_id));
		data.put("msg", msg);
		data.put("time", currentTime);
		
		echo(data);
		sendToOther(opponent_id, data);		
	}
	/*
	 * selectType �޼���
	 */
	private void selectType(JSONObject obj) {		
		JSONArray arr = (JSONArray) obj.get("arr");
		JSONArray marker = new JSONArray();
		
		for (String id : Server.clientInfo.keySet()) {
			JSONObject info = new JSONObject();
			int category = Server.clientInfo.get(id).getCategory();
			
			for (int i = 0; i < arr.size(); i++) {
				// ī�װ� ��ġ ��
				if (category ==  Integer.parseInt(String.valueOf(arr.get(i)))) {
					info.put("id", id);
					info.put("location", Server.clientInfo.get(id).getLocation());
					marker.add(info);
					
					break;
				}					
			}
		}
		JSONObject data = new JSONObject();
		data.put("flag", "selectCetegory");		
		data.put("marker", marker);
		
		echo(data);
	}
	/*
	 * selectPrice �޼���
	 */
	private void selectPrice(JSONObject obj) {
		JSONArray marker = new JSONArray();
		
		int minimum = Integer.parseInt(String.valueOf((String) obj.get("minimum")));
		int maximum = Integer.parseInt(String.valueOf((String) obj.get("maximum")));
		
		for (String id : Server.clientInfo.keySet()) {
			JSONObject info = new JSONObject();
			int price = Integer.parseInt(String.valueOf((String) Server.clientInfo.get(id).getPrice()));
			
			if (price >= minimum && price <= maximum) {
				info.put("id", id);
				info.put("location", Server.clientInfo.get(id).getLocation());
				marker.add(info);
			}
		}
		
		JSONObject data = new JSONObject();
		data.put("flag", "selectCetegory");		
		data.put("marker", marker);
		
		echo(data);

	}
	/*
	 * getContent �޼���
	 */
	private void getContent(JSONObject obj) {		
		String content = Server.clientInfo.get(id).getContent();		
		obj.put("content", content);
		
		echo(obj);
	}
//	/*
//	 * ��û ���� �Է� �޼���
//	 */
//	private synchronized void saveAboutHelpInformation(String id, String content, String price, String location, int category) {		
//		Server.clientMarkerContents.put(id, content);
//		Server.clientPrice.put(id, price);
//		Server.clientMarkers.put(id, location);
//		Server.clientCategory.put(id, category);
//	}
//	/*
//	 * ��û ���� ���� �޼���
//	 */
//	private synchronized void removeAboutHelpInformation(String id) {
//		Server.clientMarkerContents.remove(id);
//		Server.clientPrice.remove(id);
//		Server.clientMarkers.remove(id);
//		Server.clientCategory.remove(id);
//	}
//	/*
//	 * clientMarkers ��ȯ �޼���
//	 */
//	private HashMap<String, String> returnMarkers() {
//		return Server.clientMarkers;
//	}
//	/*
//	 * clientMarkerContents ��ȯ �޼���
//	 */
//	private HashMap<String, String> returnClientMarkerContents() {
//		return Server.clientMarkerContents;
//	}
//	/*
//	 * clientMarkerContents�� value ��ȯ �޼��� 
//	 */
//	private synchronized String returnContents(String id) {
//		return Server.clientMarkerContents.get(id);
//	}
	/*
	 * ���� �ݱ� �� �ؽ��� ����
	 */
	private void removeSocket() {
		try {
			socket.close();
			Server.clientSocket.remove(id);
			
			Mainform.setTf_currentStatus(nickname + "���� ������ ������ϴ�.");
			Mainform.setLb_currentClientNumber(Server.clientSocket.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * ���� ���
	 */
	private void echo(JSONObject obj) {
		try {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			out.println(obj);
			out.flush();
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}		
	}
	/*
	 * Ư�� Ŭ���̾�Ʈ���� ����
	 */
	private void sendToOther(String opponent_id, JSONObject obj) {
		try {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(Server.clientSocket.get(opponent_id).getOutputStream(), "UTF-8"));
			out.println(obj);
			out.flush();
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
}
