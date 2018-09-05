cordova.define("cordova-plugin-zuozishu-threadtcp.ThreadTcp",
    function(require, exports, module) {
        var exec = require("cordova/exec");
        module.exports = {
            connected: false,
            inited: false,
            reconnecttime: 3000,
            connecting: false,
            config:[],
            connect: function(args,cb,eb){
                ThreadTcp.initEvent();
                if(!args[0]) args[0] = '127.0.0.1';
                if(!args[1]) args[1] = 8989;
                if(!args[2]) args[2] = 5000;
                if(!args[3]) args[3] = 3000;
                if(!args[4]) args[4] = 'ping';
                ThreadTcp.config = args;
                if(!ThreadTcp.connecting){
                    ThreadTcp.connecting = true;
                    cordova.exec(function(e){
                        if(e==1){
                            ThreadTcp.connected = true;
                            if(typeof cb == 'function') cb(e);
                        }else{
                            if(typeof eb == 'function') eb(e);
                            ThreadTcp.connecting = false;
                        }
                    },function(e){
                        if(typeof eb == 'function') eb(e);
                        ThreadTcp.connecting = false;
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
            initEvent: function(){
                if(!ThreadTcp.inited){
                    ThreadTcp.inited = true;
                    document.addEventListener("ThreadTcpMessage",function(evt){
                        if(typeof ThreadTcp.onMessage == 'function')ThreadTcp.onMessage(evt.message);
                    });
                    document.addEventListener("ThreadTcpConnected",function(evt){
                        ThreadTcp.connected = true;
                        if(typeof ThreadTcp.onConnected == 'function')ThreadTcp.onConnected(evt);
                    });
                    document.addEventListener("ThreadTcpClose",function(evt){
                        ThreadTcp.connecting = false;
                        ThreadTcp.reconnect();
                        if(typeof ThreadTcp.onClose == 'function') ThreadTcp.onClose(evt);
                    });
                    document.addEventListener("ThreadTcpError",function(evt){
                        ThreadTcp.connecting = false;
                        ThreadTcp.reconnect();
                        if(typeof ThreadTcp.onError == 'function')ThreadTcp.onError(evt);
                    });
                    document.addEventListener("ThreadTcpWarning",function(evt){
                        ThreadTcp.connecting = false;
                        ThreadTcp.reconnect();
                        if(typeof ThreadTcp.onWarning == 'function')ThreadTcp.onWarning(evt);
                    });
                }
            },
            onMessage: function(data){
                console.log(data);
            },
            onConnected: function(evt){
                console.log(evt.address + ":" + evt.port);
            },
            onWarning: function(evt){
                console.log(evt.message);
            },
            onError: function(evt){
                console.log(evt.message);
            },
            onClose: function(evt){
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