var lastValue = 0;
var changeCounter = 0;

/** Parses the response from a brightness request */
function handleResponse(response) {
	if (response == null || response == "") {
		return;
	}
	lines = response.split("\r\n");
	for(i = 0; lines != null && i < lines.length; i++) {
		values = lines[i].split('=');
		if (values != null && values.length == 2 && values[0] == 'CurrentBrightness') {
			// decrease fast detection counter
			if (changeCounter > 0) changeCounter--;
			if (lastValue != values[1]) {
				changeCounter = 3;
			}			
			lastValue = values[1]; 
			
			if (changeCounter != 0) {
				setInterval(1000);
			} else {
				setInterval(5000);
			}
		
			document.getElementById('CurrentBrightness').innerHTML = values[1] + "%";
		}
	}
}
