//Provides current time

var help = "Returns current time. Syntax: ?time"
var param = 0;

function invoke() {
		out.println("Huhahahahah");
		out.println("Huhahssdssahahah");
    	time = new java.util.Date().toString();
    	msg = sender + ": The imse is now " + time;
        qb.sendMessage(channel, msg);
}