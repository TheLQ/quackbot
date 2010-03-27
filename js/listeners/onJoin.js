/* 
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var param = 0; //Prevent NPE error with loadCMDs

function invoke() {
    if(channel=="#lyokofreak-viewing-party" && sender != qb.getNick()) {
	var prefix = "ustream";
	var msg_suffix = "";
	if(sender.substr(0,prefix.length)==prefix) {
		msg_suffix = " (please change nick with /nick yournickhere)"
	}
	qb.sendMessage(channel,"User "+sender+" has joined "+channel+msg_suffix);
	/*	var netPkgs = new JavaImporter(java.io,java.net);
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
			    qb.sendMessage(channel,sender+": "+allLine);
		}*/
    }
}
