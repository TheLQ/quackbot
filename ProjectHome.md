Quackbot is a Java based IRC bot framework/program designed to be a simple, easy to use, and easy to write for bot.

Other frameworks have little or no documentation, are strange to use, or require complicated setup with XML files. Quackbot requires none of this but includes the documentation (soon), and can be started in as little as 3 lines.

Important Features
  * No complicated setup or XML
  * Extensive documentation (soon)
  * Can develop commands in only a few lines of code
  * Hook driven architecture
  * Plugins can written in Java OR Javascript (more languages in the future)

### WARNING ###

**Quackbot is currently going through a major rewrite.** This includes porting to  [PircBotX](http://pircbotx.googlecode.com), an updated event system, updated storage system, more features, etc. The following example code should be considered out of date

Howto start QuackBot in only 3 lines of code
```
Controller ctrl = new Controller();
ctrl.connectDB("DBName", 10, "com.mysql.jdbc.Driver", "connectionString", null, null, "username", "password");
ctrl.start();
```

Howto say hello when a user says ?hey
```
public class Hey implements JavaBase {
        public void invoke(Bot qb, BotEvent msgInfo) throws Exception {
                qb.sendMsg(new BotMessage(msgInfo,"Hello!"));
        }
}
```

How to give help for a command when a user says ?help Hey
```
@HelpDoc("Responds with Hello")
public class Hey implements JavaBase {
        public void invoke(Bot qb, BotEvent msgInfo) throws Exception {
                qb.sendMsg(new BotMessage(msgInfo,"Hello!"));
        }
}
```

How to say back what the user said
```
@ParamConfig({"userSaid"})
public class Hey implements JavaBase {
        String userSaid;
        public void invoke(Bot qb, BotEvent msgInfo) throws Exception {
                qb.sendMsg(new BotMessage(msgInfo,"You said "+userSaid));
        }
}
```

How to write a command in JavaScript
```
function invoke() {
        qb.sendMsg(new BotMessage(msgInfo,"Hello! "));
}
```

Howto write a command in JavaScript with help and parameters
```
var help = "Says what you told me!";
var param = 1;
function invoke() {
        qb.sendMsg(new BotMessage(msgInfo,"You said "+msgInfo.args[0]));
}
```

Much more examples and documentation on the way, probably whenever Quackbot gets out of alpha state.

Currently the bot is going through its 3rd complete code rewrite in Java, moving on from the old PHP based code.

Quackbot uses PircBotX, a fork of the popular PircBot framework, available here: http://pircbotx.googlecode.com/