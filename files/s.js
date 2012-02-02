/*jslint browser: true, sloppy: true, indent: 4 */

var S = {
	forEach: function (elements, fn) {
		var i;
		
		for (i = 0; i < elements.length; i += 1) {
			fn(elements[i]);
		}
	},
	getByClassName: function (tagName, className) {
		var containers = document.getElementsByTagName(tagName),
			containerSelection = [];
		
		S.forEach(containers, function (container) {
			if (container.getAttribute("class") === className) {
				containerSelection.push(container);
			}
		});
		
		return containerSelection;
	},
	renderScript: function (postScript) {
		var scriptTextNode, innerText;
		
		function replacePlaceholder(text) {
			text = text.replace(/\/\*\[/g, "");// /*[
			text = text.replace(/\]\*\//g, "");// ]*/
			
			return text;
		}
		
		function cleanPost(text) {
			text = text.replace(/\n/, "");
			text = text.replace("// <![CDATA[", "");
			text = text.replace("// ]]&gt;", "");		
			
			return text;
		}
		
		if (postScript) {
			innerText = postScript.innerHTML;
			innerText = replacePlaceholder(innerText);
			innerText = cleanPost(innerText);
			innerText = "<code>" + innerText + "</code>";
			
			scriptTextNode = document.createElement("pre");
			scriptTextNode.innerHTML = innerText;
			postScript.parentNode.appendChild(scriptTextNode);
		}
	},
	get: function (elementId) {
		return document.getElementById(elementId);
	}
};

window.onload = function () {
	S.forEach(S.getByClassName("script", "postMe"), function (postScript) {
		S.renderScript(postScript);
	});
};