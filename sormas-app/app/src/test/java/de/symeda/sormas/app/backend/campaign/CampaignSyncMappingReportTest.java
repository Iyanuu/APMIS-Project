package de.symeda.sormas.app.backend.campaign;

import org.junit.Test;

import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataDtoHelper;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaDtoHelper;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaRegionDtoHelper;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaWithExpDtoHelper;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.SyncMapping;
import de.symeda.sormas.app.backend.common.SyncMappingHarness;

/** Temporary. Prints the harness result for the five campaign helpers. Remove before the merge. */
public class CampaignSyncMappingReportTest {

	@Test
	public void report() {
		print(CampaignDtoHelper.class);
		print(CampaignFormDataDtoHelper.class);
		print(CampaignFormMetaDtoHelper.class);
		print(CampaignFormMetaRegionDtoHelper.class);
		print(CampaignFormMetaWithExpDtoHelper.class);
	}

	private static void print(Class<? extends AdoDtoHelper<?, ?>> helper) {
		SyncMapping m = SyncMappingHarness.analyse(helper);
		System.out.println("REPORT " + helper.getSimpleName() + " entity=" + m.getEntityClass().getSimpleName() + " dto=" + m.getDtoClass().getSimpleName() + " push=" + m.isPushSupported());
		System.out.println("REPORT   entityFields=" + m.getEntityFields());
		System.out.println("REPORT   dtoFields=" + m.getDtoFields());
		System.out.println("REPORT   shared=" + m.getSharedFields());
		System.out.println("REPORT   notCopiedToEntity=" + m.getNotCopiedToEntity());
		System.out.println("REPORT   notCopiedToDto=" + m.getNotCopiedToDto());
	}
}
