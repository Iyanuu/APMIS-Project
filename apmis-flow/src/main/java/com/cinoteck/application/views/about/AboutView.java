package com.cinoteck.application.views.about;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.utils.SessionTimeout;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.report.JsonDictionaryReportModelDto;

@PageTitle("APMIS-About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

	Grid<JsonDictionaryReportModelDto> grid = new Grid<>(JsonDictionaryReportModelDto.class, false);

	UserProvider userProvider = new UserProvider();
	
	List<JsonDictionaryReportModelDto> analysis = FacadeProvider.getCampaignFormDataFacade().getByJsonFormDefinitonToCSV();
	GridListDataView<JsonDictionaryReportModelDto> dataView ;
	
 

    public AboutView() {	
    	if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
    	FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
    	Div aboutView = new Div();
    	SessionTimeout sessionTimeout = new SessionTimeout();

		aboutView.getStyle().set("height", "100%");
		aboutView.getStyle().set("padding-left", "90px");
		aboutView.getStyle().set("padding-right", "90px");

		Div apmisImageContainer = new Div();
		apmisImageContainer.getStyle().set("height", "140px");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");
		apmisImageContainer.getStyle().set("margin-bottom", "30px");
		apmisImageContainer.getStyle().set("padding-top", "30px");

		Image img = new Image("images/apmislogo.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");

		apmisImageContainer.add(img);

		Div aboutText = new Div();

		Paragraph text = new Paragraph(I18nProperties.getDescription(Descriptions.about_description));
		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "20px");
		text.getStyle().set("margin-bottom", "30px");
		text.getStyle().set("text-align", "justify");
		aboutText.add(text);
		
		

		aboutView.add(apmisImageContainer, aboutText);
		add(aboutView);
       
      
		configureActionButtonVisibility();

		

	}

	public void configureActionButtonVisibility() {
		grid.addColumn(JsonDictionaryReportModelDto::getFormid).setHeader("Id");
		grid.addColumn(JsonDictionaryReportModelDto::getId).setHeader("Form Id ");
		grid.addColumn(JsonDictionaryReportModelDto::getCaption).setHeader("Caption");
		grid.addColumn(JsonDictionaryReportModelDto::getFormtype).setHeader("Form Type");
		grid.addColumn(JsonDictionaryReportModelDto::getModality).setHeader("Campaign Modality");
		grid.addColumn(JsonDictionaryReportModelDto::getDatatype).setHeader("Data Type");

		dataView = grid.setItems(analysis);
		add(grid);
		grid.setVisible(false);

		Button displayActionButtons = new Button("Show Action Buttons");
		displayActionButtons.setIcon(new Icon(VaadinIcon.SLIDERS));

		Button getUserGuide =  new Button(I18nProperties.getCaption(Captions.aboutuserguides));

		getUserGuide.setIcon(new Icon(VaadinIcon.NURSE));

		Button getTechnicalGuide = new Button(I18nProperties.getCaption(Captions.abouttechguides));
		getTechnicalGuide.setIcon(new Icon(VaadinIcon.DIPLOMA_SCROLL));

		Button getMobileGuide = new Button(I18nProperties.getCaption(Captions.mobileUserGuide));
		getMobileGuide.setIcon(new Icon(VaadinIcon.MOBILE));
		getMobileGuide.addClassName("wrap-button-label"); // Add a CSS class for styling

		GridExporter<JsonDictionaryReportModelDto> exporter = GridExporter.createFor(grid);

		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getString(Strings.apmisJsonGlossary));
		exporter.setFileName(I18nProperties.getString(Strings.apmisJsonGlossary)
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("", I18nProperties.getCaption(Captions.exportFormGlossary));
		excelLink.setHref(exporter.getCsvStreamResource());

		Button exportJsonGloassary = new Button(I18nProperties.getCaption(Captions.exportFormGlossary));
		exportJsonGloassary.setIcon(new Icon(VaadinIcon.FILE_TABLE));
		exportJsonGloassary.addClickListener(e -> {
			excelLink.getElement().callJsFunction("click");

		});

		getUserGuide.addClickListener(e -> {
			UI.getCurrent().getPage().open(
					"guides/APMIS_User_Guide.pdf");
		});

		getTechnicalGuide.addClickListener(e -> {
			UI.getCurrent().getPage().open(
					"guides/APMIS_Technical_Manual.pdf");
		});

		getMobileGuide.addClickListener(e -> {
//			UI.getCurrent().getPage().open(
//					
//					
//					"https://afghanistan-apmis.com/main/frontend/themes/sormas/img/Apmis_MobileUser_Guide.pdf");
			
			  String localFilePath = "guides/Apmis_MobileUser_Guide.pdf";

			    // Open the file using its local path
			    UI.getCurrent().getPage().open(localFilePath, "_blank");
		});
		;

		excelLink.setClassName("exportJsonGLoss2");

		excelLink.getStyle().set("color", "0D6938 !important");
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.getStyle().set("padding-left", "90px");
		buttonsLayout.getStyle().set("margin-right", "6rem");
		buttonsLayout.setWidth("100%");
//		buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		excelLink.getStyle().set("display", "none");
		
		buttonsLayout.add(getUserGuide, getTechnicalGuide, getMobileGuide,exportJsonGloassary, excelLink);
		

		
		
		VerticalLayout releaseDetailsLayout = new VerticalLayout();
		Paragraph versionNum = new Paragraph(I18nProperties.getCaption(Captions.apmisVersionNumber));
		versionNum.getStyle().set("font-size", "15px");
		versionNum.getStyle().set("font-weight", "500");
		versionNum.getStyle().set("color", "#0D6938");
		
		Paragraph releaseDate = new Paragraph(I18nProperties.getCaption(Captions.releaseDate));
		releaseDate.getStyle().set("font-size", "15px");
		releaseDate.getStyle().set("font-weight", "500");
		releaseDate.getStyle().set("color", "#0D6938");
		
		
		releaseDetailsLayout.getStyle().set("padding-left", "90px !important");
		releaseDetailsLayout.setWidth("100%");
//		releaseDetailsLayout.add(versionNum, releaseDate);
		add(buttonsLayout, releaseDetailsLayout, createMobileAppDownloadLayout());

	}
	
	private Div createMobileAppDownloadLayout() {

	    Div mobileAppCard = new Div();

	    mobileAppCard.getStyle()
//	            .set("width", "100%")
	            .set("box-sizing", "border-box")
	            .set("background-color", "#f3f4f6")
	            .set("border-radius", "0")
	            .set("padding", "24px 30px")
	            .set("margin-top", "20px")
	    .set("margin-left", "3.5%");

	    HorizontalLayout cardContent = new HorizontalLayout();

	    cardContent.setWidthFull();
	    cardContent.setSpacing(true);
	    cardContent.setPadding(false);
//	    cardContent.setAlignItems(Alignment.CENTER);

	    /*
	     * Mobile Icon Container
	     */
	    Div iconContainer = new Div();

	    iconContainer.getStyle()
	            .set("width", "52px")
	            .set("height", "52px")
	            .set("min-width", "52px")
	            .set("background-color", "#0D6938")
	            .set("border-radius", "10px")
	            .set("display", "flex")
	            .set("align-items", "center")
	            .set("justify-content", "center")
	            .set("margin-right", "16px");

	    Icon mobileIcon = VaadinIcon.MOBILE.create();

	    mobileIcon.getStyle()
	            .set("width", "25px")
	            .set("height", "25px")
	            .set("color", "#31577f");

	    iconContainer.add(mobileIcon);

	    /*
	     * Text Section
	     */
	    VerticalLayout textLayout = new VerticalLayout();

	    textLayout.setPadding(false);
	    textLayout.setSpacing(false);
	    textLayout.setWidthFull();

	    H3 title = new H3("Get the APMIS mobile app");

	    title.getStyle()
	            .set("margin", "0")
	            .set("font-size", "20px")
	            .set("font-weight", "600")
	            .set("color", "#2d3436");

	    Paragraph description = new Paragraph(
	            "Download the app to collect campaign data in the field, even offline.");

	    description.getStyle()
	            .set("margin", "4px 0 0 0")
	            .set("font-size", "16px")
	            .set("color", "#555");

	    /*
	     * Download Button
	     */
	    Button downloadMobileApp = new Button("Download APMIS Mobile App");

	    downloadMobileApp.setIcon(VaadinIcon.DOWNLOAD.create());

	    downloadMobileApp.getStyle()
	            .set("margin-top", "16px")
	            .set("font-size", "16px")
	            .set("font-weight", "500");

	    downloadMobileApp.addClickListener(event -> {

	        String version = getApplicationVersion();

	        String apkUrl = "/downloads/apmis-"
	                + version
	                + "-release.apk";

	        UI.getCurrent().getPage().open(apkUrl, "_blank");
	    });

	    textLayout.add(title, description);

	    cardContent.add(iconContainer, textLayout, downloadMobileApp);

	    mobileAppCard.add(cardContent);

	    return mobileAppCard;
	}
	
	private String getApplicationVersion() {

	    Properties properties = new Properties();

	    try (InputStream inputStream = getClass()
	            .getClassLoader()
	            .getResourceAsStream("application-version.properties")) {

	        if (inputStream != null) {
	            properties.load(inputStream);
	            return properties.getProperty("app.version");
	        }

	    } catch (IOException e) {
	        throw new RuntimeException("Unable to read application version", e);
	    }

	    throw new RuntimeException("Application version not found");
	}
}
