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
package de.symeda.sormas.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.VisitOrigin;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.infrastructure.facility.FacilityDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityType;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.WeeklyReportDto;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SamplePurpose;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskDto;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.visit.VisitDto;
import de.symeda.sormas.api.visit.VisitStatus;

public class TestDataCreator {

	public TestDataCreator() {

	}

	public UserDto createUser(String regionUuid, String districtUuid, String facilityUuid, String firstName, String lastName, UserRole... roles) {
		return createUser(regionUuid, districtUuid, facilityUuid, null, firstName, lastName, Language.EN, roles);
	}

	public UserDto createPointOfEntryUser(String regionUuid, String districtUuid, String pointOfEntryUuid) {
		return createUser(regionUuid, districtUuid, null, pointOfEntryUuid, "POE", "User", Language.EN, UserRole.POE_INFORMANT);
	}

	public UserDto createUser(
		String regionUuid,
		String districtUuid,
		String facilityUuid,
		String pointOfEntryUuid,
		String firstName,
		String lastName,
		Language language,
		UserRole... roles) {

		UserDto user = UserDto.build();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUserName(firstName + lastName);
		user.setUserRoles(new HashSet<UserRole>(Arrays.asList(roles)));
		user.setRegion(FacadeProvider.getRegionFacade().getRegionReferenceByUuid(regionUuid));
		user.setDistrict(FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(districtUuid));
		user.setHealthFacility(FacadeProvider.getFacilityFacade().getFacilityReferenceByUuid(facilityUuid));
		if (pointOfEntryUuid != null) {
			PointOfEntryDto pointOfEntry = FacadeProvider.getPointOfEntryFacade().getByUuid(pointOfEntryUuid);
			if (pointOfEntry != null) {
				user.setPointOfEntry(pointOfEntry.toReference());
			}
		}
		user.setLanguage(language);
		user = FacadeProvider.getUserFacade().saveUser(user);

		return user;
	}

	public PersonDto createPerson(String firstName, String lastName) {

		PersonDto cazePerson = PersonDto.build();
		cazePerson.setFirstName(firstName);
		cazePerson.setLastName(lastName);
		cazePerson = FacadeProvider.getPersonFacade().savePerson(cazePerson);

		return cazePerson;
	}

	public EventParticipantDto createEventParticipant(EventReferenceDto event, PersonDto eventPerson, UserReferenceDto reportingUser) {
		return createEventParticipant(event, eventPerson, "Description", reportingUser);
	}

	public EventParticipantDto createEventParticipant(
		EventReferenceDto event,
		PersonDto eventPerson,
		String involvementDescription,
		UserReferenceDto reportingUser) {

		EventParticipantDto eventParticipant = EventParticipantDto.build(event, reportingUser);
		eventParticipant.setPerson(eventPerson);
		eventParticipant.setInvolvementDescription(involvementDescription);

		eventParticipant = FacadeProvider.getEventParticipantFacade().saveEventParticipant(eventParticipant);
		return eventParticipant;
	}

	public ContactDto createContact(
		UserReferenceDto reportingUser,
		UserReferenceDto contactOfficer,
		PersonReferenceDto contactPerson,
		CaseDataDto caze,
		Date reportDateTime,
		Date lastContactDate) {

		ContactDto contact = ContactDto.build(caze);
		contact.setReportingUser(reportingUser);
		contact.setContactOfficer(contactOfficer);
		contact.setPerson(contactPerson);
		contact.setReportDateTime(reportDateTime);
		contact.setLastContactDate(lastContactDate);

		contact = FacadeProvider.getContactFacade().saveContact(contact);

		return contact;
	}

	public CaseDataDto createUnclassifiedCase(Disease disease) {

		RDCF rdcf = createRDCF("Region", "District", "Community", "Facility");
		UserDto user =
			createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = createPerson("Case", "Person");
		return createCase(
			user.toReference(),
			cazePerson.toReference(),
			disease,
			CaseClassification.NOT_CLASSIFIED,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportDate,
		RDCF rdcf,
		String caseUuid) {

		CaseDataDto caze = CaseDataDto.build(cazePerson, disease);
		if (caseUuid != null) {
			caze.setUuid(caseUuid);
		}
		caze.setReportDate(reportDate);
		caze.setReportingUser(user);
		caze.setCaseClassification(caseClassification);
		caze.setInvestigationStatus(investigationStatus);
		caze.setResponsibleRegion(FacadeProvider.getRegionFacade().getRegionReferenceByUuid(rdcf.region.getUuid()));
		caze.setResponsibleDistrict(FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(rdcf.district.getUuid()));
		caze.setResponsibleCommunity(FacadeProvider.getCommunityFacade().getCommunityReferenceByUuid(rdcf.community.getUuid()));
		FacilityDto facility = FacadeProvider.getFacilityFacade().getByUuid(rdcf.facility.getUuid());
		caze.setFacilityType(facility.getType());
		caze.setHealthFacility(facility.toReference());

		caze = FacadeProvider.getCaseFacade().saveCase(caze);

		return caze;
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportDate,
		RDCF rdcf) {

		return createCase(user, cazePerson, disease, caseClassification, investigationStatus, reportDate, rdcf, null);
	}

	public TaskDto createTask(
		TaskContext context,
		TaskType type,
		TaskStatus status,
		CaseReferenceDto caze,
		ContactReferenceDto contact,
		EventReferenceDto event,
		Date dueDate,
		UserReferenceDto assigneeUser) {

		ReferenceDto entityRef;
		switch (context) {
		case CASE:
			entityRef = caze;
			break;
		case CONTACT:
			entityRef = contact;
			break;
		case EVENT:
			entityRef = event;
			break;
		case GENERAL:
			entityRef = null;
			break;
		default:
			throw new IllegalArgumentException(context.toString());
		}

		TaskDto task = TaskDto.build(context, entityRef);
		task.setTaskType(type);
		task.setTaskStatus(status);
		task.setDueDate(dueDate);
		task.setAssigneeUser(assigneeUser);

		task = FacadeProvider.getTaskFacade().saveTask(task);

		return task;
	}

	public VisitDto createVisit(Disease disease, PersonReferenceDto contactPerson, Date visitDateTime, VisitStatus visitStatus) {

		VisitDto visit = VisitDto.build(contactPerson, disease, VisitOrigin.USER);
		visit.setVisitDateTime(visitDateTime);
		visit.setVisitStatus(visitStatus);
		visit = FacadeProvider.getVisitFacade().saveVisit(visit);

		return visit;
	}

	public WeeklyReportDto createWeeklyReport(
		String facilityUuid,
		UserReferenceDto informant,
		Date reportDateTime,
		int epiWeek,
		int year,
		int numberOfCases) {

		WeeklyReportDto report = new WeeklyReportDto();
		report.setUuid(DataHelper.createUuid());
		report.setHealthFacility(FacadeProvider.getFacilityFacade().getFacilityReferenceByUuid(facilityUuid));
		report.setReportingUser(informant);
		report.setReportDateTime(reportDateTime);
		report.setEpiWeek(epiWeek);
		report.setYear(year);
		report.setTotalNumberOfCases(numberOfCases);

		report = FacadeProvider.getWeeklyReportFacade().saveWeeklyReport(report);

		return report;
	}

	public EventDto createEvent(
		EventStatus eventStatus,
		String eventTitle,
		String eventDesc,
		String srcFirstName,
		String srcLastName,
		String srcTelNo,
		TypeOfPlace typeOfPlace,
		Date eventDate,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		UserReferenceDto responsibleUser,
		Disease disease) {

		EventDto event = EventDto.build();
		event.setEventStatus(eventStatus);
		event.setEventTitle(eventTitle);
		event.setEventDesc(eventDesc);
		event.setSrcFirstName(srcFirstName);
		event.setSrcLastName(srcLastName);
		event.setSrcTelNo(srcTelNo);
		event.setTypeOfPlace(typeOfPlace);
		event.setStartDate(eventDate);
		event.setReportDateTime(reportDateTime);
		event.setReportingUser(reportingUser);
		event.setResponsibleUser(responsibleUser);
		event.setDisease(disease);

		event = FacadeProvider.getEventFacade().saveEvent(event);

		return event;
	}

	public SampleDto createSample(
		CaseReferenceDto associatedCase,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		FacilityReferenceDto lab) {

		SampleDto sample = SampleDto.build(reportingUser, associatedCase);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(lab);

		sample = FacadeProvider.getSampleFacade().saveSample(sample);

		return sample;
	}

	public RDCF createRDCF(String regionName, String districtName, String communityName, String facilityName) {

		RegionDto region = createRegion(regionName);
		DistrictDto district = createDistrict(districtName, region.toReference());
		CommunityDto community = createCommunity(communityName, district.toReference());
		FacilityDto facility = createFacility(facilityName, region.toReference(), district.toReference(), community.toReference());

		return new RDCF(region, district, community, facility);
	}

	public RDP createRDP() {

		RegionDto region = createRegion("Region");
		DistrictDto district = createDistrict("District", region.toReference());
		PointOfEntryDto pointOfEntry = createPointOfEntry("POE", region.toReference(), district.toReference());

		return new RDP(region, district, pointOfEntry);
	}

	public RegionDto createRegion(String regionName) {

		RegionDto region = RegionDto.build();
		region.setUuid(DataHelper.createUuid());
		region.setName(regionName);
		FacadeProvider.getRegionFacade().save(region);
		return region;
	}

	public DistrictDto createDistrict(String districtName, RegionReferenceDto region) {

		DistrictDto district = DistrictDto.build();
		district.setUuid(DataHelper.createUuid());
		district.setName(districtName);
		district.setRegion(region);
		FacadeProvider.getDistrictFacade().save(district);

		return district;
	}

	public CommunityDto createCommunity(String communityName, DistrictReferenceDto district) {

		CommunityDto community = CommunityDto.build();
		community.setUuid(DataHelper.createUuid());
		community.setName(communityName);
		community.setDistrict(district);
		FacadeProvider.getCommunityFacade().save(community);

		return community;
	}

	public FacilityDto createFacility(
		String facilityName,
		RegionReferenceDto region,
		DistrictReferenceDto district,
		CommunityReferenceDto community) {

		FacilityDto facility = FacilityDto.build();
		facility.setUuid(DataHelper.createUuid());
		facility.setName(facilityName);
		facility.setType(FacilityType.HOSPITAL);
		facility.setCommunity(community);
		facility.setDistrict(district);
		facility.setRegion(region);
		FacadeProvider.getFacilityFacade().save(facility);
		return facility;
	}

	public PointOfEntryDto createPointOfEntry(String pointOfEntryName, RegionReferenceDto region, DistrictReferenceDto district) {

		PointOfEntryDto pointOfEntry = PointOfEntryDto.build();
		pointOfEntry.setUuid(DataHelper.createUuid());
		pointOfEntry.setName(pointOfEntryName);
		pointOfEntry.setActive(true);
		pointOfEntry.setRegion(region);
		pointOfEntry.setDistrict(district);
		pointOfEntry.setPointOfEntryType(PointOfEntryType.AIRPORT);
		FacadeProvider.getPointOfEntryFacade().save(pointOfEntry);
		return pointOfEntry;
	}

	public CampaignDto createCampaign(UserDto user) {

		CampaignDto campaign = CampaignDto.build();
		campaign.setCreatingUser(user.toReference());
		campaign.setName("CampaignName");
		campaign.setDescription("Campaign description");

		campaign = FacadeProvider.getCampaignFacade().saveCampaign(campaign);

		return campaign;
	}

	public CampaignFormMetaDto createCampaignForm(CampaignDto campaign) throws IOException {

		CampaignFormMetaDto campaignForm;

		String schema = "[{\n" + "  \"type\": \"section\",\n" + "  \"id\": \"totalNumbersSection\"\n" + "}, {\n" + "  \"type\": \"label\",\n"
			+ "  \"id\": \"totalNumbersLabel\",\n" + "  \"caption\": \"<h3>Total Numbers</h3>\"\n" + "}, {\n" + "  \"type\": \"number\",\n"
			+ "  \"id\": \"infected\",\n" + "  \"caption\": \"Number of infected\",\n" + "  \"styles\": [\"row\", \"col-3\"],\n"
			+ "  \"important\": true\n" + "}, {\n" + "  \"type\": \"number\",\n" + "  \"id\": \"withAntibodies\",\n"
			+ "  \"caption\": \"Number persons with antibodies\",\n" + "  \"styles\": [\"row\", \"col-3\"]\n" + "}, {\n" + "  \"type\": \"yes-no\",\n"
			+ "  \"id\": \"mostlyNonBelievers\",\n" + "  \"caption\": \"Mostly non believers?\",\n" + "  \"important\": true\n" + "}]";

		campaignForm = FacadeProvider.getCampaignFormMetaFacade().buildCampaignFormMetaFromJson("testForm", null, schema, null);

		campaignForm = FacadeProvider.getCampaignFormMetaFacade().saveCampaignFormMeta(campaignForm);

		return campaignForm;
	}

	public static class RDCF {

		public RegionDto region;
		public DistrictDto district;
		public CommunityDto community;
		public FacilityDto facility;

		public RDCF(RegionDto region, DistrictDto district, CommunityDto community, FacilityDto facility) {
			this.region = region;
			this.district = district;
			this.community = community;
			this.facility = facility;
		}
	}

	public static class RDP {

		public RegionDto region;
		public DistrictDto district;
		public PointOfEntryDto pointOfEntry;

		public RDP(RegionDto region, DistrictDto district, PointOfEntryDto pointOfEntry) {
			this.region = region;
			this.district = district;
			this.pointOfEntry = pointOfEntry;
		}
	}

}
