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
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.common.collect.ImmutableList;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.helpers.ContentResolverHelper;
import org.odk.collect.android.dao.helpers.FormsDaoHelper;
import org.odk.collect.android.dao.helpers.InstancesDaoHelper;
import org.odk.collect.android.events.ReadPhoneStatePermissionRxEvent;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationHelper;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationManager;
import org.odk.collect.android.fragments.MediaLoadingFragment;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FormLoadingDialogFragment;
import org.odk.collect.android.fragments.dialogs.LocationProvidersDisabledDialog;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.android.fragments.dialogs.RankingWidgetDialog;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.listeners.FormSavedListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.listeners.SavePointListener;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.location.client.GoogleLocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.logic.AuditEvent;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.FormController.FailedConstraint;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.logic.ImmutableDisplayableQuestion;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveFormIndexTask;
import org.odk.collect.android.tasks.SavePointTask;
import org.odk.collect.android.tasks.SaveResult;
import org.odk.collect.android.tasks.SaveToDiskTask;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DependencyProvider;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ImageConverter;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.RegexUtils;
import org.odk.collect.android.utilities.SnackbarUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.ODKView;
import org.odk.collect.android.widgets.DateTimeWidget;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.RangeWidget;
import org.odk.collect.android.widgets.StringWidget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_MOVING_BACKWARDS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.areStoragePermissionsGranted;
import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;

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
        SaveFormIndexTask.SaveFormIndexListener, FormLoadingDialogFragment.FormLoadingDialogFragmentListener,
        WidgetValueChangedListener {

    // Defines for FormEntryActivity
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private static final boolean EVALUATE_CONSTRAINTS = true;
    public static final boolean DO_NOT_EVALUATE_CONSTRAINTS = false;

    // Extra returned from gp activity
    public static final String LOCATION_RESULT = "LOCATION_RESULT";
    public static final String BEARING_RESULT = "BEARING_RESULT";
    public static final String GEOSHAPE_RESULTS = "GEOSHAPE_RESULTS";
    public static final String ANSWER_KEY = "ANSWER_KEY";

    public static final String KEY_INSTANCES = "instances";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    private static final String KEY_SAVE_NAME = "saveName";
    private static final String KEY_LOCATION_PERMISSIONS_GRANTED = "location_permissions_granted";

    private static final String TAG_MEDIA_LOADING_FRAGMENT = "media_loading_fragment";

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
    public static final String KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED = "readPhoneStatePermissionRequestNeeded";

    private static final int SAVING_DIALOG = 2;

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

    private FormLoaderTask formLoaderTask;
    private SaveToDiskTask saveToDiskTask;

    private TextView nextButton;
    private TextView backButton;

    private ODKView odkView;

    private boolean doSwipe = true;
    private String instancePath;
    private String startingXPath;
    private String waitingXPath;
    private boolean newForm = true;
    private boolean onResumeWasCalledWithoutPermissions;
    private boolean readPhoneStatePermissionRequestNeeded;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    MediaLoadingFragment mediaLoadingFragment;

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

    @Inject
    RxEventBus eventBus;

    private final LocationProvidersReceiver locationProvidersReceiver = new LocationProvidersReceiver();

    /**
     * True if the Android location permission was granted last time it was checked. Allows for
     * detection of location permissions changes while the activity is in the background.
     */
    private boolean locationPermissionsPreviouslyGranted;

    FormEntryViewModel viewModel;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this, new FormEntryViewModelFactory()).get(FormEntryViewModel.class);

        setContentView(R.layout.form_entry);

        Collect.getInstance().getComponent().inject(this);

        compositeDisposable
                .add(eventBus
                .register(ReadPhoneStatePermissionRxEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    readPhoneStatePermissionRequestNeeded = true;
                }));

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

        if (savedInstanceState == null) {
            mediaLoadingFragment = new MediaLoadingFragment();
            getFragmentManager().beginTransaction().add(mediaLoadingFragment, TAG_MEDIA_LOADING_FRAGMENT).commit();
        } else {
            mediaLoadingFragment = (MediaLoadingFragment) getFragmentManager().findFragmentByTag(TAG_MEDIA_LOADING_FRAGMENT);
        }

        new PermissionUtils().requestStoragePermissions(this, new PermissionListener() {
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
            if (savedInstanceState.containsKey(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED)) {
                readPhoneStatePermissionRequestNeeded = savedInstanceState.getBoolean(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED);
            }
            if (savedInstanceState.containsKey(KEY_LOCATION_PERMISSIONS_GRANTED)) {
                locationPermissionsPreviouslyGranted = savedInstanceState.getBoolean(KEY_LOCATION_PERMISSIONS_GRANTED);
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
                if (getFormController(true) != null) {
                    refreshCurrentView();
                } else {
                    Timber.w("Reloading form and restoring state.");
                    formLoaderTask = new FormLoaderTask(instancePath, startingXPath, waitingXPath);
                    showFormLoadingDialogFragment();
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
        showFormLoadingDialogFragment();
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
     * Creates save-points asynchronously in order to not affect swiping performance on larger forms.
     * If moving backwards through a form is disabled, also saves the index of the form element that
     * was last shown to the user so that no matter how the app exits and relaunches, the user can't
     * see previous questions.
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

    @Nullable
    private FormController getFormController() {
        return getFormController(false);
    }

    @Nullable
    private FormController getFormController(boolean formReloading) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            Collect.getInstance().logNullFormControllerEvent(formReloading ? "FormReloading" : "OtherInFormEntryActivity");
        }

        return formController;
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
        outState.putBoolean(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED, readPhoneStatePermissionRequestNeeded);
        outState.putBoolean(KEY_LOCATION_PERMISSIONS_GRANTED, locationPermissionsPreviouslyGranted);

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

        // If we're coming back from the hierarchy view, the user has either tapped the back
        // button or another question to jump to so we need to rebuild the view.
        if (requestCode == RequestCodes.HIERARCHY_ACTIVITY) {
            refreshCurrentView();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            if (getCurrentViewIfODKView() != null) {
                getCurrentViewIfODKView().cancelWaitingForBinaryData();
            }
            return;
        }

        // intent is needed for all requestCodes except of DRAW_IMAGE, ANNOTATE_IMAGE, SIGNATURE_CAPTURE, IMAGE_CAPTURE and HIERARCHY_ACTIVITY
        if (intent == null && requestCode != RequestCodes.DRAW_IMAGE && requestCode != RequestCodes.ANNOTATE_IMAGE
                && requestCode != RequestCodes.SIGNATURE_CAPTURE && requestCode != RequestCodes.IMAGE_CAPTURE) {
            Timber.w("The intent has a null value for requestCode: " + requestCode);
            ToastUtils.showLongToast(getString(R.string.null_intent_value));
            return;
        }

        // Handling results returned by the Zxing Barcode scanning library is done outside the
        // switch statement because IntentIntegrator.REQUEST_CODE is not final.
        // TODO: see if we can update the ZXing Android Embedded dependency to 3.6.0.
        // https://github.com/journeyapps/zxing-android-embedded#adding-aar-dependency-with-gradle
        // makes it unclear
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
                return;
            }
        }

        switch (requestCode) {
            case RequestCodes.OSM_CAPTURE:
                String osmFileName = intent.getStringExtra("OSM_FILE_NAME");
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(osmFileName);
                }
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
                break;
            case RequestCodes.ARBITRARY_FILE_CHOOSER:
            case RequestCodes.AUDIO_CHOOSER:
            case RequestCodes.VIDEO_CHOOSER:
            case RequestCodes.IMAGE_CHOOSER:
                /*
                 * We have a saved image somewhere, but we really want it to be in:
                 * /sdcard/odk/instances/[current instnace]/something.jpg so we move
                 * it there before inserting it into the content provider. Once the
                 * android image capture bug gets fixed, (read, we move on from
                 * Android 1.6) we want to handle images the audio and video
                 */

                ProgressDialogFragment.newInstance(getString(R.string.please_wait))
                        .show(getSupportFragmentManager(), ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);

                mediaLoadingFragment.beginMediaLoadingTask(intent.getData());
                break;
            case RequestCodes.AUDIO_CAPTURE:
                /*
                  Probably this approach should be used in all cases to get a file from an uri.
                  The approach which was used before and which is still used in other places
                  might be faulty because sometimes _data column might be not provided in an uri.
                  e.g. https://github.com/opendatakit/collect/issues/705
                  Let's test it here and then we can use the same code in other places if it works well.
                 */
                Uri mediaUri = intent.getData();
                if (mediaUri != null) {
                    String filePath =
                            formController.getInstanceFile().getParent()
                                    + File.separator
                                    + System.currentTimeMillis()
                                    + "."
                                    + ContentResolverHelper.getFileExtensionFromUri(this, mediaUri);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(mediaUri);
                        if (inputStream != null) {
                            OutputStream outputStream = new FileOutputStream(new File(filePath));
                            IOUtils.copy(inputStream, outputStream);
                            inputStream.close();
                            outputStream.close();
                            saveFileAnswer(new File(filePath));
                        }
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
                break;
            case RequestCodes.VIDEO_CAPTURE:
                mediaUri = intent.getData();
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
            case RequestCodes.LOCATION_CAPTURE:
                String sl = intent.getStringExtra(LOCATION_RESULT);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(sl);
                }
                break;
            case RequestCodes.GEOSHAPE_CAPTURE:
                String gshr = intent.getStringExtra(ANSWER_KEY);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(gshr);
                }
                break;
            case RequestCodes.GEOTRACE_CAPTURE:
                String traceExtra = intent.getStringExtra(ANSWER_KEY);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(traceExtra);
                }
                break;
            case RequestCodes.BEARING_CAPTURE:
                String bearing = intent.getStringExtra(BEARING_RESULT);
                if (getCurrentViewIfODKView() != null) {
                    getCurrentViewIfODKView().setBinaryData(bearing);
                }
                break;
        }
    }

    public QuestionWidget getWidgetWaitingForBinaryData() {
        ODKView odkView = getCurrentViewIfODKView();

        if (odkView != null) {
            for (QuestionWidget qw : odkView.getWidgets()) {
                if (qw.isWaitingForData()) {
                    return qw;
                }
            }
        } else {
            Timber.e("currentView returned null.");
        }
        return null;
    }

    private void saveFileAnswer(Object media) {
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
     * Rebuilds the current view. the controller and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     */
    public void refreshCurrentView() {
        int event = getFormController().getEvent();

        View current = createView(event, false);
        showView(current, AnimationType.FADE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

        if (getFormController() != null && getFormController().currentFormCollectsBackgroundLocation()
                && LocationClients.areGooglePlayServicesAvailable(this)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FormController formController = getFormController();
        switch (item.getItemId()) {
            case R.id.menu_languages:
                createLanguageDialog();
                return true;
            case R.id.menu_save:
                // don't exit
                saveDataToDisk(DO_NOT_EXIT, InstancesDaoHelper.isInstanceComplete(false), null);
                return true;
            case R.id.menu_goto:
                state = null;
                if (formController != null && formController.currentPromptIsQuestion()) {
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                }

                if (formController != null) {
                    formController.getAuditEventLogger().exitView();
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true);
                }

                Intent i = new Intent(this, FormHierarchyActivity.class);
                startActivityForResult(i, RequestCodes.HIERARCHY_ACTIVITY);
                return true;
            case R.id.menu_preferences:
                Intent pref = new Intent(this, PreferencesActivity.class);
                startActivity(pref);
                return true;
            case R.id.track_location:
                GeneralSharedPreferences.getInstance().save(KEY_BACKGROUND_LOCATION, !GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));

                viewModel.backgroundLocationPreferenceToggled();
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
    public boolean saveAnswersForCurrentScreen(boolean evaluateConstraints) {
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
            createDeleteRepeatConfirmDialog();
        } else {
            /*
             * We don't have the right view here, so we store the View's ID as the
             * item ID and loop through the possible views to find the one the user
             * clicked on.
             */
            boolean shouldClearDialogBeShown;
            ODKView odkView = getCurrentViewIfODKView();
            if (odkView != null) {
                for (QuestionWidget qw : odkView.getWidgets()) {
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
                        createClearDialog(qw);
                        break;
                    }
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
     * Creates and returns a new view based on the event type passed in. The view returned is
     * of type {@link View} if the event passed in represents the end of the form or of type
     * {@link ODKView} otherwise.
     *
     * @param advancingPage -- true if this results from advancing through the form
     * @return newly created View
     */
    private View createView(int event, boolean advancingPage) {
        FormController formController = getFormController();

        setTitle(formController.getFormTitle());

        if (event != FormEntryController.EVENT_QUESTION) {
            formController.getAuditEventLogger().logEvent(AuditEvent.getAuditEventTypeFromFecType(event),
                    formController.getFormIndex(), true, null);
        }

        switch (event) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
                return createViewForFormBeginning(formController);
            case FormEntryController.EVENT_END_OF_FORM:
                return createViewForFormEnd(formController);
            case FormEntryController.EVENT_QUESTION:
            case FormEntryController.EVENT_GROUP:
            case FormEntryController.EVENT_REPEAT:
                releaseOdkView();
                // should only be a group here if the event_group is a field-list
                try {
                    FormEntryPrompt[] prompts = formController.getQuestionPrompts();
                    for (FormEntryPrompt question : prompts) {
                        String answer = question.getAnswerValue() != null ? question.getAnswerValue().getDisplayText() : null;
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.QUESTION, question.getIndex(), true, answer);
                    }
                    FormEntryCaption[] groups = formController
                            .getGroupsForCurrentIndex();

                    odkView = new ODKView(this, prompts, groups, advancingPage);
                    odkView.setWidgetValueChangedListener(this);
                    Timber.i("Created view for group %s %s",
                            groups.length > 0 ? groups[groups.length - 1].getLongText() : "[top]",
                            prompts.length > 0 ? prompts[0].getQuestionText() : "[no question]");
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

                if (showNavigationButtons) {
                    updateNavigationButtonVisibility();
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

    /**
     * Steps to the next screen and creates a view for it. Always sets {@code advancingPage} to true
     * to auto-play media.
     */
    private View createViewForFormBeginning(FormController formController) {
        int event = FormEntryController.EVENT_BEGINNING_OF_FORM;
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

        return createView(event, true);
    }

    /**
     * Creates the final screen in a form-filling interaction. Allows the user to set a display
     * name for the instance and to decide whether the form should be finalized or not. Presents
     * a button for saving and exiting.
     */
    private View createViewForFormEnd(FormController formController) {
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
        InputFilter returnFilter = (source, start, end, dest, dstart, dend)
                -> RegexUtils.normalizeFormName(source.toString().substring(start, end), true);
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
            updateNavigationButtonVisibility();
        }

        return endView;
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
                    .get(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR);

            if (formController != null && formController.currentPromptIsQuestion()) {
                // if constraint behavior says we should validate on swipe, do so
                if (constraintBehavior.equals(GeneralKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE)) {
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

            formController.getAuditEventLogger().exitView();    // Close events waiting for an end time

            switch (event) {
                case FormEntryController.EVENT_QUESTION:
                case FormEntryController.EVENT_GROUP:
                    // create a savepoint
                    nonblockingCreateSavePointData();
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
     * If moving backwards is allowed, displays the view for the previous question or field list.
     * Steps the global {@link FormController} to the previous question and saves answers to the
     * data model without checking constraints.
     */
    private void showPreviousView() {
        if (allowMovingBackwards) {
            state = null;
            try {
                FormController formController = getFormController();
                if (formController != null) {
                    // The answer is saved on a back swipe, but question constraints are ignored.
                    if (formController.currentPromptIsQuestion()) {
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                    }

                    if (formController.getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
                        int event = formController.stepToPreviousScreenEvent();

                        // If we are the beginning of the form, there is no previous view to show
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
                            nonblockingCreateSavePointData();
                        }
                        formController.getAuditEventLogger().exitView();    // Close events
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
                        Collect.getInstance().logRemoteAnalytics("WidgetAttribute", "saveIncomplete", Collect.getCurrentFormIdentifierHash());

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
        DialogUtils.showDeleteRepeatConfirmDialog(this, () -> {
            showNextView();
        }, () -> {
            refreshCurrentView();
        });
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

        ListView listView = DialogUtils.createActionListView(this);

        final IconMenuListAdapter adapter = new IconMenuListAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IconMenuItem item = (IconMenuItem) adapter.getItem(position);
                if (item.getTextResId() == R.string.keep_changes) {
                    saveDataToDisk(EXIT, InstancesDaoHelper.isInstanceComplete(false), null);
                } else {
                    // close all open databases of external data.
                    ExternalDataManager manager = Collect.getInstance().getExternalDataManager();
                    if (manager != null) {
                        manager.close();
                    }

                    FormController formController = getFormController();
                    if (formController != null) {
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true);
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

    @Nullable
    private String getAbsoluteInstancePath() {
        FormController formController = getFormController();
        return formController != null ? formController.getAbsoluteInstancePath() : null;
    }

    /**
     * Confirm clear answer dialog
     */
    private void createClearDialog(final QuestionWidget qw) {
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
                        clearAnswer(qw);
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
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
                .setNegativeButton(getString(R.string.do_not_change), null).create();
        alertDialog.show();
    }

    /**
     * We use Android's dialog management for loading/saving progress dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SAVING_DIALOG:
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.saving_form));
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setIndeterminate(true);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        cancelSaveToDiskTask();
                    }
                });
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

    /**
     * Shows the next or back button, neither or both. Both buttons are displayed unless:
     * - we are at the first question in the form so the back button is hidden
     * - we are at the end screen so the next button is hidden
     * - settings prevent backwards navigation of the form so the back button is hidden
     *
     * The visibility of the container for these buttons is determined once {@link #onResume()}.
     */
    private void updateNavigationButtonVisibility() {
        FormController formController = getFormController();
        if (formController == null) {
            return;
        }

        backButton.setVisibility(!formController.isCurrentQuestionFirstInForm() && allowMovingBackwards ? View.VISIBLE : View.GONE);
        nextButton.setVisibility(formController.getEvent() != FormEntryController.EVENT_END_OF_FORM ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FormController formController = getFormController();

        // Register to receive location provider change updates and write them to the audit log
        if (formController != null && formController.currentFormAuditsLocation()
                && LocationClients.areGooglePlayServicesAvailable(this)) {
            registerReceiver(locationProvidersReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        // User may have changed location permissions in Android settings
        if (PermissionUtils.areLocationPermissionsGranted(this) != locationPermissionsPreviouslyGranted) {
            viewModel.locationPermissionChanged();
            locationPermissionsPreviouslyGranted = !locationPermissionsPreviouslyGranted;
        }
        activityDisplayed();
    }

    @Override
    protected void onStop() {
        viewModel.activityHidden();

        super.onStop();
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

        if (!areStoragePermissionsGranted(this)) {
            onResumeWasCalledWithoutPermissions = true;
            return;
        }

        String navigation = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_NAVIGATION);
        showNavigationButtons = navigation.contains(GeneralKeys.NAVIGATION_BUTTONS);

        findViewById(R.id.buttonholder).setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);
        findViewById(R.id.shadow_up).setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);

        if (showNavigationButtons) {
            updateNavigationButtonVisibility();
        }

        if (errorMessage != null) {
            if (alertDialog != null && !alertDialog.isShowing()) {
                createErrorDialog(errorMessage, EXIT);
            } else {
                return;
            }
        }

        FormController formController = getFormController();

        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(this);
            if (formController == null
                    && formLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                FormController fec = formLoaderTask.getFormController();
                if (fec != null) {
                    if (!readPhoneStatePermissionRequestNeeded) {
                        loadingComplete(formLoaderTask, formLoaderTask.getFormDef());
                    }
                } else {
                    dismissFormLoadingDialogFragment();
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
            }
        }

        if (saveToDiskTask != null) {
            saveToDiskTask.setFormSavedListener(this);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        /*
          Make sure the progress dialog is dismissed.
          In most cases that dialog is dismissed in MediaLoadingTask#onPostExecute() but if the app
          is in the background when MediaLoadingTask#onPostExecute() is called then the dialog
          can not be dismissed. In such a case we need to make sure it's dismissed in order
          to avoid blocking the UI.
         */
        if (!mediaLoadingFragment.isMediaLoadingTaskRunning()) {
            Fragment progressDialogFragment =
                    getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);
            if (progressDialogFragment != null) {
                ((DialogFragment) progressDialogFragment).dismiss();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                createQuitDialog();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.isAltPressed() && !beenSwiped) {
                    beenSwiped = true;
                    showNextView();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.isAltPressed() && !beenSwiped) {
                    beenSwiped = true;
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
        compositeDisposable.dispose();

        try {
            unregisterReceiver(locationProvidersReceiver);
        } catch (IllegalArgumentException e) {
            // This is the common case -- the form didn't have location audits enabled so the
            // receiver was not registered.
        }

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
     * Given a {@link FormLoaderTask} which has created a {@link FormController} for either a new or
     * existing instance, shows that instance to the user. Either launches {@link FormHierarchyActivity}
     * if an existing instance is being edited or builds the view for the current question(s) if a
     * new instance is being created.
     *
     * May do some or all of these depending on current state:
     * - Ensures phone state permissions are given if this form needs them
     * - Cleans up {@link #formLoaderTask}
     * - Sets the global form controller and database manager for search()/pulldata()
     * - Restores the last-used language
     * - Handles activity results that may have come in while the form was loading
     * - Alerts the user of a recovery from savepoint
     * - Verifies whether an instance folder exists and creates one if not
     * - Initializes background location capture (only if the instance being loaded is a new one)
     */
    @Override
    public void loadingComplete(FormLoaderTask task, FormDef formDef) {
        dismissFormLoadingDialogFragment();

        final FormController formController = task.getFormController();
        if (formController != null) {
            if (readPhoneStatePermissionRequestNeeded) {
                new PermissionUtils().requestReadPhoneStatePermission(this, true, new PermissionListener() {
                    @Override
                    public void granted() {
                        readPhoneStatePermissionRequestNeeded = false;
                        Collect.getInstance().initializeJavaRosa();
                        loadForm();
                    }

                    @Override
                    public void denied() {
                        finish();
                    }
                });
            } else {
                formLoaderTask.setFormLoaderListener(null);
                FormLoaderTask t = formLoaderTask;
                formLoaderTask = null;
                t.cancel(true);
                t.destroy();
                Collect.getInstance().setFormController(formController);
                supportInvalidateOptionsMenu();

                viewModel.formFinishedLoading();

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
                    onActivityResult(task.getRequestCode(), task.getResultCode(), task.getIntent());
                    return;
                }

                // it can be a normal flow for a pending activity result to restore from a savepoint
                // (the call flow handled by the above if statement). For all other use cases, the
                // user should be notified, as it means they wandered off doing other things then
                // returned to ODK Collect and chose Edit Saved Form, but that the savepoint for
                // that form is newer than the last saved version of their form data.
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

                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_START, true);
                } else {
                    Intent reqIntent = getIntent();
                    boolean showFirst = reqIntent.getBooleanExtra("start", false);

                    if (!showFirst) {
                        if (!allowMovingBackwards) {
                            FormIndex formIndex = SaveFormIndexTask.loadFormIndexFromFile();
                            if (formIndex != null) {
                                formController.jumpToIndex(formIndex);
                                refreshCurrentView();
                                return;
                            }
                        }

                        // we've just loaded a saved form, so start in the hierarchy view
                        String formMode = reqIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                        if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                            formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true);
                            formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true);
                            startActivityForResult(new Intent(this, FormHierarchyActivity.class), RequestCodes.HIERARCHY_ACTIVITY);
                            return; // so we don't show the intro screen before jumping to the hierarchy
                        } else {
                            if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
                                startActivity(new Intent(this, ViewOnlyFormHierarchyActivity.class));
                            }
                            finish();
                        }
                    } else {
                        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true);
                    }
                }

                // Register to receive location provider change updates and write them to the audit
                // log. onStart has already run but the formController was null so try again.
                if (formController.currentFormAuditsLocation()
                        && LocationClients.areGooglePlayServicesAvailable(this)) {
                    registerReceiver(locationProvidersReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                }

                // onStart ran before the form was loaded. Let the viewModel know that the activity
                // is about to be displayed and configured. Do this before the refresh actually
                // happens because if audit logging is enabled, the refresh logs a question event
                // and we want that to show up after initialization events.
                activityDisplayed();

                refreshCurrentView();
            }
        } else {
            Timber.e("FormController is null");
            ToastUtils.showLongToast(R.string.loading_form_failed);
            finish();
        }
    }

    /**
     * called by the FormLoaderTask if something goes wrong.
     */
    @Override
    public void loadingError(String errorMsg) {
        dismissFormLoadingDialogFragment();

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
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_SAVE, false);
                break;
            case SaveToDiskTask.SAVED_AND_EXIT:
                ToastUtils.showShortToast(R.string.data_saved_ok);
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_SAVE, false);
                if (saveResult.isComplete()) {
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, false);
                    // Force writing of audit since we are exiting
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true);

                    // Request auto-send if app-wide auto-send is enabled or the form that was just
                    // finalized specifies that it should always be auto-sent.
                    String formId = getFormController().getFormDef().getMainInstance().getRoot().getAttributeValue("", "id");
                    if (AutoSendWorker.formShouldBeAutoSent(formId, GeneralSharedPreferences.isAutoSendEnabled())) {
                        requestAutoSend();
                    }
                } else {
                    // Force writing of audit since we are exiting
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true);
                }

                finishReturnInstance();
                break;
            case SaveToDiskTask.SAVE_ERROR:
                String message;
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true);
                if (saveResult.getSaveErrorMessage() != null) {
                    message = getString(R.string.data_saved_error) + " "
                            + saveResult.getSaveErrorMessage();
                } else {
                    message = getString(R.string.data_saved_error);
                }
                ToastUtils.showLongToast(message);
                break;
            case SaveToDiskTask.ENCRYPTION_ERROR:
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true);
                ToastUtils.showLongToast(String.format(getString(R.string.encryption_error_message),
                        saveResult.getSaveErrorMessage()));
                finishReturnInstance();
                break;
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                formController.getAuditEventLogger().exitView();
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true);
                refreshCurrentView();

                // get constraint behavior preference value with appropriate default
                String constraintBehavior = (String) GeneralSharedPreferences.getInstance()
                        .get(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR);

                // an answer constraint was violated, so we need to display the proper toast(s)
                // if constraint behavior is on_swipe, this will happen if we do a 'swipe' to the
                // next question
                if (constraintBehavior.equals(GeneralKeys.CONSTRAINT_BEHAVIOR_ON_SWIPE)) {
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
        } else {
            FormLoadingDialogFragment formLoadingDialogFragment = getFormLoadingDialogFragment();
            if (formLoadingDialogFragment != null) {
                formLoadingDialogFragment.updateMessage(getString(R.string.please_wait) + "\n\n" + stepMessage);
            }
        }
    }

    public void next() {
        if (!beenSwiped) {
            beenSwiped = true;
            showNextView();
        }
    }

    /**
     * Requests that unsent finalized forms be auto-sent. If no network connection is available,
     * the work will be performed when a connection becomes available.
     *
     * TODO: if the user changes auto-send settings, should an auto-send job immediately be enqueued?
     */
    private void requestAutoSend() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest autoSendWork =
                new OneTimeWorkRequest.Builder(AutoSendWorker.class)
                        .addTag(AutoSendWorker.class.getName())
                        .setConstraints(constraints)
                        .build();
        WorkManager.getInstance().beginUniqueWork(AutoSendWorker.class.getName(),
                ExistingWorkPolicy.KEEP, autoSendWork).enqueue();
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

    private FormLoadingDialogFragment getFormLoadingDialogFragment() {
        return (FormLoadingDialogFragment) getSupportFragmentManager()
                .findFragmentByTag(FormLoadingDialogFragment.FORM_LOADING_DIALOG_FRAGMENT_TAG);
    }

    private void showFormLoadingDialogFragment() {
        FormLoadingDialogFragment
                .newInstance()
                .show(getSupportFragmentManager(), FormLoadingDialogFragment.FORM_LOADING_DIALOG_FRAGMENT_TAG);
    }

    private void dismissFormLoadingDialogFragment() {
        FormLoadingDialogFragment formLoadingDialogFragment = getFormLoadingDialogFragment();
        if (formLoadingDialogFragment != null) {
            formLoadingDialogFragment.dismiss();
        }
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
                .get(GeneralKeys.KEY_NAVIGATION);

        if (e1 != null && e2 != null
                && navigation.contains(GeneralKeys.NAVIGATION_SWIPE) && doSwipe) {
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
                        showNextView();
                    } else {
                        showPreviousView();
                    }
                } else {
                    if (e1.getX() < e2.getX()) {
                        Timber.e("showPreviousView VelocityX is bogus! %f < %f", e1.getX(), e2.getX());
                        showPreviousView();
                    } else {
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
                    widgetValueChanged(qw);
                    return;
                }
            }
        }
    }

    @Override
    public void onDateChanged(LocalDateTime date) {
        ODKView odkView = getCurrentViewIfODKView();
        if (odkView != null) {
            QuestionWidget widgetGettingNewValue = getWidgetWaitingForBinaryData();
            odkView.setBinaryData(date);
            widgetValueChanged(widgetGettingNewValue);
        }
    }

    @Override
    public void onRankingChanged(List<String> values) {
        ODKView odkView = getCurrentViewIfODKView();
        if (odkView != null) {
            QuestionWidget widgetGettingNewValue = getWidgetWaitingForBinaryData();
            odkView.setBinaryData(values);
            widgetValueChanged(widgetGettingNewValue);
        }
    }

    @Override
    public void onCancelFormLoading() {
        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(null);
            FormLoaderTask t = formLoaderTask;
            formLoaderTask = null;
            t.cancel(true);
            t.destroy();
        }
        finish();
    }

    /**
     * getter for currentView variable. This method should always be used
     * to access currentView as an ODKView object to avoid inconsistency
     **/
    @Nullable
    public ODKView getCurrentViewIfODKView() {
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
    static class EmptyView extends View {

        EmptyView(Context context) {
            super(context);
        }
    }

    private class LocationProvidersReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null
                    && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                viewModel.locationProvidersChanged();
            }
        }
    }

    private void activityDisplayed() {
        displayUIFor(viewModel.activityDisplayed());

        if (viewModel.isBackgroundLocationPermissionsCheckNeeded()) {
            new PermissionUtils().requestLocationPermissions(this, new PermissionListener() {
                @Override
                public void granted() {
                    displayUIFor(viewModel.locationPermissionsGranted());
                }

                @Override
                public void denied() {
                    viewModel.locationPermissionsDenied();
                }
            });
        }
    }

    /**
     * Displays UI representing the given background location message, if there is one.
     */
    private void displayUIFor(@Nullable BackgroundLocationManager.BackgroundLocationMessage backgroundLocationMessage) {
        if (backgroundLocationMessage == null) {
            return;
        }

        if (backgroundLocationMessage == BackgroundLocationManager.BackgroundLocationMessage.PROVIDERS_DISABLED) {
            new LocationProvidersDisabledDialog().show(getSupportFragmentManager(), LocationProvidersDisabledDialog.LOCATION_PROVIDERS_DISABLED_DIALOG_TAG);
            return;
        }

        String snackBarText;

        // Older Android versions don't have the "⋮" character. Only insert it into the text if the
        // Android version supports it. If the Android version doesn't support it, there will be a
        // double space where the placeholder is in the text resource. Collapse those spaces.
        // See https://github.com/opendatakit/collect/pull/2864
        if (backgroundLocationMessage.isMenuCharacterNeeded()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                snackBarText = String.format(getString(backgroundLocationMessage.getMessageTextResourceId()), "⋮");
            } else {
                snackBarText = String.format(getString(backgroundLocationMessage.getMessageTextResourceId()), "").replace("  ", " ");
            }
        } else {
            snackBarText = getString(backgroundLocationMessage.getMessageTextResourceId());
        }

        SnackbarUtils.showLongSnackbar(findViewById(R.id.llParent), snackBarText);
    }

    @Override
    public void widgetValueChanged(QuestionWidget changedWidget) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            // TODO: As usual, no idea if/how this is possible.
            return;
        }

        if (formController.indexIsInFieldList()) {
            updateFieldListQuestions(changedWidget.getFormEntryPrompt().getIndex());

            odkView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (!odkView.isDisplayed(changedWidget)) {
                        odkView.scrollTo(changedWidget);
                    }
                    odkView.removeOnLayoutChangeListener(this);
                }
            });
        }
    }

    /**
     * Saves the form and updates displayed widgets accordingly:
     * - removes widgets corresponding to questions that are no longer relevant
     * - adds widgets corresponding to questions that are newly-relevant
     * - removes and rebuilds widgets corresponding to questions that have changed in some way. For
     * example, the question text or hint may have updated due to a value they refer to changing.
     *
     * The widget corresponding to the {@param lastChangedIndex} is never changed.
     */
    private void updateFieldListQuestions(FormIndex lastChangedIndex) {
        // Save the user-visible state for all questions in this field-list
        FormEntryPrompt[] questionsBeforeSave = Collect.getInstance().getFormController().getQuestionPrompts();
        List<ImmutableDisplayableQuestion> immutableQuestionsBeforeSave = new ArrayList<>();
        for (FormEntryPrompt questionBeforeSave : questionsBeforeSave) {
            immutableQuestionsBeforeSave.add(new ImmutableDisplayableQuestion(questionBeforeSave));
        }

        // Re-evaluate the form with the newly-changed value and store questions in a map by FormIndex
        // to later quickly match questions that are still relevant with the corresponding question
        // before saving.
        saveAnswersForCurrentScreen(false);
        FormEntryPrompt[] questionsAfterSave = Collect.getInstance().getFormController().getQuestionPrompts();

        Map<FormIndex, FormEntryPrompt> questionsAfterSaveByIndex = new HashMap<>();
        for (FormEntryPrompt question : questionsAfterSave) {
            questionsAfterSaveByIndex.put(question.getIndex(), question);
        }

        // Identify widgets to remove or rebuild (by removing and re-adding). We'd like to do the
        // identification and removal in the same pass but removal has to be done in a loop that
        // starts from the end and itemset-based select choices will only be correctly recomputed
        // if accessed from beginning to end because the call on sameAs is what calls
        // populateDynamicChoices. See https://github.com/opendatakit/javarosa/issues/436
        List<FormEntryPrompt> questionsThatHaveNotChanged = new ArrayList<>();
        List<FormIndex> formIndexesToRemove = new ArrayList<>();
        for (ImmutableDisplayableQuestion questionBeforeSave : immutableQuestionsBeforeSave) {
            FormEntryPrompt questionAtSameFormIndex = questionsAfterSaveByIndex.get(questionBeforeSave.getFormIndex());

            // Always rebuild questions that use database-driven external data features since they
            // bypass SelectChoices stored in ImmutableDisplayableQuestion
            if (questionBeforeSave.sameAs(questionAtSameFormIndex)
                    && !getFormController().usesDatabaseExternalDataFeature(questionBeforeSave.getFormIndex())) {
                questionsThatHaveNotChanged.add(questionAtSameFormIndex);
            } else if (!lastChangedIndex.equals(questionBeforeSave.getFormIndex())) {
                formIndexesToRemove.add(questionBeforeSave.getFormIndex());
            }
        }

        for (int i = immutableQuestionsBeforeSave.size() - 1; i >= 0; i--) {
            ImmutableDisplayableQuestion questionBeforeSave = immutableQuestionsBeforeSave.get(i);

            if (formIndexesToRemove.contains(questionBeforeSave.getFormIndex())) {
                // Some widgets may call widgetValueChanged from a non-main thread but odkView can
                // only be modified from the main thread
                final int indexToRemove = i;
                runOnUiThread(() -> odkView.removeWidgetAt(indexToRemove));
            }
        }

        for (int i = 0; i < questionsAfterSave.length; i++) {
            if (!questionsThatHaveNotChanged.contains(questionsAfterSave[i])
                    && !questionsAfterSave[i].getIndex().equals(lastChangedIndex)) {
                // The values of widgets in intent groups are set by the view so widgetValueChanged
                // is never called. This means readOnlyOverride can always be set to false.
                final int targetIndex = i;
                runOnUiThread(() -> odkView.addWidgetForQuestion(questionsAfterSave[targetIndex],
                        false, targetIndex));
            }
        }
    }

    /**
     * Build {@link FormEntryViewModel} and its dependencies.
     */
    private class FormEntryViewModelFactory implements ViewModelProvider.Factory {
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.equals(FormEntryViewModel.class)) {
                GoogleLocationClient googleLocationClient = new GoogleLocationClient(Collect.getInstance().getApplicationContext());

                BackgroundLocationManager locationManager =
                        new BackgroundLocationManager(googleLocationClient, new BackgroundLocationHelper());
                return (T) new FormEntryViewModel(locationManager);
            }
            return null;
        }
    }
}

