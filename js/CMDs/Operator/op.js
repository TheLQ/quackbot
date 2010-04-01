var help = "Attempts to op a person. Admin only command. Syntax: ?op <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
    qb.op(channel,reqUser);
    Thread.sleep(500); //sleep for 500 ms
    var userObj = qb.getUser(channel,reqUser);
    if(userObj === null)
	qb.sendMessage(channel,reqUser+" does not exist");
    else if(userObj.isOp()==true)
	qb.sendMessage(channel,reqUser+" is now an op");
    else
	qb.sendMessage(channel,"Failed to op "+reqUser+" (bot not an op?)");
}