var help = "Parts a channel on current server. Syntax: ?partChan <OPTIONAL:channel>"
var param = 1;
var admin = true;
var ReqArg = true;

function invoke(newChan) {
	if(newChan=="null") {
		//Part current channel
		qb.partChannel(event.channel);
		qb.log("Parted channel "+event.channel);
		qb.sendMsg(new BotMessage(event,"Parted channel "+event.channel));
	}
	else {
		//Part given channel
		qb.partChannel(newChan);
		qb.log("Parted channel "+newChan);
		qb.sendMsg(new BotMessage(event,"Parted channel "+newChan));
	}
}