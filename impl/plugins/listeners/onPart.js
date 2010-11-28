/*
 * For the lyokofreak stream, tells users who can't see it that a new person has parted
 */

var hook = Event.onPart;
var ignore = true;

function invoke() {
//log.debug("Channel: "+event.channel+" | Bot: "+qb.isBot(event)+" | Result: "+(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event)));
//	if(event.channel=="#lyokofreak-viewing-party" && !qb.isBot(event) && qb.getServer().indexOf("ustream") != -1) {
//		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+event.sender+" has parted"));
//	}
}
