/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.ui.campaign.campaigndata;

import static com.vaadin.ui.Notification.Type.TRAY_NOTIFICATION;

import com.vaadin.server.Page;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DetailSubComponentWrapper;

@com.vaadin.annotations.JavaScript("jquerymini.js")
public class CampaignFormDataView extends AbstractCampaignDataView {

	private static final long serialVersionUID = -1890947102041773346L;

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/dataform";

	private CommitDiscardWrapperComponent<CampaignFormDataEditForm> editComponent; 

	public CampaignFormDataView() {
		super(VIEW_NAME);
	}

	@Override
	protected void initView(String params) {
		DetailSubComponentWrapper container = new DetailSubComponentWrapper(() -> editComponent);
		container.setWidth(100, Unit.PERCENTAGE);
		container.setMargin(true);
		setSubComponent(container);

		if (params.contains(",")) {
			String[] paraObj = params.split(",");
			CampaignReferenceDto camref = FacadeProvider.getCampaignFacade().getReferenceByUuid((String) paraObj[0]);
			CampaignFormMetaReferenceDto amformmeta = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetaReferenceByUuid((String) paraObj[1]);
			editComponent = ControllerProvider.getCampaignController().getCampaignFormDataComponent(null, camref,
					amformmeta,
					false, false, () -> {
						SormasUI.refreshCampaignView();
						Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
								amformmeta.getCaption()), TRAY_NOTIFICATION);
					}, () -> {}, () -> {
						SormasUI.refreshCampaignView();
						Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
								amformmeta.getCaption()), TRAY_NOTIFICATION);
					}, true);
			editComponent.setMargin(false);
			editComponent.getWrappedComponent().setWidth(100, Unit.PERCENTAGE);
			editComponent.setHeightUndefined();
			editComponent.addStyleName(CssStyles.ROOT_COMPONENT);
			editComponent.setWidth(100, Unit.PERCENTAGE);

			container.addComponent(editComponent);

			getViewTitleLabel().setValue(amformmeta.getCaption());// campaignFormData.getCampaignFormMeta().toString());querySelector
			Page.getCurrent().getJavaScript().execute(
					"$(document).ready(function() {"
				//	+ "alert();"
					//+ "document.querySelector(\".v-slot.v-align-right.v-align-bottom\").hide();"
					//+ "$('.v-slot.v-align-right.v-align-bottom').toggleClass('v-align-center').removeClass('v-align-right');"
					+ "$('.v-verticallayout.v-layout.v-vertical.v-widget.v-has-width.v-has-height.v-margin-top.v-margin-right.v-margin-bottom.v-margin-left').hide();"
					
				//+"$('#formidx').find('td:contains('Void')').parent('tr').hide();"
					+"});"
					);
			//"$('#formidx').find('td:contains('Void')').parent('tr').hide()");
			
			

		} else {

			CampaignFormDataDto campaignFormData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(getReference().getUuid());
			editComponent = ControllerProvider.getCampaignController().getCampaignFormDataComponent(campaignFormData,
					campaignFormData.getCampaign(), campaignFormData.getCampaignFormMeta(), true, true, () -> {
						SormasUI.refreshView();
						Notification.show(String.format(I18nProperties.getString(Strings.messageCampaignFormSaved),
								campaignFormData.getCampaignFormMeta().toString()), TRAY_NOTIFICATION);
					}, null, null, false);
			editComponent.setMargin(false);
			editComponent.getWrappedComponent().setWidth(100, Unit.PERCENTAGE);
			editComponent.setHeightUndefined();
			editComponent.addStyleName(CssStyles.ROOT_COMPONENT);
			editComponent.setWidth(100, Unit.PERCENTAGE);

			container.addComponent(editComponent);

			getViewTitleLabel().setValue(campaignFormData.getCampaignFormMeta().toString());

		}
	}
}
