# cordova-plugin-zuozishu-threadtcp
cordova plugin tcp socket thread, just for android now,  ios wait for later

* develop eviroment in android@6.4.0

# API
* ThreadTcp.onConnected = function(){}  //it will be trigger when the connect success
* ThreadTcp.onClose = function(){}  //it will be trigger when the connect closed
* ThreadTcp.onError = function(){}  //it will be trigger when the connect error
* ThreadTcp.onWarning = function(){}  //it will be trigger when the connect get wrong message data
* ThreadTcp.onMessage = function(data){};  //when get remote message data, it will be trigger
* ThreadTcp.reconnecttime = 3000;  //auto reconnect time,set it before connect,default false/0, if set it, when get Error/Close Event will be auto reconnect
* ThreadTcp.connect(config,successFunction,errorFunction);  //connect to remote, config = ['address string default 127.0.0.1',port int default 8989,timeout int default 5000,pingTime int default 3000,pingText string default 'ping']
* ThreadTcp.sendMessage(data,successFunction,errorFunction);  //send message to remote
