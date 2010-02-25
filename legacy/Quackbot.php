<?php
/******
*LordQuacky_BOT configuration of Quackbot by LordQuackstar
*
*Updated on 2/21/09
******/

mysql_connect("localhost","root","") or die(mysql_error());
mysql_select_db("scripts") or die(mysql_error());

include('Quackbot.class.php');
class LordQuacky_BOT extends QuackBot {
	/***********
	* RUN IRC COMMANDS
	************/
	var $help_join="Joins a channel. SYNTAX: join <channel>";
	function join($channel) {
		if($this->array_search_recursive($this->msginfo['username'],$this->current_admin)==-1)
			return false;
		$this->showDebug("Join Called on {$this->msginfo['id']} to {$this->msginfo['end']}");
		$this->joinChannel($this->msginfo['id'],$this->msginfo['args'][0]);
		//add to channel list
		$this->channel_array[$this->msginfo['id']][]=$this->msginfo['args'][0];
	}
	var $help_part="Parts a channel. SYNTAX: part <channel>";
	function part($channel) {
		if($this->array_search_recursive($this->msginfo['username'],$this->current_admin)==-1)
			return false;
		$this->showDebug("Tring to join channel $channeler");
		$this->partChannel($this->msginfo['id'],$this->msginfo['args'][0]);
		//remove from channel list
		$channel_id=array_search($this->msginfo['args'],$this->channel_array[$this->msginfo['id']]);
		unset($this->channel_array[$this->msginfo['id']][$channel_id]);
	}
	var $help_dienow="Dies or quits server. SYNTAX: dienow <OPTIONAL:server_num>";
	public function dienow($server=null) {
		if($this->array_search_recursive($this->msginfo['username'],$this->current_admin)==-1)
			return false;
		$quitmsg="The great QuackBot has fallen!";
		$this->showDebug("Quiting a Server");
		if($server!=null) {
			$this->sendMsg($this->msginfo['id'],"QUIT :$quitmsg");
			//need to do cleanup
			$this->showDebug("Cleaning up variables and connections");
			unset($this->channels[$this->msginfo['id']]);
			stream_socket_shutdown($this->sockets[$this->msginfo['id']],STREAM_SHUT_RDWR);
			unset($this->sockets[$this->msginfo['id']]);
		}
		else {
			//no args, must mean quit all servers
			$this->sendMsg("all","QUIT :$quitmsg:");
			die(1);
		}
	}
	
	var $help_rawecho = "Says a raw irc command";
	function rawecho($command) {
		if($this->array_search_recursive($this->msginfo['username'],$this->current_admin)==-1)
			return false;
		$this->sendMsg($this->msginfo['id'],$command);
	}
	function evalme($string) {
		if($this->array_search_recursive($this->msginfo['username'],$this->current_admin)==-1)
			return false;
		else {
			ob_start();
			eval($end);
			$result = ob_get_contents();
			ob_end_clean();
			$result=str_replace("\n","",$result);
			
			return $result; 
		}
		
	}
	
	
	/***********
	* BASIC PING PONG TYPE RESPONSES
	***********/
	var $help_sayit="Says hehehehe. SYNTAX: sayit";
	function sayit() {
		return "hehehehe";
	}
	var $help_echo_me="Says what you said SYNTAX: echo_me <string>";
	function echo_me($string) {
		return $string;
	}
	var $help_numecho="Repeats string x times SYNTAX: numecho <number>,<mode>,<string>; Mode of 0=Normal, Mode of 1=me";
	function numecho($repeat_num,$mode,$string) {
		$output="";
		for ( $i = 1; $i <= $repeat_num; $i++ ) {
			if($mode==1) {
				$output.="PRIVMSG $channel :".chr(001)."ACTION $string".chr(001);
			}
			elseif($mode==0) {
				$output.="PRIVMSG {$this->msginfo['channel']} :$string";
			}
		}
		return $output;
	}
	var $help_sendGlobMsgs="Sends a message to all connected channels on current server. SYNTAX: sendGlobMsgs <string>";
	function sendGlobMsgs($string) {
		$this->sendGlobMsg($this->msginfo['id'],$string);
		return false;
	}
	
	/*****show help*******/
	var $help_help="Shows help of functions or lists functions. SYNTAX: help <OPTIONAL:func_name>";
	function help($func_name=null) {
		if($func_name!=null) {
			//supplied func name
			$this->showDebug($this->help[$func_name]);
			if($this->help[$func_name]) {
				$this->showDebug("Supplied func name \"$func_name\", returning help");
				return "Help for $func_name: ".$this->help[$func_name];
			}
			else {
				//function help dosen't exist
				if(method_exists($this,$func_name)) {
					return "No help available for $func_name";
				}
				else {
					return "Function $func_name dosen't exist";
				}
			}
		}
		else {
			$this->showDebug("Didn't supply func name, return list");
			$totalhelp = "";
			foreach($this->help as $name => $help) {
				$totalhelp.=$name.", ";
			}
			$totalhelp=substr($totalhelp,0,-2);
			return "SYNTAX: help <OPTIONAL:func_name> | $totalhelp";
		}
	}
	
	var $time_help = "Shows more specific help of time specific functions.";
	function time_help() {
		$this->sendMsg($this->msginfo['id'],"PRIVMSG {$this->msginfo['username']} :This bot will do a countdown for each whole number timezone, personal countdown, and time in city.");
		$this->sendMsg($this->msginfo['id'],"PRIVMSG {$this->msginfo['username']} :addCountdown: Adds your personal countdown. SYNTAX: addCountdown <timezone>.");
		$this->sendMsg($this->msginfo['id'],"PRIVMSG {$this->msginfo['username']} :timeRemaining: Time remaining of personal countdown. SYNTAX: timeRemaining");
		$this->sendMsg($this->msginfo['id'],"PRIVMSG {$this->msginfo['username']} :currentTime: Check the current time using currentTime <OPTIONAL:offset> (offset not needed if using personal countdown");
		$this->sendMsg($this->msginfo['id'],"PRIVMSG {$this->msginfo['username']} :timeCity: Returns the current time of the specified city. SYNTAX timeCity <city> <country>");
		return false;
	}
	
	
	/********************************
	* Countdown Functions
	********************************/
	var $help_countdown = "Checks for countdown";
	function countdown() {
		$time_array=getdate(strtotime("UTC"));
		/*if($this->countdownMode==false) {
			return "Countdown is off";
		}*/
		if($time_array['minutes']!=55) {
			return false;
		}
		$this->showDebug("----------------------COUNTDOWN IS STARTING--------------------");
		$timezones=array(
			"A - Alpha Time Zone | Military"=>1,
			"ACDT - Australian Central Daylight Time | Australia" => 10.5,
			"ACST - Australian Central Standard Time | Australia"=>9.5,
			"ADT - Atlantic Daylight Time | North America"=>-3,
			"AEDT - Australian Eastern Daylight Time or Australian Eastern Summer Time | Australia"=>11,
			"AEST - Australian Eastern Standard Time | Australia"=>10,
			"AKDT - Alaska Daylight Time | North America"=>-8,
			"AKST - Alaska Standard Time | North America"=>-9,
			"AST - Atlantic Standard Time | North America"=>-4,
			"AWDT - Australian Western Daylight Time | Australia"=>9,
			"AWST - Australian Western Standard Time | Australia"=>8,
			"B - Bravo Time Zone | Military"=>2,
			"BST - British Summer Time | Europe"=>1,
			"C - Charlie Time Zone | Military"=>3,
			"CDT - Central Daylight Time | Australia"=>10.5,
			"CDT - Central Daylight Time | North America"=>-5,
			"CEDT - Central European Daylight Time | Europe"=>2,
			"CEST - Central European Summer Time | Europe"=>2,
			"CET - Central European Time | Europe"=>1,
			"CST - Central Summer Time | Australia"=>10.5,
			"CST - Central Standard Time | Australia"=>9.5,
			"CST - Central Standard Time | North America"=>-6,
			"CXT - Christmas Island Time | Australia"=>7,
			"D - Delta Time Zone | Military"=>4,
			"E - Echo Time Zone | Military"=>5,
			"EDT - Eastern Daylight Time | Australia"=>11,
			"EDT - Eastern Daylight Time | North America"=>-4,
			"EEDT - Eastern European Daylight Time | Europe"=>3,
			"EEST - Eastern European Summer Time | Europe"=>3,
			"EET - Eastern European Time | Europe"=>2,
			"EST - Eastern Summer Time | Australia"=>11,
			"EST - Eastern Standard Time | Australia"=>10,
			"EST - Eastern Standard Time | North America"=>-5,
			"F - Foxtrot Time Zone | Military"=>6,
			"G - Golf Time Zone | Military"=>7,
			"GMT - Greenwich Mean Time | Europe"=>0,
			"H - Hotel Time Zone | Military"=>8,
			"HAA - Heure Avancée de l'Atlantique | North America"=>-3,
			"HAC - Heure Avancée du Centre | North America"=>-5,
			"HADT - Hawaii-Aleutian Daylight Time | North America"=>-9,
			"HAE - Heure Avancée de l'Est | North America"=>-4,
			"HAP - Heure Avancée du Pacifique | North America"=>-7,
			"HAR - Heure Avancée des Rocheuses | North America"=>-6,
			"HAST - Hawaii-Aleutian Standard Time | North America"=>-10,
			"HAT - Heure Avancée de Terre-Neuve | North America"=>-2.5,
			"HAY - Heure Avancée du Yukon | North America"=>-8,
			"HNA - Heure Normale de l'Atlantique | North America"=>-4,
			"HNC - Heure Normale du Centre | North America"=>-6,
			"HNE - Heure Normale de l'Est | North America"=>-5,
			"HNP - Heure Normale du Pacifique | North America"=>-8,
			"HNR - Heure Normale des Rocheuses | North America"=>-7,
			"HNT - Heure Normale de Terre-Neuve | North America"=>-3.5,
			"HNY - Heure Normale du Yukon | North America"=>-9,
			"I - India Time Zone | Military"=>9,
			"IST - Irish Summer Time | Europe"=>1,
			"K - Kilo Time Zone | Military"=>10,
			"L - Lima Time Zone | Military"=>11,
			"M - Mike Time Zone | Military"=>12,
			"MDT - Mountain Daylight Time | North America"=>-6,
			"MESZ - Mitteleuroäische Sommerzeit | Europe"=>2,
			"MEZ - Mitteleuropäische Zeit | Europe"=>1,
			"MSD - Moscow Daylight Time | Europe"=>4,
			"MSK - Moscow Standard Time | Europe"=>3,
			"MST - Mountain Standard Time | North America"=>-7,
			"N - November Time Zone | Military"=>-1,
			"NDT - Newfoundland Daylight Time | North America"=>-2.5,
			"NFT - Norfolk (Island) Time | Australia"=>11.5,
			"NST - Newfoundland Standard Time | North America"=>-3.5,
			"O - Oscar Time Zone | Military"=>-2,
			"P - Papa Time Zone | Military"=>-3,
			"PDT - Pacific Daylight Time | North America"=>-7,
			"PST - Pacific Standard Time | North America"=>-8,
			"Q - Quebec Time Zone | Military"=>-4,
			"R - Romeo Time Zone | Military"=>-5,
			"S - Sierra Time Zone | Military"=>-6,
			"T - Tango Time Zone | Military"=>-7,
			"U - Uniform Time Zone | Military"=>-8,
			" - Coordinated Universal Time | Europe"=>0,
			"V - Victor Time Zone | Military"=>-9,
			"W - Whiskey Time Zone | Military"=>-10,
			"WDT - Western Daylight Time | Australia"=>9,
			"WEDT - Western European Daylight Time | Europe"=>1, 
			"WEST - Western European Summer Time | Europe"=>1,
			"WET - Western European Time | Europe"=>0,
			"WST - Western Summer Time | Australia"=>9,
			"WST - Western Standard Time | Australia"=>8,
			"X - X-ray Time Zone | Military"=>-11,
			"Y - Yankee Time Zone | Military"=>-12,
			"Z - Zulu Time Zone - | Military"=>0
		);
		for($x=-12;$x<=12;$x++) {
			echo "[WARNING] Inside hour counter $x \n";
			$tempstanp=strtotime($x." hours UTC");
			$getdata=getDate($tempstanp);
			$this->showDebug($getdata['hours']);
				
			//no need to continue if hour isn't 12 or day isn't 31
			$this->showDebug($getdata['hours']." ".$getdata['mday']);
			if(($getdata['hours'])!=13) continue;
			elseif(($getdata['mday'])!=31) continue;
				
			//figured out utc offset, get timezone name
			$timezoner=array_keys($timezones,$x);
			foreach($timezoner as $key => $value) {
				$timezone_full.=$value.", ";
				
				//get only the abbr
				$tenp=explode(' - ',$value);
				$timezone_out.=$tenp[0].", ";
			}
				
			$timezone_full=substr($timezone_full,0,-2);
			$timezone_out=substr($timezone_out,0,-2);
				
			$this->sendGlobMsg("all","Attention: Countdown for $timezone_full Timezone(s) (UTC offset: $x) started!".$ending);
			
			//start countdown
			$this->sendGlobMsg("all","Attention: $timezone_out Timezone(s) (UTC offset: $x) are going to hit New Years in 5 minutes! Note that no commands will be accepted till the end of the countdown!");
			$this->showDebug("Waiting....");
			sleep(59);
			$this->sendGlobMsg("all","Attention: $timezone_out Timezone(s) (UTC offset: $x) are going to hit New Years in 4 minutes!");
			$this->showDebug("Waiting....");
			sleep(60);
			$this->sendGlobMsg("all","Attention: New Years in 3 minutes!");
			$this->showDebug("Waiting....");
			sleep(60);
			$this->sendGlobMsg("all","Attention: New Years in 2 minutes!");
			$this->showDebug("Waiting....");
			sleep(60);
			$this->sendGlobMsg("all","Attention: New Years in 1 minute!");
			$this->showDebug("Waiting....");
			sleep(30);
			$this->sendGlobMsg("all","Attention: New Years in 30 seconds!");
			$this->showDebug("Waiting....");
			sleep(10);
			$this->sendGlobMsg("all","Attention: New Years in 20 seconds!");
			$this->showDebug("Waiting....");
			sleep(10);
			$this->sendGlobMsg("all","Attention: New Years in 10 seconds!");
			$this->showDebug("Waiting....");
			sleep(5);
			for($y=5;$y>1;$y=$y-1) {
				if($y==1)
					$this->sendGlobMsg("all","New Years in $y second");
				else {
					$this->sendGlobMsg("all","New Years in $y seconds");
					sleep(1);
				}
			}
			$this->sendGlobMsg("all","Attention: New Years in 1 second!");
			sleep(1);
			$this->sendGlobMsg("all","HAPPY AWESOME UBER-COOL NEW YEAR: $timezone_out TIMEZONE(s) ");
		}
	}
	
	var $countdownMode = false;
	var $help_countdownSwitch = "Toggles the countdown";
	function countdownSwitch($mode) {
		$this->countdownMode = true;
		$stringMode = ($mode==true) ? ("On") : ("Off");
		return "Countdown mode is now ".$stringMode;
	}
	
	/**********
	* Personal Countdown
	**********/
	var $help_addCountdown = "This allows you to see how much time you have left until your New Years. Once added use the command timeRemaining.";
	function addCountdown($UTCs) {
		$nick = $this->msginfo['username'];
		
		$UTC=(int)$UTCs;
		if(abs($UTC) > 12) { return "Number is not between -12 and 12"; }
		elseif(!is_int($UTC)) { return "Only whole number timezones are supported at this time."; }
		
		//has user already submitted name?
		if(mysql_num_rows(mysql_query(sprintf("SELECT * FROM pcountdown WHERE nick = '%s'",mysql_real_escape_string($nick)))) > 0) {
			//need to update
			mysql_query(sprintf("UPDATE pcountdown SET UTC='%s' WHERE nick='%s'",
				mysql_real_escape_string($UTC),
				mysql_real_escape_string($nick)));
		}
		//need to update
		else {
			mysql_query(sprintf("INSERT INTO pcountdown (nick,UTC) VALUES ('%s','%s')",
				mysql_real_escape_string($nick),
				mysql_real_escape_string($UTC)));
		}
		
		//output result
		if($UTC == 0) {
			return "Success. Added countdown timezone $UTC. Note: If you wanted time remaining for a city, use timeCity. Else you must use a UTC offset (IE -5).";
		}
		else {
			return "Success. Added countdown timezone $UTC";
		}
	}
	
	var $help_timeRemaining = "Gives time remaining to your new year. Use addCountdown first. If name is not found, uses UTC time.";
	function timeRemaining() {
		$nick = $this->msginfo['username'];
		$query=mysql_query(sprintf("SELECT * FROM pcountdown WHERE nick = '%s'",mysql_real_escape_string($nick)));
		if(mysql_num_rows($query)==0) return "You havn't used addCountdown yet";
		$row=mysql_fetch_array($query);
		$UTC=(mysql_num_rows($query)==0) ? 0 : $row['UTC'];
		

		
		$uts=array();
		$uts['start']=strtotime("$UTC hours UTC");
		$uts['end']=strtotime("January 1, 2010 $UTC hours UTC");
		if( $uts['start']!==-1 && $uts['end']!==-1 ) {
			if( $uts['end'] >= $uts['start'] ) {
					$diff    =    $uts['end'] - $uts['start'];
				if( $days=intval((floor($diff/86400))) )
					$diff = $diff % 86400;
				if( $hours=intval((floor($diff/3600))) )
					$diff = $diff % 3600;
				if( $minutes=intval((floor($diff/60))) )
					$diff = $diff % 60;
				$diff    =    intval( $diff );
			}
		}
		return "There are $days days, $hours hours, $minutes minutes, $diff seconds remaining till New Year there!";
	}
	
	var $help_currentTime = "Displays current time. Syntax: currentTime <OPTIONAL:offset>. By default it will use offset provided by addCountdown, failing that will use UTC";
	function currentTime($offset=0) {
		$query = mysql_query(sprintf("SELECT * FROM pcountdown WHERE nick = '%s'",mysql_real_escape_string($nick)));
		if(mysql_num_rows($query) > 0) {
			$row=mysql_fetch_array($query);
			$offset=$row['UTC'];
		}
		return date('l\, F d\, Y \a\t h:i:s A',strtotime($offset." hours UTC"));
	}
	
	var $help_timeCity = "Displays current time in the specified city. Powered by http://www.worldtimeserver.com";
	function timeCity($citys,$city1="",$city2="",$city3="",$city4="",$city5="",$city6="") {
		$city=$citys." ".$city1." ".$city2." ".$city3." ".$city4." ".$city5." ".$city6;
		$city=trim($city);
		$searchpage = $this->fetch_page("http://www.worldtimeserver.com/search.aspx",array('searchfor' => $city));
		preg_match("#((http|https|ftp)://(\S*?\.\S*?))(\s|\;|\)|\]|\[|\{|\}|,|\"|'|:|\<|$|\.\s)#ie",$searchpage,$newurl);
		$newurl = substr($newurl[0],0,-1);
		$this->showDebug("Page URL: ".$newurl);
		
		if($newurl=="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd")
			return "City does not exist or was not found.";

		$rawpage = $this->fetch_page($newurl);
		$time=explode("<span class=\"font7\">",$rawpage,2);
		$time=explode("</span>",$time[1],2);
		$time=trim($time[0]);
		$this->showDebug("Found current time: ".$time);
		
		$offset = explode("<span class=\"font1\">",$rawpage,2);
		$offset = explode("</span>",$offset[1],2);
		$offset = substr(trim($offset[0]),-9,3);
		$this->showDebug($offset);
		
		$uts=array();
		$uts['start']=strtotime("$offset hours UTC");
		$uts['end']=strtotime("January 1, 2010 $offset hours UTC");
		if( $uts['start']!==-1 && $uts['end']!==-1 ) {
			if( $uts['end'] >= $uts['start'] ) {
					$diff    =    $uts['end'] - $uts['start'];
				if( $days=intval((floor($diff/86400))) )
					$diff = $diff % 86400;
				if( $hours=intval((floor($diff/3600))) )
					$diff = $diff % 3600;
				if( $minutes=intval((floor($diff/60))) )
					$diff = $diff % 60;
				$diff    =    intval( $diff );
			}
		}
		return "Current time in $city is $time. There are $days days, $hours hours, $minutes minutes, $diff seconds remaining till New Year there!";
	}
	
	/****************
	* Random Functions
	*****************/
	protected function array_search_recursive($needle, $haystack){
		foreach ($haystack as $key => $arr) {
			if(is_array($arr)) {
				$ret=searchArrayRecursive($needle, $arr);
				if($ret!=-1) 
					return $key.','.$ret;
			} 
			else {
				if(strcasecmp($arr,$needle)===0) 
					return (string)$key;
			}
		}
		return -1;
    }
	
	protected function fetch_page($url,$post=null) {
		$this->showDebug("Fetching URL: $url");
		$curl_handle=curl_init();
		curl_setopt($curl_handle,CURLOPT_URL,$url);
		curl_setopt($curl_handle,CURLOPT_CONNECTTIMEOUT,2);
		curl_setopt($curl_handle,CURLOPT_RETURNTRANSFER,1);
		if($post!=null) {
			curl_setopt($curl_handle, CURLOPT_POST,1);
			curl_setopt($curl_handle, CURLOPT_POSTFIELDS,$post);
		}
		$buffer = curl_exec($curl_handle);
		curl_close($curl_handle);
		if (empty($buffer))
			return false;
		else
			return $buffer;
	}
}
$quackbot = new LordQuacky_BOT;
$quackbot->registerConstantFunc("countdown");
$quackbot->setInfo("LordQuacky_BOT","Quack","Bot by LordQuackstar");
$quackbot->registerAdmin("LordQuackstar","manganip");
$quackbot->setPrefixes(array("LordQuacky_BOT: ","~!"));
$quackbot->joinServer("irc.freenode.net:8000",array("##ubuntu-newyears","#freenode-newyears"),"manganip");
//$quackbot->joinServer("irc.dejatoons.net:6666",array("##newyearcountdown"),"manganip");
$quackbot->joinServer("coolchat.bhasirc.com:6667",array("#coolchat"));
$quackbot->init();

?>