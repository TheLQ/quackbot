/* 
 * For the lyokofreak stream, tells users who can't see it that a new person has joined
 */

var param = 0; //Prevent NPE error with loadCMDs

function invoke() {
    if(channel=="#quackbot" && sender != qb.getNick()) {
	qb.sendMessage(channel,"User "+sender+" has joined "+channel);
    }
}
