var help = "Parts a channel on current server. Syntax: ?partChan <OPTIONAL:channel>"
var param = 1;
var admin = true;
var ReqArg = true;

function invoke(newChan) {
	if(newChan=="null") {
		//Part current channel
		qb.partChannel(channel);
		out.println("Parted channel "+channel);
		qb.sendMessage(channel,sender+": Parted channel "+channel);
	}
	else {
		//Part given channel
		qb.partChannel(newChan);
		out.println("Parted channel "+newChan);
		qb.sendMessage(channel,sender+": Parted channel "+newChan);
	}
}