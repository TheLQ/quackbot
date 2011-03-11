var help = "Returns current time. Syntax: ?time"

function onCommand() {
	time = new java.util.Date().toString();
	return "The time is now " + time;
}