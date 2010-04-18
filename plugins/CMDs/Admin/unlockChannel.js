var help = "Unlocks the bot in channel. Syntax: ?unlockChannel"
var param = 0;
var admin = true;

function invoke() {
	qb.chanLockList.remove(channel);
	qb.sendMsg(new BotMessage(msgInfo,"Bot has been unlocked for this channel"));
}