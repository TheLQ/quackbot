//Provides current time

var help = "Returns current time. Syntax: ?time"
var param = 0;

function invoke() {
    	time = new java.util.Date().toString();
    	msg = sender + ": The time is now " + time;
        qb.sendMessage(channel, msg);
}