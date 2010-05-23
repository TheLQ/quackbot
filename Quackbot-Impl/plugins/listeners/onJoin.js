/* 
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var hook = Hooks.onJoin;

function invoke() {
	log.debug("Channel: "+msgInfo.channel+" | Bot: "+qb.isBot(msgInfo)+" | Result: "+(msgInfo.channel=="#lyokofreak-viewing-party" && !qb.isBot(msgInfo)));
	if(msgInfo.channel=="#lyokofreak-viewing-party" && !qb.isBot(msgInfo) && qb.getServer().indexOf("ustream") != -1) {
		var prefix = "ustream";
		var msg_suffix = "";
		if(msgInfo.sender.substr(0,prefix.length)==prefix) {
			msg_suffix = " (please change nick with /nick yournickhere)"
		}
		qb.sendMsg(new BotMessage(msgInfo.getChannel(),"Welcome to the LyokoFreak Viewing Party "+msgInfo.getSender()+" "+msg_suffix));
		var current =  webTalk("http://localhost:8082/current.html");
		if(current == null) {
			log.warn("Couldn't report to new user whats playing (not connected?)");
			return;
		}
		qb.sendMsg(new BotMessage(msgInfo,"Current song: "+current));
	}
}
