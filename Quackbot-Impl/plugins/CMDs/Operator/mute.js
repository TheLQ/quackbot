var help = "Mutes user for specified number of minuites. Syntax: ?mute <username> <time in minuites>";
var parameter = 2;
var admin = true;

function invoke(user,timeMin) {
	var timeMs = timeMin*60000;
	var hostmask = user+"!*@*"
	qb.ban(event.channel,hostmask);
	qb.sendMsg(new BotMessage(event,user+": You have been muted for "+timeMin));
	try {
		log.debug("Sleeping");
		Thread.sleep(timeMs);
		qb.unBan(event.channel,hostmask);
		qb.sendMsg(new BotMessage(event,user+" has been unmuted"));
	}
	catch(err) {
		println("ERROR "+err);
		if(err.toString().search("InterruptedException") != -1) {
			log.warn("mute thread interupted on hostmask "+hostmask);
			return;
		}
	}
}