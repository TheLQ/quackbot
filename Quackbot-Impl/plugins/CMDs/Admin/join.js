var help = "Joins a channel on current server. Syntax: ?join <channel>";
var admin = true;

function onCommand(newChan) {
	getBot().joinChannel(newChan);
	log.debug("Joined new channel "+newChan);
	return "Joined channel "+newChan;
}