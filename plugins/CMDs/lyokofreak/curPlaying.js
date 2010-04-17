/* 
 * Reports what VLC is currently playing
 */

var param = 0; //Prevent error with

function invoke() {
	println("Initalizing vlc update");
	var msg = webTalk("http://localhost:8082/current.html");
	if(msg == null)
		msg = "ERROR: VLC is not running";
	qb.sendMessage(channel,msg);
}