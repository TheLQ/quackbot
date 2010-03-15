<?php
// Prevent PHP from stopping the script after 30 sec
ignore_user_abort(TRUE);
/****************
/setname changes fullname
****************/
set_time_limit(0);
function fputs2($sockets, $send) {
	$send=$send."\n";
	//socket_write($sockets, $send);
	fputs($sockets, $send);
	echo "[SEND] ".$send;
}
$socket = fsockopen("irc.dejatoons.net", 6667) or die("ERROR");

/*
//set up socket
$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);

//reuse, bind, and listen
socket_set_option($socket, SOL_SOCKET, SO_REUSEADDR, 1);
socket_bind($socket,  "irc.freenode.net", 6667);
socket_listen($socket);
*/

// Send auth info
fputs2($socket,"USER Quacky evilxproductions.awardspace.com Quacky :Quacky bot");
$nick='LordQuacky_BOT';
fputs2($socket,"NICK $nick");

// Join channel
usleep(100);
//fputs2($socket,"JOIN ##NewYearCountdown");
//fputs2($socket,"JOIN #freenode-newyears");
fputs2($socket,"JOIN #lyokofreak");
$user_timezone=array();
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
"Z - Zulu Time Zone - |Military"=>0
);
function _exec($cmd) {
	$WshShell = new COM("WScript.Shell");
	$oExec = $WshShell->Run($cmd, 0,false);
	echo $cmd;
	return $oExec == 0 ? true : false;
}
// Force an endless while
stream_set_timeout($socket,2);
//while ($data = socket_read($socket,99999)) {
while (1) { 

	 while($data = fgets($socket)) {
	    // Continue the rest of the script here
		//$data = socket_read($socket, 1024);

		//$data=fgets($socket); 
		//$cleandata=explode("\n",$data);
		
		echo "[RECIVE] ".$data;
	    flush();
		
		// Separate all data
		$dataa=substr($data,1);
		list($allname,$ircmode,$channel,$msg) = explode(' ', $dataa, 4);
		if($allname == "PING"){
			//need to send channel because ping dosn't have a msg
			fputs2($socket, "PONG ".$ircmode);
		}
		$msg=substr($msg,1);
		//Get username stuff
		list($begining,$hostmask) = explode("@", $allname);
		list($username,$ident) = explode("!",$begining);
		$real_time=time()+(60*60*5);
		/***********
		This is what the server sends you on a join:
		:XanasFist!~XanasFist@evilxproductions.awardspace.com JOIN :#test123
		
		When someone says something
		:XanasFist!~XanasFist@evilxproductions.awardspace.com PRIVMSG #test123 :stuff
		
		/me command
		:XanasFist!~XanasFist@evilxproductions.awardspace.com PRIVMSG #test123 :?ACTION eats bot?
		its the assci number 001
		eg: "PRIVMSG #channel :" . chr(001) . "ACTION pokes username" . chr(001) . "\n";
		**********/
		
		if($ircmode=="PRIVMSG" && (substr($msg,0,strlen($nick.": "))==$nick.": " || ($channel==$nick))) {
			if(isset($lockuser) && $username !== $lockuser && $channel!="##NewYearCountdown") {
				//fputs2($socket,"PRIVMSG $channel :Sorry, bot is locked to $lockuser");
				continue;
			}
			$channel=($channel==$nick) ? $username : $channel;
			$cleanrawcommand = str_replace(array(chr(10), chr(13), chr(7), "\n", chr(12), chr(214)),"", $msg);
			echo "\n[WARNING] USER SAYS: $cleanrawcommand \n";
			
			if($username==$channel) {
				//used PM
				$command = explode(" ",$cleanrawcommand,2);
				$end=$command[1];
				$command = $command[0];
			}
			else {
				$command = explode(" ",$cleanrawcommand,3);
				$end=$command[2];
				$command = $command[1];
			}
			
			echo "[WARNING] COMMAND: $command \n";
			echo "[WARNING] END: $end \n";
			$switchcommand=$command;
			//require to split by ,'s
			$args=$end;
			$args=explode(',',$args);
			switch($switchcommand) {
				case "sayit":
					fputs2($socket,"PRIVMSG $channel :Combined-Minds.net irc bot tutorial!");
				break;
				case "flood":	
					for ( $i = 1; $i <= $end; $i++ ) {
						fputs2($socket,"PRIVMSG $channel :Combined-Minds.net irc bot tutorial!");
					}
					break;
			case "dienow":
				fputs2($socket,"PRIVMSG $channel: Goodbye cruel world!");
				die();
			break;
				case "eval":
					if($username==$current_admin) {
						ob_start();
						eval($end);
						$result = ob_get_contents();
						ob_end_clean();
						
						$result=str_replace("\n","",$result);
						fputs2($socket,"PRIVMSG $channel :$username: $result");
					}
					else {
						fputs2($socket,"PRIVMSG $channel :Sorry, only $current_admin can use this");
					}
				break;
				case "irc_eval":
					if($username==$current_admin) {
						fputs2($socket,$end);
					}
					else {
							fputs2($socket,"PRIVMSG $channel :Sorry, only $current_admin can use this");
				}
				break;
				case "echo":
					fputs2($socket,"PRIVMSG $channel :{$username}: {$end}");
				break;
				case "numecho":
					for ( $i = 1; $i <= $args[0]; $i++ ) {
						if($args[1]==1) {
							fputs2($socket,"PRIVMSG $channel :".chr(001)."ACTION ".$args[2]."".chr(001));
						}
						if($args[1]==0) {
							fputs2($socket,"PRIVMSG $channel :{$args[2]}");
						}
					}
				break;
				case "lock":
					if(isset($lockuser) && $username==$current_admin) {
						fputs2($socket,"PRIVMSG $channel : $lockuser is no longer locked");
						unset($lockuser);
					}
					elseif($username==$current_admin) {
						$lockuser=$current_admin;
						fputs2($socket,"PRIVMSG $channel : $lockuser is now the only person that can use me!");
					}
				break;
				case "restart":
					if($username==$current_admin) {
					fputs2($socket,"QUIT ONIONS");
					passthru('php simpleconnect.php');
					die("l");
					}
				break;
				case "countdown_set":
					if($countdown_on==FALSE) {
						$countdown_on=TRUE;
						fputs2($socket,"PRIVMSG $channel : Countdown Enabled!");
						break;
					}
					if($countdown_on==TRUE) {
						unset($countdown_on);
						fputs2($socket,"PRIVMSG $channel : Countdown Disabled!");
						break;
					}
				break;
				case "print_code":
					fputs2($socket,"PRIVMSG $channel :jseval:dd=new Date();dd=new Date(1230768000000 -Date.parse(Date())); print(Math.floor(dd/(60*60*1000*24)*1)+' Days '+Math.floor((dd%(60*60*1000*24))/(60*60*1000)*1)+' Hours '+Math.floor(((dd%(60*60*1000*24))%(60*60*1000))/(60*1000)*1)+' Minutes '+Math.floor((((dd%(60*60*1000*24))%(60*60*1000))%(60*1000))/1000*1)+' Seconds till NEW YEAR!!!');");
				break;
				case "join":
					fputs2($socket,"JOIN $end");
				break;
				case "part":
					fputs2($socket,"PART $end");
				break;
				case "register":
					if($end=='manganip') {
						$current_admin=$username;
						fputs2($socket,"PRIVMSG $channel :$current_admin registered as current admin!");
					}
				break;
				case "time_remaining":
						$current_hours=isset($user_timezone[$username]) ? $user_timezone[$username]." hours " : "";
					    $uts['start']=strtotime($current_hours."UTC");
					    $uts['end']=strtotime("January 1, 2009 -5 hours UTC");
					    if( $uts['start']!==-1 && $uts['end']!==-1 ) {
					        if( $uts['end'] >= $uts['start'] ) {
					            $diff    =    $diff    =    $uts['end'] - $uts['start'];
					            if( $days=intval((floor($diff/86400))) )
					                $diff = $diff % 86400;
					            if( $hours=intval((floor($diff/3600))) )
					                $diff = $diff % 3600;
					            if( $minutes=intval((floor($diff/60))) )
					                $diff = $diff % 60;
					            $diff    =    intval( $diff );            
					            fputs2($socket,"PRIVMSG $channel :$username: There are $days days, $hours hours, $minutes minutes, $diff seconds remaining till New Year!");
					        }
						}
				break;
				case "current_time":
					$current_hours=isset($user_timezone[$username]) ? $user_timezone[$username]." " : "";
					fputs2($socket,"PRIVMSG $channel :$username: The current time is ".date("G:i:s A",(strtotime($current_hours."UTC"))));
				break;
				case "set_timezone":
					$temp=trim($end);
					$temp=(int)$end;
					if($temp<13 && $temp>-13) {
						$user_timezone[$username]=$temp;
						fputs2($socket,"PRIVMSG $channel :$username: Your timezone has been successfully added");
					}
					else {
						fputs2($socket,"PRIVMSG $channel :$username: Number needs to be a whole number and be between -12 and 12");
					}
				break;
				case "time_countdown":
					//check to see if were within right time
					$time_array=getDate(time());
					if($time_array['minutes'] >= 52 && $time_array['minutes'] <= 55) {
					//if($time_array['minutes'] >= 5 && $time_array['minutes'] <= 7) {
					
						//figure out which utc offset makes 12
						for($x=-12;$x<=12;$x++) {
							$tempstanp=strtotime($x." hours");
							$getdata=getDate($tempstamp);
							
							//no need to continue if hour isn't 12
							if($getdata['hours']+$x==24) continue;
							
							//figured out utc offset, get timezone name
							$timezoner=array_keys($timezones,$x);
							foreach($timezoner as $value) {
								$timezone_out.=$value.", ";
							}
							
							fputs2($socket,"PRIVMSG $channel :Attention: Countdown for $timezone_out Timezone(s) (UTC offset: $x) started!".$ending);
							
							//figure out seconds to 55, sleep
							$sleeper=((60-$getdata['minutes'])*60)+$getdata['seconds'];
							//$sleeper=3;
							echo "[WARNING] Sleeping till 55 for $sleeper seconds";
							sleep($sleeper);
							
							
							//start countdown
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 5 minutes!");
							echo "[WARNING] Waiting....\n";
							sleep(60);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 4 minutes!");
							echo "[WARNING] Waiting....\n";
							sleep(60);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 3 minutes!");
							echo "[WARNING] Waiting....\n";
							sleep(60);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 2 minutes!");
							echo "[WARNING] Waiting....\n";
							sleep(60);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 1 minutes!");
							echo "[WARNING] Waiting....\n";
							sleep(30);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 30 seconds!");
							echo "[WARNING] Waiting....\n";
							sleep(10);
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 20 seconds!");
							echo "[WARNING] Waiting....\n";
							sleep(10);
							for($y=10;$y>=2;$y=$y-1) {
								fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in $y seconds!");
								echo "[WARNING] Waiting....\n";
								sleep(1);
							}
							fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) are going to hit New Years in 1 second!");
							fputs2($socket,"PRIVMSG $channel : Welcome to the New Year $timezone_out Timezone(s)");
						}
					}
					else {
						fputs2($socket,"PRIVMSG $channel :$username: Must be called when minutes is between 52 and 55. Current minute: ".$time_array['minutes']);
					}
				break;
				case "force_countdown":
					//start countdown
					//5min-1min 
					$ending=(isset($end)) ? (" for ".$end) : ("");
					fputs2($socket,"PRIVMSG $channel :Attention: Countdown started".$ending);
					fputs2($socket,"PRIVMSG $channel :Attention: 5 minutes till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(60);
					fputs2($socket,"PRIVMSG $channel :Attention: 4 minutes till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(60);
					fputs2($socket,"PRIVMSG $channel :Attention: 3 minutes till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(60);
					fputs2($socket,"PRIVMSG $channel :Attention: 2 minutes till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(60);
					fputs2($socket,"PRIVMSG $channel :Attention: 1 minute till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(30);
					fputs2($socket,"PRIVMSG $channel :Attention: 30 Seconds till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(10);
					fputs2($socket,"PRIVMSG $channel :Attention: 20 Seconds till New Year{$ending}!");
					echo "[WARNING] Waiting....\n";
					sleep(10);
					for($x=10;$x>=2;$x=$x-1) {
						fputs2($socket,"PRIVMSG $channel :Attention: $x Seconds till New Year{$ending}!");
						echo "[WARNING] Waiting....\n";
						sleep(1);
					}
					fputs2($socket,"PRIVMSG $channel :Attention: 1 second till New Year{$ending}!");
					fputs2($socket,"PRIVMSG $channel : Welcome to the New Year $output Timezone(s)");
				break;
				case "timezone_abr":
					foreach($timezones as $key => $value) {
						$temp=explode(' - ',$key);
						if($temp[0]!=$end) continue;
						fputs2($socket,"PRIVMSG $channel :$end = {$temp[1]}, UTC offset of $value hours");
						break;
					}
					fputs2($socket,"PRIVMSG $channel :$username Invalid Timezone");
				break;
				case "time_help":
					fputs2($socket,"PRIVMSG $username :Avalible Commands: (all commands must be prefixed with LordQuacky_BOT: )(IE LordQuacky_BOT: current_time");
					fputs2($socket,"PRIVMSG $username : set_timezone [UTC_offset] | Sets timezone for acurate time (if this isn't set, 0 is assumed. Go to http://www.timeanddate.com/library/abbreviations/timezones/ for UTC_Offset.");
					fputs2($socket,"PRIVMSG $username : current_time | Shows current time according to timezone");
					fputs2($socket,"PRIVMSG $username : time_remaining | Shows time remaining till New Years according to timezone");
					fputs2($socket,"PRIVMSG $username : timezone_abr [Abr_of_timezone] | Converts Abbriviation of timezone to expanded form");
					fputs2($socket,"PRIVMSG $username : force_countdown [Optional_end] | Starts countown with optional end");
					fputs2($socket,"PRIVMSG $username : time_countdown | Forces timed countdown to start");
					fputs2($socket,"PRIVMSG $username : enable_countdown | Enables the auto-countdown");
					fputs2($socket,"PRIVMSG $username : --------------------------------------------------------------------");
					fputs2($socket,"PRIVMSG $username :Notes:");
					fputs2($socket,"PRIVMSG $username : 1)The system time is synced every 10 minutes");
					fputs2($socket,"PRIVMSG $username : 2)(soon), a countdown will start 5 minutes before a certain New Year. It will go 5 min,2 min,1 min,45 sec,30-0 secs");
				break;
				case "help":
					fputs2($socket,"PRIVMSG $username :Avalible Commands: (all commands must be prefixed with LordQuacky_BOT: )(IE LordQuacky_BOT: current_time) sayit, flood, dienow, eval, irc_eval, echo, numecho, lock, restart, enable_countdown, print_code, join, part, register, current_time, time_remaining, set_timezone, force_countdown, time_help, help.");
					fputs2($socket,"PRIVMSG $username : time_help for detailed time commands. details of others comming");
				break;
				default:
					fputs2($socket,"PRIVMSG $channel :Illegal Command {$switchcommand}");
				break;
			}
		}
	}
	//$channel="#freenode-newyears";
 	$channel="#freenode-newyears";
	$time_array=getdate(strtotime("UTC"));
	echo "[WARNING] {$time_array['minutes']} minutes on the clock \n";
	
	if($countdown_on==TRUE) {
		echo "[WARNING] Inside Countdown on \n";
		//if($time_array['minutes'] >= 22 && $time_array['minutes'] <= 33) {
		if($time_array['minutes']==55) {	
			echo "[WARNING] Inside minuite counter \n";
			//figure out which utc offset makes 12
			for($x=-12;$x<=12;$x++) {
				echo "[WARNING] Inside hour counter $x \n";
				$tempstanp=strtotime($x." hours UTC");
				$getdata=getDate($tempstanp);
				
				//no need to continue if hour isn't 12
				if(($getdata['hours'])!=13) continue;
				
				//figured out utc offset, get timezone name
				$timezoner=array_keys($timezones,$x);
				foreach($timezoner as $key => $value) {
					$timezone_full.=$value.", ";
					
					//get only the abbr
					$tenp=explode(' - ',$value);
					$timezone_out.=$tenp[0].", ";
				}
				
				//over complicated way to format timezone_out
				$timezone_out=substr($timezone_out,0,-2);
				$tenp=explode(',',$timezone_out);
				$tenp=str_replace(',',', and',$tenp[count($tenp)-1]);
				foreach($tenp as $values) {
					$timezone_out.=$values;
				}
				
				fputs2($socket,"PRIVMSG $channel :Attention: Countdown for $timezone_full Timezone(s) (UTC offset: $x) started!".$ending);
				
				//start countdown
				fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) (UTC offset: $x) are going to hit New Years in 5 minutes!");
				echo "[WARNING] Waiting....\n";
				sleep(60);
				fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) (UTC offset: $x) are going to hit New Years in 4 minutes!");
				echo "[WARNING] Waiting....\n";
				sleep(60);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 3 minutes!");
				echo "[WARNING] Waiting....\n";
				sleep(60);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 2 minutes!");
				echo "[WARNING] Waiting....\n";
				sleep(60);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 1 minute!");
				echo "[WARNING] Waiting....\n";
				sleep(30);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 30 seconds!");
				echo "[WARNING] Waiting....\n";
				sleep(10);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 20 seconds!");
				echo "[WARNING] Waiting....\n";
				sleep(10);
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 10 seconds!");
				echo "[WARNING] Waiting....\n";
				sleep(5);
				for($y=5;$y>=2;$y=$y-1) {
					//fputs2($socket,"PRIVMSG $channel :Attention: $timezone_out Timezone(s) (UTC offset: $x) are going to hit New Years in $y seconds!");
					fputs2($socket,"PRIVMSG $channel :Attention: New Years in $y seconds");
					echo "[WARNING] Waiting....\n";
					sleep(1);
				}
				fputs2($socket,"PRIVMSG $channel :Attention: New Years in 1 second!");
				fputs2($socket,"PRIVMSG $channel :HAPPY AWESOME UBER-COOL NEW YEAR: $timezone_out TIMEZONE(s) ");
				for($y=0;$y<=4;$y++) {
					fputs2($socket,"PRIVMSG $channel :Happy Happy New... YEAR!");
				}
				fputs2($socket,"PRIVMSG $channel :".chr(001)."ACTION stops before gets kicked.".chr(001));
			}
		}
	}
	usleep(10000);
}
?>