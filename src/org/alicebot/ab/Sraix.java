package org.alicebot.ab;

/* Program AB Reference AIML 2.0 implementation
 Copyright (C) 2013 ALICE A.I. Foundation
 Contact: info@alicebot.org

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public License for more details.

 You should have received a copy of the GNU Library General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 Boston, MA  02110-1301, USA.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alicebot.ab.utils.CalendarUtils;
import org.alicebot.ab.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Sraix {

	public static HashMap<String, String> custIdMap = new HashMap<String, String>();

	public static String custid = "1"; // customer ID number for Pandorabots

	public static String sraix(final Chat chatSession, final String input,
			final String defaultResponse, final String hint, final String host, final String botid,
			final String apiKey, final String limit) {
		String response;
		if (!MagicBooleans.enable_network_connection) {
			response = MagicStrings.sraix_failed;
		} else if (host != null && botid != null) {
			response = sraixPandorabots(input, chatSession, host, botid);
		} else {
			response = sraixPannous(input, hint, chatSession);
		}
		System.out.println("Sraix: response = " + response + " defaultResponse = " + defaultResponse);
		if (response.equals(MagicStrings.sraix_failed)) {
			if (chatSession != null && defaultResponse == null) {
				response = AIMLProcessor.respond(MagicStrings.sraix_failed, "nothing", "nothing",
						chatSession);
			} else if (defaultResponse != null) {
				response = defaultResponse;
			}
		}
		return response;
	}

	public static String sraixPandorabots(final String input, final Chat chatSession,
			final String host, final String botid) {
		// System.out.println("Entering SRAIX with input="+input+" host ="+host+" botid="+botid);
		final String responseContent = pandorabotsRequest(input, host, botid);
		if (responseContent == null) {
			return MagicStrings.sraix_failed;
		} else {
			return pandorabotsResponse(responseContent, chatSession, host, botid);
		}
	}

	public static String pandorabotsRequest(final String input, final String host, final String botid) {
		try {
			custid = "0";
			final String key = host + ":" + botid;
			if (custIdMap.containsKey(key)) {
				custid = custIdMap.get(key);
			}
			// System.out.println("--> custid = "+custid);
			// System.out.println("Pandorabots Request "+input);
			final String spec = NetworkUtils.spec(host, botid, custid, input);
			// String fragment = "?botid="+botid+"&custid="+custid+"input="+input;
			// URI uri = new URI("http", host, "/pandora/talk-xml", fragment);
			/*
			 * String scheme = "http"; String authority = host; String path = "/pandora/talk-xml";
			 * String query = "botid="+botid+"&custid="+custid+"&input="+input; String fragment = null;
			 * URI uri=null; String out; try { uri = new URI(scheme, authority, path, query, fragment);
			 * out = "\n"; out += "URI example:\n"; out += "        URI string: "+uri.toString()+"\n";
			 * System.out.print(out); } catch (Exception e) { e.printStackTrace(); }
			 */
			// uri = new URI(spec);
			// String subInput = input;
			// while (subInput.contains(" ")) subInput = subInput.replace(" ", "+");
			// spec =
			// "http://"+host+"/pandora/talk-xml?botid="+botid+"&custid="+custid+"input="+subInput;
			if (MagicBooleans.trace_mode) {
				System.out.println("Spec = " + spec);
				// System.out.println("URI="+uri);
				// http://isengard.pandorabots.com:8008/pandora/talk-xml?botid=835f69388e345ab2&custid=dd3155d18e344a7c&input=%E3%81%93%E3%82%93%E3%81%AB%E3%81%A1%E3%81%AF
			}

			final String responseContent = NetworkUtils.responseContent(spec);
			// System.out.println("Sraix: Response="+responseContent);
			return responseContent;
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String pandorabotsResponse(final String sraixResponse, final Chat chatSession,
			final String host, final String botid) {
		String botResponse = MagicStrings.sraix_failed;
		try {
			int n1 = sraixResponse.indexOf("<that>");
			int n2 = sraixResponse.indexOf("</that>");

			if (n2 > n1) {
				botResponse = sraixResponse.substring(n1 + "<that>".length(), n2);
			}
			n1 = sraixResponse.indexOf("custid=");
			if (n1 > 0) {
				custid = sraixResponse.substring(n1 + "custid=\"".length(), sraixResponse.length());
				n2 = custid.indexOf("\"");
				if (n2 > 0) {
					custid = custid.substring(0, n2);
				} else {
					custid = "0";
				}
				final String key = host + ":" + botid;
				// System.out.println("--> Map "+key+" --> "+custid);
				custIdMap.put(key, custid);
			}
			if (botResponse.endsWith(".")) {
				botResponse = botResponse.substring(0, botResponse.length() - 1); // snnoying
																					// Pandorabots
																					// extra "."
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return botResponse;
	}

	public static String sraixPannous(String input, String hint, final Chat chatSession) {
		try {
			final String rawInput = input;
			if (hint == null) {
				hint = MagicStrings.sraix_no_hint;
			}
			input = " " + input + " ";
			input = input.replace(" point ", ".");
			input = input.replace(" rparen ", ")");
			input = input.replace(" lparen ", "(");
			input = input.replace(" slash ", "/");
			input = input.replace(" star ", "*");
			input = input.replace(" dash ", "-");
			// input = chatSession.bot.preProcessor.denormalize(input);
			input = input.trim();
			input = input.replace(" ", "+");
			final int offset = CalendarUtils.timeZoneOffset();
			// System.out.println("OFFSET = "+offset);
			String locationString = "";
			if (Chat.locationKnown) {
				locationString = "&location=" + Chat.latitude + "," + Chat.longitude;
			}
			// https://weannie.pannous.com/api?input=when+is+daylight+savings+time+in+the+us&locale=en_US&login=pandorabots&ip=169.254.178.212&botid=0&key=CKNgaaVLvNcLhDupiJ1R8vtPzHzWc8mhIQDFSYWj&exclude=Dialogues,ChatBot&out=json
			// exclude=Dialogues,ChatBot&out=json&clientFeatures=show-images,reminder,say&debug=true
			final String url = "https://ask.pannous.com/api?input="
					+ input
					+ "&locale=en_US&timeZone="
					+ offset
					+ locationString
					+ "&login="
					+ MagicStrings.pannous_login
					+ "&ip="
					+ NetworkUtils.localIPAddress()
					+ "&botid=0&key="
					+ MagicStrings.pannous_api_key
					+ "&exclude=Dialogues,ChatBot&out=json&clientFeatures=show-images,reminder,say&debug=true";
			MagicBooleans.trace("in Sraix.sraixPannous, url: '" + url + "'");
			final String page = NetworkUtils.responseContent(url);
			// MagicBooleans.trace("in Sraix.sraixPannous, page: " + page);
			String text = "";
			String imgRef = "";
			String urlRef = "";
			if (page == null || page.length() == 0) {
				text = MagicStrings.sraix_failed;
			} else {
				final JSONArray outputJson = new JSONObject(page).getJSONArray("output");
				// MagicBooleans.trace("in Sraix.sraixPannous, outputJson class: " +
				// outputJson.getClass() + ", outputJson: " + outputJson);
				if (outputJson.length() == 0) {
					text = MagicStrings.sraix_failed;
				} else {
					final JSONObject firstHandler = outputJson.getJSONObject(0);
					// MagicBooleans.trace("in Sraix.sraixPannous, firstHandler class: " +
					// firstHandler.getClass() + ", firstHandler: " + firstHandler);
					final JSONObject actions = firstHandler.getJSONObject("actions");
					// MagicBooleans.trace("in Sraix.sraixPannous, actions class: " + actions.getClass()
					// + ", actions: " + actions);
					if (actions.has("reminder")) {
						// MagicBooleans.trace("in Sraix.sraixPannous, found reminder action");
						final Object obj = actions.get("reminder");
						if (obj instanceof JSONObject) {
							if (MagicBooleans.trace_mode) {
								System.out.println("Found JSON Object");
							}
							final JSONObject sObj = (JSONObject) obj;
							String date = sObj.getString("date");
							date = date.substring(0, "2012-10-24T14:32".length());
							if (MagicBooleans.trace_mode) {
								System.out.println("date=" + date);
							}
							final String duration = sObj.getString("duration");
							if (MagicBooleans.trace_mode) {
								System.out.println("duration=" + duration);
							}

							final Pattern datePattern = Pattern.compile("(.*)-(.*)-(.*)T(.*):(.*)");
							final Matcher m = datePattern.matcher(date);
							String year = "", month = "", day = "", hour = "", minute = "";
							if (m.matches()) {
								year = m.group(1);
								month = String.valueOf(Integer.parseInt(m.group(2)) - 1);
								day = m.group(3);

								hour = m.group(4);
								minute = m.group(5);
								text = "<year>" + year + "</year>" + "<month>" + month + "</month>"
										+ "<day>" + day + "</day>" + "<hour>" + hour + "</hour>"
										+ "<minute>" + minute + "</minute>" + "<duration>" + duration
										+ "</duration>";

							} else {
								text = MagicStrings.schedule_error;
							}
						}
					} else if (actions.has("say") && !hint.equals(MagicStrings.sraix_pic_hint)
							&& !hint.equals(MagicStrings.sraix_shopping_hint)) {
						MagicBooleans.trace("in Sraix.sraixPannous, found say action");
						final Object obj = actions.get("say");
						// MagicBooleans.trace("in Sraix.sraixPannous, obj class: " + obj.getClass());
						// MagicBooleans.trace("in Sraix.sraixPannous, obj instanceof JSONObject: " +
						// (obj instanceof JSONObject));
						if (obj instanceof JSONObject) {
							final JSONObject sObj = (JSONObject) obj;
							text = sObj.getString("text");
							if (sObj.has("moreText")) {
								final JSONArray arr = sObj.getJSONArray("moreText");
								for (int i = 0; i < arr.length(); i++) {
									text += " " + arr.getString(i);
								}
							}
						} else {
							text = obj.toString();
						}
					}
					if (actions.has("show") && !text.contains("Wolfram")
							&& actions.getJSONObject("show").has("images")) {
						MagicBooleans.trace("in Sraix.sraixPannous, found show action");
						final JSONArray arr = actions.getJSONObject("show").getJSONArray("images");
						final int i = (int) (arr.length() * Math.random());
						// for (int j = 0; j < arr.length(); j++) System.out.println(arr.getString(j));
						imgRef = arr.getString(i);
						if (imgRef.startsWith("//")) {
							imgRef = "http:" + imgRef;
						}
						imgRef = "<a href=\"" + imgRef + "\"><img src=\"" + imgRef + "\"/></a>";
						// System.out.println("IMAGE REF="+imgRef);

					}
					if (hint.equals(MagicStrings.sraix_shopping_hint) && actions.has("open")
							&& actions.getJSONObject("open").has("url")) {
						urlRef = "<oob><url>" + actions.getJSONObject("open").getString("url")
								+ "</oob></url>";

					}
				}
				if (hint.equals(MagicStrings.sraix_event_hint) && !text.startsWith("<year>")) {
					return MagicStrings.sraix_failed;
				} else if (text.equals(MagicStrings.sraix_failed)) {
					return AIMLProcessor.respond(MagicStrings.sraix_failed, "nothing", "nothing",
							chatSession);
				} else {
					text = text.replace("&#39;", "'");
					text = text.replace("&apos;", "'");
					text = text.replaceAll("\\[(.*)\\]", "");
					String[] sentences;
					sentences = text.split("\\. ");
					// System.out.println("Sraix: text has "+sentences.length+" sentences:");
					String clippedPage = sentences[0];
					for (int i = 1; i < sentences.length; i++) {
						if (clippedPage.length() < 500) {
							clippedPage = clippedPage + ". " + sentences[i];
							// System.out.println(i+". "+sentences[i]);
						}
					}

					clippedPage = clippedPage + " " + imgRef + " " + urlRef;
					clippedPage = clippedPage.trim();
					log(rawInput, clippedPage);
					return clippedPage;
				}
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.out.println("Sraix '" + input + "' failed");
		}
		return MagicStrings.sraix_failed;
	} // sraixPannous

	public static void log(final String pattern, String template) {
		System.out.println("Logging " + pattern);
		template = template.trim();
		if (MagicBooleans.cache_sraix) {
			try {
				if (!template.contains("<year>") && !template.contains("No facilities")) {
					template = template.replace("\n", "\\#Newline");
					template = template.replace(",", MagicStrings.aimlif_split_char_name);
					template = template.replaceAll("<a(.*)</a>", "");
					template = template.trim();
					if (template.length() > 0) {
						final FileWriter fstream = new FileWriter(
								"c:/ab/bots/sraixcache/aimlif/sraixcache.aiml.csv", true);
						final BufferedWriter fbw = new BufferedWriter(fstream);
						fbw.write("0," + pattern + ",*,*," + template + ",sraixcache.aiml");
						fbw.newLine();
						fbw.close();
					}
				}
			} catch (final Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
}
