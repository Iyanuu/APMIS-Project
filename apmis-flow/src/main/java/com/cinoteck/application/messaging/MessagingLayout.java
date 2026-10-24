package com.cinoteck.application.messaging;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageCategory;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.messaging.MessageTemplateDto;
import de.symeda.sormas.api.messaging.MessageUtil;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

public class MessagingLayout extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6287328469071497747L;

	MessageDto messageDto;

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
	private ComboBox<MessageCategory> messageCategory;	

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;

	Binder<MessageDto> binder = new BeanValidationBinder<>(MessageDto.class);
	FormLayout formLayout = new FormLayout();

	UserProvider userProvider = new UserProvider();

	List<RegionReferenceDto> regionHolder;
	List<DistrictReferenceDto> districtHolder;
	List<CommunityReferenceDto> communityiesHolder;

	Icon savePreviewIcon = new Icon(VaadinIcon.PROGRESSBAR);
	Button savePreviewButton = new Button("Send", savePreviewIcon);

	private boolean isNew = false;
		
	private ComboBox<String> templateCombo = new ComboBox<String>("Message Template");
	Map<String, MessageUtil> listofMain;

	public MessagingLayout(MessageDto messageDto_, boolean isNew) {

		this.isNew = isNew;
		if (isNew) {
			MessageDto messageDtoNew = new MessageDto();

			this.messageDto = messageDtoNew.build();
		} else {
			this.messageDto = messageDto_;
		}
		
		List<MessageTemplateDto> listOfMessageTemplate = FacadeProvider.getMessageFacade().getIndexListForMessageTemplate(null, null, null, null);
		listofMain = listOfMessageTemplate.stream().collect(Collectors.toMap( 
				dto -> dto.getTitle() != null 
//				|| !dto.getTitle().isEmpty() 
				? dto.getTitle() 
						: "No Title", 
				this::getMessageUtil ));		
		
		templateCombo.setItems(listofMain.keySet());
		
		configureFields();
	}

	private MessageUtil getMessageUtil(MessageTemplateDto messageTemplateDto) {		
		MessageUtil messageUtil = new MessageUtil(messageTemplateDto.getMessageContent(), messageTemplateDto.getCreatingUser(), messageTemplateDto.isArchived(),
				messageTemplateDto.getMessageCategory(), messageTemplateDto.getChgDate());
		return messageUtil;
	}

	public void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	public void configureFields() {

		TextField titleField = new TextField("Message Subject");
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

		ComboBox<MessageCategory> messageCategory = new ComboBox<>("Message category");
		messageCategory.setItems(MessageCategory.values());
		messageCategory.setClearButtonVisible(true);
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

		binder.forField(titleField).asRequired("Message Subject is Required").bind(MessageDto::getTitle,
				MessageDto::setTitle);
		
		binder.forField(messageContent).asRequired("Message Content is Required").bind(MessageDto::getMessageContent,
				MessageDto::setMessageContent);

		binder.forField(messageCategory).asRequired("Message Category is Required").bind(MessageDto::getMessageCategory,
				MessageDto::setMessageCategory);
		
		binder.forField(userRoles).asRequired("User Role is Required").bind(MessageDto::getUserRoles,
				MessageDto::setUserRoles);

		binder.forField(formAccessSelector).bind(MessageDto::getFormAccess, MessageDto::setFormAccess);

		binder.forField(areaSelector).bind(MessageDto::getArea, MessageDto::setArea);

		binder.forField(regionSelector).bind(MessageDto::getRegion, MessageDto::setRegion);

		binder.forField(districtSelector).bind(MessageDto::getDistrict, MessageDto::setDistrict);

		binder.forField(communitySelector).bind(MessageDto::getCommunity, MessageDto::setCommunity);

		formLayout.add(templateCombo, titleField, messageContent, messageCategory, userRoles, formAccessSelector, areaSelector, regionSelector, districtSelector,
				communitySelector);
		formLayout.setColspan(pushNotificationHeader, 2);

		final HorizontalLayout hr = new HorizontalLayout();
		formLayout.setColspan(messageContent, 2);
		messageContent.setHeight("300px");
		
		Icon discardIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		Button discardChanges = new Button("Discard Changes", discardIcon);

		Icon saveIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		Button saved = new Button("Send", saveIcon);
		
		Icon reSendIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		Button reSend = new Button("Resend", reSendIcon);
		
		Button reSendFacade = new Button("Resend", reSendIcon);
		
		if(!isNew) {
			saved.setVisible(isNew);
			reSendFacade.setVisible(!isNew);
			reSend.setVisible(false);
			
			titleField.setReadOnly(!isNew);
			messageContent.setReadOnly(!isNew);
			messageCategory.setReadOnly(!isNew);
			userRoles.setReadOnly(!isNew);
			formAccessSelector.setReadOnly(!isNew);
			areaSelector.setReadOnly(!isNew);
			regionSelector.setReadOnly(!isNew);
			districtSelector.setReadOnly(!isNew);
			communitySelector.setReadOnly(!isNew);

		} else {
			saved.setVisible(true);
			reSendFacade.setVisible(false);
			reSend.setVisible(false);
		}
		
		hr.add(discardChanges, saved, reSend, reSendFacade);
		add(formLayout, hr);

		discardChanges.addClickListener(e -> discardChanges());

		templateCombo.addValueChangeListener(e -> {
			System.out.println("listofMain.get(templateCombo.getValue()).getMessageContent() " + listofMain.get(templateCombo.getValue()).getMessageContent());
			titleField.setValue(templateCombo.getValue());
			messageContent.setValue(listofMain.get(templateCombo.getValue()).getMessageContent());
		});
		
		messageContent.addValueChangeListener(e -> {
			templateCombo.setVisible(false);
		});
		
		saved.addClickListener(e -> {
			if (messageContent.getValue() != null && !messageContent.isEmpty() && messageCategory.getValue() != null
					&& userRoles.getValue() != null) {
				if(binder.getBean() != null) {
				preView(binder.getBean());
				} else {
					MessageDto messageDtoValue = new MessageDto();
					messageDtoValue.setMessageContent(messageContent.getValue());
					preView(messageDtoValue);
				}
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

				Paragraph text = new Paragraph("Required field cannot be left Empty");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			}
		});
		
		reSend.addClickListener(e -> {
			
			MessageDto messageDtoResend = new MessageDto().build();
			messageDtoResend.setArea(binder.getBean().getArea());
			messageDtoResend.setCommunity(binder.getBean().getCommunity());
			messageDtoResend.setDistrict(binder.getBean().getDistrict());
			messageDtoResend.setMessageCategory(binder.getBean().getMessageCategory());
			messageDtoResend.setFormAccess(binder.getBean().getFormAccess());
			messageDtoResend.setMessageCategory(binder.getBean().getMessageCategory());
			messageDtoResend.setMessageContent(binder.getBean().getMessageContent());
			messageDtoResend.setRegion(binder.getBean().getRegion());
			messageDtoResend.setUserRoles(binder.getBean().getUserRoles());
			messageDtoResend.setTitle(binder.getBean().getTitle());
			
			validateAndSave(messageDtoResend);
		});
		
		reSendFacade.addClickListener(e -> {
			
			if(reSendFacade.isVisible()) {
				reSend.setVisible(true);
				reSendFacade.setVisible(false);				
				
				titleField.setReadOnly(false);
				messageContent.setReadOnly(false);
				messageCategory.setReadOnly(false);
				userRoles.setReadOnly(false);
				formAccessSelector.setReadOnly(false);
				areaSelector.setReadOnly(false);
				regionSelector.setReadOnly(false);
				districtSelector.setReadOnly(false);
				communitySelector.setReadOnly(false);
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
	}

	public void validateAndSave() {

		if (binder.validate().isOk()) {

			messageDto = binder.getBean();
			messageDto.setChgDate(Timestamp.from(Instant.now()));
			messageDto.setCreatingUser(userProvider.getUser().getUserName());
			fireEvent(new SaveEvent(this, messageDto));

			Notification notification = new Notification("Message Sent", 3000, Position.MIDDLE);
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
	
	public void validateAndSave(MessageDto messageDto) {

		if (binder.validate().isOk()) {

//			messageDto = binder.getBean();
			messageDto.setChgDate(Timestamp.from(Instant.now()));
			messageDto.setCreatingUser(userProvider.getUser().getUserName());
			fireEvent(new SaveEvent(this, messageDto));

			Notification notification = new Notification("New Message Sent", 3000, Position.MIDDLE);
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

			Paragraph text = new Paragraph("Unable to Rebroadcast this Message at the Moment");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

	public void preView(MessageDto messageDto) {

		TextArea message = new TextArea("Message");
		message.setValue(messageDto.getMessageContent());
		message.setReadOnly(true);
		message.getStyle().set("margin", "10px");
		message.setHeight("250px");

		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>("Userroles");
		if(messageDto.getUserRoles() != null) {
			userRoles.setItems(messageDto.getUserRoles());
			userRoles.setValue(messageDto.getUserRoles());
		}		
		userRoles.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<FormAccess> formAccess = new MultiSelectComboBox<>("FormAccess");
		if(messageDto.getFormAccess() != null) {
			formAccess.setItems(messageDto.getFormAccess());
			formAccess.setValue(messageDto.getFormAccess());
		}			
		formAccess.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<AreaReferenceDto> areas = new MultiSelectComboBox<>("Regions");
		if(messageDto.getArea() != null) {
			areas.setItems(messageDto.getArea());
			areas.setValue(messageDto.getArea());
		}		
		areas.setReadOnly(true);
		areas.getStyle().set("margin", "10px");

		MultiSelectComboBox<RegionReferenceDto> region = new MultiSelectComboBox<>("Provinces");
		if(messageDto.getRegion() != null) {
			region.setItems(messageDto.getRegion());
			region.setValue(messageDto.getRegion());
		}	
		region.setReadOnly(true);
		region.getStyle().set("margin", "10px");

		MultiSelectComboBox<DistrictReferenceDto> district = new MultiSelectComboBox<>("Districts");
		if(messageDto.getDistrict() != null) {
			district.setItems(messageDto.getDistrict());
			district.setValue(messageDto.getDistrict());
		}		
		district.setReadOnly(true);
		district.getStyle().set("margin", "10px");

		MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>("Clusters");
		if(messageDto.getCommunity() != null) {
			community.setItems(messageDto.getCommunity());
			community.setValue(messageDto.getCommunity());
		}
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

	public void setMessage(MessageDto messageDto) {
		messageDto.setCreatingUser(userProvider.getUser().getUserName());
		binder.setBean(messageDto);
	}

	public static abstract class MessageEvent extends ComponentEvent<MessagingLayout> {
		private MessageDto messageDto;

		protected MessageEvent(MessagingLayout source, MessageDto messageDto) {
			super(source, false);
			this.messageDto = messageDto;
		}

		public MessageDto getMessage() {
			if (messageDto == null) {
				messageDto = new MessageDto();
				return messageDto;
			} else {
				return messageDto;
			}
		}
	}

	public static class SaveEvent extends MessageEvent {
		SaveEvent(MessagingLayout source, MessageDto messageDto) {
			super(source, messageDto);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}
}
