var help = "Joins a channel on current server. Syntax: ?joinChan <channel>";
var parameters = 1;
var admin = true;

function invoke(newChan) {
	qb.joinChannel(newChan);
	qb.log("Joined new channel "+newChan);
	qb.sendMsg(new BotMessage(event,"Joined channel "+newChan));
}