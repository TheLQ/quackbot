/* 
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var hook = Event.onJoin;

function invoke() {
	log.debug("JS Result: "+event.toString());
	//log.debug("Channel: "+event.channel+" | Name: "+event.sender+" | Bot: "+qb.isBot(event)+" | Result: "+(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event)));
	if(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event) && qb.getServer().indexOf("ustream") != -1) {
		var prefix = "ustream";
		var msg_suffix = "";
		if(event.sender.substr(0,prefix.length)==prefix) {
			msg_suffix = " (please change nick with /nick yournickhere)"
		}
		qb.sendMsg(new BotMessage(event.getChannel(),"Welcome to the LyokoFreak Viewing Party "+event.getSender()+" "+msg_suffix));
		var current =  whatsPlaying();
		if(current.indexOf("ERROR") == -1)
			qb.sendMsg(new BotMessage(event,"Current song: "+whatsPlaying()));
		
	}
}
