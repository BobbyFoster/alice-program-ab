package org.alicebot.ab.utils;

import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JapaneseUtils {

	/**
	 * Tokenize a fragment of the input that contains only text
	 *
	 * @param fragment
	 *            fragment of input containing only text and no XML tags
	 * @return tokenized fragment
	 */
	public static String tokenizeFragment(final String fragment) {
		// System.out.println("buildFragment "+fragment);
		String result = "";
		for (final Morpheme e : Tagger.parse(fragment)) {
			result += e.surface + " ";
			//
			// System.out.println("Feature "+e.feature+" Surface="+e.surface);
		}
		return result.trim();
	}

	/**
	 * Morphological analysis of an input sentence that contains an AIML pattern.
	 *
	 * @param sentence
	 * @return morphed sentence with one space between words, preserving XML markup and AIML $ operation
	 */
	public static String tokenizeSentence(final String sentence) {
		// System.out.println("tokenizeSentence "+sentence);
		if (!MagicBooleans.jp_tokenize) {
			return sentence;
		}
		String result = "";
		result = tokenizeXML(sentence);
		while (result.contains("$ ")) {
			result = result.replace("$ ", "$");
		}
		while (result.contains("  ")) {
			result = result.replace("  ", " ");
		}
		while (result.contains("anon ")) {
			result = result.replace("anon ", "anon"); // for Triple Store
		}
		result = result.trim();
		// if (MagicBooleans.trace_mode)
		// System.out.println("tokenizeSentence '"+sentence+"'-->'"+result+"'");
		return result;
	}

	public static String tokenizeXML(String xmlExpression) {
		// System.out.println("tokenizeXML "+xmlExpression);
		String response = MagicStrings.template_failed;
		try {
			xmlExpression = "<sentence>" + xmlExpression + "</sentence>";
			final Node root = DomUtils.parseString(xmlExpression);
			response = recursEval(root);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return AIMLProcessor.trimTag(response, "sentence");
	}

	private static String recursEval(final Node node) {
		try {

			final String nodeName = node.getNodeName();
			// System.out.println("recursEval "+nodeName);
			if (nodeName.equals("#text")) {
				return tokenizeFragment(node.getNodeValue());
			} else if (nodeName.equals("sentence")) {
				return evalTagContent(node);
			} else {
				return genericXML(node);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return "JP Morph Error";
	}

	public static String genericXML(final Node node) {
		// System.out.println("genericXML "+node.getNodeName());
		final String result = evalTagContent(node);
		return unevaluatedXML(result, node);
	}

	public static String evalTagContent(final Node node) {
		String result = "";
		// System.out.println("evalTagContent "+node.getNodeName());
		try {
			final NodeList childList = node.getChildNodes();
			for (int i = 0; i < childList.getLength(); i++) {
				final Node child = childList.item(i);
				result += recursEval(child);
			}
		} catch (final Exception ex) {
			System.out.println("Something went wrong with evalTagContent");
			ex.printStackTrace();
		}
		return result;
	}

	private static String unevaluatedXML(final String result, final Node node) {
		final String nodeName = node.getNodeName();
		String attributes = "";
		if (node.hasAttributes()) {
			final NamedNodeMap XMLAttributes = node.getAttributes();
			for (int i = 0; i < XMLAttributes.getLength(); i++)

			{
				attributes += " " + XMLAttributes.item(i).getNodeName() + "=\""
						+ XMLAttributes.item(i).getNodeValue() + "\"";
			}
		}
		if (result.equals("")) {
			return " <" + nodeName + attributes + "/> ";
		} else {
			return " <" + nodeName + attributes + ">" + result + "</" + nodeName + "> "; // add spaces
		}
	}
}
