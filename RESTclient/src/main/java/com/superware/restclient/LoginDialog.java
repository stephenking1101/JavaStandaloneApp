package com.superware.restclient;

import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.awt.Color;

public class LoginDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userNameField;
	private JTextField userNameValue;
	private JTextField passField;
	private JPasswordField passValue;
	private JTextField sessionId;
	private JTextField loginURL;
	private JTextField errorURL;
	private JButton btnLogin, btnLogout;
	public LoginDialog(Frame frame) {
		super(frame, true);
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JLabel lblUserName = new JLabel("User Name:");
		lblUserName.setBackground(Color.ORANGE);
		lblUserName.setBounds(10, 0, 75, 23);
		panel.add(lblUserName);
		
		JLabel lblField = new JLabel("Field:");
		lblField.setBounds(10, 34, 46, 14);
		panel.add(lblField);
		
		userNameField = new JTextField();
		userNameField.setBounds(68, 31, 86, 20);
		panel.add(userNameField);
		userNameField.setColumns(10);
		userNameField.setText("j_username");
		
		JLabel lblValue = new JLabel("Value:");
		lblValue.setBounds(195, 34, 46, 14);
		panel.add(lblValue);
		
		userNameValue = new JTextField();
		userNameValue.setBounds(256, 31, 86, 20);
		panel.add(userNameValue);
		userNameValue.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(10, 63, 75, 20);
		panel.add(lblPassword);
		
		JLabel lblField_1 = new JLabel("Field:");
		lblField_1.setBounds(10, 94, 46, 14);
		panel.add(lblField_1);
		
		passField = new JTextField();
		passField.setBounds(68, 91, 86, 20);
		panel.add(passField);
		passField.setColumns(10);
		passField.setText("j_password");
		
		JLabel lblValue_1 = new JLabel("Value:");
		lblValue_1.setBounds(195, 94, 46, 14);
		panel.add(lblValue_1);
		
		passValue = new JPasswordField();
		passValue.setBounds(256, 91, 86, 20);
		panel.add(passValue);
		passValue.setColumns(10);
		//passValue.setEchoChar((char)0);
		
		JLabel lblSessionId = new JLabel("SESSION ID:");
		lblSessionId.setBounds(10, 141, 75, 17);
		panel.add(lblSessionId);
		
		sessionId = new JTextField();
		sessionId.setBounds(85, 139, 111, 20);
		panel.add(sessionId);
		sessionId.setColumns(10);
		sessionId.setText("G3JSESSIONID");
		
		JSeparator separator = new JSeparator();
		separator.setBounds(45, 128, 1, 2);
		panel.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(0, 128, 434, 2);
		panel.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(0, 59, 434, 2);
		panel.add(separator_2);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(0, 166, 434, 2);
		panel.add(separator_3);
		
		JLabel lblLoginUrl = new JLabel("Login URL:");
		lblLoginUrl.setBounds(10, 182, 64, 23);
		panel.add(lblLoginUrl);
		
		loginURL = new JTextField();
		loginURL.setBounds(85, 182, 339, 23);
		panel.add(loginURL);
		loginURL.setColumns(10);
		loginURL.setText("https://g3softwaredeploy.it.global.hsbc/j_spring_security_check");
		
		JLabel lblLoginError = new JLabel("Login Error:");
		lblLoginError.setBounds(10, 216, 64, 23);
		panel.add(lblLoginError);
		
		errorURL = new JTextField();
		errorURL.setBounds(85, 216, 339, 23);
		panel.add(errorURL);
		errorURL.setColumns(10);
		errorURL.setText("https://g3softwaredeploy.it.global.hsbc/loginfailed");
		
		Action action = new LoginAction("Login", null, "Login Button");
		JButton btnLogin = new JButton(action);
		btnLogin.setBounds(169, 253, 89, 23);
		panel.add(btnLogin);
		setSize(450, 350);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setResizable(false);
	}
	
	private class LoginAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LoginAction(String text, ImageIcon icon,
                String desc) {
			 super(text, icon);
			 putValue(SHORT_DESCRIPTION, desc);
		}
		
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton) e.getSource();
			try {
				source.setText("Logging in...");
				source.setEnabled(false);
				if(RESTClient.login(loginURL.getText(), userNameField.getText(), 
						         userNameValue.getText(), passField.getText(), 
						         new String(passValue.getPassword()), errorURL.getText(), 
						         sessionId.getText())){
					JOptionPane.showMessageDialog(LoginDialog.this,
	                        "Login successfully!",
	                        "Success",
	                        JOptionPane.INFORMATION_MESSAGE);
					source.setText("Login");
					source.setEnabled(true);
					setVisible(false);
					btnLogin.setEnabled(false);
					btnLogout.setEnabled(true);
				}
			} catch (Exception e1) {
				source.setText("Login");
				source.setEnabled(true);
				JOptionPane.showMessageDialog(LoginDialog.this,
                        e1.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

	public void setBtnLogin(final JButton btnLogin) {
		this.btnLogin = btnLogin;
	}

	public void setBtnLogout(final JButton btnLogout) {
		this.btnLogout = btnLogout;
	}
}
