function addGoGoEventListeners() {
  if( document.captureEvents && Event.CLICK ) {
 	  document.captureEvents( Event.CLICK );
  }
  document.onclick = alertClick;
}

function findWrappingAnchorTag(node, recursionKillCount) {
   return (node == null || recursionKillCount >= 10) ? 
     null : (node.nodeName == "a" || node.nodeName == "A") ? 
     node : findWrappingAnchorTag(node.parentNode, recursionKillCount+1);
}

function alertClick(e) {
	var targ = null;
	try {
		if (e.target) 
			targ = e.target;
	} catch (e) { }
	try {
		if (targ == null && e.srcElement) 
			targ = e.srcElement;
	} catch (e) { }
	if (targ.nodeType == 3)
		targ = targ.parentNode;
		
	targ = findWrappingAnchorTag(targ, 0);
  	  
	if (targ != null && targ.hasAttribute("href")) {	
		var hostdomain = window.location.hostname;
		var link = targ.getAttribute("href");
		var index = link.indexOf("http");		
		if (index == -1) {
			//Relative links are always good.
	   		window.parent.gogo_addToHistory(link);
	   	}
	   	else {
	   		//STOP!!!
			if (e.returnValue)
		   		e.returnValue = false;
			if (e.cancelBubble)
				e.cancelBubble = true;
		   		
			if (e.preventDefault)
				e.preventDefault();
			if (e.stopPropogation)
				e.stopPropogation();
		   	
		   	/*
		   	* Let GoGoEgo interrogate the absolute url and determine 
		   	* if it is in this site's domain, or if we should pop 
		   	* open a new window so the user can go to this link, 
		   	* should the user actually mean to go to the link. 
		   	*/
		   	window.parent.gogo_Interrogate(link, hostdomain);
		}  
	}
}