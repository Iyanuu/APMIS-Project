package com.cinoteck.application.views.campaign;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.treegrid.TreeGrid;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignTreeFlatDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDtoImpl;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.community.Modality;
import de.symeda.sormas.api.infrastructure.community.Status;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

@PageTitle("APMIS-Edit Campaign")
@Route(value = "/databb")
public class AssociateCampaign extends VerticalLayout {
	private static final long serialVersionUID = 764300181578209719L;

	private java.util.Map<String, CampaignTreeGridDto> parentMap = new java.util.HashMap<>();
	public static TreeGrid<CampaignTreeGridDto> treeGrid = new TreeGrid<>();
	private UserProvider userProvider = new UserProvider();
	private CampaignDto campaignDto;
	private Set<AreaReferenceDto> areass = new HashSet<>();
	private Set<RegionReferenceDto> region = new HashSet<>();
	private Set<DistrictReferenceDto> districts = new HashSet<>();
	private Set<CommunityReferenceDto> community = new HashSet<>();
	private Set<PopulationDataDto> popopulationDataDtoSet = new HashSet<>();
	public Boolean isCreateForm = null;
	List<CampaignTreeGridDto> deletelist = new ArrayList<>();
	List<String> populationDataUuid = new ArrayList<>();
	Checkbox selectDistrictCheckbox = new Checkbox();
	private boolean isSingleSelectClickItemLock;
	private boolean isMultiSelectItemLock;
	private boolean isSelectItemLock;
	private boolean isInitializing = false;
	FormLayout formx;
	TextField creatingUuid = new TextField(I18nProperties.getCaption(Captions.uuid));
	private final Map<String, Checkbox> deleteCheckboxMap = new HashMap<>();

	private java.util.Map<String, Checkbox> checkboxMap = new java.util.HashMap<>();

	ComboBox<String> districtModality = new ComboBox<String>("Modality");
	ComboBox<String> districtStatus = new ComboBox<String>("Status");
	IntegerField popDataAge4_59M = new IntegerField("Target 4_59M");
	IntegerField popDataAge4_23M = new IntegerField("Target 4_23M");
	IntegerField popDataAge5_10 = new IntegerField("Target 60-120M");
	IntegerField popData0_4 = new IntegerField("Target 0-59M");
	ComboBox<CommunityReferenceDto> clusterFilter = new ComboBox<>(I18nProperties.getCaption(Captions.community));
	ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>(I18nProperties.getCaption(Captions.district));
	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<AreaReferenceDto>();
	HorizontalLayout buttonAfterLay = new HorizontalLayout();
	List<PopulationDataDto> populationDataList = new ArrayList<PopulationDataDto>();

	Set<String> selectedAreaUuids = new HashSet<>();
	Set<String> selectedRegionUuids = new HashSet<>();
	Set<String> selectedDistrictUuids = new HashSet<>();
	Set<String> selectedClusterUuids = new HashSet<>();

	private Set<String> pendingSelectedClusters = new HashSet<>();
	private Set<String> pendingDeselectedClusters = new HashSet<>();
	boolean hasPendingChanges = false;
	private final Span selectedClusterCountLabel = new Span("Selected clusters: 0");

	
	private boolean hasUnsavedChanges = false;
	private boolean suppressUnsavedTracking = false;
	private Runnable onDirtyStateChanged;


	private FooterRow footerRow;

	protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

	public AssociateCampaign(CampaignDto formData) {
		super();
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		this.campaignDto = formData;
		isCreateForm = formData == null;
		addClassName("campaign-form");
		if (campaignDto != null) {
			add(configureTreeGrid(false, formData));
		} else {
			Div savecampaignTextAssoccamp = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));
			add(savecampaignTextAssoccamp);
		}
	}
	

private void updateDistrictDeleteCheckboxState(CampaignTreeGridDto cluster) {
    if (cluster == null) return;
    
    // Find the parent district using a more robust method
    CampaignTreeGridDto district = findParentDistrictRobust(cluster);
    if (district == null) {
        System.out.println("Could not find parent district for cluster: " + cluster.getName());
        return;
    }
    
    List<CampaignTreeGridDto> childClusters = district.getClusterData();
    if (childClusters == null || childClusters.isEmpty()) {
        return;
    }
    
    // Check if ALL child clusters are selected for deletion
    boolean allSelected = childClusters.stream()
        .allMatch(c -> deletelist.contains(c));
    
    // Check if ANY child cluster is selected
    boolean anySelected = childClusters.stream()
        .anyMatch(c -> deletelist.contains(c));
    
    // Update district checkbox
    Checkbox districtCheckbox = deleteCheckboxMap.get(district.getUuid());
    if (districtCheckbox != null) {
        // Set to "all selected" if all are selected, otherwise "indeterminate" if some are selected
        // Vaadin Checkbox doesn't support indeterminate directly, so we'll use boolean
        if (allSelected) {
            districtCheckbox.setValue(true);
        } else if (anySelected) {
            // Keep current value, but we could add a visual indicator
            // For now, keep it as is or set to false to show partial selection
            districtCheckbox.setValue(false);
        } else {
            districtCheckbox.setValue(false);
        }
    }
}

/**
 * Robust method to find the parent district of a cluster
 * Uses multiple approaches to ensure parent is found
 */
private CampaignTreeGridDto findParentDistrictRobust(CampaignTreeGridDto cluster) {
    if (cluster == null) return null;
    
    // Method 1: Use parentMap if available
    CampaignTreeGridDto parent = parentMap.get(cluster.getUuid());
    if (parent != null && "district".equals(parent.getLevelAssessed())) {
        return parent;
    }
    
    // Method 2: Search through the tree data
    if (treeGrid == null || treeGrid.getTreeData() == null) {
        return null;
    }
    
    List<CampaignTreeGridDto> roots = treeGrid.getTreeData().getRootItems();
    if (roots == null || roots.isEmpty()) {
        return null;
    }
    
    for (CampaignTreeGridDto area : roots) {
        for (CampaignTreeGridDto region : area.getRegionData()) {
            for (CampaignTreeGridDto district : region.getDistrictData()) {
                List<CampaignTreeGridDto> childClusters = district.getClusterData();
                if (childClusters != null && childClusters.contains(cluster)) {
                    return district;
                }
            }
        }
    }
    
    // Method 3: Check by UUID pattern (cluster UUID contains district info)
    // This is a fallback - might need to be customized based on your UUID structure
    for (CampaignTreeGridDto area : roots) {
        for (CampaignTreeGridDto region : area.getRegionData()) {
            for (CampaignTreeGridDto district : region.getDistrictData()) {
                List<CampaignTreeGridDto> childClusters = district.getClusterData();
                if (childClusters != null) {
                    for (CampaignTreeGridDto child : childClusters) {
                        if (child.getUuid().equals(cluster.getUuid())) {
                            return district;
                        }
                    }
                }
            }
        }
    }
    
    System.out.println("DEBUG: Could not find parent district for cluster: " + cluster.getName() + " (UUID: " + cluster.getUuid() + ")");
    return null;
}

	
	private Component createHeaderWithTooltip(String label, String tooltipText) {
	    Span span = new Span(label);
	    Tooltip tooltip = Tooltip.forComponent(span);
	    tooltip.setText(tooltipText);
	    tooltip.setPosition(Tooltip.TooltipPosition.TOP);
	    return span;
	}
	
	public HorizontalLayout configureTreeGrid(boolean isDeletePopulationData, CampaignDto formData) {
		ComponentRenderer<Span, CampaignTreeGridDto> populationGenerate = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			} else {
				arabicFormat = NumberFormat.getInstance(new Locale("en"));
			}
			String value = String.valueOf(arabicFormat.format(input.getPopulationData()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignTreeGridDto> populationGenerate5_10 = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			} else {
				arabicFormat = NumberFormat.getInstance(new Locale("en"));
			}
			String value = String.valueOf(arabicFormat.format(input.getPopulationData5_10()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignTreeGridDto> populationGenerate4_23M = new ComponentRenderer<>(input -> {
			Locale locale;
			String lang = userProvider.getUser().getLanguage().toString();
			if ("Pashto".equals(lang)) {
				locale = new Locale("ps");
			} else if ("Dari".equals(lang)) {
				locale = new Locale("fa");
			} else {
				locale = Locale.ENGLISH;
			}
			NumberFormat format = NumberFormat.getInstance(locale);
			Number population = input.getPopulationData4_23M();
			String value = population != null ? format.format(population) : "0";
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color)");
			return label;
		});
		
		ComponentRenderer<Span, CampaignTreeGridDto> populationGenerate4_59M = new ComponentRenderer<>(input -> {
			Locale locale;
			String lang = userProvider.getUser().getLanguage().toString();
			if ("Pashto".equals(lang)) {
				locale = new Locale("ps");
			} else if ("Dari".equals(lang)) {
				locale = new Locale("fa");
			} else {
				locale = Locale.ENGLISH;
			}
			NumberFormat format = NumberFormat.getInstance(locale);
			Number population = input.getPopulationData4_59M();
			String value = population != null ? format.format(population) : "0";
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color)");
			return label;
		});

		// Enhanced checkbox renderer with pending change tracking
		ComponentRenderer<Component, CampaignTreeGridDto> selectionCheckboxRenderer = new ComponentRenderer<>(dto -> {
			Checkbox checkbox = new Checkbox();
			checkbox.setId("chk_" + dto.getId());

			// Set initial state from DTO
			boolean isSelected = false;
			if (dto.getLevelAssessed().equals("cluster")) {
				isSelected = "true".equalsIgnoreCase(dto.getSavedData()) || dto.getSelected();
			} else {
				isSelected = hasSelectedClusterBelow(dto);
			}
			checkbox.setValue(isSelected);

			// Store checkbox reference for programmatic updates
			checkboxMap.put(dto.getUuid(), checkbox);

			checkbox.addValueChangeListener(event -> {
				if (isInitializing)
					return;

				boolean newValue = event.getValue();
				CampaignTreeGridDto currentDto = dto;

				isInitializing = true;
				try {
					Set<String> affectedClusters = new HashSet<>();
					collectAllClustersUnderItem(currentDto, affectedClusters);

					if (newValue) {
						pendingSelectedClusters.addAll(affectedClusters);
						pendingDeselectedClusters.removeAll(affectedClusters);
						selectItemAndAllDescendants(currentDto);
					} else {
						pendingDeselectedClusters.addAll(affectedClusters);
						pendingSelectedClusters.removeAll(affectedClusters);
						deselectItemAndAllDescendants(currentDto);
					}

					hasPendingChanges = true;
					 markDirty();

					// If a cluster was clicked, only walk upward
					if ("cluster".equals(currentDto.getLevelAssessed())) {
						updateParents(currentDto);
					} else {
						// Area / Region / District clicked
						updateAllParentSelections();
					}

					recomputeAllTotals();

					showPendingChangesNotification(affectedClusters.size(), newValue);

				} catch (Exception ex) {
					logger.error("Error updating selection", ex);
					Notification.show("Error updating selection: " + ex.getMessage())
							.addThemeVariants(NotificationVariant.LUMO_ERROR);

					checkbox.setValue(!newValue);
				} finally {
					isInitializing = false;
				}
			});

			return checkbox;
		});

		treeGrid = new TreeGrid<>();
		treeGrid.removeAllColumns();
		treeGrid.setWidthFull();

		// Set page size for better performance
//        treeGrid.setPageSize(50);

		// Disable default selection model - we'll use custom checkboxes only
//        treeGrid.setSelectionMode(SelectionMode.NONE);
//        treeGrid.asSingleSelect();

		treeGrid.setItems(generateTreeGridData(), item -> {
			if ("area".equals(item.getLevelAssessed())) {
				return item.getRegionData();
			} else if ("region".equals(item.getLevelAssessed())) {
				return item.getDistrictData();
			} else if ("district".equals(item.getLevelAssessed())) {
				return item.getClusterData();
			} else {
				return Collections.emptyList();
			}
		});

//		initializeDeleteList();
		deletelist.clear();
		updateAllDeleteCheckboxStates();
		recomputeAllTotals();

		buildParentMap();
		treeGrid.setWidthFull();

		// Add columns
		treeGrid.addColumn(selectionCheckboxRenderer).setHeader(createHeaderWithTooltip("Select", "Select")).setWidth("70px").setFlexGrow(0);
		treeGrid.addHierarchyColumn(CampaignTreeGridDto::getName)
				.setHeader(createHeaderWithTooltip(I18nProperties.getCaption(Captions.Location), I18nProperties.getCaption(Captions.Location))).setAutoWidth(true).setResizable(true)
				.setTooltipGenerator(CampaignTreeGridDto::getName);

		treeGrid.addColumn(populationGenerate).setHeader(createHeaderWithTooltip("Target (0-59M)", "Target (0-59M)")).setResizable(true)
				.setTooltipGenerator(input -> {
					NumberFormat arabicFormat = NumberFormat.getInstance();
					if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
						arabicFormat = NumberFormat.getInstance(new Locale("ps"));
					} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
						arabicFormat = NumberFormat.getInstance(new Locale("fa"));
					} else {
						arabicFormat = NumberFormat.getInstance(new Locale("en"));
					}
					return arabicFormat.format(input.getPopulationData());
				});

		treeGrid.addColumn(populationGenerate5_10).setHeader(createHeaderWithTooltip("Target (60-120M)", "Target (60-120M)")).setResizable(true)
				.setTooltipGenerator(input -> {
					NumberFormat arabicFormat = NumberFormat.getInstance();
					if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
						arabicFormat = NumberFormat.getInstance(new Locale("ps"));
					} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
						arabicFormat = NumberFormat.getInstance(new Locale("fa"));
					} else {
						arabicFormat = NumberFormat.getInstance(new Locale("en"));
					}
					return arabicFormat.format(input.getPopulationData5_10());
				});

		treeGrid.addColumn(populationGenerate4_23M).setHeader(createHeaderWithTooltip("Target (4-23M)", "Target (4-23M)")).setResizable(true)
				.setTooltipGenerator(input -> {
					Locale locale;
					String lang = userProvider.getUser().getLanguage().toString();
					if ("Pashto".equals(lang)) {
						locale = new Locale("ps");
					} else if ("Dari".equals(lang)) {
						locale = new Locale("fa");
					} else {
						locale = Locale.ENGLISH;
					}
					NumberFormat format = NumberFormat.getInstance(locale);
					Number population = input.getPopulationData4_23M();
					return population != null ? format.format(population) : "0";
				});
		
		treeGrid.addColumn(populationGenerate4_59M).setHeader(createHeaderWithTooltip("Target (4-59M)", "Target (4-59M)")).setResizable(true)
		.setTooltipGenerator(input -> {
			Locale locale;
			String lang = userProvider.getUser().getLanguage().toString();
			if ("Pashto".equals(lang)) {
				locale = new Locale("ps");
			} else if ("Dari".equals(lang)) {
				locale = new Locale("fa");
			} else {
				locale = Locale.ENGLISH;
			}
			NumberFormat format = NumberFormat.getInstance(locale);
			Number population = input.getPopulationData4_59M();
			return population != null ? format.format(population) : "0";
		});

		treeGrid.addColumn(CampaignTreeGridDto::getDistrictModality).setHeader(createHeaderWithTooltip("Modality", "Modality")).setResizable(true)
				.setTooltipGenerator(CampaignTreeGridDto::getDistrictModality);
		treeGrid.addColumn(CampaignTreeGridDto::getDistrictStatus).setHeader(createHeaderWithTooltip("Status", "Status")).setResizable(true)
				.setTooltipGenerator(CampaignTreeGridDto::getDistrictStatus);

		// Add delete column

		
		// Add delete column with district-level selection
		// Add delete column with district-level selection
		ComponentRenderer<Component, CampaignTreeGridDto> deleteCheckboxRenderer = new ComponentRenderer<>(dto -> {
		    Checkbox deleteCheckbox = new Checkbox();
		    deleteCheckbox.setId("delete_chk_" + dto.getUuid());
		    
		    // Store checkbox reference for programmatic updates
		    deleteCheckboxMap.put(dto.getUuid(), deleteCheckbox);
		    
		    // Determine if this item should show a delete checkbox
		    boolean isCluster = "cluster".equals(dto.getLevelAssessed());
		    boolean isDistrict = "district".equals(dto.getLevelAssessed());
		    boolean isProvince = "province".equals(dto.getLevelAssessed());

		    if (isCluster) {

		        if (isCluster) {
		        	// For clusters: check if in deletelist
		            deleteCheckbox.setValue(deletelist.contains(dto));
//		        	deleteCheckbox.setValue(false);
		        }
		        
		        deleteCheckbox.addValueChangeListener(event -> {
		        	
		        	if (isInitializing) {
		        	    return;
		        	}
		            boolean isSelected = event.getValue();

		            if (isCluster) { 
		                // Cluster-level selection
		                if (isSelected) {
		                    if (!deletelist.contains(dto)) {
		                        deletelist.add(dto);
		                    }
		                } else {
		                    deletelist.remove(dto);
		                }
		                
		                // Update parent district checkbox
		                updateDistrictDeleteCheckboxState(dto);
		            }
		            
		            // Log for debugging
		            System.out.println("Deletelist size: " + deletelist.size());
		            System.out.println("Selected clusters: " + deletelist.stream()
		                .map(CampaignTreeGridDto::getName)
		                .collect(Collectors.joining(", ")));
		            
		            // Refresh grid to show updated states
		            treeGrid.getDataProvider().refreshAll();
		        });
		        
		        return deleteCheckbox;
		    } else {
		        // Areas and Regions: return empty span (no delete checkbox)
		        return new Span();
		    }
		});
		
		
		treeGrid.addColumn(deleteCheckboxRenderer).setHeader(createHeaderWithTooltip("Delete?", "Delete?")).setWidth("70px").setFlexGrow(0);

		// Load selected UUIDs from campaign DTO
		selectedAreaUuids = campaignDto.getAreas().stream().map(AreaReferenceDto::getUuid).collect(Collectors.toSet());
		selectedRegionUuids = campaignDto.getRegion().stream().map(RegionReferenceDto::getUuid)
				.collect(Collectors.toSet());
		selectedDistrictUuids = campaignDto.getDistricts().stream().map(DistrictReferenceDto::getUuid)
				.collect(Collectors.toSet());
		selectedClusterUuids = campaignDto.getCommunity().stream().map(CommunityReferenceDto::getUuid)
				.collect(Collectors.toSet());

		// Initialize selections from DB state
		UI.getCurrent().access(() -> {
			// Small delay to allow all checkboxes to be rendered by Vaadin
			UI.getCurrent().getPage().executeJs("setTimeout(() => { $0._$server.initializeSelections(); }, 150);",
					getElement());
		});

		// Item click listener for editing clusters
		treeGrid.addItemClickListener(ee -> {
			if (campaignDto != null && ee.getItem().getLevelAssessed().equals("cluster")) {
				openEditDialog(ee.getItem());
			}
		});

		updateAllParentSelections();

		treeGrid.getDataProvider().refreshAll();
		updateFooterTotals();

		// Enhanced Refresh button with confirmation and pending changes commit
		Button refreshTreeGridBtn = new Button("Refresh Tree Grid", new Icon(VaadinIcon.REFRESH));
		refreshTreeGridBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		refreshTreeGridBtn.addClickListener(e -> {
			refreshWithConfirmation();
		});

		// Add Save Changes button for explicit save
		Button saveChangesBtn = new Button("Save Changes", new Icon(VaadinIcon.CHECK));
//        saveChangesBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		saveChangesBtn.getElement().getStyle().set("color", "white");
		saveChangesBtn.getElement().getStyle().set("background", "#0D6938");
		saveChangesBtn.addClickListener(e -> {
			commitPendingChanges();
		});

		// Add button to show pending changes
		Button showPendingBtn = new Button("Show Pending", new Icon(VaadinIcon.INFO));
//        showPendingBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		showPendingBtn.getElement().getStyle().set("color", "white");
		showPendingBtn.getElement().getStyle().set("background", "#F08F3E");

		showPendingBtn.addClickListener(e -> {

			showPendingChangesDialog();
		});
		
		
		selectedClusterCountLabel.getStyle().set("font-weight", "600");
		selectedClusterCountLabel.getStyle().set("margin-left", "8px");
		selectedClusterCountLabel.getStyle().set("align-self", "center");

		HorizontalLayout buttonBar =
		    new HorizontalLayout(refreshTreeGridBtn, saveChangesBtn, showPendingBtn, selectedClusterCountLabel);


//		HorizontalLayout buttonBar = new HorizontalLayout(refreshTreeGridBtn, saveChangesBtn, showPendingBtn);
		buttonBar.setSpacing(true);
		buttonBar.setJustifyContentMode(JustifyContentMode.END);

		Component popEditForm = configurePopulationPopEdit(formData);

		VerticalLayout gridWithButtonLayout = new VerticalLayout(treeGrid, buttonBar);
		gridWithButtonLayout.setPadding(false);
		gridWithButtonLayout.setSpacing(true);
		gridWithButtonLayout.setWidthFull();

		HorizontalLayout assocCampaignLayout = new HorizontalLayout();
		assocCampaignLayout.setWidthFull();
		assocCampaignLayout.setHeight("80vh");

		assocCampaignLayout.add(gridWithButtonLayout, popEditForm);
		assocCampaignLayout.setFlexGrow(4, gridWithButtonLayout);
		
		refreshSelectedClusterCountLabel(); 
		
		return assocCampaignLayout;
	}
	
	/**
	 * Update all delete checkbox states to match the current deletelist
	 */
	private void updateAllDeleteCheckboxStates() {
	    if (treeGrid == null || treeGrid.getTreeData() == null) {
	        return;
	    }
	    
	    List<CampaignTreeGridDto> roots = treeGrid.getTreeData().getRootItems();
	    if (roots == null) {
	        return;
	    }
	    
	    for (CampaignTreeGridDto area : roots) {
	        for (CampaignTreeGridDto region : area.getRegionData()) {
	            for (CampaignTreeGridDto district : region.getDistrictData()) {
	                // Update district checkbox
	                List<CampaignTreeGridDto> childClusters = district.getClusterData();
	                if (childClusters != null && !childClusters.isEmpty()) {
	                    boolean allSelected = childClusters.stream()
	                        .allMatch(c -> deletelist.contains(c));
	                    
	                    Checkbox districtCheckbox = deleteCheckboxMap.get(district.getUuid());
	                    if (districtCheckbox != null) {
	                        districtCheckbox.setValue(allSelected);
	                    }
	                    
	                    // Update individual cluster checkboxes
	                    for (CampaignTreeGridDto cluster : childClusters) {
	                        Checkbox clusterCheckbox = deleteCheckboxMap.get(cluster.getUuid());
	                        if (clusterCheckbox != null) {
	                            clusterCheckbox.setValue(deletelist.contains(cluster));
	                        }
	                    }
	                }
	            }
	        }
	    }
	}

	
	// FIXED: Optimized generateTreeGridData with selection state from DB
	
	private List<CampaignTreeGridDto> generateTreeGridData() {

	    List<CampaignTreeGridDto> gridData = new ArrayList<>();

	    List<CampaignTreeFlatDto> flatRows =
	            FacadeProvider.getPopulationDataFacade()
	                    .getAllTreeDataForCampaign(campaignDto.getUuid());

	    Map<String, CampaignTreeGridDto> areaMap = new LinkedHashMap<>();
	    Map<String, CampaignTreeGridDto> regionMap = new LinkedHashMap<>();
	    Map<String, CampaignTreeGridDto> districtMap = new LinkedHashMap<>();

	    // Stores district selection from DB
	    // Used only when a district has no cluster records
	    Map<String, Boolean> districtSelectionMap = new HashMap<>();

	    for (CampaignTreeFlatDto row : flatRows) {

	        // =====================================================
	        // AREA
	        // =====================================================
	        CampaignTreeGridDto area =
	                areaMap.computeIfAbsent(row.areaUuid, k -> {

	                    CampaignTreeGridDto dto =
	                            new CampaignTreeGridDto(
	                                    row.areaName,
	                                    row.areaId,
	                                    "Area",
	                                    row.areaUuid,
	                                    "area");

	                    gridData.add(dto);

	                    return dto;
	                });

	        // =====================================================
	        // REGION
	        // =====================================================
	        CampaignTreeGridDto region =
	                regionMap.computeIfAbsent(row.regionUuid, k -> {

	                    CampaignTreeGridDto dto =
	                            new CampaignTreeGridDto(
	                                    row.regionName,
	                                    row.regionId,
	                                    row.areaUuid,
	                                    row.regionUuid,
	                                    "region");

	                    area.addRegionData(dto);

	                    return dto;
	                });

	        // =====================================================
	        // DISTRICT
	        // =====================================================
	        districtSelectionMap.putIfAbsent(
	                row.districtUuid,
	                Boolean.TRUE.equals(row.districtSelected));

	        CampaignTreeGridDto district =
	                districtMap.computeIfAbsent(row.districtUuid, k -> {

	                    CampaignTreeGridDto dto =
	                            new CampaignTreeGridDto(
	                                    row.districtName,
	                                    row.regionId,
	                                    row.regionUuid,
	                                    row.districtUuid,
	                                    "district",
	                                    false); // resolved later

	                    region.addDistrictData(dto);

	                    return dto;
	                });

	        // =====================================================
	        // CLUSTER
	        // =====================================================
	        if (row.clusterUuid != null && !row.clusterUuid.trim().isEmpty()) {

	            Long pop0_4 = row.pop0_4 == null ? 0L : row.pop0_4;
	            Long pop5_10 = row.pop5_10 == null ? 0L : row.pop5_10;
	            Long pop4_23m = row.pop4_23m == null ? 0L : row.pop4_23m;
	            Long pop4_59m = row.pop4_59m == null ? 0L : row.pop4_59m;


	            Long totalPopulation =
	                    		pop0_4
	                            + pop5_10
	                            + pop4_23m
	                            + pop4_59m;

	            CampaignTreeGridDto cluster =
	                    new CampaignTreeGridDtoImpl(
	                            row.clusterName,
	                            pop0_4,
	                            pop5_10,
	                            pop4_23m,
	                            pop4_59m,
	                            row.clusterId,
	                            row.districtUuid,
	                            row.clusterUuid,
	                            "cluster",
	                            Boolean.TRUE.equals(row.clusterSelected),
	                            row.clusterModality,
	                            row.clusterStatus,
	                            row.clusterFloating,
	                            totalPopulation);

	            district.addClusterData(cluster);
	        }
	    }

	    // =========================================================
	    // CALCULATE DISTRICT TOTALS + DISTRICT SELECTION
	    // =========================================================
	    for (CampaignTreeGridDto area : gridData) {

	        for (CampaignTreeGridDto region : area.getRegionData()) {

	            for (CampaignTreeGridDto district : region.getDistrictData()) {

	                boolean hasClusters =
	                        district.getClusterData() != null
	                                && !district.getClusterData().isEmpty();

	                if (hasClusters) {

	                    long district0_4 =
	                            district.getClusterData().stream()
	                                    .mapToLong(c -> c.getPopulationData() == null ? 0L : c.getPopulationData())
	                                    .sum();

	                    long district5_10 =
	                            district.getClusterData().stream()
	                                    .mapToLong(c -> c.getPopulationData5_10() == null ? 0L : c.getPopulationData5_10())
	                                    .sum();

	                    long district4_23m =
	                            district.getClusterData().stream()
	                                    .mapToLong(c -> c.getPopulationData4_23M() == null ? 0L : c.getPopulationData4_23M())
	                                    .sum();
	                    
	                    long district4_59m =
	                            district.getClusterData().stream()
	                                    .mapToLong(c -> c.getPopulationData4_59M() == null ? 0L : c.getPopulationData4_59M())
	                                    .sum();

	                    district.setPopulationData(district0_4);
	                    district.setPopulationData5_10(district5_10);
	                    district.setPopulationData4_23M(district4_23m);
	                    district.setPopulationData4_59M(district4_59m);

	                    // District selected if any cluster selected
	                    boolean districtSelected =
	                            district.getClusterData().stream()
	                                    .anyMatch(CampaignTreeGridDto::getSelected);

	                    district.setSelected(districtSelected);

	                } else {

	                    // No clusters -> use district DB selection
	                    district.setSelected(districtSelectionMap.getOrDefault(district.getUuid(),false));
	                }
	            }

	            // =====================================================
	            // REGION TOTALS
	            // =====================================================
	            long region0_4 =
	                    region.getDistrictData().stream()
	                            .mapToLong(d -> d.getPopulationData() == null ? 0L : d.getPopulationData())
	                            .sum();

	            long region5_10 =
	                    region.getDistrictData().stream()
	                            .mapToLong(d -> d.getPopulationData5_10() == null ? 0L : d.getPopulationData5_10())
	                            .sum();

	            long region4_23m =
	                    region.getDistrictData().stream()
	                            .mapToLong(d -> d.getPopulationData4_23M() == null ? 0L : d.getPopulationData4_23M())
	                            .sum();
	            
	            long region4_59m =
	                    region.getDistrictData().stream()
	                            .mapToLong(d -> d.getPopulationData4_59M() == null ? 0L : d.getPopulationData4_59M())
	                            .sum();


	            region.setPopulationData(region0_4);
	            region.setPopulationData5_10(region5_10);
	            region.setPopulationData4_23M(region4_23m);
	            region.setPopulationData4_59M(region4_59m);


	            // Region selected if any district selected
	            boolean regionSelected =
	                    region.getDistrictData().stream().anyMatch(CampaignTreeGridDto::getSelected);

	            region.setSelected(regionSelected);
	        }

	        // =====================================================
	        // AREA TOTALS
	        // =====================================================
	        long area0_4 =
	                area.getRegionData().stream()
	                        .mapToLong(r -> r.getPopulationData() == null ? 0L : r.getPopulationData())
	                        .sum();

	        long area5_10 =
	                area.getRegionData().stream()
	                        .mapToLong(r -> r.getPopulationData5_10() == null ? 0L : r.getPopulationData5_10())
	                        .sum();

	        long area4_23m =
	                area.getRegionData().stream()
	                        .mapToLong(r -> r.getPopulationData4_23M() == null ? 0L : r.getPopulationData4_23M())
	                        .sum();
	        
	        long area4_59m =
	                area.getRegionData().stream()
	                        .mapToLong(r -> r.getPopulationData4_59M() == null ? 0L : r.getPopulationData4_59M())
	                        .sum();

	        area.setPopulationData(area0_4);
	        area.setPopulationData5_10(area5_10);
	        area.setPopulationData4_23M(area4_23m);
	        area.setPopulationData4_59M(area4_59m);


	        // Area selected if any region selected
	        boolean areaSelected =
	                area.getRegionData().stream()
	                        .anyMatch(CampaignTreeGridDto::getSelected);

	        area.setSelected(areaSelected);
	    }

	    return gridData;
	}
	
	
	private void buildParentMap() {
		parentMap.clear();
		for (CampaignTreeGridDto area : treeGrid.getTreeData().getRootItems()) {
			buildParentMapRecursive(area, null);
		}
	}

	private boolean hasSelectedClusterBelow(CampaignTreeGridDto item) {

		if ("cluster".equals(item.getLevelAssessed())) {
			return item.getSelected() || "true".equalsIgnoreCase(item.getSavedData());
		}

		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			if (hasSelectedClusterBelow(child)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Recursively compute population totals for an item based on selected clusters.
	 * For clusters: use its own population values only if selected. For parents:
	 * sum the totals of selected child clusters (or child parents' totals).
	 */
	private void computeTotalsForItem(CampaignTreeGridDto item) {
		if (item == null)
			return;
		if ("cluster".equals(item.getLevelAssessed())) {
			// cluster values are fixed; nothing to compute here
			return;
		}
		long total0_59 = 0;
		long total5_10 = 0;
		long total4_23M = 0;
		long total4_59M = 0;

		List<CampaignTreeGridDto> children = getChildrenForSelection(item);
		for (CampaignTreeGridDto child : children) {
			// post-order: ensure child totals are computed first
			computeTotalsForItem(child);
			if ("cluster".equals(child.getLevelAssessed())) {
				// only include if the cluster is selected (including pending selection)
				if (child.getSelected() || "true".equalsIgnoreCase(child.getSavedData())) {
					total0_59 += child.getPopulationData() != null ? child.getPopulationData() : 0L;
					total5_10 += child.getPopulationData5_10() != null ? child.getPopulationData5_10() : 0L;
					total4_23M += child.getPopulationData4_23M() != null ? child.getPopulationData4_23M() : 0L;
					total4_59M += child.getPopulationData4_59M() != null ? child.getPopulationData4_59M() : 0L;

				}
			} else {
				// child is a parent; its totals already reflect selected descendants
				total0_59 += child.getPopulationData() != null ? child.getPopulationData() : 0L;
				total5_10 += child.getPopulationData5_10() != null ? child.getPopulationData5_10() : 0L;
				total4_23M += child.getPopulationData4_23M() != null ? child.getPopulationData4_23M() : 0L;
				total4_59M += child.getPopulationData4_59M() != null ? child.getPopulationData4_59M() : 0L;

			}
		}
		item.setPopulationData(total0_59);
		item.setPopulationData5_10(total5_10);
		item.setPopulationData4_23M(total4_23M);
		item.setPopulationData4_59M(total4_59M);

	}
	

	private void recomputeAllTotals() {
		if (treeGrid == null || treeGrid.getTreeData() == null)
			return;
		for (CampaignTreeGridDto root : treeGrid.getTreeData().getRootItems()) {
			computeTotalsForItem(root);
		}
		treeGrid.getDataProvider().refreshAll();
		updateFooterTotals(); // footer now uses updated root totals
		refreshSelectedClusterCountLabel();
	}

	private void updateFooterTotals() {
		if (footerRow == null) {
			footerRow = treeGrid.appendFooterRow();
			// Optional: style the footer row
//            footerRow.getStyle().set("font-weight", "bold");
//            footerRow.getStyle().set("background", "#f0f0f0");
		}

		List<CampaignTreeGridDto> roots = treeGrid.getTreeData().getRootItems();
		long total0_59 = 0;
		long total5_10 = 0;
		long total4_23M = 0;

		long total4_59M = 0;

		for (CampaignTreeGridDto root : roots) {
			total0_59 += root.getPopulationData() != null ? root.getPopulationData() : 0L;
			total5_10 += root.getPopulationData5_10() != null ? root.getPopulationData5_10() : 0L;
			total4_23M += root.getPopulationData4_23M() != null ? root.getPopulationData4_23M() : 0L;
			total4_59M += root.getPopulationData4_59M() != null ? root.getPopulationData4_59M() : 0L;

		}

		// Determine number format based on user language
		NumberFormat format = NumberFormat.getInstance();
		String lang = userProvider.getUser().getLanguage().toString();
		if ("Pashto".equals(lang)) {
			format = NumberFormat.getInstance(new Locale("ps"));
		} else if ("Dari".equals(lang)) {
			format = NumberFormat.getInstance(new Locale("fa"));
		} else {
			format = NumberFormat.getInstance(new Locale("en"));
		}

		// Get columns in order (they were added in the same order)
		List<Grid.Column<CampaignTreeGridDto>> columns = treeGrid.getColumns();
		if (columns.size() < 8)
			return; // safety check

		// Column indices:
		// 0 - selection checkbox, 1 - hierarchy name, 2 - target 0-59, 3 - target
		// 60-120, 4 - target 4-23, 5 - target 4-59, 6 - modality, 7 - status, 8 - delete checkbox
		footerRow.getCell(columns.get(1)).setText("National Target Total");
		footerRow.getCell(columns.get(2)).setText(format.format(total0_59));
		footerRow.getCell(columns.get(3)).setText(format.format(total5_10));
		footerRow.getCell(columns.get(4)).setText(format.format(total4_23M));
		footerRow.getCell(columns.get(5)).setText(format.format(total4_59M));
		footerRow.getCell(columns.get(6)).setText(""); // Modality – no total
		footerRow.getCell(columns.get(7)).setText(""); // Status – no total
		// Delete column (index 7) remains empty
	}

	/**
	 * Show notification for pending changes
	 */
	private void showPendingChangesNotification(int clusterCount, boolean isSelected) {
		if (hasPendingChanges) {
			Notification notification = new Notification();
			notification.setDuration(2000);
			notification.setPosition(Notification.Position.BOTTOM_END);

			Span message = new Span(isSelected ? "✓ " + clusterCount + " clusters pending selection"
					: "✗ " + clusterCount + " clusters pending deselection");
			message.getStyle().set("color", "var(--lumo-primary-color)");

			Button saveBtn = new Button("Save Now", click -> {
				notification.close();
				commitPendingChanges();
			});
			saveBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

			HorizontalLayout layout = new HorizontalLayout(message, saveBtn);
			layout.setAlignItems(Alignment.CENTER);
			layout.setSpacing(true);

			notification.add(layout);
			notification.open();
		}
	}

	/**
	 * Show dialog with pending changes details
	 */
	private void showPendingChangesDialog() {
		if (!hasPendingChanges) {
			Notification.show("No pending changes to save").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			return;
		}

		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("Pending Changes");
		dialog.setWidth("500px");

		VerticalLayout content = new VerticalLayout();

		Span title = new Span("The following changes are pending:");
		title.getStyle().set("font-weight", "bold");

		VerticalLayout changesLayout = new VerticalLayout();

		if (!pendingSelectedClusters.isEmpty()) {
			Span selectedLabel = new Span("✓ To be SELECTED: " + pendingSelectedClusters.size() + " clusters");
			selectedLabel.getStyle().set("color", "green");
			changesLayout.add(selectedLabel);
		}

		if (!pendingDeselectedClusters.isEmpty()) {
			Span deselectedLabel = new Span("✗ To be DESELECTED: " + pendingDeselectedClusters.size() + " clusters");
			deselectedLabel.getStyle().set("color", "red");
			changesLayout.add(deselectedLabel);
		}

		content.add(title, changesLayout);

		Button saveButton = new Button("Save All Changes", VaadinIcon.CHECK.create());
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
		saveButton.addClickListener(e -> {
			commitPendingChanges();
			dialog.close();
		});

		Button cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create(), e -> dialog.close());

		dialog.getFooter().add(cancelButton, saveButton);
		dialog.add(content);
		dialog.open();
	}

	/**
	 * Commit all pending changes to database with confirmation
	 */
	private void commitPendingChanges() {
		if (!hasPendingChanges) {
			Notification.show("No pending changes to save").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			return;
		}

		ConfirmDialog confirmDialog = new ConfirmDialog();
		confirmDialog.setHeader("Save Changes");

		StringBuilder message = new StringBuilder("Are you sure you want to save the following changes?\n\n");
		if (!pendingSelectedClusters.isEmpty()) {
			message.append("✓ Select ").append(pendingSelectedClusters.size()).append(" clusters\n");
		}
		if (!pendingDeselectedClusters.isEmpty()) {
			message.append("✗ Deselect ").append(pendingDeselectedClusters.size()).append(" clusters\n");
		}

		confirmDialog.setText(message.toString());
		confirmDialog.setConfirmText("Save Changes");
		confirmDialog.setCancelText("Cancel");
		confirmDialog.setConfirmButtonTheme("primary");

		confirmDialog.addConfirmListener(event -> {
			try {
				if (!pendingSelectedClusters.isEmpty()) {
					FacadeProvider.getPopulationDataFacade().updateClusterSelectionByClusterIds(
							new ArrayList<>(pendingSelectedClusters), campaignDto.getUuid(), true);
				}

				if (!pendingDeselectedClusters.isEmpty()) {
					FacadeProvider.getPopulationDataFacade().updateClusterSelectionByClusterIds(
							new ArrayList<>(pendingDeselectedClusters), campaignDto.getUuid(), false);
				}

				campaignDto = FacadeProvider.getCampaignFacade().getByUuid(campaignDto.getUuid());
				buildSelectedSetsFromCheckboxState();

				campaignDto.setAreas(new HashSet<>(areass));
				campaignDto.setRegion(new HashSet<>(region));
				campaignDto.setDistricts(new HashSet<>(districts));
				campaignDto.setCommunity(new HashSet<>(community));

				FacadeProvider.getCampaignFacade().saveCampaignPopulationData(campaignDto);

				selectedClusterUuids = campaignDto.getCommunity().stream().map(CommunityReferenceDto::getUuid)
						.collect(Collectors.toSet());

				int totalChanged = pendingSelectedClusters.size() + pendingDeselectedClusters.size();
				Notification.show("Changes saved successfully! " + totalChanged + " updates applied")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

			} catch (Exception ex) {
				logger.error("Error saving changes", ex);
				Notification.show("Error saving changes: " + ex.getMessage())
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} finally {
				// ✅ Always clear pending state — whether save succeeded or failed
				pendingSelectedClusters.clear();
				pendingDeselectedClusters.clear();
				hasPendingChanges = false;
				markClean();
				treeGrid.getDataProvider().refreshAll();
				updateAllParentSelections();
			}
			
			refreshSelectedClusterCountLabel();

		});
		confirmDialog.open();
	}

	/**
	 * Refresh with confirmation, committing pending changes first
	 */
	private void refreshWithConfirmation() {
		if (hasPendingChanges) {
			ConfirmDialog confirmDialog = new ConfirmDialog();
			confirmDialog.setHeader("Unsaved Changes");
			confirmDialog.setText("You have unsaved changes. Do you want to save them before refreshing?");
			confirmDialog.setConfirmText("Save and Refresh");
			confirmDialog.setCancelText("Refresh Without Saving");
			confirmDialog.setRejectText("Cancel");

			confirmDialog.addConfirmListener(event -> {
				// Save and then refresh
				commitPendingChangesAndRefresh();
			});

			confirmDialog.addCancelListener(event -> {
				// Refresh without saving (discard changes)
				discardPendingChanges();
				refreshTreeGrid();
			});

			confirmDialog.open();
		} else {
			// No pending changes, just refresh
			refreshTreeGrid();
		}
	}

	/**
	 * Commit pending changes and then refresh
	 */
	private void commitPendingChangesAndRefresh() {
		try {

			Notification.show("Changes saved, refreshing data...", 2000, Notification.Position.MIDDLE)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			// Refresh the grid
			refreshTreeGrid();
		} catch (Exception ex) {
			logger.error("Error saving changes before refresh", ex);
			Notification.show("Error saving changes: " + ex.getMessage())
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
		}
	}

	/**
	 * Discard all pending changes
	 */
	private void discardPendingChanges() {
		isInitializing = true;
		try {
			// Reset checkboxes to their original state from DB
			for (CampaignTreeGridDto root : treeGrid.getTreeData().getRootItems()) {
				resetCheckboxesFromDB(root);
			}

			// Clear pending changes
			pendingSelectedClusters.clear();
			pendingDeselectedClusters.clear();
			hasPendingChanges = false;
			markClean();
			Notification.show("Pending changes discarded", 2000, Notification.Position.MIDDLE)
					.addThemeVariants(NotificationVariant.LUMO_ERROR);

		} finally {
			isInitializing = false;
		}
		
		refreshSelectedClusterCountLabel();

	}

	/**
	 * Reset checkboxes to reflect actual DB state
	 */
	private void resetCheckboxesFromDB(CampaignTreeGridDto item) {
		if ("cluster".equals(item.getLevelAssessed())) {
			boolean isSelected = selectedClusterUuids.contains(item.getUuid());
			updateCheckboxState(item.getUuid(), isSelected);
			item.setSavedData(String.valueOf(isSelected));
			item.setSelected(isSelected);
		}

		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			resetCheckboxesFromDB(child);
		}
	}


	/**
	 * Select item and all its descendants recursively
	 */
	private void selectItemAndAllDescendants(CampaignTreeGridDto item) {
		// Select the item in tree grid
		// Note: Selection mode is NONE, so we just update checkbox states
		updateCheckboxState(item.getUuid(), true);

		// Update DTO's selection state
		if ("cluster".equals(item.getLevelAssessed())) {
			item.setSavedData("true");
			item.setSelected(true);
		}

		// Recursively select all children
		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			selectItemAndAllDescendants(child);
		}
	}

	/**
	 * Deselect item and all its descendants recursively
	 */
	private void deselectItemAndAllDescendants(CampaignTreeGridDto item) {
		// Deselect the item in tree grid
		updateCheckboxState(item.getUuid(), false);

		// Update DTO's selection state
		if ("cluster".equals(item.getLevelAssessed())) {
			item.setSavedData("false");
			item.setSelected(false);
		}

		// Recursively deselect all children
		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			deselectItemAndAllDescendants(child);
		}
	}

	/**
	 * Update checkbox state programmatically
	 */
	private void updateCheckboxState(String uuid, boolean selected) {
		Checkbox checkbox = checkboxMap.get(uuid);
		if (checkbox != null && checkbox.getValue() != selected) {
			checkbox.setValue(selected);
		}
	}

	/**
	 * Find parent of an item in the tree
	 */
	private CampaignTreeGridDto findParentItem(CampaignTreeGridDto item) {

		return parentMap.get(item.getUuid());
	}

	/**
	 * Collect all cluster UUIDs under an item
	 */
	private void collectAllClustersUnderItem(CampaignTreeGridDto item, Set<String> collectedClusters) {
		if ("cluster".equals(item.getLevelAssessed())) {
			if (item.getUuid() != null) {
				collectedClusters.add(item.getUuid());
			}
		} else {
			for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
				collectAllClustersUnderItem(child, collectedClusters);
			}
		}
	}

	private void initializeSelectionsFromDB() {
		checkboxMap.clear();
		for (CampaignTreeGridDto area : treeGrid.getTreeData().getRootItems()) {
			initializeSelectionsRecursive(area);
		}
		updateAllParentSelections(); // Ensure full propagation after init
		recomputeAllTotals();
		refreshSelectedClusterCountLabel();
	}

	private void initializeSelectionsRecursive(CampaignTreeGridDto item) {
		if (item == null)
			return;

		if ("cluster".equals(item.getLevelAssessed())) {
			boolean isSelected = selectedClusterUuids.contains(item.getUuid())
					|| pendingSelectedClusters.contains(item.getUuid());

			updateCheckboxState(item.getUuid(), isSelected);
			item.setSavedData(String.valueOf(isSelected));
			item.setSelected(isSelected);

		} else {
			// Parent levels
			boolean hasSelectedChild = hasAnySelectedChildCluster(item);

			if (hasSelectedChild) {
				updateCheckboxState(item.getUuid(), true);
				item.setSavedData("true");
				item.setSelected(true);
			} else {
				// Check if this parent was explicitly selected at higher level
				boolean isSelected = false;
//                if ("district".equals(item.getLevelAssessed())) {
//                    isSelected = selectedDistrictUuids.contains(item.getUuid());
//                } else if ("region".equals(item.getLevelAssessed())) {
//                    isSelected = selectedRegionUuids.contains(item.getUuid());
//                } else if ("area".equals(item.getLevelAssessed())) {
//                    isSelected = selectedAreaUuids.contains(item.getUuid());
//                }

				updateCheckboxState(item.getUuid(), isSelected);
				item.setSavedData(String.valueOf(isSelected));
				item.setSelected(isSelected);
			}

			// Recurse to children
			for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
				initializeSelectionsRecursive(child);
			}
		}
	}

	private void refreshTreeGrid() {      
		isInitializing = true;
		try {
			// Refresh campaign DTO from DB
			campaignDto = FacadeProvider.getCampaignFacade().getByUuid(campaignDto.getUuid());

			// Update selected UUID sets
			selectedAreaUuids = campaignDto.getAreas().stream().map(AreaReferenceDto::getUuid)
					.collect(Collectors.toSet());
			selectedRegionUuids = campaignDto.getRegion().stream().map(RegionReferenceDto::getUuid)
					.collect(Collectors.toSet());
			selectedDistrictUuids = campaignDto.getDistricts().stream().map(DistrictReferenceDto::getUuid)
					.collect(Collectors.toSet());
			selectedClusterUuids = campaignDto.getCommunity().stream().map(CommunityReferenceDto::getUuid)
					.collect(Collectors.toSet());

			// Clear checkbox map
			checkboxMap.clear();

			// Reload tree data
			treeGrid.setItems(generateTreeGridData(), item -> {
				if ("area".equals(item.getLevelAssessed()))
					return item.getRegionData();
				else if ("region".equals(item.getLevelAssessed()))
					return item.getDistrictData();
				else if ("district".equals(item.getLevelAssessed()))
					return item.getClusterData();
				else
					return Collections.emptyList();
			});

			
//			initializeDeleteList();
			deletelist.clear();
			updateAllDeleteCheckboxStates();
			recomputeAllTotals();
			updateAllParentSelections();

			treeGrid.getDataProvider().refreshAll();
			Notification.show("Data refreshed from database").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		} catch (Exception ex) {
			logger.error("Error refreshing tree grid", ex);
			Notification.show("Error refreshing data: " + ex.getMessage())
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
		} finally {
			isInitializing = false;
		}
		
		refreshSelectedClusterCountLabel();

	}

	private List<CampaignTreeGridDto> getChildrenForSelection(CampaignTreeGridDto item) {
		if ("area".equals(item.getLevelAssessed())) {
			return item.getRegionData();
		} else if ("region".equals(item.getLevelAssessed())) {
			return item.getDistrictData();
		} else if ("district".equals(item.getLevelAssessed())) {
			return item.getClusterData();
		}
		return Collections.emptyList();
	}

	private void openEditDialog(CampaignTreeGridDto ee) {

		System.out.println(ee.getUuid() + "uuid");
		System.out.println(ee.getName() + "nameeeee");

		isSingleSelectClickItemLock = true;
		if (campaignDto != null && ee.getLevelAssessed().equals("cluster")) {

			if (ee.getPopulationData() != null) {

				System.out.println("Age Group from item click " + ee.getAgeGroup());
				Integer popDataAge0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_0_4");

				Integer popDataAge5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_5_10");

				Integer popDataAge4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_23M");

				Integer popDataAge4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_59M");

				String districtModality_0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByUuidAndCampaignAndAgeGroup(ee.getUuid(), campaignDto.getUuid(),
								"AGE_0_4");
				String districtStatus_0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictStatusByCampaign(ee.getUuid(), campaignDto.getUuid(), "AGE_0_4");

				createDialogBasics(ee.getUuid(), ee.getPopulationData(), ee.getName(), "AGE_0_4", campaignDto, ee,
						popDataAge0_4, popDataAge5_10, popDataAge4_23M, popDataAge4_59M, districtModality_0_4, districtStatus_0_4);
			}

			else if (ee.getPopulationData5_10() != null) {
				System.out.println("Age Group from item click " + ee.getAgeGroup());

				Integer popDataAge5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_5_10");

				Integer popDataAge0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_0_4");

				Integer popDataAge4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_23M");

				Integer popDataAge4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_59M");

				String districtModality_5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByUuidAndCampaignAndAgeGroup(ee.getUuid(), campaignDto.getUuid(),
								"AGE_5_10");

				String districtStatus_5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictStatusByCampaign(ee.getUuid(), campaignDto.getUuid(), "AGE_5_10");

				
				createDialogBasics(ee.getUuid(), ee.getPopulationData(), ee.getName(), "AGE_5_10", campaignDto, ee,
						popDataAge0_4, popDataAge5_10, popDataAge4_23M, popDataAge4_59M, districtModality_5_10, districtStatus_5_10);

			} else if (ee.getPopulationData4_23M() != null) {
				System.out.println("Age Group from item click " + ee.getAgeGroup());

				Integer popDataAge5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_5_10");

				Integer popDataAge0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_0_4");

				Integer popDataAge4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_23M");
				
				Integer popDataAge4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_59M");

				
				String districtModality_4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByUuidAndCampaignAndAgeGroup(ee.getUuid(), campaignDto.getUuid(),
								"AGE_4_23M");

				String districtStatus_4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictStatusByCampaign(ee.getUuid(), campaignDto.getUuid(), "AGE_4_23M");

				createDialogBasics(ee.getUuid(), ee.getPopulationData(), ee.getName(), "AGE_4_23M", campaignDto, ee,
						popDataAge0_4, popDataAge5_10, popDataAge4_23M, popDataAge4_59M, districtModality_4_23M, districtStatus_4_23M);

			}else if (ee.getPopulationData4_59M() != null) {
				System.out.println("Age Group from item click " + ee.getAgeGroup());

				Integer popDataAge5_10 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_5_10");

				Integer popDataAge0_4 = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_0_4");

				Integer popDataAge4_23M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_23M");
				
				Integer popDataAge4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictPopulationByUuidAndAgeGroup(ee.getUuid(), campaignDto.getUuid(), "AGE_4_59M");

				
				String districtModality_4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByUuidAndCampaignAndAgeGroup(ee.getUuid(), campaignDto.getUuid(),
								"AGE_4_59M");

				String districtStatus_4_59M = FacadeProvider.getPopulationDataFacade()
						.getDistrictStatusByCampaign(ee.getUuid(), campaignDto.getUuid(), "AGE_4_59M");

				createDialogBasics(ee.getUuid(), ee.getPopulationData(), ee.getName(), "AGE_4_59M", campaignDto, ee,
						popDataAge0_4, popDataAge5_10, popDataAge4_23M, popDataAge4_59M, districtModality_4_59M, districtStatus_4_59M);

			} else {

			}

		}

	}

	private Component configurePopulationPopEdit(CampaignDto campaignDto_) {
		VerticalLayout formx = populationEditorForm(campaignDto_);
		formx.getStyle().remove("width");
		HorizontalLayout content = new HorizontalLayout(treeGrid, formx);
		content.setFlexGrow(4, treeGrid);
		content.setFlexGrow(0, formx);
		content.addClassNames("content");
		return content;
	}

	
	/**
	 * Clears all delete selections and resets checkbox states
	 */
	private void clearDeleteSelection() {
	    // Clear the list
	    deletelist.clear();
	    
	    // Reset all delete checkboxes
	    for (Map.Entry<String, Checkbox> entry : deleteCheckboxMap.entrySet()) {
	        Checkbox cb = entry.getValue();
	        if (cb != null) {
	            cb.setValue(false);
	        }
	    }
	    
	    // Refresh the grid
	    treeGrid.getDataProvider().refreshAll();
	}
	
	private VerticalLayout populationEditorForm(CampaignDto campaignDto_) {
		VerticalLayout vert = new VerticalLayout();

		formx = new FormLayout();

		Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
		plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		plusButton.setTooltipText(I18nProperties.getCaption(Captions.addNewPopulationTarget));

		Button deleteButton = new Button(new Icon(VaadinIcon.DEL_A));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		deleteButton.getStyle().set("background-color", "red!important");
		deleteButton.setTooltipText(I18nProperties.getCaption("Delete Population Data"));

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionAdd), new Icon(VaadinIcon.CHECK));

		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel),
				new Icon(VaadinIcon.REFRESH));
		cancelButton.getElement().getStyle().set("background", "red");

		regionFilter = new ComboBox<AreaReferenceDto>();
		regionFilter.setLabel(I18nProperties.getCaption(Captions.area));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.selectRegions));
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		regionFilter.isRequired();
		regionFilter.setRequiredIndicatorVisible(true);

		provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		provinceFilter.setPlaceholder(I18nProperties.getCaption(Captions.selectProvinces));
		provinceFilter.setClearButtonVisible(true);
		provinceFilter.getStyle().set("width", "145px !important");
		provinceFilter.isRequired();
		provinceFilter.setRequiredIndicatorVisible(true);

		districtFilter = new ComboBox<>(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.selectDistricts));
		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		districtFilter.getStyle().set("width", "145px !important");
		districtFilter.isRequired();
		districtFilter.setRequiredIndicatorVisible(true);

		clusterFilter = new ComboBox<>(I18nProperties.getCaption(Captions.community));
		clusterFilter.setPlaceholder(I18nProperties.getCaption(Captions.selectClusters));
		clusterFilter.setItems(new ArrayList<>());
//		FacadeProvider.getCommunityFacade().getAllActiveByDistrict()
		clusterFilter.getStyle().set("width", "145px !important");
		clusterFilter.setVisible(false);
		clusterFilter.isRequired();
		clusterFilter.setRequiredIndicatorVisible(true);

		popData0_4 = new IntegerField("Target 0-59M");
		popDataAge5_10 = new IntegerField("Target 60-120M");
		popDataAge4_23M = new IntegerField("Target 4-23M");
		popDataAge4_59M = new IntegerField("Target 4-59M");

		
		popData0_4.setMin(0);
		popDataAge5_10.setMin(0);
		popDataAge4_23M.setMin(0);
		popDataAge4_59M.setMin(0);



		popData0_4.setRequiredIndicatorVisible(true);
		popDataAge5_10.setRequiredIndicatorVisible(true);
		popDataAge4_23M.setRequiredIndicatorVisible(true);
		popDataAge4_59M.setRequiredIndicatorVisible(true);


		popData0_4.setErrorMessage("Negative Values not Allowed");
		popDataAge5_10.setErrorMessage("Negative Values not Allowed");
		popDataAge4_23M.setErrorMessage("Negative Values not Allowed");
		popDataAge4_59M.setErrorMessage("Negative Values not Allowed");


		districtModality = new ComboBox<String>("Modality");

		districtModality.setItems("H2H", "M2M", "S2S", "HF2HF", "Mixed", "M2M S2S", "General");

		districtStatus = new ComboBox<String>("Status");
		districtStatus.setPlaceholder(I18nProperties.getCaption(Captions.selectStatus));
		districtStatus.setItems("Additional", "Additional & Cold", "Cold", "Full Cluster", "HRMP only", "Partial",
				"Not Targted", "On Hold");

		popData0_4.setVisible(false);
		popDataAge5_10.setVisible(false);
		popDataAge4_23M.setVisible(false);
		popDataAge4_59M.setVisible(false);
		
		districtModality.setVisible(false);
		districtStatus.setVisible(false);

		popData0_4.setValue(null);
		popDataAge5_10.setValue(null);
		popDataAge4_23M.setValue(null);
		popDataAge4_59M.setValue(null);
		
		districtModality.setValue(null);
		districtStatus.setValue(null);

		regionFilter.addValueChangeListener(e -> {
			if (regionFilter.getValue() != null) {
				AreaReferenceDto area = e.getValue();

				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));

			} else {

			}

		});

		provinceFilter.addValueChangeListener(e -> {
			if (provinceFilter.getValue() != null) {

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid()));
				} else {
					districtFilter
							.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid()));
				}
				RegionReferenceDto province = e.getValue();

			} else {

			}

		});

		districtFilter.addValueChangeListener(e -> {
			if (districtFilter.getValue() != null) {

				List<CommunityReferenceDto> allClusters = FacadeProvider.getCommunityFacade()
						.getAllActiveByDistrict(e.getValue().getUuid());
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					clusterFilter.setItemLabelGenerator(itm -> {
						CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
						return dcfv.getNumber() + " | " + dcfv.getPs_af();
					});
					allClusters.sort(Comparator.comparing(CommunityReferenceDto::getNumber));
					clusterFilter.setItems(allClusters);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					clusterFilter.setItemLabelGenerator(itm -> {
						CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
						return dcfv.getNumber() + " | " + dcfv.getFa_af();
					});
					allClusters.sort(Comparator.comparing(CommunityReferenceDto::getNumber));
					clusterFilter.setItems(allClusters);
				} else {

					clusterFilter.setItemLabelGenerator(itm -> {
						CommunityReferenceDto dcfv = (CommunityReferenceDto) itm;
						return dcfv.getNumber() + " | " + dcfv.getCaption();
					});
					allClusters.sort(Comparator.comparing(CommunityReferenceDto::getNumber));
					clusterFilter.setItems(allClusters);
}

				clusterFilter.setVisible(true);

				DistrictReferenceDto district = e.getValue();

			} else {

			}
		});

		clusterFilter.addValueChangeListener(e -> {
			if (clusterFilter.getValue() != null) {
				if(FacadeProvider.getPopulationDataFacade().getPopulationDataByClusterandCampaign(campaignDto.getUuid(), e.getValue().getUuid()).size() < 1) {
					CommunityReferenceDto cluster = e.getValue();
					popData0_4.setVisible(true);
					popDataAge5_10.setVisible(true);
					popDataAge4_23M.setVisible(true);
					popDataAge4_59M.setVisible(true);

					districtModality.setVisible(true);
					
					districtStatus.setVisible(true);

					for (PopulationDataDto xx : FacadeProvider.getPopulationDataFacade()
							.getClusterPopulationByTypeUsingUUIDs(clusterFilter.getValue().getUuid(), campaignDto.getUuid(),
									AgeGroup.AGE_0_4)) {
						popData0_4.setValue(xx.getPopulation());
						System.out.println(xx.getPopulation() + "55555555555555555555555555555555555555555555");
					}

					for (PopulationDataDto xx : FacadeProvider.getPopulationDataFacade()
							.getClusterPopulationByTypeUsingUUIDs(clusterFilter.getValue().getUuid(), campaignDto.getUuid(),
									AgeGroup.AGE_5_10)) {
						popDataAge5_10.setValue(xx.getPopulation());
						System.out.println(xx.getPopulation() + "666666666666666666666666666666666666666666666");
					}

					for (PopulationDataDto xx : FacadeProvider.getPopulationDataFacade()
							.getClusterPopulationByTypeUsingUUIDs(clusterFilter.getValue().getUuid(), campaignDto.getUuid(),
									AgeGroup.AGE_4_23M)) {
						popDataAge4_23M.setValue(xx.getPopulation());
						System.out.println(xx.getPopulation() + "666666666666666666666666666666666666666666666");
					}	
					
					for (PopulationDataDto xx : FacadeProvider.getPopulationDataFacade()
							.getClusterPopulationByTypeUsingUUIDs(clusterFilter.getValue().getUuid(), campaignDto.getUuid(),
									AgeGroup.AGE_4_59M)) {
						popDataAge4_59M.setValue(xx.getPopulation());
						System.out.println(xx.getPopulation() + "666666666666666666666666666666666666666666666");
					}	
				}else {
					
					popData0_4.setVisible(false);
					popDataAge5_10.setVisible(false);
					popDataAge4_23M.setVisible(false);
					popDataAge4_59M.setVisible(false);

					
					districtModality.setVisible(false);
					districtStatus.setVisible(false);
					
					clusterFilter.clear();
					Notification.show("Population Target Exist for this Cluster. Select the cluster on the grid to edit" , 7000, Position.MIDDLE);
				}
 

			} else {

			}
		});

		HorizontalLayout buttonLay = new HorizontalLayout(plusButton, deleteButton);

		buttonAfterLay = new HorizontalLayout(saveButton, cancelButton);
		buttonAfterLay.getStyle().set("flex-wrap", "wrap");
		buttonAfterLay.setJustifyContentMode(JustifyContentMode.END);
		buttonLay.setSpacing(true);

		cancelButton.addClickListener(ees -> {
			CampaignFormMetaReferenceDto newcampform = new CampaignFormMetaReferenceDto();

			formx.setVisible(false);
			buttonAfterLay.setVisible(false);
			saveButton.setText(I18nProperties.getCaption(Captions.actionSave));

		});

		saveButton.addClickListener(e -> {
			validatePopulationEditForm();

		});

		formx.add(regionFilter, provinceFilter, districtFilter, clusterFilter, popData0_4, popDataAge5_10,
				popDataAge4_23M, popDataAge4_59M, districtModality, districtStatus);
		formx.setColspan(regionFilter, 1);
		formx.setColspan(provinceFilter, 1);
		formx.setColspan(districtFilter, 1);
		formx.setColspan(clusterFilter, 1);
		formx.setColspan(popData0_4, 1);
		formx.setColspan(popDataAge5_10, 1);
		formx.setColspan(popDataAge4_23M, 1);
		formx.setColspan(popDataAge4_59M, 1);
		formx.setColspan(districtModality, 1);
		formx.setColspan(districtStatus, 2);

		formx.setVisible(false);
		buttonAfterLay.setVisible(false);

		plusButton.addClickListener(ce ->

		{

			formx.setVisible(true);

			buttonAfterLay.setVisible(true);
			saveButton.setText(I18nProperties.getCaption(Captions.actionAdd));
		});
		
		deleteButton.addClickListener(delete -> {
		    if (!deletelist.isEmpty()) {
		        // Get distinct clusters (deletelist should only contain clusters)
		        long distinctCount = deletelist.stream()
		            .map(CampaignTreeGridDto::getId)
		            .distinct()
		            .count();
		        
		        ConfirmDialog confirmationDialog = new ConfirmDialog();
		        confirmationDialog.setHeader("Delete Population Data");
		        confirmationDialog.setText("Are you sure you want to delete the population data for " + 
		            distinctCount + " selected clusters?");
		        confirmationDialog.setCancelable(true);
		        confirmationDialog.setRejectable(false);
		        confirmationDialog.setConfirmText("Delete");
		        confirmationDialog.setCancelText("Cancel");
		        confirmationDialog.setCancelButtonTheme("error");

		        confirmationDialog.addCancelListener(e -> {
		            treeGrid.getDataProvider().refreshAll();
		            clearDeleteSelection();
		            confirmationDialog.close();
		        });

		        confirmationDialog.addConfirmListener(event -> {
		            try {
		                List<Long> clusterIDs = new ArrayList<>();
		                for (CampaignTreeGridDto treeData : deletelist) {
		                    // Only delete clusters
		                    if ("cluster".equals(treeData.getLevelAssessed())) {
		                        clusterIDs.add(treeData.getId());
		                    }
		                }

		                if (!clusterIDs.isEmpty()) {
		                    FacadeProvider.getPopulationDataFacade().deletePopulationDataByClusters(
		                        clusterIDs, 
		                        campaignDto != null ? campaignDto.getUuid() : ""
		                    );
		                    
		                    Notification.show("Population data deleted successfully. Please re-open the Campaign Basics form to receive updated Population Data Table.",
		                        5000, Notification.Position.MIDDLE);
		                } else {
		                    Notification.show("No valid clusters selected for deletion.", 3000, Notification.Position.MIDDLE);
		                }
		            } catch (Exception e) {
		                Notification.show("Error deleting population data: " + e.getMessage(), 10000,
		                    Notification.Position.MIDDLE);
		            } finally {
		                clearDeleteSelection();
		                treeGrid.getDataProvider().refreshAll();
		                confirmationDialog.close();
		            }
		        });

		        confirmationDialog.open();
		    } else {
		    	
		    	showErrorNotification("Please select a geographic unit(s) on the District or Cluster Level to delete.");
		    }
		});


		vert.add(buttonLay, formx, buttonAfterLay);

		return vert;
	}
	
	private void showErrorNotification(String message) {
	    Notification notification = new Notification(message, 5000);
	    notification.setPosition(Notification.Position.MIDDLE);
	    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
	    notification.open();
	}

	private void validatePopulationEditForm() {
		if (regionFilter.isEmpty()) {
			regionFilter.isInvalid();
			regionFilter.setInvalid(true);
			regionFilter.setErrorMessage("Please select a Region.");
			Notification.show("Please select an Region.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (provinceFilter.isEmpty()) {
			provinceFilter.isInvalid();
			provinceFilter.setInvalid(true);
			provinceFilter.setErrorMessage("Please select a Province.");
			Notification.show("Please select a Province.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (districtFilter.isEmpty()) {
			districtFilter.isInvalid();
			districtFilter.setInvalid(true);
			districtFilter.setErrorMessage("Please select a District.");

			Notification.show("Please select a District.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (clusterFilter.isEmpty()) {
			clusterFilter.isInvalid();
			clusterFilter.setInvalid(true);
			clusterFilter.setErrorMessage("Please select a Cluster.");
			Notification.show("Please select a Cluster.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (districtModality.isEmpty()) {
			districtModality.isInvalid();
			districtModality.setInvalid(true);
			districtModality.setErrorMessage("Please select a Modality.");
			Notification.show("Please select a Modality.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (districtStatus.isEmpty()) {
			districtStatus.isInvalid();
			districtStatus.setInvalid(true);
			districtStatus.setErrorMessage("Please select a Status.");
			Notification.show("Please select a Status.").addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}
		if (popData0_4.isEmpty() || popDataAge5_10.isEmpty() || popDataAge4_23M.isEmpty() || popDataAge4_59M.isEmpty()) {
			popData0_4.isInvalid();
			popData0_4.setInvalid(true);
			popData0_4.setErrorMessage("Please provide numeric values for Population Target");

			popDataAge5_10.isInvalid();
			popDataAge5_10.setInvalid(true);
			popDataAge5_10.setErrorMessage("Please provide numeric values for Population Target");

			popDataAge4_23M.isInvalid();
			popDataAge4_23M.setInvalid(true);
			popDataAge4_23M.setErrorMessage("Please provide numeric values for Population Target");
			
			popDataAge4_59M.isInvalid();
			popDataAge4_59M.setInvalid(true);
			popDataAge4_59M.setErrorMessage("Please provide numeric values for Population Target");

			Notification.show("Please provide numeric values for all Target population fields.")
					.addThemeVariants(NotificationVariant.LUMO_ERROR);

			return;
		}
		if (popData0_4.isInvalid() || popDataAge5_10.isInvalid() || popDataAge4_23M.isInvalid() || popDataAge4_59M.isInvalid()
				|| popData0_4.getValue() < 0 || popDataAge5_10.getValue() < 0 || popDataAge4_23M.getValue() < 0 || popDataAge4_59M.getValue() < 0) {
			popData0_4.isInvalid();
			popData0_4.setInvalid(true);
			popData0_4.setErrorMessage("Negative Values are not allowed");

			popDataAge5_10.isInvalid();
			popDataAge5_10.setInvalid(true);
			popDataAge5_10.setErrorMessage("Negative Values are not allowed");

			popDataAge4_23M.isInvalid();
			popDataAge4_23M.setInvalid(true);
			popDataAge4_23M.setErrorMessage("Please provide numeric values for Population Target");
			
			
			popDataAge4_59M.isInvalid();
			popDataAge4_59M.setInvalid(true);
			popDataAge4_59M.setErrorMessage("Please provide numeric values for Population Target");
			

			Notification.show("Negative Values are not allowed for target populations.")
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
			return;
		}

		if (clusterFilter.getValue() != null) {
			Date collectionDate = new Date();

			PopulationDataDto newPopulationData_10 = PopulationDataDto.build(collectionDate);
			PopulationDataDto newPopulationData_0_4 = PopulationDataDto.build(collectionDate);
			PopulationDataDto newPopulationData_423M = PopulationDataDto.build(collectionDate);
			PopulationDataDto newPopulationData_459M = PopulationDataDto.build(collectionDate);


			List<PopulationDataDto> itirationList = new ArrayList<PopulationDataDto>();
			itirationList.add(newPopulationData_0_4);
			itirationList.add(newPopulationData_10);
			itirationList.add(newPopulationData_423M);
			itirationList.add(newPopulationData_459M);


			System.out.println("Age Group rom ope  is age 0_4");
			if (popDataAge4_59M.getValue() != null) {
				newPopulationData_459M
						.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
				newPopulationData_459M.setRegion(
						FacadeProvider.getRegionFacade().getRegionReferenceByUuid(provinceFilter.getValue().getUuid()));
				newPopulationData_459M.setDistrict(FacadeProvider.getDistrictFacade()
						.getDistrictReferenceByUuid(districtFilter.getValue().getUuid()));
				newPopulationData_459M.setCommunity(FacadeProvider.getCommunityFacade()
						.getCommunityReferenceByUuid(clusterFilter.getValue().getUuid()));
				newPopulationData_459M.setModality(districtModality.getValue());
				newPopulationData_459M.setDistrictStatus(districtStatus.getValue());
				newPopulationData_459M.setAgeGroup(AgeGroup.AGE_4_59M);
				newPopulationData_459M.setPopulation(popDataAge4_59M.getValue());
				newPopulationData_459M.setSelected(true);

				populationDataList.add(newPopulationData_459M);

			}
			
			if (popDataAge4_23M.getValue() != null) {
				newPopulationData_423M
						.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
				newPopulationData_423M.setRegion(
						FacadeProvider.getRegionFacade().getRegionReferenceByUuid(provinceFilter.getValue().getUuid()));
				newPopulationData_423M.setDistrict(FacadeProvider.getDistrictFacade()
						.getDistrictReferenceByUuid(districtFilter.getValue().getUuid()));
				newPopulationData_423M.setCommunity(FacadeProvider.getCommunityFacade()
						.getCommunityReferenceByUuid(clusterFilter.getValue().getUuid()));
				newPopulationData_423M.setModality(districtModality.getValue());
				newPopulationData_423M.setDistrictStatus(districtStatus.getValue());
				newPopulationData_423M.setAgeGroup(AgeGroup.AGE_4_23M);
				newPopulationData_423M.setPopulation(popDataAge4_23M.getValue());
				newPopulationData_423M.setSelected(true);

				populationDataList.add(newPopulationData_423M);

			}

			if (popData0_4.getValue() != null) {
				newPopulationData_0_4
						.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
				newPopulationData_0_4.setRegion(
						FacadeProvider.getRegionFacade().getRegionReferenceByUuid(provinceFilter.getValue().getUuid()));
				newPopulationData_0_4.setDistrict(FacadeProvider.getDistrictFacade()
						.getDistrictReferenceByUuid(districtFilter.getValue().getUuid()));
				newPopulationData_0_4.setCommunity(FacadeProvider.getCommunityFacade()
						.getCommunityReferenceByUuid(clusterFilter.getValue().getUuid()));
				newPopulationData_0_4.setModality(districtModality.getValue());
				newPopulationData_0_4.setDistrictStatus(districtStatus.getValue());
				newPopulationData_0_4.setAgeGroup(AgeGroup.AGE_0_4);
				newPopulationData_0_4.setPopulation(popData0_4.getValue());
				newPopulationData_0_4.setSelected(true);

				populationDataList.add(newPopulationData_0_4);

			}

			if (popDataAge5_10.getValue() != null) {
				newPopulationData_10
						.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
				newPopulationData_10.setRegion(
						FacadeProvider.getRegionFacade().getRegionReferenceByUuid(provinceFilter.getValue().getUuid()));
				newPopulationData_10.setDistrict(FacadeProvider.getDistrictFacade()
						.getDistrictReferenceByUuid(districtFilter.getValue().getUuid()));
				newPopulationData_10.setCommunity(FacadeProvider.getCommunityFacade()
						.getCommunityReferenceByUuid(clusterFilter.getValue().getUuid()));
				newPopulationData_10.setModality(districtModality.getValue());
				newPopulationData_10.setDistrictStatus(districtStatus.getValue());
				newPopulationData_10.setAgeGroup(AgeGroup.AGE_5_10);
				newPopulationData_10.setPopulation(popDataAge5_10.getValue());
				newPopulationData_10.setSelected(true);

				populationDataList.add(newPopulationData_10);

				ConfirmDialog confirmationDialog = new ConfirmDialog();

				confirmationDialog.setCancelable(true);
				confirmationDialog.setRejectable(false);
				confirmationDialog.addCancelListener(confirmationDialogx -> confirmationDialog.close());
				confirmationDialog.setCancelButtonTheme(ValoTheme.NOTIFICATION_ERROR);
				confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionOkay));
				confirmationDialog.setCancelText(I18nProperties.getCaption("Cancel"));

				confirmationDialog.addConfirmListener(confirmationDialogx -> {

					System.out.println(popDataAge5_10.getValue() + "GGGGGGGGGGGG IN THE TRY " + popData0_4.getValue());

					try {
						
						if (popDataAge4_59M.getValue() != null) {
							try {
								FacadeProvider.getPopulationDataFacade().savePopulationData(populationDataList);
							} catch (Exception ex) {
								System.out.println(ex + "Exception");
							} finally {
								populationDataList.remove(newPopulationData_459M);

							}

						}

						if (popDataAge4_23M.getValue() != null) {
							try {
								FacadeProvider.getPopulationDataFacade().savePopulationData(populationDataList);
							} catch (Exception ex) {
								System.out.println(ex + "Exception");
							} finally {
								populationDataList.remove(newPopulationData_423M);

							}

						}

						if (popData0_4.getValue() != null) {
							try {
								FacadeProvider.getPopulationDataFacade().savePopulationData(populationDataList);
							} catch (Exception ex) {
								System.out.println(ex + "Exception");
							} finally {
								populationDataList.remove(newPopulationData_0_4);

							}

						}
						if (popDataAge5_10.getValue() != null) {
							try {
								FacadeProvider.getPopulationDataFacade().savePopulationData(populationDataList);
							} catch (Exception ex) {
								System.out.println(ex + "Exception");
							} finally {
								populationDataList.remove(newPopulationData_10);

							}
						}

					} catch (Exception exception) {

						System.out.println("excetion Caught while saving population data");

					} finally {

						confirmationDialog.close();

						updateFooterTotals();
						treeGrid.getDataProvider().refreshAll();// refreshItem(campaignTreeGridDto);
																// add , true
																// to
																// regresh
																// children
						Notification.show(I18nProperties.getString(Strings.dataSavedSuccessfully));

						regionFilter.clear();
						provinceFilter.clear();
						districtFilter.clear();
						clusterFilter.clear();
						popData0_4.clear();
						popDataAge5_10.clear();
						popDataAge4_23M.clear();
						popDataAge4_59M.clear();

						districtModality.clear();
						
						districtStatus.clear();
						
						regionFilter.setInvalid(false);
						provinceFilter.setInvalid(false);
						districtFilter.setInvalid(false);
						clusterFilter.setInvalid(false);
						popData0_4.setInvalid(false);
						popDataAge5_10.setInvalid(false);
						popDataAge4_23M.setInvalid(false);
						popDataAge4_59M.setInvalid(false);

						
						

						formx.setVisible(false);
						buttonAfterLay.setVisible(false);
					}

				});

				confirmationDialog.setText("Are you sure you want to add the population data for the District "
						+ districtFilter.getValue() + "  ?. \n"
						+ " Please note that the added population data would only be available after reloading Campaign Form");
				confirmationDialog.setHeader("Add Population Data");
				confirmationDialog.open();

			} else {
				Notification.show("Please Select a District");
			}
		}
	}

	private void createDialogBasics(String Uuid, Long selectedPopData, String name_, String ageGroup,
			CampaignDto campaignDto_, CampaignTreeGridDto campaignTreeGridDto, Integer populationByAgeGroup,
			Integer populationByAgeGroup5_10, Integer populationByAgeGroup4_23M, Integer populationByAgeGroup4_59M, String districtModalityByAgeGroup,
			String districtStatusByAgeGroup) {
		Dialog dialog = new Dialog();

		dialog.removeAll();

		dialog.setHeaderTitle(I18nProperties.getCaption(Captions.editing) + " " + name_);

		Button saveButton = createSaveButton();
		Button deleteButton = createDeleteButton();
		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), e -> dialog.close());
		dialog.getFooter().add(cancelButton);
		dialog.getFooter().add(deleteButton);
		dialog.getFooter().add(saveButton);

		VerticalLayout dialogLayout = createDialogLayoutByAge(name_, ageGroup, selectedPopData, saveButton, Uuid,
				campaignDto_, dialog, campaignTreeGridDto, populationByAgeGroup, populationByAgeGroup5_10,
				populationByAgeGroup4_23M, populationByAgeGroup4_59M, districtModalityByAgeGroup, districtStatusByAgeGroup);

		dialog.add(dialogLayout);

		add(dialog);

		dialog.open();

		isSingleSelectClickItemLock = false;
	}

	private static VerticalLayout createDialogLayoutByAge(String name_, String ageGroup, Long selectedPopData,
			Button saveButton, String Uuid, CampaignDto campaignDto_, Dialog dialog,
			CampaignTreeGridDto campaignTreeGridDto, Integer populationByAgeGroup, Integer populationByAgeGroup5_10,
			Integer populationByAgeGroup4_23M, Integer populationByAgeGroup4_59M, String districtModalityByAgeGroup, String districtStatusByAgeGroup) {

		TextField district = new TextField(I18nProperties.getCaption(Captions.community));
		district.setValue(name_);
		district.setReadOnly(true);

		IntegerField popData = new IntegerField(I18nProperties.getCaption(Captions.District_target) + " " + "0-59M");

		IntegerField popData5_10 = new IntegerField(
				I18nProperties.getCaption(Captions.District_target) + " " + "60-120M");

		IntegerField popData4_23M = new IntegerField(
				I18nProperties.getCaption(Captions.District_target) + " " + "4-23M");
		
		IntegerField popData4_59M = new IntegerField(
				I18nProperties.getCaption(Captions.District_target) + " " + "4-59M");

		ComboBox<Modality> districtModalityCombo = new ComboBox<>("Modality");
		districtModalityCombo.setItems(Modality.values());
		districtModalityCombo.setItemLabelGenerator(Modality::getDisplayName);

		ComboBox<Status> districtStatusCombo = new ComboBox<>("Campaign Status");
		districtStatusCombo.setItems(Status.values());
		districtStatusCombo.setItemLabelGenerator(Status::getDisplayName);

		districtModalityCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
//				districtModalityCombo.setValue(e.getValue());
				districtModalityCombo.setValue(e.getValue());
			}
		});

		districtStatusCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				districtStatusCombo.setValue(e.getValue());
			}
		});

		popData.setWidthFull();
		popData5_10.setWidthFull();
		popData4_23M.setWidthFull();
		popData4_59M.setWidthFull();

		popData4_59M.setMin(0);
		popData4_23M.setMin(0);
		popData5_10.setMin(0);
		popData.setMin(0);

		popData4_59M.setErrorMessage("Negative Values not Allowed");
		popData4_23M.setErrorMessage("Negative Values not Allowed");
		popData5_10.setErrorMessage("Negative Values not Allowed");
		popData.setErrorMessage("Negative Values not Allowed");

		districtStatusCombo.setWidthFull();
		districtModalityCombo.setWidthFull();

		if (populationByAgeGroup != null) {
			popData.setValue(populationByAgeGroup);
		} else {
			popData.setValue(null);
		}

		if (populationByAgeGroup5_10 != null) {
			popData5_10.setValue(populationByAgeGroup5_10);
		} else {
			popData5_10.setValue(null);
		}

		if (populationByAgeGroup4_23M != null) {
			popData4_23M.setValue(populationByAgeGroup4_23M);
		} else {
			popData4_23M.setValue(null);
		}
		
		if (populationByAgeGroup4_59M != null) {
			popData4_59M.setValue(populationByAgeGroup4_59M);
		} else {
			popData4_59M.setValue(null);
		}

		if (districtModalityByAgeGroup != null && !districtModalityByAgeGroup.trim().isEmpty()) {
			districtModalityCombo.setValue(Modality.fromValue(districtModalityByAgeGroup));
		} else {
			districtModalityCombo.clear();
		}

		if (districtStatusByAgeGroup != null && !districtStatusByAgeGroup.trim().isEmpty()) {
			districtStatusCombo.setValue(Status.fromValue(districtStatusByAgeGroup));
		} else {
			districtStatusCombo.clear();
		}

		saveButton.addClickListener(e -> {
			if (popData.isInvalid() || popData5_10.isInvalid() || popData4_23M.isInvalid() || popData4_59M.isInvalid() ) { 
				Notification.show("Negative Values are not allowed").addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}
			List<PopulationDataDto> popDataDto;
			List<PopulationDataDto> popDataDto5_10;
			List<PopulationDataDto> popDataDto4_23M;
			List<PopulationDataDto> popDataDto4_59M;
			List<PopulationDataDto> district_Modality;
			List<PopulationDataDto> district_Status;

			List<PopulationDataDto> popDataDtotoList = new ArrayList<>();

			System.out.println(ageGroup + " age group from save click listener ");
			if (ageGroup != null) {

				popDataDto = FacadeProvider.getPopulationDataFacade().getClusterPopulationByTypeUsingUUIDs(Uuid,
						campaignDto_.getUuid(), AgeGroup.AGE_0_4);

				popDataDto5_10 = FacadeProvider.getPopulationDataFacade().getClusterPopulationByTypeUsingUUIDs(Uuid,
						campaignDto_.getUuid(), AgeGroup.AGE_5_10);

				popDataDto4_23M = FacadeProvider.getPopulationDataFacade().getClusterPopulationByTypeUsingUUIDs(Uuid,
						campaignDto_.getUuid(), AgeGroup.AGE_4_23M);

				popDataDto4_59M = FacadeProvider.getPopulationDataFacade().getClusterPopulationByTypeUsingUUIDs(Uuid,
						campaignDto_.getUuid(), AgeGroup.AGE_4_59M);
				
				district_Modality = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByclusterUUIDsandCampaignUUIdAndAgeGroup(Uuid, campaignDto_.getUuid(),
								ageGroup.equals("Age_0_4") ? AgeGroup.AGE_0_4
										: ageGroup.equals("AGE_5_10") ? AgeGroup.AGE_5_10
												: ageGroup.equals("AGE_4_23M") ? AgeGroup.AGE_4_23M 
														: ageGroup.equals("AGE_4_59M") ? AgeGroup.AGE_4_59M : AgeGroup.AGE_0_4);

				district_Status = FacadeProvider.getPopulationDataFacade()
						.getDistrictModalityByclusterUUIDsandCampaignUUIdAndAgeGroup(Uuid, campaignDto_.getUuid(),
								ageGroup.equals("Age_0_4") ? AgeGroup.AGE_0_4
										: ageGroup.equals("AGE_5_10") ? AgeGroup.AGE_5_10
												: ageGroup.equals("AGE_4_23M") ? AgeGroup.AGE_4_23M 
														: ageGroup.equals("AGE_4_59M") ? AgeGroup.AGE_4_59M : AgeGroup.AGE_0_4);

				if (popData.getValue() != null
						&& (districtModalityCombo.getValue() != null
								|| !districtModalityCombo.getValue().toString().isEmpty())
						&& (districtStatusCombo.getValue() != null
								|| !districtStatusCombo.getValue().toString().isEmpty())) {

					popDataDto.get(0).setPopulation(popData.getValue());

					popDataDto.get(0).setModality(districtModalityCombo.getValue().getDisplayName());
					popDataDto.get(0).setDistrictStatus(districtStatusCombo.getValue().getDisplayName());

					popDataDtotoList.add(popDataDto.get(0));

//					System.out.println( popData5_10 + "value from popu;ation data for GE 5_10 " +  populationByAgeGroup5_10);
					if (populationByAgeGroup5_10 != null) {

						popDataDto5_10.get(0).setPopulation(popData5_10.getValue());

						popDataDto5_10.get(0).setModality(districtModalityCombo.getValue().toString());

						popDataDto5_10.get(0).setDistrictStatus(districtStatusCombo.getValue().toString());

						popDataDtotoList.add(popDataDto5_10.get(0));

					}

					if (populationByAgeGroup4_23M != null) {

						popDataDto4_23M.get(0).setPopulation(popData4_23M.getValue());

						popDataDto4_23M.get(0).setModality(districtModalityCombo.getValue().toString());

						popDataDto4_23M.get(0).setDistrictStatus(districtStatusCombo.getValue().toString());

						popDataDtotoList.add(popDataDto4_23M.get(0));

					}
					
					if (populationByAgeGroup4_59M != null) {

						popDataDto4_59M.get(0).setPopulation(popData4_59M.getValue());

						popDataDto4_59M.get(0).setModality(districtModalityCombo.getValue().toString());

						popDataDto4_59M.get(0).setDistrictStatus(districtStatusCombo.getValue().toString());

						popDataDtotoList.add(popDataDto4_59M.get(0));

					}

					ConfirmDialog confirmationDialog = new ConfirmDialog();

					confirmationDialog.setCancelable(true);
					confirmationDialog.setRejectable(false);
					confirmationDialog.addCancelListener(confirmationDialogx -> confirmationDialog.close());
					confirmationDialog.setConfirmText(I18nProperties.getCaption(Captions.actionYes));
					confirmationDialog.setCancelText(I18nProperties.getCaption("Cancel Update"));

					confirmationDialog.addConfirmListener(confirmationDialogx -> {
						try {
							List<PopulationDataDto> popDataDtox = new ArrayList<PopulationDataDto>();
							FacadeProvider.getPopulationDataFacade().savePopulationData(popDataDtotoList);
						} catch (Exception exception) {

							System.out.println("excetion Caught while saving population data edit");
						} finally {
							confirmationDialog.close();
							dialog.close();
							treeGrid.getDataProvider().refreshAll();// refreshItem(campaignTreeGridDto);

							Notification.show(I18nProperties.getString(Strings.dataSavedSuccessfully));
						}
					});

					confirmationDialog.setText(
							"Are you sure you want to update the population data for the District " + name_ + "  ?");
					confirmationDialog.setHeader("Update Population Data");
					confirmationDialog.open();

				} else {
					Notification.show("You Need to fill all the data fields in order to save this population data");
				}

			}

		});

		VerticalLayout dialogLayout = new VerticalLayout(district, popData, popData5_10, popData4_23M, popData4_59M,
				districtModalityCombo, districtStatusCombo);
		dialogLayout.setPadding(false);
		dialogLayout.setSpacing(false);
		dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
		dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

		return dialogLayout;

	}

	private static Button createSaveButton() {
		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return saveButton;
	}

	private static Button createDeleteButton() {
		Button deleteButton = new Button(I18nProperties.getCaption(Captions.actionDelete));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return deleteButton;
	}

	public String DateGetYear(Date dates) {

		SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
		String currentYear = getYearFormat.format(dates);
		return currentYear;
	}

	/**
	 * Check if any child cluster is selected for a given item
	 */
	private boolean hasAnySelectedChildCluster(CampaignTreeGridDto item) {
		List<CampaignTreeGridDto> children = getChildrenForSelection(item);
		if (children.isEmpty())
			return false;

		for (CampaignTreeGridDto child : children) {
			if ("cluster".equals(child.getLevelAssessed())) {
				boolean isSelected = selectedClusterUuids.contains(child.getUuid())
						|| pendingSelectedClusters.contains(child.getUuid())
						|| (child.getSelected() || "true".equalsIgnoreCase(child.getSavedData()));
				if (isSelected)
					return true;
			} else {
				if (hasAnySelectedChildCluster(child))
					return true;
			}
		}
		return false;
	}

	/**
	 * Full recursive update of all parent checkboxes based on current cluster state
	 * (including pending changes). Call this after any selection change.
	 */
	private void updateAllParentSelections() {

		for (CampaignTreeGridDto area : treeGrid.getTreeData().getRootItems()) {
			updateParentSelectionRecursive(area);
		}

		treeGrid.getDataProvider().refreshAll();
	}

	private void updateParents(CampaignTreeGridDto item) {

		CampaignTreeGridDto parent = findParentItem(item);

		while (parent != null) {

			boolean selected = hasSelectedClusterBelow(parent);

			parent.setSelected(selected);
			parent.setSavedData(String.valueOf(selected));

			updateCheckboxState(parent.getUuid(), selected);

			parent = findParentItem(parent);
		}
	}

	private void updateParentSelectionRecursive(CampaignTreeGridDto item) {

		if ("cluster".equals(item.getLevelAssessed())) {
			return;
		}

		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			updateParentSelectionRecursive(child);
		}

		boolean selected = hasSelectedClusterBelow(item);

		item.setSelected(selected);
		item.setSavedData(String.valueOf(selected));

		updateCheckboxState(item.getUuid(), selected);
	}

	private void buildParentMapRecursive(CampaignTreeGridDto item, CampaignTreeGridDto parent) {
		if (item != null) {
			parentMap.put(item.getUuid(), parent);
			for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
				buildParentMapRecursive(child, item);
			}
		}
	}

	public void initSelectionsAfterRender() {
		isInitializing = true;
		try {
			initializeSelectionsFromDB();
			updateAllParentSelections();
			treeGrid.getDataProvider().refreshAll();

			// Optional: Expand first level
			treeGrid.getTreeData().getRootItems().forEach(treeGrid::expand);

		} finally {
			isInitializing = false;
		}
	}

	public Set<String> getSelectedClusterUuids() {
		Set<String> selectedClusters = new HashSet<>();

		for (CampaignTreeGridDto root : treeGrid.getTreeData().getRootItems()) {
			collectSelectedClusters(root, selectedClusters);
		}

		return selectedClusters;
	}

	private void collectSelectedClusters(CampaignTreeGridDto item, Set<String> selectedClusters) {

		if ("cluster".equals(item.getLevelAssessed())) {

			boolean selected = "true".equalsIgnoreCase(item.getSavedData()) || item.getSelected();

			if (selected) {
				selectedClusters.add(item.getUuid());
			}
		}

		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			collectSelectedClusters(child, selectedClusters);
		}
	}

	private void buildSelectedSetsFromCheckboxState() {
		areass.clear();
		region.clear();
		districts.clear();
		community.clear();
		popopulationDataDtoSet.clear();

		// Walk all tree items and check their current checkbox/DTO state
		for (CampaignTreeGridDto root : treeGrid.getTreeData().getRootItems()) {
			collectSelectedItemsRecursive(root);
		}
	}

	private void collectSelectedItemsRecursive(CampaignTreeGridDto item) {
		if (item == null)
			return;
		String level = item.getLevelAssessed();
		boolean isSelected = "true".equalsIgnoreCase(item.getSavedData())
//                      || item.getSelected()
				|| pendingSelectedClusters.contains(item.getUuid());
		boolean isDeselected = pendingDeselectedClusters.contains(item.getUuid());

		if (isSelected && !isDeselected) {
			switch (level) {
			case "area":
				areass.add(FacadeProvider.getAreaFacade().getAreaReferenceByUuid(item.getUuid()));
				break;

			case "region":
				region.add(FacadeProvider.getRegionFacade().getRegionReferenceByUuid(item.getUuid()));
				break;

			case "district":
				districts.add(FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(item.getUuid()));
				break;

			case "cluster":
				CommunityReferenceDto clusterRef = FacadeProvider.getCommunityFacade()
						.getCommunityReferenceByUuid(item.getUuid());
				community.add(clusterRef);

				// Build PopulationData entry — region comes from parent chain
				PopulationDataDto popData = new PopulationDataDto();
				popData.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
				popData.setCommunity(clusterRef);

				// Resolve region + district from parent map
				CampaignTreeGridDto districtParent = parentMap.get(item.getUuid());
				if (districtParent != null) {
					popData.setDistrict(
							FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(districtParent.getUuid()));

					CampaignTreeGridDto regionParent = parentMap.get(districtParent.getUuid());
					if (regionParent != null) {
						popData.setRegion(
								FacadeProvider.getRegionFacade().getRegionReferenceByUuid(regionParent.getUuid()));
					}
				}

				popopulationDataDtoSet.add(popData);
				break;
			}
		}

		// Always recurse into children
		for (CampaignTreeGridDto child : getChildrenForSelection(item)) {
			collectSelectedItemsRecursive(child);
		}
	}
	
	private int getEffectiveSelectedClusterCount() {
	    if (treeGrid == null || treeGrid.getTreeData() == null) {
	        return 0;
	    }

	    Set<String> selected = new HashSet<>();
	    for (CampaignTreeGridDto root : treeGrid.getTreeData().getRootItems()) {
	        collectSelectedClustersFromNode(root, selected);
	    }
	    return selected.size();
	}

	private void collectSelectedClustersFromNode(CampaignTreeGridDto node, Set<String> selected) {
	    if (node == null) return;

	    if ("cluster".equals(node.getLevelAssessed())) {
	        if ((node.getSelected() || "true".equalsIgnoreCase(node.getSavedData())) && node.getUuid() != null) {
	            selected.add(node.getUuid());
	        }
	        return;
	    }

	    for (CampaignTreeGridDto child : getChildrenForSelection(node)) {
	        collectSelectedClustersFromNode(child, selected);
	    }
	}

	private void refreshSelectedClusterCountLabel() {
	    selectedClusterCountLabel.setText("Selected clusters: " + getEffectiveSelectedClusterCount());
	}
	
	
	public boolean hasUnsavedChanges() {
	    return hasUnsavedChanges;
	}
	
	public void discardUnsavedChangesForNavigation() {
	    if (hasPendingChanges) {
	        discardPendingChanges(); // your existing method
	    }
	}


	public void setOnDirtyStateChanged(Runnable onDirtyStateChanged) {
	    this.onDirtyStateChanged = onDirtyStateChanged;
	}

	private void markDirty() {
	    if (!suppressUnsavedTracking && !hasUnsavedChanges) {
	        hasUnsavedChanges = true;
	        if (onDirtyStateChanged != null) {
	            onDirtyStateChanged.run();
	        }
	    }
	}

	private void markClean() {
	    if (hasUnsavedChanges) {
	        hasUnsavedChanges = false;
	        if (onDirtyStateChanged != null) {
	            onDirtyStateChanged.run();
	        }
	    }
	}

	private void runWithoutDirtyTracking(Runnable action) {
	    boolean prev = suppressUnsavedTracking;
	    suppressUnsavedTracking = true;
	    try {
	        action.run();
	    } finally {
	        suppressUnsavedTracking = prev;
	    }
	}


}