package com.superware.restclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JToolBar;

public class RESTClientFrame extends JFrame {
	private JFrame frame;
	private JTextField url;
	private JDialog loginDialog;
	private JTextArea data;
	private ButtonGroup postMethodGroup = new ButtonGroup();
	public RESTClientFrame() {
		super("REST Client");
		this.frame = this;
		loginDialog = new LoginDialog(this.frame);
		JPanel content = new JPanel();
		getContentPane().add(content, BorderLayout.CENTER);
		content.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(150, 150));
		url = new JTextField();
		url.setBounds(98, 34, 273, 16);
		panel.add(url);
		url.setColumns(10);
		
		JLabel lblUrl = new JLabel("URL:");
		lblUrl.setBounds(10, 35, 46, 14);
		panel.add(lblUrl);
		
		Action sendAction = new SendAction("Send");
		JButton btnSend = new JButton(sendAction);
		btnSend.setBounds(397, 116, 89, 23);
		panel.add(btnSend);
		
		JRadioButton rdbtnGet = new JRadioButton("Get");
		rdbtnGet.setSelected(true);
		rdbtnGet.setBounds(164, 57, 55, 23);
		rdbtnGet.setActionCommand("GET");
		panel.add(rdbtnGet);
		
		JRadioButton rdbtnPost = new JRadioButton("Post");
		rdbtnPost.setBounds(98, 57, 55, 23);
		rdbtnPost.setActionCommand("POST");
		panel.add(rdbtnPost);
		
		postMethodGroup.add(rdbtnGet);
		postMethodGroup.add(rdbtnPost);
		
		JLabel lblNewLabel = new JLabel("Method:");
		lblNewLabel.setBounds(10, 61, 46, 14);
		panel.add(lblNewLabel);
		this.setTitle("REST Client");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setBounds(0, 0, 434, 23);
		panel.add(toolBar);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	loginDialog.setLocationRelativeTo(frame);
            	loginDialog.setLocation(frame.getLocation().x + 20,frame.getLocation().y + 20);
            	loginDialog.setVisible(true);
            }
		});
		toolBar.add(btnLogin);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(10, 82, 414, 169);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton close = new JButton("Close");
        close.setMnemonic(KeyEvent.VK_C);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.exit(0);
            }
		});
        bottom.add(close);
		
		content.add(panel, BorderLayout.PAGE_START);
		
		JLabel lblData = new JLabel("Data To Post:");
		lblData.setBounds(10, 86, 89, 23);
		panel.add(lblData);
		
		data = new JTextArea();
		JScrollPane scrollPaneData = new JScrollPane(data);
		scrollPaneData.setBounds(98, 87, 273, 54);
		panel.add(scrollPaneData);
		content.add(scrollPane, BorderLayout.CENTER);
		content.add(bottom, BorderLayout.PAGE_END);
		
		PrintStream printStream = new PrintStream(new GUIPrintStream(textArea));
		System.setOut(printStream);
	}
	
	private class SendAction extends AbstractAction {
		public SendAction(String text) {
			super(text);
			putValue(SHORT_DESCRIPTION, "Send http request");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				String command = postMethodGroup.getSelection().getActionCommand();
				RESTClient.sendRequestWithCookieStore(url.getText(), command, data.getText().trim());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
