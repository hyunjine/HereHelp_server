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
	 * XML ���� ���� �޼���
	 */
	public void createChatXML(String sender, String anotherId, String currentTime, String msg) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true); // standalone="no" �� �����ش�.

			// <HereHelp> ������Ʈ
			Element HereHelp = doc.createElement("HereHelp");
			doc.appendChild(HereHelp);

			// <chatting> ������Ʈ
			Element chatting = doc.createElement("chatting");
			HereHelp.appendChild(chatting);

			// <times> ������Ʈ
			Element times = doc.createElement("times");
			HereHelp.appendChild(times);

			// ä�� ���� ������Ʈ
			Element id = doc.createElement(sender);
			id.appendChild(doc.createTextNode(msg));
			chatting.appendChild(id);

			// �ð� ������Ʈ
			Element time = doc.createElement("time");
			time.appendChild(doc.createTextNode(currentTime));
			times.appendChild(time);

			// XML ���Ϸ� ����
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // ���� �����̽�4ĭ
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // �鿩����
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); // doc.setXmlStandalone(true); ������ �پ
																				// ��µǴºκ� ����

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(sender, anotherId))));

			transformer.transform(source, result);

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML ���� ��� �߰� �޼���
	 */
	public String insertChatXML(String sender, String anotherId, String msg) {
		String currentTime = getCurrentTime();
		
		try {						
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(setFileName(sender, anotherId));
			doc.setXmlStandalone(true); // standalone="no" �� �����ش�.
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			/*
			 * chatting ��� �߰�
			 */
			String expression = "/HereHelp/chatting";
			// <chatting> ���
			NodeList chatting = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
			
			// ä�� ���� ������Ʈ
			// sender���̵�� msg �߰�
			Element id = doc.createElement(sender);
			id.appendChild(doc.createTextNode(msg));
			chatting.item(0).appendChild(id);			
			/*
			 * times ��� �߰�
			 */
			expression = "/HereHelp/times";
			// <times> ���
			NodeList times = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

			// �ð� ������Ʈ
			Element time = doc.createElement("time");
			time.appendChild(doc.createTextNode(currentTime));
			times.item(0).appendChild(time);
			
			// XML ���Ϸ� ����
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(sender, anotherId))));

			transformer.transform(source, result);
			
			return currentTime;
		} catch (FileNotFoundException e1) {
			/*
			 * ���ο� ä���� �� ���� ����
			 */
			createChatXML(sender, anotherId, currentTime, msg);
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
	/*
	 * XML ���� ��� �Ľ� �޼���
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
				
				// ���� ���̵�� �г��� ���
				item.put("opponent_id", opponent_id);
				item.put("opponent_nickname", db.getNickname(opponent_id));
				
				// �޼����� �ð� ���
				JSONArray msgXML = new JSONArray();		// msgXML  = [ {id : id, msg : msg}, {id : id, msg : msg}, {id : id, msg : msg}, ]
				JSONArray timeXML = new JSONArray();	// timeXML = [ time, time, time]
				
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
				XPath xpath = XPathFactory.newInstance().newXPath();
				/*
				 * chatting ���
				 */
				String expression = "/HereHelp/chatting";
				// ���� ���� ���� ����� ȹ��
				NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
				// <chatting> ���
				NodeList chatting = nodeList.item(0).getChildNodes();

				// Ȯ���� ��� ����� ������� ���
				for (int i = 0; i < chatting.getLength(); i++)
				{
					JSONObject msg = new JSONObject();
					
					// #text��� ������
					if ("#text".equals(chatting.item(i).getNodeName()))
						continue;					
					// ��� �� 
					msg.put("id", chatting.item(i).getNodeName());					
					// ��� �� 
					msg.put("msg", chatting.item(i).getTextContent());
					
					msgXML.add(msg);
				}
				
				item.put("msgXML", msgXML);
				/*
				 * times ���
				 */
				expression = "/HereHelp/times";			
				nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
				// <times> ���
				NodeList times = nodeList.item(0).getChildNodes(); 

				for (int i = 0; i < times.getLength(); i++) {
					if ("#text".equals(times.item(i).getNodeName()))
						continue;

					timeXML.add(times.item(i).getTextContent());					
				}
				item.put("timeXML", timeXML);
				
				// chatXML �߰�
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
	 * chatXML ���� �̸� ����
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
	 * ���� �ð� ��ȯ
	 */
	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		return format.format(time);
	}
	/*
	 * �ڽ��� ä�� XML���� �̸� ��ȯ
	 */
	private HashMap<String, String> getMyChatXMLName(String myId) {
		// key = id, value = filename
		HashMap<String, String> myChatXML = new HashMap<String, String>();
		
		// ��� ����
		String dir = "C://chatXML/";
		File diretory = new File(dir);
		String[] fileList = diretory.list();
		for (int i = 0; i < fileList.length; i++) {
			// Ȯ���� ����
			int pos = fileList[i].lastIndexOf(".");
			String fileName = fileList[i].substring(0, pos);
		    // �ڽ��� ���̵� ���ԵǾ� ������ ��ο� Ȯ���� �����ؼ� arraylist�� �߰�
			if (fileName.contains(myId)) {
				// ���� ���̵� ����
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
