var help = "Joins a channel on current server. Syntax: ?joinChan <channel>";
var param = 1;
var admin = true;

function invoke(newChan) {
	qb.joinChannel(newChan);
	out.println("Joined new channel "+newChan);
	qb.sendMessage(channel,sender+": Joined channel "+newChan);
}