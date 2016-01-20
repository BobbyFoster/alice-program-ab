package org.alicebot.ab.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DomUtils {

	public static Node parseFile(final String fileName) throws Exception {
		final File file = new File(fileName);

		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
		// dbFactory.setIgnoringComments(true); // fix this
		final Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		final Node root = doc.getDocumentElement();
		return root;
	}

	public static Node parseString(final String string) throws Exception {
		final InputStream is = new ByteArrayInputStream(string.getBytes("UTF-16"));

		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		// from AIMLProcessor.evalTemplate and AIMLProcessor.validTemplate:
		// dbFactory.setIgnoringComments(true); // fix this
		final Document doc = dBuilder.parse(is);
		doc.getDocumentElement().normalize();
		final Node root = doc.getDocumentElement();
		return root;
	}

	/**
	 * convert an XML node to an XML statement
	 *
	 * @param node
	 *            current XML node
	 * @return XML string
	 */
	public static String nodeToString(final Node node) {
		// MagicBooleans.trace("nodeToString(node: " + node + ")");
		final StringWriter sw = new StringWriter();
		try {
			final Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "no");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (final TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		final String result = sw.toString();
		// MagicBooleans.trace("nodeToString() returning: " + result);
		return result;
	}
}
