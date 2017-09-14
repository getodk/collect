package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
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

    private static final int MSG_INC = 0;
    private static final int MSG_DEC = 1;
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

    private String[] monthsArray;
    private ScheduledExecutorService updater;

    public interface EthiopianCalendarDialogListener {
        void onDateChanged(int widgetId, IAnswerData data);
    }

    private EthiopianCalendarDialogListener listener;

    public static EthiopianCalendarDialog newInstance(int widgetId, boolean isValueSelected, int day, int month, int year) {
        EthiopianCalendarDialog dialog = new EthiopianCalendarDialog();

        Bundle args = new Bundle();
        args.putInt(WIDGET_ID, widgetId);
        args.putBoolean(IS_VALUE_SELECTED, isValueSelected);
        args.putInt(DAY, day);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_date))
                .setView(R.layout.ethiopian_calendar_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDateChanged(widgetId, new DateData(getDateAsGregorian().toDate()));
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

    private void setUpValues() {
        if (getArguments() != null) {
            widgetId = getArguments().getInt(WIDGET_ID);
            if (getArguments().getBoolean(IS_VALUE_SELECTED, false)) {
                Date date = new LocalDateTime()
                        .withYear(getArguments().getInt(YEAR))
                        .withMonthOfYear(getArguments().getInt(MONTH))
                        .withDayOfMonth(getArguments().getInt(DAY))
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
    }

    private void setUpDateTextViews() {
        txtDay = (TextView) getDialog().findViewById(R.id.day_txt);
        txtMonth = (TextView) getDialog().findViewById(R.id.month_txt);
        txtYear = (TextView) getDialog().findViewById(R.id.year_txt);
        txtGregorian = (TextView) getDialog().findViewById(R.id.date_gregorian);
    }

    private void setUpButtons() {
        Handler dayHandler = new Handler() {
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

        Handler monthHandler = new Handler() {
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

        Handler yearHandler = new Handler() {
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

        Button btnDayUp = (Button) getDialog().findViewById(R.id.day_up_button);
        Button btnMonthUp = (Button) getDialog().findViewById(R.id.month_up_button);
        Button btnYearUp = (Button) getDialog().findViewById(R.id.year_up_button);
        Button btnDayDown = (Button) getDialog().findViewById(R.id.day_down_button);
        Button btnMonthDown = (Button) getDialog().findViewById(R.id.month_down_button);
        Button btnYearDown = (Button) getDialog().findViewById(R.id.year_down_button);

        btnDayUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    incrementDay();
                }
            }
        });

        btnMonthUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    incrementMonth();
                }
            }
        });

        btnYearUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    incrementYear();
                }
            }
        });

        btnDayDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    decrementDay();
                }
            }
        });

        btnMonthDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    decrementMonth();
                }
            }
        });

        btnYearDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updater == null) {
                    decrementYear();
                }
            }
        });

        btnDayUp.setOnTouchListener(new EDWTouchListener(btnDayUp, dayHandler));
        btnDayDown.setOnTouchListener(new EDWTouchListener(btnDayUp, dayHandler));
        btnMonthUp.setOnTouchListener(new EDWTouchListener(btnMonthUp, monthHandler));
        btnMonthDown.setOnTouchListener(new EDWTouchListener(btnMonthUp, monthHandler));
        btnYearUp.setOnTouchListener(new EDWTouchListener(btnYearUp, yearHandler));
        btnYearDown.setOnTouchListener(new EDWTouchListener(btnYearUp, yearHandler));

        btnDayUp.setOnKeyListener(new EDWKeyListener(btnDayUp, dayHandler));
        btnDayDown.setOnKeyListener(new EDWKeyListener(btnDayUp, dayHandler));
        btnMonthUp.setOnKeyListener(new EDWKeyListener(btnMonthUp, monthHandler));
        btnMonthDown.setOnKeyListener(new EDWKeyListener(btnMonthUp, monthHandler));
        btnYearUp.setOnKeyListener(new EDWKeyListener(btnYearUp, yearHandler));
        btnYearDown.setOnKeyListener(new EDWKeyListener(btnYearUp, yearHandler));
    }

    private void startUpdating(boolean inc, Handler handler) {
        if (updater != null) {
            Timber.e("Another executor is still active");
            return;
        }
        updater = Executors.newSingleThreadScheduledExecutor();
        updater.scheduleAtFixedRate(new UpdateTask(inc,handler), INITIAL_DELAY, PERIOD,
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

    private class EDWTouchListener implements View.OnTouchListener {
        private View view;
        private Handler handler;

        EDWTouchListener(View view, Handler handler) {
            this.view = view;
            this.handler = handler;
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

    private class EDWKeyListener implements View.OnKeyListener {
        private View view;
        private Handler handler;

        EDWKeyListener(View view, Handler handler) {
            this.view = view;
            this.handler = handler;
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

    private class UpdateTask implements Runnable {
        private boolean inc;
        private Handler handler;

        UpdateTask(boolean inc, Handler handler) {
            this.inc = inc;
            this.handler = handler;
        }

        public void run() {
            if (inc) {
                handler.sendEmptyMessage(MSG_INC);
            } else {
                handler.sendEmptyMessage(MSG_DEC);
            }
        }
    }
}
