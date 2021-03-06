package org.alicebot.ab;

/* Program AB Reference AIML 2.0 implementation
 Copyright (C) 2013 ALICE A.I. Foundation
 Contact: info@alicebot.org
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.alicebot.ab.utils.IOUtils;

/**
 * Class representing the AIML bot
 */
public class Bot {
	public final Properties properties = new Properties();
	public final PreProcessor preProcessor;
	public final Graphmaster brain;
	public Graphmaster learnfGraph;
	public Graphmaster learnGraph;

	// public Graphmaster unfinishedGraph;
	// public final ArrayList<Category> categories;

	public String name = MagicStrings.default_bot_name;
	public HashMap<String, AIMLSet> setMap = new HashMap<String, AIMLSet>();
	public HashMap<String, AIMLMap> mapMap = new HashMap<String, AIMLMap>();
	public HashSet<String> pronounSet = new HashSet<String>();
	public String root_path = "c:/ab";
	public String bot_path = root_path + "/bots";
	public String bot_name_path = bot_path + "/super";
	public String aimlif_path = bot_path + "/aimlif";
	public String aiml_path = bot_path + "/aiml";
	public String config_path = bot_path + "/config";
	public String log_path = bot_path + "/log";
	public String sets_path = bot_path + "/sets";
	public String maps_path = bot_path + "/maps";

	/**
	 * Set all directory path variables for this bot
	 *
	 * @param root
	 *            root directory of Program AB
	 * @param name
	 *            name of bot
	 */
	public void setAllPaths(final String root, final String name) {
		bot_path = root + "/bots";
		bot_name_path = bot_path + "/" + name;
		if (MagicBooleans.trace_mode) {
			System.out.println("Name = " + name + " Path = " + bot_name_path);
		}
		aiml_path = bot_name_path + "/aiml";
		aimlif_path = bot_name_path + "/aimlif";
		config_path = bot_name_path + "/config";
		log_path = bot_name_path + "/logs";
		sets_path = bot_name_path + "/sets";
		maps_path = bot_name_path + "/maps";
		if (MagicBooleans.trace_mode) {
			System.out.println(root_path);
			System.out.println(bot_path);
			System.out.println(bot_name_path);
			System.out.println(aiml_path);
			System.out.println(aimlif_path);
			System.out.println(config_path);
			System.out.println(log_path);
			System.out.println(sets_path);
			System.out.println(maps_path);
		}
	}

	/**
	 * Constructor (default action, default path, default bot name)
	 */
	public Bot() {
		this(MagicStrings.default_bot);
	}

	/**
	 * Constructor (default action, default path)
	 *
	 * @param name
	 */
	public Bot(final String name) {
		this(name, MagicStrings.root_path);
	}

	/**
	 * Constructor (default action)
	 *
	 * @param name
	 * @param path
	 */
	public Bot(final String name, final String path) {
		this(name, path, "auto");
	}

	/**
	 * Constructor
	 *
	 * @param name
	 *            name of bot
	 * @param path
	 *            root path of Program AB
	 * @param action
	 *            Program AB action
	 */
	public Bot(final String name, final String path, final String action) {
		int cnt = 0;
		this.name = name;
		setAllPaths(path, name);
		brain = new Graphmaster(this);

		learnfGraph = new Graphmaster(this, "learnf");
		learnGraph = new Graphmaster(this, "learn");
		// this.unfinishedGraph = new Graphmaster(this);
		// this.categories = new ArrayList<Category>();

		preProcessor = new PreProcessor(this);
		addProperties();
		cnt = addAIMLSets();
		if (MagicBooleans.trace_mode) {
			System.out.println("Loaded " + cnt + " set elements.");
		}
		cnt = addAIMLMaps();
		if (MagicBooleans.trace_mode) {
			System.out.println("Loaded " + cnt + " map elements");
		}
		pronounSet = getPronouns();
		final AIMLSet number = new AIMLSet(MagicStrings.natural_number_set_name, this);
		setMap.put(MagicStrings.natural_number_set_name, number);
		final AIMLMap successor = new AIMLMap(MagicStrings.map_successor, this);
		mapMap.put(MagicStrings.map_successor, successor);
		final AIMLMap predecessor = new AIMLMap(MagicStrings.map_predecessor, this);
		mapMap.put(MagicStrings.map_predecessor, predecessor);
		final AIMLMap singular = new AIMLMap(MagicStrings.map_singular, this);
		mapMap.put(MagicStrings.map_singular, singular);
		final AIMLMap plural = new AIMLMap(MagicStrings.map_plural, this);
		mapMap.put(MagicStrings.map_plural, plural);
		// System.out.println("setMap = "+setMap);
		final Date aimlDate = new Date(new File(aiml_path).lastModified());
		final Date aimlIFDate = new Date(new File(aimlif_path).lastModified());
		if (MagicBooleans.trace_mode) {
			System.out.println("AIML modified " + aimlDate + " AIMLIF modified " + aimlIFDate);
		}
		// readUnfinishedIFCategories();
		MagicStrings.pannous_api_key = Utilities.getPannousAPIKey(this);
		MagicStrings.pannous_login = Utilities.getPannousLogin(this);
		if (action.equals("aiml2csv")) {
			addCategoriesFromAIML();
		} else if (action.equals("csv2aiml")) {
			addCategoriesFromAIMLIF();
		} else if (action.equals("chat-app")) {
			if (MagicBooleans.trace_mode) {
				System.out.println("Loading only AIMLIF files");
			}
			cnt = addCategoriesFromAIMLIF();
		} else if (aimlDate.after(aimlIFDate)) {
			if (MagicBooleans.trace_mode) {
				System.out.println("AIML modified after AIMLIF");
			}
			cnt = addCategoriesFromAIML();
			writeAIMLIFFiles();
		} else {
			addCategoriesFromAIMLIF();
			if (brain.getCategories().size() == 0) {
				System.out.println("No AIMLIF Files found.  Looking for AIML");
				cnt = addCategoriesFromAIML();
			}
		}
		final Category b = new Category(0, "PROGRAM VERSION", "*", "*",
				MagicStrings.program_name_version, "update.aiml");
		brain.addCategory(b);
		brain.nodeStats();
		learnfGraph.nodeStats();

	}

	HashSet<String> getPronouns() {
		final HashSet<String> pronounSet = new HashSet<String>();
		final String pronouns = Utilities.getFile(config_path + "/pronouns.txt");
		final String[] splitPronouns = pronouns.split("\n");
		for (final String splitPronoun : splitPronouns) {
			final String p = splitPronoun.trim();
			if (p.length() > 0) {
				pronounSet.add(p);
			}
		}
		if (MagicBooleans.trace_mode) {
			System.out.println("Read pronouns: " + pronounSet);
		}
		return pronounSet;
	}

	/**
	 * add an array list of categories with a specific file name
	 *
	 * @param file
	 *            name of AIML file
	 * @param moreCategories
	 *            list of categories
	 */
	void addMoreCategories(final String file, final ArrayList<Category> moreCategories) {
		if (file.contains(MagicStrings.deleted_aiml_file)) {
			/*
			 * for (Category c : moreCategories) { //System.out.println("Delete "+c.getPattern());
			 * deletedGraph.addCategory(c); }
			 */

		} else if (file.contains(MagicStrings.learnf_aiml_file)) {
			if (MagicBooleans.trace_mode) {
				System.out.println("Reading Learnf file");
			}
			for (final Category c : moreCategories) {
				brain.addCategory(c);
				learnfGraph.addCategory(c);
				// patternGraph.addCategory(c);
			}
			// this.categories.addAll(moreCategories);
		} else {
			for (final Category c : moreCategories) {
				// System.out.println("Brain size="+brain.root.size());
				// brain.printgraph();
				brain.addCategory(c);
				// patternGraph.addCategory(c);
				// brain.printgraph();
			}
			// this.categories.addAll(moreCategories);
		}
	}

	/**
	 * Load all brain categories from AIML directory
	 */
	int addCategoriesFromAIML() {
		final Timer timer = new Timer();
		timer.start();
		int cnt = 0;
		try {
			// Directory path here
			String file;
			final File folder = new File(aiml_path);
			if (folder.exists()) {
				final File[] listOfFiles = IOUtils.listFiles(folder);
				if (MagicBooleans.trace_mode) {
					System.out.println("Loading AIML files from " + aiml_path);
				}
				for (final File listOfFile : listOfFiles) {
					if (listOfFile.isFile()) {
						file = listOfFile.getName();
						if (file.endsWith(".aiml") || file.endsWith(".AIML")) {
							if (MagicBooleans.trace_mode) {
								System.out.println(file);
							}
							try {
								final ArrayList<Category> moreCategories = AIMLProcessor
										.AIMLToCategories(aiml_path, file);
								addMoreCategories(file, moreCategories);
								cnt += moreCategories.size();
							} catch (final Exception iex) {
								System.out.println("Problem loading " + file);
								iex.printStackTrace();
							}
						}
					}
				}
			} else {
				System.out.println("addCategoriesFromAIML: " + aiml_path + " does not exist.");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		if (MagicBooleans.trace_mode) {
			System.out.println("Loaded " + cnt + " categories in " + timer.elapsedTimeSecs() + " sec");
		}
		return cnt;
	}

	/**
	 * load all brain categories from AIMLIF directory
	 */
	public int addCategoriesFromAIMLIF() {
		final Timer timer = new Timer();
		timer.start();
		int cnt = 0;
		try {
			// Directory path here
			String file;
			final File folder = new File(aimlif_path);
			if (folder.exists()) {
				final File[] listOfFiles = IOUtils.listFiles(folder);
				if (MagicBooleans.trace_mode) {
					System.out.println("Loading AIML files from " + aimlif_path);
				}
				for (final File listOfFile : listOfFiles) {
					if (listOfFile.isFile()) {
						file = listOfFile.getName();
						if (file.endsWith(MagicStrings.aimlif_file_suffix)
								|| file.endsWith(MagicStrings.aimlif_file_suffix.toUpperCase())) {
							if (MagicBooleans.trace_mode) {
								System.out.println(file);
							}
							try {
								final ArrayList<Category> moreCategories = readIFCategories(aimlif_path
										+ "/" + file);
								cnt += moreCategories.size();
								addMoreCategories(file, moreCategories);
								// MemStats.memStats();
							} catch (final Exception iex) {
								System.out.println("Problem loading " + file);
								iex.printStackTrace();
							}
						}
					}
				}
			} else {
				System.out.println("addCategoriesFromAIMLIF: " + aimlif_path + " does not exist.");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		if (MagicBooleans.trace_mode) {
			System.out.println("Loaded " + cnt + " categories in " + timer.elapsedTimeSecs() + " sec");
		}
		return cnt;
	}

	/**
	 * write all AIML and AIMLIF categories
	 */
	public void writeQuit() {
		writeAIMLIFFiles();
		// System.out.println("Wrote AIMLIF Files");
		writeAIMLFiles();
		// System.out.println("Wrote AIML Files");
		/*
		 * updateUnfinishedCategories(); writeUnfinishedIFCategories();
		 */
	}

	/**
	 * read categories from specified AIMLIF file into specified graph
	 *
	 * @param graph
	 *            Graphmaster to store categories
	 * @param fileName
	 *            file name of AIMLIF file
	 */
	public int readCertainIFCategories(final Graphmaster graph, final String fileName) {
		int cnt = 0;
		final File file = new File(aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix);
		if (file.exists()) {
			try {
				final ArrayList<Category> certainCategories = readIFCategories(aimlif_path + "/"
						+ fileName + MagicStrings.aimlif_file_suffix);
				for (final Category d : certainCategories) {
					graph.addCategory(d);
				}
				cnt = certainCategories.size();
				System.out.println("readCertainIFCategories " + cnt + " categories from " + fileName
						+ MagicStrings.aimlif_file_suffix);
			} catch (final Exception iex) {
				System.out.println("Problem loading " + fileName);
				iex.printStackTrace();
			}
		} else {
			System.out.println("No " + aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix
					+ " file found");
		}
		return cnt;
	}

	/**
	 * write certain specified categories as AIMLIF files
	 *
	 * @param graph
	 *            the Graphmaster containing the categories to write
	 * @param file
	 *            the destination AIMLIF file
	 */
	public void writeCertainIFCategories(final Graphmaster graph, final String file) {
		if (MagicBooleans.trace_mode) {
			System.out.println("writeCertainIFCaegories " + file + " size= "
					+ graph.getCategories().size());
		}
		writeIFCategories(graph.getCategories(), file + MagicStrings.aimlif_file_suffix);
		final File dir = new File(aimlif_path);
		dir.setLastModified(new Date().getTime());
	}

	/**
	 * write deleted categories to AIMLIF file
	 */

	/**
	 * write learned categories to AIMLIF file
	 */
	public void writeLearnfIFCategories() {
		writeCertainIFCategories(learnfGraph, MagicStrings.learnf_aiml_file);
	}

	/**
	 * write unfinished categories to AIMLIF file
	 */
	/*
	 * public void writeUnfinishedIFCategories() { writeCertainIFCategories(unfinishedGraph,
	 * MagicStrings.unfinished_aiml_file); }
	 */

	/**
	 * write categories to AIMLIF file
	 *
	 * @param cats
	 *            array list of categories
	 * @param filename
	 *            AIMLIF filename
	 */
	public void writeIFCategories(final ArrayList<Category> cats, final String filename) {
		// System.out.println("writeIFCategories "+filename);
		BufferedWriter bw = null;
		final File existsPath = new File(aimlif_path);
		if (existsPath.exists()) {
			try {
				// Construct the bw object
				bw = new BufferedWriter(new FileWriter(aimlif_path + "/" + filename));
				for (final Category category : cats) {
					bw.write(Category.categoryToIF(category));
					bw.newLine();
				}
			} catch (final FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (final IOException ex) {
				ex.printStackTrace();
			} finally {
				// Close the bw
				try {
					if (bw != null) {
						bw.flush();
						bw.close();
					}
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Write all AIMLIF files from bot brain
	 */
	public void writeAIMLIFFiles() {
		if (MagicBooleans.trace_mode) {
			System.out.println("writeAIMLIFFiles");
		}
		final HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
		final Category b = new Category(0, "BRAIN BUILD", "*", "*", new Date().toString(),
				"update.aiml");
		brain.addCategory(b);
		final ArrayList<Category> brainCategories = brain.getCategories();
		Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
		for (final Category c : brainCategories) {
			try {
				BufferedWriter bw;
				final String fileName = c.getFilename();
				if (fileMap.containsKey(fileName)) {
					bw = fileMap.get(fileName);
				} else {
					bw = new BufferedWriter(new FileWriter(aimlif_path + "/" + fileName
							+ MagicStrings.aimlif_file_suffix));
					fileMap.put(fileName, bw);

				}
				bw.write(Category.categoryToIF(c));
				bw.newLine();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
		final Set set = fileMap.keySet();
		for (final Object aSet : set) {
			final BufferedWriter bw = fileMap.get(aSet);
			// Close the bw
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();

			}

		}
		final File dir = new File(aimlif_path);
		dir.setLastModified(new Date().getTime());
	}

	/**
	 * Write all AIML files. Adds categories for BUILD and DEVELOPMENT ENVIRONMENT
	 */
	public void writeAIMLFiles() {
		if (MagicBooleans.trace_mode) {
			System.out.println("writeAIMLFiles");
		}
		final HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
		final Category b = new Category(0, "BRAIN BUILD", "*", "*", new Date().toString(),
				"update.aiml");
		brain.addCategory(b);
		// b = new Category(0, "PROGRAM VERSION", "*", "*", MagicStrings.program_name_version,
		// "update.aiml");
		// brain.addCategory(b);
		final ArrayList<Category> brainCategories = brain.getCategories();
		Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
		for (final Category c : brainCategories) {

			if (!c.getFilename().equals(MagicStrings.null_aiml_file)) {
				try {
					// System.out.println("Writing "+c.getCategoryNumber()+" "+c.inputThatTopic());
					BufferedWriter bw;
					final String fileName = c.getFilename();
					if (fileMap.containsKey(fileName)) {
						bw = fileMap.get(fileName);
					} else {
						final String copyright = Utilities.getCopyright(this, fileName);
						bw = new BufferedWriter(new FileWriter(aiml_path + "/" + fileName));
						fileMap.put(fileName, bw);
						bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + "<aiml>\n");
						bw.write(copyright);
						// bw.newLine();
					}
					bw.write(Category.categoryToAIML(c) + "\n");
					// bw.newLine();
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		final Set set = fileMap.keySet();
		for (final Object aSet : set) {
			final BufferedWriter bw = fileMap.get(aSet);
			// Close the bw
			try {
				if (bw != null) {
					bw.write("</aiml>\n");
					bw.flush();
					bw.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();

			}

		}
		final File dir = new File(aiml_path);
		dir.setLastModified(new Date().getTime());
	}

	/**
	 * load bot properties
	 */
	void addProperties() {
		try {
			properties.getProperties(config_path + "/properties.txt");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * read AIMLIF categories from a file into bot brain
	 *
	 * @param filename
	 *            name of AIMLIF file
	 * @return array list of categories read
	 */
	public ArrayList<Category> readIFCategories(final String filename) {
		final ArrayList<Category> categories = new ArrayList<Category>();
		try {
			// Open the file that is the first
			// command line parameter
			final FileInputStream fstream = new FileInputStream(filename);
			// Get the object
			final BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				try {
					final Category c = Category.IFToCategory(strLine);
					categories.add(c);
				} catch (final Exception ex) {
					System.out.println("Invalid AIMLIF in " + filename + " line " + strLine);
				}
			}
			// Close the input stream
			br.close();
		} catch (final Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return categories;
	}

	/**
	 * Load all AIML Sets
	 */
	int addAIMLSets() {
		int cnt = 0;
		final Timer timer = new Timer();
		timer.start();
		try {
			// Directory path here
			String file;
			final File folder = new File(sets_path);
			if (folder.exists()) {
				final File[] listOfFiles = IOUtils.listFiles(folder);
				if (MagicBooleans.trace_mode) {
					System.out.println("Loading AIML Sets files from " + sets_path);
				}
				for (final File listOfFile : listOfFiles) {
					if (listOfFile.isFile()) {
						file = listOfFile.getName();
						if (file.endsWith(".txt") || file.endsWith(".TXT")) {
							if (MagicBooleans.trace_mode) {
								System.out.println(file);
							}
							final String setName = file.substring(0, file.length() - ".txt".length());
							if (MagicBooleans.trace_mode) {
								System.out.println("Read AIML Set " + setName);
							}
							final AIMLSet aimlSet = new AIMLSet(setName, this);
							cnt += aimlSet.readAIMLSet(this);
							setMap.put(setName, aimlSet);
						}
					}
				}
			} else {
				System.out.println("addAIMLSets: " + sets_path + " does not exist.");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return cnt;
	}

	/**
	 * Load all AIML Maps
	 */
	int addAIMLMaps() {
		int cnt = 0;
		final Timer timer = new Timer();
		timer.start();
		try {
			// Directory path here
			String file;
			final File folder = new File(maps_path);
			if (folder.exists()) {
				final File[] listOfFiles = IOUtils.listFiles(folder);
				if (MagicBooleans.trace_mode) {
					System.out.println("Loading AIML Map files from " + maps_path);
				}
				for (final File listOfFile : listOfFiles) {
					if (listOfFile.isFile()) {
						file = listOfFile.getName();
						if (file.endsWith(".txt") || file.endsWith(".TXT")) {
							if (MagicBooleans.trace_mode) {
								System.out.println(file);
							}
							final String mapName = file.substring(0, file.length() - ".txt".length());
							if (MagicBooleans.trace_mode) {
								System.out.println("Read AIML Map " + mapName);
							}
							final AIMLMap aimlMap = new AIMLMap(mapName, this);
							cnt += aimlMap.readAIMLMap(this);
							mapMap.put(mapName, aimlMap);
						}
					}
				}
			} else {
				System.out.println("addAIMLMaps: " + maps_path + " does not exist.");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return cnt;
	}

	public void deleteLearnfCategories() {
		final ArrayList<Category> learnfCategories = learnfGraph.getCategories();
		for (final Category c : learnfCategories) {
			final Nodemapper n = brain.findNode(c);
			System.out.println("Found node " + n + " for " + c.inputThatTopic());
			if (n != null) {
				n.category = null;
			}
		}
		learnfGraph = new Graphmaster(this);
	}

	public void deleteLearnCategories() {
		final ArrayList<Category> learnCategories = learnGraph.getCategories();
		for (final Category c : learnCategories) {
			final Nodemapper n = brain.findNode(c);
			System.out.println("Found node " + n + " for " + c.inputThatTopic());
			if (n != null) {
				n.category = null;
			}
		}
		learnGraph = new Graphmaster(this);
	}

	/**
	 * check Graphmaster for shadowed categories
	 */
	public void shadowChecker() {
		shadowChecker(brain.root);
	}

	/**
	 * traverse graph and test all categories found in leaf nodes for shadows
	 *
	 * @param node
	 */
	void shadowChecker(final Nodemapper node) {
		if (NodemapperOperator.isLeaf(node)) {
			String input = node.category.getPattern();
			input = brain.replaceBotProperties(input);
			input = input.replace("*", "XXX").replace("_", "XXX").replace("^", "").replace("#", "");
			final String that = node.category.getThat().replace("*", "XXX").replace("_", "XXX")
					.replace("^", "").replace("#", "");
			final String topic = node.category.getTopic().replace("*", "XXX").replace("_", "XXX")
					.replace("^", "").replace("#", "");
			input = instantiateSets(input);
			System.out.println("shadowChecker: input=" + input);
			final Nodemapper match = brain.match(input, that, topic);
			if (match != node) {
				System.out.println("" + Graphmaster.inputThatTopic(input, that, topic));
				System.out.println("MATCHED:     " + match.category.inputThatTopic());
				System.out.println("SHOULD MATCH:" + node.category.inputThatTopic());
			}
		} else {
			for (final String key : NodemapperOperator.keySet(node)) {
				shadowChecker(NodemapperOperator.get(node, key));
			}
		}
	}

	public String instantiateSets(String pattern) {
		final String[] splitPattern = pattern.split(" ");
		pattern = "";
		for (String x : splitPattern) {
			if (x.startsWith("<SET>")) {
				final String setName = AIMLProcessor.trimTag(x, "SET");
				final AIMLSet set = setMap.get(setName);
				if (set != null) {
					x = "FOUNDITEM";
				} else {
					x = "NOTFOUND";
				}
			}
			pattern = pattern + " " + x;
		}
		return pattern.trim();
	}
}
