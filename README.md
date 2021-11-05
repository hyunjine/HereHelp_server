# 여기 도움
**사용자 간 필요한 정보나 상황들을 지도 위 마커를 통하여 제시한 금액에 맞춰 도움을 줄 수 있는    
안드로이드 애플리케이션**
## Preview
<img src="https://user-images.githubusercontent.com/92709137/140468537-b92ab937-f106-430c-8437-a588b7d13988.PNG" width="90%"/>

## 개발 기간
**2021-05-01 ~ 2021-09-23**

## 개발 환경 및 라이브러리
> **Server**
> * JAVA - Eclipse
> * GUI
> * Multi-Thread SocketIO
> * JSON
> * XPath

> **DataBase**
> * SQL Developer    
> * XML

> **Client**
> * JAVA - Android Stuio
> * Google Map API
> * JSON GSON

## 어플 소개


## 어플 특징
> 지도위에서 필요한 글을 작성하는 방식입니다.

> 다양한 카테고리가 있어 넓은 용도로 어플을 사용할 수 있습니다.   
> Ex - 중고거래, 급한 도움(비가 와서 우산이 필요, 짐이 많아 같이 들어줄 사람 필요 등), 구인구직 등

> 지도의 마커를 확인하거나 상대방과의 거리가 나와있어 근처 사용자가 빠르게 도움을 주고 보수를 얻을 수 있습니다.

## 1. 소켓 대기
``` java
// 1. 서버 소켓 생성
ServerSocket serverSocket = new ServerSocket();

// 2. 바인딩
String hostAddress = InetAddress.getLocalHost().getHostAddress();
serverSocket.bind(new InetSocketAddress(hostAddress, PORT));
			
Mainform.setTf_currentStatus("연결 기다림 - " + hostAddress + ":" + PORT);

// 3. 요청 대기
while (true) {
  Socket socket = serverSocket.accept();				
  // 스레드 생성
  new serverClientThread(socket).start();
}
```
## 2. 데이터 수신

```java
public void run() {
	try {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		String fromClient = "";
		JSONParser jsonParser = new JSONParser();
		
		while (socket.isConnected()) {
			fromClient = in.readLine();
			// 데이터 확인
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
```
* JSONObject의 flag키값에 의해 각각의 메서드로 진행됩니다.

## 3. 데이터 저장 - JDBC, XML
* 클라이언트의 정보는 SQL Developer로, 채팅기록과 활동기록은 XML파일형태로 저장했습니다.
> XML 변환 예시 - 채팅기록
```java
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
```
* 채팅내용을 저장하는 insertChatXML메서드입니다.
* 새로운 데이터가 생성되면 우선 insertChatXML메서드가 실행되고, FileNotFoundException이 발생하면 새로운 파일이 필요하기에 createChatXML메서드가 실행됩니다.
* XML 파싱은 XPath API를 사용하여 파싱했습니다.
* insertChatXML 한 곳에서 현재시각을 얻고 반환하는 이유는 컴파일 시 발생하는 실행시간때문에 오차가 생길 염려가 있기에 한 곳에서 처리하였습니다. 

> 저장된 모습
<img src="https://user-images.githubusercontent.com/92709137/140471556-cc70eee2-531c-4751-a716-2ee3d534d437.PNG" width="90%"/>

> 파일명 세팅
```java
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

	return filename; //C://chatXML/system_bbbbbb.xml
}
```
* 두 클라이언트의 아이디로 XML파일명을 생성합니다.
* 아이디가 두개이기 때문에 항상 동일한 파일명을 얻기 위해 compareTo메서드를 활용하여 두개의 아이디를 비교해 항상 같은 순서로 파일명을 반환하게 했습니다.

```java
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
		Mainform.setTf_currentStatus(e.getMessage());
	}
	
	return null;
}
```

* 채팅내용을 가져오는 getChatXML메서드입니다.
* chatXML = [ {id : id, nickname : nickname, msgXML : msgXML, timeXML : timeXML},{id : id, nickname : nickname, msgXML : msgXML, timeXML : timeXML} ] 형태로 JSONObject에 담겨 전송됩니다.
* C:\chatXML파일경로에 있는 XML파일 중 자신의 아이디가 포함된 XML파일을 모두 담아와 파싱합니다.

```java
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
```
