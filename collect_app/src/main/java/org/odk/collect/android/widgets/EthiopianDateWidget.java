package org.odk.collect.android.widgets;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.odk.collect.android.R;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import timber.log.Timber;

/**
 * Ethiopian Date Widget.
 * 
 * @author Alex Little (alex@alexlittle.net)
 */
public class EthiopianDateWidget extends QuestionWidget{

	private TextView txtMonth;
	private TextView txtDay;
	private TextView txtYear;
	private TextView txtGregorian;

	private static Chronology chron_eth = EthiopicChronology.getInstance();
	private String[] monthsArray;
	private int ethiopianMonthArrayPointer;

	private ScheduledExecutorService updater;
	private static final int MSG_INC = 0;
	private static final int MSG_DEC = 1;

	// Alter this to make the button more/less sensitive to an initial long press
	private static final int INITIAL_DELAY = 500;
	// Alter this to vary how rapidly the date increases/decreases on long press
	private static final int PERIOD = 200;

	private class UpdateTask implements Runnable {
		private boolean inc;
		private Handler handler;

		UpdateTask(boolean inc, Handler h) {
			this.inc = inc;
			handler = h;
		}

		public void run() {
			if (inc) {
				handler.sendEmptyMessage(MSG_INC);
			} else {
				handler.sendEmptyMessage(MSG_DEC);
			}
		}
	}

	public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		Resources res = getResources();
		monthsArray = res.getStringArray(R.array.ethiopian_months);

		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = vi.inflate(R.layout.ethiopian_date_widget, null);
		addAnswerView(view);

		Handler mDayHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_INC:
						incrementDay();
						return;
					case MSG_DEC:
						decrementDay();
						return;
				}
				super.handleMessage(msg);
			}
		};

		Handler mMonthHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_INC:
						incrementMonth();
						return;
					case MSG_DEC:
						decrementMonth();
						return;
				}
				super.handleMessage(msg);
			}
		};

		Handler mYearHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_INC:
						incrementYear();
						return;
					case MSG_DEC:
						decrementYear();
						return;
				}
				super.handleMessage(msg);
			}
		};

		txtDay = (TextView) findViewById(R.id.day_text);
		txtMonth = (TextView) findViewById(R.id.month_txt);
		txtYear = (TextView) findViewById(R.id.year_txt);
		txtGregorian = (TextView) findViewById(R.id.date_gregorian);

		Button btnDayUp = (Button) findViewById(R.id.day_up_button);
		Button btnMonthUp = (Button) findViewById(R.id.month_up_button);
		Button btnYearUp = (Button) findViewById(R.id.year_up_button);
		Button btnDayDown = (Button) findViewById(R.id.day_down_button);
		Button btnMonthDown = (Button) findViewById(R.id.month_down_button);
		Button btnYearDown = (Button) findViewById(R.id.year_down_button);

		btnDayUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					incrementDay();
				}
			}
		});

		btnMonthUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					incrementMonth();
				}
			}
		});

		btnYearUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					incrementYear();
				}
			}
		});

		btnDayDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					decrementDay();
				}
			}
		});

		btnMonthDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					decrementMonth();
				}
			}
		});

		btnYearDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updater == null) {
					decrementYear();
				}
			}
		});

		btnDayUp.setOnTouchListener(new EDWTouchListener(btnDayUp, mDayHandler));
		btnDayDown.setOnTouchListener(new EDWTouchListener(btnDayUp, mDayHandler));
		btnMonthUp.setOnTouchListener(new EDWTouchListener(btnMonthUp, mMonthHandler));
		btnMonthDown.setOnTouchListener(new EDWTouchListener(btnMonthUp, mMonthHandler));
		btnYearUp.setOnTouchListener(new EDWTouchListener(btnYearUp, mYearHandler));
		btnYearDown.setOnTouchListener(new EDWTouchListener(btnYearUp, mYearHandler));

		btnDayUp.setOnKeyListener(new EDWKeyListener(btnDayUp, mDayHandler));
		btnDayDown.setOnKeyListener(new EDWKeyListener(btnDayUp, mDayHandler));
		btnMonthUp.setOnKeyListener(new EDWKeyListener(btnMonthUp, mMonthHandler));
		btnMonthDown.setOnKeyListener(new EDWKeyListener(btnMonthUp, mMonthHandler));
		btnYearUp.setOnKeyListener(new EDWKeyListener(btnYearUp, mYearHandler));
		btnYearDown.setOnKeyListener(new EDWKeyListener(btnYearUp, mYearHandler));

		setAnswer();
	}

	@Override
	public void clearAnswer() {
		DateTime dt = new DateTime();
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	@Override
	public IAnswerData getAnswer() {
		DateTime dt = getDateAsGregorian();
		return new DateData(dt.toDate());
	}

	@Override
	public void setFocus(Context context) {
		InputMethodManager inputManager =
			(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		//super.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
	}

	/**
	 * Start Updater, for when using long press to increment/decrement date without repeated pressing on the buttons
	 */
	private void startUpdating(boolean inc, Handler mHandler) {
		if (updater != null) {
			Timber.e("Another executor is still active");
			return;
		}
		updater = Executors.newSingleThreadScheduledExecutor();
		updater.scheduleAtFixedRate(new UpdateTask(inc,mHandler), INITIAL_DELAY, PERIOD,
				TimeUnit.MILLISECONDS);
	}

	private void stopUpdating() {
		updater.shutdownNow();
		updater = null;
	}

	private void incrementDay() {
		DateTime dt = getDateAsGregorian().plusDays(1);
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void incrementMonth() {
		DateTime dt = getCurrentEthiopianDateDisplay().plusMonths(1).withChronology(GregorianChronology.getInstance());
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void incrementYear() {
		DateTime dt = getCurrentEthiopianDateDisplay().plusYears(1).withChronology(GregorianChronology.getInstance());
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void decrementDay() {
		DateTime dt = getDateAsGregorian().minusDays(1);
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void decrementMonth() {
		DateTime dt = getCurrentEthiopianDateDisplay().minusMonths(1).withChronology(GregorianChronology.getInstance());
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void decrementYear() {
		DateTime dt = getCurrentEthiopianDateDisplay().minusYears(1).withChronology(GregorianChronology.getInstance());
		updateEthiopianDateDisplay(dt);
		updateGregorianDateHelperDisplay();
	}

	private void setAnswer() {
		if (getPrompt().getAnswerValue() != null) {
			DateTime dtISO = new DateTime(((Date) getPrompt().getAnswerValue().getValue()).getTime());

			// find out what the same instant is using the Ethiopic Chronology
			DateTime dtEthiopic = dtISO.withChronology(chron_eth);

			txtDay.setText(Integer.toString(dtEthiopic.getDayOfMonth()));
			txtMonth.setText(monthsArray[dtEthiopic.getMonthOfYear()-1]);
			ethiopianMonthArrayPointer = dtEthiopic.getMonthOfYear()-1;
			txtYear.setText(Integer.toString(dtEthiopic.getYear()));
			updateGregorianDateHelperDisplay();

		} else {
			clearAnswer();
		}
	}

	private DateTime getDateAsGregorian(){
		return getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
	}

	private DateTime getCurrentEthiopianDateDisplay(){
		int ethiopianDay = Integer.parseInt(txtDay.getText().toString());
		int ethiopianMonth = ethiopianMonthArrayPointer + 1;
		int ethiopianYear = Integer.parseInt(txtYear.getText().toString());
		return new DateTime(ethiopianYear, ethiopianMonth, ethiopianDay, 0, 0, 0, 0, chron_eth);
	}

	private void updateEthiopianDateDisplay(DateTime dtGreg){
		DateTime dtEthiopian = dtGreg.withChronology(chron_eth);
		txtDay.setText(String.format("%02d", dtEthiopian.getDayOfMonth()));
		txtMonth.setText(monthsArray[dtEthiopian.getMonthOfYear()-1]);
		ethiopianMonthArrayPointer = dtEthiopian.getMonthOfYear()-1;
		txtYear.setText(String.format("%04d", dtEthiopian.getYear()));
	}

	// Update the widget helper date text (useful for those who don't know the Ethiopian calendar)
	private void updateGregorianDateHelperDisplay(){
		DateTime dtLMDGreg = getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");
		String str = fmt.print(dtLMDGreg);
		txtGregorian.setText("("+str+")");
	}

	private class EDWTouchListener implements OnTouchListener {
		private View view;
		private Handler handler;

		EDWTouchListener(View mV, Handler mH){
			view = mV;
			handler = mH;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean isReleased = event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL;
			boolean isPressed = event.getAction() == MotionEvent.ACTION_DOWN;

			if (isReleased) {
				stopUpdating();
			} else if (isPressed) {
				startUpdating(v == view, handler);
			}
			return false;
		}
	}

	private class EDWKeyListener implements OnKeyListener {
		private View view;
		private Handler handler;

		EDWKeyListener(View mV, Handler mH) {
			view = mV;
			handler = mH;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean isKeyOfInterest = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER;
			boolean isReleased = event.getAction() == KeyEvent.ACTION_UP;
			boolean isPressed = event.getAction() == KeyEvent.ACTION_DOWN && event.getAction() != KeyEvent.ACTION_MULTIPLE;

			if (isKeyOfInterest && isReleased) {
				stopUpdating();
			} else if (isKeyOfInterest && isPressed) {
				startUpdating(v == view, handler);
			}
			return false;
		}
	}
}
