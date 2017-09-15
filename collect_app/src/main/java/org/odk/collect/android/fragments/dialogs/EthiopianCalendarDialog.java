package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GregorianChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class EthiopianCalendarDialog extends DialogFragment {
    public static final String ETHIOPIAN_DIALOG = "ethiopianDialog";

    private static final String WIDGET_ID = "widgetId";
    private static final String IS_VALUE_SELECTED = "isValueSelected";
    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";

    private static final int MSG_INC_DAY = 0;
    private static final int MSG_INC_MONTH = 1;
    private static final int MSG_INC_YEAR = 2;
    private static final int MSG_DEC_DAY = 3;
    private static final int MSG_DEC_MONTH = 4;
    private static final int MSG_DEC_YEAR = 5;

    // Alter this to make the button more/less sensitive to an initial long press
    private static final int INITIAL_DELAY = 500;
    // Alter this to vary how rapidly the date increases/decreases on long press
    private static final int PERIOD = 200;

    private TextView txtMonth;
    private TextView txtDay;
    private TextView txtYear;
    private TextView txtGregorian;

    private int widgetId;
    private int ethiopianMonthArrayPointer;
    private int day;
    private int month;
    private int year;

    private boolean isValueSelected;

    private String[] monthsArray;
    private ScheduledExecutorService updater;

    public interface EthiopianCalendarDialogListener {
        void onDateChanged(int widgetId, int day, int month, int year);
    }

    private EthiopianCalendarDialogListener listener;

    public static EthiopianCalendarDialog newInstance(int widgetId, boolean isValueSelected, int day, int month, int year) {
        Bundle args = new Bundle();
        args.putInt(WIDGET_ID, widgetId);
        args.putBoolean(IS_VALUE_SELECTED, isValueSelected);
        args.putInt(DAY, day);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);

        EthiopianCalendarDialog dialog = new EthiopianCalendarDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EthiopianCalendarDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedInstanceStateToRead = savedInstanceState;
        if (savedInstanceStateToRead == null) {
            savedInstanceStateToRead = getArguments();
        }

        isValueSelected = savedInstanceStateToRead.getBoolean(IS_VALUE_SELECTED);
        widgetId = savedInstanceStateToRead.getInt(WIDGET_ID);
        day = savedInstanceStateToRead.getInt(DAY);
        month = savedInstanceStateToRead.getInt(MONTH);
        year = savedInstanceStateToRead.getInt(YEAR);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_date))
                .setView(R.layout.ethiopian_calendar_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DateTime dateTime = getDateAsGregorian();
                        listener.onDateChanged(widgetId, dateTime.getDayOfMonth(), dateTime.getMonthOfYear(), dateTime.getYear());
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.ethiopian_months);

        setUpDateTextViews();
        setUpButtons();
        setUpValues();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DateTime dateTime = getDateAsGregorian();
        outState.putInt(WIDGET_ID, widgetId);
        outState.putBoolean(IS_VALUE_SELECTED, true);
        outState.putInt(DAY, dateTime.getDayOfMonth());
        outState.putInt(MONTH, dateTime.getMonthOfYear());
        outState.putInt(YEAR, dateTime.getYear());

        super.onSaveInstanceState(outState);
    }

    private void setUpValues() {
        if (isValueSelected) {
            Date date = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear(month)
                    .withDayOfMonth(day)
                    .withHourOfDay(0)
                    .withMinuteOfHour(0)
                    .toDate();

            DateTime dtISO = new DateTime(date.getTime());

            DateTime dtEthiopian = dtISO.withChronology(EthiopicChronology.getInstance());

            txtDay.setText(String.valueOf(dtEthiopian.getDayOfMonth()));
            txtMonth.setText(monthsArray[dtEthiopian.getMonthOfYear() - 1]);
            ethiopianMonthArrayPointer = dtEthiopian.getMonthOfYear() - 1;
            txtYear.setText(String.valueOf(dtEthiopian.getYear()));
            updateGregorianDateHelperDisplay();
        } else {
            DateTime dt = new DateTime();
            updateEthiopianDateDisplay(dt);
            updateGregorianDateHelperDisplay();
        }
    }

    private void setUpDateTextViews() {
        txtDay = (TextView) getDialog().findViewById(R.id.day_txt);
        txtMonth = (TextView) getDialog().findViewById(R.id.month_txt);
        txtYear = (TextView) getDialog().findViewById(R.id.year_txt);
        txtGregorian = (TextView) getDialog().findViewById(R.id.date_gregorian);
    }

    private void setUpButtons() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                performClick(msg.what);
                super.handleMessage(msg);
            }
        };

        Button btnDayUp = (Button) getDialog().findViewById(R.id.day_up_button);
        btnDayUp.setOnClickListener(new ClickListener(MSG_INC_DAY));
        Button btnDayDown = (Button) getDialog().findViewById(R.id.day_down_button);
        btnDayDown.setOnClickListener(new ClickListener(MSG_DEC_DAY));
        Button btnMonthUp = (Button) getDialog().findViewById(R.id.month_up_button);
        btnMonthUp.setOnClickListener(new ClickListener(MSG_INC_MONTH));
        Button btnMonthDown = (Button) getDialog().findViewById(R.id.month_down_button);
        btnMonthDown.setOnClickListener(new ClickListener(MSG_DEC_MONTH));
        Button btnYearUp = (Button) getDialog().findViewById(R.id.year_up_button);
        btnYearUp.setOnClickListener(new ClickListener(MSG_INC_YEAR));
        Button btnYearDown = (Button) getDialog().findViewById(R.id.year_down_button);
        btnYearDown.setOnClickListener(new ClickListener(MSG_DEC_YEAR));

        btnDayUp.setOnTouchListener(new TouchListener(MSG_INC_DAY, handler));
        btnDayDown.setOnTouchListener(new TouchListener(MSG_DEC_DAY, handler));
        btnMonthUp.setOnTouchListener(new TouchListener(MSG_INC_MONTH, handler));
        btnMonthDown.setOnTouchListener(new TouchListener(MSG_DEC_MONTH, handler));
        btnYearUp.setOnTouchListener(new TouchListener(MSG_INC_YEAR, handler));
        btnYearDown.setOnTouchListener(new TouchListener(MSG_DEC_YEAR, handler));
    }

    private void startUpdating(int msg, Handler handler) {
        if (updater != null) {
            Timber.e("Another executor is still active");
            return;
        }
        updater = Executors.newSingleThreadScheduledExecutor();
        updater.scheduleAtFixedRate(new UpdateTask(msg, handler), INITIAL_DELAY, PERIOD, TimeUnit.MILLISECONDS);
    }

    private void stopUpdating() {
        if (updater != null) {
            updater.shutdownNow();
            updater = null;
        }
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

    private DateTime getDateAsGregorian() {
        return getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
    }

    private DateTime getCurrentEthiopianDateDisplay() {
        int ethiopianDay = Integer.parseInt(txtDay.getText().toString());
        int ethiopianMonth = ethiopianMonthArrayPointer + 1;
        int ethiopianYear = Integer.parseInt(txtYear.getText().toString());
        return new DateTime(ethiopianYear, ethiopianMonth, ethiopianDay, 0, 0, 0, 0, EthiopicChronology.getInstance());
    }

    private void updateEthiopianDateDisplay(DateTime dtGreg) {
        DateTime dtEthiopian = dtGreg.withChronology(EthiopicChronology.getInstance());
        txtDay.setText(String.valueOf(dtEthiopian.getDayOfMonth()));
        txtMonth.setText(monthsArray[dtEthiopian.getMonthOfYear() - 1]);
        ethiopianMonthArrayPointer = dtEthiopian.getMonthOfYear() - 1;
        txtYear.setText(String.valueOf(dtEthiopian.getYear()));
    }

    private void updateGregorianDateHelperDisplay() {
        DateTime dtLMDGreg = getCurrentEthiopianDateDisplay().withChronology(GregorianChronology.getInstance());
        txtGregorian.setText(DateTimeUtils.getDateTimeBasedOnUserLocale(dtLMDGreg.toDate(), null, false));
    }

    private class TouchListener implements View.OnTouchListener {
        private int msg;
        private Handler handler;

        TouchListener(int msg, Handler handler) {
            this.msg = msg;
            this.handler = handler;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean isReleased = event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL;
            boolean isPressed = event.getAction() == MotionEvent.ACTION_DOWN;

            if (isReleased) {
                stopUpdating();
            } else if (isPressed) {
                startUpdating(msg, handler);
            }
            return false;
        }
    }

    private class ClickListener implements View.OnClickListener {
        private int msg;

        ClickListener(int msg) {
            this.msg = msg;
        }

        @Override
        public void onClick(View v) {
            performClick(msg);
        }
    }

    private class UpdateTask implements Runnable {
        private int msg;
        private Handler handler;

        UpdateTask(int msg, Handler handler) {
            this.msg = msg;
            this.handler = handler;
        }

        public void run() {
            handler.sendEmptyMessage(msg);
        }
    }

    private void performClick(int msg) {
        switch (msg) {
            case MSG_INC_DAY:
                incrementDay();
                break;
            case MSG_INC_MONTH:
                incrementMonth();
                break;
            case MSG_INC_YEAR:
                incrementYear();
                break;
            case MSG_DEC_DAY:
                decrementDay();
                break;
            case MSG_DEC_MONTH:
                decrementMonth();
                break;
            case MSG_DEC_YEAR:
                decrementYear();
        }
    }
}
