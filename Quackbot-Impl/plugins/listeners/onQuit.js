/*
 * For the lyokofreak stream, tells users who can't see it that a new person has quit
 */

var listener = Hooks.onQuit;

function invoke() {
	if(msgInfo.sender != qb.getNick()) {
		//Yes this is blind, but there is no way to tell what channel the user was on
		qb.sendMsg(new BotMessage("#lyokofreak-viewing-party","User "+msgInfo.sender+" has quit"));
	}
}
