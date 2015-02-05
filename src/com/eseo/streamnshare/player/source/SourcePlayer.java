package com.eseo.streamnshare.player.source;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.alljoyn.bus.annotation.BusSignalHandler;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Message;
import android.os.Process;

import com.eseo.streamnshare.model.AudioMetadata;
import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.player.EventPlayer;
import com.eseo.streamnshare.player.HandlerPlayer;
import com.eseo.streamnshare.player.Player;

public class SourcePlayer extends Player{


	private MediaExtractor extractor = null;
	private MediaCodec decoder = null ;
	private MediaFormat format = null;

	private SourceActionner mSourceActionner ;

	private Uri source = null ;

	private long presentationTimeUs = 0 ;

	private boolean available = true ;

	

	public SourcePlayer(Context context) 
	{
		mHandlerPlayer = new HandlerPlayer(this);
		mContext = context ;
		mSourceActionner = new SourceActionner(mContext,this);
	}

	public SourcePlayer(Context context,EventPlayer events) {
		mHandlerPlayer = new HandlerPlayer(this,events);
		mContext = context ;
		mSourceActionner = new SourceActionner(mContext,this);
	}

	public void setSource(Song song) {
		currentSong = song ;
		source = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currentSong.getId());
	}


	public void seek(int percent) {
		long pos = percent * mConfig.duration / 100;
		extractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	}

	@Override
	public void pause() {
		super.pause();
		if(ready){
			mSourceActionner.sendState(mState);
		}
	}

	@Override
	public void play() {
		super.play();
		if(ready){
			mSourceActionner.sendState(mState);
		}
	}

	@Override
	public void stop() {
		super.stop();
		if(ready){
			mSourceActionner.sendState(mState);
		}
	}


	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

		available = false ;
		
		mSourceActionner.sendMetadata(buildMetadata());

		extractSource(); if(mError) return ;
		readHeader();  if(mError) return ;
		createDecoder();  if(mError) return ;
		
		
		mSourceActionner.sendAudioConfig(mConfig);
		configureAudioTrack(); 

		Message startMessage = mHandlerPlayer.obtainMessage(EventPlayer.START,currentSong);
		mHandlerPlayer.sendMessage(startMessage);

		ByteBuffer[] codecInputBuffers = decoder.getInputBuffers();
		ByteBuffer[] codecOutputBuffers = decoder.getOutputBuffers();

		long timeOutUs = 500;
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		boolean inputEndOfStream = false;

		while (!mStop) {
			makePause();
			
			if (!inputEndOfStream) {
				int inputBufIndex = decoder.dequeueInputBuffer(timeOutUs);
				if (inputBufIndex >= 0) {
					ByteBuffer buf = codecInputBuffers[inputBufIndex];
					int sampleSize = extractor.readSampleData(buf, 0);
					if (sampleSize < 0) {
						inputEndOfStream = true;
						sampleSize = 0;
					} 
					else {
						presentationTimeUs = extractor.getSampleTime();
					}
					decoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, inputEndOfStream ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
					if (!inputEndOfStream){
						extractor.advance();
					}
				}
			}

			int outputBufferIndex = decoder.dequeueOutputBuffer(info,timeOutUs);

			if (outputBufferIndex >= 0 ) {
				ByteBuffer buf = codecOutputBuffers[outputBufferIndex];

				final byte[] chunk = new byte[info.size];
				buf.get(chunk);
				buf.clear();

				if(chunk.length >0){
					chunks.add(chunk);
					int percent =  (mConfig.duration == 0)? 0 : (int) (1000 * presentationTimeUs / mConfig.duration);
					positions.add(percent);
					mSourceActionner.sendAudioData(chunk,percent);
				}
				decoder.releaseOutputBuffer(outputBufferIndex, false);
			}
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				codecOutputBuffers = decoder.getOutputBuffers();
			} 
			else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				MediaFormat nFormat = decoder.getOutputFormat();
			} 
			
			if(ready){
				if(!chunks.isEmpty()){
					byte[] currentChunk = chunks.remove() ;
					int currentPos = positions.remove();
					Message msg = mHandlerPlayer.obtainMessage(EventPlayer.SEEK,currentPos);
					mHandlerPlayer.sendMessage(msg);
					mAudioTrack.write(currentChunk,0,currentChunk.length);
				}
				else {
					stop();
				}
			} 
			
		}
//		}
		clear();

		available = true ;


		mSourceActionner.sendState(mState);
		mHandlerPlayer.sendEmptyMessage(EventPlayer.STOP);
	}


	private void extractSource(){
		extractor = new MediaExtractor();
		try {
			if (source != null) {
				extractor.setDataSource(mContext,source,null);
			}
			else {
				mHandlerPlayer.sendEmptyMessage(EventPlayer.ERROR);
			}
		} 
		catch (Exception e) {
			mHandlerPlayer.sendEmptyMessage(EventPlayer.ERROR);
		}
	}

	private void readHeader(){
		try {
			format = extractor.getTrackFormat(0);
			mConfig = new AudioTrackConfig();
			mConfig.mime = format.getString(MediaFormat.KEY_MIME);
			mConfig.sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			mConfig.channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			mConfig.duration = format.getLong(MediaFormat.KEY_DURATION);
		} 

		catch (Exception e) {
			mHandlerPlayer.sendEmptyMessage(EventPlayer.ERROR);
		}

		if (format == null || !mConfig.mime.startsWith("audio/")) {
			mHandlerPlayer.sendEmptyMessage(EventPlayer.ERROR);
		}
	}

	private void createDecoder(){

		try {
			decoder = MediaCodec.createDecoderByType(mConfig.mime);
			decoder.configure(format, null, null, 0);
			decoder.start();
			extractor.selectTrack(0);
		}
		catch(Exception e){
			mHandlerPlayer.sendEmptyMessage(EventPlayer.ERROR);
		}
	}

	protected void clear (){
		super.clear();
		if(decoder != null) {
			decoder.stop();
			decoder.release();
			decoder = null;
		}
		chunks.clear();
	}

	@BusSignalHandler(iface="com.eseo.streamnshare.player.sink",signal="signalFifo")
	public void receivedSignalFifo() {
		if(!ready){
			ready = true ;
		}
	}

	public SourceActionner getSourceActionner() {
		return mSourceActionner;
	}

	public void setSourceActionner(SourceActionner mSourceActionner) {
		this.mSourceActionner = mSourceActionner;
	}

	public long getPosition(){
		return presentationTimeUs;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	private AudioMetadata buildMetadata(){
		AudioMetadata metadata = new AudioMetadata();
		metadata.albumName = currentSong.getAlbumName();
		metadata.artistName = currentSong.getArtist();
		metadata.songName = currentSong.getTitle();
		metadata.duration = currentSong.getDuration();
		
		return metadata ;
		
	}



}
