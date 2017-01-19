package com.superware.restclient;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class GUIPrintStream extends OutputStream {

	private JTextArea textArea;
	public GUIPrintStream(JTextArea textArea) {
		super();
		this.textArea = textArea;
	}
	@Override
	public void write(int arg0) throws IOException {
	}

	public void write(byte data[]) {
		textArea.append(new String(data));
	}
	
	public void write(byte data[], int off, int len) {
		textArea.append(new String(data, off, len));
		textArea.setCaretPosition(textArea.getText().length());
	}
}
