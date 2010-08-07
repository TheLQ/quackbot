var help = "Parts a channel on current server. Syntax: ?part <OPTIONAL:channel>"
var parameter = {
	optional:1
};
var admin = true;

function onCommandChannel(channel, sender, login, hostname, args) {
	if(StringUtils.isBlank(args[0])) { //Should we part current channel?
		getBot().partChannel(channel);
		return "Bot has been locked for this channel ("+channel+")";
	}
	//Part given channel
	getBot().partChannel(args[0]);
	return "Bot has been locked for channel "+args[0];
}