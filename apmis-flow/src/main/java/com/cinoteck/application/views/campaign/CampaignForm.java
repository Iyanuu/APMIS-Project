package com.cinoteck.application.views.campaign;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.user.UserUiHelper;
import com.cinoteck.application.views.utils.DownloadFlowUtilityView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.treegrid.TreeGrid;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignLogDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDtoImpl;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;

@PageTitle("APMIS-Edit Campaign")
@Route(value = "/data")
public class CampaignForm extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 764300181578209719L;
	private static final String PRE_CAMPAIGN = "pre-campaign";
	private static final String INTRA_CAMPAIGN = "intra-campaign";
	private static final String POST_CAMPAIGN = "post-campaign";

	Button archiveDearchive = new Button(I18nProperties.getCaption(Captions.actionArchive));

	Button openCloseCampaign;
	Button duplicateCampaign;
	Button deleteCampaign;
	Button publishUnpublishCampaign;
	private Div tooltipHolder;
	Button discardChanges;
	Button saveChanges;

	Button logButton;
//	Button generateDefaultPopulation;

	Binder<CampaignIndexDto> binder = new BeanValidationBinder<>(CampaignIndexDto.class);
	Binder<CampaignDto> binderx = new BeanValidationBinder<>(CampaignDto.class);

	List<CampaignIndexDto> campaignNames, startDates, endDates, descriptions;
//	CampaignRounds rounds;
	private CampaignDto campaignDto;
	private CampaignIndexDto campaignDtox;

	H4 campaignBasics = new H4(I18nProperties.getCaption(Captions.campaignBasics));

	TextField campaignName = new TextField(I18nProperties.getCaption(Captions.Campaign_name));
	ComboBox round = new ComboBox<>(I18nProperties.getCaption(Captions.round));

	ComboBox vaccineType = new ComboBox<>(I18nProperties.getCaption("Vaccine Type"));

	DatePicker preCampaignstartDate = new DatePicker(I18nProperties.getCaption(Captions.PreCampaignStartdate));
	DatePicker preCampaignendDate = new DatePicker(I18nProperties.getCaption(Captions.PreCampaignEnddate));
	DatePicker startDate = new DatePicker(I18nProperties.getCaption(Captions.IntraCampaignStartdate));
	DatePicker endDate = new DatePicker(I18nProperties.getCaption(Captions.IntraCampaignEnddate));
	DatePicker postCampaignstartDate = new DatePicker(I18nProperties.getCaption(Captions.PostCampaignStartdate));
	DatePicker postCampaignendDate = new DatePicker(I18nProperties.getCaption(Captions.PostCampaignEnddate));

	TextField creatingUser = new TextField(I18nProperties.getCaption(Captions.Campaign_creatingUser));
	TextField creatingUuid = new TextField(I18nProperties.getCaption(Captions.uuid));
	TextField campaaignYear = new TextField(I18nProperties.getCaption(Captions.campaignYear));

	UUID uuid = UUID.randomUUID();
	TextArea description = new TextArea(I18nProperties.getCaption(Captions.description));

	public static TreeGrid<CampaignTreeGridDto> treeGrid = new TreeGrid<>();

	HorizontalLayout actionButtonsLayout = new HorizontalLayout();

	private Set<AreaReferenceDto> areass = new HashSet<>();;
	private Set<RegionReferenceDto> region = new HashSet<>();
	private Set<DistrictReferenceDto> districts = new HashSet<>();
	private Set<CommunityReferenceDto> community = new HashSet<>();
	private Set<PopulationDataDto> popopulationDataDtoSet = new HashSet<>();

	private final VerticalLayout statusChangeLayout;
	FormLayout formx;

	public Boolean isCreateForm = null;

	CampaignFormMetaReferenceDto xx;
	CampaignDto formDatac;
	boolean isArchived;
	boolean isPublished;
	boolean isOpenClose;
	boolean editMode;

	Set<CampaignFormMetaReferenceDto> selectedFormData = new HashSet<>();
	List<CampaignFormMetaReferenceDto> vvvv;
	CampaignFormGridComponent preCampaignFormGridComponent;

	CampaignFormGridComponent intraCampaignFormGridComponent;

	CampaignFormGridComponent postCampaignFormGridComponent;


	private boolean isSingleSelectClickItemLock;
	private boolean isMultiSelectItemLock;
	private boolean isSelectItemLock;

	private static final String DEFAULT_POPULATION_DATA_IMPORT_TEMPLATE_FILE_NAME = "default_population_data.csv";

	private UserProvider userProvider = new UserProvider();

	private String userLanguage = "";

	Checkbox selectDistrictCheckbox = new Checkbox();

	List<CampaignTreeGridDto> deletelist = new ArrayList<>();

	List<String> populationDataUuid = new ArrayList<>();

	boolean isClickListenerAttached;
	private boolean callbackRunning = false;
	private Timer timer;
	protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

	public CampaignForm(CampaignDto formData) {

		super();
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		this.statusChangeLayout = new VerticalLayout();
		this.formDatac = formData;

		isCreateForm = formData == null;

		statusChangeLayout.setSpacing(false);
		statusChangeLayout.setMargin(false);
		add(statusChangeLayout);

		addClassName("campaign-form");
		configureFields(formData);
		publishUnvisibleForEOC();
	}

	public void publishUnvisibleForEOC() {
		if (userProvider.getUser().getUsertype().equals(UserType.EOC_USER)) {
			publishUnpublishCampaign.setVisible(false);
		}
	}

	public LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
		return Instant.ofEpochMilli(dateToConvert.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private boolean validateDatesByPhase(String campaignphase) {
		if (campaignphase.equalsIgnoreCase("pre-campaign")) {

			LocalDate preCampaignsstartDateValue = preCampaignstartDate.getValue();
			LocalDate preCampaignsendDateValue = preCampaignendDate.getValue();

			if (preCampaignsstartDateValue == null || preCampaignsendDateValue == null) {

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : Enter a valid Pre-Campaign Start Date and End Date to continue.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();

//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
				saveChanges.setEnabled(false);
//
				return false;
			}

			if (preCampaignsstartDateValue.isAfter(preCampaignsendDateValue)) {
				preCampaignstartDate.setInvalid(true);
				preCampaignendDate.setInvalid(true);

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : Pre-Campaign End Date has to be after or on the same day as Pre-Campaign Start Date.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
				saveChanges.setEnabled(false);
//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
//			startDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");
//			endDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

				return false; // Start date is after end date
			}

			preCampaignstartDate.setInvalid(false);
			preCampaignendDate.setInvalid(false);

		} else if (campaignphase.equalsIgnoreCase("intra-campaign")) {

			LocalDate startDateValue = startDate.getValue();
			LocalDate endDateValue = endDate.getValue();

			if (startDateValue == null || endDateValue == null) {

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : Enter a valid Start Date and End Date to continue.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();

//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
				saveChanges.setEnabled(false);
//
				return false;
			}

			if (startDateValue.isAfter(endDateValue)) {
				startDate.setInvalid(true);
				endDate.setInvalid(true);

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
				saveChanges.setEnabled(false);
//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
//			startDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");
//			endDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

				return false; // Start date is after end date
			}

			startDate.setInvalid(false);
			endDate.setInvalid(false);

		} else if (campaignphase.equalsIgnoreCase("intra-campaign")) {

			LocalDate postCampaignsstartDateValue = postCampaignstartDate.getValue();
			LocalDate postCampaignsendDateValue = postCampaignendDate.getValue();

			if (postCampaignsstartDateValue == null || postCampaignsendDateValue == null) {

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : Enter a valid Post-Campaign Start Date and End Date to continue.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();

//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
				saveChanges.setEnabled(false);
//
				return false;
			}

			if (postCampaignsstartDateValue.isAfter(postCampaignsendDateValue)) {
				postCampaignstartDate.setInvalid(true);
				postCampaignendDate.setInvalid(true);

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph(
						"Please Check the Input Data : Pre-Campaign End Date has to be after or on the same day as Pre-Campaign Start Date.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
				saveChanges.setEnabled(false);
//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
//			startDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");
//			endDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

				return false; // Start date is after end date
			}

			postCampaignstartDate.setInvalid(false);
			postCampaignendDate.setInvalid(false);

		}

		// Clear invalid state if dates are valid

		saveChanges.setEnabled(true);

		return true; // Dates are valid
	}

	private void configureFields(CampaignDto formData) {

		this.campaignDto = formData;
		CampaignsView view = new CampaignsView();
		description.getStyle().set("height", "10rem");

		creatingUser.setReadOnly(true);
		creatingUuid.setReadOnly(true);
		campaaignYear.setReadOnly(true);

		UserProvider usr = new UserProvider();
		String curentUse = usr.getUserName();

		creatingUser.setWidthFull();
		creatingUuid.setWidthFull();
		campaaignYear.setWidthFull();
		
		campaignName.setWidthFull();
		round.setWidthFull();
		vaccineType.setWidthFull();

		HorizontalLayout hort = new HorizontalLayout();
		hort.add(creatingUuid, creatingUser, campaaignYear);
		hort.setJustifyContentMode(JustifyContentMode.BETWEEN);
		


		round.setItems("NID", "SNID", "CRC", "SIA", "Mopping-Up", "Training", "IPV");
		
		vaccineType.setItems("bOPV", "mOPV ","nOPV","fIPV + bOPV", "IPV + bOPV");
		
		HorizontalLayout hort2 = new HorizontalLayout();
		hort2.add(campaignName, round, vaccineType);
		hort2.setJustifyContentMode(JustifyContentMode.BETWEEN);

		if (creatingUuid.getValue() == "") {

			creatingUuid.setValue(DataHelper.createUuid());
			creatingUser.setValue(curentUse);

		}

//		System.out.println(creatingUuid.getValue() + "craeting uuid ");
		binderx.forField(creatingUuid).bind(CampaignDto.UUID);

		binderx.forField(creatingUser).bind(CampaignDto.CREATING_USER_NAME);
		binderx.forField(campaaignYear).bind(CampaignDto.CAMPAIGN_YEAR);

		binderx.forField(campaignName).asRequired(I18nProperties.getString(Strings.campaignNameRequired))
				.bind(CampaignDto.NAME);
		binderx.forField(round).asRequired(I18nProperties.getString(Strings.campaignRoundrequired))
				.bind(CampaignDto.ROUND);
		binderx.forField(vaccineType).asRequired(I18nProperties.getString("Vaccine Type"))
		.bind(CampaignDto.VACCINETYPE);

		binderx.forField(preCampaignstartDate).withConverter(new LocalDateToDateConverter())
				.bind(CampaignDto::getPreCampStartDate, CampaignDto::setPreCampStartDate);

		binderx.forField(preCampaignendDate).withConverter(new LocalDateToDateConverter())
				.bind(CampaignDto::getPreCampEndDate, CampaignDto::setPreCampEndDate);

		binderx.forField(startDate).withConverter(new LocalDateToDateConverter()).bind(CampaignDto::getStartDate,
				CampaignDto::setStartDate);

		binderx.forField(endDate).withConverter(new LocalDateToDateConverter()).bind(CampaignDto::getEndDate,
				CampaignDto::setEndDate);

		binderx.forField(postCampaignstartDate).withConverter(new LocalDateToDateConverter())
				.bind(CampaignDto::getPostCampStartDate, CampaignDto::setPostCampStartDate);

		binderx.forField(postCampaignendDate).withConverter(new LocalDateToDateConverter())
				.bind(CampaignDto::getPostCampEndDate, CampaignDto::setPostCampEndDate);

		if (formData != null) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d. M. yyyy");

			if (formData.getPreCampStartDate() != null) {
				LocalDate timestamp = formData.getPreCampStartDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				LocalDate localDatex = formData.getPreCampEndDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				String formString = timestamp.format(dateTimeFormatter);
				LocalDate localDate = LocalDate.parse(formString, dateTimeFormatter);
				preCampaignstartDate.setValue(localDate);
				preCampaignendDate.setValue(localDatex);
			}

			if (formData.getStartDate() != null) {
				LocalDate timestamp = formData.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate localDatex = formData.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				String formString = timestamp.format(dateTimeFormatter);
				LocalDate localDate = LocalDate.parse(formString, dateTimeFormatter);
				startDate.setValue(localDate);
				endDate.setValue(localDatex);
			}

			if (formData.getPostCampStartDate() != null) {
				LocalDate timestamp = formData.getPostCampStartDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				LocalDate localDatex = formData.getPostCampEndDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				String formString = timestamp.format(dateTimeFormatter);
				LocalDate localDate = LocalDate.parse(formString, dateTimeFormatter);
				postCampaignstartDate.setValue(localDate);
				postCampaignendDate.setValue(localDatex);
			}

		} 

		preCampaignstartDate.addValueChangeListener(e -> {
			LocalDate selectedDate = e.getValue();
			int selectedYear = selectedDate.getYear();
			String selectedYearAsString = Integer.toString(selectedYear);

			if (preCampaignendDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("pre-campaign");
 
			} else if (formData == null || formData != null) {
//				campaaignYear.setValue(selectedYearAsString);

				// System.out.println(selectedYearAsString + "Selected Year: " + selectedYear);
			}
		});

		startDate.addValueChangeListener(e -> {
			LocalDate selectedDate = e.getValue();
			int selectedYear = selectedDate.getYear();
			String selectedYearAsString = Integer.toString(selectedYear);

			if (endDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("intra-campaign");

				campaaignYear.setValue(selectedYearAsString);

				if (intraCampaignFormGridComponent != null) {
					intraCampaignFormGridComponent.recalculateExpiryForPhase(campaignDto);
				}
				// System.out.println(selectedYearAsString + "Selected Yearaaaaaaaaaaa: " +
				// selectedYear);

			} else if (formData == null || formData != null) {
				campaaignYear.setValue(selectedYearAsString);

				// System.out.println(selectedYearAsString + "Selected Year: " + selectedYear);
			}
		});

		postCampaignstartDate.addValueChangeListener(e -> {
			LocalDate selectedDate = e.getValue();
			int selectedYear = selectedDate.getYear();
			String selectedYearAsString = Integer.toString(selectedYear);

			if (postCampaignendDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("post-campaign");

//				campaaignYear.setValue(selectedYearAsString);
				// System.out.println(selectedYearAsString + "Selected Yearaaaaaaaaaaa: " +
				// selectedYear);

				if (postCampaignFormGridComponent != null) {
					postCampaignFormGridComponent.recalculateExpiryForPhase(campaignDto);
				}
			} else if (formData == null || formData != null) {
//				campaaignYear.setValue(selectedYearAsString);

				// System.out.println(selectedYearAsString + "Selected Year: " + selectedYear);
			}
		});

		endDate.addValueChangeListener(e -> {
			if (startDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("intra-campaign");

			}
		});

		preCampaignendDate.addValueChangeListener(e -> {
			if (preCampaignstartDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("pre-campaign");

			}
		});
		postCampaignendDate.addValueChangeListener(e -> {
			if (postCampaignstartDate.getValue() != null) {
//				validateDates();
				validateDatesByPhase("post-campaign");

			}
		});

		binderx.forField(description).asRequired(I18nProperties.getString(Strings.campaignDescriptionRequired)).bind(
				CampaignDto::getDescription,

				CampaignDto::setDescription);

		final HorizontalLayout layoutParent = new HorizontalLayout();
		layoutParent.setWidthFull();

		TabSheet tabsheetParent = new TabSheet();
		layoutParent.add(tabsheetParent);
		VerticalLayout parentTab1 = new VerticalLayout();
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();

		TabSheet tabsheet = new TabSheet();
		layout.add(tabsheet);

		userLanguage = userProvider.getUser().getLanguage().toString();

//		System.out.println(userLanguage + "User language in campaugn data ");

		VerticalLayout tab1 = new VerticalLayout();

		preCampaignFormGridComponent = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.emptyList()
						: new ArrayList<>(campaignDto.getCampaignFormMetas(PRE_CAMPAIGN)),
				FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(
						PRE_CAMPAIGN, userLanguage),
				campaignDto, PRE_CAMPAIGN);

		intraCampaignFormGridComponent = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.emptyList()
						: new ArrayList<>(campaignDto.getCampaignFormMetas(INTRA_CAMPAIGN)),
				FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(
						INTRA_CAMPAIGN, userLanguage),
				campaignDto, INTRA_CAMPAIGN);

		postCampaignFormGridComponent = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.emptyList()
						: new ArrayList<>(campaignDto.getCampaignFormMetas(POST_CAMPAIGN)),
				FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRoundAndUserLanguage(
						POST_CAMPAIGN, userLanguage),
				campaignDto, POST_CAMPAIGN);

		VerticalLayout tab2 = new VerticalLayout();

		tabsheet.add(I18nProperties.getCaption(Captions.preCampaignForms), tab1);
		// tabsheet.add(I18nProperties.getCaption(Captions.preCampaignDashboard), tab2);
		tabsheet.setWidthFull();
		parentTab1.add(layout);
		tabsheetParent.add(I18nProperties.getCaption(Captions.preCampaignPhase), parentTab1);

		VerticalLayout parentTab2 = new VerticalLayout();

		VerticalLayout parentTab3 = new VerticalLayout();

		final HorizontalLayout layoutIntra = new HorizontalLayout();
		layoutIntra.setWidthFull();

		TabSheet tabsheetIntra = new TabSheet();
		layoutIntra.add(tabsheetIntra);

		VerticalLayout tab1Intra = new VerticalLayout();

		H1 text = new H1(I18nProperties.getString(Strings.contentGoeshere));

//		this.campaignDto = compp.getModifiedDto();
		tabsheetIntra.add(I18nProperties.getCaption(Captions.intraCampaignForms), tab1Intra);
		tabsheetIntra.setWidthFull();

		VerticalLayout tab2Intra = new VerticalLayout();

//		final List<CampaignDashboardElement> intracampaignDashboardElements = FacadeProvider.getCampaignFacade()
//				.getCampaignDashboardElements(null, INTRA_CAMPAIGN);

		// tabsheetIntra.add(I18nProperties.getCaption(Captions.intraCampaignDashboard),
		// tab2Intra);
		parentTab2.add(layoutIntra);
		// parentTab2.getStyle().set("color", "green");

		tabsheetParent.add(I18nProperties.getCaption(Captions.intraCampaignPhase), parentTab2);

		final HorizontalLayout layoutPost = new HorizontalLayout();
		layoutPost.setWidthFull();

		TabSheet tabsheetPost = new TabSheet();
		layoutPost.add(tabsheetPost);

		VerticalLayout tab1Post = new VerticalLayout();

//		this.campaignDto = comppp.getModifiedDto();
		tabsheetPost.add(I18nProperties.getCaption(Captions.postCampaignForms), tab1Post);

		VerticalLayout tab2Post = new VerticalLayout();

		// tabsheetPost.add(I18nProperties.getCaption(Captions.postCampaignDashboard),
		// tab2Post);
		tabsheetPost.setWidthFull();
		parentTab3.add(layoutPost);
		tabsheetParent.add(I18nProperties.getCaption(Captions.postCampaignPhase), parentTab3);


		if (campaignDto != null) {
			tab1.add(preCampaignFormGridComponent);
			tab1Intra.add(intraCampaignFormGridComponent);
			tab1Post.add(postCampaignFormGridComponent);

//			tab2.add(comp1);
//			tab2Intra.add(compp2);
//			tab2Post.add(comppp2);
			
//			AssociateCampaign associateTab = new AssociateCampaign(formData); 
//			parentTab4.add(associateTab);

		} else {

			Div savecampaignText, savecampaignText1, savecampaignText2, savecampaignTextt, savecampaignText11,
					savecampaignText22, savecampaignTextAssoccamp;

			savecampaignText = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignText1 = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignText2 = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignTextt = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignText11 = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignText22 = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			savecampaignTextAssoccamp = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

			tab1.add(savecampaignText);
			tab2.add(savecampaignText1);
			tab1Intra.add(savecampaignText2);
			tab2Intra.add(savecampaignTextt);
			tab1Post.add(savecampaignText11);
			tab2Post.add(savecampaignText22);

		}

//		parentTab4.add(layoutAssocCamp);

		VerticalLayout parentTab5 = new VerticalLayout();
		parentTab5.setId("parentTab5");

		VerticalLayout poplayout = new VerticalLayout();
		poplayout.setSpacing(false);
		poplayout.setId("poplayout");

		Label lblIntroduction = new Label(I18nProperties.getString(Strings.infoPopulationDataView));

		poplayout.add(lblIntroduction);

		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, lblIntroduction);// .setHorizontalComponentAlignment(lblIntroduction,
		
		if (campaignDto != null) {
		Button btnGeneratePopulationData = new Button(I18nProperties.getCaption("Generate and Override Population Data | " +  campaignDto.getName()));// , e -> {
		btnGeneratePopulationData.setTooltipText("Clicking this button, Generates and Overrides Target Population for this Campaign");

		Button btnUpdatePopulationData = new Button(I18nProperties.getCaption("Merge  Population Data | " +  campaignDto.getName()));// , e -> {
		btnUpdatePopulationData.setTooltipText("Clicking this button, Updates and Merge Target Population for this Campaign");

		btnGeneratePopulationData.addClickListener(e -> {
			if (campaignDto != null) {
				Dialog genDialog = new Dialog();
				genDialog.setHeaderTitle(I18nProperties.getCaption("Generate and Override Population Data | " +  campaignDto.getName()));
				genDialog.setWidth("40%");
				
				VerticalLayout dialogLayout = new VerticalLayout();
				
//				TextField campaignName = new TextField(I18nProperties.getCaption("Campaign Name"));
//				campaignName.setValue(campaignDto.getName());
//				campaignName.setReadOnly(true);
//				campaignName.setWidthFull();
				
				Paragraph note = new Paragraph("Please select the population target group to generate and override population targets for this campaign");
				
				List<AreaReferenceDto> regions;
				regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

				MultiSelectComboBox<AreaReferenceDto> regionSelection = new MultiSelectComboBox<>("Regions");
				regionSelection.setItems(regions);
				regionSelection.setWidthFull();
				regionSelection.setClearButtonVisible(true);

				
				MultiSelectComboBox<AgeGroup> ageGroupsSelection = new MultiSelectComboBox<>("Poulation Target Categories");
				ageGroupsSelection.setItems(AgeGroup.AGE_0_4, AgeGroup.AGE_5_10, AgeGroup.AGE_4_23M, AgeGroup.AGE_4_59M);
				ageGroupsSelection.setItemLabelGenerator(item -> {
					if (item == AgeGroup.AGE_0_4) return "Target 0-59M";
					if (item == AgeGroup.AGE_5_10) return "Target 60-120M";
					if (item == AgeGroup.AGE_4_23M) return "Target 4-23M";
					if (item == AgeGroup.AGE_4_59M) return "Target 4-59M";

					return item.toString();
				});
				ageGroupsSelection.setWidthFull();
				ageGroupsSelection.setClearButtonVisible(true);

				Button selectAllBtn = new Button("Select All", event -> {
					regionSelection.setValue(regions);
					ageGroupsSelection.setValue(Set.of(AgeGroup.AGE_0_4, AgeGroup.AGE_5_10, AgeGroup.AGE_4_23M, AgeGroup.AGE_4_59M));
				});
				selectAllBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

//				HorizontalLayout selectionHeader = new HorizontalLayout(new Span(I18nProperties.getCaption("Population Categories")), selectAllBtn);
//				selectionHeader.setWidthFull();
//				selectionHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
//				selectionHeader.setAlignItems(Alignment.CENTER);

				dialogLayout.add(note, regionSelection, ageGroupsSelection, selectAllBtn);
				genDialog.add(dialogLayout);

				Button confirmBtn = new Button(I18nProperties.getCaption("Generate & Override Data"), event -> {
					if (ageGroupsSelection.getValue().isEmpty()) {
						Notification.show("Please, Select at least one Region");
						return;
					}
					
					if (ageGroupsSelection.getValue().isEmpty()) {
						Notification.show(I18nProperties.getString(Strings.infoSelectAtLeastOneCategory));
						return;
					}

					ConfirmDialog confirmGeneration = new ConfirmDialog();
					confirmGeneration.setHeader(I18nProperties.getCaption("Confirm Generation"));
					Paragraph textNote = new Paragraph("Are you sure you want to generate and override population data for the selected categories? This will overwrite existing data for this campaign.");
					confirmGeneration.add(textNote);
//					confirmGeneration.setText(I18nProperties.getString("Are you sure you want to generate population data for the selected categories? This will overwrite existing data for this campaign."));
					confirmGeneration.setCancelable(true);
					confirmGeneration.setConfirmText(I18nProperties.getCaption("Yes, Generate & Override"));
					confirmGeneration.setCancelText("No, Cancel");
					confirmGeneration.setCancelButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());
					confirmGeneration.addConfirmListener(confirmEvent -> {
						genDialog.close();

						List<AreaReferenceDto> selectedRegions = new ArrayList<>(regionSelection.getValue());
						List<AgeGroup> selectedGroups = new ArrayList<>(ageGroupsSelection.getValue());
						if (FacadeProvider.getPopulationDataFacade().generatePopulationDataForCamapignByRegionAndPopulationType(campaignDto, selectedGroups, selectedRegions)) {
							Notification.show("Population Data Generation & Override Complete For Campaign");
							
							CampaignLogDto log = new CampaignLogDto();

							// logging audit
							if (campaignDto.getUuid() != null) {
								log.setCampaign(campaignDto);
								String slectedString=""; 
								for (Iterator iterator = selectedGroups.iterator(); iterator.hasNext();) {
									AgeGroup ageGroup = (AgeGroup) iterator.next();
									String ageGroupString = ageGroup+"";
									
									
									System.out.println(ageGroupString + "ageGroupStringageGroupStringageGroupStringageGroupStringageGroupString");
									if (ageGroupString.equalsIgnoreCase("0--4")) { 
										ageGroupString = "Target 0-59M";
									}else if (ageGroupString.equalsIgnoreCase("5--10")) {
										ageGroupString = "Target 60-120M";
									}else if (ageGroupString.equalsIgnoreCase("AGE_4_23M")) { 
										ageGroupString = "Target 4-23M";
									}else if (ageGroupString.equalsIgnoreCase("AGE_4_59M")) { 
										ageGroupString = "Target 4-59M";
									}
									slectedString = slectedString + " " + ageGroupString  + " ";
	
								}
								log.setAction("Generated Population Targets Categories: " + slectedString);
							}

							FacadeProvider.getCampaignFacade().saveAuditLog(log);
							

							
						} else {
							Notification.show("Population Data Generation & Override could not be Complete For Campaign");
						}
					});
					

					confirmGeneration.open();
				});
				
				Button cancelBtn = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> genDialog.close());
//				cancelBtn.setId("erroredButton");
//				cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
//				cancelBtn.getStyle().set("color", "red");
				genDialog.getFooter().add(cancelBtn, confirmBtn);
				genDialog.open();
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.add(layoutx);
				notification.open();
			}
		});
		
		btnUpdatePopulationData.addClickListener(e -> {
			if (campaignDto != null) {
				Dialog genDialog = new Dialog();
				genDialog.setHeaderTitle(I18nProperties.getCaption("Merge Population Data | " +  campaignDto.getName()));
				genDialog.setWidth("40%");
				
				VerticalLayout dialogLayout = new VerticalLayout();

				Paragraph note = new Paragraph("Please select the population target group to merge population targets for this campaign");
				
				List<AreaReferenceDto> regions;
				regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

				MultiSelectComboBox<AreaReferenceDto> regionSelection = new MultiSelectComboBox<>("Regions");
				regionSelection.setItems(regions);
				regionSelection.setWidthFull();
				regionSelection.setClearButtonVisible(true);

				
				MultiSelectComboBox<AgeGroup> ageGroupsSelection = new MultiSelectComboBox<>("Poulation Target Categories");
				ageGroupsSelection.setItems(AgeGroup.AGE_0_4, AgeGroup.AGE_5_10, AgeGroup.AGE_4_23M, AgeGroup.AGE_4_59M);
				ageGroupsSelection.setItemLabelGenerator(item -> {
					if (item == AgeGroup.AGE_0_4) return "Target 0-59M";
					if (item == AgeGroup.AGE_5_10) return "Target 60-120M";
					if (item == AgeGroup.AGE_4_23M) return "Target 4-23M";
					if (item == AgeGroup.AGE_4_59M) return "Target 4-59M";

					return item.toString();
					
				});
				ageGroupsSelection.setWidthFull();
				ageGroupsSelection.setClearButtonVisible(true);

				Button selectAllBtn = new Button("Select All", event -> {
					regionSelection.setValue(regions);
					ageGroupsSelection.setValue(Set.of(AgeGroup.AGE_0_4, AgeGroup.AGE_5_10, AgeGroup.AGE_4_23M, AgeGroup.AGE_4_59M));
				});
				selectAllBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

//				HorizontalLayout selectionHeader = new HorizontalLayout(new Span(I18nProperties.getCaption("Population Categories")), selectAllBtn);
//				selectionHeader.setWidthFull();
//				selectionHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
//				selectionHeader.setAlignItems(Alignment.CENTER);

				dialogLayout.add(note, regionSelection, ageGroupsSelection, selectAllBtn);
				genDialog.add(dialogLayout);

				Button confirmBtn = new Button(I18nProperties.getCaption("Update Data"), event -> {
					if (ageGroupsSelection.getValue().isEmpty()) {
						Notification.show("Please, Select at least one Region");
						return;
					}
					
					if (ageGroupsSelection.getValue().isEmpty()) {
						Notification.show(I18nProperties.getString(Strings.infoSelectAtLeastOneCategory));
						return;
					}

					ConfirmDialog confirmGeneration = new ConfirmDialog();
					confirmGeneration.setHeader(I18nProperties.getCaption("Confirm Population Data Update"));
					Paragraph textNote = new Paragraph("Are you sure you want to update population data for the selected categories? This will overwrite existing data for this campaign.");
					confirmGeneration.add(textNote);
//					confirmGeneration.setText(I18nProperties.getString("Are you sure you want to generate population data for the selected categories? This will overwrite existing data for this campaign."));
					confirmGeneration.setCancelable(true);
					confirmGeneration.setConfirmText(I18nProperties.getCaption("Yes, Update Data"));
					confirmGeneration.setCancelText("No, Cancel");
					confirmGeneration.setCancelButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName());
					
					confirmGeneration.addConfirmListener(confirmEvent -> {
						genDialog.close();

						List<AreaReferenceDto> selectedRegions = new ArrayList<>(regionSelection.getValue());
						List<AgeGroup> selectedGroups = new ArrayList<>(ageGroupsSelection.getValue());
						if (FacadeProvider.getPopulationDataFacade().updatePopulationDataForCampaignByRegionAndPopulationType(campaignDto, selectedGroups, selectedRegions)) {
							Notification.show("Population Data Update Complete For Campaign");
							
							CampaignLogDto log = new CampaignLogDto();

							// logging audit
							if (campaignDto.getUuid() != null) {
								log.setCampaign(campaignDto);
								String slectedString=""; 
								for (Iterator iterator = selectedGroups.iterator(); iterator.hasNext();) {
									AgeGroup ageGroup = (AgeGroup) iterator.next();
									String ageGroupString = ageGroup+"";
									
									
									System.out.println(ageGroupString + "ageGroupStringageGroupStringageGroupStringageGroupStringageGroupString");
									if (ageGroupString.equalsIgnoreCase("0--4")) { 
										ageGroupString = "Target 0-59M";
									}else if (ageGroupString.equalsIgnoreCase("5--10")) {
										ageGroupString = "Target 60-120M";
									}else if (ageGroupString.equalsIgnoreCase("AGE_4_23M")) { 
										ageGroupString = "Target 4-23M";
									}else if (ageGroupString.equalsIgnoreCase("AGE_4_59M")) {
										ageGroupString = "Target 4-59M";
									}
	
									slectedString = slectedString + " " + ageGroupString  + " ";
	
								}
								log.setAction("Update Population Targets Categories: " + slectedString);
							}

							FacadeProvider.getCampaignFacade().saveAuditLog(log);
							
						} else {
							Notification.show("Population Data Update could not be Complete For Campaign");
						}
					});
					confirmGeneration.open();
				});
				
				Button cancelBtn = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> genDialog.close());
				genDialog.getFooter().add(cancelBtn, confirmBtn);
				genDialog.open();
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.add(layoutx);
				notification.open();
			}
		});

		poplayout.add(btnGeneratePopulationData);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnGeneratePopulationData);
		
		poplayout.add(btnUpdatePopulationData);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnUpdatePopulationData);
	}

		Button btnImport = new Button(I18nProperties.getCaption(Captions.actionImport));// , e -> {

		btnImport.addClickListener(e -> {
			if (campaignDto != null) {

				startIntervalCallback();
				UI.getCurrent().addPollListener(event -> {
					if (callbackRunning) {
						UI.getCurrent().access(this::pokeFlow);
					} else {
						stopPullers();
					}
				});

				ImportPopulationDataDialog dialog = new ImportPopulationDataDialog(InfrastructureType.POPULATION_DATA,
						campaignDto);
				dialog.open();
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.add(layoutx);
				notification.open();
			}
		});

		poplayout.add(btnImport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnImport);

		if (campaignDto != null) {
			Button btnExport = new Button(I18nProperties.getCaption(Captions.export)); // poplayout.addComponent(btnExport);
			poplayout.add(btnExport);
			poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnExport);

			StreamResource populationDataStreamResource = DownloadFlowUtilityView
					.createPopulationDataExportResource(campaignDto.getUuid());

			populationDataStreamResource.setContentType("text/csv");
			populationDataStreamResource.setCacheTime(0);

			// Create an anchor to trigger the download
			Anchor downloadAnchor = new Anchor(populationDataStreamResource,
					I18nProperties.getCaption(Captions.downloadCsv));
			downloadAnchor.getElement().setAttribute("download", true);
			downloadAnchor.getStyle().set("display", "none");

			poplayout.add(downloadAnchor);

			// Simulate a click event on the hidden anchor to trigger the download

			btnExport.addClickListener(e -> {
				downloadAnchor.getElement().callJsFunction("click");
				Notification.show("downloading...");
			});
		}
//		StreamResource populationDataExportResource = DownloadUtil.createPopulationDataExportResource();
//		new FileDownloader(populationDataExportResource).extend(btnExportDummy);
		poplayout.addClickListener(e -> {
			if (campaignDto == null) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);

				notification.add(layoutx);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.open();
			}
		});
		parentTab5.add(poplayout);
//		

		tabsheetParent.add(I18nProperties.getCaption(Captions.View_configuration_populationdata), parentTab5);
		tabsheetParent.setWidthFull();
		tabsheetParent.setId("tabsheetParent");

		openCloseCampaign = new Button();

		openCloseCampaign.setText(I18nProperties.getString(Strings.openCampaign));
		duplicateCampaign = new Button(I18nProperties.getString(Strings.duplicate));
		duplicateCampaign.addClickListener(e -> {
			duplicateCampaign();
//			updatePublishButtonText(isPublished);
		});

		deleteCampaign = new Button();
		deleteCampaign.setText(I18nProperties.getCaption(Captions.actionDelete));
		deleteCampaign.getStyle().set("background", "red");
		deleteCampaign.addClickListener(e -> {
			deleteCampaign();
		});

		logButton = new Button();
		logButton.setText(I18nProperties.getCaption(Captions.log));
		logButton.addClickListener(e -> {
			Notification.show("clicked");
			logEventMethod();
		});

		publishUnpublishCampaign = new Button();

		if (campaignDto != null) {
			isArchived = FacadeProvider.getCampaignFacade().isArchived(campaignDto.getUuid());
			isPublished = FacadeProvider.getCampaignFacade().isPublished(campaignDto.getUuid());
			isOpenClose = FacadeProvider.getCampaignFacade().isClosedd(campaignDto.getUuid());
			updateArchiveButtonText(isArchived);
			updatePublishButtonText(isPublished);
			updateOpenCloseButtonText(isOpenClose);
		} else {
			openCloseCampaign.setEnabled(false);
			archiveDearchive.setEnabled(false);
			duplicateCampaign.setEnabled(false);
			publishUnpublishCampaign.setEnabled(false);
		}

		publishUnpublishCampaign.addClickListener(e -> {
			publishUnpublish();
		});

		openCloseCampaign.addClickListener(e -> {
			openCloseCampaign();
		});

		if (isOpenClose) {
			archiveDearchive.addClickListener(e -> {
				archive();
			});
		} else {
			archiveDearchive.addClickListener(e -> {
				Notification.show("Please close present campaign before attemping to Archive")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			});
		}

		discardChanges = new Button();
		discardChanges.setText(I18nProperties.getCaption(Captions.actionDiscard));
		discardChanges.addThemeVariants(ButtonVariant.LUMO_ERROR);

		discardChanges.addClickListener(e -> {
			discard();
		});

		saveChanges = new Button();

		saveChanges.setText(I18nProperties.getCaption(Captions.actionSave));

		saveChanges.addClickListener(e -> {
			if (campaignDto != null) {
				updateCampaignDatesFromForm();
				// Recalculate expiry for all phases
				recalculateExpiryForAllPhases();
			}
			validateAndSave(editMode);
		});

		HorizontalLayout leftFloat = new HorizontalLayout();
		HorizontalLayout rightFloat = new HorizontalLayout();
		leftFloat.setJustifyContentMode(JustifyContentMode.START);

		rightFloat.setJustifyContentMode(JustifyContentMode.END);

		if (campaignDto != null) {
			leftFloat.add(archiveDearchive, openCloseCampaign, duplicateCampaign, deleteCampaign, logButton);
		} else {
			publishUnpublishCampaign.setText(I18nProperties.getString(Strings.headingPublishCampaign));
			openCloseCampaign.setText("Open Campaign");
			leftFloat.add(openCloseCampaign, logButton);
		}

		leftFloat.setWidth("50%");

		rightFloat.add(discardChanges, saveChanges);
		rightFloat.setWidth("50%");

		actionButtonsLayout.add(leftFloat, rightFloat);
		actionButtonsLayout.setWidthFull();

		FormLayout formL = new FormLayout();
		HorizontalLayout header = new HorizontalLayout();
		HorizontalLayout headerLevel2 = new HorizontalLayout();

		header.add(creatingUser, creatingUuid, campaaignYear);
		headerLevel2.add(campaignName, round, vaccineType);


		formL.add(header, headerLevel2, preCampaignstartDate, preCampaignendDate, startDate, endDate,
				postCampaignstartDate, postCampaignendDate, description);

		formL.setColspan(header, 2);
		formL.setColspan(headerLevel2, 2);
		formL.setColspan(description, 2);
		formL.setColspan(hort, 2);
		formL.setColspan(hort2, 2);
		formL.setColspan(leftFloat, 1);
		formL.setColspan(rightFloat, 1);
//		formL.setColspan(actionButtonsLayout, 2);
		round.addValueChangeListener(e -> {
			roundChange();
		});

		campaignBasics.getStyle().set("margin-top", "0px");
		campaignBasics.getStyle().set("margin-bottom", "0px");
		add(campaignBasics, formL, layoutParent, actionButtonsLayout); // ,

	}

	private void updateCampaignDatesFromForm() {
		if (campaignDto != null) {
			if (preCampaignstartDate.getValue() != null) {
				LocalDate localDate = preCampaignstartDate.getValue();
				campaignDto.setPreCampStartDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}

			if (preCampaignendDate.getValue() != null) {
				LocalDate localDate = preCampaignendDate.getValue();
				campaignDto.setPreCampEndDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}

			if (startDate.getValue() != null) {
				LocalDate localDate = startDate.getValue();
				campaignDto.setStartDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}

			if (endDate.getValue() != null) {
				LocalDate localDate = endDate.getValue();
				campaignDto.setEndDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}

			if (postCampaignstartDate.getValue() != null) {
				LocalDate localDate = postCampaignstartDate.getValue();
				campaignDto.setPostCampStartDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}

			if (postCampaignendDate.getValue() != null) {
				LocalDate localDate = postCampaignendDate.getValue();
				campaignDto.setPostCampEndDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			}
		}
	}

	private void recalculateExpiryForAllPhases() {
		// Recalculate expiry for all phases based on current date values
		if (campaignDto != null) {
			updateCampaignDatesFromForm();
			Set<CampaignFormMetaWithExpReferenceDto> allExpiryData = new HashSet<>();

			// Update the campaignDto with current date values from the form
			if (preCampaignstartDate.getValue() != null) {
				LocalDate preCampaignLocalDate = preCampaignstartDate.getValue();
				Date preCampaignStartDate = Date
						.from(preCampaignLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				campaignDto.setPreCampStartDate(preCampaignStartDate);
			}

			if (startDate.getValue() != null) {
				LocalDate intraCampaignLocalDate = startDate.getValue();
				Date intraCampaignStartDate = Date
						.from(intraCampaignLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				campaignDto.setStartDate(intraCampaignStartDate);
			}

			if (postCampaignstartDate.getValue() != null) {
				LocalDate postCampaignLocalDate = postCampaignstartDate.getValue();
				Date postCampaignStartDate = Date
						.from(postCampaignLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				campaignDto.setPostCampStartDate(postCampaignStartDate);
			}

			// Recalculate expiry for each phase
			if (preCampaignFormGridComponent != null) {
				preCampaignFormGridComponent.recalculateExpiryForPhase(campaignDto);
				allExpiryData.addAll(campaignDto.getCampaignFormMetaExpiry());

			}

			if (intraCampaignFormGridComponent != null) {
				intraCampaignFormGridComponent.recalculateExpiryForPhase(campaignDto);
				allExpiryData.addAll(campaignDto.getCampaignFormMetaExpiry());

			}

			if (postCampaignFormGridComponent != null) {
				postCampaignFormGridComponent.recalculateExpiryForPhase(campaignDto);
				allExpiryData.addAll(campaignDto.getCampaignFormMetaExpiry());

			}

			campaignDto.setCampaignFormMetaExpiryDto(allExpiryData);

			System.out.println("Total forms with expiry: " + allExpiryData.size());

		}
	}

	


	public String DateGetYear(Date dates) {

		SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
		String currentYear = getYearFormat.format(dates);
		return currentYear;
	}

	public void updateArchiveButtonText(boolean isArchived) {
		this.isArchived = isArchived;
		if (isArchived) {
			archiveDearchive.setText(I18nProperties.getCaption(Captions.actionDearchive));

		} else {

			archiveDearchive.setText(I18nProperties.getCaption(Captions.actionArchive));
		}

	}

	public void updatePublishButtonText(boolean isPublished) {
		this.isPublished = isPublished;
		if (isPublished) {
			publishUnpublishCampaign.setText(I18nProperties.getString(Strings.publish));
		} else {

			publishUnpublishCampaign.setText(I18nProperties.getString(Strings.unpublish));
		}

	}

	public void updateOpenCloseButtonText(boolean isOpenClose) {
		this.isOpenClose = isOpenClose;
		if (isOpenClose) {
			openCloseCampaign.setText(I18nProperties.getString(Strings.openCampaign));
		} else {

			openCloseCampaign.setText(I18nProperties.getString(Strings.closeCampaign));
		}

	}

	private void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	// Events
		public static abstract class CampaignFormEvent extends ComponentEvent<CampaignForm> {
		private CampaignDto campaign;

		protected CampaignFormEvent(CampaignForm source, CampaignDto campaign) {
			super(source, false);
			this.campaign = campaign;
		}

		public CampaignDto getCampaign() {
			if (campaign == null) {
				campaign = new CampaignDto();
				return campaign;
			} else {
				return campaign;

			}
		}
	}

	public void validateAndSave(boolean editMode) {
		this.editMode = editMode;
		if (formDatac == null) {

			UserReferenceDto user = new UserReferenceDto();
			UserProvider usr = new UserProvider();
			user.setUuid(usr.getUuid());
			formDatac = new CampaignDto();
			formDatac.setUuid(creatingUuid.getValue());
			formDatac.setCreatingUser(user);

			LocalDate preCampaignlocalDate = preCampaignstartDate.getValue();
			Date preCampaignstartdate = Date
					.from(preCampaignlocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate preCampaignendxDate = preCampaignendDate.getValue();
			Date preCampaignendxDatex = Date.from(preCampaignendxDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate localDate = startDate.getValue();
			Date startdate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate endxDate = endDate.getValue();
			Date endxDatex = Date.from(endxDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate postCampaignlocalDate = postCampaignstartDate.getValue();
			Date postCampaignstartdate = Date
					.from(postCampaignlocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate postCampaignendxDate = postCampaignendDate.getValue();
			Date postCampaignendxDatex = Date
					.from(postCampaignendxDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			formDatac.setCampaignYear(campaaignYear.getValue().toString());

			formDatac.setName(campaignName.getValue());
			formDatac.setRound(round.getValue().toString());

			formDatac.setPreCampStartDate(preCampaignstartdate);
			formDatac.setPreCampEndDate(preCampaignendxDatex);
			formDatac.setStartDate(startdate);
			formDatac.setEndDate(endxDatex);
			formDatac.setPostCampStartDate(postCampaignstartdate);
			formDatac.setPostCampEndDate(postCampaignendxDatex);
			formDatac.setDescription(description.getValue());
			formDatac.setCampaignStatus(formDatac.campaignStatus = "Closed");
			formDatac.setVaccineType(vaccineType.getValue().toString());

			List<CampaignDashboardElement> superList = new ArrayList<>();

			List<CampaignFormMetaReferenceDto> preCampaignFormgridData = preCampaignFormGridComponent
					.getSavedElements();

			List<CampaignFormMetaReferenceDto> intraCampaignFormgridData = intraCampaignFormGridComponent
					.getSavedElements();
			List<CampaignFormMetaReferenceDto> postCampaignFormgridData = preCampaignFormGridComponent
					.getSavedElements();

			Set<CampaignFormMetaReferenceDto> superSet = new HashSet<>();
			Set<CampaignFormMetaWithExpReferenceDto> formMetatExpirySet = formDatac.getCampaignFormMetaExpiry();

			for (CampaignFormMetaReferenceDto item : preCampaignFormgridData) {

				if (item != null) {
					superSet.add(item);
				}
			}

			// Add items from intraCampaignDashboardgridData if they are not null
			for (CampaignFormMetaReferenceDto item : intraCampaignFormgridData) {
				if (item != null) {
					superSet.add(item);
				}
			}

			// Add items from postCampaignDashboardgridData if they are not null
			for (CampaignFormMetaReferenceDto item : postCampaignFormgridData) {
				if (item != null) {
					superSet.add(item);
				}
			}

			formDatac.setCampaignDashboardElements(superList);
			formDatac.setCampaignFormMetas(superSet);
			formDatac.setCampaignFormMetaExpiryDto(formMetatExpirySet);

			fireEvent(new SaveEvent(this, formDatac));
			UI.getCurrent().getPage().reload();

			System.out.println("eskelebetiolebetetttttttthvkfvskv");

		} else {
			if (binder.validate().isOk()) {
				
				System.out.println(campaignDto.getPopulationdata() + "campaignDto.getPopulationdata()");

				System.out.println(campaignDto.getPopulationdata().size() + "campaignDto.getPopulationdata()");
for(PopulationDataDto ccc :  campaignDto.getPopulationdata()) {
	System.out.println(ccc.getCampaign_id() + "getCampaign_id()");

	System.out.println(ccc.getCommunity().getCaption() + "getCommunity().getCaption()()");
	
	System.out.println(ccc.getSelected() + "getCommunity().getCaption()()");


}
				FacadeProvider.getPopulationDataFacade().savePopulationList(campaignDto.getPopulationdata());

				Notification.show(
						String.format(I18nProperties.getString(Strings.messageCampaignSaved), campaignDto.getName()));

				fireEvent(new SaveEvent(this, binderx.getBean()));

				UI.getCurrent().getPage().reload();

//				Notification.show(I18nProperties.getString(Strings.headingUploadSuccess) + "!");

			} else {
				Notification.show(I18nProperties.getString(Strings.errorCampaignForm));
			}
		}
	}

	private void archive() {
//		updateArchiveButtonText(isArchived);
//		UI.getCurrent().getPage().reload();
		try {
			fireEvent(new ArchiveEvent(this, binderx.getBean()));
		} finally {
//			isArchived = FacadeProvider.getCampaignFacade().isArchived(campaignDto.getUuid());
//			updateArchiveButtonText(isArchived);
		}

	}

	private void publishUnpublish() {
		fireEvent(new PublishUnpublishEvent(this, binderx.getBean()));
	}

	private void logEventMethod() {
		fireEvent(new LogCampaignEvent(this, binderx.getBean()));
	}

	private void deleteCampaign() {

		fireEvent(new DeleteEvent(this, binderx.getBean()));
//		updatePublishButtonText(isArchived);
//		UI.getCurrent().getPage().reload();

	}

	private void duplicateCampaign() {

		fireEvent(new DuplicateEvent(this, binderx.getBean()));
//		updatePublishButtonText(isArchived);
//		UI.getCurrent().getPage().reload();

	}

	private void openCloseCampaign() {

		fireEvent(new OpenCloseEvent(this, binderx.getBean()));

	}

	private void roundChange() {

		fireEvent(new RoundChangeEvent(this, binderx.getBean()));

	}

	private void discard() {

		fireEvent(new DiscardEvent(this));

	}

	public void setCampaign(CampaignDto campaignIndexDto) {
		// TODO Auto-generated method stub
		binderx.setBean(campaignIndexDto);
	}

	public static class SaveEvent extends CampaignFormEvent {
		SaveEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class RoundChangeEvent extends CampaignFormEvent {
		RoundChangeEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public class ArchiveEvent extends CampaignFormEvent {
		ArchiveEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);

		}
	}

	public static class PublishUnpublishEvent extends CampaignFormEvent {
		PublishUnpublishEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class LogCampaignEvent extends CampaignFormEvent {
		LogCampaignEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class OpenCloseEvent extends CampaignFormEvent {
		OpenCloseEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class DeleteEvent extends CampaignFormEvent {
		DeleteEvent(CampaignForm source, CampaignDto contact) {
			super(source, contact);
		}

	}

	public static class DuplicateEvent extends CampaignFormEvent {
		DuplicateEvent(CampaignForm source, CampaignDto contact) {
			super(source, contact);
		}

	}

	public static class CloseEvent extends CampaignFormEvent {
		CloseEvent(CampaignForm source) {
			super(source, null);
		}
	}

	public static class DiscardEvent extends CampaignFormEvent {
		DiscardEvent(CampaignForm source) {
			super(source, null);
		}
	}

	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);
	}

	public Registration addDuplicateListener(ComponentEventListener<DuplicateEvent> listener) {
		return addListener(DuplicateEvent.class, listener);
	}

	public Registration addArchiveListener(ComponentEventListener<ArchiveEvent> listener) {
		return addListener(ArchiveEvent.class, listener);
	}

	public Registration addPublishListener(ComponentEventListener<PublishUnpublishEvent> listener) {
		return addListener(PublishUnpublishEvent.class, listener);
	}

	public Registration addLogListener(ComponentEventListener<LogCampaignEvent> listener) {
		return addListener(LogCampaignEvent.class, listener);
	}

	public Registration addOpenCloseListener(ComponentEventListener<OpenCloseEvent> listener) {
		return addListener(OpenCloseEvent.class, listener);
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}

	public Registration addRoundChangeListener(ComponentEventListener<RoundChangeEvent> listener) {
		return addListener(RoundChangeEvent.class, listener);
	}

	public Registration addDiscardListener(ComponentEventListener<DiscardEvent> listener) {
		return addListener(DiscardEvent.class, listener);
	}

	private List<CampaignDashboardElement> getListDashboardFromType(String phaseTy) {
		final List<CampaignDiagramDefinitionDto> allDiagram = FacadeProvider.getCampaignDiagramDefinitionFacade()
				.getAll();

		List<CampaignDashboardElement> elements = new ArrayList<>();
		final List<CampaignDiagramDefinitionDto> filterList = allDiagram.stream()
				.filter(e -> e.getFormType().equalsIgnoreCase(phaseTy)).collect(Collectors.toList());

		for (CampaignDiagramDefinitionDto lsiter : filterList) {
			CampaignDashboardElement emptyElements = new CampaignDashboardElement();
			emptyElements.setDiagramId(lsiter.getDiagramId());
			emptyElements.setPhase(phaseTy);
			elements.add(emptyElements);

		}
		return elements;

	}

	private void pokeFlow() {
		logger.debug("runingImport...");
	}

	private void startIntervalCallback() {
		UI.getCurrent().setPollInterval(5000);
		if (!callbackRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
//					stopIntervalCallback();
				}
			}, 15000); // 10 minutes

			callbackRunning = true;
		}
	}

	private void stopIntervalCallback() {
		if (callbackRunning) {
			callbackRunning = false;
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}

		}

	}

	private void stopPullers() {
		UI.getCurrent().setPollInterval(-1);
	}

}