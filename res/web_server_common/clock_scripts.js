/** Parses the response from a clock request */
function handleResponse(response) {
//	alert(response);
	setInterval(1000);
	if (response == null || response == "") {
		return;
	}
	lines = response.split("\r\n");
	for(i = 0; lines != null && i < lines.length; i++) {
		values = lines[i].split('=');
		if (values != null && values.length == 2 && values[0] == 'Seconds') {
			document.getElementById('Seconds').innerHTML = values[1] + " Sekunden";
		}
		if (values != null && values.length == 2 && values[0] == 'Time') {
			document.getElementById('Time').innerHTML = values[1];
		}
	}
}
