/* 
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var listener = true;

function invoke() {
	if(msgInfo.channel=="#lyokofreak-viewing-party" && !msgInfo.isBot()) {
		var prefix = "ustream";
		var msg_suffix = "";
		if(msgInfo.sender.substr(0,prefix.length)==prefix) {
			msg_suffix = " (please change nick with /nick yournickhere)"
		}
		qb.sendMsg(new BotMessage(msgInfo,"Welcome to the LyokoFreak Viewing Party"+msg_suffix));
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
