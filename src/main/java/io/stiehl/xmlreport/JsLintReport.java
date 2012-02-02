package io.stiehl.xmlreport;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class JsLintReport {
	private static final String JS_FILES_PATH = "/Users/5oc/JavaApps/JsLintXmlReport/files/";
	private static Logger LOGGER = Logger.getLogger(JsLintReport.class);
	private Scriptable scriptable;

	public static void main(String[] args) throws IOException {
		new JsLintReport();
	}

	public JsLintReport() throws IOException {
		LOGGER.info("starting");
		Context context = initContext();
		initSourceFiles();
		ByteArrayOutputStream outputStream = addPrintStream();
		evaluateScripts(context);
		writeStringToFile(outputStream.toString());
	}

	private void initSourceFiles() {
		final Collection<File> files = new ArrayList<File>();
		findJsFilesRecursively(new File(JS_FILES_PATH), files);
		Scriptable jsFiles = Context.toObject(files.toArray(), scriptable);
		scriptable.put("jsFiles", scriptable, jsFiles);
	}

	private Context initContext() {
		Context context = Context.enter();
		context.setLocale(Locale.GERMANY);
		scriptable = context.initStandardObjects();
		return context;
	}

	private void evaluateScripts(Context context) throws IOException {
		String jslint = getFileAsString("jslint.js");
		String run = getFileAsString("run.js");
		
		context.evaluateString(scriptable, jslint, "jslint.js", 1, null);
		context.evaluateString(scriptable, run, "run.js", 1, null);
	}

	private void writeStringToFile(String text) throws IOException {
		File reportFile = new File("report.xml");
		LOGGER.info("writing report to: " + reportFile.getPath());
		FileUtils.writeStringToFile(reportFile, text);
	}

	protected String getFileAsString(String fileName) throws IOException {
		URL resource = this.getClass().getClassLoader().getResource(fileName);
		return FileUtils.readFileToString(new File(resource.getPath()), "UTF-8");
	}

	private ByteArrayOutputStream addPrintStream() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(buff);
		Scriptable stream = Context.toObject(printStream, scriptable);
		scriptable.put("printStream", scriptable, stream);

		return buff;
	}

	private static void findJsFilesRecursively(File file, Collection<File> all) {
		final File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.getName().toLowerCase().endsWith(".js")) {
					all.add(child);
					LOGGER.info("add file for analyses: " + child.getPath());
				}
				findJsFilesRecursively(child, all);
			}
		}
	}
}
