var help = "Returns current time. Syntax: ?time"
var param = 0;

function invoke() {
	time = new java.util.Date().toString();
	qb.sendMsg(new BotMessage(event, "The time is now " + time));
}