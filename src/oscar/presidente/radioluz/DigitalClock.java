package oscar.presidente.radioluz;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Like AnalogClock, but digital.  Shows seconds.
 *
 * FIXME: implement separate views for hours/minutes/seconds, so
 * proportional fonts don't shake rendering
 */

public class DigitalClock extends TextView {

	private final static String TAG = DigitalClock.class.getSimpleName();
	Calendar mCalendar;
	private SimpleDateFormat mDateFormat;
	private final static String m12 = "h:mm aa\nEEEE MMMM, dd";
	private final static String m24 = "k:mm\nEEEE MMMM, dd";
	private FormatChangeObserver mFormatChangeObserver;

	private Context mContext;
	private Runnable mTicker;
	private Handler mHandler;

	private float mTimeFontSize;
	private Typeface mTimeTypeface;
	private float mDateFontSize;
	private Typeface mDateTypeface;

	private Paint mDatePaint;
	private Paint mTimePaint;
	
	private String mContent;

	private boolean mTickerStopped = false;

	String mFormat;

	public DigitalClock(Context context) {
		this(context,null);
	}

	public DigitalClock(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public DigitalClock(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);        
		init(context,attrs);
	}

	private void init(Context context,AttributeSet attrs) {
		mContext = context;	
		String fontName;
		String fontsFolder = "fonts/";
		AssetManager assetManager = mContext.getAssets();

		TypedArray attrsArray = context.obtainStyledAttributes(attrs,R.styleable.DigitalClock);
		float defaultTextSize = 0.0f;
		Typeface defaultFontName = getTypeface();
		if ( attrsArray.hasValue(R.styleable.DigitalClock_dateTextSize) ) {
			mDateFontSize = attrsArray.getFloat(R.styleable.DigitalClock_dateTextSize, defaultTextSize);
		}
		if ( attrsArray.hasValue(R.styleable.DigitalClock_timeTextSize) ) {
			mTimeFontSize = attrsArray.getFloat(R.styleable.DigitalClock_timeTextSize, defaultTextSize);
		}

		if( attrsArray.hasValue(R.styleable.DigitalClock_dateTextFont) ) {
			fontName = attrsArray.getString(R.styleable.DigitalClock_dateTextFont);
			mDateTypeface = Typeface.createFromAsset(assetManager, fontsFolder + fontName);
		} else {
			mDateTypeface = defaultFontName;
		}

		if ( attrsArray.hasValue(R.styleable.DigitalClock_timeTextFont)) {
			fontName = attrsArray.getString(R.styleable.DigitalClock_timeTextFont);
			mTimeTypeface = Typeface.createFromAsset(assetManager, fontsFolder + fontName);
		} else {
			mTimeTypeface = defaultFontName;
		}

		attrsArray.recycle();

		if (mCalendar == null) {
			mCalendar = Calendar.getInstance();
		}

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		mDatePaint = new Paint();
		mDatePaint.setTypeface(mDateTypeface);
		mDatePaint.setTextSize(mDateFontSize);
		mDatePaint.setAntiAlias(true);

		mTimePaint = new Paint();
		mTimePaint.setTypeface(mTimeTypeface);
		mTimePaint.setTextSize(mTimeFontSize);
		mTimePaint.setAntiAlias(true);

		setFormat();
	}

	public void setText(String text) {
		mContent = text;
		this.invalidate();
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();

		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {
			public void run() {
				if (mTickerStopped) return;
				mCalendar.setTimeInMillis(System.currentTimeMillis());
				String dateString = mDateFormat.format(mCalendar.getTime());
				String[] dateLines = dateString.split("\n"); 
				//dateLines[0] = "<font size=\"" + mTimeFontSize + "\">" + dateLines[0] + "</font>";
				//dateLines[1] = "<font size=\"" + mDateFontSize + "\">" + dateLines[1] + "</font>";

				Log.d(TAG,"Time font size: " + mTimeFontSize);
				Log.d(TAG,"Date font size: " + mDateFontSize);
				Spannable span = new SpannableString(dateString);
				span.setSpan(new CustomTypefaceSpan("serif", mTimeTypeface), 0, dateLines[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new CustomTypefaceSpan("serif", mDateTypeface), dateLines[0].length(), dateLines[0].length() + dateLines[1].length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new AbsoluteSizeSpan((int) mTimeFontSize), 0, dateLines[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new AbsoluteSizeSpan((int)mDateFontSize),dateLines[0].length(), dateLines[0].length() + dateLines[1].length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				setText(span);
				invalidate();
				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				mHandler.postDelayed(mTicker, 1000);
			}
		};
		mTicker.run();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}

	/**
	 * Pulls 12/24 mode from system settings
	 */
	private boolean get24HourMode() {
		return android.text.format.DateFormat.is24HourFormat(getContext());
	}

	private void setFormat() {
		if (get24HourMode()) {
			mFormat = m24;
		} else {
			mFormat = m12;
		}
		mDateFormat = new SimpleDateFormat(mFormat, Locale.getDefault());
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			setFormat();
		}
	}
}