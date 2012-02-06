var S = S || {};
S.runJsLint = function () {

	var report = "",
		jsLintOptions = {
			bitwise: true, undef: true, unparam: true, sloppy: true, eqeq: true, vars: true,
			white: true, forin: true, passfail: false, nomen: true, plusplus: true,
			maxerr: 100, indent: 4
		};

	function addToReport(text) {
		report += text + "\n";
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
		addToReport('\t<file name="' + fileName + '" errorCount="' + errorCount + '">');
		for (i = 0; i < errorCount; i += 1) {
			currentError = JSLINT.errors[i];

			if (currentError) {
				escapeForXml(currentError);
				currentError.evidence = currentError.evidence || "";
				addToReport('\t\t<issue line="' + currentError.line + '" char="' + currentError.character + '" reason="' + currentError.reason + '" evidence="' + currentError.evidence + '" />');
			}
		}
		addToReport('\t</file>');
	}

	function lintFile(jsFile, fileName) {
		JSLINT(jsFile, jsLintOptions);
		reportErrors(fileName);
	}

	function run() {
		var jsFile, fileName;
		
		jsFile = jsFileAsString + "\n"; //force to cast to js-string-object
		fileName = jsFileName;

		lintFile(jsFile, fileName);
		printStream.print(report);
	}


	run();
};
