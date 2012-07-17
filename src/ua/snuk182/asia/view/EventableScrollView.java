package ua.snuk182.asia.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class EventableScrollView extends ScrollView {

	private Runnable scrollerTask;
	private int initialPosition;

	private int newCheck = 100;
	
	public interface OnScrollStoppedListener {
		void onScrollStopped(int frameTop, int frameBottom);
	}

	protected OnScrollStoppedListener onScrollStoppedListener;

	public EventableScrollView(Context context) {
		this(context, null);
	}

	public EventableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

		scrollerTask = new Runnable() {

			public void run() {
				int newPosition = getScrollY();
				if (initialPosition - newPosition == 0) {// has stopped

					if (onScrollStoppedListener != null) {
						onScrollStoppedListener.onScrollStopped(newPosition, newPosition + (getBottom()-getTop()));
					}
				} else {
					initialPosition = getScrollY();
					EventableScrollView.this.postDelayed(scrollerTask, newCheck);
				}
			}
		};
		
		setOnTouchListener(new OnTouchListener() {

	        public boolean onTouch(View v, MotionEvent event) {

	            if (event.getAction() == MotionEvent.ACTION_UP) {
	                startScrollerTask();
	            }

	            return false;
	        }
	});

	}

	public void setOnScrollStoppedListener(EventableScrollView.OnScrollStoppedListener listener) {
		onScrollStoppedListener = listener;
	}

	public void startScrollerTask() {
		initialPosition = getScrollY();
		EventableScrollView.this.postDelayed(scrollerTask, newCheck);
	}

}
