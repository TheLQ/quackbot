//Provides current time

var help = "Returns current time. Syntax: ?time"
var param = 0;

function invoke() {
		println("Huhahahahah");
    	time = new java.util.Date().toString();
    	msg = sender + ": The ime is now " + time;
        qb.sendMessage(channel, msg);
}