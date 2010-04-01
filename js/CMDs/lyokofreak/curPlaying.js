/* 
 * Reports what VLC is currently playing
 */

var param = 0; //Prevent error with

function invoke() {
	println("Initalizing vlc update");
	println("Checking");
	var netPkgs = new JavaImporter(java.io,java.net);
	with (netPkgs) {
		var url = "http://localhost:8082/current.html";
		println("Visiting url: "+url);

		// Get the response
		var rd = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
		var allLine = "";
		var line = "";
		while ((line = rd.readLine()) != null)
			allLine = allLine+line;
		rd.close();

		println("Done visiting url");
		var current =  allLine;
		qb.sendMessage(channel,current);
	}
}