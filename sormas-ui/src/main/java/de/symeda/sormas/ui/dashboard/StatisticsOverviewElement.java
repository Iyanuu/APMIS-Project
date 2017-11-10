package de.symeda.sormas.ui.dashboard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.symeda.sormas.ui.utils.CssStyles;

@SuppressWarnings("serial")
public class StatisticsOverviewElement extends VerticalLayout {

	private Label countLabel;
	
	public StatisticsOverviewElement(String caption, String labelClass) {
		countLabel = new Label();
		CssStyles.style(countLabel, CssStyles.UPPERCASE_NORMAL, CssStyles.VSPACE_5, CssStyles.VSPACE_TOP_NONE);
		addComponent(countLabel);
		
		Label captionLabel = new Label(caption);
		CssStyles.style(captionLabel, CssStyles.UPPERCASE_SMALL, CssStyles.VSPACE_5, CssStyles.VSPACE_TOP_NONE);
		captionLabel.addStyleName(labelClass);
		addComponent(captionLabel);
		
		setComponentAlignment(countLabel, Alignment.MIDDLE_CENTER);
		setComponentAlignment(captionLabel, Alignment.MIDDLE_CENTER);
	}

	public void updateCountLabel(int count) {
		countLabel.setValue(Integer.toString(count));
	}
	
}
