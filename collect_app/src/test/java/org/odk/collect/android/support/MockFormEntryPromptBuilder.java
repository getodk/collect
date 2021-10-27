package org.odk.collect.android.support;

import androidx.core.util.Pair;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockFormEntryPromptBuilder {

    private final FormEntryPrompt prompt;

    public MockFormEntryPromptBuilder() {
        this(mock(FormEntryPrompt.class));
    }

    public MockFormEntryPromptBuilder(FormEntryPrompt prompt) {
        this.prompt = prompt;
        when(prompt.getIndex()).thenReturn(mock(FormIndex.class));
        when(prompt.getIndex().toString()).thenReturn("0, 0");
        when(prompt.getFormElement()).thenReturn(mock(IFormElement.class));

        // Make sure we have a non-null question
        withQuestion(mock(QuestionDef.class));
    }

    public MockFormEntryPromptBuilder withIndex(String index) {
        when(prompt.getIndex().toString()).thenReturn(index);
        return this;
    }

    public MockFormEntryPromptBuilder withReadOnly(boolean readOnly) {
        when(prompt.isReadOnly()).thenReturn(readOnly);
        return this;
    }

    public MockFormEntryPromptBuilder withAudioURI(String audioURI) {
        when(prompt.getAudioText()).thenReturn(audioURI);
        return this;
    }

    public MockFormEntryPromptBuilder withImageURI(String imageURI) {
        when(prompt.getImageText()).thenReturn(imageURI);
        return this;
    }

    public MockFormEntryPromptBuilder withAdditionalAttribute(String name, String value) {
        when(prompt.getFormElement().getAdditionalAttribute(null, name)).thenReturn(value);
        return this;
    }

    public MockFormEntryPromptBuilder withSelectChoices(List<SelectChoice> choices) {
        for (int i = 0; i < choices.size(); i++) {
            choices.get(i).setIndex(i);
            when(prompt.getSelectChoiceText(choices.get(i))).thenReturn(choices.get(i).getValue());
        }

        when(prompt.getSelectChoices()).thenReturn(choices);
        return this;
    }

    public MockFormEntryPromptBuilder withSpecialFormSelectChoiceText(List<Pair<String, String>> formAndTexts) {
        for (int i = 0; i < prompt.getSelectChoices().size(); i++) {
            when(prompt.getSpecialFormSelectChoiceText(prompt.getSelectChoices().get(i), formAndTexts.get(i).first)).thenReturn(formAndTexts.get(i).second);
        }

        return this;
    }

    public MockFormEntryPromptBuilder withControlType(int controlType) {
        when(prompt.getControlType()).thenReturn(controlType);
        return this;
    }

    public FormEntryPrompt build() {
        return prompt;
    }

    public MockFormEntryPromptBuilder withAppearance(String appearance) {
        when(prompt.getAppearanceHint()).thenReturn(appearance);
        return this;
    }

    public MockFormEntryPromptBuilder withAnswerDisplayText(String text) {
        IAnswerData answer = mock(IAnswerData.class);
        when(answer.getDisplayText()).thenReturn(text);
        when(prompt.getAnswerText()).thenReturn(text);
        when(prompt.getAnswerValue()).thenReturn(answer);

        return this;
    }

    public MockFormEntryPromptBuilder withAnswer(IAnswerData answer) {
        when(prompt.getAnswerValue()).thenReturn(answer);
        when(prompt.getAnswerText()).thenCallRealMethod();

        return this;
    }

    public MockFormEntryPromptBuilder withQuestion(QuestionDef questionDef) {
        when(prompt.getQuestion()).thenReturn(questionDef);
        return this;
    }

    public MockFormEntryPromptBuilder withBindAttribute(String namespace, String name, String value) {
        TreeElement treeElement = TreeElement.constructAttributeElement(namespace, name, value);
        treeElement.setValue(new StringData(value));

        when(prompt.getBindAttributes()).thenReturn(asList(treeElement));

        return this;
    }
}
