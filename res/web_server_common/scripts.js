var resourceURLs = null;
var urlIndex = 0;
var httpRequest = null;
// interval between requests
var interval = 3000;

window.setTimeout(getResource, 100);

/** Initializes the HTTP request object */
function getRequestObject() {
	if(navigator.appName == "Microsoft Internet Explorer") {
		return new ActiveXObject("Msxml2.XMLHTTP");
	} else {
		return new XMLHttpRequest();
	} 
}

/** Reads a specific resource */
function getResource() {
 	if (resourceURLs == null) {
 		return;
 	}
 	if (httpRequest != null) {
 		window.setTimeout(getResource, 500);
 		return;
 	}

	// check all stored URLs
	httpRequest = getRequestObject();
	if (!httpRequest) {
		window.setTimeout(getResource, 5000);
		return;
	}

	try {
		httpRequest.open("GET", resourceURLs[urlIndex], true);
		httpRequest.onreadystatechange=function() {
			if(httpRequest.readyState == 4 && httpRequest.status == 200) {
				// located in another, device-specific .js file 
		    	handleResponse(httpRequest.responseText);
			}
			if(httpRequest.readyState == 4) {
				httpRequest = null;
			}
		}
		httpRequest.send(null);
	} catch (e) {
	}
	urlIndex++;
	if (urlIndex == resourceURLs.length) {
		// start from the beginning
		window.setTimeout(getResource, interval);
		urlIndex = 0;
	} else { 
		window.setTimeout(getResource, 100);
	}	
}

function setResourceURLs(resourceURLs) {
	this.resourceURLs = resourceURLs;
	// start immediately
	window.setTimeout(getResource, 100);	
}

function setInterval(interval) {
	this.interval = interval;
}