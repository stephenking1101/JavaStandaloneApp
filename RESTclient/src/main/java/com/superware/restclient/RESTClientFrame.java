package com.superware.restclient;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.PrintStream;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;

public class RESTClientFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField url;
	private LoginDialog loginDialog;
	private JTextArea data;
	private ButtonGroup postMethodGroup = new ButtonGroup();
	public RESTClientFrame() {
		super("REST Client");
		this.frame = this;
		loginDialog = new LoginDialog(this.frame);
		JPanel content = new JPanel();
		getContentPane().add(content, BorderLayout.CENTER);
		content.setLayout(new BorderLayout());
		
		JPanel mainpanel = new JPanel();
		//mainpanel.setPreferredSize(new Dimension(150, 150));
		mainpanel.setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{100, 55, 55, 55, 55};
		gbl_panel.rowHeights = new int[]{20, 30, 20, 30, 40};
		//gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		//gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblUrl = new JLabel("URL:");
		lblUrl.setBorder(new EmptyBorder(0, 0, 0, 2));
		GridBagConstraints gbc_lblUrl = new GridBagConstraints();
		gbc_lblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblUrl.insets = new Insets(0, 0, 0, 0);
		gbc_lblUrl.gridx = 0;
		gbc_lblUrl.gridy = 1;
		gbc_lblUrl.weightx = 0.1;
		panel.add(lblUrl, gbc_lblUrl);
		url = new JTextField();
		GridBagConstraints gbc_url = new GridBagConstraints();
		gbc_url.fill = GridBagConstraints.BOTH;
		gbc_url.insets = new Insets(0, 0, 5, 5);
		gbc_url.gridwidth = 4;
		gbc_url.gridx = 1;
		gbc_url.gridy = 1;
		gbc_url.weightx = 0.5;
		panel.add(url, gbc_url);
		url.setColumns(10);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 5;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		panel.add(new JSeparator(), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 5;
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.5;
		c.weighty = 0.5;
		panel.add(new JSeparator(), c);
		
		JLabel lblNewLabel = new JLabel("Method:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 3;
		gbc_lblNewLabel.weightx = 0.1;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		JRadioButton rdbtnPost = new JRadioButton("Post");
		rdbtnPost.setActionCommand("POST");
		GridBagConstraints gbc_rdbtnPost = new GridBagConstraints();
		gbc_rdbtnPost.anchor = GridBagConstraints.NORTH;
		gbc_rdbtnPost.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnPost.insets = new Insets(0, 0, 0, 0);
		gbc_rdbtnPost.gridx = 1;
		gbc_rdbtnPost.gridy = 3;
		gbc_rdbtnPost.weightx = 0.2;
		panel.add(rdbtnPost, gbc_rdbtnPost);
		postMethodGroup.add(rdbtnPost);
		
		JRadioButton rdbtnGet = new JRadioButton("Get");
		rdbtnGet.setSelected(true);
		rdbtnGet.setActionCommand("GET");
		GridBagConstraints gbc_rdbtnGet = new GridBagConstraints();
		gbc_rdbtnGet.anchor = GridBagConstraints.NORTH;
		gbc_rdbtnGet.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnGet.insets = new Insets(0, 0, 0, 0);
		gbc_rdbtnGet.gridx = 2;
		gbc_rdbtnGet.gridy = 3;
		gbc_rdbtnGet.weightx = 0.2;
		panel.add(rdbtnGet, gbc_rdbtnGet);
		
		postMethodGroup.add(rdbtnGet);
		
		JRadioButton rdbtnPut = new JRadioButton("Put");
		rdbtnPut.setActionCommand("PUT");
		GridBagConstraints gbc_rdbtnPut = new GridBagConstraints();
		gbc_rdbtnPut.anchor = GridBagConstraints.NORTH;
		gbc_rdbtnPut.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnPut.insets = new Insets(0, 0, 0, 0);
		gbc_rdbtnPut.gridx = 3;
		gbc_rdbtnPut.gridy = 3;
		gbc_rdbtnPut.weightx = 0.2;
		panel.add(rdbtnPut, gbc_rdbtnPut);
		postMethodGroup.add(rdbtnPut);
		
		JRadioButton rdbtnDelete = new JRadioButton("Delete");
		rdbtnDelete.setActionCommand("DELETE");
		GridBagConstraints gbc_rdbtnDelete = new GridBagConstraints();
		gbc_rdbtnDelete.anchor = GridBagConstraints.NORTHWEST;
		gbc_rdbtnDelete.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnDelete.gridx = 4;
		gbc_rdbtnDelete.gridy = 3;
		gbc_rdbtnDelete.weightx = 0.5;
		panel.add(rdbtnDelete, gbc_rdbtnDelete);
		postMethodGroup.add(rdbtnDelete);
		
		Action sendAction = new SendAction("Send");
		this.setTitle("REST Client");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		
		JToolBar toolBar = new JToolBar();
		mainpanel.add(toolBar, BorderLayout.NORTH);
		mainpanel.add(panel, BorderLayout.CENTER);
		
		final JButton btnLogin = new JButton("Login");
		final JButton btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(RESTClient.logout()){
            		btnLogin.setEnabled(true);
            		btnLogout.setEnabled(false);
            	}
            }
		});
		btnLogout.setEnabled(false);
		btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	loginDialog.setBtnLogin(btnLogin);
            	loginDialog.setBtnLogout(btnLogout);
            	loginDialog.setLocationRelativeTo(frame);
            	loginDialog.setLocation(frame.getLocation().x + 20,frame.getLocation().y + 20);
            	loginDialog.setVisible(true);
            }
		});
		toolBar.add(btnLogin);
		toolBar.add(btnLogout);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, Color.CYAN));
		scrollPane.setBounds(10, 82, 414, 169);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

        JButton close = new JButton("Close");
        close.setMnemonic(KeyEvent.VK_C);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.exit(0);
            }
		});
        JButton btnSend = new JButton(sendAction);
        bottom.add(btnSend);
        bottom.add(close);
		
		content.add(mainpanel, BorderLayout.PAGE_START);
		
		JPanel datapanel = new JPanel();
		datapanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		datapanel.setLayout(new BorderLayout(0, 0));
//		JLabel lblData = new JLabel("Data To Post:");
//		datapanel.add(lblData, BorderLayout.WEST);
		
		data = new JTextArea();
		data.setRows(10);
		data.setColumns(25);
		JScrollPane scrollPaneData = new JScrollPane(data);
		scrollPaneData.setBorder(new TitledBorder(null, "Data To Send", TitledBorder.LEADING, TitledBorder.TOP, null, Color.CYAN));
		datapanel.add(scrollPaneData, BorderLayout.CENTER);
		mainpanel.add(datapanel, BorderLayout.EAST);
		content.add(scrollPane, BorderLayout.CENTER);
		
//		JLabel lblOutput = new JLabel("Output:");
//		lblOutput.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
//		content.add(lblOutput, BorderLayout.WEST);
		content.add(bottom, BorderLayout.PAGE_END);
		
		PrintStream printStream = new PrintStream(new GUIPrintStream(textArea));
		System.setOut(printStream);
	}
	
	private class SendAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public SendAction(String text) {
			super(text);
			putValue(SHORT_DESCRIPTION, "Send http request");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				String command = postMethodGroup.getSelection().getActionCommand();
				RESTClient.sendRequestWithContext(url.getText(), command, data.getText().trim());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(RESTClientFrame.this,
                        e1.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
