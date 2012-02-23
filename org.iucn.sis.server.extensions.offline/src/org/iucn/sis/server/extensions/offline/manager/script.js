function getScrollY() {
	var scrOfX = 0, scrOfY = 0;
	if (typeof (window.pageYOffset) == 'number') {
		// Netscape compliant
		scrOfY = window.pageYOffset;
		scrOfX = window.pageXOffset;
	} else if (document.body
			&& (document.body.scrollLeft || document.body.scrollTop)) {
		// DOM compliant
		scrOfY = document.body.scrollTop;
		scrOfX = document.body.scrollLeft;
	} else if (document.documentElement
			&& (document.documentElement.scrollLeft || document.documentElement.scrollTop)) {
		// IE6 standards compliant mode
		scrOfY = document.documentElement.scrollTop;
		scrOfX = document.documentElement.scrollLeft;
	}
	return scrOfY;
}

function getWidth() {
	var myWidth = 0, myHeight = 0;
	if (typeof (window.innerWidth) == 'number') {
		// Non-IE
		myWidth = window.innerWidth;
		myHeight = window.innerHeight;
	} else if (document.documentElement
			&& (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
		// IE 6+ in 'standards compliant mode'
		myWidth = document.documentElement.clientWidth;
		myHeight = document.documentElement.clientHeight;
	} else if (document.body
			&& (document.body.clientWidth || document.body.clientHeight)) {
		// IE 4 compatible
		myWidth = document.body.clientWidth;
		myHeight = document.body.clientHeight;
	}
	return myWidth;
}

function getHeight() {
	var myWidth = 0, myHeight = 0;
	if (typeof (window.innerWidth) == 'number') {
		// Non-IE
		myWidth = window.innerWidth;
		myHeight = window.innerHeight;
	} else if (document.documentElement
			&& (document.documentElement.clientWidth || document.documentElement.clientHeight)) {
		// IE 6+ in 'standards compliant mode'
		myWidth = document.documentElement.clientWidth;
		myHeight = document.documentElement.clientHeight;
	} else if (document.body
			&& (document.body.clientWidth || document.body.clientHeight)) {
		// IE 4 compatible
		myWidth = document.body.clientWidth;
		myHeight = document.body.clientHeight;
	}
	return myHeight;
}

function expose() {
	document.getElementById('loadingArea').style.top = "" + (getScrollY() + 200)
			+ "px";
	document.getElementById('loadingArea').style.left = ""
			+ ((getWidth() / 2) - 220) + "px";
	document.getElementById('loadingArea').style.display = "block";
}

function showLoadingScreen() {
	document.getElementById('veil').style.width = ""
			+ document.documentElement.scrollWidth + "px";
	document.getElementById('veil').style.height = ""
			+ document.documentElement.scrollHeight + "px";
	document.getElementById('veil').style.display = "block";
	document.getElementById('loadingText').innerHTML = "Submitting, please wait...";
	setTimeout("expose()", 100);
	
	return true;
}

function hideLoadingScreen() {
	document.getElementById('loadingArea').style.display = "none";
	document.getElementById('veil').style.display = "none";
	document.getElementById('loadingText').innerHTML = "";
}