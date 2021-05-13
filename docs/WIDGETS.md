# Question Widgets

Collect forms are defined using [XForms](https://getodk.github.io/xforms-spec/) which in the most simple sense are just a list of questions (which can be of various [types](https://xlsform.org/en/#question-types)). To render the form and let the enumerator fill it out Collect needs to be able to deal with each of these different question types. To do this Collect has a series of different `QuestionWidget` implementations - usually one for each type of question. The exact mapping between question types and widgets happens in `WidgetFactory`.

## Implementing widgets

The `TriggerWiget` will be used here as an example of how to implement a widget. The `TriggerWidget` represents the `acknowledge` question type (its docs are [here](https://docs.getodk.org/form-question-types/#trigger-acknowledge-widget)).

```java
public class TriggerWidget extends QuestionWidget {

    private AppCompatCheckBox triggerButton;

    public TriggerWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerTextSize) {
        ViewGroup answerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.trigger_widget_answer, null);

        triggerButton = answerView.findViewById(R.id.check_box);
        triggerButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerTextSize);
        triggerButton.setEnabled(!prompt.isReadOnly());
        triggerButton.setChecked(prompt.getAnswerText().equals("OK"));
        triggerButton.setOnCheckedChangeListener((buttonView, isChecked) -> widgetValueChanged());

        return answerView;
    }
    
    @Override
    public IAnswerData getAnswer() {
        return triggerButton.isChecked() ? new StringData(OK_TEXT) : null;
    }

    @Override
    public void clearAnswer() {
        triggerButton.setChecked(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        triggerButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        triggerButton.cancelLongPress();
    }
}
```

To create your own widget your class needs to override several methods:

* `onCreateAnswerView` - Returns the `View` object that represents the interface for answering the question. This will be rendered underneath the question's `label`, `hint` and `guidance_hint`. This method is passed the question itself (as a `FormEntryPrompt`) which will often be needed in rendering the widget. It is also passed the size to be used for question text.
* `getAnswer` - Returns the current answer for the question. Can be `null` if the question has not been answered yet.
* `clearAnswer` - Called when the answer for this question needs to be cleared for some reason. The implementation of this method should reset the UI of the widget.
* `setOnLongClickListener` - Used to make sure clickable views in the widget work with the long click feature (shows the "Edit Prompt" menu). The passed listener should be set as the long click listener on clickable views in the widget.
* `cancelLongPress` - As above this is used to make sure long click features work. The `cancelLongPress` call should simply be forwarded to clickable views in the widget.

As you'll see from the example you need to call `widgetValueChanged` whenever the answer is changed in someway. This will make sure that any listeners attached to the widget will be called.

### Handling rotation/configuration changes

When the device is rotated during form entry, the on screen answers will be saved. Then, when it is is recreated, the `Widget` will be passed the correct answers in with `FormEntryPrompt` meaning no extra work is required to handle configuration changes. If a widget's answer layout contains any editable views (like an `EditText` for example) they should include `android:saveEnabled="false"` to prevent Android from trying to load previous values in.

## Testing widgets

Widgets should have the majority of their behavior driven out by tests that treat them as an individual component. This means higher level feature tests for Collect won't have to be as concerned around the many types of question that exist and can be more focused on form entry, form management, settings etc as a whole.

You can use [Robolectric](https://robolectric.org) to write tests for widgets without having to run them on device or manually mock out the Android SDK:

```java
@RunWith(AndroidJUnit4.class)
public class TriggerWidgetTest {

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));
        assertThat(widget.getAnswer().getDisplayText(), equalTo("OK"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));

        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void usingReadOnlyOption_makesAllClickableElementsDisabled() {
        TriggerWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getCheckBox().getVisibility(), equalTo(View.VISIBLE));
        assertThat(widget.getCheckBox().isEnabled(), equalTo(Boolean.FALSE));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveAnswer_checkboxIsUnchecked() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getCheckBox().isChecked(), equalTo(false));
    }

    @Test
    public void whenPromptHasAnswer_checkboxIsChecked() {
        TriggerWidget widget = createWidget(promptWithAnswer(new StringData("OK")));
        assertThat(widget.getCheckBox().isChecked(), equalTo(true));
    }

    @Test
    public void checkingCheckbox_setsAnswer() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        CheckBox triggerButton = widget.getCheckBox();

        triggerButton.setChecked(true);
        assertThat(widget.getAnswer().getDisplayText(), equalTo("OK"));

        triggerButton.setChecked(false);
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void checkingCheckbox_callsValueChangeListeners() {
        TriggerWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        CheckBox triggerButton = widget.getCheckBox();

        triggerButton.setChecked(true);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    private TriggerWidget createWidget(FormEntryPrompt prompt) {
        return new TriggerWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
```

This example makes sure that the widget interface methods (`getAnswer`, `clearAnswer` etc) all behave as expected and also checks that the widget looks and behaves correctly. Widget test helpers make these tests easier to write and can be in `QuestionWidgetHelpers`.
