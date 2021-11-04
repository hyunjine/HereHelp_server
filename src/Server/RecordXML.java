package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class RecordXML {
	/*
	 * XML 파일 생성 메서드
	 */
	public void createRecordXML(String flag, String my_id, String opponent_id, String category, String price) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true); // standalone="no" 를 없애준다.

			// <HereHelp> 엘리먼트
			Element HereHelp = doc.createElement("HereHelp");
			doc.appendChild(HereHelp);

			// <give> 엘리먼트
			Element give = doc.createElement("give");
			HereHelp.appendChild(give);

			// <receive> 엘리먼트
			Element receive = doc.createElement("receive");
			HereHelp.appendChild(receive);
			
			// item 노드 생성
			Element item = setItemNode(doc, opponent_id, category, price);
			// give or receive
			if (flag.equals("give")) 
				give.appendChild(item);			
			else if (flag.equals("receive"))
				receive.appendChild(item);			

			// XML 파일로 쓰기
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // 정렬 스페이스4칸
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 들여쓰기
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); // doc.setXmlStandalone(true); 했을때 붙어서
																				// 출력되는부분 개행

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(my_id))));

			transformer.transform(source, result);

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML 파일 노드 추가 메서드
	 */
	public void insertRecordXML(String flag, String my_id, String opponent_id, int _category, String price) {
		// 문자열로 변환
		String category = String.format("%d", _category);
		try {			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(setFileName(my_id));
			doc.setXmlStandalone(true); // standalone="no" 를 없애준다.
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			// give or receive
			String expression = "";
			if (flag.equals("give"))
				expression = "/HereHelp/give";
			else if (flag.equals("receive"))
				expression = "/HereHelp/receive";

			// give or receive 노드
			NodeList record = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
			
			
			// item 노드 생성
			Element item = setItemNode(doc, opponent_id, category, price);

			record.item(0).appendChild(item);			
			
			// XML 파일로 쓰기
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(my_id))));

			transformer.transform(source, result);
		} catch (FileNotFoundException e1) {
			/*
			 * 새로운 활동기록일 시 파일 생성
			 */
			createRecordXML(flag, my_id, opponent_id, category, price);
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML 파일 노드 파싱 메서드
	 */
	public JSONArray getRecordXML(String flag, String myId) { // giveXML, receiveXML = [ { nickname = nickname, time = time, category = category, price = price }, 
																		 	 	 	  // { nickname = nickname, time = time, category = category, price = price } ]
		try {			
			DataBase db = new DataBase();
			JSONArray recordXML = new JSONArray();			

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(setFileName(myId));
			XPath xpath = XPathFactory.newInstance().newXPath();
			// give or receive
			String expression = "";
			if (flag.equals("give"))
				expression = "/HereHelp/give/item";
			else if (flag.equals("receive"))
				expression = "/HereHelp/receive/item";
			// give or receive 노드
			NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);			
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				// item 노드
				NodeList data = nodeList.item(i).getChildNodes();				
				JSONObject item = new JSONObject();
				
				for (int j = 0; j < data.getLength(); j++) {
					String nodeName = data.item(j).getNodeName();
					String value = data.item(j).getTextContent();
					// #text노드 버리기
					if ("#text".equals(nodeName))
						continue;
					// 닉네임 담기
					if (nodeName.equals("id")) {						
						item.put("nickname", db.getNickname(value));
					}
					
					// 시간 담기
					else if (nodeName.equals("time"))
						item.put("time", value);
					
					// 카테고리 담기
					else if (nodeName.equals("category"))
						item.put("category", Integer.parseInt(value));
					
					// 가격 담기
					else if (nodeName.equals("price"))
						item.put("price", value);				
					
				}
				
				recordXML.add(item);
			}
			

			return recordXML;
			
		} catch (Exception e) {			
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
	
	/*
	 * item 노드 생성
	 */
	private Element setItemNode(Document doc, String opponent_id, String category, String price) {
		// 활동기록 엘리먼트
		Element item = doc.createElement("item");

		// 아이디
		Element ele_id = doc.createElement("id");
		ele_id.appendChild(doc.createTextNode(opponent_id));
		item.appendChild(ele_id);

		// 시간
		Element ele_time = doc.createElement("time");
		ele_time.appendChild(doc.createTextNode(getCurrentTime()));
		item.appendChild(ele_time);

		// 카테고리
		Element ele_category = doc.createElement("category");
		ele_category.appendChild(doc.createTextNode(category));
		item.appendChild(ele_category);

		// 가격
		Element ele_price = doc.createElement("price");
		ele_price.appendChild(doc.createTextNode(price));
		item.appendChild(ele_price);
		
		return item;
	}

	/*
	 * recordXML 파일 이름 생성
	 */
	private String setFileName(String myId) {
		return "C://recordXML/" + myId + ".xml";
	}
	/*
	 * 현재 시각 반환
	 */
	public String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		return format.format(time);
	}
}
