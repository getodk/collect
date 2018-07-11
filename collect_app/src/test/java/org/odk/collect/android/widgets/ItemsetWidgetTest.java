package org.odk.collect.android.widgets;

import android.database.Cursor;
import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.expr.XPathExpression;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.XPathParseTool;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */
public class ItemsetWidgetTest extends QuestionWidgetTest<ItemsetWidget, StringData> {

    @Mock
    XPathParseTool parseTool;

    @Mock
    XPathExpression expression;

    @Mock
    XPathNodeset nodeset;

    @Mock
    FormDef formDef;

    @Mock
    FormInstance formInstance;

    @Mock
    FormIndex formIndex;

    @Mock
    TreeReference formIndexReference;

    @Mock
    TreeElement treeElement;

    @Mock
    EvaluationContext evaluationContext;

    @Mock
    QuestionDef questionDef;

    @Mock
    File file;

    @Mock
    File itemsetFile;

    @Mock
    FileUtil fileUtil;

    @Mock
    ItemsetDbAdapter adapter;

    @Mock
    Cursor cursor;

    private Map<String, String> choices;

    @NonNull
    @Override
    public ItemsetWidget createWidget() {
        return new ItemsetWidget(RuntimeEnvironment.application, formEntryPrompt,
                false, parseTool, adapter, fileUtil);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public StringData getInitialAnswer() {
        int selectedIndex = Math.abs(random.nextInt()) % choices.size();
        String answer = choices.get(Integer.toString(selectedIndex));

        return new StringData(answer);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        choices = createChoices();
        CursorMocker cursorMocker = new CursorMocker(choices, cursor);

        when(parseTool.parseXPath(any(String.class))).thenReturn(expression);

        when(formController.getFormDef()).thenReturn(formDef);
        when(formController.getMediaFolder()).thenReturn(file);

        when(file.getAbsolutePath()).thenReturn("");

        when(fileUtil.getItemsetFile(any(String.class))).thenReturn(itemsetFile);

        when(itemsetFile.exists()).thenReturn(true);
        when(itemsetFile.getAbsolutePath()).thenReturn("");

        when(formDef.getMainInstance()).thenReturn(formInstance);
        when(formEntryPrompt.getIndex()).thenReturn(formIndex);
        when(formIndex.getReference()).thenReturn(formIndexReference);
        when(formInstance.resolveReference(formIndexReference)).thenReturn(treeElement);
        when(formDef.getEvaluationContext()).thenReturn(evaluationContext);
        when(treeElement.getRef()).thenReturn(formIndexReference);

        when(expression.eval(any(FormInstance.class), any(EvaluationContext.class))).thenReturn(nodeset);
        when(nodeset.getValAt(0)).thenReturn("");

        when(adapter.query(anyString(), anyString(), any(String[].class))).thenReturn(cursorMocker.getCursor());


        when(formEntryPrompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAdditionalAttribute(null, "query")).thenReturn("instance('cities')/root/item[state=/data/state]");
    }

    @Test
    public void getAnswerShouldReflectWhichSelectionWasMade() {
        ItemsetWidget widget = getWidget();
        assertNull(widget.getAnswer());

        int randomIndex = (Math.abs(random.nextInt()) % widget.getChoiceCount());
        widget.setChoiceSelected(randomIndex, true);

        String selectedChoice = choices.get(Integer.toString(randomIndex));

        StringData answer = (StringData) widget.getAnswer();
        assertEquals(answer.getDisplayText(), selectedChoice);
    }

    private Map<String, String> createChoices() {
        int choiceCount = (Math.abs(random.nextInt()) % 3) + 2;

        Map<String, String> choices = new HashMap<>();
        for (int i = 0; i < choiceCount; i++) {
            choices.put(Integer.toString(i), RandomString.make());
        }

        return choices;
    }

    public static class CursorMocker {
        private int cursorIndex = -1;
        private final Cursor cursor;

        CursorMocker(final Map<String, String> choices, Cursor cursor) {
            this.cursor = cursor;

            when(cursor.moveToNext()).thenAnswer(new Answer<Boolean>() {
                @Override
                public Boolean answer(InvocationOnMock invocation) throws Throwable {
                    return ++cursorIndex < choices.size();
                }
            });

            when(cursor.getColumnIndex("name")).thenAnswer(new Answer<Integer>() {
                @Override
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    return cursorIndex;
                }
            });

            when(cursor.getString(anyInt())).thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Object[] arguments = invocation.getArguments();
                    Object first = arguments[0];
                    if (first instanceof Integer) {
                        if (first.equals(-1)) {
                            return Integer.toString(cursorIndex);
                        } else {
                            return choices.get(first.toString());
                        }
                    }

                    return "";
                }
            });

            when(cursor.getColumnIndex("label")).thenReturn(-1);
            when(cursor.getColumnIndex("label::")).thenReturn(-1);
        }

        Cursor getCursor() {
            return cursor;
        }
    }
}
