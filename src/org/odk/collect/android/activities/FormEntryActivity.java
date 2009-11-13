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

package org.odk.collect.android.activities;

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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.IService;
import org.javarosa.model.xform.XFormsModule;
import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.logic.FormHandler;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.GestureDetector;
import org.odk.collect.android.utilities.ImageUtils;
import org.odk.collect.android.views.QuestionView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;


/**
 * FormEntryActivity is responsible for displaying questions, animating
 * transitions between questions, and allowing the user to enter data.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormEntryActivity extends Activity implements AnimationListener, FormLoaderListener,
        FormSavedListener {
    private final String t = "FormEntryActivity";

    private static final String FORMPATH = "formpath";
    private static final String INSTANCEPATH = "instancepath";
    private static final String NEWFORM = "newform";

    private static final int MENU_CLEAR = Menu.FIRST;
    private static final int MENU_DELETE_REPEAT = Menu.FIRST + 1;
    private static final int MENU_LANGUAGES = Menu.FIRST + 2;
    private static final int MENU_HIERARCHY_VIEW = Menu.FIRST + 3;
    private static final int MENU_SUBMENU = Menu.FIRST + 4;
    private static final int MENU_SAVE = Menu.FIRST + 5;
    private static final int MENU_COMPLETE = Menu.FIRST + 6;

    private static final int PROGRESS_DIALOG = 1;
    private static final int SAVING_DIALOG = 2;

    // uncomment when ProgressBar slowdown is fixed.
    // private ProgressBar mProgressBar;
    private String mFormPath;
    private String mInstancePath;

    private GestureDetector mGestureDetector;
    public static FormHandler mFormHandler;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private RelativeLayout mRelativeLayout;
    private View mCurrentView;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;

    // used to limit forward/backward swipes to one per question
    private boolean mBeenSwiped;

    private FormLoaderTask mFormLoaderTask;
    private SaveToDiskTask mSaveToDiskTask;

    enum AnimationType {
        LEFT, RIGHT, FADE
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_entry);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        // mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);

        mBeenSwiped = false;
        mAlertDialog = null;
        mCurrentView = null;
        mInAnimation = null;
        mOutAnimation = null;
        mGestureDetector = new GestureDetector();

        // Load JavaRosa modules.
        // needed to restore forms.
        new XFormsModule().registerModule(null);

        // load JavaRosa services
        // needed to overwrite rms property manager
        Vector<IService> v = new Vector<IService>();
        v.add(new PropertyManager(getApplicationContext()));
        JavaRosaServiceProvider.instance().initialize(v);

        Boolean newForm = true;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FORMPATH)) {
                mFormPath = savedInstanceState.getString(FORMPATH);
            }
            if (savedInstanceState.containsKey(INSTANCEPATH)) {
                mInstancePath = savedInstanceState.getString(INSTANCEPATH);
            }
            if (savedInstanceState.containsKey(NEWFORM)) {
                newForm = savedInstanceState.getBoolean(NEWFORM, true);
            }
        }

        // Check to see if this is a screen flip or a new form load.
        Object data = getLastNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            mFormLoaderTask = (FormLoaderTask) data;
        } else if (data instanceof SaveToDiskTask) {
            mSaveToDiskTask = (SaveToDiskTask) data;
        } else if (data == null) {
            if (!newForm) {
                refreshCurrentView();
                return;
            }

            // Not a restart from a screen orientation change (or other).
            mFormHandler = null;

            Intent intent = getIntent();
            if (intent != null) {
                mFormPath = intent.getStringExtra(GlobalConstants.KEY_FORMPATH);
                mInstancePath = intent.getStringExtra(GlobalConstants.KEY_INSTANCEPATH);
                mFormLoaderTask = new FormLoaderTask();
                mFormLoaderTask.execute(mFormPath, mInstancePath);
                showDialog(PROGRESS_DIALOG);
            }
        }
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
        outState.putString(INSTANCEPATH, mInstancePath);
        outState.putBoolean(NEWFORM, false);
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
            case GlobalConstants.BARCODE_CAPTURE:
                String sb = intent.getStringExtra("SCAN_RESULT");
                ((QuestionView) mCurrentView).setBinaryData(sb);
                saveCurrentAnswer(false);
                break;
            case GlobalConstants.IMAGE_CAPTURE:
                if (ImageUtils.hasImageCaptureBug()) {
                    File fi = new File(GlobalConstants.IMAGE_PATH);
                    try {
                        Uri ui =
                                Uri.parse(android.provider.MediaStore.Images.Media.insertImage(
                                        getContentResolver(), fi.getAbsolutePath(), null, null));
                        if (!fi.delete()) {
                            Log.i(t, "Failed to delete " + fi);
                        }
                        ((QuestionView) mCurrentView).setBinaryData(ui);
                        saveCurrentAnswer(false);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    refreshCurrentView();
                    break;
                }
                // $FALL-THROUGH$
            case GlobalConstants.AUDIO_CAPTURE:
            case GlobalConstants.VIDEO_CAPTURE:
                Uri um = intent.getData();
                ((QuestionView) mCurrentView).setBinaryData(um);
                saveCurrentAnswer(false);
                refreshCurrentView();
                break;
            case GlobalConstants.LOCATION_CAPTURE:
                String sl = intent.getStringExtra("LOCATION_RESULT");
                ((QuestionView) mCurrentView).setBinaryData(sl);
                saveCurrentAnswer(false);
                break;
        }
    }


    /**
     * Refreshes the current view. mFormHandler and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     */
    public void refreshCurrentView() {
        PromptElement p = mFormHandler.currentPrompt();
        /*
         * Since we're not using managed dialogs, go back to the last actual
         * question if it's a repeat dialog.
         */
        // TODO: carlhartung. Desired functionality would be to display the same
        // dialog again
        // not the last question.
        if (p.getType() == PromptElement.TYPE_REPEAT_DIALOG) {
            p = mFormHandler.prevPrompt();
        }
        View current = createView(p);
        showView(current, AnimationType.FADE);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: carlhartung. This contains remains from when we changed the
        // menu options based on
        // question type. we now just enable and disable functionality so we
        // should
        // not add/remove each time.
        menu.removeItem(MENU_CLEAR);
        menu.removeItem(MENU_DELETE_REPEAT);
        menu.removeItem(MENU_LANGUAGES);
        menu.removeItem(MENU_HIERARCHY_VIEW);
        menu.removeItem(MENU_SUBMENU);

        SubMenu sm =
                menu.addSubMenu(0, MENU_SUBMENU, 0, R.string.quit_entry).setIcon(
                        android.R.drawable.ic_menu_save);
        sm.add(0, MENU_SAVE, 0, getString(R.string.save_for_later));
        sm.add(0, MENU_COMPLETE, 0, getString(R.string.finalize_for_send));

        PromptElement pe = null;
        if (currentPromptIsQuestion()) {
            pe = mFormHandler.currentPrompt();
        }
        menu.add(0, MENU_CLEAR, 0, getString(R.string.clear_answer)).setIcon(
                android.R.drawable.ic_menu_close_clear_cancel).setEnabled(
                pe != null && !pe.isReadOnly() ? true : false);
        menu.add(0, MENU_DELETE_REPEAT, 0, getString(R.string.delete_repeat)).setIcon(
                R.drawable.ic_menu_clear_playlist).setEnabled(
                pe != null && pe.isInRepeatableGroup() ? true : false);
        menu.add(0, MENU_HIERARCHY_VIEW, 0, getString(R.string.view_hierarchy)).setIcon(
                R.drawable.ic_menu_goto);

        menu.add(0, MENU_LANGUAGES, 0, getString(R.string.change_language)).setIcon(
                R.drawable.ic_menu_start_conversation).setEnabled(
                (mFormHandler.getLanguages() == null || mFormHandler.getLanguages().length == 1) ? false : true);

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
            case MENU_SAVE:
                createSaveExitDialog(false);
                return true;
            case MENU_COMPLETE:
                createSaveExitDialog(true);
                return true;
            case MENU_HIERARCHY_VIEW:
                Intent i = new Intent(this, FormHierarchyActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 
     * @return true if the current View represents a question in the form
     */
    private boolean currentPromptIsQuestion() {
        return (mFormHandler.currentPrompt().getType() == PromptElement.TYPE_QUESTION);
    }


    private boolean saveCurrentAnswer(boolean evaluateConstraints) {
        PromptElement pe = mFormHandler.currentPrompt();

        // If the question is readonly there's nothing to save.
        if (!pe.isReadOnly()) {

            int saveStatus =
                    mFormHandler.saveAnswer(pe, ((QuestionView) mCurrentView).getAnswer(),
                            evaluateConstraints);
            if (evaluateConstraints && saveStatus != GlobalConstants.ANSWER_OK) {
                createConstraintToast(pe, saveStatus);
                return false;
            }
        }
        return true;
    }


    private void clearCurrentAnswer() {
        if (!mFormHandler.currentPrompt().isReadOnly())
            ((QuestionView) mCurrentView).clearAnswer();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance() If we're
     * loading, then we pass the loading thread to our next instance.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        // if a form is loading, pass the loader task
        if (mFormLoaderTask != null && mFormLoaderTask.getStatus() != AsyncTask.Status.FINISHED)
            return mFormLoaderTask;

        // if a form is writing to disk, pass the save to disk task
        if (mSaveToDiskTask != null && mSaveToDiskTask.getStatus() != AsyncTask.Status.FINISHED)
            return mSaveToDiskTask;

        // mFormHandler is static so we don't need to pass it.
        if (mFormHandler != null && currentPromptIsQuestion()) {
            saveCurrentAnswer(false);
        }
        return null;
    }


    /**
     * Creates a view given the View type and a prompt
     * 
     * @param prompt
     * @return newly created View
     */
    private View createView(PromptElement prompt) {
        setTitle(getString(R.string.app_name) + " > " + mFormHandler.getFormTitle());
        FileDbAdapter fda = null;
        Cursor c = null;

        switch (prompt.getType()) {
            case PromptElement.TYPE_START:
                View startView = View.inflate(this, R.layout.form_entry_start, null);
                setTitle(getString(R.string.app_name) + " > " + mFormHandler.getFormTitle());

                fda = new FileDbAdapter(FormEntryActivity.this);
                fda.open();
                c = fda.fetchFilesByPath(mInstancePath, null);
                if (c != null && c.getCount() > 0) {
                    ((TextView) startView.findViewById(R.id.description)).setText(getString(
                            R.string.review_data_description, c.getString(c
                                    .getColumnIndex(FileDbAdapter.KEY_DISPLAY))));
                } else {
                    ((TextView) startView.findViewById(R.id.description)).setText(getString(
                            R.string.enter_data_description, mFormHandler.getFormTitle()));
                }

                // clean up cursor
                if (c != null) {
                    c.close();
                }

                fda.close();
                return startView;
            case PromptElement.TYPE_END:
                View endView = View.inflate(this, R.layout.form_entry_end, null);
                fda = new FileDbAdapter(FormEntryActivity.this);
                fda.open();
                c = fda.fetchFilesByPath(mInstancePath, null);
                if (c != null && c.getCount() > 0) {
                    ((TextView) endView.findViewById(R.id.description)).setText(getString(
                            R.string.save_review_data_description, c.getString(c
                                    .getColumnIndex(FileDbAdapter.KEY_DISPLAY))));
                } else {
                    ((TextView) endView.findViewById(R.id.description)).setText(getString(
                            R.string.save_enter_data_description, mFormHandler.getFormTitle()));
                }
                // Create 'save complete' button.
                ((Button) endView.findViewById(R.id.complete_exit_button))
                        .setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                // Form is markd as 'done' here.
                                saveDataToDisk(true);
                            }
                        });
                // Create 'save for later' button
                ((Button) endView.findViewById(R.id.save_exit_button))
                        .setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                // Form is markd as 'saved' here.
                                saveDataToDisk(false);
                            }
                        });

                // clean up cursor
                if (c != null) {
                    c.close();
                }

                fda.close();
                return endView;
            case PromptElement.TYPE_QUESTION:
            default:
                QuestionView qv = new QuestionView(this, prompt, mInstancePath);
                qv.buildView(prompt);
                return qv;
        }
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
        /*
         * constrain the user to only be able to swipe (that causes a view
         * transition) once per screen with the mBeenSwiped variable.
         */
        boolean handled = false;
        if (!mBeenSwiped) {
            switch (mGestureDetector.getGesture(motionEvent)) {
                case SWIPE_RIGHT:
                    mBeenSwiped = true;
                    showPreviousView();
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
        if (currentPromptIsQuestion()) {
            if (!saveCurrentAnswer(true)) {
                // A constraint was violated so a dialog should be showing.
                return;
            }
        }

        if (!mFormHandler.isEnd()) {
            PromptElement p = mFormHandler.nextPrompt();
            View next;

            switch (p.getType()) {
                case PromptElement.TYPE_QUESTION:
                case PromptElement.TYPE_END:
                    next = createView(p);
                    showView(next, AnimationType.RIGHT);
                    break;
                case PromptElement.TYPE_REPEAT_DIALOG:
                    createRepeatDialog(p);
                    break;
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
    private void showPreviousView() {
        // The answer is saved on a back swipe, but question constraints are
        // ignored.
        if (currentPromptIsQuestion()) {
            saveCurrentAnswer(false);
        }

        if (!mFormHandler.isBeginning()) {
            PromptElement p = mFormHandler.prevPrompt();
            View next = createView(p);
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

        // UnComment to make progress bar work.
        // WARNING: will currently slow large forms considerably
        // TODO: make the progress bar fast.
        // mProgressBar.setMax(mFormHandler.getQuestionCount());
        // mProgressBar.setProgress(mFormHandler.getQuestionNumber());

        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        lp.addRule(RelativeLayout.ABOVE, R.id.progressbar);

        mCurrentView = next;
        mRelativeLayout.addView(mCurrentView, lp);

        mCurrentView.startAnimation(mInAnimation);
        if (mCurrentView instanceof QuestionView)
            ((QuestionView) mCurrentView).setFocus(this);
        else {
            InputMethodManager inputManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(mCurrentView.getWindowToken(), 0);
        }
    }


    // TODO: use managed dialogs when the bugs are fixed
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


    public void createLocationDialog() {
        mProgressDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geopointButtonListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("yaw", "inside form entry cancel button");
                    }
                };
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setTitle(getString(R.string.getting_location));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setButton(getString(R.string.cancel), geopointButtonListener);
        mProgressDialog.show();
    }


    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    private void createConstraintToast(PromptElement p, int saveStatus) {
        String constraintText = null;
        switch (saveStatus) {
            case GlobalConstants.ANSWER_CONSTRAINT_VIOLATED:
                if (p.getConstraintText() != null) {
                    constraintText = p.getConstraintText();
                } else {
                    constraintText = getString(R.string.invalid_answer_error);
                }
                break;
            case GlobalConstants.ANSWER_REQUIRED_BUT_EMPTY:
                constraintText = getString(R.string.required_answer_error);
                break;
        }

        showCustomToast(constraintText);
        mBeenSwiped = false;
    }


    private void showCustomToast(String message) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.toast_view, null);

        // set the text in the view
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(message);

        Toast t = new Toast(this);
        t.setView(view);
        t.setDuration(Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
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
        String name = mFormHandler.currentPrompt().getLastRepeatedGroupName();
        int repeatcount = mFormHandler.currentPrompt().getLastRepeatedGroupRepeatCount();
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        mAlertDialog.setMessage(getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        mFormHandler.deleteCurrentRepeat();
                        showPreviousView();
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
    private void saveDataToDisk(boolean markCompleted) {
        mSaveToDiskTask = new SaveToDiskTask();
        mSaveToDiskTask.setFormSavedListener(this);
        mSaveToDiskTask.setExportVars(mInstancePath, getApplicationContext(), markCompleted);
        mSaveToDiskTask.execute();
        showDialog(SAVING_DIALOG);
    }


    /**
     * Confirm save and quit dialog
     */
    private void createSaveExitDialog(boolean markCompleted) {

        int promptType = mFormHandler.currentPrompt().getType();
        boolean saveStatus = true;

        if (promptType != PromptElement.TYPE_START && promptType != PromptElement.TYPE_END) {
            saveStatus = saveCurrentAnswer(true);
        }

        if (saveStatus) {
            saveDataToDisk(markCompleted);
        }
    }


    /**
     * Confirm quit dialog
     */
    private void createQuitDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setMessage(getString(R.string.entry_exit_confirm));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // yes
                        FileDbAdapter fda = new FileDbAdapter(FormEntryActivity.this);
                        fda.open();
                        Cursor c = fda.fetchFilesByPath(mInstancePath, null);
                        if (c != null && c.getCount() > 0) {
                            Log.i(t, "prevously saved");
                        } else {
                            // not previously saved, cleaning up
                            String instanceFolder =
                                    mInstancePath.substring(0, mInstancePath.lastIndexOf("/") + 1);
                            FileUtils.deleteFolder(instanceFolder);
                        }
                        // clean up cursor
                        if (c != null) {
                            c.close();
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
                        clearCurrentAnswer();
                        saveCurrentAnswer(false);
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
        }
        mAlertDialog =
                new AlertDialog.Builder(this).setSingleChoiceItems(languages, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mFormHandler.setLanguage(languages[whichButton]);
                                dialog.dismiss();
                                if (currentPromptIsQuestion()) {
                                    saveCurrentAnswer(false);
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
                mProgressDialog.setTitle(getString(R.string.loading_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case SAVING_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener savingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mSaveToDiskTask.setFormSavedListener(null);
                                mSaveToDiskTask.cancel(true);
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.saving_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), savingButtonListener);
                return mProgressDialog;

        }
        return null;
    }


    /**
     * Dismiss any showing dialogs
     */
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
        if (mSaveToDiskTask != null) {
            mSaveToDiskTask.setFormSavedListener(this);
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
        if (mSaveToDiskTask != null) mSaveToDiskTask.setFormSavedListener(null);
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
            createErrorDialog(getString(R.string.load_error, mFormPath.substring(mFormPath
                    .lastIndexOf('/') + 1)), true);
        } else {
            mFormHandler = formHandler;

            // Set saved answer path
            if (mInstancePath == null) {

                // Create new answer folder.
                String time =
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance()
                                .getTime());
                String file =
                        mFormPath.substring(mFormPath.lastIndexOf('/') + 1, mFormPath
                                .lastIndexOf('.'));
                String path = GlobalConstants.INSTANCES_PATH + file + "_" + time;
                if (FileUtils.createFolder(path)) {
                    mInstancePath = path + "/" + file + "_" + time + ".xml";
                }
            } else {
                // we've just loaded a saved form, so start in the hierarchy
                // view
                Intent i = new Intent(this, FormHierarchyActivity.class);
                startActivity(i);
                return; // so we don't show the intro screen before jumping to
                // the hierarchy
            }

            refreshCurrentView();
        }
    }


    public void savingComplete(int saveStatus) {
        dismissDialog(SAVING_DIALOG);
        switch (saveStatus) {
            case SaveToDiskTask.SAVED:
                Toast.makeText(getApplicationContext(), getString(R.string.data_saved_ok),
                        Toast.LENGTH_SHORT).show();
                finish();
                break;
            case SaveToDiskTask.SAVE_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.data_saved_error),
                        Toast.LENGTH_LONG).show();
                break;
            case GlobalConstants.ANSWER_CONSTRAINT_VIOLATED:
            case GlobalConstants.ANSWER_REQUIRED_BUT_EMPTY:
                refreshCurrentView();
                createConstraintToast(mFormHandler.currentPrompt(), saveStatus);
                break;
        }
    }

}
