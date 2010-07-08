var help = "Attempts to deop a person. Admin only command. Syntax: ?deop <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.deOp(event.channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(event.channel,reqUser);
	if(userObj === null)
		qb.sendMsg(new BotMessage(event,reqUser+" does not exist"));
	else if(userObj.isOp()==false)
		qb.sendMsg(new BotMessage(event,reqUser+" is no longer an op"));
	else
		qb.sendMsg(new BotMessage(event,"Failed to deop "+reqUser+" (bot not an op?)"));
}