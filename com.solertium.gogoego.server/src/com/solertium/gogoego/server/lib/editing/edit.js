function gogo_gX(e){return gogo_gXY(e,0)};
function gogo_gY(e){return gogo_gXY(e,1)};
function gogo_gXY(e,f){c=0;while(e.offsetParent){c+=(f==0)?e.offsetLeft:e.offsetTop;e=e.offsetParent};return c};

function gogo_px(n){
  return(typeof n=='string')?n:n+'px'
}

var gogo_eid = 0;
var gogo_spots = new Array();

var offsetX = 20;
var offsetY = 20;

var veil;
var spot;

function renderVeil() {
  veil = document.createElement("div");
  veil.id = "veil";
  veil.style.zIndex = "2998";
  veil.style.border = "0px solid black";
  veil.style.width = ""+document.documentElement.scrollWidth+"px";
  veil.style.height = ""+document.documentElement.scrollHeight+"px";
  veil.style.backgroundColor = "#000000";
  veil.style.position = "absolute";
  veil.style.left = 0;
  veil.style.top = 0;
  veil.style.filter = "alpha(opacity=15)";
  veil.style.opacity = "0.15";
  
  document.body.appendChild(veil);
}

function createPMDiv(s) {
  var pmdiv = document.createElement("div");
  gogo_eid++;
	    
  var eid = "eid"+gogo_eid;
	    
  pmdiv.id = eid;
  pmdiv.style.fontFamily = "Tahoma,Verdana,Arial,sans-serif";
  pmdiv.style.fontSize = "9px";
  pmdiv.style.fontWeight = "bold";
  pmdiv.style.textDecoration = "none";
  pmdiv.style.position = "absolute";
  pmdiv.style.left = ""+gogo_gX(s)+"px";
  pmdiv.style.top = ""+gogo_gY(s)+"px";
  pmdiv.style.background = "white";
  pmdiv.style.color = "#505050";
  pmdiv.style.zIndex = "3000";
  pmdiv.style.border = "1px solid #c0c0c0";
  pmdiv.style.cursor = "pointer";
  pmdiv.style.padding = "2px";
  
  return pmdiv;
}

function gogo_initWYSIWYG() {
  var count = 0;

  var nl = document.getElementsByTagName("gogo:wysiwyg");
  for (var i = 0; i < nl.length; i++) {
    var el = nl.item(i);
    var s = el.parentNode;
    if (s != null) {
        var pmdiv = createPMDiv(s);
        var desc = el.getAttribute("description");
        if (desc == null)
            desc = "";
        pmdiv.innerHTML = "<img src=\"/admin/images/g.png\" style=\"vertical-align:middle; display:inline; border:none\"/> edit " + desc;
        
        pmdiv.onclick = new Function(
        	"gogo_doEditInnerWYSIWYG(\"" + el.getAttribute("id") + "\",\"" + el.getAttribute("src") + "\");"
        );
        
        document.body.appendChild(pmdiv);
        count++;
    }
  }
  
  return count;
}

function gogo_initEdit(){
  renderVeil();
  
  var count = gogo_initWYSIWYG();
  if (count > 0)
  	return;

  var nl = document.getElementsByTagName("gogo:marker");
  for (var i=0;i<nl.length;i++) {
    var el = nl.item(i);
    var s = el.parentNode;
    if(s!=null){
	    var pmdiv = createPMDiv(s);
	    
	    var desc = el.getAttribute("description");
	    if (desc == null)
	    	desc = "";
	    
	    pmdiv.innerHTML = "<img src=\"/admin/images/g.png\" style=\"vertical-align:middle; display:inline; border:none\"/> edit " + desc;
	
	    // dynamically create the click function to avoid later lookups
		pmdiv.onclick = new Function(
			"gogo_doEditInner(\""+el.getAttribute("gclass")+"\",\""+el.getAttribute("src")+"\",\""+el.getAttribute("id")+"\");"
		);
	    
	    document.body.appendChild(pmdiv);
    }
  }
}

function gogo_doEditInner(markerclass, src, id) {
	var body = "body";
	if (id != null && id != "null")
		body = id;
	try {
		window.parent.gogo_doEdit(markerclass, src, body);
	} catch (e) { }
}

function gogo_doEditInnerWYSIWYG(tag, src) {
	try {
		window.parent.gogo_doEditWYSIWYG(tag, src);
	} catch (e) { }
}