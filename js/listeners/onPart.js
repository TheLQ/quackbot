/*
 * For the lyokofreak stream, tells users who can't see it that a new person has parted
 */

var listener = true;

function invoke() {
	if(msgInfo.channel=="#lyokofreak-viewing-party" && !msgInfo.isBot()) {
		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+msgInfo.sender+" has parted"));
	}
}
