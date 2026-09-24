/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.campaign.edit;

import static android.view.View.GONE;

import android.content.Context;

import android.os.DeadSystemException;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.infrastructure.InfrastructureHelper;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.api.utils.fieldaccess.UiFieldAccessCheckers;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaRegion;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.PopulationData;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.dialog.FormDialog;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.databinding.DialogSelectCampaignFormMetaLayoutBinding;
import de.symeda.sormas.app.util.DataUtils;
import de.symeda.sormas.app.util.ErrorReportingHelper;
import de.symeda.sormas.app.util.InfrastructureDaoHelper;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;

public class CampaignFormMetaDialog extends FormDialog {

    private DialogSelectCampaignFormMetaLayoutBinding contentBinding;
    private Campaign campaign;

    public CampaignFormMetaDialog(final FragmentActivity activity, Campaign campaign) {
        super(
                activity,
                R.layout.dialog_root_layout,
                R.layout.dialog_select_campaign_form_meta_layout,
                R.layout.dialog_root_two_button_panel_layout,
                R.string.heading_campaign_form_meta_select,
                -1,
                UiFieldAccessCheckers.forSensitiveData(campaign.isPseudonymized()));

        this.campaign = campaign;
    }

    @Override
    protected void setContentBinding(Context context, ViewDataBinding binding, String layoutName) {
        this.contentBinding = (DialogSelectCampaignFormMetaLayoutBinding) binding;
    }

    @Override
    protected void initializeContentView(ViewDataBinding rootBinding, ViewDataBinding buttonPanelBinding) {

        List<CampaignFormMeta> allFormsForCampaign =new ArrayList<CampaignFormMeta>();
        allFormsForCampaign =  campaign.getCampaignFormMetas();

        List<CampaignFormMeta> allUnexpiredFormsForCampaign = new ArrayList<>();

        // TODO
        // Think of a way to make this Enum so if it changes from sormas api you dont need to make changes here
        // and code wont break in production because of it
        List<String> preCampaignsCategories = List.of("FLW", "MODALITY_PRE", "TRAINING", "MONITORING");
        List<String> intraCampaignsCategories = List.of("ICM", "ADMIN", "EAG-ICM", "EAG-ADMIN");
        List<String> postCampaignsCategories = List.of("PCA", "FMS", "LQAS", "EAG-PCA", "EAG-FMS", "EAG-LQAS", "MODALITY_POST", "VALIDATION");

        for (CampaignFormMeta campaignFormMeta : allFormsForCampaign) {
            LocalDate currentDate = LocalDate.now();
            Date expiryDate = DatabaseHelper.getCampaignFormMetaWithExpDao().getCampaignFormExpiryDateByCampaignIdAndFormId(campaign.getUuid(), campaignFormMeta.getUuid());

            User user = ConfigProvider.getUser();
            List<CampaignFormMetaRegion> formsSelectedForCampaign =
                    DatabaseHelper.getCampaignFormMetaRegionDao().getSelectedFormsByRegion(campaignFormMeta.getUuid(), user.getRegion().getArea().getUuid());

            if (preCampaignsCategories.contains(campaignFormMeta.getFormCategory())) {

                System.out.println("cuurentdate " + currentDate + "expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())" + expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + "---"  + campaign.getPreCampStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " precampaign start date ");
                if ((!currentDate.isBefore(campaign.getPreCampStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        && !currentDate.isAfter(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))) {
                    if (formsSelectedForCampaign.size() > 0) {
                        allUnexpiredFormsForCampaign.add(campaignFormMeta);
                    }
                }
            } else if (intraCampaignsCategories.contains(campaignFormMeta.getFormCategory())) {

                System.out.println( "cuurentdate " + currentDate + "expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())" + expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + "---"  +campaign.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " intracampaign start date  start date ");

                if ((!currentDate.isBefore(campaign.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) &&
                        !currentDate.isAfter(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))) {
                    if (formsSelectedForCampaign.size() > 0) {
                        allUnexpiredFormsForCampaign.add(campaignFormMeta);
                    }
//                    }
                }
            } else if (postCampaignsCategories.contains(campaignFormMeta.getFormCategory())) {

                System.out.println( "cuurentdate " + currentDate + "expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())" + expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + "---"  + campaign.getPostCampStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " post - campaign start date ");

                if (((!currentDate.isBefore(campaign.getPostCampStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        && !currentDate.isAfter(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())))) {
                    if (formsSelectedForCampaign.size() > 0) {
                        allUnexpiredFormsForCampaign.add(campaignFormMeta);
                    }
                }
            }
        }
        Collections.sort(allUnexpiredFormsForCampaign, Comparator.comparing(CampaignFormMeta::getFormName));

        contentBinding.campaignFormMeta.initializeSpinner(DataUtils.toItems(allUnexpiredFormsForCampaign));
    }

    public CampaignFormMeta getCampaignFormMeta() {
        return (CampaignFormMeta) contentBinding.campaignFormMeta.getValue();
    }

    @Override
    protected void onPositiveClick() {
        try {
            System.out.println("Positvite ccallback clicked -------------------------");
            setLiveValidationDisabled(false);
            FragmentValidator.validate(getContext(), contentBinding);
        } catch (ValidationException  e) {
            NotificationHelper.showDialogNotification(CampaignFormMetaDialog.this, ERROR, e.getMessage());
            System.out.println("META DIALOG  Fragment Error Logged--------------------");
            ErrorReportingHelper.logAndStoreDeviceError( "New Form : " + e.getMessage(), e); // replaced sendCaughtException

            return;

        }catch (RuntimeException e) {
            NotificationHelper.showDialogNotification(CampaignFormMetaDialog.this, ERROR, e.getMessage());

            System.out.println("META DIALOG  Fragment Error Logged--------------------");

            ErrorReportingHelper.logAndStoreDeviceError( "New Form : " + e.getMessage(), e); // replaced sendCaughtException

            return;        }

        super.setCloseOnPositiveButtonClick(true);
        super.onPositiveClick();
    }
}