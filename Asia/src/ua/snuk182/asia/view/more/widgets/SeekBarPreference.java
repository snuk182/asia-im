package ua.snuk182.asia.view.more.widgets;

import ua.snuk182.asia.R;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix, mDefault;
	private int mMin, mMax, mValue, mCachedValue = 0;
	
	public final Runnable resizeDialogLabelRunnable = new Runnable() {
		
		@Override
		public void run() {
			mValueText.setTextSize(mValue * density);
		}
	};

	public Runnable onProgressChangedRunnable = null;

	private static float density = 0f;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (density == 0) {
			DisplayMetrics metrics = new DisplayMetrics();
			final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			display.getMetrics(metrics);
			density = metrics.density;
		}

		mContext = context;

		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mMin = attrs.getAttributeIntValue("http://ua.snuk182.asia/res", "min", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100) - mMin;
		mDefault = attrs.getAttributeValue(androidns, "defaultValue");
		if (mDefault == null) {
			mDefault = String.valueOf(mMin);
		}
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		/*
		 * if (shouldPersist()) mValue =
		 * parseValue(getPersistedString(mDefault)); else mValue =
		 * Integer.parseInt(mDefault);
		 */
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue - mMin);
		return layout;
	}

	// old-style compatibility
	private int parseValue(String value) {
		try {
			return Integer.parseInt(value);
		} catch (Exception ex) {
			if (value == null || value.equals(getContext().getResources().getString(R.string.value_size_medium))) {
				return 16;
			} else if (value.equals(getContext().getResources().getString(R.string.value_size_big))) {
				return 20;
			} else if (value.equals(getContext().getResources().getString(R.string.value_size_small))) {
				return 12;
			} else {
				return 8;
			}
		}
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mCachedValue = mValue;
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue - mMin);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (!positiveResult){
			mValue = mCachedValue;
		} else {
			if (shouldPersist())
				persistInt(mValue);			
		}
		callChangeListener(new Integer(mValue));
    }

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? parseValue(getPersistedString(mDefault)) : mMin;
		else
			mValue = (Integer) defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		if (fromTouch){
			mValue = value + mMin;			
		}
		
		String t = String.valueOf(mValue);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));

		if (onProgressChangedRunnable != null) {
			onProgressChangedRunnable.run();
		}		
	}

	public void onStartTrackingTouch(SeekBar seek) {
	}

	public void onStopTrackingTouch(SeekBar seek) {
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setMin(int min) {
		mMin = min;
	}

	public int getMin() {
		return mMin;
	}

	public void setProgress(int progress) {
		mValue = progress + mMin;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}

	public void setValue(String value) {
		setProgress(parseValue(value) - mMin);
	}
}
