/*
 * For the lyokofreak stream, tells users who can't see it that a new person has parted
 */

var listener = true;

function invoke() {
	if(channel=="#lyokofreak-viewing-party" && sender != qb.getNick()) {
		qb.sendMessage(channel,"User "+sender+" has parted "+channel);
	}
}
