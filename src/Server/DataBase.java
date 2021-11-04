package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	final String driver = "oracle.jdbc.driver.OracleDriver";
	final String url = "jdbc:oracle:thin:@localhost:1521:orcl";
	final String db_id = "";
	final String db_pw = "";

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
	 * 아이디 중복체크 메서드
	 */
	public boolean checkID(String id) {
		try {
			String sql = String.format("SELECT ID FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);

			// 중복된 아이디가 없을 시
			if (!rs.next())
				return true;

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

		return false;
	}

	/*
	 * 닉네임 중복체크 메서드
	 */
	public boolean checkNickname(String nickname) {
		try {
			String sql = String.format("SELECT NICKNAME FROM HERE_HELP_CLIENTS WHERE NICKNAME = '%s'", nickname);
			ResultSet rs = stmt.executeQuery(sql);

			// 중복된 닉네임이 없을 시
			if (!rs.next())
				return true;

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}

		return false;
	}
	/*
	 * 계정 생성 메서드
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
	 * 로그인 요청 메서드
	 */
	public boolean login(String id, String password) {
		try {
			String sql = String.format("SELECT PASSWORD FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				String rs_password = rs.getString("PASSWORD");
				// 입력한 비밀번호와 일치 시
				if (rs_password.equals(password))
					return true;
			}				

		} catch (Exception e) {
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return false;
	}
	/* 
	 * 해당 아이디의 닉네임 반환
	 */
	public String getNickname(String id) {
		try {
			String sql = String.format("SELECT NICKNAME FROM HERE_HELP_CLIENTS WHERE ID = '%s'", id);
			ResultSet rs = stmt.executeQuery(sql);
			// 닉네임 반환
			if (rs.next()) 
				return rs.getString("NICKNAME");			

		} catch (Exception e) {
			System.out.println(e.getMessage());
			Mainform.setTf_currentStatus(e.getMessage());
		}
		
		return null;
	}
}
