package oscar.presidente.radioluz;

import java.io.IOException;
import oscar.presidente.radioluz.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;
import com.pheelicks.visualizer.renderer.CircleBarRenderer;
import com.pheelicks.visualizer.renderer.CircleRenderer;
import com.pheelicks.visualizer.renderer.LineRenderer;

public class MainActivity extends Activity  {

	private static final String TAG =            MainActivity.class.getSimpleName();
	private static final Uri RADIO_STREAM_LINK = Uri.parse("http://giss.tv:8001/deejayonline.mp3");

	private int             mVolumeValue;
	private ToggleButton    mVolumeSign;
	private Button          mPlayButton;
	private Button          mStopButton;
	private AudioManager    mAudioManager;
	private MediaPlayer     mMediaPlayer;
	private SeekBar         mVolumeGauge;
	private VisualizerView  mVisualizerView;
	private AdView          mAdView;
	private WifiLock 		wifiLock;

	private static final int REFRESH_RATE = 1000;
	private float            mScreenBrightness;
	private double           mSecondsToDim;
	private Handler          mHandler;
	private Runnable         mDimRunnable = new Runnable() {

		@Override
		public void run() {
			if ( mSecondsToDim >= 0.0 ) {
				mSecondsToDim--;
				mHandler.postDelayed(this,REFRESH_RATE);
			} else {
				setScreenBrightness(0.01f);
				mHandler.removeCallbacks(this);
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
			    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock");
		wifiLock.acquire();
		setContentView(R.layout.main_activity);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mAdView = (AdView)findViewById(R.id.ad_view);
		mAdView.loadAd(new AdRequest().addTestDevice(AdRequest.TEST_EMULATOR));
		init();
	}

	protected void onResume() {
		super.onResume();
		//init();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	protected void onStop() {
		setScreenBrightness(mScreenBrightness);
		super.onStop();
	}
	
	protected void onDestroy() {
		mMediaPlayer.release();
		wifiLock.release();
		super.onDestroy();
	}

	private void setScreenBrightness(float brightness) {
		WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
		lp.screenBrightness = brightness;
		MainActivity.this.getWindow().setAttributes(lp);
	}

	private void init() {		
		mStopButton = (Button)findViewById(R.id.stop_button);
		mPlayButton = (Button)findViewById(R.id.play_button);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mVolumeGauge  = (SeekBar)findViewById(R.id.volume_gauge);
		mVolumeGauge.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		mVolumeGauge.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		mVolumeGauge.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			}
		});
		
		mVolumeValue = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mVolumeSign   = (ToggleButton)findViewById(R.id.volume_sign);
		mVolumeSign.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if ( isChecked ) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeValue, 0);	
				} else {
					mVolumeValue = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
				}
			}
		});
		

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setScreenOnWhilePlaying (false);

		mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
			}
		});
		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d(TAG, "I'm ready...");
				mMediaPlayer.start();

			}
		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG,  "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
				return false;
			}
		});

		try{
			mMediaPlayer.setDataSource(this, RADIO_STREAM_LINK);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		mVisualizerView = (VisualizerView)findViewById(R.id.music_visualizerView);
		mVisualizerView.link(mMediaPlayer);
		addBarGraphRenderers();

		mSecondsToDim = 15.0;
		mHandler = new Handler();
		mHandler.post(mDimRunnable);		

		try {
			mScreenBrightness = android.provider.Settings.System.getInt(getContentResolver(), 
					android.provider.Settings.System.SCREEN_BRIGHTNESS)/255.0f;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	public void onPlayButtonPressed(View v) {
		try {
			mMediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		mStopButton.setVisibility(View.VISIBLE);
		mPlayButton.setVisibility(View.GONE);
	}

	public void onStopButtonPressed(View v) {
		mMediaPlayer.pause();
		mStopButton.setVisibility(View.GONE);
		mPlayButton.setVisibility(View.VISIBLE);
	}
	
	private void addCircleRenderer()
	{
		Paint paint = new Paint();
		paint.setStrokeWidth(3f);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(255, 222, 92, 143));
		CircleRenderer circleRenderer = new CircleRenderer(paint, true);
		mVisualizerView.addRenderer(circleRenderer);
	}
	
	private void addCircleBarRenderer()
	{
		Paint paint = new Paint();
		paint.setStrokeWidth(8f);
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
		paint.setColor(Color.argb(255, 222, 92, 143));
		CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
		mVisualizerView.addRenderer(circleBarRenderer);
	}

	private void addBarGraphRenderers() {
		Paint paint = new Paint();
		paint.setStrokeWidth(50f);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(190, 255, 255, 51));
		BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(20, paint, false);
		mVisualizerView.addRenderer(barGraphRendererBottom);

		Paint paint2 = new Paint();
		paint2.setStrokeWidth(12f);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.argb(200, 51, 153, 255));
		BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, false);
		mVisualizerView.addRenderer(barGraphRendererTop);
	}

	private void addLineRenderer()
	{
		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(1f);
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.argb(88, 51, 0, 255));

		Paint lineFlashPaint = new Paint();
		lineFlashPaint.setStrokeWidth(5f);
		lineFlashPaint.setAntiAlias(true);
		lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
		LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
		mVisualizerView.addRenderer(lineRenderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mSecondsToDim = 15.0;
		setScreenBrightness(mScreenBrightness);
		mHandler.post(mDimRunnable);
		return false;
	}
}
