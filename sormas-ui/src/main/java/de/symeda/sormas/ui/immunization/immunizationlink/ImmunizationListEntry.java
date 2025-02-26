package de.symeda.sormas.ui.immunization.immunizationlink;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.immunization.ImmunizationListEntryDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateFormatHelper;

public class ImmunizationListEntry extends HorizontalLayout {

	public static final String SEPARATOR = ": ";

	private final ImmunizationListEntryDto immunization;
	private Button editButton;

	public ImmunizationListEntry(ImmunizationListEntryDto immunization) {
		this.immunization = immunization;

		setMargin(false);
		setSpacing(true);
		setWidth(100, Unit.PERCENTAGE);
		addStyleName(CssStyles.SORMAS_LIST_ENTRY);

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth(100, Unit.PERCENTAGE);
		mainLayout.setMargin(false);
		mainLayout.setSpacing(false);
		addComponent(mainLayout);
		setExpandRatio(mainLayout, 1);

		HorizontalLayout uuidReportLayout = new HorizontalLayout();
		uuidReportLayout.setMargin(false);
		uuidReportLayout.setSpacing(true);

		Label immunizationUuidLabel = new Label(DataHelper.getShortUuid(immunization.getUuid()));
		immunizationUuidLabel.setDescription(immunization.getUuid());
		CssStyles.style(immunizationUuidLabel, CssStyles.LABEL_BOLD, CssStyles.LABEL_UPPERCASE);
		uuidReportLayout.addComponent(immunizationUuidLabel);

		Label diseaseLabel = new Label(DataHelper.toStringNullable(immunization.getDisease()));
		CssStyles.style(diseaseLabel, CssStyles.LABEL_BOLD, CssStyles.LABEL_UPPERCASE);
		uuidReportLayout.addComponent(diseaseLabel);

		uuidReportLayout.setWidthFull();
		uuidReportLayout.setComponentAlignment(immunizationUuidLabel, Alignment.MIDDLE_LEFT);
		uuidReportLayout.setComponentAlignment(diseaseLabel, Alignment.MIDDLE_RIGHT);
		mainLayout.addComponent(uuidReportLayout);

		HorizontalLayout meansOfImmunizationLayout = new HorizontalLayout();
		Label meansOfImmunizationLabel = new Label(
			I18nProperties.getPrefixCaption(ImmunizationListEntryDto.I18N_PREFIX, ImmunizationListEntryDto.MEANS_OF_IMMUNIZATION)
				+ SEPARATOR
				+ DataHelper.toStringNullable(immunization.getMeansOfImmunization()));
		meansOfImmunizationLayout.addComponent(meansOfImmunizationLabel);
		mainLayout.addComponent(meansOfImmunizationLayout);

		HorizontalLayout immunizationStatusLayout = new HorizontalLayout();
		Label immunizationStatusLabel = new Label(
			I18nProperties.getPrefixCaption(ImmunizationListEntryDto.I18N_PREFIX, ImmunizationListEntryDto.IMMUNIZATION_STATUS)
				+ SEPARATOR
				+ DataHelper.toStringNullable(immunization.getImmunizationStatus()));
		immunizationStatusLayout.addComponent(immunizationStatusLabel);
		mainLayout.addComponent(immunizationStatusLayout);

		HorizontalLayout managementStatusLayout = new HorizontalLayout();
		Label managementStatusLabel = new Label(
			I18nProperties.getPrefixCaption(ImmunizationListEntryDto.I18N_PREFIX, ImmunizationListEntryDto.MANAGEMENT_STATUS)
				+ SEPARATOR
				+ DataHelper.toStringNullable(immunization.getManagementStatus()));
		managementStatusLayout.addComponent(managementStatusLabel);
		mainLayout.addComponent(managementStatusLayout);

		HorizontalLayout immunizationPeriodLayout = new HorizontalLayout();
		Label reportDateLabel = new Label(
			I18nProperties.getPrefixCaption(ImmunizationListEntryDto.I18N_PREFIX, ImmunizationListEntryDto.IMMUNIZATION_PERIOD)
				+ SEPARATOR
				+ DateFormatHelper.buildPeriodString(immunization.getStartDate(), immunization.getEndDate()));
		immunizationPeriodLayout.addComponent(reportDateLabel);
		mainLayout.addComponent(immunizationPeriodLayout);
	}

	public void addEditListener(Button.ClickListener editClickListener) {
		if (editButton == null) {
			editButton = ButtonHelper.createIconButtonWithCaption(
				"edit-immunization-" + immunization.getUuid(),
				null,
				VaadinIcons.PENCIL,
				editClickListener,
				ValoTheme.BUTTON_LINK,
				CssStyles.BUTTON_COMPACT);

			addComponent(editButton);
			setComponentAlignment(editButton, Alignment.TOP_RIGHT);
			setExpandRatio(editButton, 0);
		}
	}

	public ImmunizationListEntryDto getImmunizationEntry() {
		return immunization;
	}
}
