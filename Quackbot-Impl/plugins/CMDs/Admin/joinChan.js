var help = "Joins a channel on current server. Syntax: ?joinChan <channel>";
var param = 1;
var admin = true;

function invoke(newChan) {
	qb.joinChannel(newChan);
	qb.log("Joined new channel "+newChan);
	qb.sendMsg(new BotMessage(msgInfo,"Joined channel "+newChan));
}