cordova.define("cordova-plugin-zuozishu-threadtcp.ThreadTcp", function(require, exports, module) {
var exec = require("cordova/exec");
module.exports = {
	connected: false,
	reconnecttime: 5000,
	config:[],
	connect: function(args,cb,eb){
		if(!args[0]) args[0] = '127.0.0.1';
		if(!args[1]) args[1] = 8989;
		if(!args[2]) args[2] = 5000;
		if(!args[3]) args[3] = 3000;
		if(!args[4]) args[4] = 'ping';
		ThreadTcp.config = args;
		if(!ThreadTcp.connected){
			ThreadTcp.connected = true;
			cordova.exec(function(e){
				if(e==1){
					ThreadTcp.connected = true;
					if(typeof cb == 'function') cb(e);
				}else{
					if(typeof eb == 'function') eb(e);
					ThreadTcp.connected = false;
				}
			},function(e){
				if(typeof eb == 'function') eb(e);
				ThreadTcp.connected = false;
			},'ThreadTcp','connect',args);
		}
	},
	sendMessage: function(data,cb,eb){
		cordova.exec(function(e){
			if(e==1){
				if(typeof cb == 'function') cb(e);
			}else{
				if(typeof eb == 'function') eb(e);
			}
		},function(e){
			if(typeof eb == 'function') eb(e);
		},'ThreadTcp','sendMessage',[data]);
	},
	getStatus: function(cb,eb){
		cordova.exec(function(e){
			if(typeof cb == 'function') cb(e);
		},function(e){
			if(typeof eb == 'function') eb(e);
		},'ThreadTcp','isConnected',[]);
	},
	onMessage: function(data){
		console.log(decodeURI(data));
	},
	onConnected: function(evt){
		console.log(evt.address + ":" + evt.port);
	},
	onWarning: function(evt){
		console.log(evt.message);
	},
	onError: function(evt){
        ThreadTcp.connected = false;
        ThreadTcp.reconnect();
		console.log(evt.message);
	},
	onClose: function(evt){
        ThreadTcp.connected = false;
        ThreadTcp.reconnect();
		console.log(evt.message);
	},
	reconnect:function(){
		var reconnecttime = parseInt(ThreadTcp.reconnecttime);
		if(reconnecttime>0 && ThreadTcp.config.length>0){
			setTimeout(function(){
				ThreadTcp.connect(ThreadTcp.config,function(d){
					console.log(d);
				},function(){
					console.log(e);
				});
			},reconnecttime);
		}
	}
}
});
