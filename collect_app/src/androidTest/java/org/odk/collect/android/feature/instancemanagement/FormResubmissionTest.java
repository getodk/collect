package org.odk.collect.android.feature.instancemanagement;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.RecordedIntentsRule;
import org.odk.collect.android.dao.CursorLoaderFactory;
import org.odk.collect.android.openrosa.HttpCredentialsInterface;
import org.odk.collect.android.openrosa.HttpPostResult;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SendFinalizedFormPage;

import java.io.File;
import java.net.URI;
import java.util.List;

import timber.log.Timber;

@RunWith(AndroidJUnit4.class)
public class FormResubmissionTest {

    public static final String _FORM_NAME = "One Question";
    public static final String _FORM_XML = "one-question.xml";
    public static final String _QUESTION = "what is your age";
    public static final String _ANSWER = "123";

    private boolean noHttpPostResult;
    private boolean rejectResubmission;
    private File submissionFile;

    private final StubOpenRosaServer server = new StubOpenRosaServer() {
        @NonNull
        @Override
        public HttpPostResult uploadSubmissionAndFiles(@NonNull File submissionFile,
                                                       @NonNull List<File> fileList,
                                                       @NonNull URI uri,
                                                       @Nullable HttpCredentialsInterface credentials,
                                                      long contentLength) throws Exception {
            if(noHttpPostResult||rejectResubmission){
                boolean doReject = handleFlag(submissionFile);
                if(doReject){
                    return new HttpPostResult("", 500, "Resubmission not permitted for " + submissionFile.getName());
                }
            }

            return super.uploadSubmissionAndFiles(submissionFile,
                    fileList,
                    uri,
                    credentials,
                    contentLength);
        }
    };

    private boolean handleFlag(@NotNull File submissionFile) throws InterruptedException {
        if (noHttpPostResult) {
            this.submissionFile = submissionFile;
            int timeOutMs = 1000;
            int timeOuts = 60*60;
            Timber.i("sleeping for %s sec", timeOutMs * timeOuts / 1000);
            for (int timeOut = 1; timeOut <= timeOuts; timeOut++) {
                Thread.sleep(timeOutMs);
                Timber.i("slept for %s ms", timeOut * timeOutMs);
            }
        } else{
            return rejectResubmission
                    && this.submissionFile.equals(submissionFile) ;
        }
        return false;
    }

    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain chain = TestRuleChain.chain(new TestDependencies(server))
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(new RecordedIntentsRule())
            .around(rule);

    private MainMenuPage createAndSubmitFormWithFailure() {
        noHttpPostResult=true;
        return rule.startAtMainMenu()
                .setServer(server.getURL())
                .copyForm(_FORM_XML)
                .startBlankForm(_FORM_NAME)
                .answerQuestion(_QUESTION, _ANSWER)
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickSendFinalizedForm(1)
                .clickOnForm(_FORM_NAME)
                .clickSendSelected()
//               clickOnText("CANCEL")
                .pressBack(new MainMenuPage())
                .clickViewSentForm(0)
                .assertTextDoesNotExist(_FORM_NAME)
                .pressBack(new MainMenuPage());
    }

    @Test
    public void whenFailedFormCanBeEdited_ServerRejectsResubmission() {
        CursorLoaderFactory.beforeUpdate = true;
        rejectResubmission=true;
        MainMenuPage mainMenuPage = createAndSubmitFormWithFailure();
        noHttpPostResult=false;
        mainMenuPage
                .clickEditSavedForm(1)
                .clickOnForm(_FORM_NAME)
                .clickOnQuestion(_QUESTION)
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickSendFinalizedForm(1)
                .clickOnForm(_FORM_NAME)
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())
                .clickViewSentForm(0)
                .assertTextDoesNotExist(_FORM_NAME);
    }

    @Test
    public void whenFailedFormCannotBeEdited_ServerAcceptsResubmission() {
        CursorLoaderFactory.beforeUpdate = false;
        rejectResubmission=false;
        MainMenuPage mainMenuPage = createAndSubmitFormWithFailure();
        noHttpPostResult=false;
        mainMenuPage
                .clickEditSavedForm(1)
                .assertTextDoesNotExist(_FORM_NAME)
                .pressBack(new MainMenuPage())
                .clickSendFinalizedForm(1)
                .clickOnForm(_FORM_NAME)
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())
                .clickViewSentForm(1)
                .assertText(_FORM_NAME);
    }

}
