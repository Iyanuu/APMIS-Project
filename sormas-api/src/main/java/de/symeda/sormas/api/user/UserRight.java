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
package de.symeda.sormas.api.user;

import static de.symeda.sormas.api.user.UserRole.ADMIN;
import static de.symeda.sormas.api.user.UserRole.CASE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CASE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.COMMUNITY_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.CONTACT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CONTACT_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.DISTRICT_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.EVENT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.EXTERNAL_LAB_USER;
import static de.symeda.sormas.api.user.UserRole.HOSPITAL_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.IMPORT_USER;
import static de.symeda.sormas.api.user.UserRole.LAB_USER;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_CLINICIAN;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_USER;
import static de.symeda.sormas.api.user.UserRole.POE_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.POE_NATIONAL_USER;
import static de.symeda.sormas.api.user.UserRole.POE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.STATE_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_SUPERVISOR;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum UserRight {

	//@formatter:off
	CASE_CREATE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EVENT_OFFICER
	),
	CASE_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EVENT_OFFICER
	),
	CASE_EDIT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EVENT_OFFICER
	),
	CASE_TRANSFER(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR
	),
	CASE_REFER_FROM_POE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			POE_SUPERVISOR
	),
	/*
	 * Edit the investigation status - either by setting a respective task to done or by manually changing it in the case
	 */
	CASE_INVESTIGATE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR
	),
	/*
	 * Edit the classification and outcome of a case
	 */
	CASE_CLASSIFY(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			LAB_USER
	),
	CASE_CHANGE_DISEASE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR
	),
	CASE_CHANGE_EPID_NUMBER(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			LAB_USER,
			EVENT_OFFICER
	),
	CASE_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	CASE_IMPORT(
			ADMIN,
			IMPORT_USER
	),
	CASE_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER
	),
	CASE_SHARE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR
	),
	CASE_ARCHIVE(
			ADMIN
	),
	CASE_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CASE_SUPERVISOR,
			POE_SUPERVISOR,
			CONTACT_SUPERVISOR
	),
	CASE_MERGE(
			ADMIN
	),
	SAMPLE_CREATE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER
	),
	SAMPLE_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EXTERNAL_LAB_USER,
			EVENT_OFFICER
	),
	SAMPLE_EDIT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	SAMPLE_EDIT_NOT_OWNED(
			ADMIN,
			SURVEILLANCE_SUPERVISOR
	),
	SAMPLE_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	SAMPLE_TRANSFER(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	SAMPLE_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			LAB_USER
	),
	SAMPLE_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR
	),
	PATHOGEN_TEST_CREATE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	PATHOGEN_TEST_EDIT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	PATHOGEN_TEST_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	ADDITIONAL_TEST_VIEW(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	ADDITIONAL_TEST_CREATE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	ADDITIONAL_TEST_EDIT(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			LAB_USER,
			EXTERNAL_LAB_USER
	),
	ADDITIONAL_TEST_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	CONTACT_CREATE(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	CONTACT_IMPORT(ADMIN, IMPORT_USER),
	CONTACT_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			EVENT_OFFICER
	),
	CONTACT_ASSIGN(
			ADMIN,
			NATIONAL_USER,
			CONTACT_SUPERVISOR
	),
	CONTACT_EDIT(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	CONTACT_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	CONTACT_CLASSIFY(
			ADMIN,
			NATIONAL_USER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	// users that are allowed to convert a contact to a case need to be allowed to create a case
	CONTACT_CONVERT(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	CONTACT_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			LAB_USER
	),
	CONTACT_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR
	),
	// reassign or remove the case from an existing contact
	CONTACT_REASSIGN_CASE(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CONTACT_OFFICER
	),
	CONTACT_CREATE_PIA_ACCOUNT(
			NATIONAL_USER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	VISIT_CREATE(
			ADMIN,
			NATIONAL_USER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	VISIT_EDIT(
			ADMIN,
			NATIONAL_USER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	VISIT_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	VISIT_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			LAB_USER
	),
	TASK_CREATE(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER,
			SURVEILLANCE_OFFICER
	),
	TASK_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EXTERNAL_LAB_USER,
			EVENT_OFFICER
	),
	TASK_EDIT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EXTERNAL_LAB_USER,
			EVENT_OFFICER
	),
	TASK_ASSIGN(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER,
			SURVEILLANCE_OFFICER
	),
	TASK_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR
	),
	TASK_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	EVENT_CREATE(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			EVENT_OFFICER
	),
	EVENT_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			EVENT_OFFICER
	),
	EVENT_EDIT(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			EVENT_OFFICER
	),
	EVENT_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER
	),
	EVENT_ARCHIVE(
			ADMIN
	),
	EVENT_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	EVENT_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR
	),
	EVENTPARTICIPANT_CREATE(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			EVENT_OFFICER
	),
	EVENTPARTICIPANT_EDIT(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			EVENT_OFFICER
	),
	EVENTPARTICIPANT_DELETE(
			ADMIN,
			NATIONAL_USER
	),
	WEEKLYREPORT_CREATE(
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			SURVEILLANCE_OFFICER
	),
	WEEKLYREPORT_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT
	),
	USER_CREATE(
			ADMIN
			//			NATIONAL_USER,
			//			SURVEILLANCE_SUPERVISOR,
			//			CASE_SUPERVISOR,
			//			CONTACT_SUPERVISOR,
			//			LAB_USER,
			//			EVENT_OFFICER
	),
	USER_EDIT(
			ADMIN
			//			NATIONAL_USER,
			//			SURVEILLANCE_SUPERVISOR,
			//			CASE_SUPERVISOR,
			//			CONTACT_SUPERVISOR,
			//			LAB_USER,
			//			EVENT_OFFICER
	),
	USER_VIEW(
			ADMIN
			//			NATIONAL_USER,
			//			SURVEILLANCE_SUPERVISOR,
			//			CASE_SUPERVISOR,
			//			CONTACT_SUPERVISOR,
			//			LAB_USER,
			//			EVENT_OFFICER
	),
	CONFIGURATION_ACCESS(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			POE_SUPERVISOR
	),
	OUTBREAK_CONFIGURE_ALL(
			ADMIN,
			NATIONAL_USER
	),
	OUTBREAK_CONFIGURE_RESTRICTED(
			SURVEILLANCE_SUPERVISOR
	),
	STATISTICS_ACCESS(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER
	),
	STATISTICS_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER
	),
	DATABASE_EXPORT_ACCESS(
			ADMIN,
			NATIONAL_USER
	),
	PERFORM_BULK_OPERATIONS(
			ADMIN
	),
	INFRASTRUCTURE_CREATE(
			ADMIN
	),
	INFRASTRUCTURE_EDIT(
			ADMIN
	),
	INFRASTRUCTURE_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			POE_SUPERVISOR
	),
	INFRASTRUCTURE_VIEW_ARCHIVED(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_OBSERVER
	),
	INFRASTRUCTURE_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			POE_SUPERVISOR
	),
	INFRASTRUCTURE_IMPORT(
			ADMIN
	),
	INFRASTRUCTURE_ARCHIVE(
			ADMIN,
			NATIONAL_USER
	),
	USER_RIGHTS_MANAGE(
			ADMIN
	),
	DASHBOARD_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER,
			EVENT_OFFICER,
			SURVEILLANCE_OFFICER,
			CASE_OFFICER,
			CONTACT_OFFICER
	),
	DASHBOARD_SURVEILLANCE_ACCESS(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			POE_SUPERVISOR,
			LAB_USER,
			EVENT_OFFICER,
			SURVEILLANCE_OFFICER,
			CASE_OFFICER
	),
	DASHBOARD_CONTACT_ACCESS(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER
	),
	DASHBOARD_CONTACT_VIEW_TRANSMISSION_CHAINS(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_OBSERVER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			CONTACT_SUPERVISOR
	),
	CASE_MANAGEMENT_ACCESS(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			LAB_USER
	),
	THERAPY_VIEW(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	PRESCRIPTION_CREATE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	PRESCRIPTION_EDIT(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	PRESCRIPTION_DELETE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR
	),
	TREATMENT_CREATE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	TREATMENT_EDIT(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	TREATMENT_DELETE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR
	),
	CLINICAL_COURSE_VIEW(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	CLINICAL_COURSE_EDIT(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	CLINICAL_VISIT_CREATE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	CLINICAL_VISIT_EDIT(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR,
			CASE_OFFICER
	),
	CLINICAL_VISIT_DELETE(
			ADMIN,
			NATIONAL_CLINICIAN,
			CASE_SUPERVISOR
	),
	PORT_HEALTH_INFO_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			LAB_USER,
			EVENT_OFFICER
	),
	PORT_HEALTH_INFO_EDIT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			POE_SUPERVISOR,
			POE_INFORMANT
	),
	POPULATION_MANAGE(
			ADMIN
	),
	LINE_LISTING_CONFIGURE(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR),
	LINE_LISTING_CONFIGURE_NATION(
			ADMIN,
			NATIONAL_USER),
	AGGREGATE_REPORT_VIEW(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			NATIONAL_OBSERVER,
			POE_NATIONAL_USER,
			STATE_OBSERVER,
			DISTRICT_OBSERVER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR,
			POE_INFORMANT,
			HOSPITAL_INFORMANT
	),
	AGGREGATE_REPORT_EXPORT(
			ADMIN,
			NATIONAL_USER,
			NATIONAL_CLINICIAN,
			POE_NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			POE_SUPERVISOR
	),
	AGGREGATE_REPORT_EDIT(ADMIN, NATIONAL_USER, NATIONAL_CLINICIAN, NATIONAL_OBSERVER, POE_NATIONAL_USER,
			STATE_OBSERVER, DISTRICT_OBSERVER, SURVEILLANCE_SUPERVISOR, CASE_SUPERVISOR, CONTACT_SUPERVISOR,
			POE_SUPERVISOR, POE_INFORMANT, HOSPITAL_INFORMANT
	),
	SEE_PERSONAL_DATA_IN_JURISDICTION(
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			EVENT_OFFICER,
			NATIONAL_USER,
			POE_SUPERVISOR,
			POE_NATIONAL_USER,
			SURVEILLANCE_OFFICER,
			CASE_OFFICER,
			CONTACT_OFFICER,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			POE_INFORMANT),
	SEE_PERSONAL_DATA_OUTSIDE_JURISDICTION(),
	SEE_SENSITIVE_DATA_IN_JURISDICTION(
			SURVEILLANCE_SUPERVISOR,
			CASE_SUPERVISOR,
			CONTACT_SUPERVISOR,
			EVENT_OFFICER,
			NATIONAL_USER,
			POE_SUPERVISOR,
			POE_NATIONAL_USER,
			SURVEILLANCE_OFFICER,
			CASE_OFFICER,
			CONTACT_OFFICER,
			HOSPITAL_INFORMANT,
			COMMUNITY_INFORMANT,
			POE_INFORMANT),
	SEE_SENSITIVE_DATA_OUTSIDE_JURISDICTION(),
  CAMPAIGN_VIEW(
      ADMIN,
      NATIONAL_USER,
      SURVEILLANCE_SUPERVISOR,
      SURVEILLANCE_OFFICER,
      CASE_SUPERVISOR,
      CASE_OFFICER,
			CONTACT_SUPERVISOR,
      CONTACT_OFFICER,
      EVENT_OFFICER,
			POE_SUPERVISOR),
	CAMPAIGN_EDIT(
      ADMIN,
      NATIONAL_USER),
	CAMPAIGN_ARCHIVE(
      ADMIN,
      NATIONAL_USER),
	CAMPAIGN_DELETE(
      ADMIN),

	CAMPAIGN_FORM_DATA_VIEW(
			ADMIN,
			NATIONAL_USER,
			SURVEILLANCE_SUPERVISOR,
			SURVEILLANCE_OFFICER,
			CASE_SUPERVISOR,
			CASE_OFFICER,
			CONTACT_SUPERVISOR,
			CONTACT_OFFICER,
			EVENT_OFFICER,
			POE_SUPERVISOR
	),
	CAMPAIGN_FORM_DATA_EDIT(
			ADMIN,
			NATIONAL_USER
	),
	CAMPAIGN_FORM_DATA_ARCHIVE(
			ADMIN, NATIONAL_USER
	),
	CAMPAIGN_FORM_DATA_DELETE(
			ADMIN
	);

	//@formatter:on

	private final Set<UserRole> defaultUserRoles;

	UserRight(UserRole... defaultUserRoles) {

		this.defaultUserRoles = defaultUserRoles.length > 0
			? Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(defaultUserRoles)))
			: Collections.<UserRole> emptySet();
	}

	public boolean isDefaultForRole(UserRole userRole) {
		return defaultUserRoles.contains(userRole);
	}

	public Set<UserRole> getDefaultUserRoles() {
		return defaultUserRoles;
	}
}
