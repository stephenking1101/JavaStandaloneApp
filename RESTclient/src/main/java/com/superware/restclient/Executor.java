package com.superware.restclient;

import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Executor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				try{
					Font font = new Font("Arial", Font.PLAIN, 12);
					
					UIManager.put("Label.font", font);
					UIManager.put("ComboBox.font", font);
					UIManager.put("Button.font", font);
					UIManager.put("RadioButton.font", font);
					UIManager.put("TitledBorder.font", font);
					UIManager.put("TabbedPane.font", font);
					
					RESTClientFrame frame = new RESTClientFrame();
					frame.setLocation(100, 100);
					frame.setSize(650, 500);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
					frame.setResizable(true);
				} catch(Exception e){
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		});
	}

}
