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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
	    
	    private Button btnDayUp;
	    private Button btnMonthUp;
	    private Button btnYearUp;
	    private Button btnDayDown;
	    private Button btnMonthDown;
	    private Button btnYearDown;
	    
	    private ScheduledExecutorService mUpdater;
	    private Handler mDayHandler;
	    private Handler mMonthHandler;
	    private Handler mYearHandler;
	    private static final int MSG_INC = 0;
	    private static final int MSG_DEC = 1;
	    
	    // Alter this to make the button more/less sensitive to an initial long press 
	    private static final int INITIAL_DELAY = 500;
	    // Alter this to vary how rapidly the date increases/decreases on long press 
	    private static final int PERIOD = 200;
	    
	    private class UpdateTask implements Runnable {
	        private boolean mInc;
	        private Handler mHandler;
	        
	        public UpdateTask(boolean inc, Handler h) {
	            mInc = inc;
	            mHandler = h;
	        }

	        public void run() {
	            if (mInc) {
	            	mHandler.sendEmptyMessage(MSG_INC);
	            } else {
	            	mHandler.sendEmptyMessage(MSG_DEC);
	            }
	        }
	    }
	    
	    /**
	     * Constructor method
	     * @param context
	     * @param prompt
	     */
	    public EthiopianDateWidget(Context context, FormEntryPrompt prompt) {
	        super(context, prompt);
     
	        Resources res = getResources();
	        // load the months - will automatically get correct strings for current phone locale
	        monthsArray = res.getStringArray(R.array.ethiopian_months);
	        
	        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View vv = vi.inflate(R.layout.ethiopian_date_widget, null);
	        addView(vv);
	        
	        /*
	         * Initialise handlers for incrementing/decrementing dates
	         */
	        mDayHandler = new Handler() {
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
	        
	        mMonthHandler = new Handler() {
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
	        
	        mYearHandler = new Handler() {
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

	        // Date fields
	        txtDay = (TextView) findViewById(R.id.daytxt);
            txtMonth = (TextView) findViewById(R.id.monthtxt);
            txtYear = (TextView) findViewById(R.id.yeartxt);
            txtGregorian = (TextView) findViewById(R.id.dateGregorian);
            
            // action buttons
	        btnDayUp = (Button) findViewById(R.id.dayupbtn);
	        btnMonthUp = (Button) findViewById(R.id.monthupbtn);
	        btnYearUp = (Button) findViewById(R.id.yearupbtn);
	        btnDayDown = (Button) findViewById(R.id.daydownbtn);
	        btnMonthDown = (Button) findViewById(R.id.monthdownbtn);
	        btnYearDown = (Button) findViewById(R.id.yeardownbtn);
	        
	        // button click listeners
            btnDayUp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
			            incrementDay();
			        }
				}
			});
            
            btnMonthUp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
						incrementMonth();
					}
				}
			});
           
            btnYearUp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
						incrementYear();
					}
				}
			});

            btnDayDown.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
			            decrementDay();
			        }
				}
			});

            btnMonthDown.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
						decrementMonth();
					}
				}
			});

            btnYearDown.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mUpdater == null) {
						decrementYear();
					}
				}
			});

            // button touch listeners
            btnDayUp.setOnTouchListener(new EDWTouchListener(btnDayUp,mDayHandler));
            btnDayDown.setOnTouchListener(new EDWTouchListener(btnDayUp,mDayHandler));
            btnMonthUp.setOnTouchListener(new EDWTouchListener(btnMonthUp,mMonthHandler));
            btnMonthDown.setOnTouchListener(new EDWTouchListener(btnMonthUp,mMonthHandler));
            btnYearUp.setOnTouchListener(new EDWTouchListener(btnYearUp,mYearHandler));
            btnYearDown.setOnTouchListener(new EDWTouchListener(btnYearUp,mYearHandler));
            
            // button key listeners
            btnDayUp.setOnKeyListener(new EDWKeyListener(btnDayUp,mDayHandler));
            btnDayDown.setOnKeyListener(new EDWKeyListener(btnDayUp,mDayHandler));
            btnMonthUp.setOnKeyListener(new EDWKeyListener(btnMonthUp,mMonthHandler));
            btnMonthDown.setOnKeyListener(new EDWKeyListener(btnMonthUp,mMonthHandler));
            btnYearUp.setOnKeyListener(new EDWKeyListener(btnYearUp,mYearHandler));
            btnYearDown.setOnKeyListener(new EDWKeyListener(btnYearUp,mYearHandler));
            
	    	// If there's an answer, use it.
	        setAnswer();
	    }

	    /**
	     * Resets date to today
	     */
	    @Override
	    public void clearAnswer() {
	    	DateTime dt = new DateTime();
	    	updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }

	    /**
	     * Return the date for storing in ODK 
	     */
	    @Override
	    public IAnswerData getAnswer() {
	    	DateTime dt = getDateAsGregorian();
	    	return new DateData(dt.toDate());
	    }

	    @Override
	    public void setFocus(Context context) {
	        // Hide the soft keyboard if it's showing.
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
	     * @param inc
	     * @param mHandler
	     */
	    private void startUpdating(boolean inc, Handler mHandler) {
	        if (mUpdater != null) {
	            Log.e(getClass().getSimpleName(), "Another executor is still active");
	            return;
	        }
	        mUpdater = Executors.newSingleThreadScheduledExecutor();
	        mUpdater.scheduleAtFixedRate(new UpdateTask(inc,mHandler), INITIAL_DELAY, PERIOD,
	                TimeUnit.MILLISECONDS);
	    }

	    /**
	     * Stop incrementing/decrementing
	     */
	    private void stopUpdating() {
	        mUpdater.shutdownNow();
	        mUpdater = null;
	    }
	    
	    /**
	     * Increase by 1 day
	     */
	    private void incrementDay(){
	    	// get the current date into gregorian, add one and redisplay
			DateTime dt = getDateAsGregorian().plusDays(1);
			updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Increase by 1 month
	     */
	    private void incrementMonth(){
	    	DateTime dt = getCurrentEthiopianDateDisplay().plusMonths(1).withChronology(GregorianChronology.getInstance());
	    	updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Increase by 1 year
	     */
	    private void incrementYear(){
	    	DateTime dt = getCurrentEthiopianDateDisplay().plusYears(1).withChronology(GregorianChronology.getInstance());
	    	updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Decrease by 1 day
	     */
	    private void decrementDay(){
			DateTime dt = getDateAsGregorian().minusDays(1);
			updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Decrease by 1 month
	     */
	    private void decrementMonth(){
	    	DateTime dt = getCurrentEthiopianDateDisplay().minusMonths(1).withChronology(GregorianChronology.getInstance());
	    	updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Decrease by 1 year
	     */
	    private void decrementYear(){
	    	DateTime dt = getCurrentEthiopianDateDisplay().minusYears(1).withChronology(GregorianChronology.getInstance());
	    	updateEthiopianDateDisplay(dt);
			updateGregorianDateHelperDisplay();
	    }
	    
	    /**
	     * Initial date display
	     */
	    private void setAnswer() {

	        if (mPrompt.getAnswerValue() != null) {
	            // setup date object
	            DateTime dtISO = new DateTime(((Date) ((DateData) mPrompt.getAnswerValue()).getValue()).getTime());

	            // find out what the same instant is using the Ethiopic Chronology
	            DateTime dtEthiopic = dtISO.withChronology(chron_eth);
	           
	            txtDay.setText(Integer.toString(dtEthiopic.getDayOfMonth()));
	            txtMonth.setText(monthsArray[dtEthiopic.getMonthOfYear()-1]);
	            ethiopianMonthArrayPointer = dtEthiopic.getMonthOfYear()-1;
	            txtYear.setText(Integer.toString(dtEthiopic.getYear()));
	            updateGregorianDateHelperDisplay();
	            
	        } else {
	            // create date widget with current date
	            clearAnswer();
	        }
	    }
	    
	    /**
	     * Get the current widget date in Gregorian chronology
	     * @return
	     */
	    private DateTime getDateAsGregorian(){
	    	DateTime dtGregorian = getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
	    	return dtGregorian;
	    }
	    
	    /**
	     * Get the current widget date in Ethiopian chronology
	     * @return
	     */
	    private DateTime getCurrentEthiopianDateDisplay(){
	    	int ethioDay = Integer.parseInt(txtDay.getText().toString());
			int ethioMonth = ethiopianMonthArrayPointer + 1;
			int ethioYear = Integer.parseInt(txtYear.getText().toString());
	    	return new DateTime(ethioYear, ethioMonth, ethioDay, 0, 0, 0, 0, chron_eth);
	    }
	    
	    /**
	     * Update the widget date to display the amended date
	     * @param dtGreg
	     */
	    private void updateEthiopianDateDisplay(DateTime dtGreg){
	    	DateTime dtEthio = dtGreg.withChronology(chron_eth);
			txtDay.setText(String.format("%02d",dtEthio.getDayOfMonth()));
			txtMonth.setText(monthsArray[dtEthio.getMonthOfYear()-1]);
			ethiopianMonthArrayPointer = dtEthio.getMonthOfYear()-1;
			txtYear.setText(String.format("%04d",dtEthio.getYear()));
	    }
	    
	    /**
	     * Update the widget helper date text (useful for those who don't know the Ethiopian calendar)
	     * @param dtGreg
	     */
	    private void updateGregorianDateHelperDisplay(){
	    	DateTime dtLMDGreg = getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
	    	DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");
	    	String str = fmt.print(dtLMDGreg);
	    	txtGregorian.setText("("+str+")");
	    }
	   
	    /**
	     * Listens for button being pressed by touchscreen
	     * @author alex
	     */
	    private class EDWTouchListener implements OnTouchListener{
	    	private View mView;
	    	private Handler mHandler;
	    	public EDWTouchListener(View mV, Handler mH){
	    		mView = mV;
	    		mHandler = mH;
	    	}
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean isReleased = event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL;
		        boolean isPressed = event.getAction() == MotionEvent.ACTION_DOWN;

		        if (isReleased) {
		            stopUpdating();
		        } else if (isPressed) {
		        	startUpdating(v == mView,mHandler);
		        }
		        return false;
			}
	    }
	    
	    /**
	     * Listens for button being pressed by keypad/trackball
	     * @author alex
	     */
	    private class EDWKeyListener implements OnKeyListener{
	    	private View mView;
	    	private Handler mHandler;
	    	public EDWKeyListener(View mV, Handler mH){
	    		mView = mV;
	    		mHandler = mH;
	    	}
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				boolean isKeyOfInterest = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER;
		        boolean isReleased = event.getAction() == KeyEvent.ACTION_UP;
		        boolean isPressed = event.getAction() == KeyEvent.ACTION_DOWN
		                && event.getAction() != KeyEvent.ACTION_MULTIPLE;

		        if (isKeyOfInterest && isReleased) {
		            stopUpdating();
		        } else if (isKeyOfInterest && isPressed) {
		            startUpdating(v == mView,mHandler);
		        }
		        return false;
			}
	    }
}
