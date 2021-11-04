package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChatXML {
	/*
	 * XML 파일 생성 메서드
	 */
	public void createChatXML(String sender, String anotherId, String currentTime, String msg) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true); // standalone="no" 를 없애준다.

			// <HereHelp> 엘리먼트
			Element HereHelp = doc.createElement("HereHelp");
			doc.appendChild(HereHelp);

			// <chatting> 엘리먼트
			Element chatting = doc.createElement("chatting");
			HereHelp.appendChild(chatting);

			// <times> 엘리먼트
			Element times = doc.createElement("times");
			HereHelp.appendChild(times);

			// 채팅 내용 엘리먼트
			Element id = doc.createElement(sender);
			id.appendChild(doc.createTextNode(msg));
			chatting.appendChild(id);

			// 시간 엘리먼트
			Element time = doc.createElement("time");
			time.appendChild(doc.createTextNode(currentTime));
			times.appendChild(time);

			// XML 파일로 쓰기
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // 정렬 스페이스4칸
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 들여쓰기
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); // doc.setXmlStandalone(true); 했을때 붙어서
																				// 출력되는부분 개행

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(sender, anotherId))));

			transformer.transform(source, result);

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML 파일 노드 추가 메서드
	 */
	public String insertChatXML(String sender, String anotherId, String msg) {
		String currentTime = getCurrentTime();
		
		try {						
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(setFileName(sender, anotherId));
			doc.setXmlStandalone(true); // standalone="no" 를 없애준다.
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			/*
			 * chatting 노드 추가
			 */
			String expression = "/HereHelp/chatting";
			// <chatting> 노드
			NodeList chatting = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
			
			// 채팅 내용 엘리먼트
			// sender아이디로 msg 추가
			Element id = doc.createElement(sender);
			id.appendChild(doc.createTextNode(msg));
			chatting.item(0).appendChild(id);			
			/*
			 * times 노드 추가
			 */
			expression = "/HereHelp/times";
			// <times> 노드
			NodeList times = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

			// 시간 엘리먼트
			Element time = doc.createElement("time");
			time.appendChild(doc.createTextNode(currentTime));
			times.item(0).appendChild(time);
			
			// XML 파일로 쓰기
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(sender, anotherId))));

			transformer.transform(source, result);
			
			return currentTime;
		} catch (FileNotFoundException e1) {
			/*
			 * 새로운 채팅일 시 파일 생성
			 */
			createChatXML(sender, anotherId, currentTime, msg);
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
	/*
	 * XML 파일 노드 파싱 메서드
	 */
	public JSONArray getChatXML(String myId) { // chatXML = [ {id : id, nickname : nickname, msgXML : msgXML, timeXML : timeXML},
														   // {id : id, nickname : nickname, msgXML : msgXML, timeXML : timeXML} ]
		try {			
			DataBase db = new DataBase();
			JSONArray chatXML = new JSONArray();
			HashMap<String, String> chatData = getMyChatXMLName(myId);
			
			for (String opponent_id : chatData.keySet()) {
				String fileName = chatData.get(opponent_id);
				JSONObject item = new JSONObject();
				
				// 상대방 아이디와 닉네임 담기
				item.put("opponent_id", opponent_id);
				item.put("opponent_nickname", db.getNickname(opponent_id));
				
				// 메세지와 시간 담기
				JSONArray msgXML = new JSONArray();		// msgXML  = [ {id : id, msg : msg}, {id : id, msg : msg}, {id : id, msg : msg}, ]
				JSONArray timeXML = new JSONArray();	// timeXML = [ time, time, time]
				
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
				XPath xpath = XPathFactory.newInstance().newXPath();
				/*
				 * chatting 노드
				 */
				String expression = "/HereHelp/chatting";
				// 지정 노드로 부터 노드목록 획득
				NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
				// <chatting> 노드
				NodeList chatting = nodeList.item(0).getChildNodes();

				// 확보된 노드 목록을 순서대로 출력
				for (int i = 0; i < chatting.getLength(); i++)
				{
					JSONObject msg = new JSONObject();
					
					// #text노드 버리기
					if ("#text".equals(chatting.item(i).getNodeName()))
						continue;					
					// 노드 명 
					msg.put("id", chatting.item(i).getNodeName());					
					// 노드 값 
					msg.put("msg", chatting.item(i).getTextContent());
					
					msgXML.add(msg);
				}
				
				item.put("msgXML", msgXML);
				/*
				 * times 노드
				 */
				expression = "/HereHelp/times";			
				nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
				// <times> 노드
				NodeList times = nodeList.item(0).getChildNodes(); 

				for (int i = 0; i < times.getLength(); i++) {
					if ("#text".equals(times.item(i).getNodeName()))
						continue;

					timeXML.add(times.item(i).getTextContent());					
				}
				item.put("timeXML", timeXML);
				
				// chatXML 추가
				chatXML.add(item);
			}			
			
			return chatXML;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
	/*
	 * chatXML 파일 이름 생성
	 */
	private String setFileName(String id_1, String id_2) {
		String filename = "C://chatXML/";
		if (id_1.compareTo(id_2) > 0) {
			filename += id_1 + "_" + id_2;
		} else {
			filename += id_2 + "_" + id_1;
		}
		filename += ".xml";

		return filename;
	}
	/*
	 * 현재 시각 반환
	 */
	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		return format.format(time);
	}
	/*
	 * 자신의 채팅 XML파일 이름 반환
	 */
	private HashMap<String, String> getMyChatXMLName(String myId) {
		// key = id, value = filename
		HashMap<String, String> myChatXML = new HashMap<String, String>();
		
		// 경로 설정
		String dir = "C://chatXML/";
		File diretory = new File(dir);
		String[] fileList = diretory.list();
		for (int i = 0; i < fileList.length; i++) {
			// 확장자 제거
			int pos = fileList[i].lastIndexOf(".");
			String fileName = fileList[i].substring(0, pos);
		    // 자신의 아이디가 포함되어 있으면 경로와 확장자 포함해서 arraylist에 추가
			if (fileName.contains(myId)) {
				// 상대방 아이디 색출
				String[] str = fileName.split("_");
				String opponent_id = "";
				if (str[0].equals(myId))
					opponent_id = str[1];
				else
					opponent_id = str[0];
				fileName = dir + fileName + ".xml";
				myChatXML.put(opponent_id, fileName);
			}
		}
		
		return myChatXML;
	}
}
