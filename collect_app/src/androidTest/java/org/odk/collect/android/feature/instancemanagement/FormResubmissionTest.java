package org.odk.collect.android.feature.instancemanagement;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.RecordedIntentsRule;
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

    public static final String TEXT_FORM_NAME = "One Question";
    public static final String TEXT_FORM_XML = "one-question.xml";
    public static final String TEXT_QUESTION = "what is your age";
    public static final String TEXT_ANSWER = "123";

    private boolean noHttpPostResult;

    private final StubOpenRosaServer server = new StubOpenRosaServer() {
        @NonNull
        @Override
        public HttpPostResult uploadSubmissionAndFiles(@NonNull File submissionFile,
                                                       @NonNull List<File> fileList,
                                                       @NonNull URI uri,
                                                       @Nullable HttpCredentialsInterface credentials,
                                                       long contentLength) throws Exception {
            if (noHttpPostResult) {
                int timeOutMs = 1000;
                int timeOuts = 60 * 60;
                Timber.i("sleeping for %s sec", timeOutMs * timeOuts / 1000);
                for (int timeOut = 1; timeOut <= timeOuts; timeOut++) {
                    Thread.sleep(timeOutMs);
                    Timber.i("slept for %s ms", timeOut * timeOutMs);
                }
            }

            return super.uploadSubmissionAndFiles(submissionFile,
                    fileList,
                    uri,
                    credentials,
                    contentLength);
        }
    };

    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain chain = TestRuleChain.chain(new TestDependencies(server))
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(new RecordedIntentsRule())
            .around(rule);

    @Test
    public void whenFailedFormCannotBeEdited_ServerAcceptsResubmission() {
        noHttpPostResult = true;
        MainMenuPage mainMenuPage = rule.startAtMainMenu()
                .setServer(server.getURL())
                .copyForm(TEXT_FORM_XML)
                .startBlankForm(TEXT_FORM_NAME)
                .answerQuestion(TEXT_QUESTION, TEXT_ANSWER)
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickSendFinalizedForm(1)
                .clickOnForm(TEXT_FORM_NAME)
                .clickSendSelected()
//               clickOnText("CANCEL")
                .pressBack(new MainMenuPage())
                .clickViewSentForm(0)
                .assertTextDoesNotExist(TEXT_FORM_NAME)
                .pressBack(new MainMenuPage());
        noHttpPostResult = false;
        mainMenuPage
                .clickEditSavedForm(1)
                .assertTextDoesNotExist(TEXT_FORM_NAME)
                .pressBack(new MainMenuPage())
                .clickSendFinalizedForm(1)
                .clickOnForm(TEXT_FORM_NAME)
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())
                .clickViewSentForm(1)
                .assertText(TEXT_FORM_NAME);
    }

}
