/*
 * For the lyokofreak stream, tells users who can't see it that a new person has quit
 */

var param = 0; //Prevent NPE error with loadCMDs

function invoke() {
    if(sender != qb.getNick()) {
	//Yes this is blind, but there is no way to tell what channel the user was on
	qb.sendMessage("#quackbot","User "+sender+" has quit");
    }
}
