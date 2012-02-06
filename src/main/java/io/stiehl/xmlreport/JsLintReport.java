package io.stiehl.xmlreport;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class JsLintReport {
	private static Logger LOGGER = Logger.getLogger(JsLintReport.class);

	private Context context;
	private Collection<File> jsFiles;
	private ByteArrayOutputStream outputStream;
	private PrintStream printStream;

	public static void main(String[] args) throws IOException {
		String filesPath = "./";
		if (args.length > 0) {
			filesPath = args[0];
		}

		new JsLintReport(filesPath);
	}

	public JsLintReport(String filesPath) throws IOException {
		long startTimeStamp = new Date().getTime();
		LOGGER.info("starting with filesPath: " + filesPath);

		initStreams();
		findJsFiles(filesPath);
		printXmlHead();
		lintJSFiles(initContext());
		writeReport(outputStream.toString());
		
		LOGGER.info("ready within: " + ((new Date().getTime() - startTimeStamp) / 1000) + "sec");
	}

	private void printXmlHead() {
		printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		printStream.println("<jslint date=\"" + new Date() + "\" files=\"" + jsFiles.size() + "\">");
	}

	private void findJsFiles(String filesPath) {
		jsFiles = new ArrayList<File>();
		findJsFilesRecursively(new File(filesPath), jsFiles);
		LOGGER.info(jsFiles.size() + " js files found");
	}

	private String readFile(File file) throws IOException {
		return FileUtils.readFileToString(file, "UTF-8");
	}

	private void lintJSFiles(ScriptableObject scriptable) throws IOException {
		for (File jsFile : jsFiles) {
			String jsFileName = jsFile.getPath();
			LOGGER.info("evaluating script: " + jsFileName);

			putObjectIntoJsContext(jsFileName, "jsFileName", scriptable);
			putObjectIntoJsContext(readFile(jsFile), "jsFileAsString", scriptable);
			evalJsLintRunFile(scriptable);
		}

		Context.exit();
		printStream.println("</jslint>");
	}

	private void evalJsLintRunFile(ScriptableObject scriptable) {
		context.evaluateString(scriptable, "S.runJsLint();", "call", 1, null);
	}

	private void putObjectIntoJsContext(Object object, String name, ScriptableObject scriptable) {
		Scriptable extScriptable = Context.toObject(object, scriptable);
		scriptable.put(name, scriptable, extScriptable);
	}

	private ScriptableObject initContext() throws IOException {
		context = Context.enter();
		ScriptableObject scriptable = context.initStandardObjects();

		Scriptable scStream = Context.toObject(printStream, scriptable);
		scriptable.put("printStream", scriptable, scStream);

		String jslint = getFileAsString("jslint.js");
		String runLint = getFileAsString("run.js");
		context.evaluateString(scriptable, jslint, "jslint.js", 1, null);
		context.evaluateString(scriptable, runLint, "run.js", 1, null);

		return scriptable;
	}

	private void writeReport(String text) throws IOException {
		File reportFile = new File("report.xml");
		LOGGER.info("writing report to: " + reportFile.getPath());
		FileUtils.writeStringToFile(reportFile, text);
	}

	protected String getFileAsString(String fileName) throws IOException {
		URL resource = getClass().getClassLoader().getResource(fileName);
		return FileUtils.readFileToString(new File(resource.getPath()), "UTF-8");
	}

	private void initStreams() {
		outputStream = new ByteArrayOutputStream();
		printStream = new PrintStream(outputStream);
	}

	private static void findJsFilesRecursively(File file, Collection<File> all) {
		final File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				String fileName = child.getName();
				if (fileName.toLowerCase().endsWith(".js") && !fileName.contains("-min")) {
					all.add(child);
					LOGGER.info("add file for analyses: " + child.getPath());
				}
				findJsFilesRecursively(child, all);
			}
		}
	}
}
