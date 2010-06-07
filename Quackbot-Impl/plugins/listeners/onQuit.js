/*
 * For the lyokofreak stream, tells users who can't see it that a new person has quit
 */

var hook = Hooks.onQuit;

function invoke() {
log.debug("Channel: "+msgInfo.channel+" | Bot: "+qb.isBot(msgInfo)+" | Result: "+(qb.isBot(msgInfo) && qb.getServer().indexOf("ustream") != -1));
	if(qb.isBot(msgInfo) && qb.getServer().indexOf("ustream") != -1) {
		//Yes this is blind, but there is no way to tell what channel the user was on
		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+msgInfo.sender+" has quit"));
	}
}
