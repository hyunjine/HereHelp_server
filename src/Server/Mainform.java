package Server;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;

public class Mainform extends JFrame {
	
	private JPanel contentPane;
	private JTextField tf_titleField;
	
	private static JLabel lb_currentClientNumber;	
	private static JTextArea tf_currentStatus = new JTextArea();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Mainform frame = new Mainform();
					frame.setVisible(true);		
					
					// 서버 가동
					new Server().start();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		

	}	

	/**
	 * Create the frame.
	 */
	public Mainform() {				
		setTitle("여기도움_서버");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 895, 393);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/*
		 * 접속 현황 라벨
		 */
		lb_currentClientNumber = new JLabel("현재 접속 인원 : 0명");
		lb_currentClientNumber.setFont(new Font("굴림", Font.BOLD, 14));
		lb_currentClientNumber.setBounds(24, 27, 184, 15);
		contentPane.add(lb_currentClientNumber);
		/*
		 * 공지사항 판넬
		 */
		JPanel pn_notice = new JPanel();
		pn_notice.setBorder(
				new TitledBorder(null, "공지사항", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pn_notice.setBounds(24, 52, 399, 292);
		contentPane.add(pn_notice);
		pn_notice.setLayout(null);

		/*
		 * 제목 라벨
		 */
		JLabel lb_title = new JLabel("제목");
		lb_title.setBounds(22, 25, 40, 15);
		pn_notice.add(lb_title);
		// 제목 텍스트
		tf_titleField = new JTextField();
		tf_titleField.setBounds(69, 22, 226, 21);
		pn_notice.add(tf_titleField);
		tf_titleField.setColumns(10);
		/*
		 * 스크롤 판넬
		 */
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(69, 50, 226, 232);
		pn_notice.add(scrollPane);
		// 내용 텍스트
		JTextArea tf_contentField = new JTextArea();
		scrollPane.setViewportView(tf_contentField);
		
		/*
		 * 내용 라벨
		 */
		JLabel lb_content = new JLabel("내용");
		lb_content.setBounds(22, 50, 40, 15);
		pn_notice.add(lb_content);
		/*
		 * 전송 버튼 클릭
		 */
		JButton btn_send = new JButton("전송");
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String notice_title = tf_titleField.getText();
				String notice_content = tf_contentField.getText();

				tf_contentField.setText("");
			}
		});
		btn_send.setBounds(307, 22, 74, 260);
		pn_notice.add(btn_send);

		/*
		 * 접속 현황 판넬
		 */
		JPanel pn_connectionStatus = new JPanel();
		pn_connectionStatus.setLayout(null);
		pn_connectionStatus.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"접속 현황", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pn_connectionStatus.setBounds(435, 52, 432, 292);
		contentPane.add(pn_connectionStatus);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 22, 408, 260);
		pn_connectionStatus.add(scrollPane_1);
		scrollPane_1.setViewportView(tf_currentStatus);

	}
	
	public static void setLb_currentClientNumber(int size) {
		lb_currentClientNumber.setText("현재 접속 인원 : " + size + "명");
	}
	
	public static void setTf_currentStatus(String status) {
		tf_currentStatus.append(status + "\n");
	}
}
