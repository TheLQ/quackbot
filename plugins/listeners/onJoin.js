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
		var current =  webTalk("http://localhost:8082/current.html");
		if(current == null) {
			log.warn("Couldn't report to new user whats playing (not connected?)");
			return;
		}
		qb.sendMsg(new BotMessage(msgInfo,"Current song: "+current));
	}
}
