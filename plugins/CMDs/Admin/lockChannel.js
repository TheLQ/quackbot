var help = "Locks the bot in channel. Syntax: ?lockChannel"
var param = 0;
var admin = true;

function invoke() {
	qb.chanLockList.add(msgInfo.getChannel());
	qb.sendMsg(new BotMessage(msgInfo,"Bot has been locked for this channel"));
}