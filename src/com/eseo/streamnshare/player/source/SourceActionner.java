package com.eseo.streamnshare.player.source;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.eseo.streamnshare.model.AudioMetadata;
import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.player.StatesPlayer;
import com.eseo.streamnshare.player.sink.SinkObject;

public class SourceActionner  {

	public static final String SERVICE_NAME = "com.eseo.streamnshare.player.source.service";
	public static final short SERVICE_PORT = 42;

	private BusAttachment mBus ;
	private BusHandler mBusHandler ;
	private Mutable.ShortValue mSessionPort ;
	private SessionOpts mSessionOpts ;

	private SourceInterface mSourceInterface ;
	private SourceObject mSourceObject = new SourceObject();
	private SinkObject mSinkObject = new SinkObject();
	
	private SourcePlayer mSourcePlayer ;
	
	private AudioMetadata mAudioMetadata ;
	private AudioTrackConfig mConfig ;

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

	public SourceActionner(Context context,SourcePlayer sourcePlayer) {
		org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(context.getApplicationContext());
		mBus =  new BusAttachment(context.getPackageName(), BusAttachment.RemoteMessage.Receive);

		mSessionPort = new Mutable.ShortValue(SERVICE_PORT);
		mSessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);

		mConfig = sourcePlayer.getConfig() ;
		
		mSourcePlayer = sourcePlayer;

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

	public void startAdvertisingName(){
		mBusHandler.sendEmptyMessage(BusHandler.START_ADVERTISING_NAME);
	}

	public void stopAdvertisingName(){
		mBusHandler.sendEmptyMessage(BusHandler.STOP_ADVERTISING_NAME);
	}

	public void bindSession(){
		mBusHandler.sendEmptyMessage(BusHandler.BIND_SESSION);
	}

	public void unbindSession(){
		mBusHandler.sendEmptyMessage(BusHandler.UNBIND_SESSION);
	}

	public void sendAudioConfig(AudioTrackConfig config){
		Message msg = mBusHandler.obtainMessage(BusHandler.SIGNAL_AUDIO_CONFIG,config);
		mBusHandler.sendMessage(msg);
	}

	public void sendAudioData(byte[] data, int position){
		Message msg = mBusHandler.obtainMessage(BusHandler.SIGNAL_AUDIO_DATA,data);
		msg.arg1 = position ;
		mBusHandler.sendMessage(msg);
	}

	public void sendMetadata(AudioMetadata metadata){
		Message msg = mBusHandler.obtainMessage(BusHandler.SIGNAL_METADATA,metadata);
		mBusHandler.sendMessage(msg);
	}
	
	public void sendState(StatesPlayer state){
		Message msg = mBusHandler.obtainMessage(BusHandler.SIGNAL_PLAYERSTATE,state);
		mBusHandler.sendMessage(msg);
	}


	class BusHandler extends Handler {

		public static final int CONNECT = 1;
		public static final int DISCONNECT = 2;
		public static final int START_ADVERTISING_NAME = 3;
		public static final int STOP_ADVERTISING_NAME = 4;
		public static final int BIND_SESSION = 5;
		public static final int UNBIND_SESSION = 6;
		public static final int SIGNAL_AUDIO_CONFIG = 7;
		public static final int SIGNAL_AUDIO_DATA = 8;
		public static final int SIGNAL_METADATA = 9;
		public static final int SIGNAL_PLAYERSTATE = 10;
		public static final int EXIT = 11;

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
			case START_ADVERTISING_NAME : {
				doStartAdvertisingName();
				break ;
			}
			case STOP_ADVERTISING_NAME : {
				doStopAdvertisingName();
				break ;
			}
			case BIND_SESSION : {
				doBindSession();
				break ;
			}
			case UNBIND_SESSION : {
				doUnbindSession();
				break ;
			}
			case SIGNAL_AUDIO_CONFIG: {
				AudioTrackConfig config = (AudioTrackConfig) msg.obj ;
				doSendAudioConfig(config);
				break;
			}
			case SIGNAL_AUDIO_DATA: {
				byte[] audioData = (byte []) msg.obj ;
				int position = msg.arg1 ;
				doSendAudioData(audioData,position);
				break;
			}
			case SIGNAL_METADATA: {
				AudioMetadata metadata = (AudioMetadata) msg.obj ;
				doSendMetadata(metadata);
				break;
			}
			case SIGNAL_PLAYERSTATE : {
				StatesPlayer state = (StatesPlayer) msg.obj ;
				doSendState(state);
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
		mBus.registerBusListener(new BusListener());
		Status status = mBus.registerBusObject(mSourceObject, "/source");
		if(status != Status.OK)  return;
		status = mBus.registerBusObject(mSinkObject, "/sink");
		if(status != Status.OK)  return;
		status = mBus.connect();
		if(status != Status.OK)  return;

		status = mBus.registerSignalHandlers(mSourcePlayer);
       	if(status != Status.OK) return ;
		
		sendMessageToUI("Start Service");


	}

	private void doDisconnect() {
		mBus.unregisterBusObject(mSourceObject);
		mBus.unregisterBusObject(mSinkObject);
		mBus.disconnect();
		mBusHandler.getLooper().quit();

		sendMessageToUI("Stop Service");

	}

	private void doStartAdvertisingName() {
		Status status = mBus.requestName(SERVICE_NAME,BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE);
		if (status != Status.OK)  return ;
		status = mBus.advertiseName(SERVICE_NAME, SessionOpts.TRANSPORT_ANY);
		if (status != Status.OK) return ;

		//sendMessageToUI("start ad");

	}

	private void doStopAdvertisingName() {
		Status status = mBus.releaseName(SERVICE_NAME);
		if (status != Status.OK) return ;

		status = mBus.cancelAdvertiseName(SERVICE_NAME, SessionOpts.TRANSPORT_ANY);
		if (status != Status.OK) return ;

		//sendMessageToUI("Stop ad");

	}

	private void doBindSession() {
		Status status = mBus.bindSessionPort(mSessionPort, mSessionOpts, new SessionPortListener() {
			@Override
			public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
				if (sessionPort == SERVICE_PORT) {
					sendMessageToUI("Accept new Sink");
					return true;
				} 
				else {
					return false;
				}
			}

			@Override
			public void sessionJoined(short sessionPort, int id, String joiner) {
				super.sessionJoined(sessionPort, id, joiner);
				SignalEmitter emitter = new SignalEmitter(mSourceObject,joiner, id, SignalEmitter.GlobalBroadcast.Off);
				mSourceInterface = emitter.getInterface(SourceInterface.class);
			}
			
			
		});

		if (status != Status.OK) return ;

		//sendMessageToUI("BIND");


	}

	private void doUnbindSession() {
		mBus.unbindSessionPort(SERVICE_PORT);
		mSourceInterface = null ;
		//sendMessageToUI("UNBIND");
	}

	private void doSendAudioData(byte[] audioData, int position) {
		if(mSourceInterface != null){
			try {
				mSourceInterface.signalAudioData(audioData,position);
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
	}

	private void doSendAudioConfig(AudioTrackConfig config) {
		if(mSourceInterface != null){
			try {
				mSourceInterface.signalAudioTrackConfig(config);
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
	}

	private void doSendMetadata(AudioMetadata metadata) {
		if(mSourceInterface != null){
			try {
				mSourceInterface.signalMetadata(metadata);
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doSendState(StatesPlayer state) {
		if(mSourceInterface != null){
			try {
				mSourceInterface.signalStatePlayer(state);
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendMessageToUI(String text){
		Message msg = mUIHandler.obtainMessage(SHOW_TOAST, text);
		mUIHandler.sendMessage(msg);
	}

	public AudioMetadata getmAudioMetadata() {
		return mAudioMetadata;
	}

	public void setmAudioMetadata(AudioMetadata mAudioMetadata) {
		this.mAudioMetadata = mAudioMetadata;
	}

	public AudioTrackConfig getmConfig() {
		return mConfig;
	}

	public void setmConfig(AudioTrackConfig mConfig) {
		this.mConfig = mConfig;
	}


}
