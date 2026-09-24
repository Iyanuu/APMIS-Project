package com.cinoteck.application.views.uiformbuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaGeographyLevel;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.utils.DataHelper;

public class FormBuilderLayout extends VerticalLayout {

	CampaignFormMetaDto campaignFormMetaDto;
	CampaignFormMetaDto campaignFormMetaDtoDupli;
	List<CampaignFormElement> campaignFormElements;
	List<CampaignFormElement> savedCampaignFormElements;

	H3 formBasics = new H3("Form Basics");
	TextField formName;
	TextField formId;
	ComboBox<CampaignPhase> formType;
	ComboBox<FormAccess> formCategory;
	MultiSelectComboBox<AreaReferenceDto> areaSelector;
	ComboBox<Modality> modality;
	IntegerField daysExpired;
	ComboBox<CampaignFormMetaGeographyLevel> geographyLevel;
	ComboBox<String> languageCode;
	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

	FormGridComponent formGridComponent;
	TranslationGridComponent translationGridComponent;

	Binder<CampaignFormMetaDto> binder = new BeanValidationBinder<>(CampaignFormMetaDto.class);

	FormLayout formLayout = new FormLayout();

	private boolean isNew = false;

	public FormBuilderLayout(CampaignFormMetaDto campaignFormMetaDto_, boolean isNew) {

		this.isNew = isNew;
		if (isNew) {
			CampaignFormMetaDto campMeta = new CampaignFormMetaDto();

			this.campaignFormMetaDto = campMeta.build();
		} else {
			this.campaignFormMetaDto = campaignFormMetaDto_;
		}
		formGridComponent = new FormGridComponent(campaignFormMetaDto);
		translationGridComponent = new TranslationGridComponent(campaignFormMetaDto);
		configureFields(campaignFormMetaDto_);
	}

	private List<CampaignFormElement> getFormListDashboard() {

		List<CampaignFormMetaDto> empty = FacadeProvider.getCampaignFormMetaFacade().getAllFormElement().stream()
				.collect(Collectors.toList());

		List<CampaignFormElement> list = new ArrayList<>();
		for (CampaignFormMetaDto campaignFormElement : empty) {

			CampaignFormElement makeList = new CampaignFormElement();
			makeList.setId(campaignFormElement.getFormId());
			list.add(makeList);
		}
		return list;
	}

	private void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	void configureFields(CampaignFormMetaDto campaignFormMetaDto_) {

		formName = new TextField("Form Name");
		formId = new TextField("Id");
		formType = new ComboBox<CampaignPhase>("Form Type");
		formType.setItems(CampaignPhase.values());
		formCategory = new ComboBox<FormAccess>("Form Category");
		formCategory.setItems(FormAccess.values());
		areaSelector = new MultiSelectComboBox<AreaReferenceDto>("Region");
		areaSelector.setItems(regions);
		modality = new ComboBox<Modality>("Modality");
		modality.setItems(Modality.values());
		daysExpired = new IntegerField("Days Expired");
		geographyLevel = new ComboBox<CampaignFormMetaGeographyLevel>("Geography Level");
		geographyLevel.setItems(CampaignFormMetaGeographyLevel.values());
		languageCode = new ComboBox<String>("Language Code");
		languageCode.setItems("en", "fa_AF", "ps_AF");

		binder.forField(formName).asRequired("Form Name is Required").bind(CampaignFormMetaDto::getFormName,
				CampaignFormMetaDto::setFormName);

		binder.forField(formId).asRequired("Form Id is Required").bind(CampaignFormMetaDto::getFormId,
				CampaignFormMetaDto::setFormId);

		binder.forField(formType).asRequired("Form Type is Required").bind(CampaignFormMetaDto::getFormType,
				CampaignFormMetaDto::setFormType);

		binder.forField(formCategory).asRequired("Form Category is Required").bind(CampaignFormMetaDto::getFormCategory,
				CampaignFormMetaDto::setFormCategory);

		binder.forField(areaSelector).bind(CampaignFormMetaDto::getArea, CampaignFormMetaDto::setArea);

		binder.forField(modality).asRequired("Modality is Required").bind(CampaignFormMetaDto::getModality,
				CampaignFormMetaDto::setModality);

		binder.forField(daysExpired).asRequired("Days Expired is Required").bind(CampaignFormMetaDto::getDaysExpired,
				CampaignFormMetaDto::setDaysExpired);

		binder.forField(geographyLevel).asRequired("Geography Level is Required")
				.bind(CampaignFormMetaDto::getGeographyLevel, CampaignFormMetaDto::setGeographyLevel);

		binder.forField(languageCode).asRequired("Language Code is Required").bind(CampaignFormMetaDto::getLanguageCode,
				CampaignFormMetaDto::setLanguageCode);

		formLayout.add(formBasics, formName, formId, formType, formCategory, areaSelector, modality, daysExpired,
				languageCode, geographyLevel);

		formLayout.setColspan(formBasics, 2);

		final HorizontalLayout hr = new HorizontalLayout();
		hr.setWidthFull();

		TabSheet sheet = new TabSheet();
		sheet.addClassName("formbuildertab");
		sheet.setId("formbuildertab");
		VerticalLayout tab1 = new VerticalLayout(formGridComponent);
		VerticalLayout tab2 = new VerticalLayout(translationGridComponent);
		tab1.getStyle().set("color", "black");
		tab1.getStyle().set("color", "black");

		sheet.add("Form Elements", tab1);
		sheet.add("Form Translations", tab2);

		sheet.setSizeFull();

		hr.add(sheet);

		add(formLayout);
		add(hr);

		Icon discardIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		discardIcon.getStyle().set("color", "red !important");
		Button discardChanges = new Button("Discard Changes", discardIcon);
		discardChanges.getStyle().set("color", "red !important");
		discardChanges.getStyle().set("background", "white");
		discardChanges.getStyle().set("border", "1px solid red");

		Icon saveIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		saveIcon.getStyle().set("color", "green");
		Button saved = new Button("Save", saveIcon);

		Button duplicateForm = new Button("Duplicate Form");
		duplicateForm.setText("Duplicate Form");

		Icon downloadIcon = new Icon(VaadinIcon.DOWNLOAD);
		Button downloadButton = new Button("Export JSON", downloadIcon);
		downloadButton.setText("Export JSON");

		Anchor downloadLink = new Anchor("", "Export JSON");
		downloadLink.getElement().setAttribute("download", true);
		downloadLink.add(downloadButton);

		HorizontalLayout buttonLayout = new HorizontalLayout(duplicateForm, downloadButton, downloadLink,
				discardChanges, saved);
		downloadLink.getStyle().set("display", "none");
		buttonLayout.getStyle().set("margin-left", "auto");

		add(buttonLayout);

		discardChanges.addClickListener(e -> discardChanges());
		saved.addClickListener(e -> {

			if (isNew) {
				validateAndSaveNew();
			} else {
				validateAndSave();
			}
		});

		downloadButton.addClickListener(event -> {
			StreamResource resource = createJsonStreamResource();
			downloadLink.setHref(resource);
			downloadLink.getElement().callJsFunction("click");
		});

		duplicateForm.addClickListener(e -> {
			try {
				// Get the current version count
				long version = FacadeProvider.getCampaignFormMetaFacade()
						.getFormCountByGroupUuid(campaignFormMetaDto_.getFormGroupUuid());

				long incrementedVersion = version + 1L;
				String incrementedVersionString = incrementedVersion + "";
				// Create a completely new DTO instance
				CampaignFormMetaDto campaignFormMetaDtoDupli = new CampaignFormMetaDto();
				campaignFormMetaDtoDupli = campaignFormMetaDto_;
				// Copy all relevant fields from the original
				campaignFormMetaDtoDupli.setUuid(DataHelper.createUuid()); // Keep same uuid
				campaignFormMetaDtoDupli.setFormId(campaignFormMetaDto.getFormId() + incrementedVersionString);
				campaignFormMetaDtoDupli.setFormversion(incrementedVersion);
				campaignFormMetaDtoDupli.setCreationDate(new Timestamp(new Date().getTime()));
				campaignFormMetaDtoDupli.setArchived(false);

				// Save the new duplicate
				fireEvent(new DuplicateEvent(this, campaignFormMetaDtoDupli));

				// Refresh UI and show notification
//		        UI.getCurrent().getPage().reload();
				Notification notification = new Notification("Form Duplicated", 3000, Position.MIDDLE);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				notification.open();

			} catch (Exception ex) {
				System.out.println("Exception Occurred while saving: " + ex);
				Notification.show("Error duplicating form: " + ex.getMessage(), 3000, Position.MIDDLE);
			}

			discardChanges();
		});

	}

	private StreamResource createJsonStreamResource() {

		ObjectMapper objectMapper = new ObjectMapper();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			objectMapper.writeValue(outputStream, campaignFormMetaDto.getCampaignFormElements());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new StreamResource(campaignFormMetaDto.getFormName() + ".json", () -> {
			byte[] jsonBytes = outputStream.toByteArray();
			return new ByteArrayInputStream(jsonBytes);
		});
	}

	public void setForm(CampaignFormMetaDto formData) {
		binder.setBean(formData);
	}
	
	private void validateAndSaveNew() {

		if (binder.validate().isOk()) {

			campaignFormMetaDto = binder.getBean();
			campaignFormMetaDto.setFormname_fa_af(binder.getBean().getFormName());
			campaignFormMetaDto.setFormname_ps_af(binder.getBean().getFormName());
			campaignFormMetaDto.setCampaignFormElements(formGridComponent.getGridData());
			campaignFormMetaDto.setCampaignFormTranslations(translationGridComponent.getGridData());
			campaignFormMetaDto.setFormversion(1L);
			campaignFormMetaDto.setFormGroupUuid(DataHelper.createUuid());

			List<String> listofElement = formGridComponent.getGridData().stream().map(CampaignFormElement::getId)
					.collect(Collectors.toList());

			Set<String> seen = new HashSet<>();

			List<String> filteredList = listofElement.stream().filter(s -> !seen.add(s)).collect(Collectors.toList());

			if (filteredList.isEmpty()) {

				try {
					fireEvent(new SaveEvent(this, campaignFormMetaDto));

				} catch (Exception e) {
					System.out.println("Exception Occured while saving : " + e);

				} finally {
					UserProvider usr = new UserProvider();

					UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
					userActivitySummaryDto.setActionModule("Form Manager");
					userActivitySummaryDto.setAction("Form Saved: " + campaignFormMetaDto.getFormName());
					userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
					FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);

					UI.getCurrent().getPage().reload();

					Notification notification = new Notification("Form Saved", 3000, Position.MIDDLE);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					notification.open();

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

				Paragraph text = new Paragraph(
						"This Form cannot save because you have multiple elements with the same id "
								+ filteredList.get(0));
				System.out.println("This Form cannot save because you have multiple elements with the same id "
						+ filteredList.get(0));
				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
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

			Paragraph text = new Paragraph("Unable to Save Form");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

	private void validateAndSave() {

		if (binder.validate().isOk()) {

			campaignFormMetaDto = binder.getBean();
			campaignFormMetaDto.setFormname_fa_af(binder.getBean().getFormName());
			campaignFormMetaDto.setFormname_ps_af(binder.getBean().getFormName());
			campaignFormMetaDto.setCampaignFormElements(formGridComponent.getGridData());
			campaignFormMetaDto.setCampaignFormTranslations(translationGridComponent.getGridData());

			List<String> listofElement = formGridComponent.getGridData().stream().map(CampaignFormElement::getId)
					.collect(Collectors.toList());

			Set<String> seen = new HashSet<>();

			List<String> filteredList = listofElement.stream().filter(s -> !seen.add(s)).collect(Collectors.toList());

			if (filteredList.isEmpty()) {

				try {
					fireEvent(new SaveEvent(this, campaignFormMetaDto));

				} catch (Exception e) {
					System.out.println("Exception Occured while saving : " + e);

				} finally {
					UserProvider usr = new UserProvider();

					UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
					userActivitySummaryDto.setActionModule("Form Manager");
					userActivitySummaryDto.setAction("Form Saved: " + campaignFormMetaDto.getFormName());
					userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
					FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);

					UI.getCurrent().getPage().reload();

					Notification notification = new Notification("Form Saved", 3000, Position.MIDDLE);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					notification.open();

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

				Paragraph text = new Paragraph(
						"This Form cannot save because you have multiple elements with the same id "
								+ filteredList.get(0));
				System.out.println("This Form cannot save because you have multiple elements with the same id "
						+ filteredList.get(0));
				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
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

			Paragraph text = new Paragraph("Unable to Save Form");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

	public static abstract class FormBuilderEvent extends ComponentEvent<FormBuilderLayout> {
		private CampaignFormMetaDto form;

		protected FormBuilderEvent(FormBuilderLayout source, CampaignFormMetaDto form) {
			super(source, false);
			this.form = form;
		}

		public CampaignFormMetaDto getForm() {
			if (form == null) {
				form = new CampaignFormMetaDto();
				return form;
			} else {
				return form;
			}
		}
	}

	public static class SaveEvent extends FormBuilderEvent {
		SaveEvent(FormBuilderLayout source, CampaignFormMetaDto form) {
			super(source, form);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public static class DuplicateEvent extends FormBuilderEvent {
		DuplicateEvent(FormBuilderLayout source, CampaignFormMetaDto form) {

			super(source, form);
		}
	}

	public Registration addDuplicateListener(ComponentEventListener<DuplicateEvent> listener) {
		return addListener(DuplicateEvent.class, listener);
	}
}
