<?php
/************
*Quackbot Class V2 By Lord.Quackstar
*
*Updated 2/21/09, 9/7/09, 12/26/09
**************/
class QuackBot {
	//preconfig
	var $sockets;
	var $channels;
	var $joinchannels;
	var $ready_servers=array();
	
	//bot irc config
	var $nick;
	var $realname;
	var $ident;
	var $prefixes=array();
	var $nickserv_pass=array();
	
	//message
	var $rawmsg;
	var $msginfo;
	
	//constant functions
	var $time_funcs;
	var $constant_funcs = array();
	
	//admin control
	var $admin=array();
	var $current_admin;
	var $locked=false;
	var $chan_lock=array();
	
	//help
	var $help=array();
	
	//bot state
	var $started=false;
	
	//last output
	var $last_output;
	
	/**************
	* CONNECTION FUNCTIONS
	**************/
	/*Connect to servers*/
	function connect($server) {
		$this->sockets[] = stream_socket_client("tcp://$server", $errno, $errstr, 30);
		end($this->sockets);
		$ids=key($this->sockets);
		if ($this->sockets[$ids] < 0) {
			die("socket_create() failed: ".socket_strerror(socket_last_error()));
		}
		if (!stream_set_blocking($this->sockets[$ids],0)) {
			die("socket_set_nonblock() failed: ".socket_strerror(socket_last_error()));
		}
		stream_set_timeout($this->sockets[$ids], 2);
		return $ids;
	}
	/*Join and quit channels properly*/
	function joinChannel($id,$channel) {
		$this->showDebug("Tring to join channel $channel");
		$this->sendMsg($id,"JOIN $channel");
	}
	function partChannel($id,$channel) {
		$this->showDebug("Tring to part channel $channel");
		$this->sendMsg($id,"PART $channel");
	}
	/*General Server config*/
	function joinServer($server, $channels,$nick_pass=false) {
		$id=$this->connect($server);
		$this->channels[$id]=$channels;
		if($nick_pass!==false) {
			//need password
			$this->nickserv_pass[$id]=$nick_pass;
		}
	}
	/*set prefixes*/
	function setPrefixes($prefixes) {
		$this->prefixes=array_merge($this->prefixes,$prefixes);
	}
	
	
	/*****************
	* SEND STUFF INFO FUNCTIONS
	*****************/
	/****Set irc info properties****/
	function setInfo($name,$ident,$realname) {
		$this->nick=$name;
		$this->realname=$realname;
		$this->ident=$ident;
	}
	/****Send and print out info to server****/
	function sendMsg($server,$sends) {
		$send=$sends."\n";
		if($server=="all" && is_string($server)) {
			foreach($this->sockets as $key => $value) {
				$this->showDebug("Looping through all sockets");
				fwrite($this->sockets[$key], $send);
			}
		}
		else {
			fwrite($this->sockets[$server], $send);
		}
		echo "[SEND][$server] ".$send;
	}
	/****Send messages to all channels****/
	function sendGlobMsg($server,$sends) {
		if($server==="all") {
			foreach($this->sockets as $id => $socket) {
				foreach($this->channels[$id] as $key => $channel) {
					$sendme="PRIVMSG $channel :{$sends}";
					$this->sendMsg($id, $sendme);
				}
			}
		}
		else {
			foreach($this->channels[$server] as $key => $channel) {
				$sendme="PRIVMSG $channel :{$sends}";
				$this->sendMsg($server, $sendme);
			}
		}
	}
	/***run functions****/
	function run_command($command,$args) {
		$id=$this->msginfo['id'];
		$this->showDebug("Function Exists, trying to run.");
		
		//generate args
		$argys="";
		foreach($args as $key => $value) {
			$argys.="\"$value\",";
		}
		$argys=substr($argys,0,-1);
		
		eval("\$output=\$this->$command($argys);");
		
		//send user a message only if its set
		if($output!==false) { 
			$this->sendMsg($id,"PRIVMSG {$this->msginfo['channel']} :{$this->msginfo['username']}: ".$output);
		}
		else {
			$this->showDebug("No output present");
		}
	}
	

	/************
	* CONSTANT FUNCTIONS
	**************/
	/***register time function for use in other functions****/
	function registerTimeFunc($func,$times) {
		$this->time_funcs[]['name']=$func;
		end($this->time_funcs);
		$ids=key($this->time_funcs);
		$this->time_funcs[$ids]['times']=$times;
	}
	/***if its around the right time, run time function***/
	function aroundTime($time,$tolerance=10) {
		foreach($this->time_funcs as $key => $sub_array) {
			/****determin type of time**/
			switch($sub_array['type']) {
				case "time":
					//based on unix epoch times
					for($i=$time-$tolerance; $i<=$time+$tolerance; $i++) {
						//check if time exists within tolerance level
						if(array_search($i,$sub_array)) {
							//execute time function
							$this->run_command($this->time_funcs[$key]['name']);
						}
					}
				break;
				case "relative":
					//every hour, every minuite, etc
					$time_array=getDate(time());
					
				break;
			}
		}
	}
	/***register functions that run every iteration***/
	function registerConstantFunc($func) {
		$this->constant_funcs[]=$func;
	}
	
	/******************
	* ADMIN CONTROL FUNCTIONS
	********************/
	function registerAdmin($username,$password) {
		$this->admin[] = array("user" => strtolower($username), "pass" => $password);
	}
	function login($password) {
		$lmsginfo=$this->msginfo;
		foreach($this->admin as $key => $admin_array) {
			$lower=strtolower($lmsginfo['username']);
			if($lower == $admin_array['user'] && $password == $admin_array['pass'])  {
				//username and password matches, add to array
				$this->current_admin[]=$lower;
				return "Successfully Logged in!";
			}
		}
		return "Wrong username or password";
	}
	function isAdmin($tell=true) {
		$lmsginfo=$this->msginfo;
		foreach($this->current_admin as $key => $admin) {
			$lower=strtolower($lmsginfo['username']);
			if($lower===$admin) {
				$this->showDebug("User $lower matches $admin and is an admin");
				return true;
			}
		}
		if($tell===true) {
			$this->showDebug("User $lower isn't an admin");
			//isn't an admin, tell user
			$this->sendMsg($lmsginfo['id'],"PRIVMSG {$lmsginfo['channel']} :{$lmsginfo['username']}: Function requires admin privliges");
		}
		return false;
	}	
	function lock($channel) {
		if($this->isAdmin()==true) {
			if(empty($channel)) {
				//lock bot globaly
				$this->locked=true;
				$this->sendGlobMsg("all","Bot has been globaly locked by {$this->msginfo['username']}");
				return false;
			}
			else {
				//lock only a channel
				if($channel==="this") {
					$channel=$this->msginfo['channel'];
				}
				$this->chan_lock[]=$channel;
				$this->sendMsg($this->msginfo['id'],"PRIVMSG {$channel} :Bot has been locked in this channel by {$this->msginfo['username']}");
				return false;
			}
		}
		else {
			return false;
		}
	}
	function unlock($channel) {
		if($this->isAdmin()==true) {
			if(empty($channel)) {
				//lock bot globaly
				$this->locked=false;
				$this->sendGlobMsg("all","Bot has been globaly unlocked by {$this->msginfo['username']}");
			}
			else {
				//unlock only a channel		
				if($channel==="this") {
					$channel=$this->msginfo['channel'];
				}			
				unset($this->chan_lock[array_search($channel,$this->chan_lock)]);
				$this->sendMsg($this->msginfo['id'],"PRIVMSG {$channel} :Bot has been unlocked in this channel by {$this->msginfo['username']}");
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**************
	* OTHER FUNCTIONS
	**************/
	/*Show debug messages properly*/
	function showDebug($message) {
		echo "[DEBUG]$message\n";
	}
	
	/**************
	* CLASS FUNCTIONS
	**************/
	//Add custom classes to global class var
	/*
	Disabled do to class not being defined yet, conflicting with porpose as class
	function addClass($class) {
		//Is input a string?
		if(is_string($class)) {
			eval("\$this->classs[]=new $class();"); //eval to do it properly
			$this->showDebug("Added class ".$class);
		}
	}*/
	
	/**************
	* HELP FUNCTIONS
	*************/
	function addHelp() {
		$func_name="";
		//loop through all defined class vars till we get to help
		//foreach($this->classs as $key => $class) {
		foreach($this as $key => $class) {
			$func_name="";
			//see if var starts with help_
			if(strtolower(substr($key,0,5))=="help_") {
				//get function name
				$func_name=substr($key,5);
				$this->help[$func_name]=$value;
				$this->showDebug("ADDED FUNCTION $func_name TO HELP");
			}
		}
	}
	
	/**************
	* Initialize and run everything
	***************/
	function init() {
		//Bot is started, function can't be called again
		$this->started=true;
		
		//Send bot info
		$this->sendMsg("all","USER {$this->ident} localhost evilxproductions.awardspace.com :{$this->realname}");
		$this->sendMsg("all","NICK {$this->nick}");
		
		//add all help definitions
		$this->addHelp();
		
		//run forever
		while(1) {
			$read = $this->sockets;
			stream_select($read, $w=null, $e=null, 5); //wait for data to be read
			foreach ($read as $r) { //loop through any info recieved
				$id=array_search($r, $this->sockets); //obtain socket id
				if(($data=fgets($r, 10240))===false) { //dump data into string
					unset($this->sockets[$id]);
					if(count($this->sockets) == 0) {
						die("Bot killed due to no more socket connections");
					}
				}
				//if string is empty, continue
				if (empty($data)) {
					continue;
				}
				/*****************
				* DATA HANDLING
				****************/
				$data=str_replace("\n","",$data); //strip newlines
				$data=trim($data); //trim usless stuff from ends
				echo "[RECIEVE][$id]: ".$data."\n"; //show that recieved something
				$this->rawmsg=$data; //make recived data globally accessable
				
				//Parse data
				$dataa=substr($data,1); //remove first chat
				$lmsginfo=array(); //setup empty array
				list($lmsginfo['allname'],$lmsginfo['ircmode'],$lmsginfo['channel'],$lmsginfo['msg']) = explode(' ', $dataa, 4); //store parts in array
				
				//Is this just a ping?
				if($lmsginfo['allname'] == "ING") {
					$this->sendMsg($id, "PONG ".$lmsginfo['ircmode']);
				}
				
				//Clean up string
				$lmsginfo['msg']=(substr($lmsginfo['msg'],0,1)==":") ? substr($lmsginfo['msg'],1) : $lmsginfo['msg']; //Remove : if it exists first
				$lmsginfo['msg']=trim($lmsginfo['msg']); //remove unneeded chars from message
				
				//Parse username
				list($lmsginfo['begining'],$lmsginfo['hostmask']) = explode("@", $lmsginfo['allname']);
				list($lmsginfo['username'],$lmsginfo['ident']) = explode("!",$lmsginfo['begining']);
				
				//Put id in info array
				$lmsginfo['id']=$id;
				
				//Make message array global
				$this->msginfo=$lmsginfo;
				
				/*************
				* FUNCTION HANDLING
				*************/
				//Check if its just a special command
				//Did motd just end?
				if($lmsginfo['ircmode']=="376") {
					//Now we can identify outself if a nickserv password was provided
					if(!empty($this->nickserv_pass[$lmsginfo['id']])) {
						$this->sendMsg($lmsginfo['id'],"NICKSERV IDENTIFY {$this->nickserv_pass[$lmsginfo['id']]}");
					}
					
					//Join all the requested channels on this server
					foreach($this->channels as $id => $channel_array) {
						foreach($channel_array as $key => $channeler) {
							$this->joinChannel($id,$channeler);
						}
						$this->ready_servers[$id]=true; //This server is now ready
					}
				}
		
				//Was bot info requested?
				if(substr($lmsginfo['msg'],0,1) == "") {
					$this->showDebug("-----------BOT ACTIVATED FROM {$lmsginfo['msg']}-----------"); //Show that bot is activated
					$command=str_replace(array(chr(001),""),"",$lmsginfo['msg']); //Cleanup message and get command
					$this->showDebug("Special Command: {$command}"); //show that this is a special command
					//Excecute requested command
					switch($command) {
						case "VERSION": 
							$this->sendMsg($lmsginfo['id'],"NOTICE {$lmsginfo['username']} :VERSION QuackBot V2 By LordQuackstar Copyright Evil X Productions");
						break;	
						case "FINGER": 
							$this->sendMsg($lmsginfo['id'],"NOTICE {$lmsginfo['username']} :FINGER QuackBot V2 By LordQuackstar Copyright Evil X Productions on PHP ".phpversion()."");
						break;
						case "TIME":
							$this->sendMsg($lmsginfo['id'],"NOTICE {$lmsginfo['username']} :TIME 	".date('D M d H:i:s Y')."");
						break;
					}
					//Was command a ping?
					$ping_array=explode(" ",$command);
					if($ping_array[0]=="PING") {
						$this->sendMsg($lmsginfo['id'],"NOTICE {$lmsginfo['username']} :{$command}");
					}
					$this->showDebug("-----------END-----------"); //Show bot is deactivated
				}
				
				//Does a prefix exist in the message?
				$prefix_ok=false;
				$beg_length=0;
				foreach($this->prefixes as $key => $prefix) {
					//Does it begin with prefix?
					if(stripos($lmsginfo['msg'],$prefix,0)===0) {
						$prefix_ok=true;
						$beg_length=mb_strlen($prefix);
						break;
					}		
				}
				
				//Is bot locked for this channel?
				$locked=false;
				foreach($this->chan_lock as $key => $channel) {
					if($lmsginfo['channel']==$channel) {
						$locked=true;
						break;
					}
				}
				
				//Is bot completly locked?
				if($this->locked==true) {
					$locked=true;
				}
				
				//Are all requirements met to run a command? (IRC command is a private message, has a prefix or is a pm, isn't output from bot)
				if($lmsginfo['ircmode']=="PRIVMSG" && ($prefix_ok==true || $channel==$this->nick) && $lmsginfo['username']!=$this->nick) {
					//Show that bot is activated
					$this->showDebug("-----------BOT ACTIVATED FROM {$lmsginfo['msg']}-----------");
					
					//check if bot locked
					if($locked===true) {
						if($this->isAdmin(false)===false) {
							//person isn't admin, show
							$this->showDebug("Bot locked and person isn't admin");
							$this->showDebug("-----------END-----------");
							continue; //stop processing
						}
					}
					
					//Remove prefix
					$rawcommand=trim(substr($lmsginfo['msg'],$beg_length));
					
					//split out data
					list($command,$end) = explode(" ",$rawcommand,2); //Get command and end
					$this->showDebug("\$Command: $command \$end: \"$end\""); //Show results
					
					//Do we have arguments?
					if(!empty($end)) {
						$args_pre=explode(",",$end); //Split args by comma
						//Clean args and save
						$args=array();
						foreach($args_pre as $key => $value) {
							$args[$key]=trim($value); //remove crap
						}
						$this->msginfo['args']=$args; //Make arg array global
						$this->msginfo['end']=$end; //Also make raw end global
					}
					//No args
					else {
						//Need to make everything empty so no complaining about unset vars
						$args=array();
						$this->msginfo['args']=array();
						$this->msginfo['end']="";
						$end="";
					}
					
					//check if requested function exists
					//foreach($this->classs as $key => $class) {
					if(method_exists(get_class($this), $command)) {
						//method exists, get info on it
						$method = new ReflectionMethod($this, $command);  //use reflection to reverse-engeneer class
						//Do we have a real, public method?
						if($method->isPrivate()===false && $method->isProtected()===false) {
							//get number of parameters needed
							$req_params=$method->getNumberOfRequiredParameters();
							$opt_params=$method->getNumberOfParameters();
							
							//Is this command in this class?
							if($method->getName()=="init") {
								//woah! User can't do this!
							}
							
							$user_num_args=count($this->msginfo['args']); //Count the number of user supplied args
							$this->showDebug("User # args: $user_num_args; Required: $req_params; Optional: $opt_params"); //Show argument values
							//Does the number of supplied args matched number of required or optional args?
							if($req_params==$user_num_args || $opt_params==$user_num_args || ($opt_params > $user_num_args && $user_num_args > $req_params)) {
								//run command
								$this->run_command($command,$this->msginfo['args']);
							}
							else {
								//wrong number of params
								$this->sendMsg($id,"PRIVMSG {$this->msginfo['channel']} :{$this->msginfo['username']}: ERROR: Invalid number of arguments! Passed: {$user_num_args} Required: {$req_params} Optional: $opt_parans. Use help {$command} for more information.");
							}
						}
					}
					//Command dosen't exist?
					else {
						$this->sendMsg($id,"PRIVMSG {$this->msginfo['channel']} :{$this->msginfo['username']}: ERROR: Command $command not found!");
					}
					$this->showDebug("-----------END-----------");
				}
			}
			
			//run constant functions
			foreach($this->ready_servers as $ids => $bools) {
				foreach($this->constant_funcs as $key => $command) {
					eval("\$return=\$this->$command();");
					if($return != false) $this->showDebug($return);
				}
			}
		}
	}
}

?>