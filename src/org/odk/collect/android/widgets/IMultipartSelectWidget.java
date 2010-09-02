package org.odk.collect.android.widgets;

import org.javarosa.core.model.SelectChoice;
import android.view.ViewGroup;

public interface IMultipartSelectWidget {
	/**
	 * Build the element for the indicated SelectChoice.
	 * <p>
	 * In the future, there may be other widgets with 
	 * separately laid out elements.  For those, this is 
	 * expected to throw an exception.
	 *   
	 * @param sc the select choice to be rendered
	 * @return the question widget to which can be appended additional UI elements.
	 */
	public ViewGroup buildSelectElement(SelectChoice sc);
}
