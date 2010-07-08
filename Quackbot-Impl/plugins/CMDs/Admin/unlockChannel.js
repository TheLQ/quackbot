var help = "Unlocks the bot in channel. Syntax: ?unlockChannel"
var param = 0;
var admin = true;

function invoke() {
	qb.chanLockList.remove(event.getChannel());
	qb.sendMsg(new BotMessage(event,"Bot has been unlocked for this channel"));
}