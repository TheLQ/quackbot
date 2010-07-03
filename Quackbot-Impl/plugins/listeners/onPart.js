/*
 * For the lyokofreak stream, tells users who can't see it that a new person has parted
 */

var hook = Event.onPart;
var ignore = true;

function invoke() {
//log.debug("Channel: "+msgInfo.channel+" | Bot: "+qb.isBot(msgInfo)+" | Result: "+(msgInfo.channel=="#lyokofreak-viewing-party" && !qb.isBot(msgInfo)));
//	if(msgInfo.channel=="#lyokofreak-viewing-party" && !qb.isBot(msgInfo) && qb.getServer().indexOf("ustream") != -1) {
//		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+msgInfo.sender+" has parted"));
//	}
}
