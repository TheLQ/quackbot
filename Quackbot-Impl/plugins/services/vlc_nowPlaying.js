/* 
 * Reports what VLC is currently playing
 */

importClass(java.lang.Thread);

var service = true;

//Start checking loop in another thread
function invoke() {
	log.debug("Initalizing vlc update");
	var previous = "";
	while(1) {
		try{
			Thread.sleep(5000);
			log.debug("Checking");
			var current =  webTalk("http://localhost:8082/current.html");
			if(current == null) {
				log.warn("Unable to connect to VLC, reload CMDs to retry");
				return;
			}
			if(current != previous)
				ctrl.sendGlobalMessage(current);
			else
				log.debug("Nothing to do");
			previous = current;
		}
		catch(err) {
			log.error("ERROR "+err);
			if(err.toString().search("InterruptedException") != -1) {
				log.warn("VLC thread interrupted");
			}
			return;
		}
	}
}