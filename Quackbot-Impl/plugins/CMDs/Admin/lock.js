importClass(org.apache.commons.lang.StringUtils);
var help = "Locks the bot globaly or by channel. Syntax: ?lock <OPTIONAL:channel>"
var parameter = {
	optional:1
};
var admin = true;

function onCommandChannel(channel, sender, login, hostname, args) {
	if(ars[0] == "this") {
		getBot().chanLockList.add(channel);
		return "Bot has been locked for this channel ("+channel+")";
	} else if(StringUtils.isNotBlank(args[0])) {
		getBot().chanLockList.add(args[0]);
		return "Bot has been locked for channel "+args[0];
	} else {
		getBot().botLocked = true;
		return "Bot has been locked globally";
	}
}