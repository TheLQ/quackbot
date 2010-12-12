var help = "Starts countdown of specified length. Syntax: ?simpleCountdown <seconds>";

var futureMs = 0;
var msRemain = 0;
var closestMin = 0;

importClass(java.lang.System);

function onCommand(seconds) {
	futureMs = System.currentTimeMillis()+(seconds*1000);
	try{
		recalculate();
		while(msRemain > 60000) {
			getBot().sendMessage(timeRemaining(futureMs)+" remaining! (min countdows)");
			if(closestMin < 900)
				continue;
			log.debug("sleeping for "+closestMin+" | msremain: "+msRemain)
			Thread.sleep(closestMin)
			recalculate();
		}

		//Now at 1 min
		getBot().sendMessage(channel, "1 minuite remaining! (hard coded)");

		//Wait for 30 sec
		Thread.sleep(30000)
		qb.sendMsg(new BotMessage(event,"30 seconds remaining! (hard coded)"));

		//Wait for 20 sec
		Thread.sleep(20000)
		qb.sendMsg(new BotMessage(event,"20 seconds remaining! (hard coded)"));

		//Wait for 10 sec
		Thread.sleep(30000)
		qb.sendMsg(new BotMessage(event,"10 seconds remaining! (hard coded)"));

		//Wait for 5 sec
		Thread.sleep(5000)
		for(var i=5;i>0;i--) {
			qb.sendMsg(new BotMessage(event,i+" seconds remaining! (hard coded)"));
			Thread.sleep(1000);
		}

		qb.sendMsg(new BotMessage(event,"Whoo, end!!"));
	}
	catch(err) {
		log.error("ERROR "+err);
		if(err.toString().search("InterruptedException") != -1) {
			log.warn("Countdown thread interrupted");
			return;
		}
	}
}

function recalculate() {
	msRemain = futureMs-System.currentTimeMillis();
	closestMin = msRemain % 60000;
}