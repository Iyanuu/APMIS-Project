/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.caze;

import static de.symeda.sormas.ui.utils.CssStyles.ERROR_COLOR_PRIMARY;
import static de.symeda.sormas.ui.utils.CssStyles.FORCE_CAPTION;
import static de.symeda.sormas.ui.utils.CssStyles.H3;
import static de.symeda.sormas.ui.utils.CssStyles.SOFT_REQUIRED;
import static de.symeda.sormas.ui.utils.CssStyles.VSPACE_3;
import static de.symeda.sormas.ui.utils.CssStyles.style;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumn;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLoc;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRowLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.locs;

import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Sets;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.validator.EmailValidator;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.TextField;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.customizableenum.CustomizableEnumType;
import de.symeda.sormas.api.disease.DiseaseVariant;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityType;
import de.symeda.sormas.api.infrastructure.facility.FacilityTypeGroup;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PresentCondition;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.ComboBoxHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.InfrastructureFieldsHelper;
import de.symeda.sormas.ui.utils.NullableOptionGroup;
import de.symeda.sormas.ui.utils.PhoneNumberValidator;

public class CaseCreateForm extends AbstractEditForm<CaseDataDto> {

	private static final long serialVersionUID = 1L;

	private static final String FACILITY_OR_HOME_LOC = "facilityOrHomeLoc";
	private static final String FACILITY_TYPE_GROUP_LOC = "typeGroupLoc";
	private static final String DONT_SHARE_WARNING_LOC = "dontShareWithReportingToolWarnLoc";
	private static final String RESPONSIBLE_JURISDICTION_HEADING_LOC = "responsibleJurisdictionHeadingLoc";
	private static final String DIFFERENT_PLACE_OF_STAY_JURISDICTION = "differentPlaceOfStayJurisdiction";
	private static final String PLACE_OF_STAY_HEADING_LOC = "placeOfStayHeadingLoc";

	private ComboBox birthDateDay;
	private NullableOptionGroup facilityOrHome;
	private ComboBox facilityTypeGroup;
	private ComboBox facilityType;
	private ComboBox responsibleDistrictCombo;
	private ComboBox responsibleCommunityCombo;
	private CheckBox differentPlaceOfStayJurisdiction;
	private ComboBox districtCombo;
	private ComboBox communityCombo;
	private ComboBox facilityCombo;

	//@formatter:off
	private static final String HTML_LAYOUT = fluidRowLocs(CaseDataDto.CASE_ORIGIN, "")
			+ fluidRowLocs(CaseDataDto.REPORT_DATE, CaseDataDto.EPID_NUMBER, CaseDataDto.EXTERNAL_ID)
			+ fluidRow(
					fluidColumnLoc(6, 0, CaseDataDto.DISEASE),
					fluidColumn(6, 0,
							locs(CaseDataDto.DISEASE_DETAILS, CaseDataDto.PLAGUE_TYPE, CaseDataDto.DENGUE_FEVER_TYPE,
									CaseDataDto.RABIES_TYPE)),
					fluidColumnLoc(6, 0, CaseDataDto.DISEASE_VARIANT)) +
			fluidRowLocs(RESPONSIBLE_JURISDICTION_HEADING_LOC)
			+ fluidRowLocs(CaseDataDto.RESPONSIBLE_REGION, CaseDataDto.RESPONSIBLE_DISTRICT, CaseDataDto.RESPONSIBLE_COMMUNITY)
			+ fluidRowLocs(CaseDataDto.DONT_SHARE_WITH_REPORTING_TOOL)
			+ fluidRowLocs(DONT_SHARE_WARNING_LOC)
			+ fluidRowLocs(DIFFERENT_PLACE_OF_STAY_JURISDICTION)
			+ fluidRowLocs(PLACE_OF_STAY_HEADING_LOC)
			+ fluidRowLocs(FACILITY_OR_HOME_LOC)
			+ fluidRowLocs(CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY)
			+ fluidRowLocs(FACILITY_TYPE_GROUP_LOC, CaseDataDto.FACILITY_TYPE)
			+ fluidRowLocs(CaseDataDto.HEALTH_FACILITY, CaseDataDto.HEALTH_FACILITY_DETAILS)
			+ fluidRowLocs(CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS)
			+ fluidRowLocs(PersonDto.FIRST_NAME, PersonDto.LAST_NAME)
			+ fluidRow(fluidRowLocs(PersonDto.BIRTH_DATE_YYYY, PersonDto.BIRTH_DATE_MM, PersonDto.BIRTH_DATE_DD),
			fluidRowLocs(PersonDto.SEX))
			+ fluidRowLocs(PersonDto.NATIONAL_HEALTH_ID, PersonDto.PASSPORT_NUMBER)
			+ fluidRowLocs(PersonDto.PRESENT_CONDITION, SymptomsDto.ONSET_DATE)
			+ fluidRowLocs(PersonDto.PHONE, PersonDto.EMAIL_ADDRESS);
	//@formatter:on

	public CaseCreateForm() {

		super(CaseDataDto.class, CaseDataDto.I18N_PREFIX, FieldVisibilityCheckers.withCountry(FacadeProvider.getConfigFacade().getCountryLocale()));

		setWidth(720, Unit.PIXELS);

		hideValidationUntilNextCommit();
	}

	@Override
	protected void addFields() {

		NullableOptionGroup ogCaseOrigin = addField(CaseDataDto.CASE_ORIGIN, NullableOptionGroup.class);
		ogCaseOrigin.setRequired(true);

		TextField epidField = addField(CaseDataDto.EPID_NUMBER, TextField.class);
		epidField.setInvalidCommitted(true);
		style(epidField, ERROR_COLOR_PRIMARY);

		if (!FacadeProvider.getExternalSurveillanceToolFacade().isFeatureEnabled()) {
			TextField externalIdField = addField(CaseDataDto.EXTERNAL_ID, TextField.class);
			style(externalIdField, ERROR_COLOR_PRIMARY);
		} else {
			CheckBox dontShareCheckbox = addField(CaseDataDto.DONT_SHARE_WITH_REPORTING_TOOL, CheckBox.class);
			CaseFormHelper.addDontShareWithReportingTool(getContent(), () -> dontShareCheckbox, DONT_SHARE_WARNING_LOC);
		}

		addField(CaseDataDto.REPORT_DATE, DateField.class);
		ComboBox diseaseField = addDiseaseField(CaseDataDto.DISEASE, false, true);
		ComboBox diseaseVariantField = addField(CaseDataDto.DISEASE_VARIANT, ComboBox.class);
		diseaseVariantField.setNullSelectionAllowed(true);
		diseaseVariantField.setVisible(false);
		addField(CaseDataDto.DISEASE_DETAILS, TextField.class);
		NullableOptionGroup plagueType = addField(CaseDataDto.PLAGUE_TYPE, NullableOptionGroup.class);
		addField(CaseDataDto.DENGUE_FEVER_TYPE, NullableOptionGroup.class);
		addField(CaseDataDto.RABIES_TYPE, NullableOptionGroup.class);
		addCustomField(PersonDto.FIRST_NAME, String.class, TextField.class);
		addCustomField(PersonDto.LAST_NAME, String.class, TextField.class);
		addCustomField(PersonDto.NATIONAL_HEALTH_ID, String.class, TextField.class);
		addCustomField(PersonDto.PASSPORT_NUMBER, String.class, TextField.class);

		birthDateDay = addCustomField(PersonDto.BIRTH_DATE_DD, Integer.class, ComboBox.class);
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateDay.setNullSelectionAllowed(true);
		birthDateDay.addStyleName(FORCE_CAPTION);
		birthDateDay.setInputPrompt(I18nProperties.getString(Strings.day));
		ComboBox birthDateMonth = addCustomField(PersonDto.BIRTH_DATE_MM, Integer.class, ComboBox.class);
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateMonth.setNullSelectionAllowed(true);
		birthDateMonth.addItems(DateHelper.getMonthsInYear());
		birthDateMonth.setPageLength(12);
		birthDateMonth.addStyleName(FORCE_CAPTION);
		birthDateMonth.setInputPrompt(I18nProperties.getString(Strings.month));
		setItemCaptionsForMonths(birthDateMonth);
		ComboBox birthDateYear = addCustomField(PersonDto.BIRTH_DATE_YYYY, Integer.class, ComboBox.class);
		birthDateYear.setCaption(I18nProperties.getPrefixCaption(PersonDto.I18N_PREFIX, PersonDto.BIRTH_DATE));
		// @TODO: Done for nullselection Bug, fixed in Vaadin 7.7.3
		birthDateYear.setNullSelectionAllowed(true);
		birthDateYear.addItems(DateHelper.getYearsToNow());
		birthDateYear.setItemCaptionMode(ItemCaptionMode.ID_TOSTRING);
		birthDateYear.setInputPrompt(I18nProperties.getString(Strings.year));
		birthDateDay.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) birthDateYear.getValue(), (Integer) birthDateMonth.getValue(), (Integer) e));
		birthDateMonth.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) birthDateYear.getValue(), (Integer) e, (Integer) birthDateDay.getValue()));
		birthDateYear.addValidator(
			e -> ControllerProvider.getPersonController()
				.validateBirthDate((Integer) e, (Integer) birthDateMonth.getValue(), (Integer) birthDateDay.getValue()));

		// Update the list of days according to the selected month and year
		birthDateYear.addValueChangeListener(e -> {
			updateListOfDays((Integer) e.getProperty().getValue(), (Integer) birthDateMonth.getValue());
			birthDateMonth.markAsDirty();
			birthDateDay.markAsDirty();
		});
		birthDateMonth.addValueChangeListener(e -> {
			updateListOfDays((Integer) birthDateYear.getValue(), (Integer) e.getProperty().getValue());
			birthDateYear.markAsDirty();
			birthDateDay.markAsDirty();
		});
		birthDateDay.addValueChangeListener(e -> {
			birthDateYear.markAsDirty();
			birthDateMonth.markAsDirty();
		});

		ComboBox sex = addCustomField(PersonDto.SEX, Sex.class, ComboBox.class);
		sex.setCaption(I18nProperties.getCaption(Captions.Person_sex));
		ComboBox presentCondition = addCustomField(PersonDto.PRESENT_CONDITION, PresentCondition.class, ComboBox.class);
		presentCondition.setCaption(I18nProperties.getCaption(Captions.Person_presentCondition));

		addCustomField(
			SymptomsDto.ONSET_DATE,
			Date.class,
			DateField.class,
			I18nProperties.getPrefixCaption(SymptomsDto.I18N_PREFIX, SymptomsDto.ONSET_DATE));

		TextField phone = addCustomField(PersonDto.PHONE, String.class, TextField.class);
		phone.setCaption(I18nProperties.getCaption(Captions.Person_phone));
		TextField email = addCustomField(PersonDto.EMAIL_ADDRESS, String.class, TextField.class);
		email.setCaption(I18nProperties.getCaption(Captions.Person_emailAddress));

		phone.addValidator(new PhoneNumberValidator(I18nProperties.getValidationError(Validations.validPhoneNumber, phone.getCaption())));
		email.addValidator(new EmailValidator(I18nProperties.getValidationError(Validations.validEmailAddress, email.getCaption())));

		differentPlaceOfStayJurisdiction = addCustomField(DIFFERENT_PLACE_OF_STAY_JURISDICTION, Boolean.class, CheckBox.class);
		differentPlaceOfStayJurisdiction.addStyleName(VSPACE_3);

		Label placeOfStayHeadingLabel = new Label(I18nProperties.getCaption(Captions.casePlaceOfStay));
		placeOfStayHeadingLabel.addStyleName(H3);
		getContent().addComponent(placeOfStayHeadingLabel, PLACE_OF_STAY_HEADING_LOC);

		ComboBox region = addInfrastructureField(CaseDataDto.REGION);
		districtCombo = addInfrastructureField(CaseDataDto.DISTRICT);
		communityCombo = addInfrastructureField(CaseDataDto.COMMUNITY);
		communityCombo.setNullSelectionAllowed(true);

		// jurisdictionfields
		Label jurisdictionHeadingLabel = new Label(I18nProperties.getString(Strings.headingCaseResponsibleJurisidction));
		jurisdictionHeadingLabel.addStyleName(H3);
		getContent().addComponent(jurisdictionHeadingLabel, RESPONSIBLE_JURISDICTION_HEADING_LOC);

		ComboBox responsibleRegion = addInfrastructureField(CaseDataDto.RESPONSIBLE_REGION);
		responsibleRegion.setRequired(true);
		responsibleDistrictCombo = addInfrastructureField(CaseDataDto.RESPONSIBLE_DISTRICT);
		responsibleDistrictCombo.setRequired(true);
		responsibleCommunityCombo = addInfrastructureField(CaseDataDto.RESPONSIBLE_COMMUNITY);
		responsibleCommunityCombo.setNullSelectionAllowed(true);
		responsibleCommunityCombo.addStyleName(SOFT_REQUIRED);

		InfrastructureFieldsHelper.initInfrastructureFields(responsibleRegion, responsibleDistrictCombo, responsibleCommunityCombo);

		FieldHelper.setVisibleWhen(
			differentPlaceOfStayJurisdiction,
			Arrays.asList(region, districtCombo, communityCombo),
			Collections.singletonList(Boolean.TRUE),
			true);

		FieldHelper.setRequiredWhen(
			differentPlaceOfStayJurisdiction,
			Arrays.asList(region, districtCombo),
			Collections.singletonList(Boolean.TRUE),
			false,
			null);

		facilityOrHome =
			addCustomField(FACILITY_OR_HOME_LOC, TypeOfPlace.class, NullableOptionGroup.class, I18nProperties.getCaption(Captions.casePlaceOfStay));
		facilityOrHome.removeAllItems();
		for (TypeOfPlace place : TypeOfPlace.FOR_CASES) {
			facilityOrHome.addItem(place);
			facilityOrHome.setItemCaption(place, I18nProperties.getEnumCaption(place));
		}
		facilityOrHome.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		facilityOrHome.setId("facilityOrHome");
		facilityOrHome.setWidth(100, Unit.PERCENTAGE);
		CssStyles.style(facilityOrHome, ValoTheme.OPTIONGROUP_HORIZONTAL);
		facilityTypeGroup = ComboBoxHelper.createComboBoxV7();
		facilityTypeGroup.setId("typeGroup");
		facilityTypeGroup.setCaption(I18nProperties.getCaption(Captions.Facility_typeGroup));
		facilityTypeGroup.setWidth(100, Unit.PERCENTAGE);
		facilityTypeGroup.addItems(FacilityTypeGroup.getAccomodationGroups());
		getContent().addComponent(facilityTypeGroup, FACILITY_TYPE_GROUP_LOC);
		facilityType = ComboBoxHelper.createComboBoxV7();
		facilityType.setId("type");
		facilityType.setCaption(I18nProperties.getCaption(Captions.facilityType));
		facilityType.setWidth(100, Unit.PERCENTAGE);
		getContent().addComponent(facilityType, CaseDataDto.FACILITY_TYPE);
		facilityCombo = addInfrastructureField(CaseDataDto.HEALTH_FACILITY);
		facilityCombo.setImmediate(true);
		TextField facilityDetails = addField(CaseDataDto.HEALTH_FACILITY_DETAILS, TextField.class);
		facilityDetails.setVisible(false);
		ComboBox cbPointOfEntry = addInfrastructureField(CaseDataDto.POINT_OF_ENTRY);
		cbPointOfEntry.setImmediate(true);
		TextField tfPointOfEntryDetails = addField(CaseDataDto.POINT_OF_ENTRY_DETAILS, TextField.class);
		tfPointOfEntryDetails.setVisible(false);

		region.addValueChangeListener(e -> {
			RegionReferenceDto regionDto = (RegionReferenceDto) e.getProperty().getValue();
			FieldHelper
				.updateItems(districtCombo, regionDto != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(regionDto.getUuid()) : null);
		});
		districtCombo.addValueChangeListener(e -> {
			FieldHelper.removeItems(communityCombo);
			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getProperty().getValue();
			FieldHelper.updateItems(
				communityCombo,
				districtDto != null ? FacadeProvider.getCommunityFacade().getAllActiveByDistrict(districtDto.getUuid()) : null);

			updateFacility();
			updatePOEs();
		});
		communityCombo.addValueChangeListener(e -> {
			updateFacility();
		});
		facilityOrHome.addValueChangeListener(e -> {
			FieldHelper.removeItems(facilityCombo);
			if (TypeOfPlace.FACILITY.equals(facilityOrHome.getValue())
				|| ((facilityOrHome.getValue() instanceof java.util.Set) && TypeOfPlace.FACILITY.equals(facilityOrHome.getNullableValue()))) {
				if (facilityTypeGroup.getValue() == null) {
					facilityTypeGroup.setValue(FacilityTypeGroup.MEDICAL_FACILITY);
				}
				if (facilityType.getValue() == null && FacilityTypeGroup.MEDICAL_FACILITY.equals(facilityTypeGroup.getValue())) {
					facilityType.setValue(FacilityType.HOSPITAL);
				}

				if (facilityType.getValue() != null) {
					updateFacility();
				}

				if (CaseOrigin.IN_COUNTRY.equals(ogCaseOrigin.getValue())) {
					facilityCombo.setRequired(true);
				}
				updateFacilityFields(facilityCombo, facilityDetails);
			} else if (TypeOfPlace.HOME.equals(facilityOrHome.getValue())
				|| ((facilityOrHome.getValue() instanceof java.util.Set) && TypeOfPlace.HOME.equals(facilityOrHome.getNullableValue()))) {
				setNoneFacility();
			} else {
				facilityCombo.removeAllItems();
				facilityCombo.setValue(null);
				updateFacilityFields(facilityCombo, facilityDetails);
			}
		});
		facilityTypeGroup.addValueChangeListener(e -> {
			FieldHelper.removeItems(facilityCombo);
			FieldHelper.updateEnumData(facilityType, FacilityType.getAccommodationTypes((FacilityTypeGroup) facilityTypeGroup.getValue()));
		});
		facilityType.addValueChangeListener(e -> updateFacility());
		region.addItems(FacadeProvider.getRegionFacade().getAllActiveByServerCountry());

		JurisdictionLevel userJurisditionLevel = UserRole.getJurisdictionLevel(UserProvider.getCurrent().getUserRoles());
		if (userJurisditionLevel == JurisdictionLevel.HEALTH_FACILITY) {
			region.setReadOnly(true);
			responsibleRegion.setReadOnly(true);
			districtCombo.setReadOnly(true);
			responsibleDistrictCombo.setReadOnly(true);
			communityCombo.setReadOnly(true);
			responsibleCommunityCombo.setReadOnly(true);
			differentPlaceOfStayJurisdiction.setVisible(false);
			differentPlaceOfStayJurisdiction.setEnabled(false);

			facilityOrHome.setImmediate(true);
			facilityOrHome.setValue(Sets.newHashSet(TypeOfPlace.FACILITY)); // [FACILITY]
			facilityOrHome.setReadOnly(true);
			facilityTypeGroup.setValue(FacilityTypeGroup.MEDICAL_FACILITY);
			facilityTypeGroup.setReadOnly(true);
			facilityType.setValue(FacilityType.HOSPITAL);
			facilityType.setReadOnly(true);
			facilityCombo.setValue(UserProvider.getCurrent().getUser().getHealthFacility());
			facilityCombo.setReadOnly(true);
		}

		if (!UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
			ogCaseOrigin.addValueChangeListener(ev -> {
				if (ev.getProperty().getValue() == CaseOrigin.IN_COUNTRY) {
					setVisible(false, CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS);
					setRequired(true, FACILITY_OR_HOME_LOC, FACILITY_TYPE_GROUP_LOC, CaseDataDto.FACILITY_TYPE, CaseDataDto.HEALTH_FACILITY);
					setRequired(false, CaseDataDto.POINT_OF_ENTRY);
					updateFacilityFields(facilityCombo, facilityDetails);
				} else {
					setVisible(true, CaseDataDto.POINT_OF_ENTRY);
					setRequired(true, CaseDataDto.POINT_OF_ENTRY);
					if (userJurisditionLevel != JurisdictionLevel.HEALTH_FACILITY) {
						facilityOrHome.clear();
						setRequired(false, FACILITY_OR_HOME_LOC, FACILITY_TYPE_GROUP_LOC, CaseDataDto.FACILITY_TYPE, CaseDataDto.HEALTH_FACILITY);
					}
					updatePointOfEntryFields(cbPointOfEntry, tfPointOfEntryDetails);
				}
			});
		}

		// jurisdiction field valuechangelisteners
		responsibleDistrictCombo.addValueChangeListener(e -> {
			Boolean differentPlaceOfStay = differentPlaceOfStayJurisdiction.getValue();
			if (differentPlaceOfStay == null || Boolean.FALSE.equals(differentPlaceOfStay)) {
				updateFacility();
			}

			updatePOEs();
		});
		responsibleCommunityCombo.addValueChangeListener((e) -> {
			Boolean differentPlaceOfStay = differentPlaceOfStayJurisdiction.getValue();
			if (differentPlaceOfStay == null || Boolean.FALSE.equals(differentPlaceOfStay)) {
				updateFacility();
			}
		});

		differentPlaceOfStayJurisdiction.addValueChangeListener(e -> {
			updateFacility();
			updatePOEs();
		});

		// Set initial visibilities & accesses
		initializeVisibilitiesAndAllowedVisibilities();

		setRequired(
			true,
			CaseDataDto.REPORT_DATE,
			PersonDto.FIRST_NAME,
			PersonDto.LAST_NAME,
			CaseDataDto.DISEASE,
			PersonDto.SEX,
			FACILITY_OR_HOME_LOC,
			FACILITY_TYPE_GROUP_LOC,
			CaseDataDto.FACILITY_TYPE);
		FieldHelper.addSoftRequiredStyle(plagueType, communityCombo, facilityDetails);

		FieldHelper
			.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DISEASE_DETAILS), CaseDataDto.DISEASE, Arrays.asList(Disease.OTHER), true);
		FieldHelper.setRequiredWhen(getFieldGroup(), CaseDataDto.DISEASE, Arrays.asList(CaseDataDto.DISEASE_DETAILS), Arrays.asList(Disease.OTHER));
		FieldHelper.setRequiredWhen(
			getFieldGroup(),
			CaseDataDto.CASE_ORIGIN,
			Arrays.asList(CaseDataDto.HEALTH_FACILITY),
			Arrays.asList(CaseOrigin.IN_COUNTRY));
		FieldHelper.setRequiredWhen(
			getFieldGroup(),
			CaseDataDto.CASE_ORIGIN,
			Arrays.asList(CaseDataDto.POINT_OF_ENTRY),
			Arrays.asList(CaseOrigin.POINT_OF_ENTRY));
		FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.PLAGUE_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.PLAGUE), true);
		FieldHelper
			.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DENGUE_FEVER_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.DENGUE), true);
		FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.RABIES_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.RABIES), true);
		FieldHelper.setVisibleWhen(
			facilityOrHome,
			Arrays.asList(facilityTypeGroup, facilityType, facilityCombo),
			Collections.singletonList(TypeOfPlace.FACILITY),
			false);
		FieldHelper.setRequiredWhen(
			facilityOrHome,
			Arrays.asList(facilityTypeGroup, facilityType, facilityCombo),
			Collections.singletonList(TypeOfPlace.FACILITY),
			false,
			null);

		facilityCombo.addValueChangeListener(e -> {
			updateFacilityFields(facilityCombo, facilityDetails);
			this.getValue().setFacilityType((FacilityType) facilityType.getValue());
		});

		cbPointOfEntry.addValueChangeListener(e -> {
			updatePointOfEntryFields(cbPointOfEntry, tfPointOfEntryDetails);
		});

		addValueChangeListener(e -> {
			if (UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
				setVisible(false, CaseDataDto.CASE_ORIGIN, CaseDataDto.DISEASE, CaseDataDto.COMMUNITY, CaseDataDto.HEALTH_FACILITY);
				setVisible(true, CaseDataDto.POINT_OF_ENTRY);
			}
		});
		diseaseField.addValueChangeListener((ValueChangeListener) valueChangeEvent -> {
			Disease disease = (Disease) valueChangeEvent.getProperty().getValue();
			List<DiseaseVariant> diseaseVariants =
				FacadeProvider.getCustomizableEnumFacade().getEnumValues(CustomizableEnumType.DISEASE_VARIANT, disease);
			FieldHelper.updateItems(diseaseVariantField, diseaseVariants);
			diseaseVariantField
				.setVisible(disease != null && isVisibleAllowed(CaseDataDto.DISEASE_VARIANT) && CollectionUtils.isNotEmpty(diseaseVariants));
		});
	}

	private void setNoneFacility() {
		FacilityReferenceDto noFacilityRef = FacadeProvider.getFacilityFacade().getByUuid(FacilityDto.NONE_FACILITY_UUID).toReference();
		facilityCombo.addItem(noFacilityRef);
		facilityCombo.setValue(noFacilityRef);
	}

	private void updateFacility() {

		if (UserRole.getJurisdictionLevel(UserProvider.getCurrent().getUserRoles()) == JurisdictionLevel.HEALTH_FACILITY) {
			return;
		}

		Object facilityOrHomeValue = facilityOrHome.isRequired() ? facilityOrHome.getValue() : facilityOrHome.getNullableValue();
		if (TypeOfPlace.HOME.equals(facilityOrHomeValue)) {
			setNoneFacility();
			return;
		}

		FieldHelper.removeItems(facilityCombo);

		final DistrictReferenceDto district;
		final CommunityReferenceDto community;

		if (Boolean.TRUE.equals(differentPlaceOfStayJurisdiction.getValue())) {
			district = (DistrictReferenceDto) districtCombo.getValue();
			community = (CommunityReferenceDto) communityCombo.getValue();
		} else {
			district = (DistrictReferenceDto) responsibleDistrictCombo.getValue();
			community = (CommunityReferenceDto) responsibleCommunityCombo.getValue();
		}

		if (facilityType.getValue() != null && district != null) {
			if (community != null) {
				FieldHelper.updateItems(
					facilityCombo,
					FacadeProvider.getFacilityFacade()
						.getActiveFacilitiesByCommunityAndType(community, (FacilityType) facilityType.getValue(), true, false));
			} else {
				FieldHelper.updateItems(
					facilityCombo,
					FacadeProvider.getFacilityFacade()
						.getActiveFacilitiesByDistrictAndType(district, (FacilityType) facilityType.getValue(), true, false));
			}
		}
	}

	private void updatePOEs() {

		DistrictReferenceDto districtDto = differentPlaceOfStayJurisdiction.getValue()
			? (DistrictReferenceDto) districtCombo.getValue()
			: (DistrictReferenceDto) responsibleDistrictCombo.getValue();

		List<PointOfEntryReferenceDto> POEs = districtDto == null
			? Collections.emptyList()
			: FacadeProvider.getPointOfEntryFacade().getAllActiveByDistrict(districtDto.getUuid(), true);
		FieldHelper.updateItems((ComboBox) getField(CaseDataDto.POINT_OF_ENTRY), POEs);
	}

	private void updateListOfDays(Integer selectedYear, Integer selectedMonth) {

		Integer currentlySelected = (Integer) birthDateDay.getValue();
		birthDateDay.removeAllItems();
		birthDateDay.addItems(DateHelper.getDaysInMonth(selectedMonth, selectedYear));
		if (birthDateDay.containsId(currentlySelected)) {
			birthDateDay.setValue(currentlySelected);
		}
	}

	private void updateFacilityFields(ComboBox cbFacility, TextField tfFacilityDetails) {

		if (cbFacility.getValue() != null) {
			boolean otherHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
			boolean noneHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.NONE_FACILITY_UUID);
			boolean visibleAndRequired = otherHealthFacility || noneHealthFacility;

			tfFacilityDetails.setVisible(visibleAndRequired);
			tfFacilityDetails.setRequired(otherHealthFacility);

			if (otherHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.HEALTH_FACILITY_DETAILS));
			}
			if (noneHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getCaption(Captions.CaseData_noneHealthFacilityDetails));
			}
			if (!visibleAndRequired) {
				tfFacilityDetails.clear();
			}
		} else if (((facilityOrHome.getValue() instanceof java.util.Set)
			&& (facilityOrHome.getNullableValue() == null || TypeOfPlace.FACILITY.equals(facilityOrHome.getNullableValue())))
			|| TypeOfPlace.FACILITY.equals(facilityOrHome.getValue())) {
			tfFacilityDetails.setVisible(false);
			tfFacilityDetails.setRequired(false);
			tfFacilityDetails.clear();
		}
	}

	private void updatePointOfEntryFields(ComboBox cbPointOfEntry, TextField tfPointOfEntryDetails) {

		if (cbPointOfEntry.getValue() != null) {
			boolean isOtherPointOfEntry = ((PointOfEntryReferenceDto) cbPointOfEntry.getValue()).isOtherPointOfEntry();
			setVisible(isOtherPointOfEntry, CaseDataDto.POINT_OF_ENTRY_DETAILS);
			setRequired(isOtherPointOfEntry, CaseDataDto.POINT_OF_ENTRY_DETAILS);
			if (!isOtherPointOfEntry) {
				tfPointOfEntryDetails.clear();
			}
		} else {
			tfPointOfEntryDetails.setVisible(false);
			tfPointOfEntryDetails.setRequired(false);
			tfPointOfEntryDetails.clear();
		}
	}

	private void setItemCaptionsForMonths(AbstractSelect months) {

		months.setItemCaption(1, I18nProperties.getEnumCaption(Month.JANUARY));
		months.setItemCaption(2, I18nProperties.getEnumCaption(Month.FEBRUARY));
		months.setItemCaption(3, I18nProperties.getEnumCaption(Month.MARCH));
		months.setItemCaption(4, I18nProperties.getEnumCaption(Month.APRIL));
		months.setItemCaption(5, I18nProperties.getEnumCaption(Month.MAY));
		months.setItemCaption(6, I18nProperties.getEnumCaption(Month.JUNE));
		months.setItemCaption(7, I18nProperties.getEnumCaption(Month.JULY));
		months.setItemCaption(8, I18nProperties.getEnumCaption(Month.AUGUST));
		months.setItemCaption(9, I18nProperties.getEnumCaption(Month.SEPTEMBER));
		months.setItemCaption(10, I18nProperties.getEnumCaption(Month.OCTOBER));
		months.setItemCaption(11, I18nProperties.getEnumCaption(Month.NOVEMBER));
		months.setItemCaption(12, I18nProperties.getEnumCaption(Month.DECEMBER));
	}

	public String getPersonFirstName() {
		return (String) getField(PersonDto.FIRST_NAME).getValue();
	}

	public String getPersonLastName() {
		return (String) getField(PersonDto.LAST_NAME).getValue();
	}

	public String getNationalHealthId() {
		return (String) getField(PersonDto.NATIONAL_HEALTH_ID).getValue();
	}

	public String getPassportNumber() {
		return (String) getField(PersonDto.PASSPORT_NUMBER).getValue();
	}

	public Integer getBirthdateDD() {
		return (Integer) getField(PersonDto.BIRTH_DATE_DD).getValue();
	}

	public Integer getBirthdateMM() {
		return (Integer) getField(PersonDto.BIRTH_DATE_MM).getValue();
	}

	public Integer getBirthdateYYYY() {
		return (Integer) getField(PersonDto.BIRTH_DATE_YYYY).getValue();
	}

	public Sex getSex() {
		return (Sex) getField(PersonDto.SEX).getValue();
	}

	public PresentCondition getPresentCondition() {
		return (PresentCondition) getField(PersonDto.PRESENT_CONDITION).getValue();
	}

	public Date getOnsetDate() {
		return (Date) getField(SymptomsDto.ONSET_DATE).getValue();
	}

	public String getPhone() {
		return (String) getField(PersonDto.PHONE).getValue();
	}

	public String getEmailAddress() {
		return (String) getField(PersonDto.EMAIL_ADDRESS).getValue();
	}

	public void setPerson(PersonDto person) {

		if (person != null) {
			((TextField) getField(PersonDto.FIRST_NAME)).setValue(person.getFirstName());
			((TextField) getField(PersonDto.LAST_NAME)).setValue(person.getLastName());
			((ComboBox) getField(PersonDto.BIRTH_DATE_YYYY)).setValue(person.getBirthdateYYYY());
			((ComboBox) getField(PersonDto.BIRTH_DATE_MM)).setValue(person.getBirthdateMM());
			((ComboBox) getField(PersonDto.BIRTH_DATE_DD)).setValue(person.getBirthdateDD());
			((ComboBox) getField(PersonDto.SEX)).setValue(person.getSex());
			((ComboBox) getField(PersonDto.PRESENT_CONDITION)).setValue(person.getPresentCondition());
			((TextField) getField(PersonDto.PHONE)).setValue(person.getPhone());
			((TextField) getField(PersonDto.EMAIL_ADDRESS)).setValue(person.getEmailAddress());
		} else {
			getField(PersonDto.FIRST_NAME).clear();
			getField(PersonDto.LAST_NAME).clear();
			getField(PersonDto.BIRTH_DATE_DD).clear();
			getField(PersonDto.BIRTH_DATE_MM).clear();
			getField(PersonDto.BIRTH_DATE_YYYY).clear();
			getField(PersonDto.SEX).clear();
			getField(PersonDto.PRESENT_CONDITION).clear();
			getField(PersonDto.PHONE).clear();
			getField(PersonDto.EMAIL_ADDRESS).clear();
		}
	}

	public void setSymptoms(SymptomsDto symptoms) {

		if (symptoms != null) {
			((DateField) getField(SymptomsDto.ONSET_DATE)).setValue(symptoms.getOnsetDate());
		} else {
			getField(SymptomsDto.ONSET_DATE).clear();
		}
	}

	public void setPersonalDetailsReadOnlyIfNotEmpty(boolean readOnly) {

		getField(PersonDto.FIRST_NAME).setEnabled(!readOnly);
		getField(PersonDto.LAST_NAME).setEnabled(!readOnly);
		if (getField(PersonDto.SEX).getValue() != null) {
			getField(PersonDto.SEX).setEnabled(!readOnly);
		}
		if (getField(PersonDto.BIRTH_DATE_YYYY).getValue() != null) {
			getField(PersonDto.BIRTH_DATE_YYYY).setEnabled(!readOnly);
		}
		if (getField(PersonDto.BIRTH_DATE_MM).getValue() != null) {
			getField(PersonDto.BIRTH_DATE_MM).setEnabled(!readOnly);
		}
		if (getField(PersonDto.BIRTH_DATE_DD).getValue() != null) {
			getField(PersonDto.BIRTH_DATE_DD).setEnabled(!readOnly);
		}
	}

	public void setDiseaseReadOnly(boolean readOnly) {
		getField(CaseDataDto.DISEASE).setEnabled(!readOnly);
	}

	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}
}
