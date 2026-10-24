package com.cinoteck.application.messaging;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.google.api.client.util.Value;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.FCMDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.messaging.MessageScheduleDto;
import de.symeda.sormas.api.messaging.MessageTemplateDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@Route(layout = MessagingView.class)
public class SentMessageView extends VerticalLayout implements RouterLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1903665073038797749L;

	private Button hideFilters;
	private HorizontalLayout filterLayout;
	private TextField search;
	private ComboBox<UserType> userType;
	private ComboBox<UserRole> userRole;
	private ComboBox<AreaReferenceDto> areaFilter;
	private ComboBox<RegionReferenceDto> regionFilter;
	private ComboBox<DistrictReferenceDto> districtFilter;
	private ComboBox<FormAccess> formAccessFilter;
	private MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>();
	private ComboBox<FormAccess> status;

	private List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	private List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	private List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();

	private Button newMessage;
	private Button bulkModeButton;
	private Button leaveBulkModeButton;

	private MessageDto messageDto;
	private MessageTemplateDto messageTemplateDto;
	private MessageScheduleDto messageScheduleDto;

	private UserProvider userProvider = new UserProvider();
	private HorizontalLayout buttonLayout = new HorizontalLayout();
	private HorizontalLayout filters = new HorizontalLayout();

	private Grid<MessageDto> grid = new Grid<>(MessageDto.class, false);
	private GridListDataView<MessageDto> dataView;

	private MessagingDataProvider messagingDataProvider = new MessagingDataProvider();
	private ConfigurableFilterDataProvider<MessageDto, Void, MessageCriteria> filterDataProvider;

	private MessageCriteria criteria = new MessageCriteria();

	private StringBuilder baseTopic = new StringBuilder();
	private FCMDto fcmDto = new FCMDto();
	private List<MessageScheduleDto> scheduledMessageList = FacadeProvider.getMessageFacade()
			.getIndexListMessageSchedule(null, 0, 100, null);

	@Value("${fcm.secret.key}")
	private String fcmSecretKey;
	private Paragraph countRowItems;

	private Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	private String exportFileName;
	private GridExporter<MessageDto> exporter;
	private Icon icon = VaadinIcon.UPLOAD_ALT.create();
	private Button exportButton;
	private Button scheduleMessageButton;
	private Button scheduledMessages;
	private Button scheduleMessageClose;
	
	public SentMessageView() {

		this.setSizeFull();
		this.setHeightFull();
		this.setWidthFull();
		this.addClassName("notificationview");

		filterDataProvider = messagingDataProvider.withConfigurableFilter();

		exportButton = new Button(I18nProperties.getCaption(Captions.export));

		exportButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportButton.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});
		anchor.getStyle().set("display", "none");
		
		scheduleMessageButton = new Button("Schedule a Message");
		scheduleMessageButton.setIcon(new Icon(VaadinIcon.ARROW_FORWARD));
		
		scheduledMessages = new Button("Scheduled Messages");
		scheduledMessages.setIcon(new Icon(VaadinIcon.ORIENTATION));
		
		scheduleMessageClose = new Button("Scheduled Messages");
		scheduleMessageClose.setIcon(new Icon(VaadinIcon.CLOSE));
		
		configureView();
		configureGrid();

		buttonLayout.getStyle().set("margin-left", "10px");
		buttonLayout.setAlignItems(Alignment.END);
		buttonLayout.add(newMessage, exportButton, anchor, scheduleMessageButton, scheduledMessages);

		filters.getStyle().set("margin-left", "10px");
		filters.setAlignItems(Alignment.END);
		filterLayout.add(search, userRole, formAccessFilter, areaFilter, regionFilter, districtFilter, countRowItems);
		filterLayout.setAlignItems(Alignment.START);
		filters.add(hideFilters, filterLayout);
		add(buttonLayout, filters, grid);

	}

	public void configureView() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + numberOfRows);
		countRowItems.setId("rowCount");
		countRowItems.getStyle().set("margin-top", "60px");
//		countRowItems.getStyle().set("padding-left", "150px");
		countRowItems.getStyle().set("margin-right", "20px");
		countRowItems.getStyle().set("margin-left", "auto");

		filterLayout = new HorizontalLayout();
		filterLayout.getStyle().set("margin-top", "10px");
		hideFilters = new Button("Hide Filters", new Icon(VaadinIcon.SLIDERS));
		hideFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				hideFilters.setText("Hide Filter");
			} else {
				filterLayout.setVisible(false);
				hideFilters.setText("Show Filters");
			}
		});

		search = new TextField("Search");
		search.setClearButtonVisible(true);

		search.setValueChangeMode(ValueChangeMode.EAGER);
		search.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.freeText(e.getValue().toString());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.freeText(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		userType = new ComboBox<>("User Type");
		userType.setItems(UserType.values());
		userType.setClearButtonVisible(true);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		List<UserRole> rolesz = new ArrayList<>(roles);
		roles.remove(UserRole.BAG_USER);
		roles.remove(UserRole.ADMIN);
		Set<UserRole> sortedUserRoless = new TreeSet<>(rolesz);
		userRole = new ComboBox<>("User Roles");
		userRole.setItems(sortedUserRoless);
		userRole.setClearButtonVisible(true);

		userRole.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.userRole(e.getValue());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.userRole(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		formAccessFilter = new ComboBox<FormAccess>("Form Access");
		formAccessFilter.setItems(Arrays.stream(FormAccess.values())
                .filter(value -> value != FormAccess.MODALITY_PRE  && value != FormAccess.MODALITY_POST && value != FormAccess.EAG)
                .collect(Collectors.toList()));
		formAccessFilter.setClearButtonVisible(true);
		formAccessFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.formAccess(e.getValue());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.formAccess(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId("");
		areaFilter.setWidth("145px");

		areaFilter.setLabel(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.AREA));
		areaFilter.setPlaceholder(I18nProperties.getCaption(Captions.area));
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			areaFilter.setItems(regions);
		}

		areaFilter.setClearButtonVisible(true);
		areaFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				AreaReferenceDto area = e.getValue();
				regionFilter.clear();
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					regionFilter.setItems(
							FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					regionFilter
							.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid()));
				} else {
					regionFilter.setItems(provinces);
				}

				criteria.area(area);
				regionFilter.setReadOnly(false);
				districtFilter.clear();
				districtFilter.setReadOnly(true);
				criteria.region(null);
				criteria.district(null);
			} else {
				regionFilter.clear();
				regionFilter.setReadOnly(true);
				criteria.area(null);

			}
			filterDataProvider.setFilter(criteria);
			updateRowCount();

		});

		regionFilter = new ComboBox<RegionReferenceDto>();
		regionFilter.setId(CaseDataDto.REGION);
		regionFilter.setWidth(145, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.region));
		regionFilter.setClearButtonVisible(true);

		regionFilter.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				RegionReferenceDto region = e.getValue();
				districtFilter.clear();

				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid()));
				} else {
					districtFilter.setItems(districts);
				}

				criteria.region(region);
				districtFilter.setReadOnly(false);
				criteria.district(null);
			} else {
				districtFilter.clear();
				districtFilter.setReadOnly(true);
				criteria.region(null);

			}
			filterDataProvider.setFilter(criteria);
			updateRowCount();

		});

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(145, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districtFilter.setClearButtonVisible(true);
		districtFilter.setReadOnly(true);

		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				community.setItems(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid()));
				criteria.district(district);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			} else {
				criteria.district(null);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			}
			updateRowCount();
		});

		newMessage = new Button("New Message", new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newMessage.getStyle().set("margin-top", "10px");

		bulkModeButton = new Button("Enter Bulk Edit Mode");
		bulkModeButton.addClickListener(e -> {

		});

		newMessage.addClickListener(e -> {

			messageDto = new MessageDto();
			newMessage(messageDto);
		});
		
		scheduleMessageButton.addClickListener(e -> {
			messageScheduleDto = new MessageScheduleDto();
			scheduleMessage(messageScheduleDto);
		});
		
		scheduledMessages.addClickListener(e -> {

			if(scheduledMessageList.size() > 0) {
				scheduledMessage();
			} else {
				com.vaadin.flow.component.notification.Notification notification = new com.vaadin.flow.component.notification.Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("There is no existing Scheduled Message(s)");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();				
			}
		});
	}

	private String formAccessConfig(MessageDto messageDto) {
		String value = messageDto.getFormAccess().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String rolesConfig(MessageDto messageDto) {
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		String value = messageDto.getUserRoles().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String areaConfig(MessageDto messageDto) {
		String value = messageDto.getArea().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String regionConfig(MessageDto messageDto) {
		String value = messageDto.getRegion().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String districtConfig(MessageDto messageDto) {
		String value = messageDto.getDistrict().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String communityConfig(MessageDto messageDto) {
		String value = messageDto.getCommunity().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	public void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(MessageDto::getTitle).setHeader("Message Subject").setSortable(false).setResizable(true);
		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message Content").setSortable(false).setResizable(true);
		grid.addColumn(this::rolesConfig).setHeader("User Roles").setSortable(false).setResizable(true);
		grid.addColumn(this::areaConfig).setHeader("Region").setSortable(false).setResizable(true);
		grid.addColumn(this::regionConfig).setHeader("Province").setSortable(false).setResizable(true);
		grid.addColumn(this::districtConfig).setHeader("District").setSortable(false).setResizable(true);
		grid.addColumn(this::communityConfig).setHeader("Cluster").setSortable(false).setResizable(true);
		grid.addColumn(this::formAccessConfig).setHeader("Form Access").setSortable(false).setResizable(true);
		grid.addColumn(MessageDto.CREATED_BY).setHeader("Created By").setSortable(false).setResizable(true);
		grid.addColumn(MessageDto.CHG_DATE).setHeader("Broadcast Date").setSortable(false).setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);

		grid.setDataProvider(filterDataProvider);
		if (userProvider.hasUserRight(UserRight.CAMPAIGN_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> editMessage(event.getValue()));
		}

		exporter = GridExporter.createFor(grid);

//		exporter.setExportValue(userActionDateColumn, dto -> {
//			Date timestamp = dto.getActionDate();
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//			return dateFormat.format(timestamp);
//		});
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Export");

		exportFileName = "Message_Export_"

				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
		exporter.setFileName(exportFileName);
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");
		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");
		anchor.getElement().insertChild(0, icon.getElement());

	}

	public void newMessage(MessageDto messageDto) {

		MessagingLayout messagingLayout = new MessagingLayout(messageDto, true);
		messagingLayout.setMessage(messageDto);

		messagingLayout.addSaveListener(event -> {
			try {
				saveMessage(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Dialog dialog = new Dialog();
		
		Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		dialog.getHeader().add(closeButton);
		
		dialog.add(closeButton);
		dialog.add(messagingLayout);
		dialog.setHeaderTitle("New Message");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(true);
		dialog.setModal(true);
		dialog.setClassName("new-message");
				
	}

	public void editMessage(MessageDto messageDto) {

		MessagingLayout messagingLayout = new MessagingLayout(messageDto, false);
		messagingLayout.setMessage(messageDto);

		messagingLayout.addSaveListener(event -> {
			try {
				saveMessage(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Dialog dialog = new Dialog();
		
		Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		dialog.getHeader().add(closeButton);
		
		dialog.add(closeButton);
		dialog.add(messagingLayout);
		dialog.setHeaderTitle("Edit Message");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(true);
		dialog.setModal(true);
		dialog.setClassName("edit-message");
		
	}
	
	public void scheduleMessage(MessageScheduleDto messageScheduleDto) {

		ScheduleMessageLayout scheduleMessageLayout = new ScheduleMessageLayout(messageScheduleDto, true);
		scheduleMessageLayout.setScheduleMessage(messageScheduleDto);
		scheduleMessageLayout.addSaveListener(event -> {
			try {
				saveMessages(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		Dialog dialog = new Dialog();
		dialog.add(scheduleMessageLayout);
		dialog.setHeaderTitle("Schedule a Message");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("schedule-message");
	}
	
	public void scheduledMessage() {
		Dialog dialog = new Dialog();
		ScheduledMessageLayout scheduleMessageLayout = new ScheduledMessageLayout();
					
		dialog.add(scheduleMessageLayout);
		dialog.setHeaderTitle("Scheduled Messages");
		dialog.setHeight("500px");
		dialog.setWidth("1200px");
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("schedule-message");
		dialog.open();
		scheduleMessageClose.addClickListener(e -> {dialog.close();});
		dialog.getFooter().add(scheduleMessageClose);
	}

//	public void sendFcmSdk(MessageDto messageDto) throws IOException {
//
//		System.out.println("SENFCMSDKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
//		List<String> tokens = FacadeProvider.getUserFacade().getUserForFCM(messageDto.getFormAccess(),
//				messageDto.getArea(), messageDto.getRegion(), messageDto.getDistrict(), messageDto.getCommunity());
//
//		tokens = tokens.stream().filter(element -> element != null).collect(Collectors.toList());
//
//		Set<String> mySet = new HashSet<>(tokens);
//		for (String string : mySet) {
//			System.out.println("token " + string);
//		}
//		try {
//			if (FirebaseApp.getApps().isEmpty()) {
//			    FileInputStream serviceAccount = new FileInputStream(
//			        "C:\\Users\\yyaha\\Downloads\\sormasapp-7d2db-firebase-adminsdk-tukt0-9def4aa8f9.json"
//			    );
//
//			    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
//			            .createScoped("https://www.googleapis.com/auth/cloud-platform");
//
//			    FirebaseOptions options = FirebaseOptions.builder()
//			            .setCredentials(credentials)
//			            .build();
//
//			    FirebaseApp.initializeApp(options);
//			}
//
//			String token = FirebaseMessaging.getInstance().send(
//				    Message.builder()
//				        .setToken(mySet.iterator().next())
//				        .putData(messageDto.getTitle(), messageDto.getMessageContent())
//				        .build()
//				);
//
//				System.out.println("FCM RESULT = " + token);
//				
//			MulticastMessage multicastMessage = MulticastMessage.builder() .putData("title", messageDto.getTitle())
//				    .putData("body", messageDto.getMessageContent()).setNotification(
//					Notification.builder().setTitle(messageDto.getTitle()).setBody(messageDto.getMessageContent()).build())
//					.addAllTokens(mySet).build();
//			System.out.println("BROADCASTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
//			BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(multicastMessage);
//			System.out.println("RESPONSEEEEEEEEEEEEEEE " + response);
//		} catch (FirebaseMessagingException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void sendFcmSdk(MessageDto messageDto) throws IOException {

	    System.out.println("SENFCMSDKKKKKKK");

	    // Fetch tokens
	    List<String> tokens = FacadeProvider.getUserFacade()
	            .getUserForFCM(
	                    messageDto.getFormAccess(),
	                    messageDto.getArea(),
	                    messageDto.getRegion(),
	                    messageDto.getDistrict(),
	                    messageDto.getCommunity()
	            );

	    Set<String> uniqueTokens = tokens.stream()
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    uniqueTokens.forEach(t -> System.out.println("token " + t));

	    try {
	        // ---------------------------
	        // INIT FIREBASE ONLY ONCE
	        // ---------------------------
	        if (FirebaseApp.getApps().isEmpty()) {
//	            FileInputStream serviceAccount = new FileInputStream(
//	                    "C:\\Users\\yyaha\\Downloads\\sormasapp-7d2db-firebase-adminsdk-tukt0-9def4aa8f9.json"
//	            );
	        	
	        	InputStream serviceAccount = 
	        	        this.getClass().getClassLoader().getResourceAsStream("serviceaccount.json");

	        	if (serviceAccount == null) {
	        	    throw new IllegalStateException("Service account file not found in resources!");
	        	}

	            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
	                    .createScoped(Collections.singletonList(
	                            "https://www.googleapis.com/auth/cloud-platform"
	                    ));

	            FirebaseOptions options = FirebaseOptions.builder()
	                    .setCredentials(credentials)
	                    .build();

	            FirebaseApp.initializeApp(options);
	            System.out.println("Firebase initialized!");
	        }

	        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

	        // ---------------------------
	        // SEND ONE BY ONE — NO BATCH
	        // ---------------------------
	        int success = 0;
	        int failure = 0;

	        for (String token : uniqueTokens) {
	            try {
	                Message message = Message.builder()
	                        .setToken(token)
	                        .putData("title", messageDto.getTitle())
	                        .putData("body", messageDto.getMessageContent())
	                        .setNotification(
	                                Notification.builder()
	                                        .setTitle(messageDto.getTitle())
	                                        .setBody(messageDto.getMessageContent())
	                                        .build()
	                        )
	                        .build();

	                String response = messaging.send(message);
	                System.out.println("Sent successfully → " + token + " | ID=" + response);
	                success++;

	            } catch (Exception e) {
	                System.err.println("Failed to send → " + token + ": " + e.getMessage());
	                failure++;
	            }
	        }

	        // Summary
	        System.out.println("FINISHED SENDING:");
	        System.out.println("  Success: " + success);
	        System.out.println("  Failure: " + failure);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}



	public void saveMessage(MessagingLayout.SaveEvent event) throws Exception {
		FacadeProvider.getMessageFacade().saveMessage(event.getMessage());

		if (event.getMessage().getUserRoles().contains(UserRole.REST_USER)) {
			System.out.println("SENDINGMOBILEUSERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			sendFcmSdk(event.getMessage());
		}
	}

	public void saveMessages(ScheduleMessageLayout.SaveEvent event) throws Exception {
		FacadeProvider.getMessageFacade().saveMessage(event.getScheduleMessage());
	}
	
	private void updateRowCount() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

}
