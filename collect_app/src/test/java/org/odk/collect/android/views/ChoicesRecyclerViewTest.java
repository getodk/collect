package org.odk.collect.android.views;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.flexbox.FlexboxLayoutManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ChoicesRecyclerViewTest {
    private Context context;
    private ChoicesRecyclerView recyclerView;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).get();
        recyclerView = spy(new ChoicesRecyclerView(context));
    }

    @Test
    public void whenNonFLexAppearanceIsUsed_shouldGridLayoutManagerBeUsed() {
        recyclerView.initRecyclerView(getSelectOneListAdapter(1), false);
        assertThat(recyclerView.getLayoutManager().getClass().getName(), equalTo(GridLayoutManager.class.getName()));
    }

    @Test
    public void whenFLexAppearanceIsUsed_shouldFlexboxLayoutManagerBeUsed() {
        recyclerView.initRecyclerView(getSelectOneListAdapter(1), true);
        assertThat(recyclerView.getLayoutManager().getClass().getName(), equalTo(FlexboxLayoutManager.class.getName()));
    }

    @Test
    public void whenNonFLexAppearanceIsUsedWithOneColumn_shouldDividersBeAdded() {
        recyclerView.initRecyclerView(getSelectOneListAdapter(1), false);
        verify(recyclerView).enableDivider();
    }

    @Test
    public void whenNonFLexAppearanceIsUsedWithMoreThanOneColumn_shouldNotDividersBeAdded() {
        recyclerView.initRecyclerView(getSelectOneListAdapter(2), false);
        verify(recyclerView, times(0)).enableDivider();
    }

    @Test
    public void noMatterWhatAppearanceIsUsed_shouldRecyclerViewBeAdjusted() {
        recyclerView.initRecyclerView(getSelectOneListAdapter(1), false);
        verify(recyclerView).adjustRecyclerViewSize();

        recyclerView = spy(new ChoicesRecyclerView(context));
        recyclerView.initRecyclerView(getSelectOneListAdapter(1), true);
        verify(recyclerView).adjustRecyclerViewSize();
    }

    private SelectOneListAdapter getSelectOneListAdapter(int numOfColumns) {
        ChoicesRecyclerViewAdapterProps props = new ChoicesRecyclerViewAdapterProps(null, new ArrayList<>(), null,
                null, null, 0, numOfColumns, false);
        return new SelectOneListAdapter(null, null, props);
    }
}
