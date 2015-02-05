package com.eseo.streamnshare.player.sink;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.eseo.streamnshare.player.source.SourceActionner;
import com.eseo.streamnshare.player.source.SourceObject;

public class SinkActionner {

	private BusAttachment mBus ;
	private BusHandler mBusHandler ;
	
	private int mSessionId = -1 ;
	private boolean mHasJoined = false ;
	
	private SourceObject mSourceObject = new SourceObject() ;
	private SinkObject mSinkObject = new SinkObject();
	private SinkInterface mSinkInterface ;
	
	private SinkPlayer mSinkPlayer  ;
	
	private Context mContext ;
	
	private static final int SHOW_TOAST = 1 ;

	private Handler mUIHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == SHOW_TOAST){
				String text = (String) msg.obj ;
				Toast.makeText(mContext,text,Toast.LENGTH_SHORT).show();
			}
		}
	};	
	
	
	public SinkActionner(Context context, SinkPlayer signalHandler) {
		org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(context.getApplicationContext());
		mBus =  new BusAttachment(context.getPackageName(), BusAttachment.RemoteMessage.Receive);
		
		mSinkPlayer = signalHandler ;
		mContext = context ;
		
	}
	
	public void startBusHandler(){
		HandlerThread handlerThread = new HandlerThread("BusHandler");
		handlerThread.start();
		mBusHandler = new BusHandler(handlerThread.getLooper());
		
	}
	
	public void stopBusHandler(){
		mBusHandler.sendEmptyMessage(BusHandler.EXIT);
	}
	
	public void connect(){
		mBusHandler.sendEmptyMessage(BusHandler.CONNECT);
	}
	
	public void disconnect(){
		mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
	}
	
	public void startDiscovery(){
		mBusHandler.sendEmptyMessage(BusHandler.START_DISCOVERY);
	}

	public void stopDiscovery(){
		mBusHandler.sendEmptyMessage(BusHandler.STOP_DISCOVERY);
	}
	
	
	public void joinSession(){
		mBusHandler.sendEmptyMessage(BusHandler.JOIN_SESSION);
	}
	
	public void quitSession(){
		mBusHandler.sendEmptyMessage(BusHandler.QUIT_SESSION);
	}
	
	public void sendSignalFifo(){
		mBusHandler.sendEmptyMessage(BusHandler.SIGNAL_FIFO);
	}
	
	

	class BusHandler extends Handler {

		public static final int CONNECT = 1;
		public static final int DISCONNECT = 2;
		public static final int START_DISCOVERY = 3;
		public static final int STOP_DISCOVERY = 4;
		public static final int JOIN_SESSION = 5;
		public static final int QUIT_SESSION = 6;
		public static final int SIGNAL_FIFO = 7;
		public static final int EXIT = 8;

		public BusHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch(msg.what){
			case CONNECT : {
				doConnect();
				break ;
			}
			case DISCONNECT : {
				doDisconnect();
				break ;
			}
			case START_DISCOVERY : {
				doStartDiscovery();
				break ;
			}
			case STOP_DISCOVERY : {
				doStopDiscovery();
				break ;
			}
			case JOIN_SESSION : {
				doJoinSession();
				break ;
			}
			case QUIT_SESSION : {
				doQuitSession();
				break ;
			}
			case SIGNAL_FIFO: {
				doSendSignalFifo();
				break;
			}
			case EXIT: {
				getLooper().quit();
				break;
			}
			default :
				break ;
			}
		}
	}

	private void doConnect() {
        mBus.registerBusListener(new BusListener() {
			@Override
			public void foundAdvertisedName(String name, short transport, String namePrefix) {
				if (! mHasJoined){
					//sendMessageToUI("find ad ");
					joinSession();
				}
			}
		});
        
        Status status = mBus.registerBusObject(mSourceObject, "/source");
        if(status != Status.OK) return ;
        status = mBus.registerBusObject(mSinkObject, "/sink");
        if(status != Status.OK) return ;
		status = mBus.connect();
		if(status != Status.OK) return ;
		status = mBus.registerSignalHandlers(mSinkPlayer);
       	if(status != Status.OK) return ;
       	
		sendMessageToUI("Start service ");
		
	}

	private void doDisconnect() {
		mBus.unregisterBusObject(mSourceObject);
		mBus.disconnect();
		mBusHandler.getLooper().quit();
		
		sendMessageToUI("Stop service ");

		
	}
	
	private void doStartDiscovery(){
		Status status = mBus.findAdvertisedName(SourceActionner.SERVICE_NAME);
		if(status != Status.OK) return ;
		
		//sendMessageToUI("Start disco ");

	}

	private void doStopDiscovery(){
		 mBus.cancelFindAdvertisedName(SourceActionner.SERVICE_NAME);
		 
			//sendMessageToUI("Stop discovery ");

	}

	private void doJoinSession() {
		if (!mHasJoined) {
			short servicePort = SourceActionner.SERVICE_PORT ;
			SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
			Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

			Status status = mBus.joinSession(SourceActionner.SERVICE_NAME, servicePort, sessionId, sessionOpts, new SessionListener(){
				@Override
				public void sessionLost(int sessionId, int reason) {
					mHasJoined = false;
					mSinkPlayer.stop();
					startDiscovery();
					sendMessageToUI("Session lost");
				}
			});

			if (status == Status.OK) {
				mSessionId = sessionId.value;
				mHasJoined = true;
				SignalEmitter emitter = new SignalEmitter(mSinkObject,mSessionId, SignalEmitter.GlobalBroadcast.Off);
				mSinkInterface = emitter.getInterface(SinkInterface.class);
				stopDiscovery();
				sendMessageToUI("Join session ");
			}
		}
		
		

		
	}

	private void doQuitSession() {
		mBus.leaveSession(mSessionId);
		mSessionId = -1 ;
		mHasJoined = false ;
	}
	
	private void doSendSignalFifo(){
		if(mSinkInterface != null){
			try {
				mSinkInterface.signalFifo();
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void sendMessageToUI(String text){
		Message msg = mUIHandler.obtainMessage(SHOW_TOAST, text);
		mUIHandler.sendMessage(msg);
	}
	
	

}
