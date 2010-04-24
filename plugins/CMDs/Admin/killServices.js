var help = "Kills all services. Syntax: ?killServices";
var param = 0;
var admin = true;

importClass(java.util.concurrent.Executors);

function invoke() {
	ThreadPoolManager.restartPlugin();
	qb.sendMsg(new BotMessage(msgInfo,"Services sucessfully killed"));
}