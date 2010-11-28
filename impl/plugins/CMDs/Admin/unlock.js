importClass(org.apache.commons.lang.StringUtils);
var help = "Unlocks the bot globaly or by channel. Syntax: ?unlock <OPTIONAL:channel>"
var parameter = {
	optional:1
};
var admin = true;

function onCommandChannel(channel, sender, login, hostname, args) {
	if(ars[0] == "this") {
		getBot().chanLockList.remove(channel);
		return "Bot has been unlocked for this channel ("+channel+")";
	} else if(StringUtils.isNotBlank(args[0])) {
		getBot().chanLockList.remove(args[0]);
		return "Bot has been unlocked for channel "+args[0];
	} else {
		getBot().botLocked = false;
		return "Bot has been unlocked globally";
	}
}