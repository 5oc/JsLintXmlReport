(function () {

	var report = "",
			options = {
				rhino: true,
				passfail: false,
				maxerr: 100,
				indent: 4
			};

	function addToReport(text) {
		report += text + "\n";
	}

	function lintAllFiles() {
		var i, file;

		for (i = 0; i < jsFiles.length; i += 1) {
			file = jsFiles[i];
			addToReport('\t<file name="' + file + '">');
			lintFile(file);
			addToReport('\t</file>');
		}
	}

	function escapeForXml(currentError) {
		if (currentError.reason) {
			currentError.reason = currentError.reason.replace(/"/g, '\"');
		}
		if (currentError.evidence) {
			currentError.evidence = currentError.evidence.replace(/"/g, '\"');
		}
	}

	function reportErrors() {
		var i, currentError;

		for (i = 0; i < JSLINT.errors.length; i += 1) {
			currentError = JSLINT.errors[i];

			if (currentError) {
				escapeForXml(currentError);
				addToReport('\t\t<issue char="' + currentError.character + '" evidence="' + currentError.evidence + '" line="' + currentError.line + '" reason="' + currentError.reason + '" />"');
			} else {

				addToReport('\t\t<issue char="" evidence="" line="" reason="fatal violation detected" />');
			}
		}
	}

	function readFile(fileName) {
		var jq, reader, line, lines;

		jq = new java.io.File(fileName);
		reader = new java.io.BufferedReader(new java.io.FileReader(jq));
		line = null;
		lines = "";

		while ((line = reader.readLine()) != null) {
			lines += line + "\n";
		}

		return lines;
	}

	function lintFile(fileName) {
		var good;

		good = JSLINT(readFile(fileName), options);

		if (!good) {
			reportErrors();
		}
	}

	function run() {
		addToReport('<?xml version="1.0" encoding="UTF-8" ?>');
		addToReport('<jslint>');
		lintAllFiles();
		addToReport('</jslint>');
		printStream.print(report);
	}

	run();

}());