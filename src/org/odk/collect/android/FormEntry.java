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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import org.odk.collect.android.FormLoader.LoadingState;

import java.io.File;

/**
 * FormEntry is responsible for displaying questions, animating transitions
 * between questions, and allowing the user to enter data.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormEntry extends Activity implements AnimationListener, FormLoaderListener {

    private final String t = "FormEntry";

    private final String FORMPATH = "formpath";
    private final String FORMLOADER = "formloader";

    public static final int MENU_CLEAR = Menu.FIRST;
    public static final int MENU_DELETE_REPEAT = Menu.FIRST + 1;
    public static final int MENU_QUIT = Menu.FIRST + 2;
    public static final int MENU_LANGUAGES = Menu.FIRST + 3;

    private ProgressBar mProgressBar;
    private String mFormPath;

    private GestureDetector mGestureDetector;
    private FormHandler mFormHandler;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private RelativeLayout mRelativeLayout;
    private View mCurrentView;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    private boolean mBeenSwiped;

    private FormLoader mFormLoader;
    private final Handler mHandler = new Handler();


    private final Runnable mUpdateDisplayByFormLoader = new Runnable() {
        public void run() {
            updateDisplay();
        }
    };

    enum AnimationType {
        LEFT, RIGHT, FADE
    }

    enum FormEntryViews {
        START_SCREEN, QUESTION_VIEW, END_SCREEN
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setTheme(SharedConstants.APPLICATION_THEME);

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        setContentView(R.layout.formentry);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        initializeVariables();

        // if starting for the first time, get stored data
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FORMLOADER)) {
                mFormLoader = (FormLoader) savedInstanceState.getSerializable(FORMLOADER);
            }
            if (savedInstanceState.containsKey(FORMPATH)) {
                mFormPath = savedInstanceState.getString(FORMPATH);
            }
        }

        if (mFormLoader == null) {
            mFormLoader = new FormLoader();
        }

        if (mFormLoader.getState() == LoadingState.RUNNING
                || mFormLoader.getState() == LoadingState.NOT_RUNNING) {
            mProgressDialog.show();
        }

        if (mFormLoader.getState() == LoadingState.RUNNING) {
            // If we're loading a form then the mProgressDialog is displayed and
            // we need to wait for the loading thread to finish.
            return;
        }

        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            // The application is starting for the first time.
            Intent intent = getIntent();
            if (intent != null) {

                mFormPath = intent.getStringExtra(SharedConstants.FILEPATH_KEY);
                mFormLoader.loadForm(mFormPath);

            }
        } else {
            // The application had a screen flip or a similar restart happened
            // at a point other than the form loading.
            mFormHandler = (FormHandler) data;
            refreshCurrentView();
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

        mGestureDetector = new GestureDetector();
        setupLoadingDialog();
    }


    /** Builds the dialog which is shown to the user when an form is loading */
    private void setupLoadingDialog() {
        mProgressDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener loadingButtonListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // mFormLoader.setFormLoaderListener(null);
                        finish();
                    }
                };
        mProgressDialog.setMessage(getString(R.string.loading_form));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
    }



    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(FORMLOADER, mFormLoader);
        outState.putString(FORMPATH, mFormPath);
    }


    /**
     * updateDisplay updates the display based on the state of FormLoader. This
     * should only be called in onResume() or by updateDisplayByFormLoader(). If
     * you want to manually refresh the page call refreshCurrentView().
     */
    private void updateDisplay() {
        switch (mFormLoader.getState()) {
            case FINISHED:
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                refreshCurrentView();
                break;
            case ERROR:
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                createErrorDialog(getString(R.string.form_load_error, mFormPath), true);
                break;
        }
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
                // save bitmap in question view and data model
                PromptElement pi = ((QuestionView) mCurrentView).getPrompt();
                if (!pi.isReadonly()) {
                    Bitmap b = BitmapFactory.decodeFile(SharedConstants.TMPFILE_PATH);
                    ((QuestionView) mCurrentView).setBinaryData(b);
                    mFormHandler.saveAnswer(pi, ((QuestionView) mCurrentView).getAnswer(), false);
                }
                // delete the tmp file
                new File(SharedConstants.TMPFILE_PATH).delete();
                break;
            case (SharedConstants.BARCODE_CAPTURE):
                PromptElement pe = ((QuestionView) mCurrentView).getPrompt();
                if (!pe.isReadonly()) {
                    String s = intent.getStringExtra("SCAN_RESULT");
                    ((QuestionView) mCurrentView).setBinaryData(s);
                    mFormHandler.saveAnswer(pe, ((QuestionView) mCurrentView).getAnswer(), false);
                }
                break;
            case SharedConstants.AUDIO_CAPTURE:
                Uri u = intent.getData();
                PromptElement pa = ((QuestionView) mCurrentView).getPrompt();
                if (!pa.isReadonly()) {
                    String s = u.toString();
                    ((QuestionView) mCurrentView).setBinaryData(s);
                    mFormHandler.saveAnswer(pa, ((QuestionView) mCurrentView).getAnswer(), false);
                }
                break;
            case SharedConstants.VIDEO_CAPTURE:
                Uri uv = intent.getData();
                PromptElement pv = ((QuestionView) mCurrentView).getPrompt();
                if (!pv.isReadonly()) {
                    String s = uv.toString();
                    ((QuestionView) mCurrentView).setBinaryData(s);
                    mFormHandler.saveAnswer(pv, ((QuestionView) mCurrentView).getAnswer(), false);
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
        } else {
            menu.removeItem(MENU_CLEAR);
            menu.removeItem(MENU_DELETE_REPEAT);
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
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
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

                // set window title using form filename
                String filename =
                        mFormHandler.getSourcePath().substring(
                                mFormHandler.getSourcePath().lastIndexOf("/") + 1,
                                mFormHandler.getSourcePath().length());
                setTitle(getString(R.string.app_name) + " > " + filename);

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
                                mFormHandler.finalizeDataModel();
                                if (mFormHandler.exportData()) {
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
                nextView = new QuestionView(this, prompt);
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
     * and onPrepareDialog(), but it's currently horribly broken so the methods
     * below manage our dialogs for us.
     * 
     * The two issues we've noticed and are waiting to see fixed are: 1)
     * onPrepareDialog() is not called after a screen orientation change.
     * http://code.google.com/p/android/issues/detail?id=1639
     * 
     * 2) The activity leaks the dialog window when the orientation changes
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
                mBeenSwiped = false;
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), constraintListener);
        mAlertDialog.show();
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
                    case AlertDialog.BUTTON1: // yes, repeat
                        mFormHandler.newRepeat();
                        showNextView();
                        break;
                    case AlertDialog.BUTTON2: // no, no repeat
                        showNextView();
                        break;
                }
                mBeenSwiped = false;
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), repeatListener);
        mAlertDialog.setButton2(getString(R.string.no), repeatListener);
        mAlertDialog.show();
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
                    case AlertDialog.BUTTON1:
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
                    case AlertDialog.BUTTON1: // yes
                        mFormHandler.deleteCurrentRepeat();
                        showPreviousView(false);
                        break;
                    case AlertDialog.BUTTON2: // no
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
                    case AlertDialog.BUTTON1: // yes
                        finish();
                        break;
                    case AlertDialog.BUTTON2: // no
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
                    case AlertDialog.BUTTON1: // yes
                        QuestionView qv = ((QuestionView) mCurrentView);
                        qv.clearAnswer();
                        mFormHandler.saveAnswer(qv.getPrompt(), qv.getAnswer(), false);
                        break;
                    case AlertDialog.BUTTON2: // no
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


    private void dismissDialogs() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
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
        mFormLoader.setFormLoaderListener(null);
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
        mFormLoader.setFormLoaderListener(this);
        updateDisplay();
        super.onResume();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        Log.d(t, "onDestroy");
        // dismissDialogs();
        super.onStop();
    }



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
     * loadingComplete() is called by FormLoader once it has finished loading a
     * form.
     */
    public void loadingComplete(FormHandler formHandler) {
        if (mFormLoader.getState() == LoadingState.FINISHED) {
            mFormHandler = formHandler;
            mFormHandler.initialize(getApplicationContext());
            mFormHandler.setSourcePath(mFormPath);
        }

        mHandler.post(mUpdateDisplayByFormLoader);
    }

}
