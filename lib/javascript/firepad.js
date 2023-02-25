//this javascript file is licensed under the MIT license (be careful, the JGraphpad license is more restrictive, see JGraphpad Community Edition for details)
//author: Raphael Valyi; June 2006


function initGlobalContext() {//default parameters shared among all jgraphpad applets (you should pass them as argument to override them!)
	window.all_pads=new Array();
	window.default_pad=new Object();
	
	p=new Array();

	//message fired by the ajax part
	p['tooltipMessage'] ="Click on the diagram to start editing it...";
	p['noDiagramMessage'] ="Click here to create a diagram...";
	p['loadingMessage']="Please wait, I'm still loading; first use is always slow, but further use will be fast!";
	p['downloadLinkMessage']="Download last uploaded diagram";
	p['webstartLinkMessage']="Webstart me"; 

	p['readOnly']=false;

	//default params for the applet part	
	p['id']='editor1';
	p['basename']='diagram1';
	p['archive'] = "jgraphpad.jar,jgraphpad_lazy.jar,library.jar,translations.jar,gpgraph.jar,layouts.jar,console.jar,codecs.jar";
	//hint: codebase should be set to "unpacked" for local use
	p['codebase'] ="unpacked"//"http://wiki.visualmodeller.org/applets/JGraphpadCE";//
	p['mainClass'] = "org.jgraph.pad.jgraphpad.JGraphpad.class";
	p['webstartLink'] = "webstart_demo.jnlp";
	//available modes are: applet, webstart and both
	p['mode'] = "both";
	p['appletAutoPopup'] = true;//true if you want the applet to popup as soon as it's loaded
	
	p['plugins']="no";//no plugin by default
	p['imageUploadPath']="sample.gif";
	p['xmlUploadLPath']='myXmlUploadPath';
	p['xmlDownloadPath']='myXmlDownloadPath';
	p['imageDownloadPath']='myImageDownloadPath';

	p['backgroundcolor'] = "#FFFFFF";
	p['foregroundcolor'] = "#000000";
	
	window.default_params=p;

	if (p['mode'] != "webstart")
		setTimeout('appendAllApplets();' , 200);
}


//************** code to embbed an applet + degraded ajax app on the fly according to the parameters of the HTML page

if (!window.default_pad)
	initGlobalContext();//parameters by default, needs to be initialized ony once
	
//we create the div container
document.write("<center><div id ="+window.id+"></div></center>");
div=document.getElementById(window.id);


//css
document.write("<style id='st0' type='text/css'>");
document.write("div.visible_ajax_div {}");
document.write("div.hidden_ajax_div {height: 0px; overflow:hidden; visibility:hidden; display:none;}");
document.write("div.visible_applet {height: none; overflow:hidden; visibility:visible;width:100%;}");
document.write("div.hidden_applet {height:1px; overflow:hidden; visibility:hidden}");
document.write("div.hidden_loading_indicator{height:1px; overflow:hidden; visibility:hidden;}");
document.write("img.visible_image{border:10px solid #8A8A8A}");
document.write("img.visible_hover_image{border:10px solid yellow}");
document.write("</style>");

window.all_pads.push(new jsAppletPad(window, div));


function appendAllApplets() {for( i in window.all_pads) { window.all_pads[i].appendApplet(); }}
function collapseAllApplets() {for( i in window.all_pads) { window.all_pads[i].toggleAppletHidden(); } document.onclick="";}


function jsAppletPad(e, div) {

	//now we get the parameters from the HTML context or set the default values
	for (var i in e.default_params) {
		this[i]=(e[i] ? e[i] : e.default_params[i]);
		e[i]=null;
	}
	
	//now we cross reference our jsAppletPad with the DOM elements involved by our GUI events (see Ajax in action p136)
	this.blackjax_div=div;
	this.blackjax_div.jsAppletPad=this;
	
	this.image=document.createElement("img");
	mouseOverAjax=function(){ this.className="visible_hover_image"; };
	mouseOutAjax=function(){ this.className="visible_image"; };
	img_id=this.id+"_img";
	//we do the following because else Internet Explorer wrongly resizes the image!
	document.write("<img id="+img_id+" class='visible_image' src="+this.imageDownloadPath+" alt="+this.noDiagramMessage+"></img>");
	this.image=document.getElementById(img_id);
	this.image.onmouseover=mouseOverAjax;
	this.image.onmouseout=mouseOutAjax;
	
	this.loading_indicator=document.createElement("div");
	src=this.codebase+'/lib/javascript/loading.gif';
	this.loading_indicator.innerHTML="<img src="+src+" alt='loading'></img>";
	links_table=document.createElement("div");
	if (this.mode == "applet")
		links_table.innerHTML="<a href="+this.xmlDownloadPath+">"+this.downloadLinkMessage+"</a>";
	else
		links_table.innerHTML="<a href="+this.xmlDownloadPath+">"+this.downloadLinkMessage+"</a> .       . <a href="+this.webstartLink+">"+this.webstartLinkMessage+"</a>";
	
	this.dockable_ajax_div=document.createElement("div");
	this.dockable_ajax_div.className='visible_ajax_div';
	this.blackjax_div.innerHTML=this.tooltipMessage;
	this.blackjax_div.appendChild(this.loading_indicator);
	
	this.blackjax_div.appendChild(links_table);
	this.blackjax_div.appendChild(this.dockable_ajax_div);
	this.dockable_ajax_div.appendChild(this.image);
	this.dockable_ajax_div.jsAppletPad=this;
	
	this.applet_div=document.createElement("div");
	this.applet_div.className='hidden_applet';
	this.applet_div.jsAppletPad=this;
	this.applet_id=this.blackjax_div.id+"_applet";

	
	
	//************** workflow state
	
	this.started=false;
	this.uploading=false;
	this.saved=true;
	
	
	jsAppletPad.prototype.appendApplet=function() {
		width=this.blackjax_div.scrollWidth-5;//adjust the size to what is available for the div container
		this.appletWidth=width+"px";
		this.appletHeight=0.75*width+"px";
		
		this.applet_div.innerHTML="<applet id="+this.applet_id+" codebase="+this.codebase+" "
		+"code = "+this.mainClass+" cache_archive = "+this.archive+" width="+this.appletWidth+" "
		+"height="+this.appletHeight+" MAYSCRIPT backgroundcolor="+this.backgroundcolor+" foregroundcolor="+this.foregroundcolor+" "
		+"basename="+this.basename+" drawpath="+this.xmlDownloadPath+" savepath="+this.xmlUploadLPath+" viewpath="+this.imageUploadPath+" "
		+"plugins="+this.plugins+" readonly="+this.readOnly+"></applet>";
		this.blackjax_div.appendChild(this.applet_div);
	}
	
	
	//************** GUI callbacks
	
	jsAppletPad.prototype.toggleAppletVisible=function(){
		document.onclick="";
		pad=resolvePad(this);
		if (pad.mode == "webstart") {
			document.writte("<iframe src="+pad.webstartLink+"></iframe>");
			return;
		}
		if (!pad.started)
			alert(pad.loadingMessage);
		pad.applet_div.className='visible_applet';
		pad.dockable_ajax_div.className='hidden_ajax_div';
		setTimeout("document.onclick=collapseAllApplets", 100);
	}
	
	jsAppletPad.prototype.toggleAppletHidden=function(){
		pad=resolvePad(this);
		pad.applet_div.className='hidden_applet';
		pad.dockable_ajax_div.className='visible_ajax_div';
	}
	
	this.dockable_ajax_div.onclick=this.toggleAppletVisible;
	this.applet_div.onclick=this.toggleAppletHidden;
	
	
	jsAppletPad.prototype.uploadingStarted=function(){
		this.uploading=true;

		//reload the image as it has been uploaded
		var search="&date="+(new Date()).getTime();
		this.image.src+=search;//we change the src so that the server will ignore the change and return the same image while the image won't be taken from the browser's cache.
		
		//hide the applet again
		this.toggleAppletHidden();
	}
	
}

//utility function used to handle transparently function called by objects and function called by DOM events
function resolvePad(object) { return object.jsAppletPad ? object.jsAppletPad : object; }



//************** applet call backs (we retrieve the ajax app thanks to its id we first passed to the applet)

function getBlackJaxObjectFromAppletID(appletID) {
	return document.getElementById(appletID.replace('_applet', '')).jsAppletPad;
}

//called once the applet is downloaded and init has been called
function started(appletID) {
	jsAppletPad=getBlackJaxObjectFromAppletID(appletID);
	jsAppletPad.started=true;
	jsAppletPad.loading_indicator.className='hidden_loading_indicator';
	if (jsAppletPad.appletAutoPopup)
		jsAppletPad.toggleAppletVisible();
}

function clientChanged(appletID) {
	jsAppletPad=getBlackJaxObjectFromAppletID(appletID);
	jsAppletPad.saved=false;
}

//since the upload may last for some time, we already provide visual feedback to the user
function uploadCalled(appletID) {
	jsAppletPad=getBlackJaxObjectFromAppletID(appletID);
	jsAppletPad.loading_indicator.className='';
}

//called when the image has been uploaded, we can already close the editor as uploading the XML file will take more time due to a Sun bug
function uploading(appletID) {
	jsAppletPad.loading_indicator.className='';
	jsAppletPad=getBlackJaxObjectFromAppletID(appletID);
	jsAppletPad.toggleAppletHidden();
	jsAppletPad.image.src+="&date="+(new Date()).getTime();//force the image reloading
}

//called when the diagram has been fully uploaded (user can now quit the page safely)
function uploaded(appletID) {
	jsAppletPad=getBlackJaxObjectFromAppletID(appletID);
	jsAppletPad.saved=true;
	jsAppletPad.uploading = false;
	jsAppletPad.loading_indicator.className='hidden_loading_indicator';
}
