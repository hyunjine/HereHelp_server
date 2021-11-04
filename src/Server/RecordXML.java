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
	 * XML ���� ���� �޼���
	 */
	public void createRecordXML(String flag, String my_id, String opponent_id, String category, String price) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true); // standalone="no" �� �����ش�.

			// <HereHelp> ������Ʈ
			Element HereHelp = doc.createElement("HereHelp");
			doc.appendChild(HereHelp);

			// <give> ������Ʈ
			Element give = doc.createElement("give");
			HereHelp.appendChild(give);

			// <receive> ������Ʈ
			Element receive = doc.createElement("receive");
			HereHelp.appendChild(receive);
			
			// item ��� ����
			Element item = setItemNode(doc, opponent_id, category, price);
			// give or receive
			if (flag.equals("give")) 
				give.appendChild(item);			
			else if (flag.equals("receive"))
				receive.appendChild(item);			

			// XML ���Ϸ� ����
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // ���� �����̽�4ĭ
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // �鿩����
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); // doc.setXmlStandalone(true); ������ �پ
																				// ��µǴºκ� ����

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(my_id))));

			transformer.transform(source, result);

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML ���� ��� �߰� �޼���
	 */
	public void insertRecordXML(String flag, String my_id, String opponent_id, int _category, String price) {
		// ���ڿ��� ��ȯ
		String category = String.format("%d", _category);
		try {			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(setFileName(my_id));
			doc.setXmlStandalone(true); // standalone="no" �� �����ش�.
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			// give or receive
			String expression = "";
			if (flag.equals("give"))
				expression = "/HereHelp/give";
			else if (flag.equals("receive"))
				expression = "/HereHelp/receive";

			// give or receive ���
			NodeList record = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
			
			
			// item ��� ����
			Element item = setItemNode(doc, opponent_id, category, price);

			record.item(0).appendChild(item);			
			
			// XML ���Ϸ� ����
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(setFileName(my_id))));

			transformer.transform(source, result);
		} catch (FileNotFoundException e1) {
			/*
			 * ���ο� Ȱ������� �� ���� ����
			 */
			createRecordXML(flag, my_id, opponent_id, category, price);
			
		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}
	/*
	 * XML ���� ��� �Ľ� �޼���
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
			// give or receive ���
			NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);			
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				// item ���
				NodeList data = nodeList.item(i).getChildNodes();				
				JSONObject item = new JSONObject();
				
				for (int j = 0; j < data.getLength(); j++) {
					String nodeName = data.item(j).getNodeName();
					String value = data.item(j).getTextContent();
					// #text��� ������
					if ("#text".equals(nodeName))
						continue;
					// �г��� ���
					if (nodeName.equals("id")) {						
						item.put("nickname", db.getNickname(value));
					}
					
					// �ð� ���
					else if (nodeName.equals("time"))
						item.put("time", value);
					
					// ī�װ� ���
					else if (nodeName.equals("category"))
						item.put("category", Integer.parseInt(value));
					
					// ���� ���
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
	 * item ��� ����
	 */
	private Element setItemNode(Document doc, String opponent_id, String category, String price) {
		// Ȱ����� ������Ʈ
		Element item = doc.createElement("item");

		// ���̵�
		Element ele_id = doc.createElement("id");
		ele_id.appendChild(doc.createTextNode(opponent_id));
		item.appendChild(ele_id);

		// �ð�
		Element ele_time = doc.createElement("time");
		ele_time.appendChild(doc.createTextNode(getCurrentTime()));
		item.appendChild(ele_time);

		// ī�װ�
		Element ele_category = doc.createElement("category");
		ele_category.appendChild(doc.createTextNode(category));
		item.appendChild(ele_category);

		// ����
		Element ele_price = doc.createElement("price");
		ele_price.appendChild(doc.createTextNode(price));
		item.appendChild(ele_price);
		
		return item;
	}

	/*
	 * recordXML ���� �̸� ����
	 */
	private String setFileName(String myId) {
		return "C://recordXML/" + myId + ".xml";
	}
	/*
	 * ���� �ð� ��ȯ
	 */
	public String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		return format.format(time);
	}
}
