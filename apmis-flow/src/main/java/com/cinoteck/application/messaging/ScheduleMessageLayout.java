package com.cinoteck.application.messaging;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageScheduleDto;
import de.symeda.sormas.api.messaging.MessageTemplateDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

public class ScheduleMessageLayout extends VerticalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = 207602334407422169L;		


	MessageScheduleDto messageScheduleDto;

	H3 pushNotificationHeader = new H3("Push Notication Configuration");
	TextField titleField;
	public TextArea messageContent;
	MultiSelectComboBox<UserRole> userRoles;
	ComboBox<UserType> userType;
	MultiSelectComboBox<FormAccess> formAccessSelector;
	MultiSelectComboBox<AreaReferenceDto> areaSelector;
	MultiSelectComboBox<RegionReferenceDto> regionSelector;
	MultiSelectComboBox<DistrictReferenceDto> districtSelector;
	MultiSelectComboBox<CommunityReferenceDto> communitySelector;
	
	DatePicker scheduleMessageDate = new DatePicker();
	TimePicker scheduleMessageTime = new TimePicker();

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;

	Binder<MessageScheduleDto> binder = new BeanValidationBinder<>(MessageScheduleDto.class);
	FormLayout formLayout = new FormLayout();

	UserProvider userProvider = new UserProvider();

	List<RegionReferenceDto> regionHolder;
	List<DistrictReferenceDto> districtHolder;
	List<CommunityReferenceDto> communityiesHolder;

	Icon savePreviewIcon = new Icon(VaadinIcon.PROGRESSBAR);
	Button savePreviewButton = new Button("Schedule", savePreviewIcon);

	private boolean isNew = false;
		
	private ComboBox<String> templateCombo = new ComboBox<String>("Message Template");

	public ScheduleMessageLayout(MessageScheduleDto messageScheduleDto_, boolean isNew) {

		this.isNew = isNew;
		if (isNew) {
			MessageScheduleDto messageScheduleDtoNew = new MessageScheduleDto();

			this.messageScheduleDto = messageScheduleDtoNew.build();
		} else {
			this.messageScheduleDto = messageScheduleDto_;
		}
		
		List<MessageTemplateDto> listOfMessageTemplate = FacadeProvider.getMessageFacade().getIndexListForMessageTemplate(null, null, null, null);
		List<String> listofMain = listOfMessageTemplate.stream().map(MessageTemplateDto::getMessageContent).collect(Collectors.toList());;		
		templateCombo.setItems(listofMain);
		
		configureFields();
	}

	public void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	public void configureFields() {

		TextField titleField = new TextField("Title");
		TextArea messageContent = new TextArea("Message Content");
		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<UserRole>("User roles");
		ComboBox<UserType> userType = new ComboBox<UserType>("User Type");
		MultiSelectComboBox<FormAccess> formAccessSelector = new MultiSelectComboBox<FormAccess>("Form Access");
		MultiSelectComboBox<AreaReferenceDto> areaSelector = new MultiSelectComboBox<AreaReferenceDto>("Region");
		MultiSelectComboBox<RegionReferenceDto> regionSelector = new MultiSelectComboBox<RegionReferenceDto>(
				"Province");
		MultiSelectComboBox<DistrictReferenceDto> districtSelector = new MultiSelectComboBox<DistrictReferenceDto>(
				"District");
		MultiSelectComboBox<CommunityReferenceDto> communitySelector = new MultiSelectComboBox<CommunityReferenceDto>(
				"Cluster");
		
		scheduleMessageDate = new DatePicker("Date");
		scheduleMessageTime = new TimePicker("Time");
		scheduleMessageTime.setLocale(Locale.GERMAN);

		List<UserType> userTypeConfig = new ArrayList<>();

		userTypeConfig.add(UserType.COMMON_USER);
		userTypeConfig.add(UserType.EOC_USER);
		userTypeConfig.add(UserType.WHO_USER);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		roles.remove(UserRole.POE_INFORMANT);
		roles.remove(UserRole.ADMIN);
		List<UserRole> userRoleConfig = new ArrayList<>(roles);

		userRoles.setItems(userRoleConfig);
		userType.setItems(userTypeConfig);
		areaSelector.setItems(regions);
		regionSelector.setItems(provinces);
		districtSelector.setItems(districts);
		formAccessSelector.setItems(Arrays.stream(FormAccess.values())
                .filter(value -> value != FormAccess.MODALITY_PRE  && value != FormAccess.MODALITY_POST  && value != FormAccess.EAG)
                .collect(Collectors.toList()));
		formAccessSelector.setClearButtonVisible(true);

		binder.forField(messageContent).asRequired("Message Content is Required").bind(MessageScheduleDto::getMessageContent,
				MessageScheduleDto::setMessageContent);

		binder.forField(userRoles).asRequired("User Role is Required").bind(MessageScheduleDto::getUserRoles,
				MessageScheduleDto::setUserRoles);

		binder.forField(formAccessSelector).bind(MessageScheduleDto::getFormAccess, MessageScheduleDto::setFormAccess);

		binder.forField(areaSelector).bind(MessageScheduleDto::getArea, MessageScheduleDto::setArea);

		binder.forField(regionSelector).bind(MessageScheduleDto::getRegion, MessageScheduleDto::setRegion);

		binder.forField(districtSelector).bind(MessageScheduleDto::getDistrict, MessageScheduleDto::setDistrict);

		binder.forField(communitySelector).bind(MessageScheduleDto::getCommunity, MessageScheduleDto::setCommunity);
		
		binder.forField(scheduleMessageDate).bind(MessageScheduleDto::getScheduleDate, MessageScheduleDto::setScheduleDate);
		binder.forField(scheduleMessageTime).bind(MessageScheduleDto::getScheduleTime, MessageScheduleDto::setScheduleTime);

		formLayout.add(templateCombo, messageContent, userRoles, formAccessSelector, areaSelector, regionSelector, districtSelector,
				communitySelector, scheduleMessageDate, scheduleMessageTime);
		formLayout.setColspan(pushNotificationHeader, 2);

		final HorizontalLayout hr = new HorizontalLayout();
		formLayout.setColspan(messageContent, 2);
		messageContent.setHeight("300px");

		Icon discardIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		Button discardChanges = new Button("Discard Changes", discardIcon);

		Icon saveIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		Button saved = new Button("Schedule", saveIcon);
		hr.add(discardChanges, saved);
		add(formLayout, hr);

		discardChanges.addClickListener(e -> discardChanges());

		templateCombo.addValueChangeListener(e -> {
			messageContent.setValue(templateCombo.getValue());
		});
		
		messageContent.addValueChangeListener(e -> {
			templateCombo.setVisible(false);
		});
		saved.addClickListener(e -> {
			if (messageContent.getValue() != null && !messageContent.isEmpty()) {
				preView(binder.getBean());
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Message cannot be left Empty");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			}
		});

		savePreviewButton.addClickListener(e -> {
			validateAndSave();
		});

		areaSelector.addValueChangeListener(e -> {
			regionHolder = new ArrayList<>();
			if (e.getValue() != null) {
				for (

				AreaReferenceDto eachArea : e.getValue()) {
					regionHolder.addAll(FacadeProvider.getRegionFacade().getAllActiveByArea(eachArea.getUuid()));
				}
				regionSelector.clear();
				provinces = regionHolder;
				regionSelector.setItems(provinces);
			}

		});

		regionSelector.addValueChangeListener(e -> {
			districtHolder = new ArrayList();
			if (e.getValue() != null) {
				for (RegionReferenceDto eachRegion : e.getValue()) {
					districtHolder
							.addAll(FacadeProvider.getDistrictFacade().getAllActiveByRegion(eachRegion.getUuid()));
				}

				districtSelector.clear();

				districts = districtHolder;
				districtSelector.setItems(districts);
			}

		});

		districtSelector.addValueChangeListener(e -> {
			communityiesHolder = new ArrayList<>();
			if (e.getValue() != null) {
				for (DistrictReferenceDto eachDsitrict : e.getValue()) {

					communityiesHolder
							.addAll(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(eachDsitrict.getUuid()));
				}
				communitySelector.clear();

				communities = communityiesHolder;
				communitySelector.setItems(communities);
//				criteria.district(district);
//				filterDataProvider.setFilter(criteria);
//				filterDataProvider.refreshAll();
//				updateRowCount();

			} else {
//				criteria.district(null);
//				filterDataProvider.setFilter(criteria);
//				filterDataProvider.refreshAll();
//				updateRowCount();

			}
		});
		
		scheduleMessageDate.addValueChangeListener(e -> {
		});
		
		scheduleMessageTime.addValueChangeListener(e -> {		
		});
	}

	public void validateAndSave() {

		if (binder.validate().isOk()) {

			messageScheduleDto = binder.getBean();
			messageScheduleDto.setChgDate(Timestamp.from(Instant.now()));
			messageScheduleDto.setCreatingUser(userProvider.getUser().getUserName());
			fireEvent(new SaveEvent(this, messageScheduleDto));

			Notification notification = new Notification("New Message Created", 3000, Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.open();
			UI.getCurrent().getPage().reload();
		} else {
			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Position.MIDDLE);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(event -> {
				notification.close();
			});

			Paragraph text = new Paragraph("Unable to Create a Message at the Moment");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

	public void preView(MessageScheduleDto messageScheduleDto) {

		TextArea message = new TextArea("Message");
		message.setValue(messageScheduleDto.getMessageContent());
		message.setReadOnly(true);
		message.getStyle().set("margin", "10px");
		message.setHeight("250px");

		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>("Userroles");
		userRoles.setItems(messageScheduleDto.getUserRoles());
		userRoles.setValue(messageScheduleDto.getUserRoles());
		userRoles.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<FormAccess> formAccess = new MultiSelectComboBox<>("FormAccess");
		formAccess.setItems(messageScheduleDto.getFormAccess());
		formAccess.setValue(messageScheduleDto.getFormAccess());
		formAccess.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<AreaReferenceDto> areas = new MultiSelectComboBox<>("Regions");
		areas.setItems(messageScheduleDto.getArea());
		areas.setValue(messageScheduleDto.getArea());
		areas.setReadOnly(true);
		areas.getStyle().set("margin", "10px");

		MultiSelectComboBox<RegionReferenceDto> region = new MultiSelectComboBox<>("Provinces");
		region.setItems(messageScheduleDto.getRegion());
		region.setValue(messageScheduleDto.getRegion());
		region.setReadOnly(true);
		region.getStyle().set("margin", "10px");

		MultiSelectComboBox<DistrictReferenceDto> district = new MultiSelectComboBox<>("Districts");
		district.setItems(messageScheduleDto.getDistrict());
		district.setValue(messageScheduleDto.getDistrict());
		district.setReadOnly(true);
		district.getStyle().set("margin", "10px");

		MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>("Clusters");
		community.setItems(messageScheduleDto.getCommunity());
		community.setValue(messageScheduleDto.getCommunity());
		community.setReadOnly(true);
		community.getStyle().set("margin", "10px");

		FormLayout preViewContent = new FormLayout();
		preViewContent.add(message);
//		, userRoles, formAccess, areas, region, district, community
		preViewContent.setColspan(message, 2);
		Dialog preViewDialog = new Dialog();
		preViewDialog.setWidth("700px");
		preViewDialog.setHeight("400px");
		Button closePreviewButton = new Button("Cancel", e -> preViewDialog.close());
		Icon backIcon = new Icon(VaadinIcon.BACKWARDS);
		closePreviewButton.setIcon(backIcon);
		preViewDialog.add(preViewContent);
		preViewDialog.setHeaderTitle("Send Message?");
		preViewDialog.open();
		preViewDialog.setCloseOnEsc(false);
		preViewDialog.setCloseOnOutsideClick(false);
		preViewDialog.setModal(true);
		preViewDialog.setClassName("notification-preview");
		preViewDialog.getFooter().add(closePreviewButton, savePreviewButton);
	}

	public void setScheduleMessage(MessageScheduleDto messageScheduleDto) {
		messageScheduleDto.setCreatingUser(userProvider.getUser().getUserName());
		binder.setBean(messageScheduleDto);
	}

	public static abstract class MessageEvent extends ComponentEvent<ScheduleMessageLayout> {
		private MessageScheduleDto messageScheduleDto;

		protected MessageEvent(ScheduleMessageLayout source, MessageScheduleDto messageScheduleDto) {
			super(source, false);
			this.messageScheduleDto = messageScheduleDto;
		}

		public MessageScheduleDto getScheduleMessage() {
			if (messageScheduleDto == null) {
				messageScheduleDto = new MessageScheduleDto();
				return messageScheduleDto;
			} else {
				return messageScheduleDto;
			}
		}
	}

	public static class SaveEvent extends MessageEvent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1355108957223638756L;

		SaveEvent(ScheduleMessageLayout source, MessageScheduleDto messageScheduleDto) {
			super(source, messageScheduleDto);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}
		
}
