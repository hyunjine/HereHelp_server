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
					
					// ���� ����
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
		setTitle("���⵵��_����");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 895, 393);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/*
		 * ���� ��Ȳ ��
		 */
		lb_currentClientNumber = new JLabel("���� ���� �ο� : 0��");
		lb_currentClientNumber.setFont(new Font("����", Font.BOLD, 14));
		lb_currentClientNumber.setBounds(24, 27, 184, 15);
		contentPane.add(lb_currentClientNumber);
		/*
		 * �������� �ǳ�
		 */
		JPanel pn_notice = new JPanel();
		pn_notice.setBorder(
				new TitledBorder(null, "��������", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pn_notice.setBounds(24, 52, 399, 292);
		contentPane.add(pn_notice);
		pn_notice.setLayout(null);

		/*
		 * ���� ��
		 */
		JLabel lb_title = new JLabel("����");
		lb_title.setBounds(22, 25, 40, 15);
		pn_notice.add(lb_title);
		// ���� �ؽ�Ʈ
		tf_titleField = new JTextField();
		tf_titleField.setBounds(69, 22, 226, 21);
		pn_notice.add(tf_titleField);
		tf_titleField.setColumns(10);
		/*
		 * ��ũ�� �ǳ�
		 */
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(69, 50, 226, 232);
		pn_notice.add(scrollPane);
		// ���� �ؽ�Ʈ
		JTextArea tf_contentField = new JTextArea();
		scrollPane.setViewportView(tf_contentField);
		
		/*
		 * ���� ��
		 */
		JLabel lb_content = new JLabel("����");
		lb_content.setBounds(22, 50, 40, 15);
		pn_notice.add(lb_content);
		/*
		 * ���� ��ư Ŭ��
		 */
		JButton btn_send = new JButton("����");
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
		 * ���� ��Ȳ �ǳ�
		 */
		JPanel pn_connectionStatus = new JPanel();
		pn_connectionStatus.setLayout(null);
		pn_connectionStatus.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"���� ��Ȳ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pn_connectionStatus.setBounds(435, 52, 432, 292);
		contentPane.add(pn_connectionStatus);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 22, 408, 260);
		pn_connectionStatus.add(scrollPane_1);
		scrollPane_1.setViewportView(tf_currentStatus);

	}
	
	public static void setLb_currentClientNumber(int size) {
		lb_currentClientNumber.setText("���� ���� �ο� : " + size + "��");
	}
	
	public static void setTf_currentStatus(String status) {
		tf_currentStatus.append(status + "\n");
	}
}
