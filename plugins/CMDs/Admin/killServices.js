var help = "Kills all services. Syntax: ?killServices";
var param = 0;
var admin = true;

importClass(java.util.concurrent.Executors);

function invoke() {
	InstanceTracer.getMainInst().threadPool_js.shutdownNow();
	InstanceTracer.getMainInst().threadPool_js.shutdownNow();
	InstanceTracer.getMainInst().threadPool_js = Executors.newCachedThreadPool();
	qb.sendMsg(new BotMessage(msgInfo,"Services sucessfully killed"));
}