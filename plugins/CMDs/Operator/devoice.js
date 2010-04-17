var help = "Attempts to deop a person. Admin only command. Syntax: ?devoice <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.deVoice(channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(channel,reqUser);
	if(userObj === null)
		qb.sendMessage(channel,reqUser+" does not exist");
	else if(userObj.hasVoice()==false)
		qb.sendMessage(channel,reqUser+" is no longer voiced");
	else
		qb.sendMessage(channel,"Failed to devoice "+reqUser+" (bot not an op?)");
}