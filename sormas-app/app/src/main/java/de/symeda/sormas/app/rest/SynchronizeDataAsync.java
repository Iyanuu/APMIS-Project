/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.perf.metrics.Trace;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.infrastructure.InfrastructureChangeDatesDto;
import de.symeda.sormas.api.infrastructure.InfrastructureSyncDto;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.campaign.CampaignDtoHelper;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataDtoHelper;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaDtoHelper;

import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaRegionDtoHelper;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaWithExpDtoHelper;
import de.symeda.sormas.app.backend.campaign.usertoken.FCMTokenDtoHelper;
import de.symeda.sormas.app.backend.caze.CaseDtoHelper;
import de.symeda.sormas.app.backend.classification.DiseaseClassificationDtoHelper;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalVisitDtoHelper;

import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.device.errorLog.DeviceErrorLogDtoHelper;
import de.symeda.sormas.app.backend.device.info.DeviceInfoDtoHelper;
import de.symeda.sormas.app.backend.document.DocumentDtoHelper;
import de.symeda.sormas.app.backend.feature.FeatureConfigurationDtoHelper;
import de.symeda.sormas.app.backend.infrastructure.InfrastructureHelper;
import de.symeda.sormas.app.backend.region.AreaDtoHelper;
import de.symeda.sormas.app.backend.region.CommunityDtoHelper;
import de.symeda.sormas.app.backend.region.DistrictDtoHelper;
import de.symeda.sormas.app.backend.region.PopulationDataDtoHelper;
import de.symeda.sormas.app.backend.region.RegionDtoHelper;
import de.symeda.sormas.app.backend.user.UserDtoHelper;
import de.symeda.sormas.app.backend.user.UserRoleConfigDtoHelper;
import de.symeda.sormas.app.core.TaskNotificationService;
import de.symeda.sormas.app.util.ErrorReportingHelper;
import de.symeda.sormas.app.util.SyncCallback;
import retrofit2.Call;
import retrofit2.Response;

public class SynchronizeDataAsync extends AsyncTask<Void, Void, Void> {

	/**
	 * Should be set to true when the synchronization fails and reset to false as soon
	 * as the last callback is called (i.e. the synchronization has been completed/cancelled).
	 */
	protected boolean syncFailed;
	protected boolean resyncDataRepull;
	protected String syncFailedMessage;

	private SyncMode syncMode;
	private final Context context;

	private SynchronizeDataAsync(SyncMode syncMode, Context context) {
		this.syncMode = syncMode;
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (!RetroProvider.isConnected()) {
			return null;
		}

		if (ConfigProvider.isRepullNeeded()) {
			syncMode = SyncMode.CompleteAndRepull;
		}

		try {
			Trace syncModeTrace = null;
			switch (syncMode) {
			case Changes:
				System.out.println("++++++++----------------------- changes");
				syncModeTrace = FirebasePerformance.getInstance().newTrace("syncModeChangesTrace");
				syncModeTrace.start();

				// infrastructure always has to be pulled - otherwise referenced data may be lost (e.g. #586)
				pullInfrastructure();
				// pull and remove deleted entities when the last time this has been done is more than 24 hours ago
				if (ConfigProvider.getLastDeletedSyncDate() == null
					|| DateHelper.getFullDaysBetween(ConfigProvider.getLastDeletedSyncDate(), new Date()) >= 1) {

					pullAndRemoveDeletedUuidsSince(ConfigProvider.getLastDeletedSyncDate());
				}
				// pull and remove archived entities when the last time this has been done is more than 24 hours ago
				if (ConfigProvider.getLastArchivedSyncDate() == null
					|| DateHelper.getFullDaysBetween(ConfigProvider.getLastArchivedSyncDate(), new Date()) >= 1) {

					pullAndRemoveArchivedUuidsSince(ConfigProvider.getLastArchivedSyncDate());
				}
				//synchronizeChangedData();


				pullMissingAndDeleteInvalidInfrastructure();
				//repullData();
				pushNewPullMissingAndDeleteInvalidData();
				synchronizeChangedData();
				ConfigProvider.setRepullNeeded(false);


				syncModeTrace.stop();
				break;


			case CompleteAndRepull:

				System.out.println("+++++++++---------------------- CompleteAndRepull");

				resyncDataRepull = true;
				syncModeTrace = FirebasePerformance.getInstance().newTrace("syncModeCompleteAndRepullTrace");
				syncModeTrace.start();
				pullInfrastructure(); // do before missing, because we may have a completely empty database

				if (ConfigProvider.getLastDeletedSyncDate() == null
						|| DateHelper.getFullDaysBetween(ConfigProvider.getLastDeletedSyncDate(), new Date()) >= 1) {

					pullAndRemoveDeletedUuidsSince(ConfigProvider.getLastDeletedSyncDate());
				}
				// pull and remove archived entities when the last time this has been done is more than 24 hours ago
				if (ConfigProvider.getLastArchivedSyncDate() == null
						|| DateHelper.getFullDaysBetween(ConfigProvider.getLastArchivedSyncDate(), new Date()) >= 1) {

					pullAndRemoveArchivedUuidsSince(ConfigProvider.getLastArchivedSyncDate());
				}
				synchronizeChangedData();

				pullMissingAndDeleteInvalidInfrastructure();

//				pullMissingAndDeleteInvalidInfrastructure
				//repullData();
				pushNewPullMissingAndDeleteInvalidData();

				ConfigProvider.setRepullNeeded(false);

				syncModeTrace.stop();
				break;

			case Complete:
					System.out.println("_+++++++++----------------------- complete");
					syncModeTrace = FirebasePerformance.getInstance().newTrace("syncModeCompleteTrace");
					syncModeTrace.start();

					pullInfrastructure(); // do before missing, because we may have a completely empty database
					pullMissingAndDeleteInvalidInfrastructure();

					pushNewPullMissingAndDeleteInvalidData();
					synchronizeChangedData();

					syncModeTrace.stop();
					break;

				case Reinitialize:
					System.out.println("_+++++++++----------------------- Reinitialize");
					syncModeTrace = FirebasePerformance.getInstance().newTrace("syncModeReinitializeTraceAsync");
					syncModeTrace.start();

					if (ConfigProvider.getLastDeletedSyncDate() == null
							|| DateHelper.getFullDaysBetween(ConfigProvider.getLastDeletedSyncDate(), new Date()) >= 1) {
						pullAndRemoveDeletedUuidsSince(ConfigProvider.getLastDeletedSyncDate());
					}
					// pull and remove archived entities when the last time this has been done is more than 24 hours ago
					if (ConfigProvider.getLastArchivedSyncDate() == null
							|| DateHelper.getFullDaysBetween(ConfigProvider.getLastArchivedSyncDate(), new Date()) >= 1) {
						pullAndRemoveArchivedUuidsSince(ConfigProvider.getLastArchivedSyncDate());
					}

					syncDeviceInfoAndErrorLogsOnly();

					syncModeTrace.stop();
					break;
			default:
				throw new IllegalArgumentException(syncMode.toString());
			}

			if (syncMode == SyncMode.Changes && hasAnyUnsynchronizedData()) {
				Log.w(getClass().getName(), "Still having unsynchronized data. Trying again in complete mode.");

				syncMode = SyncMode.Complete;
				doInBackground(params);
			}

		} catch (ServerConnectionException e) {
			syncFailed = true;
			syncFailedMessage = e.getMessage(context);
			RetroProvider.disconnect();

		} catch (NoConnectionException | ServerCommunicationException e) {

			Log.e(getClass().getName(), "Error trying to synchronizing data in mode '" + syncMode + "'", e);

			// Single error log per synchronization failure
			ErrorReportingHelper.sendCaughtException(e);

			System.out.println("DATA ASYNCY 1  Fragment Error Logged--------------------");

			ErrorReportingHelper.logAndStoreDeviceError("Synchronization Failed - No Connection: " + syncMode.toString(), e);

			syncFailed = true;
			syncFailedMessage = DatabaseHelper.getContext().getString(R.string.error_server_communication);
			RetroProvider.disconnect();

		} catch (RuntimeException | DaoException e) {

			SyncMode newSyncMode = null;
			switch (syncMode) {
			case Changes:
				newSyncMode = SyncMode.Complete;
				break;
			case Complete:
				newSyncMode = SyncMode.CompleteAndRepull;
				break;
			case CompleteAndRepull:
				break;
			case Reinitialize:
				newSyncMode = SyncMode.Changes;
				break;
			default:
				throw new IllegalArgumentException(syncMode.toString());
			}

			if (newSyncMode != null) {

				Log.w(getClass().getName(), "Error trying to synchronizing data in mode '" + syncMode + "'", e);
				ErrorReportingHelper.sendCaughtException(e);
				syncMode = newSyncMode;
				doInBackground(params);

			} else {
				Log.e(getClass().getName(), "Error trying to synchronizing data in mode '" + syncMode + "'", e);
				            ErrorReportingHelper.sendCaughtException(e);


				System.out.println("SYNC 2 Fragment Error Logged--------------------");

				// Single error log per synchronization failure
				ErrorReportingHelper.logAndStoreDeviceError("Synchronization Failed - Runtime/Dao Exception: " + syncMode.toString(), e);

				syncFailed = true;
				syncFailedMessage = DatabaseHelper.getContext().getString(R.string.error_synchronization);
				RetroProvider.disconnect();
			}

		}

		return null;
	}

	public static boolean hasAnyUnsynchronizedData() {
		final boolean hasUnsynchronizedCampaignData = !DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)
			&& (DatabaseHelper.getCampaignFormDataDao().isAnyModified());
		return hasUnsynchronizedCampaignData || DatabaseHelper.getDeviceErrorLogDao().isAnyModified();
	}

	@AddTrace(name = "synchronizeChangedDataTrace")
	private void synchronizeChangedData() throws DaoException, NoConnectionException, ServerConnectionException, ServerCommunicationException {

		// Campaigns
		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)) {


			final CampaignDtoHelper campaignDtoHelper = new CampaignDtoHelper();
			System.out.println(campaignDtoHelper + "++++++------------------__campaignFormDataDtoHelpercampaignFormDataDtoHelpercampaignFormDataDtoHelper");
			System.out.println(campaignDtoHelper.pullAndPushEntities() + "campaignFormDataDtoHelpercampaignFormDataDtoHelpercampaignFormDataDtoHelper");
			if (campaignDtoHelper.pullAndPushEntities())
				campaignDtoHelper.pullEntities(true);

			final CampaignFormMetaDtoHelper campaignFormMetaDtoHelper = new CampaignFormMetaDtoHelper();
			if (campaignFormMetaDtoHelper.pullAndPushEntities())
				campaignFormMetaDtoHelper.pullEntities(true);

			final CampaignFormDataDtoHelper campaignFormDataDtoHelper = new CampaignFormDataDtoHelper();
			if (campaignFormDataDtoHelper.pullAndPushEntities())
				campaignFormDataDtoHelper.pullEntities(true);

			final CampaignFormMetaWithExpDtoHelper campaignFormMetaWithExpDtoHelper = new CampaignFormMetaWithExpDtoHelper();
			if (campaignFormMetaWithExpDtoHelper.pullAndPushEntities())
				campaignFormMetaWithExpDtoHelper.pullEntities(true);

			final CampaignFormMetaRegionDtoHelper campaignFormMetaRegionDtoHelper = new CampaignFormMetaRegionDtoHelper();
			if (campaignFormMetaRegionDtoHelper.pullAndPushEntities())
				campaignFormMetaRegionDtoHelper.pullEntities(true);

			final PopulationDataDtoHelper populationDataDtoHelper = new PopulationDataDtoHelper();
			if (populationDataDtoHelper.pullAndPushEntities())
				populationDataDtoHelper.pullEntities(true);

			final FCMTokenDtoHelper fcmTokenDtoHelper = new FCMTokenDtoHelper();
			if (fcmTokenDtoHelper.pullAndPushEntities())
				fcmTokenDtoHelper.pullEntities(true);

			final DeviceInfoDtoHelper deviceInfoDtoHelper = new DeviceInfoDtoHelper();
			if (deviceInfoDtoHelper.pullAndPushEntities())
				deviceInfoDtoHelper.pullEntities(true);

			final DeviceErrorLogDtoHelper deviceErrorLogDtoHelper = new DeviceErrorLogDtoHelper();
			if (deviceErrorLogDtoHelper.pullAndPushEntities())
				deviceErrorLogDtoHelper.pullEntities(true);



			repullData();
		}
	}



	@AddTrace(name = "repullDataTrace")
	private void repullData() throws DaoException, NoConnectionException, ServerConnectionException, ServerCommunicationException {

//		// order is important, due to dependencies (e.g. case & person)
		new UserRoleConfigDtoHelper().repullEntities();
		new UserDtoHelper().repullEntities();

		// Campaigns
		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)) {
			final CampaignDtoHelper campaignDtoHelper = new CampaignDtoHelper();
			final CampaignFormDataDtoHelper campaignFormDataDtoHelper = new CampaignFormDataDtoHelper();
			final CampaignFormMetaDtoHelper campaignFormMetaDtoHelper = new CampaignFormMetaDtoHelper();
			final CampaignFormMetaWithExpDtoHelper campaignFormMetaWithExpDtoHelper = new CampaignFormMetaWithExpDtoHelper();
			final DeviceInfoDtoHelper deviceInfoDtoHelper = new DeviceInfoDtoHelper();
			final DeviceErrorLogDtoHelper deviceErrorLogDtoHelper = new DeviceErrorLogDtoHelper();


			campaignDtoHelper.repullEntities();
			campaignFormMetaDtoHelper.repullEntities();
			campaignFormDataDtoHelper.repullEntities();
			campaignFormMetaWithExpDtoHelper.repullEntities();
			deviceInfoDtoHelper.repullEntities();
			deviceErrorLogDtoHelper.repullEntities();

		}
	}

	@AddTrace(name = "pullInfrastructureTrace")
	private void pullInfrastructure() throws DaoException, NoConnectionException, ServerConnectionException, ServerCommunicationException {
		if (ConfigProvider.isInitialSyncRequired() || resyncDataRepull) {
			System.out.println("+++++++++++++111111 resync ongoing: "+ resyncDataRepull);
			pullInitialInfrastructure();
		} else {
			System.out.println("+++++++++++++222222");
			InfrastructureChangeDatesDto changeDates = InfrastructureHelper.getInfrastructureChangeDates();

			try {
				Response<InfrastructureSyncDto> response = RetroProvider.getInfrastructureFacade().pullInfrastructureSyncData(changeDates).execute();
				if (!response.isSuccessful()) {
					RetroProvider.throwException(response);
				}

				InfrastructureSyncDto infrastructureData = response.body();
				if (infrastructureData != null) {
					if (infrastructureData.isInitialSyncRequired()) {
						ConfigProvider.setInitialSyncRequired(true);
						pullInfrastructure();
					} else {
						System.out.println("+++++++++++++222222 Handling pulled infra");

						InfrastructureHelper.handlePulledInfrastructureData(infrastructureData);
					}
				}
			} catch (IOException e) {
				Log.e(SynchronizeDataAsync.class.getSimpleName(), "Error when trying to pull infrastructure data: " + e.getMessage());
			}
		}
	}

	@AddTrace(name = "pullInitialInfrastructureTrace")
	private void pullInitialInfrastructure() throws DaoException, ServerCommunicationException, ServerConnectionException, NoConnectionException {
	//	new ContinentDtoHelper().pullEntities(false);
	//	new SubcontinentDtoHelper().pullEntities(false);
	//	new CountryDtoHelper().pullEntities(false);

		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.INFRASTRUCTURE_TYPE_AREA)) {
			new AreaDtoHelper().pullEntities(false);
		}
		new RegionDtoHelper().pullEntities(false);
		new DistrictDtoHelper().pullEntities(false);

		System.out.println("+++++++++++++++++++++++++XXXXXXXXXXXXXXSSSSSSSSSSSSS+++++++++++++");
		new CommunityDtoHelper().pullEntities(false);
		//new FacilityDtoHelper().pullEntities(false);
		//new PointOfEntryDtoHelper().pullEntities(false);
		new UserDtoHelper().pullEntities(false);

		//new DiseaseClassificationDtoHelper().pullEntities(false);
		//new DiseaseConfigurationDtoHelper().pullEntities(false);
	//	new CustomizableEnumValueDtoHelper().pullEntities(false);

		// user role configurations may be removed, so have to pull the deleted uuids
		// this may be applied to other entities later as well
		Date latestChangeDate = DatabaseHelper.getUserRoleConfigDao().getLatestChangeDate();
		List<String> userRoleConfigUuids =
			executeUuidCall(RetroProvider.getUserRoleConfigFacade().pullDeletedUuidsSince(latestChangeDate != null ? latestChangeDate.getTime() : 0));
		DatabaseHelper.getUserRoleConfigDao().delete(userRoleConfigUuids);

		new UserRoleConfigDtoHelper().pullEntities(false);

		Date featureConfigurationChangeDate = DatabaseHelper.getFeatureConfigurationDao().getLatestChangeDate();
		List<String> featureConfigurationConfigUuids = executeUuidCall(
			RetroProvider.getFeatureConfigurationFacade()
				.pullDeletedUuidsSince(featureConfigurationChangeDate != null ? featureConfigurationChangeDate.getTime() : 0));
		DatabaseHelper.getFeatureConfigurationDao().delete(featureConfigurationConfigUuids);

		new FeatureConfigurationDtoHelper().pullEntities(false);

		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)) {
			new CampaignFormMetaDtoHelper().pullEntities(false);
			new CampaignFormMetaWithExpDtoHelper().pullEntities(false);
			new CampaignDtoHelper().pullEntities(false);
			new CampaignFormMetaRegionDtoHelper().pullEntities(false);
			new UserDtoHelper().pullEntities(false);

		}

		new PopulationDataDtoHelper().pullEntities(false);


		ConfigProvider.setInitialSyncRequired(false);
	}

	@AddTrace(name = "pullAndRemoveArchivedUuidsSinceTrace")
	private void pullAndRemoveArchivedUuidsSince(Date since) throws NoConnectionException, ServerConnectionException, ServerCommunicationException {
		Log.d(SynchronizeDataAsync.class.getSimpleName(), "pullArchivedUuidsSince");
	}

	@AddTrace(name = "pullAndRemoveDeletedUuidsSinceTrace")
	private void pullAndRemoveDeletedUuidsSince(Date since) throws NoConnectionException, ServerConnectionException, ServerCommunicationException {
		Log.d(SynchronizeDataAsync.class.getSimpleName(), "pullDeletedUuidsSince");
if (1 == 3) {
	// Cases
	List<String> caseUuids = executeUuidCall(RetroProvider.getCaseFacade().pullDeletedUuidsSince(since != null ? since.getTime() : 0));
}
	ConfigProvider.setLastDeletedSyncDate(new Date());

	}

	@AddTrace(name = "pushNewPullMissingAndDeleteInvalidDataTrace")
	private void pushNewPullMissingAndDeleteInvalidData()
		throws NoConnectionException, ServerConnectionException, ServerCommunicationException, DaoException {
		// ATTENTION: Since we are working with UUID lists we have no type safety. Look for typos!

		Log.d(SynchronizeDataAsync.class.getSimpleName(), "pushNewPullMissingAndDeleteInvalidData");

		// order is important, due to dependencies (e.g. case & person)

		// first push everything that has been CREATED by the user - otherwise this data my lose it's references to other entities.
		// Example: Case is created using an existing person, meanwhile user loses access to the person
//		new PersonDtoHelper().pushEntities(true);
//		new CaseDtoHelper().pushEntities(true);
//		new ImmunizationDtoHelper().pushEntities(true);
//		new EventDtoHelper().pushEntities(true);
//		new EventParticipantDtoHelper().pushEntities(true);
//		new SampleDtoHelper().pushEntities(true);
//		new PathogenTestDtoHelper().pushEntities(true);
//		new AdditionalTestDtoHelper().pushEntities(true);
//		new ContactDtoHelper().pushEntities(true);
//		new VisitDtoHelper().pushEntities(true);
//		new TaskDtoHelper().pushEntities(true);
//		new WeeklyReportDtoHelper().pushEntities(true);
//		new AggregateReportDtoHelper().pushEntities(true);
//		new PrescriptionDtoHelper().pushEntities(true);
//		new TreatmentDtoHelper().pushEntities(true);
//		new ClinicalVisitDtoHelper().pushEntities(true);
//
//		// weekly reports and entries
//		List<String> weeklyReportUuids = executeUuidCall(RetroProvider.getWeeklyReportFacade().pullUuids());
//		DatabaseHelper.getWeeklyReportDao().deleteInvalid(weeklyReportUuids);
//		// aggregate reports
//		List<String> aggregateReportUuids = executeUuidCall(RetroProvider.getAggregateReportFacade().pullUuids());
//		DatabaseHelper.getAggregateReportDao().deleteInvalid(aggregateReportUuids);
//		// tasks
//		List<String> taskUuids = executeUuidCall(RetroProvider.getTaskFacade().pullUuids());
//		DatabaseHelper.getTaskDao().deleteInvalid(taskUuids);
//		// visits
//		List<String> visitUuids = executeUuidCall(RetroProvider.getVisitFacade().pullUuids());
//		DatabaseHelper.getVisitDao().deleteInvalid(visitUuids);
//		// contacts
//		List<String> contactUuids = executeUuidCall(RetroProvider.getContactFacade().pullUuids());
//		DatabaseHelper.getContactDao().deleteInvalid(contactUuids);
//		// sample tests
//		List<String> sampleTestUuids = executeUuidCall(RetroProvider.getSampleTestFacade().pullUuids());
//		DatabaseHelper.getSampleTestDao().deleteInvalid(sampleTestUuids);
//		// additional tests
//		List<String> additionalTestUuids = executeUuidCall(RetroProvider.getAdditionalTestFacade().pullUuids());
//		DatabaseHelper.getAdditionalTestDao().deleteInvalid(additionalTestUuids);
//		// samples
//		List<String> sampleUuids = executeUuidCall(RetroProvider.getSampleFacade().pullUuids());
//		DatabaseHelper.getSampleDao().deleteInvalid(sampleUuids);
//		// event participants
//		List<String> eventParticipantUuids = executeUuidCall(RetroProvider.getEventParticipantFacade().pullUuids());
//		DatabaseHelper.getEventParticipantDao().deleteInvalid(eventParticipantUuids);
//		// events
//		List<String> eventUuids = executeUuidCall(RetroProvider.getEventFacade().pullUuids());
//		DatabaseHelper.getEventDao().deleteInvalid(eventUuids);
//		// treatments
//		List<String> treatmentUuids = executeUuidCall(RetroProvider.getTreatmentFacade().pullUuids());
//		DatabaseHelper.getTreatmentDao().deleteInvalid(treatmentUuids);
//		// prescriptions
//		List<String> prescriptionUuids = executeUuidCall(RetroProvider.getPrescriptionFacade().pullUuids());
//		DatabaseHelper.getPrescriptionDao().deleteInvalid(prescriptionUuids);
//		// clinical visits
//		List<String> clinicalVisitUuids = executeUuidCall(RetroProvider.getClinicalVisitFacade().pullUuids());
//		DatabaseHelper.getClinicalVisitDao().deleteInvalid(clinicalVisitUuids);
//		// immunizations
//		List<String> immunizationUuids = executeUuidCall(RetroProvider.getImmunizationFacade().pullUuids());
//		DatabaseHelper.getImmunizationDao().deleteInvalid(immunizationUuids);
//		// cases
//		List<String> caseUuids = executeUuidCall(RetroProvider.getCaseFacade().pullUuids());
//		DatabaseHelper.getCaseDao().deleteInvalid(caseUuids);
//		// persons
//		List<String> personUuids = executeUuidCall(RetroProvider.getPersonFacade().pullUuids());
//		DatabaseHelper.getPersonDao().deleteInvalid(personUuids);
//		// outbreak
//		List<String> outbreakUuids = executeUuidCall(RetroProvider.getOutbreakFacade().pullActiveUuids());
//		DatabaseHelper.getOutbreakDao().deleteInvalid(outbreakUuids);
//
//		// order is important, due to dependencies (e.g. case & person)
//
//		new PersonDtoHelper().pullMissing(personUuids);
//		new CaseDtoHelper().pullMissing(caseUuids);
//		new ImmunizationDtoHelper().pullMissing(caseUuids);
//		new PrescriptionDtoHelper().pullMissing(prescriptionUuids);
//		new TreatmentDtoHelper().pullMissing(treatmentUuids);
//		new EventDtoHelper().pullMissing(eventUuids);
//		new EventParticipantDtoHelper().pullMissing(eventParticipantUuids);
//		new SampleDtoHelper().pullMissing(sampleUuids);
//		new AdditionalTestDtoHelper().pullMissing(additionalTestUuids);
//		new PathogenTestDtoHelper().pullMissing(sampleTestUuids);
//		new ContactDtoHelper().pullMissing(contactUuids);
//		new VisitDtoHelper().pullMissing(visitUuids);
//		new TaskDtoHelper().pullMissing(taskUuids);
//		new WeeklyReportDtoHelper().pullMissing(weeklyReportUuids);
//		new AggregateReportDtoHelper().pullMissing(aggregateReportUuids);
//		new PrescriptionDtoHelper().pullMissing(prescriptionUuids);
//		new TreatmentDtoHelper().pullMissing(treatmentUuids);
//		new ClinicalVisitDtoHelper().pullMissing(clinicalVisitUuids);

		// CampaignData
		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)) {

			// Push documents before campaign form data: form values reference imageId (document uuid).
			final DocumentDtoHelper documentDtoHelper = new DocumentDtoHelper();
			documentDtoHelper.pushEntities(true);

			final CampaignFormDataDtoHelper campaignFormDataDtoHelper = new CampaignFormDataDtoHelper();
			campaignFormDataDtoHelper.pushEntities(true);
			final List<String> campaignFormDataUuids = executeUuidCall(RetroProvider.getCampaignFormDataFacade().pullUuids());
			DatabaseHelper.getCampaignFormDataDao().deleteInvalid(campaignFormDataUuids);
			campaignFormDataDtoHelper.pullMissing(campaignFormDataUuids);

			final List<String> campaignUuids = executeUuidCall(RetroProvider.getCampaignFacade().pullUuids());
			DatabaseHelper.getCampaignFormDataDao().deleteInvalid(campaignUuids);
			campaignFormDataDtoHelper.pullMissing(campaignFormDataUuids);
		//TODO: Add Expiry Date Login

			System.out.println("USer Pushhh  notified ===========================");

			final UserDtoHelper userDtoHelper = new UserDtoHelper();
			userDtoHelper.pushEntities(true);


			final DeviceInfoDtoHelper deviceInfoDtoHelper = new DeviceInfoDtoHelper();
			deviceInfoDtoHelper.pushEntities(true);


			final DeviceErrorLogDtoHelper deviceErrorLogDtoHelper = new DeviceErrorLogDtoHelper();
			deviceErrorLogDtoHelper.pushEntities(true);

			System.out.println("USer Pushhh  concluded ===========================");


		}


	}

	/**
	 * Sync only device information and error logs
	 */
	@AddTrace(name = "syncDeviceInfoAndErrorLogsOnlyTrace")
	public void syncDeviceInfoAndErrorLogsOnly() throws DaoException, NoConnectionException, ServerConnectionException, ServerCommunicationException {
		syncDeviceInfoAndErrorLogsOnlyStatic();
	}

	public  void syncDeviceInfoAndErrorLogsOnlyStatic() throws DaoException, NoConnectionException, ServerConnectionException, ServerCommunicationException {
		Log.d(SynchronizeDataAsync.class.getSimpleName(), "syncDeviceInfoAndErrorLogsOnlyTrace");


			final DocumentDtoHelper documentDtoHelper = new DocumentDtoHelper();
			documentDtoHelper.pushEntities(true);

			final CampaignFormDataDtoHelper campaignFormDataDtoHelper = new CampaignFormDataDtoHelper();
			campaignFormDataDtoHelper.pushEntities(true);

			final DeviceInfoDtoHelper deviceInfoDtoHelper = new DeviceInfoDtoHelper();
			deviceInfoDtoHelper.pushEntities(true);

			final DeviceErrorLogDtoHelper deviceErrorLogDtoHelper = new DeviceErrorLogDtoHelper();
			deviceErrorLogDtoHelper.pushEntities(true);
	}

	@AddTrace(name = "pullMissingAndDeleteInvalidInfrastructureTrace")
	private void pullMissingAndDeleteInvalidInfrastructure()
		throws NoConnectionException, ServerConnectionException, ServerCommunicationException, DaoException {
		// ATTENTION: Since we are working with UUID lists we have no type safety. Look for typos!

		Log.d(SynchronizeDataAsync.class.getSimpleName(), "pullMissingAndDeleteInvalidInfrastructure");

		// TODO get a count first and only retrieve all uuids when count is different?

		// users
		List<String> userUuids = executeUuidCall(RetroProvider.getUserFacade().pullUuids());
		DatabaseHelper.getUserDao().deleteInvalid(userUuids);

		List<String> featureConfigurationUuids = executeUuidCall(RetroProvider.getFeatureConfigurationFacade().pullUuids());
		DatabaseHelper.getFeatureConfigurationDao().deleteInvalid(featureConfigurationUuids);
		// user role config
		List<String> userRoleConfigUuids = executeUuidCall(RetroProvider.getUserRoleConfigFacade().pullUuids());
		DatabaseHelper.getUserRoleConfigDao().deleteInvalid(userRoleConfigUuids);

		List<String> communityUuids = executeUuidCall(RetroProvider.getCommunityFacade().pullUuids());
		DatabaseHelper.getCommunityDao().deleteInvalid(communityUuids);
		// districts
		List<String> districtUuids = executeUuidCall(RetroProvider.getDistrictFacade().pullUuids());
		DatabaseHelper.getDistrictDao().deleteInvalid(districtUuids);
		// regions
		List<String> regionUuids = executeUuidCall(RetroProvider.getRegionFacade().pullUuids());
		DatabaseHelper.getRegionDao().deleteInvalid(regionUuids);
		// areas
		List<String> areaUuids = executeUuidCall(RetroProvider.getAreaFacade().pullUuids());
		DatabaseHelper.getAreaDao().deleteInvalid(areaUuids);

		//population data
		List<String> populationDataUuids =  executeUuidCall(RetroProvider.getPopulationDataFacade().pullUuids());
		DatabaseHelper.getPopulationDataDao().deleteInvalid(populationDataUuids);


		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.INFRASTRUCTURE_TYPE_AREA)) {
			new AreaDtoHelper().pullMissing(areaUuids);
		}

		new RegionDtoHelper().pullMissing(regionUuids);
		new DistrictDtoHelper().pullMissing(districtUuids);
		new CommunityDtoHelper().pullMissing(communityUuids);


		new UserRoleConfigDtoHelper().pullMissing(userRoleConfigUuids);
		new UserDtoHelper().pullMissing(userUuids);

		new FeatureConfigurationDtoHelper().pullMissing(featureConfigurationUuids);

		if (!DatabaseHelper.getFeatureConfigurationDao().isFeatureDisabled(FeatureType.CAMPAIGNS)) {
			// campaigns
			List<String> campaignUuids = executeUuidCall(RetroProvider.getCampaignFacade().pullUuids());
			//for(String dcs : campaignUuids){
				System.out.println(campaignUuids + "++++++--------------------============================--0-0-0-0-0-0-0  CHecking Missingand DeleteInvalid");
			//}
			DatabaseHelper.getCampaignDao().deleteInvalid(campaignUuids);
			new CampaignDtoHelper().pullMissing(campaignUuids);

			// campaignFormMetas
			List<String> campaignFormMetaUuids = executeUuidCall(RetroProvider.getCampaignFormMetaFacade().pullUuids());
			DatabaseHelper.getCampaignFormMetaDao().deleteInvalid(campaignFormMetaUuids);

			new CampaignFormMetaDtoHelper().pullMissing(campaignFormMetaUuids);
			new PopulationDataDtoHelper().pullMissing(populationDataUuids);
			new CampaignFormMetaRegionDtoHelper().pullMissing(populationDataUuids);




		}
	}

	private List<String> executeUuidCall(Call<List<String>> call) throws ServerConnectionException, ServerCommunicationException {
		Response<List<String>> response;
		try {
			response = call.execute();
		} catch (IOException e) {
			throw new ServerCommunicationException(e);
		}
		if (!response.isSuccessful()) {
			RetroProvider.throwException(response);
		}
		return response.body();
	}

	public static void call(SyncMode syncMode, final Context context, final SyncCallback callback) {
		new SynchronizeDataAsync(syncMode, context) {

			@Override
			protected void onPostExecute(Void aVoid) {
				if (callback != null) {
					callback.call(syncFailed, syncFailedMessage);
				}
				if (context != null) {
					TaskNotificationService.doTaskNotification(context);
				}
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public enum SyncMode {
		Changes,

		Complete,

		/**
		 * used to sync app errors
		 * resulting from bugs or incompatibilities*/
		Reinitialize,
		/**
		 * Also repulls all non-infrastructure data and users
		 * Use to handle conflict states resulting out of bugs
		 */
		CompleteAndRepull,
	}
}
