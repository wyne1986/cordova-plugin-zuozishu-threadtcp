package com.zuozishu;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class ThreadTcp extends CordovaPlugin {

    public static ThreadTcp instance ;
    public static final String TAG = "com.zuozishu.ThreadTcp";
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected Socket socket = null;
    protected SocketAddress remoteAddr = null;
    protected static Integer status = 0;
    private static Thread t2 = null;
    private static CordovaArgs config;
    private Timer timer = null;
    private TimerTask timerTask = null;

    @Override
    protected void pluginInitialize() {

        super.pluginInitialize();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("connect")) {
            config = args;
            final CordovaArgs arg = args;
            final CallbackContext context = callbackContext;
            if(t2 != null)t2.interrupt();
            t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        connect(arg, context);
                    }catch(JSONException e){

                    }
                }
            });
            t2.start();
            callbackContext.success(1);
            return true;
        } else if (action.equals("sendMessage")) {
            return sendMessage(args, callbackContext);
        } else if (action.equals("getStatus")) {
            return getStatus(args, callbackContext);
        }

        return false;
    }

    protected void connect(final CordovaArgs args, final CallbackContext callbackContext)
            throws JSONException {
        final String address = args.getString(0);
        final Integer port = args.getInt(1);
        final Integer timeout = args.getInt(2);
        final Integer period = args.getInt(3);
        final String ping = args.getString(4);
        try {
            if(socket == null) socket = new Socket();
            if(remoteAddr == null) remoteAddr = new InetSocketAddress(address,port);
            if(!socket.isConnected()){
                socket.connect(remoteAddr,timeout);
                socket.setKeepAlive(true);
                status = 1;
            }
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            timer = new Timer();
             timerTask = new TimerTask() {
                 @Override
                 public void run() {
                         try {
                             outputStream.write(ping.getBytes());
                             outputStream.flush();
                         } catch (IOException e) {
                             cordova.getActivity().runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     status = 0;
                                     webView.loadUrl("javascript:(function(){\n" +
                                             "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                             "        evt.message = 'connect has been closed from remote "+address+":"+port+"';evt.initEvent(\"ThreadTcpClose\", true, true);\n" +
                                             "        document.dispatchEvent(evt);})()");
                                     release();
                                 }
                             });
                         }
                 }
             };
            while (socket.isConnected()) {
                if(status == 1){
                    status = 2;
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:(function(){\n" +
                                    "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                    "        evt.message = 'connected';evt.address = '"+ address +"';evt.port = "+port+";evt.initEvent(\"ThreadTcpConnected\", true, true);\n" +
                                    "        document.dispatchEvent(evt);})()");
                            timer.schedule(timerTask,period,period);
                        }
                    });
                }
                try{
                    final byte[] bytes = new byte[inputStream.available()];
                    if(bytes.length>0){
                        inputStream.read(bytes);
                        cordova.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:ThreadTcp.getMessage('" + new String(bytes) + "');");
                            }
                        });
                    }
                }catch(final IOException ioe){
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:(function(){\n" +
                                    "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                    "        evt.message = '" + ioe.getMessage() + "';evt.initEvent(\"ThreadTcpWarning\", true, true);\n" +
                                    "        document.dispatchEvent(evt);})()");
                        }
                    });
                }
            }
        } catch (final Exception e) {
            release();
            if (e instanceof SocketTimeoutException) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){\n" +
                                "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                "        evt.message = 'connect timeout to remote "+address+":"+port+"';evt.initEvent(\"ThreadTcpError\", true, true);\n" +
                                "        document.dispatchEvent(evt);})()");
                    }
                });
            } else if (e instanceof NoRouteToHostException) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){\n" +
                                "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                "        evt.message = 'address not found on "+address+":"+port+"';evt.initEvent(\"ThreadTcpError\", true, true);\n" +
                                "        document.dispatchEvent(evt);})()");
                    }
                });
            } else if (e instanceof ConnectException) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){\n" +
                                "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                "        evt.message = 'connect has been refused by remote "+address+":"+port+"';evt.initEvent(\"ThreadTcpError\", true, true);\n" +
                                "        document.dispatchEvent(evt);})()");
                    }
                });
            } else {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:(function(){\n" +
                                "        var evt = document.createEvent(\"HTMLEvents\");\n" +
                                "        evt.message = '"+e.getMessage()+"';evt.initEvent(\"ThreadTcpError\", true, true);\n" +
                                "        document.dispatchEvent(evt);})()");
                    }
                });
            }
        }
    }

    protected boolean sendMessage(CordovaArgs args, CallbackContext callbackContext)
            throws JSONException {
        try {
            if(status == 2){
                outputStream.write(new String(args.getString(0)).getBytes());
                outputStream.flush();
                callbackContext.success(1);
                return true;
            }else{
                callbackContext.error("not connect");
                return false;
            }
        } catch (IOException e) {
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    protected boolean getStatus(CordovaArgs args, CallbackContext callbackContext){
        callbackContext.success(status);
        return true;
    }

    private void release(){
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }
        if (t2 != null) {
            t2 = null;
        }
    }
}
