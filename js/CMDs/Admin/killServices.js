var help = "Kills all services. Syntax: ?killServices";
var param = 0;
var admin = true;

importClass(java.util.concurrent.Executors);

function invoke() {
    ctrl.threadPool_js.shutdownNow();
    ctrl.threadPool_js.shutdownNow();
    ctrl.threadPool_js = Executors.newCachedThreadPool();
    qb.sendMessage(channel,sender+": Services sucessfully killed");
}