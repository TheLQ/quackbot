/*
 * Reports what VLC is currently playing
 */
var util = true;

function whatsPlaying() {
	log.debug("Initalizing vlc update");
	var msg = webTalk("http://localhost:8907/current.html");
	if(msg == null)
		return "ERROR: No media player is running";

	var msg = msg.split(",");
	if(msg[2] != "playing")
		return "ERROR: Music is not playing";

	return "Now playing "+msg[0]+" by "+msg[1];
}