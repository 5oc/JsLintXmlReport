(function () {

	var report = "",
		apacheIo,
		jsLintOptions = {
			bitwise: true, undef: true, unparam: true, sloppy: true, eqeq: true, vars: true,
			white: true, forin: true, passfail: false, nomen: true, plusplus: true,
			maxerr: 100, indent: 4
		};

	function addToReport(text) {
		report += text + "\n";
	}

	function lintAllFiles() {
		var i;

		for (i = 0; i < jsFiles.length; i += 1) {
			lintFile(jsFiles[i]);
		}
	}

	function escapeForXml(currentError) {
		if (currentError.reason) {
			currentError.reason = currentError.reason.replace(/"/g, "'");
		}
		if (currentError.evidence) {
			currentError.evidence = currentError.evidence.replace(/"/g, "'");
		}
	}

	function reportErrors(fileName) {
		var i, currentError, errorCount;

		errorCount = JSLINT.errors.length;
		addToReport('\t<file name="' + fileName + '" errorCount="' + (errorCount - 1) + '">');
		for (i = 0; i < errorCount; i += 1) {
			currentError = JSLINT.errors[i];

			if (currentError) {
				escapeForXml(currentError);
				addToReport('\t\t<issue char="' + currentError.character + '" evidence="' + currentError.evidence + '" line="' + currentError.line + '" reason="' + currentError.reason + '" />');
			}
		}
		addToReport('\t</file>');
	}

	function readFile(fileName) {
		var file, lines;

		file = new java.io.File(fileName);
		lines = apacheIo.FileUtils.readFileToString(file);
		lines = lines + ""; //to js string

		return lines;
	}

	function lintFile(fileName) {
		var good = JSLINT(readFile(fileName), jsLintOptions);

		if (!good) {
			reportErrors(fileName);
		}
	}

	function run() {
		apacheIo = JavaImporter(Packages.org.apache.commons.io);

		addToReport('<?xml version="1.0" encoding="UTF-8" ?>');
		addToReport('<jslint date="' + new Date() + '">');
		lintAllFiles();
		addToReport('</jslint>');
		printStream.print(report);
	}

	run();

}());