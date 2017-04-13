package com.ibm.rtc.automation.examples.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DOMUtil {

	/** Prints the specified node, then prints all of its children. */
	public static void printDOM(Node node) {
		int type = node.getNodeType();
		switch (type) {
		// print the document element
		case Node.DOCUMENT_NODE: {
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			printDOM(((Document) node).getDocumentElement());
			break;
		}

			// print element with attributes
		case Node.ELEMENT_NODE: {
			System.out.print("<");
			System.out.print(node.getNodeName());
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				System.out.print(" " + attr.getNodeName().trim() + "=\""
						+ attr.getNodeValue().trim() + "\"");
			}
			System.out.println(">");

			NodeList children = node.getChildNodes();
			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++)
					printDOM(children.item(i));
			}

			break;
		}

			// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			System.out.print("&");
			System.out.print(node.getNodeName().trim());
			System.out.print(";");
			break;
		}

			// print cdata sections
		case Node.CDATA_SECTION_NODE: {
			System.out.print("");
			break;
		}

			// print text
		case Node.TEXT_NODE: {
			System.out.print(node.getNodeValue().trim());
			break;
		}

			// print processing instruction
		case Node.PROCESSING_INSTRUCTION_NODE: {
			System.out.print("");
			break;
		}
		}

		if (type == Node.ELEMENT_NODE) {
			System.out.println();
			System.out.print("");
		}
	}

	/**
	 * Parse the XML file and create Document
	 * 
	 * @param fileName
	 * @return Document
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document parse(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		Document document = null;
		// Initiate DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// To get a validating parser
		factory.setValidating(false);
		// To get one that understands namespaces
		factory.setNamespaceAware(true);

		// Get DocumentBuilder
		DocumentBuilder builder = factory.newDocumentBuilder();
		// Parse and load into memory the Document
		try {
			document = builder.parse(new File(fileName));
		} catch (SAXParseException mbe) {
			// Cater for UTF-8 file
			File file = new File(fileName);
			InputStream inputStream = new FileInputStream(file);
			document = builder.parse(inputStream, "UTF-8");
		}
		return document;

	}

	public static Document parse(InputStream inputStream, boolean namespaceAware)
			throws ParserConfigurationException, SAXException, IOException {
		Document document = null;
		// Initiate DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// To get a validating parser
		factory.setValidating(false);
		// To get one that understands namespaces
		factory.setNamespaceAware(namespaceAware);

		// Get DocumentBuilder
		DocumentBuilder builder = factory.newDocumentBuilder();
		// Parse and load into memory the Document
		document = builder.parse(inputStream, "UTF-8");
		return document;
	}

	/**
	 * This method writes a DOM document to a file
	 * 
	 * @param filename
	 * @param document
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public static void writeXmlToFile(String filename, Document document)
			throws TransformerFactoryConfigurationError, TransformerException {

		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		File file = new File(filename);
		Result result = new StreamResult(file);

		// Write the DOM document to the file
		// Get Transformer
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		// Write to a file
		xformer.transform(source, result);

	}

	/**
	 * Count Elements in Document by Tag Name
	 * 
	 * @param tag
	 * @param document
	 * @return number elements by Tag Name
	 */
	public static int countByTagName(String tag, Document document) {
		NodeList list = document.getElementsByTagName(tag);
		return list.getLength();
	}

	public static Node getChildrenNodeByName(Node parentNode, String name) {
		if (parentNode == null || name == null) {
			return null;
		}
		NodeList children = parentNode.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node n = (Node) children.item(i);
				if (name.equals(n.getNodeName())) {
					return n;
				}
			}
		}
		return null;
	}

	public static Node getChildNodeByElementNameAndAttribute(Node parentNode,
			String elementName, String attributeName, String attributeValue) {
		if (parentNode == null || elementName == null || attributeName == null
				|| attributeValue == null) {
			return null;
		}
		NodeList children = parentNode.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node n = (Node) children.item(i);
				int type = n.getNodeType();

				if (type == Node.ELEMENT_NODE
						&& elementName.equals(n.getNodeName())) {
					NamedNodeMap nnm = n.getAttributes();

					Node namedItem = null;
					if (nnm != null) {
						namedItem = nnm.getNamedItem(attributeName);
					}
					String valueFromDocument = null;
					if (namedItem != null) {
						valueFromDocument = namedItem.getTextContent();
					}
					if (valueFromDocument != null
							&& attributeValue.equals(valueFromDocument)) {
						return n;
					}
				}
			}

		}
		return null;
	}

	public static String getTextValue(Node node) {
		StringBuffer text = new StringBuffer();
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE
					|| nodeList.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
				text.append(((Text) nodeList.item(i)).getData());
			} else {
				text.append(getTextValue(nodeList.item(i)));
			}
		}
		return text.toString();
	}

	public static String transformNodeToXML(Node node)
			throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter writer = new StringWriter();

		TransformerFactory tFactory = TransformerFactory.newInstance();
		tFactory.setAttribute("indent-number", 0);
		Transformer transformer = tFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		String xml = writer.toString();
		return xml;
	}

	public static Node getFirstElementNode(Node parentNode) {
		if (parentNode == null) {
			return null;
		}
		NodeList children = parentNode.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node n = (Node) children.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					return n;
				}
			}
		}
		return null;

	}

}
