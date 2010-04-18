var help = "Attempts to deop a person. Admin only command. Syntax: ?devoice <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.deVoice(channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(channel,reqUser);
	if(userObj === null)
		qb.sendMsg(new BotMessage(msgInfo,reqUser+" does not exist"));
	else if(userObj.hasVoice()==false)
		qb.sendMsg(new BotMessage(msgInfo,reqUser+" is no longer voiced"));
	else
		qb.sendMsg(new BotMessage(msgInfo,"Failed to devoice "+reqUser+" (bot not an op?)"));
}