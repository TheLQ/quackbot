/*
 * For the lyokofreak stream, tells users who can't see it that a new person has quit
 */

var hook = Event.onQuit;

function invoke() {
//log.debug("Channel: "+event.channel+" | Bot: "+qb.isBot(event)+" | Result: "+(qb.isBot(event) && qb.getServer().indexOf("ustream") != -1));
//	if(qb.isBot(event) && qb.getServer().indexOf("ustream") != -1) {
		//Yes this is blind, but there is no way to tell what channel the user was on
		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+event.sender+" has quit"));
//	}
}
