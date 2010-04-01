var help = "Mutes user for specified number of minuites. Syntax: ?mute <username> <time in minuites>";
var param = 2;

function invoke(user,timeMin) {
    var timeMs = timeMin*60000;
    var hostmask = "user!*@*"
    qb.ban(channel,hostmask);
    qb.sendMessage(channel,user+": You have been muted for "+timeMin);
    try {
	log("Sleeping");
	Thread.sleep(timeMs);
	qb.unBan(channel,hostmask);
	qb.sendMessage(channel,user+" has been unmuted");
    }
    catch(err) {
	println("ERROR "+err);
	if(err.toString().search("InterruptedException") != -1) {
	    log("Warning, mute thread interupted on hostmask "+hostmask);
	    return;
	}
    }
}