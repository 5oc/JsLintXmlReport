package io.stiehl.xmlreport;


import org.apache.commons.io.FileUtils;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class JsLintReport {
	private static final String BASE_PATH = "/Users/5oc/JavaApps/JsLintXmlReport/";
	private static final String RESOURCE_PATH = BASE_PATH + "resources/";
	private static final String JS_FILES_PATH = BASE_PATH + "files/";
	private static final String REPORT_PATH = BASE_PATH + "report/";
	private Scriptable scriptable;

	public static void main(String[] args) throws IOException {
		new JsLintReport();
	}


	public JsLintReport() throws IOException {
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
		String jslint = getFileAsString(RESOURCE_PATH + "jslint.js");
		String run = getFileAsString(RESOURCE_PATH + "run.js");
		
		context.evaluateString(scriptable, jslint, "jslint.js", 1, null);
		context.evaluateString(scriptable, run, "run.js", 1, null);
	}

	private void writeStringToFile(String text) throws IOException {
		File reportFile = new File(REPORT_PATH + "report.xml");
		FileUtils.writeStringToFile(reportFile, text);
	}

	protected String getFileAsString(String path) throws IOException {
		return FileUtils.readFileToString(new File(path), "UTF-8");
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
				}
				findJsFilesRecursively(child, all);
			}
		}
	}
}
