/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.helpers.ContentResolverHelper;
import org.odk.collect.android.dao.helpers.FormsDaoHelper;
import org.odk.collect.android.dao.helpers.InstancesDaoHelper;
import org.odk.collect.android.exception.GDriveConnectionException;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.fragments.dialogs.RankingWidgetDialog;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.listeners.SavePointListener;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.FormController.FailedConstraint;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveFormIndexTask;
import org.odk.collect.android.tasks.SavePointTask;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DependencyProvider;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.TimerLogger;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.DateTimeWidget;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.RangeWidget;
import org.odk.collect.android.widgets.StringWidget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_MOVING_BACKWARDS;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.checkIfStoragePermissionsGranted;
import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;
import static org.odk.collect.android.utilities.PermissionUtils.requestStoragePermissions;

/**
 * FormEntryActivity is responsible for displaying questions, animating
 * transitions between questions, and allowing the user to enter data.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 * option)
 */
public class FormEntryActivity extends CollectAbstractActivity implements AnimationListener,
        FormLoaderListener, FormSavedListener, AdvanceToNextListener,
        OnGestureListener, SavePointListener, NumberPickerDialog.NumberPickerListener,
        DependencyProvider<ActivityAvailability>,
        CustomDatePickerDialog.CustomDatePickerDialogListener,
        RankingWidgetDialog.RankingListener,
        SaveFormIndexTask.SaveFormIndexListener {

    // save with every swipe forward or back. Timings indicate this takes .25
    // seconds.
    // if it ever becomes an issue, this value can be changed to save every n'th
    // screen.
    private static final int SAVEPOINT_INTERVAL = 1;

    // Defines for FormEntryActivity
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private static final boolean EVALUATE_CONSTRAINTS = true;
    private static final boolean DO_NOT_EVALUATE_CONSTRAINTS = false;

    // Extra returned from gp activity
    public static final String LOCATION_RESULT = "LOCATION_RESULT";
    public static final String BEARING_RESULT = "BEARING_RESULT";
    public static final String GEOSHAPE_RESULTS = "GEOSHAPE_RESULTS";
    public static final String GEOTRACE_RESULTS = "GEOTRACE_RESULTS";

    public static final String KEY_INSTANCES = "instances";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    private static final String KEY_SAVE_NAME = "saveName";

    // Identifies the gp of the form used to launch form entry
    public static final String KEY_FORMPATH = "formpath";

    // Identifies whether this is a new form, or reloading a form after a screen
    // rotation (or similar)
    private static final String NEWFORM = "newform";
    // these are only processed if we shut down and are restoring after an
    // external intent fires

    public static final String KEY_INSTANCEPATH = "instancepath";
    public static final String KEY_XPATH = "xpath";
    public static final String KEY_XPATH_WAITING_FOR_DATA = "xpathwaiting";

    // Tracks whether we are autosaving
    public static final String KEY_AUTO_SAVED = "autosaved";

    public static final String EXTRA_TESTING_PATH = "testingPath";

    private static final int PROGRESS_DIALOG = 1;
    private static final int SAVING_DIALOG = 2;
    private static final int SAVING_IMAGE_DIALOG = 3;

    private boolean autoSaved;
    private boolean allowMovingBackwards;

    // Random ID
    private static final int DELETE_REPEAT = 654321;

    private String formPath;
    private String saveName;

    private GestureDetector gestureDetector;

    private Animation inAnimation;
    private Animation outAnimation;
    private View staleView;

    private LinearLayout questionHolder;
    private View currentView;

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private String errorMessage;
    private boolean shownAlertDialogIsGroupRepeat;

    // used to limit forward/backward swipes to one per question
    private boolean beenSwiped;

    private final Object saveDialogLock = new Object();
    private int viewCount;

    private FormLoaderTask formLoaderTask;
    private SaveToDiskTask saveToDiskTask;

    private ImageButton nextButton;
    private ImageButton backButton;

    private ODKView odkView;
    private boolean doSwipe = true;
    private String instancePath;
    private String startingXPath;
    private String waitingXPath;
    private boolean newForm = true;
    private boolean onResumeWasCalledWithoutPermissions;

    public void allowSwiping(boolean doSwipe) {
        this.doSwipe = doSwipe;
    }

    enum AnimationType {
        LEFT, RIGHT, FADE
    }

    private boolean showNavigationButtons;

    private Bundle state;

    @NonNull
    private ActivityAvailability activityAvailability = new ActivityAvailability(this);

    private boolean shouldOverrideAnimations;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.form_entry);

        errorMessage = null;

        beenSwiped = false;

        gestureDetector = new GestureDetector(this, this);
        questionHolder = findViewById(R.id.questionholder);

        initToolbar();

        nextButton = findViewById(R.id.form_forward_button);
        nextButton.setOnClickListener(v -> {
            beenSwiped = true;
            showNextView();
        });

        backButton = findViewById(R.id.form_back_button);
        backButton.setOnClickListener(v -> {
            beenSwiped = true;
            showPreviousView();
        });

        requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    Collect.createODKDirs();
                    setupFields(savedInstanceState);
                    loadForm();

                    /**
                     * Since onResume is called after onCreate we check to see if
                     * it was called without the permissions that are required. If so then
                     * we call it.This is especially useful for cases where a user might revoke
                     * permissions to storage and not know the implications it has on the form entry.
                     */
                    if (onResumeWasCalledWithoutPermissions) {
                        onResume();
                    }
                } catch (RuntimeException e) {
                    createErrorDialog(e.getMessage(), EXIT);
                    return;
                }
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finishAllActivities(FormEntryActivity.this);
            }
        });
    }

    private void setupFields(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            state = savedInstanceState;
            if (savedInstanceState.containsKey(KEY_FORMPATH)) {
                formPath = savedInstanceState.getString(KEY_FORMPATH);
            }
            if (savedInstanceState.containsKey(KEY_INSTANCEPATH)) {
                instancePath = savedInstanceState.getString(KEY_INSTANCEPATH);
            }
            if (savedInstanceState.containsKey(KEY_XPATH)) {
                startingXPath = savedInstanceState.getString(KEY_XPATH);
                Timber.i("startingXPath is: %s", startingXPath);
            }
            if (savedInstanceState.containsKey(KEY_XPATH_WAITING_FOR_DATA)) {
                waitingXPath = savedInstanceState
                        .getString(KEY_XPATH_WAITING_FOR_DATA);
                Timber.i("waitingXPath is: %s", waitingXPath);
            }
            if (savedInstanceState.containsKey(NEWFORM)) {
                newForm = savedInstanceState.getBoolean(NEWFORM, true);
            }
            if (savedInstanceState.containsKey(KEY_ERROR)) {
                errorMessage = savedInstanceState.getString(KEY_ERROR);
            }
            saveName = savedInstanceState.getString(KEY_SAVE_NAME);
            if (savedInstanceState.containsKey(KEY_AUTO_SAVED)) {
                autoSaved = savedInstanceState.getBoolean(KEY_AUTO_SAVED);
            }
        }

    }

    private void loadForm() {
        allowMovingBackwards = (boolean) AdminSharedPreferences.getInstance().get(KEY_MOVING_BACKWARDS);

        // If a parse error message is showing then nothing else is loaded
        // Dialogs mid form just disappear on rotation.
        if (errorMessage != null) {
            createErrorDialog(errorMessage, EXIT);
            return;
        }

        // Check to see if this is a screen flip or a new form load.
        Object data = getLastCustomNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            formLoaderTask = (FormLoaderTask) data;
        } else if (data instanceof SaveToDiskTask) {
            saveToDiskTask = (SaveToDiskTask) data;
        } else if (data == null) {
            if (!newForm) {
                if (getFormController() != null) {
                    refreshCurrentView();
                } else {
                    Timber.w("Reloading form and restoring state.");
                    // we need to launch the form loader to load the form
                    // controller...
                    formLoaderTask = new FormLoaderTask(instancePath,
                            startingXPath, waitingXPath);
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "formReloaded", formPath);
                    // TODO: this doesn' work (dialog does not get removed):
                    // showDialog(PROGRESS_DIALOG);
                    // show dialog before we execute...
                    formLoaderTask.execute(formPath);
                }
                return;
            }

            // Not a restart from a screen orientation change (or other).
            Collect.getInstance().setFormController(null);
            supportInvalidateOptionsMenu();
            Intent intent = getIntent();
            if (intent != null) {
                loadFromIntent(intent);
            }
        }
    }

    private void loadFromIntent(Intent intent) {
        Uri uri = intent.getData();
        String uriMimeType = null;

        if (uri != null) {
            uriMimeType = getContentResolver().getType(uri);
        }

        if (uriMimeType == null && intent.hasExtra(EXTRA_TESTING_PATH)) {
            formPath = intent.getStringExtra(EXTRA_TESTING_PATH);

        } else if (uriMimeType != null && uriMimeType.equals(InstanceColumns.CONTENT_ITEM_TYPE)) {
            // get the formId and version for this instance...

            FormInfo formInfo = ContentResolverHelper.getFormDetails(uri);

            if (formInfo == null) {
                createErrorDialog(getString(R.string.bad_uri, uri), EXIT);
                return;
            }

            instancePath = formInfo.getInstancePath();
            Collect.getInstance()
                    .getActivityLogger()
                    .logAction(this, "instanceLoaded",
                            instancePath);

            String jrFormId = formInfo.getFormID();
            String jrVersion = formInfo.getFormVersion();

            String[] selectionArgs;
            String selection;
            if (jrVersion == null) {
                selectionArgs = new String[]{jrFormId};
                selection = FormsColumns.JR_FORM_ID + "=? AND "
                        + FormsColumns.JR_VERSION + " IS NULL";
            } else {
                selectionArgs = new String[]{jrFormId, jrVersion};
                selection = FormsColumns.JR_FORM_ID + "=? AND "
                        + FormsColumns.JR_VERSION + "=?";
            }

            int formCount = FormsDaoHelper.getFormsCount(selection, selectionArgs);
            if (formCount < 1) {
                createErrorDialog(getString(
                        R.string.parent_form_not_present,
                        jrFormId)
                                + ((jrVersion == null) ? ""
                                : "\n"
                                + getString(R.string.version)
                                + " "
                                + jrVersion),
                        EXIT);
                return;
            } else {
                formPath = FormsDaoHelper.getFormPath(selection, selectionArgs);

                /**
                 * Still take the first entry, but warn that there are multiple rows. User will
                 * need to hand-edit the SQLite database to fix it.
                 */
                if (formCount > 1) {
                    createErrorDialog(getString(R.string.survey_multiple_forms_error), EXIT);
                    return;
                }
            }
        } else if (uriMimeType != null
                && uriMimeType.equals(FormsColumns.CONTENT_ITEM_TYPE)) {
            formPath = ContentResolverHelper.getFormPath(uri);
            if (formPath == null) {
                createErrorDialog(getString(R.string.bad_uri, uri), EXIT);
                return;
            } else {
                /**
                 * This is the fill-blank-form code path.See if there is a savepoint for this form
                 * that has never been explicitly saved by the user. If there is, open this savepoint(resume this filled-in form).
                 * Savepoints for forms that were explicitly saved will be recovered when that
                 * explicitly saved instance is edited via edit-saved-form.
                 */
                final String filePrefix = formPath.substring(
                        formPath.lastIndexOf('/') + 1,
                        formPath.lastIndexOf('.'))
                        + "_";
                final String fileSuffix = ".xml.save";
                File cacheDir = new File(Collect.CACHE_PATH);
                File[] files = cacheDir.listFiles(pathname -> {
                    String name = pathname.getName();
                    return name.startsWith(filePrefix)
                            && name.endsWith(fileSuffix);
                });

                /**
                 * See if any of these savepoints are for a filled-in form that has never
                 * been explicitly saved by the user.
                 */
                for (File candidate : files) {
                    String instanceDirName = candidate.getName()
                            .substring(
                                    0,
                                    candidate.getName().length()
                                            - fileSuffix.length());
                    File instanceDir = new File(
                            Collect.INSTANCES_PATH + File.separator
                                    + instanceDirName);
                    File instanceFile = new File(instanceDir,
                            instanceDirName + ".xml");
                    if (instanceDir.exists()
                            && instanceDir.isDirectory()
                            && !instanceFile.exists()) {
                        // yes! -- use this savepoint file
                        instancePath = instanceFile
                                .getAbsolutePath();
                        break;
                    }
                }
            }
        } else {
            Timber.e("Unrecognized URI: %s", uri);
            createErrorDialog(getString(R.string.unrecognized_uri, uri), EXIT);
            return;
        }

        formLoaderTask = new FormLoaderTask(instancePath, null, null);
        Collect.getInstance().getActivityLogger()
                .logAction(this, "formLoaded", formPath);
        showDialog(PROGRESS_DIALOG);
        // show dialog before we execute...
        formLoaderTask.execute(formPath);
    }


    public Bundle getState() {
        return state;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.loading_form));
    }

    /**
     * Create save-points asynchronously in order to not affect swiping performance
     * on larger forms.
     */
    private void nonblockingCreateSavePointData() {
        try {
            SavePointTask savePointTask = new SavePointTask(this);
            savePointTask.execute();

            if (!allowMovingBackwards) {
                FormController formController = getFormController();
                if (formController != null) {
                    new SaveFormIndexTask(this, formController.getFormIndex()).execute();
                }
            }
        } catch (Exception e) {
            Timber.e("Could not schedule SavePointTask. Perhaps a lot of swiping is taking place?");
        }
    }

    private FormController getFormController() {
        return Collect.getInstance().getFormController();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FORMPATH, formPath);
        FormController formController = getFormController();
        if (formController != null) {
            if (formController.getInstanceFile() != null) {
                outState.putString(KEY_INSTANCEPATH, getAbsoluteInstancePath());
            }
            outState.putString(KEY_XPATH,
                    formController.getXPath(formController.getFormIndex()));
            FormIndex waiting = formController.getIndexWaitingForData();
            if (waiting != null) {
                outState.putString(KEY_XPATH_WAITING_FOR_DATA,
                        formController.getXPath(waiting));
            }
            // save the instance to a temp path...
            nonblockingCreateSavePointData();
        }
        outState.putBoolean(NEWFORM, false);
        outState.putString(KEY_ERROR, errorMessage);
        outState.putString(KEY_SAVE_NAME, saveName);
        outState.putBoolean(KEY_AUTO_SAVED, autoSaved);

        if (currentView instanceof ODKView) {
            outState.putAll(((ODKView) currentView).getState());
            // This value is originally set in onCreate() method but if you only minimize the app or
            // block/unblock the screen, onCreate() method might not be called (if the activity is just paused
            // not stopped https://developer.android.com/guide/components/activities/activity-lifecycle.html)
            state = outState;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        FormController formController = getFormController();
        if (formController == null) {
            // we must be in the midst of a reload of the FormController.
            // try to save this callback data to the FormLoaderTask
            if (formLoaderTask != null
                    && formLoaderTask.getStatus() != AsyncTask.Status.FINISHED) {
                formLoaderTask.setActivityResult(requestCode, resultCode, intent);
            } else {
                Timber.e("Got an activityResult without any pending form loader");
            }
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            // request was canceled...
            if (requestCode != RequestCodes.HIERARCHY_ACTIVITY && getCurrentViewIfODKView() != null) {
                getCurrentViewIfODKView().cancelWaitingForBinaryData();
            }
            return;
        }

        // intent is needed for all requestCodes except of DRAW_IMAGE, ANNOTATE_IMAGE, SIGNATURE_CAPTURE, IMAGE_CAPTURE and HIERARCHY_ACTIVITY
        if (intent == null && requestCode != RequestCodes.DRAW_IMAGE && requestCode != RequestCodes.ANNOTATE_IMAGE
                && requestCode != RequestCodes.SIGNATURE_CAPTURE && requestCode != RequestCodes.IMAGE_CAPTURE
                && requestCode != RequestCodes.HIERARCHY_ACTIVITY) {
            Timber.w("The intent has a null value for requestCode: " + requestCode);
            ToastUtils.showLongToast(getString(R.string.null_intent_value));
            return;
        }

        // For handling results returned by the Zxing Barcode scanning library
        IntentResult barcodeScannerResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (barcodeScannerResult != null) {
            if (barcodeScannerResult.getContents() == null) {
                // request was canceled...
                Timber.i("QR code scanning cancelled");
            } else {
                String sb = intent.getStringExtra("SCAN_RESULT");
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(sb);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                refreshCurrentView();
                return;
            }
        }


        switch (requestCode) {

            case RequestCodes.OSM_CAPTURE:
                String osmFileName = intent.getStringExtra("OSM_FILE_NAME");
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(osmFileName);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.EX_STRING_CAPTURE:
            case RequestCodes.EX_INT_CAPTURE:
            case RequestCodes.EX_DECIMAL_CAPTURE:
                String key = "value";
                boolean exists = intent.getExtras().containsKey(key);
                if (exists) {
                    Object externalValue = intent.getExtras().get(key);
                    if (getCurrentViewIfODKView() != null) {
                        getCurrentViewIfODKView().setBinaryData(externalValue);
                    }
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                }
                break;
            case RequestCodes.EX_GROUP_CAPTURE:
                try {
                    Bundle extras = intent.getExtras();
                    if (getCurrentViewIfODKView() != null) {
                        getCurrentViewIfODKView().setDataForFields(extras);
                    }
                } catch (JavaRosaException e) {
                    Timber.e(e);
                    createErrorDialog(e.getCause().getMessage(), DO_NOT_EXIT);
                }
                break;
            case RequestCodes.DRAW_IMAGE:
            case RequestCodes.ANNOTATE_IMAGE:
            case RequestCodes.SIGNATURE_CAPTURE:
            case RequestCodes.IMAGE_CAPTURE:
                /*
                 * We saved the image to the tempfile_path, but we really want it to
                 * be in: /sdcard/odk/instances/[current instnace]/something.jpg so
                 * we move it there before inserting it into the content provider.
                 * Once the android image capture bug gets fixed, (read, we move on
                 * from Android 1.6) we want to handle images the audio and video
                 */
                // The intent is empty, but we know we saved the image to the temp
                // file
                ImageConverter.execute(Collect.TMPFILE_PATH, getWidgetWaitingForBinaryData(), this);
                File fi = new File(Collect.TMPFILE_PATH);

                //revoke permissions granted to this file due its possible usage in
                //the camera app
                Uri uri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        fi);
                FileUtils.revokeFileReadWritePermission(this, uri);

                String instanceFolder = formController.getInstanceFile()
                        .getParent();
                String s = instanceFolder + File.separator + System.currentTimeMillis() + ".jpg";

                File nf = new File(s);
                if (!fi.renameTo(nf)) {
                    Timber.e("Failed to rename %s", fi.getAbsolutePath());
                } else {
                    Timber.i("Renamed %s to %s", fi.getAbsolutePath(), nf.getAbsolutePath());
                }

                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(nf);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.ALIGNED_IMAGE:
                /*
                 * We saved the image to the tempfile_path; the app returns the full
                 * path to the saved file in the EXTRA_OUTPUT extra. Take that file
                 * and move it into the instance folder.
                 */
                String path = intent
                        .getStringExtra(android.provider.MediaStore.EXTRA_OUTPUT);
                fi = new File(path);
                instanceFolder = formController.getInstanceFile().getParent();
                s = instanceFolder + File.separator + System.currentTimeMillis() + ".jpg";

                nf = new File(s);
                if (!fi.renameTo(nf)) {
                    Timber.e("Failed to rename %s", fi.getAbsolutePath());
                } else {
                    Timber.i("Renamed %s to %s", fi.getAbsolutePath(), nf.getAbsolutePath());
                }

                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(nf);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.IMAGE_CHOOSER:
                /*
                 * We have a saved image somewhere, but we really want it to be in:
                 * /sdcard/odk/instances/[current instnace]/something.jpg so we move
                 * it there before inserting it into the content provider. Once the
                 * android image capture bug gets fixed, (read, we move on from
                 * Android 1.6) we want to handle images the audio and video
                 */

                showDialog(SAVING_IMAGE_DIALOG);
                Runnable runnable = () -> saveChosenImage(intent.getData());
                new Thread(runnable).start();

                break;
            case RequestCodes.AUDIO_CAPTURE:
            case RequestCodes.VIDEO_CAPTURE:
                Uri mediaUri = intent.getData();
                saveFileAnswer(mediaUri);
                String filePath = MediaUtils.getDataColumn(this, mediaUri, null, null);
                if (filePath != null) {
                    new File(filePath).delete();
                }
                try {
                    getContentResolver().delete(mediaUri, null, null);
                } catch (Exception e) {
                    Timber.e(e);
                }
                break;

            case RequestCodes.ARBITRARY_FILE_CHOOSER:
                // Same with VIDEO_CHOOSER.
            case RequestCodes.AUDIO_CHOOSER:
                // Same with VIDEO_CHOOSER.
            case RequestCodes.VIDEO_CHOOSER:
                /*
                 * Start a task to save the chosen file/audio/video with a new Thread,
                 * This could support retrieving file from Google Drive.
                 * */
                showDialog(SAVING_DIALOG);
                Runnable saveFileRunnable = new Runnable() {
                    @Override
                    public void run() {
                        saveChosenFile(intent.getData());
                    }
                };
                new Thread(saveFileRunnable).start();
                break;
            case RequestCodes.LOCATION_CAPTURE:
                String sl = intent.getStringExtra(LOCATION_RESULT);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(sl);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.GEOSHAPE_CAPTURE:
                String gshr = intent.getStringExtra(GEOSHAPE_RESULTS);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(gshr);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.GEOTRACE_CAPTURE:
                String traceExtra = intent.getStringExtra(GEOTRACE_RESULTS);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(traceExtra);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.BEARING_CAPTURE:
                String bearing = intent.getStringExtra(BEARING_RESULT);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(bearing);
                }
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case RequestCodes.HIERARCHY_ACTIVITY:
                // We may have jumped to a new index in hierarchy activity, so
                // refresh
                break;

        }
        refreshCurrentView();
    }

    /**
     * Save a copy of the chosen media in Collect's own path such as
     * "/storage/emulated/0/odk/instances/{form name}/filename",
     * and if it's from Google Drive and not cached yet, we'll retrieve it using network.
     * This may take a long time.
     *
     * @param selectedFile uri of the selected audio
     * @see #getFileExtensionFromUri(Uri)
     */
    private void saveChosenFile(Uri selectedFile) {
        String extension = getFileExtensionFromUri(selectedFile);

        String instanceFolder = Collect.getInstance().getFormController()
                .getInstanceFile().getParent();
        String destPath = instanceFolder + File.separator
                + System.currentTimeMillis() + extension;

        File chosenFile;
        try {
            chosenFile = MediaUtils.getFileFromUri(this, selectedFile, Images.Media.DATA);
            if (chosenFile != null) {
                final File newFile = new File(destPath);
                FileUtils.copyFile(chosenFile, newFile);
                runOnUiThread(() -> {
                    dismissDialog(SAVING_DIALOG);
                    if (getCurrentViewIfODKView() != null) {
                        getCurrentViewIfODKView().setBinaryData(newFile);
                    }
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                    refreshCurrentView();
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog(SAVING_DIALOG);
                        Timber.e("Could not receive chosen file");
                        ToastUtils.showShortToastInMiddle(R.string.error_occured);
                    }
                });
            }
        } catch (GDriveConnectionException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog(SAVING_DIALOG);
                    Timber.e("Could not receive chosen file due to connection problem");
                    ToastUtils.showLongToastInMiddle(R.string.gdrive_connection_exception);
                }
            });
        }
    }


    private void saveChosenImage(Uri selectedImage) {
        // Copy file to sdcard
        File instanceFile = getFormController().getInstanceFile();
        if (instanceFile != null) {
            String instanceFolder1 = instanceFile.getParent();
            String destImagePath = instanceFolder1 + File.separator + System.currentTimeMillis() + ".jpg";

            File chosenImage;
            try {
                chosenImage = MediaUtils.getFileFromUri(this, selectedImage, Images.Media.DATA);
                if (chosenImage != null) {
                    final File newImage = new File(destImagePath);
                    FileUtils.copyFile(chosenImage, newImage);
                    ImageConverter.execute(newImage.getPath(), getWidgetWaitingForBinaryData(), this);
                    runOnUiThread(() -> {
                        dismissDialog(SAVING_IMAGE_DIALOG);
                        if (getCurrentViewIfODKView() != null) {
                            getCurrentViewIfODKView().setBinaryData(newImage);
                        }
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                        refreshCurrentView();
                    });
                } else {
                    runOnUiThread(() -> {
                        dismissDialog(SAVING_IMAGE_DIALOG);
                        Timber.e("Could not receive chosen image");
                        ToastUtils.showShortToastInMiddle(R.string.error_occured);
                    });
                }
            } catch (GDriveConnectionException e) {
                runOnUiThread(() -> {
                    dismissDialog(SAVING_IMAGE_DIALOG);
                    Timber.e("Could not receive chosen image due to connection problem");
                    ToastUtils.showLongToastInMiddle(R.string.gdrive_connection_exception);
                });
            }
        } else {
            ToastUtils.showLongToast(R.string.image_not_saved);
            Timber.w(getString(R.string.image_not_saved));
        }
    }


    /**
     * Using contentResolver to get a file's extension by the uri returned from OnActivityResult.
     *
     * @param fileUri Whose name we want to get
     * @return The file's extension
     * @see #onActivityResult(int, int, Intent)
     * @see #saveChosenFile(Uri)
     * @see android.content.ContentResolver
     */
    private String getFileExtensionFromUri(Uri fileUri) {
        Cursor returnCursor =
                getContentResolver().query(fileUri, null, null, null, null);
        try {
            if (returnCursor == null) {
                return "";
            }
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String filename = returnCursor.getString(nameIndex);
            // If the file's name contains extension , we cut it down for latter use (copy a new file).
            if (filename.lastIndexOf('.') != -1) {
                return filename.substring(filename.lastIndexOf('.'));
            } else {
                // I found some mp3 files' name don't contain extension, but can be played as Audio
                // So I write so, but I still there are some way to get its extension
                return "";
            }
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }
    }

    private QuestionWidget getWidgetWaitingForBinaryData() {
        QuestionWidget questionWidget = null;
        for (QuestionWidget qw : ((ODKView) currentView).getWidgets()) {
            if (qw.isWaitingForData()) {
                questionWidget = qw;
            }
        }

        return questionWidget;
    }

    private void saveFileAnswer(Uri media) {
        // For audio/video capture/chooser, we get the URI from the content
        // provider
        // then the widget copies the file and makes a new entry in the
        // content provider.
        if (getCurrentViewIfODKView() != null) {
            getCurrentViewIfODKView().setBinaryData(media);
        }
        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
    }

    /**
     * Refreshes the current view. the controller and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     */
    public void refreshCurrentView() {
        int event = getFormController().getEvent();

        // When we refresh, repeat dialog state isn't maintained, so step back
        // to the previous
        // question.
        // Also, if we're within a group labeled 'field list', step back to the
        // beginning of that
        // group.
        // That is, skip backwards over repeat prompts, groups that are not
        // field-lists,
        // repeat events, and indexes in field-lists that is not the containing
        // group.

        View current = createView(event, false);
        showView(current, AnimationType.FADE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "onCreateOptionsMenu", "show");
        getMenuInflater().inflate(R.menu.form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean useability;

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        FormController formController = getFormController();

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = (boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FormController formController = getFormController();
        switch (item.getItemId()) {
            case R.id.menu_languages:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onOptionsItemSelected",
                                "MENU_LANGUAGES");
                createLanguageDialog();
                return true;
            case R.id.menu_save:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onOptionsItemSelected",
                                "MENU_SAVE");
                // don't exit
                saveDataToDisk(DO_NOT_EXIT, InstancesDaoHelper.isInstanceComplete(false), null);
                return true;
            case R.id.menu_goto:
                state = null;
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onOptionsItemSelected",
                                "MENU_HIERARCHY_VIEW");
                if (formController != null && formController.currentPromptIsQuestion()) {
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                }

                if (formController != null) {
                    formController.getTimerLogger().exitView();
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.HIERARCHY,
                            0, null, false, true);
                }

                Intent i = new Intent(this, EditFormHierarchyActivity.class);
                startActivityForResult(i, RequestCodes.HIERARCHY_ACTIVITY);
                return true;
            case R.id.menu_preferences:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onOptionsItemSelected",
                                "MENU_PREFERENCES");
                Intent pref = new Intent(this, PreferencesActivity.class);
                startActivity(pref);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempt to save the answer(s) in the current screen to into the data
     * model.
     *
     * @return false if any error occurs while saving (constraint violated,
     * etc...), true otherwise.
     */
    private boolean saveAnswersForCurrentScreen(boolean evaluateConstraints) {
        FormController formController = getFormController();
        // only try to save if the current event is a question or a field-list group
        // and current view is an ODKView (occasionally we show blank views that do not have any
        // controls to save data from)
        if (formController != null && formController.currentPromptIsQuestion()
                && getCurrentViewIfODKView() != null) {
            HashMap<FormIndex, IAnswerData> answers = getCurrentViewIfODKView()
                    .getAnswers();
            try {
                FailedConstraint constraint = formController.saveAllScreenAnswers(answers,
                        evaluateConstraints);
                if (constraint != null) {
                    createConstraintToast(constraint.index, constraint.status);
                    if (formController.indexIsInFieldList() && formController.getQuestionPrompts().length > 1) {
                        getCurrentViewIfODKView().highlightWidget(constraint.index);
                    }
                    return false;
                }
            } catch (JavaRosaException e) {
                Timber.e(e);
                createErrorDialog(e.getCause().getMessage(), DO_NOT_EXIT);
                return false;
            }
        }
        return true;
    }

    /**
     * Clears the answer on the screen.
     */
    private void clearAnswer(QuestionWidget qw) {
        if (qw.getAnswer() != null || qw instanceof DateTimeWidget) {
            qw.clearAnswer();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "onCreateContextMenu", "show");
        FormController formController = getFormController();

        menu.add(0, v.getId(), 0, getString(R.string.clear_answer));
        if (formController.indexContainsRepeatableGroup()) {
            menu.add(0, DELETE_REPEAT, 0, getString(R.string.delete_repeat));
        }
        menu.setHeaderTitle(getString(R.string.edit_prompt));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == DELETE_REPEAT) {
            Collect.getInstance()
                    .getActivityLogger()
                    .logInstanceAction(this, "onContextItemSelected",
                            "createDeleteRepeatConfirmDialog");
            createDeleteRepeatConfirmDialog();
        } else {
            /*
             * We don't have the right view here, so we store the View's ID as the
             * item ID and loop through the possible views to find the one the user
             * clicked on.
             */
            boolean shouldClearDialogBeShown;
            for (QuestionWidget qw : getCurrentViewIfODKView().getWidgets()) {
                shouldClearDialogBeShown = false;
                if (qw instanceof StringWidget) {
                    for (int i = 0; i < qw.getChildCount(); i++) {
                        if (item.getItemId() == qw.getChildAt(i).getId()) {
                            shouldClearDialogBeShown = true;
                            break;
                        }
                    }
                } else if (item.getItemId() == qw.getId()) {
                    shouldClearDialogBeShown = true;
                }

                if (shouldClearDialogBeShown) {
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this, "onContextItemSelected",
                                    "createClearDialog", qw.getFormEntryPrompt().getIndex());
                    createClearDialog(qw);
                    break;
                }
            }
        }

        return super.onContextItemSelected(item);
    }

    /**
     * If we're loading, then we pass the loading thread to our next instance.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        FormController formController = getFormController();
        // if a form is loading, pass the loader task
        if (formLoaderTask != null
                && formLoaderTask.getStatus() != AsyncTask.Status.FINISHED) {
            return formLoaderTask;
        }

        // if a form is writing to disk, pass the save to disk task
        if (saveToDiskTask != null
                && saveToDiskTask.getStatus() != AsyncTask.Status.FINISHED) {
            return saveToDiskTask;
        }

        // mFormEntryController is static so we don't need to pass it.
        if (formController != null && formController.currentPromptIsQuestion()) {
            saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
        }
        return null;
    }

    /**
     * Creates a view given the View type and an event
     *
     * @param advancingPage -- true if this results from advancing through the form
     * @return newly created View
     */
    private View createView(int event, boolean advancingPage) {
        FormController formController = getFormController();

        setTitle(formController.getFormTitle());

        formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FEC,
                event, formController.getFormIndex().getReference(), advancingPage, true);

        switch (event) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
                return createViewForFormBeginning(event, true, formController);

            case FormEntryController.EVENT_END_OF_FORM:
                View endView = View.inflate(this, R.layout.form_entry_end, null);
                ((TextView) endView.findViewById(R.id.description))
                        .setText(getString(R.string.save_enter_data_description,
                                formController.getFormTitle()));

                // checkbox for if finished or ready to send
                final CheckBox instanceComplete = endView
                        .findViewById(R.id.mark_finished);
                instanceComplete.setChecked(InstancesDaoHelper.isInstanceComplete(true));

                if (!(boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_MARK_AS_FINALIZED)) {
                    instanceComplete.setVisibility(View.GONE);
                }

                // edittext to change the displayed name of the instance
                final EditText saveAs = endView.findViewById(R.id.save_name);

                // disallow carriage returns in the name
                InputFilter returnFilter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                                return "";
                            }
                        }
                        return null;
                    }
                };
                saveAs.setFilters(new InputFilter[]{returnFilter});

                if (formController.getSubmissionMetadata().instanceName == null) {
                    // no meta/instanceName field in the form -- see if we have a
                    // name for this instance from a previous save attempt...
                    String uriMimeType = null;
                    Uri instanceUri = getIntent().getData();
                    if (instanceUri != null) {
                        uriMimeType = getContentResolver().getType(instanceUri);
                    }

                    if (saveName == null && uriMimeType != null
                            && uriMimeType.equals(InstanceColumns.CONTENT_ITEM_TYPE)) {
                        Cursor instance = null;
                        try {
                            instance = getContentResolver().query(instanceUri,
                                    null, null, null, null);
                            if (instance != null && instance.getCount() == 1) {
                                instance.moveToFirst();
                                saveName = instance
                                        .getString(instance
                                                .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                            }
                        } finally {
                            if (instance != null) {
                                instance.close();
                            }
                        }
                    }
                    if (saveName == null) {
                        // last resort, default to the form title
                        saveName = formController.getFormTitle();
                    }
                    // present the prompt to allow user to name the form
                    TextView sa = endView.findViewById(R.id.save_form_as);
                    sa.setVisibility(View.VISIBLE);
                    saveAs.setText(saveName);
                    saveAs.setEnabled(true);
                    saveAs.setVisibility(View.VISIBLE);
                    saveAs.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            saveName = String.valueOf(s);
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });
                } else {
                    // if instanceName is defined in form, this is the name -- no
                    // revisions
                    // display only the name, not the prompt, and disable edits
                    saveName = formController.getSubmissionMetadata().instanceName;
                    TextView sa = endView.findViewById(R.id.save_form_as);
                    sa.setVisibility(View.GONE);
                    saveAs.setText(saveName);
                    saveAs.setEnabled(false);
                    saveAs.setVisibility(View.VISIBLE);
                }

                // override the visibility settings based upon admin preferences
                if (!(boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_AS)) {
                    saveAs.setVisibility(View.GONE);
                    TextView sa = endView
                            .findViewById(R.id.save_form_as);
                    sa.setVisibility(View.GONE);
                }

                // Create 'save' button
                endView.findViewById(R.id.save_exit_button)
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(
                                                this,
                                                "createView.saveAndExit",
                                                instanceComplete.isChecked() ? "saveAsComplete"
                                                        : "saveIncomplete");
                                // Form is marked as 'saved' here.
                                if (saveAs.getText().length() < 1) {
                                    ToastUtils.showShortToast(R.string.save_as_error);
                                } else {
                                    saveDataToDisk(EXIT, instanceComplete
                                            .isChecked(), saveAs.getText()
                                            .toString());
                                }
                            }
                        });

                if (showNavigationButtons) {
                    backButton.setEnabled(allowMovingBackwards);
                    nextButton.setEnabled(false);
                }

                return endView;
            case FormEntryController.EVENT_QUESTION:
            case FormEntryController.EVENT_GROUP:
            case FormEntryController.EVENT_REPEAT:
                releaseOdkView();
                // should only be a group here if the event_group is a field-list
                try {
                    FormEntryPrompt[] prompts = formController.getQuestionPrompts();
                    FormEntryCaption[] groups = formController
                            .getGroupsForCurrentIndex();
                    odkView = new ODKView(this, prompts, groups, advancingPage);
                    Timber.i("Created view for group %s %s",
                            (groups.length > 0 ? groups[groups.length - 1].getLongText() : "[top]"),
                            (prompts.length > 0 ? prompts[0].getQuestionText() : "[no question]"));
                } catch (RuntimeException e) {
                    Timber.e(e);
                    // this is badness to avoid a crash.
                    try {
                        event = formController.stepToNextScreenEvent();
                        createErrorDialog(e.getMessage(), DO_NOT_EXIT);
                    } catch (JavaRosaException e1) {
                        Timber.d(e1);
                        createErrorDialog(e.getMessage() + "\n\n" + e1.getCause().getMessage(),
                                DO_NOT_EXIT);
                    }
                    return createView(event, advancingPage);
                }

                // Makes a "clear answer" menu pop up on long-click
                for (QuestionWidget qw : odkView.getWidgets()) {
                    if (!qw.getFormEntryPrompt().isReadOnly()) {
                        // If it's a StringWidget register all its elements apart from EditText as
                        // we want to enable paste option after long click on the EditText
                        if (qw instanceof StringWidget) {
                            for (int i = 0; i < qw.getChildCount(); i++) {
                                if (!(qw.getChildAt(i) instanceof EditText)) {
                                    registerForContextMenu(qw.getChildAt(i));
                                }
                            }
                        } else {
                            registerForContextMenu(qw);
                        }
                    }
                }

                if (showNavigationButtons) {
                    backButton.setEnabled(!formController.isCurrentQuestionFirstInForm() && allowMovingBackwards);
                    nextButton.setEnabled(true);
                }
                return odkView;

            case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                createRepeatDialog();
                return new EmptyView(this);

            default:
                Timber.e("Attempted to create a view that does not exist.");
                // this is badness to avoid a crash.
                try {
                    event = formController.stepToNextScreenEvent();
                    createErrorDialog(getString(R.string.survey_internal_error), EXIT);
                } catch (JavaRosaException e) {
                    Timber.d(e);
                    createErrorDialog(e.getCause().getMessage(), EXIT);
                }
                return createView(event, advancingPage);
        }
    }

    private void releaseOdkView() {
        if (odkView != null) {
            odkView.releaseWidgetResources();
            odkView = null;
        }
    }

    private View createViewForFormBeginning(int event, boolean advancingPage,
                                            FormController formController) {
        try {
            event = formController.stepToNextScreenEvent();

        } catch (JavaRosaException e) {
            Timber.d(e);
            if (e.getMessage().equals(e.getCause().getMessage())) {
                createErrorDialog(e.getMessage(), DO_NOT_EXIT);
            } else {
                createErrorDialog(e.getMessage() + "\n\n" + e.getCause().getMessage(), DO_NOT_EXIT);
            }
        }

        return createView(event, advancingPage);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent mv) {
        boolean handled = gestureDetector.onTouchEvent(mv);
        if (!handled) {
            return super.dispatchTouchEvent(mv);
        }

        return handled; // this is always true
    }

    /**
     * Determines what should be displayed on the screen. Possible options are:
     * a question, an ask repeat dialog, or the submit screen. Also saves
     * answers to the data model after checking constraints.
     */
    private void showNextView() {
        state = null;
        try {
            FormController formController = getFormController();

            // get constraint behavior preference value with appropriate default
            String constraintBehavior = (String) GeneralSharedPreferences.getInstance()
                    .get(PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR);

            if (formController != null && formController.currentPromptIsQuestion()) {
                // if constraint behavior says we should validate on swipe, do so
                if (constraintBehavior.equals(PreferenceKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE)) {
                    if (!saveAnswersForCurrentScreen(EVALUATE_CONSTRAINTS)) {
                        // A constraint was violated so a dialog should be showing.
                        beenSwiped = false;
                        return;
                    }

                    // otherwise, just save without validating (constraints will be validated on
                    // finalize)
                } else {
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                }
            }

            View next;

            int originalEvent = formController.getEvent();
            int event = formController.stepToNextScreenEvent();

            // Helps prevent transition animation at the end of the form (if user swipes left
            // she will stay on the same screen)
            if (originalEvent == event && originalEvent == FormEntryController.EVENT_END_OF_FORM) {
                beenSwiped = false;
                return;
            }

            formController.getTimerLogger().exitView();    // Close timer events waiting for an end time

            switch (event) {
                case FormEntryController.EVENT_QUESTION:
                case FormEntryController.EVENT_GROUP:
                    // create a savepoint
                    if ((++viewCount) % SAVEPOINT_INTERVAL == 0) {
                        nonblockingCreateSavePointData();
                    }
                    next = createView(event, true);
                    showView(next, AnimationType.RIGHT);
                    break;
                case FormEntryController.EVENT_END_OF_FORM:
                case FormEntryController.EVENT_REPEAT:
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    next = createView(event, true);
                    showView(next, AnimationType.RIGHT);
                    break;
                case FormEntryController.EVENT_REPEAT_JUNCTURE:
                    Timber.i("Repeat juncture: %s", formController.getFormIndex().getReference());
                    // skip repeat junctures until we implement them
                    break;
                default:
                    Timber.w("JavaRosa added a new EVENT type and didn't tell us... shame on them.");
                    break;
            }
        } catch (JavaRosaException e) {
            Timber.d(e);
            createErrorDialog(e.getCause().getMessage(), DO_NOT_EXIT);
        }
    }

    /**
     * Determines what should be displayed between a question, or the start
     * screen and displays the appropriate view. Also saves answers to the data
     * model without checking constraints.
     */
    private void showPreviousView() {
        if (allowMovingBackwards) {
            state = null;
            try {
                FormController formController = getFormController();
                if (formController != null) {
                    // The answer is saved on a back swipe, but question constraints are
                    // ignored.
                    if (formController.currentPromptIsQuestion()) {
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                    }

                    if (formController.getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
                        int event = formController.stepToPreviousScreenEvent();

                        // If we are the begining of the form, lets revert our actions and ignore
                        // this swipe
                        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                            event = formController.stepToNextScreenEvent();
                            beenSwiped = false;

                            if (event != FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                                // Returning here prevents the same view sliding when user is on the first screen
                                return;
                            }
                        }

                        if (event == FormEntryController.EVENT_GROUP
                                || event == FormEntryController.EVENT_QUESTION) {
                            // create savepoint
                            if ((++viewCount) % SAVEPOINT_INTERVAL == 0) {
                                nonblockingCreateSavePointData();
                            }
                        }
                        formController.getTimerLogger().exitView();    // Close timer events
                        View next = createView(event, false);
                        showView(next, AnimationType.LEFT);
                    } else {
                        beenSwiped = false;
                    }
                } else {
                    Timber.w("FormController has a null value");
                }
            } catch (JavaRosaException e) {
                Timber.d(e);
                createErrorDialog(e.getCause().getMessage(), DO_NOT_EXIT);
            }
        } else {
            beenSwiped = false;
        }
    }

    /**
     * Displays the View specified by the parameter 'next', animating both the
     * current view and next appropriately given the AnimationType. Also updates
     * the progress bar.
     */
    public void showView(View next, AnimationType from) {

        // disable notifications...
        if (inAnimation != null) {
            inAnimation.setAnimationListener(null);
        }
        if (outAnimation != null) {
            outAnimation.setAnimationListener(null);
        }

        // logging of the view being shown is already done, as this was handled
        // by createView()
        switch (from) {
            case RIGHT:
                inAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_left_in);
                outAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_left_out);
                // if animation is left or right then it was a swipe, and we want to re-save on
                // entry
                autoSaved = false;
                break;
            case LEFT:
                inAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_right_in);
                outAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_right_out);
                autoSaved = false;
                break;
            case FADE:
                inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                outAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                break;
        }

        // complete setup for animations...
        inAnimation.setAnimationListener(this);
        outAnimation.setAnimationListener(this);

        if (shouldOverrideAnimations) {
            inAnimation.setDuration(0);
            outAnimation.setDuration(0);
        }

        // drop keyboard before transition...
        if (currentView != null) {
            SoftKeyboardUtils.hideSoftKeyboard(currentView);
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        // adjust which view is in the layout container...
        staleView = currentView;
        currentView = next;
        questionHolder.addView(currentView, lp);
        animationCompletionSet = 0;

        if (staleView != null) {
            // start OutAnimation for transition...
            staleView.startAnimation(outAnimation);
            // and remove the old view (MUST occur after start of animation!!!)
            questionHolder.removeView(staleView);
        } else {
            animationCompletionSet = 2;
        }
        // start InAnimation for transition...
        currentView.startAnimation(inAnimation);

        String logString = "";
        switch (from) {
            case RIGHT:
                logString = "next";
                break;
            case LEFT:
                logString = "previous";
                break;
            case FADE:
                logString = "refresh";
                break;
        }

        Collect.getInstance().getActivityLogger().logInstanceAction(this, "showView", logString);

        FormController formController = getFormController();
        if (formController.getEvent() == FormEntryController.EVENT_QUESTION
                || formController.getEvent() == FormEntryController.EVENT_GROUP
                || formController.getEvent() == FormEntryController.EVENT_REPEAT) {
            FormEntryPrompt[] prompts = getFormController()
                    .getQuestionPrompts();
            for (FormEntryPrompt p : prompts) {
                List<TreeElement> attrs = p.getBindAttributes();
                for (int i = 0; i < attrs.size(); i++) {
                    if (!autoSaved && "saveIncomplete".equals(attrs.get(i).getName())) {
                        saveDataToDisk(false, false, null, false);
                        autoSaved = true;
                    }
                }
            }
        }
    }

    // Hopefully someday we can use managed dialogs when the bugs are fixed
    /*
     * Ideally, we'd like to use Android to manage dialogs with onCreateDialog()
     * and onPrepareDialog(), but dialogs with dynamic content are broken in 1.5
     * (cupcake). We do use managed dialogs for our static loading
     * ProgressDialog. The main issue we noticed and are waiting to see fixed
     * is: onPrepareDialog() is not called after a screen orientation change.
     * http://code.google.com/p/android/issues/detail?id=1639
     */

    //

    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    private void createConstraintToast(FormIndex index, int saveStatus) {
        FormController formController = getFormController();
        String constraintText;
        switch (saveStatus) {
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this,
                                "createConstraintToast.ANSWER_CONSTRAINT_VIOLATED",
                                "show", index);
                constraintText = formController
                        .getQuestionPromptConstraintText(index);
                if (constraintText == null) {
                    constraintText = formController.getQuestionPrompt(index)
                            .getSpecialFormQuestionText("constraintMsg");
                    if (constraintText == null) {
                        constraintText = getString(R.string.invalid_answer_error);
                    }
                }
                break;
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this,
                                "createConstraintToast.ANSWER_REQUIRED_BUT_EMPTY",
                                "show", index);
                constraintText = formController
                        .getQuestionPromptRequiredText(index);
                if (constraintText == null) {
                    constraintText = formController.getQuestionPrompt(index)
                            .getSpecialFormQuestionText("requiredMsg");
                    if (constraintText == null) {
                        constraintText = getString(R.string.required_answer_error);
                    }
                }
                break;
            default:
                return;
        }

        ToastUtils.showShortToastInMiddle(constraintText);
    }

    /**
     * Creates and displays a dialog asking the user if they'd like to create a
     * repeat of the current group.
     */
    private void createRepeatDialog() {
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "createRepeatDialog", "show");

        // In some cases dialog might be present twice because refreshView() is being called
        // from onResume(). This ensures that we do not preset this modal dialog if it's already
        // visible. Checking for shownAlertDialogIsGroupRepeat because the same field
        // alertDialog is being used for all alert dialogs in this activity.
        if (alertDialog != null && alertDialog.isShowing() && shownAlertDialogIsGroupRepeat) {
            return;
        }

        alertDialog = new AlertDialog.Builder(this).create();
        DialogInterface.OnClickListener repeatListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                shownAlertDialogIsGroupRepeat = false;
                FormController formController = getFormController();
                switch (i) {
                    case BUTTON_POSITIVE: // yes, repeat
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "createRepeatDialog",
                                        "addRepeat");
                        try {
                            formController.newRepeat();
                        } catch (Exception e) {
                            FormEntryActivity.this.createErrorDialog(
                                    e.getMessage(), DO_NOT_EXIT);
                            return;
                        }
                        if (!formController.indexIsInFieldList()) {
                            // we are at a REPEAT event that does not have a
                            // field-list appearance
                            // step to the next visible field...
                            // which could be the start of a new repeat group...
                            showNextView();
                        } else {
                            // we are at a REPEAT event that has a field-list
                            // appearance
                            // just display this REPEAT event's group.
                            refreshCurrentView();
                        }
                        break;
                    case BUTTON_NEGATIVE: // no, no repeat
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "createRepeatDialog",
                                        "showNext");

                        //
                        // Make sure the error dialog will not disappear.
                        //
                        // When showNextView() popups an error dialog (because of a
                        // JavaRosaException)
                        // the issue is that the "add new repeat dialog" is referenced by
                        // alertDialog
                        // like the error dialog. When the "no repeat" is clicked, the error dialog
                        // is shown. Android by default dismisses the dialogs when a button is
                        // clicked,
                        // so instead of closing the first dialog, it closes the second.
                        new Thread() {

                            @Override
                            public void run() {
                                FormEntryActivity.this.runOnUiThread(() -> {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        //This is rare
                                        Timber.e(e);
                                    }
                                    showNextView();
                                });
                            }
                        }.start();

                        break;
                }
            }
        };
        FormController formController = getFormController();
        if (formController.getLastRepeatCount() > 0) {
            alertDialog.setTitle(getString(R.string.leaving_repeat_ask));
            alertDialog.setMessage(getString(R.string.add_another_repeat,
                    formController.getLastGroupText()));
            alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.add_another),
                    repeatListener);
            alertDialog.setButton(BUTTON_NEGATIVE, getString(R.string.leave_repeat_yes),
                    repeatListener);

        } else {
            alertDialog.setTitle(getString(R.string.entering_repeat_ask));
            alertDialog.setMessage(getString(R.string.add_repeat,
                    formController.getLastGroupText()));
            alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.entering_repeat),
                    repeatListener);
            alertDialog.setButton(BUTTON_NEGATIVE, getString(R.string.add_repeat_no),
                    repeatListener);
        }
        alertDialog.setCancelable(false);
        beenSwiped = false;
        shownAlertDialogIsGroupRepeat = true;
        alertDialog.show();
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "createErrorDialog",
                        "show." + Boolean.toString(shouldExit));

        if (alertDialog != null && alertDialog.isShowing()) {
            errorMsg = errorMessage + "\n\n" + errorMsg;
            errorMessage = errorMsg;
        } else {
            alertDialog = new AlertDialog.Builder(this).create();
            errorMessage = errorMsg;
        }

        alertDialog.setTitle(getString(R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger()
                                .logInstanceAction(this, "createErrorDialog", "OK");
                        if (shouldExit) {
                            errorMessage = null;
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        beenSwiped = false;
        alertDialog.show();
    }

    /**
     * Creates a confirm/cancel dialog for deleting repeats.
     */
    private void createDeleteRepeatConfirmDialog() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "createDeleteRepeatConfirmDialog",
                        "show");
        FormController formController = getFormController();

        alertDialog = new AlertDialog.Builder(this).create();
        String name = formController.getLastRepeatedGroupName();
        int repeatcount = formController.getLastRepeatedGroupRepeatCount();
        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        alertDialog.setTitle(getString(R.string.delete_repeat_ask));
        alertDialog
                .setMessage(getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                FormController formController = getFormController();
                switch (i) {
                    case BUTTON_POSITIVE: // yes
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this,
                                        "createDeleteRepeatConfirmDialog", "OK");
                        formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.DELETE_REPEAT, 0, null, false, true);
                        formController.deleteRepeat();
                        showNextView();
                        break;

                    case BUTTON_NEGATIVE: // no
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this,
                                        "createDeleteRepeatConfirmDialog", "cancel");

                        refreshCurrentView();
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.discard_group), quitListener);
        alertDialog.setButton(BUTTON_NEGATIVE, getString(R.string.delete_repeat_no),
                quitListener);
        alertDialog.show();
    }

    /**
     * Saves data and writes it to disk. If exit is set, program will exit after
     * save completes. Complete indicates whether the user has marked the
     * isntancs as complete. If updatedSaveName is non-null, the instances
     * content provider is updated with the new name
     */
    // by default, save the current screen
    private boolean saveDataToDisk(boolean exit, boolean complete, String updatedSaveName) {
        return saveDataToDisk(exit, complete, updatedSaveName, true);
    }

    // but if you want save in the background, can't be current screen
    private boolean saveDataToDisk(boolean exit, boolean complete, String updatedSaveName,
                                   boolean current) {
        // save current answer
        if (current) {
            if (!saveAnswersForCurrentScreen(complete)) {
                ToastUtils.showShortToast(R.string.data_saved_error);
                return false;
            }
        }

        synchronized (saveDialogLock) {
            saveToDiskTask = new SaveToDiskTask(getIntent().getData(), exit, complete,
                    updatedSaveName);
            saveToDiskTask.setFormSavedListener(this);
            autoSaved = true;
            showDialog(SAVING_DIALOG);
            // show dialog before we execute...
            saveToDiskTask.execute();
        }

        return true;
    }

    /**
     * Create a dialog with options to save and exit or quit without
     * saving
     */
    private void createQuitDialog() {
        String title;
        {
            FormController formController = getFormController();
            title = (formController == null) ? null : formController.getFormTitle();
            if (title == null) {
                title = getString(R.string.no_form_loaded);
            }
        }

        List<IconMenuItem> items;
        if ((boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID)) {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_save, R.string.keep_changes),
                    new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        } else {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        }

        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "createQuitDialog", "show");

        ListView listView = DialogUtils.createActionListView(this);

        final IconMenuListAdapter adapter = new IconMenuListAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IconMenuItem item = (IconMenuItem) adapter.getItem(position);
                if (item.getTextResId() == R.string.keep_changes) {
                    Collect.getInstance().getActivityLogger()
                            .logInstanceAction(this, "createQuitDialog", "saveAndExit");
                    saveDataToDisk(EXIT, InstancesDaoHelper.isInstanceComplete(false),
                            null);
                } else {
                    Collect.getInstance().getActivityLogger()
                            .logInstanceAction(this, "createQuitDialog", "discardAndExit");

                    // close all open databases of external data.
                    ExternalDataManager manager = Collect.getInstance().getExternalDataManager();
                    if (manager != null) {
                        manager.close();
                    }

                    FormController formController = getFormController();
                    if (formController != null) {
                        formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_EXIT, 0, null, false, true);
                    }
                    removeTempInstance();
                    MediaManager.INSTANCE.revertChanges();
                    finishReturnInstance();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = new AlertDialog.Builder(this)
                .setTitle(
                        getString(R.string.quit_application, title))
                .setPositiveButton(getString(R.string.do_not_exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Collect.getInstance().getActivityLogger()
                                        .logInstanceAction(this, "createQuitDialog", "cancel");
                                dialog.cancel();

                            }
                        })
                .setView(listView).create();
        alertDialog.show();
    }

    // Cleanup when user exits a form without saving
    private void removeTempInstance() {
        FormController formController = getFormController();

        if (formController != null && formController.getInstanceFile() != null) {
            SaveToDiskTask.removeSavepointFiles(formController.getInstanceFile().getName());

            // if it's not already saved, erase everything
            if (!InstancesDaoHelper.isInstanceAvailable(getAbsoluteInstancePath())) {
                // delete media first
                String instanceFolder = formController.getInstanceFile().getParent();
                Timber.i("Attempting to delete: %s", instanceFolder);
                File file = formController.getInstanceFile().getParentFile();
                int images = MediaUtils.deleteImagesInFolderFromMediaProvider(file);
                int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(file);
                int video = MediaUtils.deleteVideoInFolderFromMediaProvider(file);

                Timber.i("Removed from content providers: %d image files, %d audio files and %d audio files.",
                        images, audio, video);
                FileUtils.purgeMediaPath(instanceFolder);
            }
        } else {
            Timber.w("null returned by getFormController()");
        }
    }

    private String getAbsoluteInstancePath() {
        return getFormController().getInstanceFile().getAbsolutePath();
    }

    /**
     * Confirm clear answer dialog
     */
    private void createClearDialog(final QuestionWidget qw) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "createClearDialog", "show",
                        qw.getFormEntryPrompt().getIndex());
        alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle(getString(R.string.clear_answer_ask));

        String question = qw.getFormEntryPrompt().getLongText();
        if (question == null) {
            question = "";
        }
        if (question.length() > 50) {
            question = question.substring(0, 50) + "...";
        }

        alertDialog.setMessage(getString(R.string.clearanswer_confirm,
                question));

        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case BUTTON_POSITIVE: // yes
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "createClearDialog",
                                        "clearAnswer", qw.getFormEntryPrompt().getIndex());
                        clearAnswer(qw);
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                        break;
                    case BUTTON_NEGATIVE: // no
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "createClearDialog",
                                        "cancel", qw.getFormEntryPrompt().getIndex());
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog
                .setButton(BUTTON_POSITIVE, getString(R.string.discard_answer), quitListener);
        alertDialog.setButton(BUTTON_NEGATIVE, getString(R.string.clear_answer_no),
                quitListener);
        alertDialog.show();
    }

    /**
     * Creates and displays a dialog allowing the user to set the language for
     * the form.
     */
    private void createLanguageDialog() {
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "createLanguageDialog", "show");
        FormController formController = getFormController();
        final String[] languages = formController.getLanguages();
        int selected = -1;
        if (languages != null) {
            String language = formController.getLanguage();
            for (int i = 0; i < languages.length; i++) {
                if (language.equals(languages[i])) {
                    selected = i;
                }
            }
        }
        alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(languages, selected,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Update the language in the content provider
                                // when selecting a new
                                // language
                                ContentValues values = new ContentValues();
                                values.put(FormsColumns.LANGUAGE,
                                        languages[whichButton]);
                                String selection = FormsColumns.FORM_FILE_PATH
                                        + "=?";
                                String[] selectArgs = {formPath};
                                int updated = new FormsDao().updateForm(values, selection, selectArgs);
                                Timber.i("Updated language to: %s in %d rows",
                                        languages[whichButton],
                                        updated);

                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(
                                                this,
                                                "createLanguageDialog",
                                                "changeLanguage."
                                                        + languages[whichButton]);
                                FormController formController = getFormController();
                                formController.setLanguage(languages[whichButton]);
                                dialog.dismiss();
                                if (formController.currentPromptIsQuestion()) {
                                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                                }
                                refreshCurrentView();
                            }
                        })
                .setTitle(getString(R.string.change_language))
                .setNegativeButton(getString(R.string.do_not_change),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(this,
                                                "createLanguageDialog",
                                                "cancel");
                            }
                        }).create();
        alertDialog.show();
    }

    /**
     * We use Android's dialog management for loading/saving progress dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onCreateDialog.PROGRESS_DIALOG",
                                "show");
                progressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(this,
                                                "onCreateDialog.PROGRESS_DIALOG", "cancel");
                                dialog.dismiss();

                                if (formLoaderTask != null) {
                                    formLoaderTask.setFormLoaderListener(null);
                                    FormLoaderTask t = formLoaderTask;
                                    formLoaderTask = null;
                                    t.cancel(true);
                                    t.destroy();
                                }
                                finish();
                            }
                        };
                progressDialog.setTitle(getString(R.string.loading_form));
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setButton(getString(R.string.cancel_loading_form),
                        loadingButtonListener);
                return progressDialog;
            case SAVING_DIALOG:
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "onCreateDialog.SAVING_DIALOG",
                                "show");
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.saving_form));
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setIndeterminate(true);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this,
                                        "onCreateDialog.SAVING_DIALOG", "OnDismissListener");
                        cancelSaveToDiskTask();
                    }
                });
                return progressDialog;

            case SAVING_IMAGE_DIALOG:
                progressDialog = new ProgressDialog(this);
                progressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCancelable(false);

                return progressDialog;
        }
        return null;
    }

    private void cancelSaveToDiskTask() {
        synchronized (saveDialogLock) {
            if (saveToDiskTask != null) {
                saveToDiskTask.setFormSavedListener(null);
                boolean cancelled = saveToDiskTask.cancel(true);
                Timber.w("Cancelled SaveToDiskTask! (%s)", cancelled);
                saveToDiskTask = null;
            }
        }
    }

    /**
     * Dismiss any showing dialogs that we manually manage.
     */
    private void dismissDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        FormController formController = getFormController();
        dismissDialogs();
        // make sure we're not already saving to disk. if we are, currentPrompt
        // is getting constantly updated
        if (saveToDiskTask == null
                || saveToDiskTask.getStatus() == AsyncTask.Status.FINISHED) {
            if (currentView != null && formController != null
                    && formController.currentPromptIsQuestion()) {
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
            }
        }
        if (getCurrentViewIfODKView() != null) {
            // stop audio if it's playing
            getCurrentViewIfODKView().stopAudio();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkIfStoragePermissionsGranted(this)) {
            onResumeWasCalledWithoutPermissions = true;
            return;
        }

        String navigation = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_NAVIGATION);
        showNavigationButtons = navigation.contains(PreferenceKeys.NAVIGATION_BUTTONS);
        backButton.setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);
        nextButton.setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);

        if (errorMessage != null) {
            if (alertDialog != null && !alertDialog.isShowing()) {
                createErrorDialog(errorMessage, EXIT);
            } else {
                return;
            }
        }

        FormController formController = getFormController();
        Collect.getInstance().getActivityLogger().open();

        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(this);
            if (formController == null
                    && formLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                FormController fec = formLoaderTask.getFormController();
                if (fec != null) {
                    loadingComplete(formLoaderTask, formLoaderTask.getFormDef());
                } else {
                    dismissDialog(PROGRESS_DIALOG);
                    FormLoaderTask t = formLoaderTask;
                    formLoaderTask = null;
                    t.cancel(true);
                    t.destroy();
                    // there is no formController -- fire MainMenu activity?
                    startActivity(new Intent(this, MainMenuActivity.class));
                }
            }
        } else {
            if (formController == null) {
                // there is no formController -- fire MainMenu activity?
                startActivity(new Intent(this, MainMenuActivity.class));
                finish();
                return;
            } else {
                refreshCurrentView();
            }
        }

        if (saveToDiskTask != null) {
            saveToDiskTask.setFormSavedListener(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Collect.getInstance().getActivityLogger()
                        .logInstanceAction(this, "onKeyDown.KEYCODE_BACK", "quit");
                createQuitDialog();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.isAltPressed() && !beenSwiped) {
                    beenSwiped = true;
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this,
                                    "onKeyDown.KEYCODE_DPAD_RIGHT", "showNext");
                    showNextView();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.isAltPressed() && !beenSwiped) {
                    beenSwiped = true;
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this, "onKeyDown.KEYCODE_DPAD_LEFT",
                                    "showPrevious");
                    showPreviousView();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(null);
            // We have to call cancel to terminate the thread, otherwise it
            // lives on and retains the FEC in memory.
            // but only if it's done, otherwise the thread never returns
            if (formLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                FormLoaderTask t = formLoaderTask;
                formLoaderTask = null;
                t.cancel(true);
                t.destroy();
            }
        }
        if (saveToDiskTask != null) {
            saveToDiskTask.setFormSavedListener(null);
            // We have to call cancel to terminate the thread, otherwise it
            // lives on and retains the FEC in memory.
            if (saveToDiskTask.getStatus() == AsyncTask.Status.FINISHED) {
                saveToDiskTask.cancel(true);
                saveToDiskTask = null;
            }
        }
        releaseOdkView();
        super.onDestroy();

    }

    private int animationCompletionSet;

    private void afterAllAnimations() {
        if (staleView != null) {
            if (staleView instanceof ODKView) {
                // http://code.google.com/p/android/issues/detail?id=8488
                ((ODKView) staleView).recycleDrawables();
            }
            staleView = null;
        }

        if (getCurrentViewIfODKView() != null) {
            getCurrentViewIfODKView().setFocus(this);
        }
        beenSwiped = false;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (inAnimation == animation) {
            animationCompletionSet |= 1;
        } else if (outAnimation == animation) {
            animationCompletionSet |= 2;
        } else {
            Timber.e("Unexpected animation");
        }

        if (animationCompletionSet == 3) {
            this.afterAllAnimations();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    /**
     * loadingComplete() is called by FormLoaderTask once it has finished
     * loading a form.
     */
    @Override
    public void loadingComplete(FormLoaderTask task, FormDef formDef) {
        dismissDialog(PROGRESS_DIALOG);

        final FormController formController = task.getFormController();
        int requestCode = task.getRequestCode(); // these are bogus if
        // pendingActivityResult is
        // false
        int resultCode = task.getResultCode();
        Intent intent = task.getIntent();

        formLoaderTask.setFormLoaderListener(null);
        FormLoaderTask t = formLoaderTask;
        formLoaderTask = null;
        t.cancel(true);
        t.destroy();
        Collect.getInstance().setFormController(formController);
        supportInvalidateOptionsMenu();

        Collect.getInstance().setExternalDataManager(task.getExternalDataManager());

        // Set the language if one has already been set in the past
        String[] languageTest = formController.getLanguages();
        if (languageTest != null) {
            String defaultLanguage = formController.getLanguage();
            String newLanguage = FormsDaoHelper.getFormLanguage(formPath);

            long start = System.currentTimeMillis();
            Timber.i("calling formController.setLanguage");
            try {
                formController.setLanguage(newLanguage);
            } catch (Exception e) {
                // if somehow we end up with a bad language, set it to the default
                Timber.e("Ended up with a bad language. %s", newLanguage);
                formController.setLanguage(defaultLanguage);
            }
            Timber.i("Done in %.3f seconds.", (System.currentTimeMillis() - start) / 1000F);
        }

        boolean pendingActivityResult = task.hasPendingActivityResult();

        if (pendingActivityResult) {
            // set the current view to whatever group we were at...
            refreshCurrentView();
            // process the pending activity request...
            onActivityResult(requestCode, resultCode, intent);
            return;
        }

        // it can be a normal flow for a pending activity result to restore from
        // a savepoint
        // (the call flow handled by the above if statement). For all other use
        // cases, the
        // user should be notified, as it means they wandered off doing other
        // things then
        // returned to ODK Collect and chose Edit Saved Form, but that the
        // savepoint for that
        // form is newer than the last saved version of their form data.

        boolean hasUsedSavepoint = task.hasUsedSavepoint();

        if (hasUsedSavepoint) {
            runOnUiThread(() -> ToastUtils.showLongToast(R.string.savepoint_used));
        }

        // Set saved answer path
        if (formController.getInstanceFile() == null) {

            // Create new answer folder.
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
                    Locale.ENGLISH).format(Calendar.getInstance().getTime());
            String file = formPath.substring(formPath.lastIndexOf('/') + 1,
                    formPath.lastIndexOf('.'));
            String path = Collect.INSTANCES_PATH + File.separator + file + "_"
                    + time;
            if (FileUtils.createFolder(path)) {
                File instanceFile = new File(path + File.separator + file + "_" + time + ".xml");
                formController.setInstanceFile(instanceFile);
            }

            formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_START, 0, null, false, true);
        } else {
            Intent reqIntent = getIntent();
            boolean showFirst = reqIntent.getBooleanExtra("start", false);

            if (!showFirst) {
                // we've just loaded a saved form, so start in the hierarchy view

                if (!allowMovingBackwards) {
                    FormIndex formIndex = SaveFormIndexTask.loadFormIndexFromFile();
                    if (formIndex != null) {
                        formController.jumpToIndex(formIndex);
                        refreshCurrentView();
                        return;
                    }
                }

                String formMode = reqIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_RESUME, 0, null, false, true);
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.HIERARCHY, 0, null, false, true);
                    startActivity(new Intent(this, EditFormHierarchyActivity.class));
                    return; // so we don't show the intro screen before jumping to the hierarchy
                } else {
                    if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
                        startActivity(new Intent(this, ViewFormHierarchyActivity.class));
                    }
                    finish();
                }
            } else {
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_RESUME, 0, null, false, true);
            }
        }

        refreshCurrentView();
    }

    /**
     * called by the FormLoaderTask if something goes wrong.
     */
    @Override
    public void loadingError(String errorMsg) {
        dismissDialog(PROGRESS_DIALOG);
        if (errorMsg != null) {
            createErrorDialog(errorMsg, EXIT);
        } else {
            createErrorDialog(getString(R.string.parse_error), EXIT);
        }
    }

    /**
     * Called by SavetoDiskTask if everything saves correctly.
     */
    @Override
    public void savingComplete(SaveResult saveResult) {
        dismissDialog(SAVING_DIALOG);

        int saveStatus = saveResult.getSaveResult();
        FormController formController = getFormController();
        switch (saveStatus) {
            case SaveToDiskTask.SAVED:
                ToastUtils.showShortToast(R.string.data_saved_ok);
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_SAVE, 0, null, false, false);
                sendSavedBroadcast();
                break;
            case SaveToDiskTask.SAVED_AND_EXIT:
                ToastUtils.showShortToast(R.string.data_saved_ok);
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_SAVE, 0, null, false, false);
                if (saveResult.isComplete()) {
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_EXIT, 0, null, false, false);
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_FINALIZE, 0, null, false, true);     // Force writing of audit since we are exiting
                } else {
                    formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FORM_EXIT, 0, null, false, true);         // Force writing of audit since we are exiting
                }
                sendSavedBroadcast();
                finishReturnInstance();
                break;
            case SaveToDiskTask.SAVE_ERROR:
                String message;
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.SAVE_ERROR, 0, null, false, true);
                if (saveResult.getSaveErrorMessage() != null) {
                    message = getString(R.string.data_saved_error) + ": "
                            + saveResult.getSaveErrorMessage();
                } else {
                    message = getString(R.string.data_saved_error);
                }
                ToastUtils.showLongToast(message);
                break;
            case SaveToDiskTask.ENCRYPTION_ERROR:
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.FINALIZE_ERROR, 0, null, false, true);
                ToastUtils.showLongToast(String.format(getString(R.string.encryption_error_message),
                        saveResult.getSaveErrorMessage()));
                finishReturnInstance();
                break;
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                formController.getTimerLogger().exitView();
                formController.getTimerLogger().logTimerEvent(TimerLogger.EventTypes.CONSTRAINT_ERROR, 0, null, false, true);
                refreshCurrentView();

                // get constraint behavior preference value with appropriate default
                String constraintBehavior = (String) GeneralSharedPreferences.getInstance()
                        .get(PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR);

                // an answer constraint was violated, so we need to display the proper toast(s)
                // if constraint behavior is on_swipe, this will happen if we do a 'swipe' to the
                // next question
                if (constraintBehavior.equals(PreferenceKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE)) {
                    next();
                } else {
                    // otherwise, we can get the proper toast(s) by saving with constraint check
                    saveAnswersForCurrentScreen(EVALUATE_CONSTRAINTS);
                }

                break;
        }
    }

    @Override
    public void onProgressStep(String stepMessage) {
        if (progressDialog != null) {
            progressDialog.setMessage(getString(R.string.please_wait) + "\n\n" + stepMessage);
        }
    }

    public void next() {
        if (!beenSwiped) {
            beenSwiped = true;
            showNextView();
        }
    }

    /**
     * Returns the instance that was just filled out to the calling activity, if
     * requested.
     */
    private void finishReturnInstance() {
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_EDIT.equals(action)) {
            // caller is waiting on a picked form
            Uri uri = InstancesDaoHelper.getLastInstanceUri(getAbsoluteInstancePath());
            if (uri != null) {
                setResult(RESULT_OK, new Intent().setData(uri));
            }
        }
        finish();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // only check the swipe if it's enabled in preferences
        String navigation = (String) GeneralSharedPreferences.getInstance()
                .get(PreferenceKeys.KEY_NAVIGATION);

        if (navigation.contains(PreferenceKeys.NAVIGATION_SWIPE) && doSwipe) {
            // Looks for user swipes. If the user has swiped, move to the
            // appropriate screen.

            // for all screens a swipe is left/right of at least
            // .25" and up/down of less than .25"
            // OR left/right of > .5"
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int xpixellimit = (int) (dm.xdpi * .25);
            int ypixellimit = (int) (dm.ydpi * .25);

            if (getCurrentViewIfODKView() != null) {
                if (getCurrentViewIfODKView().suppressFlingGesture(e1, e2,
                        velocityX, velocityY)) {
                    return false;
                }
            }

            if (beenSwiped) {
                return false;
            }

            if ((Math.abs(e1.getX() - e2.getX()) > xpixellimit && Math.abs(e1
                    .getY() - e2.getY()) < ypixellimit)
                    || Math.abs(e1.getX() - e2.getX()) > xpixellimit * 2) {
                beenSwiped = true;
                if (velocityX > 0) {
                    if (e1.getX() > e2.getX()) {
                        Timber.e("showNextView VelocityX is bogus! %f > %f", e1.getX(), e2.getX());
                        Collect.getInstance().getActivityLogger()
                                .logInstanceAction(this, "onFling", "showNext");
                        showNextView();
                    } else {
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "onFling",
                                        "showPrevious");
                        showPreviousView();
                    }
                } else {
                    if (e1.getX() < e2.getX()) {
                        Timber.e("showPreviousView VelocityX is bogus! %f < %f", e1.getX(), e2.getX());
                        Collect.getInstance()
                                .getActivityLogger()
                                .logInstanceAction(this, "onFling",
                                        "showPrevious");
                        showPreviousView();
                    } else {
                        Collect.getInstance().getActivityLogger()
                                .logInstanceAction(this, "onFling", "showNext");
                        showNextView();
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // The onFling() captures the 'up' event so our view thinks it gets long
        // pressed.
        // We don't wnat that, so cancel it.
        if (currentView != null) {
            currentView.cancelLongPress();
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void advance() {
        next();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    private void sendSavedBroadcast() {
        sendBroadcast(new Intent("org.odk.collect.android.FormSaved"));
    }

    @Override
    public void onSavePointError(String errorMessage) {
        if (errorMessage != null && errorMessage.trim().length() > 0) {
            ToastUtils.showLongToast(getString(R.string.save_point_error, errorMessage));
        }
    }

    @Override
    public void onSaveFormIndexError(String errorMessage) {
        if (errorMessage != null && errorMessage.trim().length() > 0) {
            ToastUtils.showLongToast(getString(R.string.save_point_error, errorMessage));
        }
    }

    @Override
    public void onNumberPickerValueSelected(int widgetId, int value) {
        if (currentView != null) {
            for (QuestionWidget qw : ((ODKView) currentView).getWidgets()) {
                if (qw instanceof RangeWidget && widgetId == qw.getId()) {
                    ((RangeWidget) qw).setNumberPickerValue(value);
                }
            }
        }
    }

    @Override
    public void onDateChanged(LocalDateTime date) {
        ODKView odkView = getCurrentViewIfODKView();
        if (odkView != null) {
            odkView.setBinaryData(date);
        }
    }

    @Override
    public void onRankingChanged(List<String> values) {
        ODKView odkView = getCurrentViewIfODKView();
        if (odkView != null) {
            odkView.setBinaryData(values);
        }
    }

    /**
     * getter for currentView variable. This method should always be used
     * to access currentView as an ODKView object to avoid inconsistency
     **/
    @Nullable
    private ODKView getCurrentViewIfODKView() {
        if (currentView instanceof ODKView) {
            return (ODKView) currentView;
        }
        return null;
    }

    @Override
    public ActivityAvailability provide() {
        return activityAvailability;
    }

    public void setActivityAvailability(@NonNull ActivityAvailability activityAvailability) {
        this.activityAvailability = activityAvailability;
    }

    public void setShouldOverrideAnimations(boolean shouldOverrideAnimations) {
        this.shouldOverrideAnimations = shouldOverrideAnimations;
    }

    /**
     * Used whenever we need to show empty view and be able to recognize it from the code
     */
    class EmptyView extends View {

        EmptyView(Context context) {
            super(context);
        }
    }
}
