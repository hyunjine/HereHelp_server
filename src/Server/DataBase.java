package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	final String driver = "oracle.jdbc.driver.OracleDriver";
	final String url = "jdbc:oracle:thin:@localhost:1521:orcl";
	final String db_id = "thevlakk1";
	final String db_pw = "dhfl265213";

	private Connection con;
	private Statement stmt;

	public DataBase() {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, db_id, db_pw);
			stmt = con.createStatement();

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
	}

	/*
	 * ���̵� �ߺ�üũ �޼���
	 */
	public boolean checkID(String id) {
		try {
			String sql = String.format("SELECT ID FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);

			// �ߺ��� ���̵� ���� ��
			if (!rs.next())
				return true;

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

		return false;
	}

	/*
	 * �г��� �ߺ�üũ �޼���
	 */
	public boolean checkNickname(String nickname) {
		try {
			String sql = String.format("SELECT NICKNAME FROM HERE_HELP_CLIENTS WHERE NICKNAME = '%s'", nickname);
			ResultSet rs = stmt.executeQuery(sql);

			// �ߺ��� �г����� ���� ��
			if (!rs.next())
				return true;

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

		return false;
	}
	/*
	 * ���� ���� �޼���
	 */
	public boolean createAccount(String id, String name, String password, String nickname, String phonenumber) {
		try {
			String sql = String.format("INSERT INTO HERE_HELP_CLIENTS VALUES(%s, %s, %s, %s, %s)", id, name, password, nickname, phonenumber);
			stmt.execute(sql);

			return true;

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

		return false;
	}
	/*
	 * �α��� ��û �޼���
	 */
	public boolean login(String id, String password) {
		try {
			String sql = String.format("SELECT PASSWORD FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				String rs_password = rs.getString("PASSWORD");
				// �Է��� ��й�ȣ�� ��ġ ��
				if (rs_password.equals(password))
					return true;
			}				

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return false;
	}
	/* 
	 * �ش� ���̵��� �г��� ��ȯ
	 */
	public String getNickname(String id) {
		try {
			String sql = String.format("SELECT NICKNAME FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);
			// �г��� ��ȯ
			if (rs.next()) 
				return rs.getString("NICKNAME");			

		} catch (Exception e) {
			System.out.println(e.getMessage());
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
}
