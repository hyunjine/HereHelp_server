package Server;

import java.net.Socket;

public class ClientData {
	// �г���
	private String nickname;
	// ��Ŀ ��ġ
	private String location;
	// �� ����
	private String content;
	// ���� �ݾ�
	private String price;
	// �� ī�װ�
	private int category;

	// ���� ���� �� ����
	public ClientData (String nickname, String location, String content, String price, int category) { 
		this.nickname = nickname;
		this.location = location;
		this.content = content;
		this.price = price;
		this.category = category;
	}
	
	// getter & setter
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}
	
}
