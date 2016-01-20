package org.alicebot.ab.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class IOUtils {

	BufferedReader reader;
	BufferedWriter writer;

	public IOUtils(final String filePath, final String mode) {
		try {
			if (mode.equals("read")) {
				reader = new BufferedReader(new FileReader(filePath));
			} else if (mode.equals("write")) {
				new File(filePath).delete();
				writer = new BufferedWriter(new FileWriter(filePath, true));
			}
		} catch (final IOException e) {
			System.err.println("error: " + e);
		}
	}

	public String readLine() {
		String result = null;
		try {
			result = reader.readLine();
		} catch (final IOException e) {
			System.err.println("error: " + e);
		}
		return result;
	}

	public void writeLine(final String line) {
		try {
			writer.write(line);
			writer.newLine();
		} catch (final IOException e) {
			System.err.println("error: " + e);
		}
	}

	public void close() {
		try {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
		} catch (final IOException e) {
			System.err.println("error: " + e);
		}

	}

	public static void writeOutputTextLine(final String prompt, final String text) {
		System.out.println(prompt + ": " + text);
	}

	public static String readInputTextLine() {
		return readInputTextLine(null);
	}

	public static String readInputTextLine(final String prompt) {
		if (prompt != null) {
			System.out.print(prompt + ": ");
		}
		final BufferedReader lineOfText = new BufferedReader(new InputStreamReader(System.in));
		String textLine = null;
		try {
			textLine = lineOfText.readLine();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return textLine;
	}

	public static File[] listFiles(final File dir) {
		return dir.listFiles();
	}

	public static String system(final String evaluatedContents, final String failedString) {
		final Runtime rt = Runtime.getRuntime();
		// System.out.println("System "+evaluatedContents);
		try {
			final Process p = rt.exec(evaluatedContents);
			final InputStream istrm = p.getInputStream();
			final InputStreamReader istrmrdr = new InputStreamReader(istrm);
			final BufferedReader buffrdr = new BufferedReader(istrmrdr);
			String result = "";
			String data = "";
			while ((data = buffrdr.readLine()) != null) {
				result += data + "\n";
			}
			// System.out.println("Result = "+result);
			return result;
		} catch (final Exception ex) {
			ex.printStackTrace();
			return failedString;

		}
	}

	public static String evalScript(final String engineName, final String script) throws Exception {
		// System.out.println("evaluating "+script);
		final ScriptEngineManager mgr = new ScriptEngineManager();
		final ScriptEngine engine = mgr.getEngineByName("JavaScript");
		final String result = "" + engine.eval(script);
		return result;
	}

}
