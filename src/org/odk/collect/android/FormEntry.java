/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.regex.Pattern;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.IService;
import org.javarosa.model.xform.XFormsModule;


/**
 * FormEntry is responsible for displaying questions, animating transitions
 * between questions, and allowing the user to enter data.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormEntry extends Activity implements AnimationListener, FormLoaderListener {
    private final String t = "FormEntry";
    private final String FORMPATH = "formpath";

    private static final int MENU_CLEAR = Menu.FIRST;
    private static final int MENU_DELETE_REPEAT = Menu.FIRST + 1;
    private static final int MENU_QUIT = Menu.FIRST + 2;
    private static final int MENU_LANGUAGES = Menu.FIRST + 3;

    private static final int PROGRESS_DIALOG = 1;

    private ProgressBar mProgressBar;
    private String mFormPath;
    private String mAnswersPath;
    private String mInstancePath;

    private GestureDetector mGestureDetector;
    private FormHandler mFormHandler;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private RelativeLayout mRelativeLayout;
    private View mCurrentView;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    private boolean mBeenSwiped;

    private FormLoaderTask mFormLoaderTask;

    enum AnimationType {
        LEFT, RIGHT, FADE
    }

    enum FormEntryViews {
        START_SCREEN, QUESTION_VIEW, END_SCREEN
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");
        setTheme(SharedConstants.APPLICATION_THEME);
        setContentView(R.layout.formentry);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        initializeVariables();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FORMPATH)) {
                mFormPath = savedInstanceState.getString(FORMPATH);
            }
            if (savedInstanceState.containsKey("answerpath")) {
                mAnswersPath = savedInstanceState.getString("answerpath");
            }
        }

        Object data = getLastNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            mFormLoaderTask = (FormLoaderTask) data;
        } else if (data instanceof FormHandler) {
            mFormHandler = (FormHandler) data;
            refreshCurrentView();
        } else if (data == null) {
            // starting for the first time
            Intent intent = getIntent();
            if (intent != null) {
                // restoring from saved form
                if (intent.getBooleanExtra(("instance"), false)) {
                    mInstancePath = intent.getStringExtra(SharedConstants.FILEPATH_KEY);
                    mFormPath = getFormPath(mInstancePath);
                } else {
                    mFormPath = intent.getStringExtra(SharedConstants.FILEPATH_KEY);
                }
                mFormLoaderTask = new FormLoaderTask();
                mFormLoaderTask.execute(mFormPath, mInstancePath);
                showDialog(PROGRESS_DIALOG);
            }
        }
    }


    private String getFormPath(String path) {

        // trim the date stamp off
        String regex = "\\_[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\_[0-9]{2}\\-[0-9]{2}\\-[0-9]{2}\\.xml$";
        Pattern pattern = Pattern.compile(regex);
        String formname = pattern.split(path)[0];
        formname = formname.substring(formname.lastIndexOf("/") + 1);

        File xmlfile = new File(SharedConstants.FORMS_PATH + "/" + formname + ".xml");
        File xhtmlfile = new File(SharedConstants.FORMS_PATH + "/" + formname + ".xhtml");

        if (xmlfile.exists()) {
            return xmlfile.getAbsolutePath();
        } else if (xhtmlfile.exists()) {
            return xhtmlfile.getAbsolutePath();
        } else {
            return null;
        }
    }


    /** Initializes all member variables */
    public void initializeVariables() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);

        mAlertDialog = null;
        mFormHandler = null;
        mCurrentView = null;
        mInAnimation = null;
        mOutAnimation = null;
        mBeenSwiped = false;
        mInstancePath = null;

        mGestureDetector = new GestureDetector();

        // load modules
        new XFormsModule().registerModule(null);

        // load services
        Vector<IService> v = new Vector<IService>();
        v.add(new PropertyManager(getApplicationContext()));
        JavaRosaServiceProvider.instance().initialize(v);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FORMPATH, mFormPath);
        outState.putString("answerpath", mAnswersPath);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case (SharedConstants.IMAGE_CAPTURE):
                PromptElement pi = ((QuestionView) mCurrentView).getPrompt();
                if (!pi.isReadonly()) {
                    File fi = new File(SharedConstants.TMPFILE_PATH);
                    try {
                        Uri ui =
                                Uri.parse(android.provider.MediaStore.Images.Media.insertImage(
                                        getContentResolver(), fi.getAbsolutePath(), null, null));
                        fi.delete();
                        ((QuestionView) mCurrentView).setBinaryData(ui);
                        mFormHandler.saveAnswer(pi, ((QuestionView) mCurrentView).getAnswer(),
                                false);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    refreshCurrentView();
                }
                break;
            case (SharedConstants.BARCODE_CAPTURE):
                PromptElement pb = ((QuestionView) mCurrentView).getPrompt();
                if (!pb.isReadonly()) {
                    String s = intent.getStringExtra("SCAN_RESULT");
                    ((QuestionView) mCurrentView).setBinaryData(s);
                    mFormHandler.saveAnswer(pb, ((QuestionView) mCurrentView).getAnswer(), false);
                }
                break;
            case SharedConstants.AUDIO_CAPTURE:
                PromptElement pa = ((QuestionView) mCurrentView).getPrompt();
                if (!pa.isReadonly()) {
                    Uri ua = intent.getData();
                    // save answer in data model
                    ((QuestionView) mCurrentView).setBinaryData(ua);
                    mFormHandler.saveAnswer(pa, ((QuestionView) mCurrentView).getAnswer(), false);
                    refreshCurrentView();
                }
                break;
            case SharedConstants.VIDEO_CAPTURE:
                PromptElement pv = ((QuestionView) mCurrentView).getPrompt();
                if (!pv.isReadonly()) {
                    Uri uv = intent.getData();
                    // save answer in data model
                    ((QuestionView) mCurrentView).setBinaryData(uv);
                    mFormHandler.saveAnswer(pv, ((QuestionView) mCurrentView).getAnswer(), false);
                    refreshCurrentView();
                }
                break;
        }
    }


    /**
     * Refreshes the current view. mFormHandler and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     */
    public void refreshCurrentView() {
        View current;
        if (mFormHandler.isBeginning()) {
            current = createView(FormEntryViews.START_SCREEN, null);
        } else if (mFormHandler.isEnd()) {
            current = createView(FormEntryViews.END_SCREEN, null);
        } else {
            PromptElement p = mFormHandler.currentPrompt();

            if (!p.isRepeat()) {
                current = createView(FormEntryViews.QUESTION_VIEW, p);
            } else {
                // repeat prompt, so go back to the previous question.
                PromptElement pr = mFormHandler.prevPrompt();
                if (pr != null) {
                    current = createView(FormEntryViews.QUESTION_VIEW, pr);
                } else {
                    current = createView(FormEntryViews.START_SCREEN, null);
                }
            }
        }
        showView(current, AnimationType.FADE);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_CLEAR, 0, getString(R.string.clear_answer)).setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        menu.add(0, MENU_DELETE_REPEAT, 0, getString(R.string.delete_repeat)).setIcon(
                R.drawable.ic_menu_clear_playlist);
        menu.add(0, MENU_LANGUAGES, 0, getString(R.string.change_language)).setIcon(
                android.R.drawable.ic_menu_more);
        menu.add(0, MENU_QUIT, 0, getString(R.string.quit_entry)).setIcon(
                android.R.drawable.ic_menu_save);
        return true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*
         * Menu options are added only for views where they're appropriate, and
         * removed for those that they're not.
         */
        if (isQuestionView()) {
            if (!((QuestionView) mCurrentView).getPrompt().isReadonly()) {
                if (menu.findItem(MENU_CLEAR) == null) {
                    menu.add(0, MENU_CLEAR, 0, getString(R.string.clear_answer)).setIcon(
                            android.R.drawable.ic_menu_close_clear_cancel);
                }
            } else {
                menu.removeItem(MENU_CLEAR);
            }
            if (((QuestionView) mCurrentView).getPrompt().isInRepeatableGroup()) {
                if (menu.findItem(MENU_DELETE_REPEAT) == null) {
                    menu.add(0, MENU_DELETE_REPEAT, 0, getString(R.string.delete_repeat)).setIcon(
                            R.drawable.ic_menu_clear_playlist);
                }
            } else {
                menu.removeItem(MENU_DELETE_REPEAT);
            }
            if (menu.findItem(MENU_QUIT) == null) {
                menu.add(0, MENU_QUIT, 0, getString(R.string.quit_entry)).setIcon(
                        android.R.drawable.ic_menu_save);
            }

        } else {
            menu.removeItem(MENU_CLEAR);
            menu.removeItem(MENU_DELETE_REPEAT);
            menu.removeItem(MENU_QUIT);
        }

        return true;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LANGUAGES:
                createLanguageDialog();
                return true;
            case MENU_CLEAR:
                createClearDialog();
                return true;
            case MENU_DELETE_REPEAT:
                createDeleteRepeatConfirmDialog();
                return true;
            case MENU_QUIT:
                createSaveQuitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 
     * @return true if the current View is a QuestionView.
     */
    private boolean isQuestionView() {
        if (mCurrentView instanceof QuestionView) {
            return true;
        }
        return false;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance() If we're
     * loading, then we pass the loading thread to our next instance. If we've
     * finished loading, we pass the formhandler.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        synchronized (this) {
            if (mFormLoaderTask != null && mFormLoaderTask.getStatus() != AsyncTask.Status.FINISHED)
                return mFormLoaderTask;
        }
        if (mFormHandler != null && isQuestionView()) {
            PromptElement p = ((QuestionView) mCurrentView).getPrompt();
            if (!p.isReadonly()) {
                mFormHandler.saveAnswer(p, ((QuestionView) mCurrentView).getAnswer(), true);
            }
        }

        return mFormHandler;
    }


    /**
     * Creates a view given the View type and a prompt
     * 
     * @param viewType
     * @param prompt
     * @return newly created View
     */
    private View createView(FormEntryViews viewType, PromptElement prompt) {
        setTitle(getString(R.string.app_name) + " > " + mFormHandler.getFormTitle());

        View nextView = null;
        switch (viewType) {
            case START_SCREEN:
                nextView = View.inflate(this, R.layout.formentry_start, null);

                // set window title using form name
                setTitle(getString(R.string.app_name) + " > " + mFormHandler.getFormTitle());

                // set description using form title
                ((TextView) nextView.findViewById(R.id.description)).setText(getString(
                        R.string.enter_data_description, mFormHandler.getFormTitle()));

                break;
            case END_SCREEN:
                nextView = View.inflate(this, R.layout.formentry_end, null);

                // set description using form title
                ((TextView) nextView.findViewById(R.id.description)).setText(getString(
                        R.string.save_data_description, mFormHandler.getFormTitle()));

                // create save data dialog box
                ((Button) nextView.findViewById(R.id.submit))
                        .setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                // Form is markd as 'done' here.
                                mFormHandler.finalizeDataModel();
                                if (mFormHandler.exportData(mAnswersPath, getApplicationContext(),
                                        true)) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.data_saved_ok), Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.data_saved_error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case QUESTION_VIEW:
                nextView = new QuestionView(this, prompt, mAnswersPath);
                ((QuestionView) nextView).buildView();
                break;
        }
        return nextView;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent mv) {
        boolean handled = onTouchEvent(mv);
        if (!handled) {
            return super.dispatchTouchEvent(mv);
        }
        return handled; // this is always true
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // constrain the user to only be able to swipe (that causes an action)
        // once per screen with the mBeenSwiped variable.
        boolean handled = false;
        if (!mBeenSwiped) {
            switch (mGestureDetector.getGesture(motionEvent)) {
                case SWIPE_RIGHT:
                    mBeenSwiped = true;
                    showPreviousView(true);
                    handled = true;
                    break;
                case SWIPE_LEFT:
                    mBeenSwiped = true;
                    showNextView();
                    handled = true;
                    break;
            }
        }
        return handled;
    }


    /**
     * Determines what should be displayed on the screen. Possible options are:
     * a question, an ask repeat dialog, or the submit screen. Also saves
     * answers to the data model after checking constraints.
     */
    private void showNextView() {
        // The beginning and end Views aren't questions.
        if (isQuestionView()) {
            PromptElement p = ((QuestionView) mCurrentView).getPrompt();
            if (!p.isReadonly()) {
                int saveStatus =
                        mFormHandler.saveAnswer(p, ((QuestionView) mCurrentView).getAnswer(), true);
                if (saveStatus != SharedConstants.ANSWER_OK) {
                    createConstraintDialog(p, saveStatus);
                    return;
                }
            }
        }

        if (!mFormHandler.isEnd()) {
            PromptElement p = mFormHandler.nextPrompt();
            View next;

            if (p == null) {
                // We've reached the end of the form.
                next = createView(FormEntryViews.END_SCREEN, null);
                showView(next, AnimationType.RIGHT);
            } else if (p.isRepeat()) {
                createRepeatDialog(p);
            } else {
                next = createView(FormEntryViews.QUESTION_VIEW, p);
                showView(next, AnimationType.RIGHT);
            }
        } else {
            mBeenSwiped = false;
        }
    }


    /**
     * Determines what should be displayed between a question, or the start
     * screen and displays the appropriate view. Also saves answers to the data
     * model without checking constraints.
     */
    private void showPreviousView(boolean save) {
        // The beginning and end Views aren't questions.
        // Also, we save the answer on a back swipe, but we ignore the question
        // constraints.
        if (isQuestionView() && save) {
            PromptElement p = ((QuestionView) mCurrentView).getPrompt();
            if (!p.isReadonly()) {
                mFormHandler.saveAnswer(p, ((QuestionView) mCurrentView).getAnswer(), false);
            }
        }

        if (!mFormHandler.isBeginning()) {
            PromptElement p = mFormHandler.prevPrompt();
            View next;
            if (p == null) {
                next = createView(FormEntryViews.START_SCREEN, null);
            } else {
                next = createView(FormEntryViews.QUESTION_VIEW, p);
            }
            showView(next, AnimationType.LEFT);
        } else {
            mBeenSwiped = false;
        }
    }


    /**
     * Displays the View specified by the parameter 'next', animating both the
     * current view and next appropriately given the AnimationType. Also updates
     * the progress bar.
     */
    public void showView(View next, AnimationType from) {
        switch (from) {
            case RIGHT:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
                break;
            case LEFT:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
                break;
            case FADE:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                break;
        }

        if (mCurrentView != null) {
            mCurrentView.startAnimation(mOutAnimation);
            mRelativeLayout.removeView(mCurrentView);
        }

        mInAnimation.setAnimationListener(this);

        // We must call setMax() first because it doesn't redraw the progress
        // bar.
        mProgressBar.setMax(mFormHandler.getQuestionCount());
        mProgressBar.setProgress(mFormHandler.getQuestionNumber());

        RelativeLayout.LayoutParams p =
                new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        p.addRule(RelativeLayout.ABOVE, R.id.progressbar);

        mCurrentView = next;
        mRelativeLayout.addView(mCurrentView, p);
        // hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mCurrentView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mCurrentView.startAnimation(mInAnimation);
    }


    /*
     * Ideally, we'd like to use Android to manage dialogs with onCreateDialog()
     * and onPrepareDialog(), but dialogs with dynamic content are broken in 1.5
     * (cupcake). We do use managed dialogs for our static loading
     * ProgressDialog.
     * 
     * The main issue we noticed and are waiting to see fixed is:
     * onPrepareDialog() is not called after a screen orientation change.
     * http://code.google.com/p/android/issues/detail?id=1639
     */


    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    private void createConstraintDialog(PromptElement p, int saveStatus) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        String constraintText = null;
        switch (saveStatus) {
            case SharedConstants.ANSWER_CONSTRAINT_VIOLATED:
                if (p.getConstraintText() != null) {
                    constraintText = p.getConstraintText();
                } else {
                    constraintText = getString(R.string.invalid_answer_error);
                }
                break;
            case SharedConstants.ANSWER_REQUIRED_BUT_EMPTY:
                constraintText = getString(R.string.required_answer_error);
                break;
        }
        mAlertDialog.setMessage(constraintText);
        DialogInterface.OnClickListener constraintListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing.  should pry remove this.
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), constraintListener);
        mAlertDialog.show();
        mBeenSwiped = false;
    }


    /**
     * Creates and displays a dialog asking the user if they'd like to create a
     * repeat of the current group.
     */
    private void createRepeatDialog(PromptElement p) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        if (p.getLastRepeatCount() > 0) {
            mAlertDialog.setMessage(getString(R.string.add_another_repeat, p.getLastGroupText()));
        } else {
            mAlertDialog.setMessage(getString(R.string.add_repeat, p.getLastGroupText()));
        }
        DialogInterface.OnClickListener repeatListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes, repeat
                        mFormHandler.newRepeat();
                        showNextView();
                        break;
                    case DialogInterface.BUTTON2: // no, no repeat
                        showNextView();
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), repeatListener);
        mAlertDialog.setButton2(getString(R.string.no), repeatListener);
        mAlertDialog.show();
        mBeenSwiped = false;
    }


    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }


    /**
     * Creates a confirm/cancel dialog for deleting repeats.
     */
    private void createDeleteRepeatConfirmDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        String name = ((QuestionView) mCurrentView).getPrompt().getLastRepeatedGroupName();
        int repeatcount =
                ((QuestionView) mCurrentView).getPrompt().getLastRepeatedGroupRepeatCount();
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        mAlertDialog.setMessage(getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        mFormHandler.deleteCurrentRepeat();
                        showPreviousView(false);
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), quitListener);
        mAlertDialog.setButton2(getString(R.string.no), quitListener);
        mAlertDialog.show();
    }


    /*
     * Called during a 'save and exit' command. The form is not 'done' here.
     */
    private void saveData() {
        mFormHandler.finalizeDataModel();
        if (mFormHandler.exportData(mAnswersPath, getApplicationContext(), false)) {
            Toast.makeText(getApplicationContext(), getString(R.string.data_saved_ok),
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.data_saved_error),
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Confirm save and quit dialog
     */
    private void createSaveQuitDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setMessage(getString(R.string.savequit_confirm));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        saveData();
                        finish();
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), quitListener);
        mAlertDialog.setButton2(getString(R.string.no), quitListener);
        mAlertDialog.show();
    }


    /**
     * Confirm quit dialog
     */
    private void createQuitDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setMessage(getString(R.string.entryquit_confirm));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        FileDbAdapter fda = new FileDbAdapter(FormEntry.this);
                        fda.open();
                        Cursor c = fda.fetchFile(new File(mAnswersPath).getName());
                        if (c != null && c.getCount() > 0) {
                            Log.i(t, "prevously saved");
                        } else {
                            // not previously saved, cleaning up
                            FileUtils.deleteFolder(mAnswersPath);
                        }
                        fda.close();
                        finish();
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), quitListener);
        mAlertDialog.setButton2(getString(R.string.no), quitListener);
        mAlertDialog.show();
    }


    /**
     * Confirm clear dialog
     */
    private void createClearDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setMessage(getString(R.string.clearanswer_confirm));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        QuestionView qv = ((QuestionView) mCurrentView);
                        qv.clearAnswer();
                        mFormHandler.saveAnswer(qv.getPrompt(), qv.getAnswer(), false);
                        break;
                    case DialogInterface.BUTTON2: // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), quitListener);
        mAlertDialog.setButton2(getString(R.string.no), quitListener);
        mAlertDialog.show();
    }


    /**
     * Creates and displays a dialog allowing the user to set the language for
     * the form.
     */
    private void createLanguageDialog() {
        final String[] languages = mFormHandler.getLanguages();
        int selected = -1;
        if (languages != null) {
            String language = mFormHandler.getCurrentLanguage();
            for (int i = 0; i < languages.length; i++) {
                if (language.equals(languages[i])) {
                    selected = i;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.languages_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAlertDialog =
                new AlertDialog.Builder(this).setSingleChoiceItems(languages, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mFormHandler.setLanguage(languages[whichButton]);
                                dialog.dismiss();
                                if (isQuestionView()) {
                                    PromptElement p = ((QuestionView) mCurrentView).getPrompt();
                                    if (!p.isReadonly()) {
                                        mFormHandler.saveAnswer(p, ((QuestionView) mCurrentView)
                                                .getAnswer(), false);
                                    }
                                }
                                refreshCurrentView();
                            }
                        }).setTitle(getString(R.string.change_language)).setNegativeButton(
                        getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).create();
        mAlertDialog.show();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mFormLoaderTask.setFormLoaderListener(null);
                                mFormLoaderTask.cancel(true);
                                finish();
                            }
                        };
                mProgressDialog.setMessage(getString(R.string.loading_form));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    private void dismissDialogs() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }



    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        Log.d(t, "onPause");
        dismissDialogs();
        super.onPause();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        Log.d(t, "onResume");
        if (mFormLoaderTask != null) {
            mFormLoaderTask.setFormLoaderListener(this);
            if (mFormLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                dismissDialog(PROGRESS_DIALOG);
                refreshCurrentView();
            }
        }
        super.onResume();
    }



    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                createQuitDialog();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        if (mFormLoaderTask != null) mFormLoaderTask.setFormLoaderListener(null);
        super.onDestroy();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.view.animation.Animation.AnimationListener#onAnimationEnd(android
     * .view.animation.Animation)
     */
    public void onAnimationEnd(Animation arg0) {
        mBeenSwiped = false;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.view.animation.Animation.AnimationListener#onAnimationRepeat(
     * android.view.animation.Animation)
     */
    public void onAnimationRepeat(Animation animation) {
        // Added by AnimationListener interface.
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * android.view.animation.Animation.AnimationListener#onAnimationStart(android
     * .view.animation.Animation)
     */
    public void onAnimationStart(Animation animation) {
        // Added by AnimationListener interface.
    }


    /**
     * loadingComplete() is called by FormLoaderTask once it has finished
     * loading a form.
     */
    public void loadingComplete(FormHandler formHandler) {
        dismissDialog(PROGRESS_DIALOG);
        if (formHandler == null) {
            createErrorDialog(getString(R.string.form_load_error, mFormPath), true);
        } else {
            mFormHandler = formHandler;

            // restore saved data
            if (mInstancePath != null) {
                mAnswersPath = mInstancePath.substring(0, mInstancePath.lastIndexOf("/"));
            } else {
                // create new answer folder
                String time =
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance()
                                .getTime());
                String file = mFormPath.toString();
                file = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'));
                String path = SharedConstants.ANSWERS_PATH + file + "_" + time;
                if (FileUtils.createFolder(path)) {
                    mAnswersPath = path;
                }
            }
            refreshCurrentView();
        }
    }

}
