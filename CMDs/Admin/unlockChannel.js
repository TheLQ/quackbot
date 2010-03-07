var help = "Unlocks the bot in channel. Syntax: ?unlockChannel"
var param = 0;
var admin = true;

function invoke() {
	qb.chanLockList.remove(channel);
	qb.sendMessage(channel,"Bot has been unlocked for this channel");
}