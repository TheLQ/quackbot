/* 
 * Tells user whats playing
 */

var param = 0; //Prevent error with

function invoke() {
	log.debug("Initalizing vlc update");
	qb.sendMsg(new BotMessage(event,whatsPlaying()));
}