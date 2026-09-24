package com.cinoteck.application.views.configurations;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.ImportCellData;
import com.cinoteck.application.views.utils.importutils.ImportErrorException;
import com.cinoteck.application.views.utils.importutils.ImportLineResult;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ImportColumn;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.community.Modality;
import de.symeda.sormas.api.infrastructure.community.Status;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionFacade;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

/**
 * Data importer that is used to import population data.
 */
public class ClusterDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceDataImporter.class);
	private static final String CLUSTER_NAME = "Cluster_Name";
	private static final String CLUSTER_NO = "ClusterNo";
	private static final String P_CODE = "PCode";
	private static final String D_CODE = "DCode";
	private static final String C_CODE = "CCode";
	private static final String INTERNATIONAL_BORDER = "International_Border";
	private static final String POPULATIONDATA_0_4 = "PopulationData_0_59M";
	private static final String POPULATIONDATA_5_10 = "PopulationData_60_120M";
	private static final String POPULATIONDATA_4_23M = "Populationdata_4_23M";
	private static final String POPULATIONDATA_4_59M = "Populationdata_4_59M";

	private static final String MODALITY = "Modality";
	private static final String STATUS = "Status";

	private final CommunityFacade clusterFacade;

	private UI currentUI;
	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	List<CommunityReferenceDto> clusters = new ArrayList<>();
	List<DistrictReferenceDto> districtThree = new ArrayList<>();
	List<DistrictReferenceDto> districtFour = new ArrayList<>();
	List<CommunityReferenceDto> clusterNameList = new ArrayList<>();
	UserProvider userProvider = new UserProvider();
	LocalDate localDate = LocalDate.now();
	Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	private ValueSeparator csvSeparator;
	File file_;
	Integer externalIdValueForValidation = 0;
	// Constructor and other methods...

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public ClusterDataImporter(File inputFile, boolean hasEntityClassRow, CommunityDto currentUser,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {
		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.isOverWrite = overwrite;
		this.clusterFacade = FacadeProvider.getCommunityFacade();
	}

	public ValueSeparator getCsvSeparator() {
		return csvSeparator;
	}

	public List<String> extractColumnValues(String columnName) throws IOException {
		Set<String> columnValues = new HashSet<>();

		try (FileReader reader = new FileReader(inputFile);
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()
						.withDelimiter(FacadeProvider.getConfigFacade().getCsvSeparator()))) {
			for (CSVRecord csvRecord : csvParser) {
				columnValues.add(csvRecord.get(columnName));
			}

		}

		return new ArrayList<>(columnValues);
	}

	@Override
	public void startImport(File file_, Consumer<StreamResource> addErrorReportToLayoutCallback,
			Consumer<StreamResource> addCredentialReportToLayoutCallback, boolean isUserCreation, UI currentUI,
			boolean duplicatesPossible) throws IOException, CsvValidationException {

		this.currentUI = currentUI;
		super.startImport(file_, addErrorReportToLayoutCallback, null, false, currentUI, false);
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine) throws IOException, InterruptedException {

		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}
		List<Long> comuityExternalIdsInFile = new ArrayList<>();

		for (int i = 0; i < entityProperties.length; i++) {
			if (C_CODE.equalsIgnoreCase(entityProperties[i])) {
				if (!DataHelper.isNullOrEmpty(values[i])) {

					Long externalIdValue = Long.parseLong(values[i]);
					comuityExternalIdsInFile.add(externalIdValue);
//					clusters = FacadeProvider.getCommunityFacade().get

				}
			}
		}
		System.out.println(comuityExternalIdsInFile
				+ "ttttttttttttttttTTTTTTTTTTTTT--------------000000000000000000000000000000000");
		// Lets run some validations

		RegionReferenceDto province = null;
		DistrictReferenceDto district = null;
		Long province_xt_id = null;
		Long district_xt_id = null;

		Long clusterExtId = null;
		Integer clusterNumber = null;
		String clusterName = "";
		String floatStatus = "";

		boolean activeStatus = false;
		boolean isInternationalBorder = false;

		Long populationData_0_4 = null;
		Long populationData_5_10 = null;
		Long populationData_4_23M = null;
 		
		Long populationData_4_59M = null;
 
		String status = "";
		String modality = "";

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			if (P_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					province_xt_id = Long.parseLong(values[i]);
					List<RegionReferenceDto> existingProvinces = FacadeProvider.getRegionFacade()
							.getByExternalId(province_xt_id, false);

					if (existingProvinces.size() < 1) {
						province_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Province does not exist or > 1");
						return ImportLineResult.ERROR;
					} else if (existingProvinces.size() == 1) {

						province = existingProvinces.get(0);

					} else {
						province_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Posible Duplicate PCode Found");
						return ImportLineResult.ERROR;
					}
				} catch (NumberFormatException e) {
					province_xt_id = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (D_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					district_xt_id = Long.parseLong(values[i]);

					List<DistrictReferenceDto> existingDistricts = FacadeProvider.getDistrictFacade()
							.getByExternalId(district_xt_id, false);

					if (existingDistricts.size() < 1) {
						district_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | District does not exist or > 1");
						return ImportLineResult.ERROR;
					} else if (existingDistricts.size() == 1) {

						district = existingDistricts.get(0);

					} else {
						district_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Posible Duplicate DCode Found");
						return ImportLineResult.ERROR;
					}
				} catch (NumberFormatException e) {
					district_xt_id = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (C_CODE.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | CCode cannot be left empty");
					return ImportLineResult.ERROR;

				} else {

					try {
						Long externalIdValue = Long.parseLong(values[i]);
						externalIdValueForValidation = Integer.parseInt(values[i]);

						clusters = FacadeProvider.getCommunityFacade().getByExternalId(externalIdValue, false);

						if (clusters.size() > 0) {

							if (isOverWrite && clusters.size() == 1) {
								try {
//									Long externalIdValue = Long.parseLong(values[i]);
									clusterExtId = externalIdValue;
									isOverWriteEnabledCode = true;
								} catch (NumberFormatException e) {
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage()
													+ " | " + e.getMessage());
									return ImportLineResult.ERROR;
								}

							} else if (clusters.size() > 1) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Possible duplicate already on this system");
								return ImportLineResult.ERROR;
							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | CCode exists on the system");

								return ImportLineResult.ERROR;
							}

						} else if (clusters.size() == 0) {

							if (externalIdValue.toString().length() < 6 || externalIdValue.toString().length() > 7) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | CCode must be between 6 and 7 digits");
								return ImportLineResult.ERROR;
							} else {

								String threeDCode = values[i].substring(0, 3);
								String fourDCode = values[i].substring(0, 4);

								districtThree = FacadeProvider.getDistrictFacade()
										.getByExternalId(Long.parseLong(threeDCode), false);
								districtFour = FacadeProvider.getDistrictFacade()
										.getByExternalId(Long.parseLong(fourDCode), false);
								if (isOverWrite) {
									if (districtThree.size() > 0 || districtFour.size() > 0) {
										clusterExtId = externalIdValue;

									} else if (districtThree.size() == 0 && districtFour.size() == 0) {
										writeImportError(values,
												new ImportErrorException(values[i], entityProperties[i]).getMessage()
														+ " | CCode must be a subchild of DCode | Wrong CCode");
										return ImportLineResult.ERROR;

									}
								} else {
									if (districtThree.size() > 0 || districtFour.size() > 0) {
										clusterExtId = externalIdValue;

									} else if (districtThree.size() == 0 && districtFour.size() == 0) {
										writeImportError(values,
												new ImportErrorException(values[i], entityProperties[i]).getMessage()
														+ " | CCode must be a subchild of DCode | Wrong CCode");
										return ImportLineResult.ERROR;

									}
								}

							}
						}

					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}

			if (CLUSTER_NO.equalsIgnoreCase(entityProperties[i])) {
				try {

					Integer externalIdValue = Integer.parseInt(values[i]);
					int clusterNoSize = String.valueOf(Math.abs(externalIdValue)).length();
					String externalIdValueForValidationString = String.valueOf(Math.abs(externalIdValueForValidation));
					externalIdValueForValidationString = externalIdValueForValidationString.substring(externalIdValueForValidationString.length() - 3);
					System.out
							.println(clusterNoSize + " clusterNumber.SIZEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
					if (clusterNoSize > 3 && clusterNoSize < 1) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Cluster Number cannot be more than 3 or less than 1 digits");
						return ImportLineResult.ERROR;
					} else if (externalIdValue < 0 || externalIdValue == 0) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Cluster Number cannot be negative or zero");
						return ImportLineResult.ERROR;

					} else if (!Objects.equals(externalIdValue, Integer.parseInt(externalIdValueForValidationString))) {
						System.out.println("!Objects.equals(externalIdValue, externalIdValueForValidation) " + !Objects.equals(externalIdValue, Integer.parseInt(externalIdValueForValidationString)));
						System.out.println(externalIdValue + " externalIdValue " + Integer.parseInt(externalIdValueForValidationString) + " Integer.parseInt(externalIdValueForValidationString)");
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Cluster Number does not match the CCode combination");
						return ImportLineResult.ERROR;

					} else if (Objects.equals(externalIdValue, Integer.parseInt(externalIdValueForValidationString))) {
						clusterNumber = externalIdValue;
					}
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (CLUSTER_NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					clusterName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {
					String clusterName_ = values[i];

					boolean isNoSpaceMatch = clusterName_.matches(clusterName_);

					if (!isNoSpaceMatch) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space and might be "
										+ Validations.textTooLong);
						return ImportLineResult.ERROR;
					} else {

						DistrictReferenceDto regrefDto = new DistrictReferenceDto();
						clusterNameList = FacadeProvider.getCommunityFacade().getByName(clusterName_, regrefDto, true);

						if (clusterNameList.size() < 1) {
							clusterName = clusterName_;

						} else {
							if (clusterNameList.size() >= 0) {
								System.out.println(clusterName_ + "6666666666666666666666666666666666666666666666666666"
										+ clusterName_);

								List<DistrictReferenceDto> existingDistricts = FacadeProvider.getDistrictFacade()
										.getByExternalId(district_xt_id, false);

								String finalDistrictUUid = "";

								for (DistrictReferenceDto isolatedDistrict : existingDistricts) {
									finalDistrictUUid = isolatedDistrict.getUuid();
								}

								List<CommunityReferenceDto> checkClusterInDistrictList = FacadeProvider
										.getCommunityFacade().getAllActiveByDistrict(finalDistrictUUid);

								List<String> clusterNames = new ArrayList<String>();

								System.out.println(checkClusterInDistrictList.size() + "List of Clusters in District");
								for (CommunityReferenceDto ffff : checkClusterInDistrictList) {

									String caption = ffff.getCaption();

									clusterNames.add(caption);
								}
								if (clusterNames.contains(values[i])) {
									clusterName = values[i];
//									writeImportError(values,
//											new ImportErrorException(values[i], entityProperties[i]).getMessage()
//													+ " | Cluster Name exist ");
//
//									return ImportLineResult.ERROR;
								} else {
									clusterName = clusterName_;
								}
							}
						}

					}
				}
			}

			if ("Float_Status".equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					floatStatus = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Float Status cannot be left empty");
					return ImportLineResult.ERROR;

				} else {
					if (values[i].toString().equalsIgnoreCase("floating")
							|| values[i].toString().equalsIgnoreCase("normal")) {
						floatStatus = values[i];
					} else {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Float Status can only be either Floating or Normal");
						return ImportLineResult.ERROR;
					}

				}
			}

			if ("Active_Status".equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					activeStatus = false;

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Active Status cannot be left empty");
					return ImportLineResult.ERROR;

				} else {
					if (values[i].toString().equalsIgnoreCase("Archived")) {
						activeStatus = true;
					} else if (values[i].toString().equalsIgnoreCase("Active")) {
						activeStatus = false;
					} else {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Active Status can only be either Active or Archived");
						return ImportLineResult.ERROR;
					}

				}
			}

			if ("Status".equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					status = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Status cannot be left empty");
					return ImportLineResult.ERROR;

				} else {
					if (values[i].toString().equalsIgnoreCase("Additional")
							|| values[i].toString().equalsIgnoreCase("Additional & Cold")
							|| values[i].toString().equalsIgnoreCase("Cold")
							|| values[i].toString().equalsIgnoreCase("Full Cluster")
							|| values[i].toString().equalsIgnoreCase("HRMP Only")
							|| values[i].toString().equalsIgnoreCase("Partial")
							|| values[i].toString().equalsIgnoreCase("Not Targeted")
							|| values[i].toString().equalsIgnoreCase("On Hold")) {
						status = values[i];
					} else {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Status can only be one of the following Additional, Additional & Cold, Cold, Full Cluster, HRMP Only, Partial, Not Targeted, On Hold");
						return ImportLineResult.ERROR;
					}

				}
			}

			if ("Modality".equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					modality = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Modality cannot be left empty");
					return ImportLineResult.ERROR;

				} else {
					if (values[i].toString().equalsIgnoreCase("H2H") || values[i].toString().equalsIgnoreCase("M2M")
							|| values[i].toString().equalsIgnoreCase("S2S")
							|| values[i].toString().equalsIgnoreCase("HF2HF")
							|| values[i].toString().equalsIgnoreCase("Mixed")
							|| values[i].toString().equalsIgnoreCase("M2M S2S")) {
						modality = values[i];
					} else {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Modality can only be one of the following H2H, M2M, S2S, HF2HF, Mixed, M2M S2S");
						return ImportLineResult.ERROR;
					}

				}
			}

			if (INTERNATIONAL_BORDER.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					isInternationalBorder = false;

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | International Border cannot be left empty enter Yes or No");
					return ImportLineResult.ERROR;

				} else {
					if (values[i].toString().equalsIgnoreCase("Yes")) {
						isInternationalBorder = true;
					} else if (values[i].toString().equalsIgnoreCase("No")) {
						isInternationalBorder = false;
					} else {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | International Border value can only be either Yes or No");
						return ImportLineResult.ERROR;
					}

				}
			}

			if (POPULATIONDATA_0_4.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Population Data cannot be left empty");
					return ImportLineResult.ERROR;
				} else {
					try {
						long pop = Long.parseLong(values[i]);
						if (pop < 0) {
							writeImportError(values,
									"Negative values are not allowed for " + entityProperties[i] + ": " + values[i]);
							return ImportLineResult.ERROR;
						}
						populationData_0_4 = pop;
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}

			if (POPULATIONDATA_5_10.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Population Data cannot be left empty enter");
					return ImportLineResult.ERROR;
				} else {
					try {
						long pop = Long.parseLong(values[i]);
						if (pop < 0) {
							writeImportError(values,
									"Negative values are not allowed for " + entityProperties[i] + ": " + values[i]);
							return ImportLineResult.ERROR;
						}
						populationData_5_10 = pop;
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}

			if (POPULATIONDATA_4_23M.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Population Data cannot be left emptyo");
					return ImportLineResult.ERROR;
				} else {
					try {
						long pop = Long.parseLong(values[i]);
						if (pop < 0) {
							writeImportError(values,
									"Negative values are not allowed for " + entityProperties[i] + ": " + values[i]);
							return ImportLineResult.ERROR;
						}
						populationData_4_23M = pop;
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}
 			
			if (POPULATIONDATA_4_59M.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Population Data cannot be left emptyo");
					return ImportLineResult.ERROR;
				} else {
					try {
						long pop = Long.parseLong(values[i]);
						if (pop < 0) {
							writeImportError(values, "Negative values are not allowed for " + entityProperties[i] + ": " + values[i]);
							return ImportLineResult.ERROR;
						}
						populationData_4_59M = pop;
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}
			
 
		}

		if (province_xt_id != null && province != null && clusterName != "" && clusterExtId != null
				&& clusterNumber != null && floatStatus != null) {
		} else {
			writeImportError(values, " | Somthing went wrong: Required data not supplied");
			return ImportLineResult.ERROR;
		}

		// check if dcode is a subchild of pcode
		List<DistrictReferenceDto> toCheck = FacadeProvider.getDistrictFacade()
				.getAllActiveByRegion(province.getUuid());
		if (!toCheck.contains(district)) {
			writeImportError(values, " | PCode does not match DCode");
			return ImportLineResult.ERROR;
		}

		final RegionReferenceDto finalRegion = province;
		final DistrictReferenceDto finalDistrict = district;

		final String finalClustername = clusterName;
		final Long clusterid = clusterExtId;
		final Integer clusterNo = clusterNumber;
		final String finalFloatStatus = floatStatus;
		final boolean finalActiveStatus = activeStatus;
		final boolean finalIntlBorderStatus = isInternationalBorder;

		final Long finalPopData_0_4 = populationData_0_4;
		final Long finalPopData_5_10 = populationData_5_10;
		final Long finalPopData_4_23M = populationData_4_23M;
 		final Long finalPopData_4_59M = populationData_4_59M;
 
		final String finalStatus = status;
		final String finalModality = modality;

		List<CommunityDto> newUserLinetoSave = new ArrayList<>();

		if (isOverWrite && isOverWriteEnabledCode) {

			if (clusters.size() != 0) {
				CommunityDto newUserLine_ = FacadeProvider.getCommunityFacade().getByUuid(clusters.get(0).getUuid());
				newUserLine_.setName(finalClustername);
				newUserLine_.setRegion(finalRegion);
				newUserLine_.setDistrict(finalDistrict);
				newUserLine_.setClusterNumber(clusterNo);
				newUserLine_.setExternalId(clusterid);
				newUserLine_.setFloating(finalFloatStatus);
				newUserLine_.setArchived(finalActiveStatus);// setFloating(finalFloatStatus);
				newUserLine_.setInternationalborder(finalIntlBorderStatus);// setFloating(finalFloatStatus);

				newUserLine_.setPopulationData(finalPopData_0_4);// setFloating(finalFloatStatus);
				newUserLine_.setPopulationData5_10(finalPopData_5_10);// setFloating(finalFloatStatus);
				newUserLine_.setPopulationData4_23M(finalPopData_4_23M);// setFloating(finalFloatStatus);

				newUserLine_.setPopulationData4_59M(finalPopData_4_59M);// setFloating(finalFloatStatus);

				
				newUserLine_.setStatus(Status.fromValue(finalStatus));
				newUserLine_.setModality(Modality.fromValue(finalModality));

				boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
						new Function<ImportCellData, Exception>() {

							@Override
							public Exception apply(ImportCellData cellData) {
								System.out.println(
										"++++++++++++++++111111111:ccc " + cellData.getEntityPropertyPath()[0]);

								try {

									if (CommunityDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setName(cellData.getValue());
									}
									if (CommunityDto.CLUSTER_NUMBER
											.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setClusterNumber(Integer.parseInt(cellData.getValue()));
									}
									if (CommunityDto.EXTERNAL_ID
											.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setExternalId(Long.parseLong(cellData.getValue()));
									}
									if (CommunityDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										Long externalId = Long.parseLong(cellData.getValue());
										List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
												.getByExternalId(externalId, false);
										RegionReferenceDto areaReferenceDto = areasz.get(0);
										newUserLine_.setRegion(areaReferenceDto);
//											newUserLine_.setArea(cellData.getValue());
									}

									if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										Long externalId = Long.parseLong(cellData.getValue());
										List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
												.getByExternalId(externalId, false);
										DistrictReferenceDto districtReferenceDto = areasz.get(0);
										newUserLine_.setDistrict(districtReferenceDto);

//											newUserLine_.setArea(cellData.getValue());
									}

									/*
									 * PLEASE MAKE SURE TO REMOVE THIS SETTING OF FLOAT UNTIL WE GET A CLEAR
									 * DESCRIPTION ON WETHER IT SHOULD BE A COMPULSORY FIELD TO FILL WHEN UPLOADING
									 * CLUSTERS IN IMPORT --SEGUN
									 * 
									 * REPEAT THE PROCESS IN THE ELSE STATEMENT WHEN THIS HAS BEEN FIXED
									 */

									if ("Float_Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Active_Statustttttttttttfloating cellData.getValue()cellData.getValue()");

										newUserLine_.setFloating(cellData.getValue());

//										newUserLine_.setName(cellData.getValue());
									}

									if ("Active_Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {

										System.out.println(cellData.getValue()
												+ "Active_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
										newUserLine_.setArchived(finalActiveStatus);

//										newUserLine_.setName(cellData.getValue());
									}

									if ("Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Statusssssssssssssssssss cellData.getValue()cellData.getValue()");

										newUserLine_.setStatus(Status.fromValue(cellData.getValue()));
									}

									if ("Modality".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Statusssssssssssssssssss cellData.getValue()cellData.getValue()");

										newUserLine_.setModality(Modality.fromValue(cellData.getValue()));
									}

									if (INTERNATIONAL_BORDER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Active_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
										newUserLine_.setInternationalborder(finalIntlBorderStatus);

//										newUserLine_.setName(cellData.getValue());
									}

									if (POPULATIONDATA_0_4.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());
										if (pop < 0) {
											throw new NumberFormatException("Negative values not allowed");
										}
										newUserLine_.setPopulationData(pop);
									}

									if (POPULATIONDATA_5_10.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());
										if (pop < 0) {
											throw new NumberFormatException("Negative values not allowed");
										}
										newUserLine_.setPopulationData5_10(pop);
									}

									if (POPULATIONDATA_4_23M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());
										if (pop < 0) {
											throw new NumberFormatException("Negative values not allowed");
										}
										newUserLine_.setPopulationData4_23M(pop);
									}
									
									if (POPULATIONDATA_4_59M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());
										if (pop < 0) {
											throw new NumberFormatException("Negative values not allowed");
										}
										newUserLine_.setPopulationData4_59M(pop);
									}
									
//									newUserLine_.setFloating("");

									newUserLinetoSave.add(newUserLine_);

								} catch (NumberFormatException e) {
									System.out.println("++++++++++++++++Error found++++++++++++++++ ");

									return e;
								}

								return null;
							}
						});

				if (!usersDataHasImportError) {

					try {
						FacadeProvider.getCommunityFacade().save(newUserLinetoSave.get(0), true);

						return ImportLineResult.SUCCESS;
					} catch (ValidationRuntimeException e) {
						writeImportError(values, values + " already exists.");
						return ImportLineResult.ERROR;
					}
				} else {
					return ImportLineResult.ERROR;
				}

			} else {

				CommunityDto newUserLine_ = CommunityDto.build();

//				CommunityDto newUserLine_ = FacadeProvider.getCommunityFacade().getByUuid(clusters.get(0).getUuid());
				newUserLine_.setName(finalClustername);
				newUserLine_.setRegion(finalRegion);
				newUserLine_.setDistrict(finalDistrict);
				newUserLine_.setClusterNumber(clusterNo);
				newUserLine_.setExternalId(clusterid);
				newUserLine_.setFloating(finalFloatStatus);
				newUserLine_.setArchived(finalActiveStatus);

				newUserLine_.setInternationalborder(finalIntlBorderStatus);

				newUserLine_.setPopulationData(finalPopData_0_4);// setFloating(finalFloatStatus);
				newUserLine_.setPopulationData5_10(finalPopData_5_10);// setFloating(finalFloatStatus);
				newUserLine_.setPopulationData4_23M(finalPopData_4_23M);
 				newUserLine_.setPopulationData4_59M(finalPopData_4_59M);
 
				newUserLine_.setStatus(Status.fromValue(finalStatus));
				newUserLine_.setModality(Modality.fromValue(finalModality));

				boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
						new Function<ImportCellData, Exception>() {

							@Override
							public Exception apply(ImportCellData cellData) {
								System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

								try {

									if (CommunityDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setName(cellData.getValue());
									}
									if (CommunityDto.CLUSTER_NUMBER
											.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setClusterNumber(Integer.parseInt(cellData.getValue()));
									}
									if (CommunityDto.EXTERNAL_ID
											.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine_.setExternalId(Long.parseLong(cellData.getValue()));
									}
									if (CommunityDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										Long externalId = Long.parseLong(cellData.getValue());
										List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
												.getByExternalId(externalId, false);
										RegionReferenceDto areaReferenceDto = areasz.get(0);
										newUserLine_.setRegion(areaReferenceDto);
//											newUserLine_.setArea(cellData.getValue());
									}

									if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										Long externalId = Long.parseLong(cellData.getValue());
										List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
												.getByExternalId(externalId, false);
										DistrictReferenceDto districtReferenceDto = areasz.get(0);
										newUserLine_.setDistrict(districtReferenceDto);
//											newUserLine_.setArea(cellData.getValue());
									}

									/*
									 * PLEASE MAKE SURE TO REMOVE THIS SETTING OF FLOAT UNTIL WE GET A CLEAR
									 * DESCRIPTION ON WETHER IT SHOULD BE A COMPULSORY FIELD TO FILL WHEN UPLOADING
									 * CLUSTERS IN IMPORT --SEGUN
									 * 
									 * REPEAT THE PROCESS IN THE IF STATEMENT WHEN THIS HAS BEEN FIXED
									 */
									if (CommunityDto.FLOATING_ATTRIBUTE
											.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "floating cellData.getValue()cellData.getValue()");
										newUserLine_.setFloating(cellData.getValue());

//										newUserLine_.setName(cellData.getValue());
									}

									if ("Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Statussssssssssssssss cellData.getValue()cellData.getValue()");
										newUserLine_.setStatus(Status.fromValue(cellData.getValue()));
									}

									if ("Modality".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "Modalityyyyyyyyyyyyyyyyyyyyy cellData.getValue()cellData.getValue()");
										newUserLine_.setModality(Modality.fromValue(cellData.getValue()));
									}

									if ("Active_Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "tttttttttttfloating cellData.getValue()cellData.getValue()");

										newUserLine_.setArchived(finalActiveStatus);

//										newUserLine_.setName(cellData.getValue());
									}

									if (INTERNATIONAL_BORDER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										System.out.println(cellData.getValue()
												+ "tttttttttttfloating cellData.getValue()cellData.getValue()");

										newUserLine_.setInternationalborder(finalIntlBorderStatus);

//										newUserLine_.setName(cellData.getValue());
									}

									if (POPULATIONDATA_0_4.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());

										newUserLine_.setPopulationData(pop);
									}

									if (POPULATIONDATA_5_10.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());

										newUserLine_.setPopulationData5_10(pop);
									}

									if (POPULATIONDATA_4_23M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());

										newUserLine_.setPopulationData4_23M(pop);
									}
 									
									if (POPULATIONDATA_4_59M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										long pop = Long.parseLong(cellData.getValue());
									 
										newUserLine_.setPopulationData4_59M(pop);
									}
 
									newUserLinetoSave.add(newUserLine_);

								} catch (NumberFormatException e) {
									System.out.println("++++++++++++++++Error found++++++++++++++++ ");

									return e;
								}

								return null;
							}
						});

				if (!usersDataHasImportError) {

					try {
						FacadeProvider.getCommunityFacade().save(newUserLinetoSave.get(0), true);

						return ImportLineResult.SUCCESS;
					} catch (ValidationRuntimeException e) {
						writeImportError(values, values + " already exists.");
						return ImportLineResult.ERROR;
					}
				} else {
					return ImportLineResult.ERROR;
				}

			}

		} else {
			CommunityDto newUserLine = CommunityDto.build();

			System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
			newUserLine.setName(finalClustername);
			newUserLine.setRegion(finalRegion);
			newUserLine.setDistrict(finalDistrict);
			newUserLine.setClusterNumber(clusterNo);
			newUserLine.setExternalId(clusterid);
			newUserLine.setFloating(finalFloatStatus);
			newUserLine.setArchived(finalActiveStatus);

			newUserLine.setInternationalborder(finalIntlBorderStatus);

			newUserLine.setPopulationData(finalPopData_0_4);// setFloating(finalFloatStatus);
			newUserLine.setPopulationData5_10(finalPopData_5_10);// setFloating(finalFloatStatus);
			newUserLine.setPopulationData4_23M(finalPopData_4_23M);
			newUserLine.setPopulationData4_59M(finalPopData_4_59M);
 
			newUserLine.setStatus(Status.fromValue(finalStatus));
			newUserLine.setModality(Modality.fromValue(finalModality));

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (CommunityDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setName(cellData.getValue());
								}
								if (CommunityDto.CLUSTER_NUMBER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setClusterNumber(Integer.parseInt(cellData.getValue()));
								}
								if (CommunityDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setExternalId(Long.parseLong(cellData.getValue()));
								}
								if (CommunityDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
											.getByExternalId(externalId, false);
									RegionReferenceDto areaReferenceDto = areasz.get(0);
									newUserLine.setRegion(areaReferenceDto);
//										newUserLine.setArea(cellData.getValue());
								}

								if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
											.getByExternalId(externalId, false);
									DistrictReferenceDto districtReferenceDto = areasz.get(0);
									newUserLine.setDistrict(districtReferenceDto);
//										newUserLine.setArea(cellData.getValue());
								}

								/*
								 * PLEASE MAKE SURE TO REMOVE THIS SETTING OF FLOAT UNTIL WE GET A CLEAR
								 * DESCRIPTION ON WETHER IT SHOULD BE A COMPULSORY FIELD TO FILL WHEN UPLOADING
								 * CLUSTERS IN IMPORT --SEGUN
								 * 
								 * REPEAT THE PROCESS IN THE ELSE STATEMENT WHEN THIS HAS BEEN FIXED
								 */
								if (CommunityDto.FLOATING_ATTRIBUTE
										.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "eeeeefloating cellData.getValue()cellData.getValue()");

									newUserLine.setFloating(cellData.getValue());

//									newUserLine_.setName(cellData.getValue());
								}

								if ("Active_Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "tttttttttttfloating cellData.getValue()cellData.getValue()");

									newUserLine.setArchived(finalActiveStatus);

//									newUserLine_.setName(cellData.getValue());
								}

								if ("Status".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "Statussssssssssssssss cellData.getValue()cellData.getValue()");
									newUserLine.setStatus(Status.fromValue(cellData.getValue()));
								}

								if ("Modality".equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "Modalityyyyyyyyyyyyyyy cellData.getValue()cellData.getValue()");
									newUserLine.setModality(Modality.fromValue(cellData.getValue()));
								}

								if (INTERNATIONAL_BORDER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "tttttttttttfloating cellData.getValue()cellData.getValue()");

									newUserLine.setInternationalborder(finalIntlBorderStatus);

//									newUserLine_.setName(cellData.getValue());
								}

								if (POPULATIONDATA_0_4.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "Popilationdata 0-4_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
									newUserLine.setPopulationData(finalPopData_0_4);

								}

								if (POPULATIONDATA_5_10.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "Popilationdata 0-4_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
									newUserLine.setPopulationData5_10(finalPopData_5_10);

								}

								if (POPULATIONDATA_4_23M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "--5-10Popilationdata4-23_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
									newUserLine.setPopulationData4_23M(Long.parseLong(cellData.getValue()));

								}
								
								if (POPULATIONDATA_4_59M.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									System.out.println(cellData.getValue()
											+ "--5-10Popilationdata4-59_Statustttttttttttfloating cellData.getValue()cellData.getValue()");
									newUserLine.setPopulationData4_59M(Long.parseLong(cellData.getValue()));

								}

								newUserLinetoSave.add(newUserLine);

							} catch (NumberFormatException e) {
								System.out.println("++++++++++++++++Error found++++++++++++++++ ");

								return e;
							}

							return null;
						}
					});

			if (!usersDataHasImportError) {
				boolean checkExeption = false;

				try {
					FacadeProvider.getCommunityFacade().save(newUserLinetoSave.get(0), true);

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
					checkExeption = true;

					writeImportError(values, values + " already exists.");
					return ImportLineResult.ERROR;
				} finally {
					if (!checkExeption) {

						ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();

						for (CommunityDto clusterData : newUserLinetoSave) {

							configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
							configurationChangeLogDto.setAction_unit_type("Cluster");
							configurationChangeLogDto.setAction_unit_name(clusterData.getName());
							configurationChangeLogDto.setUnit_code(clusterData.getExternalId());
							configurationChangeLogDto.setAction_date(date);

							if (isOverWrite) {
								configurationChangeLogDto.setAction_logged("Import : Overwrite");

							} else {
								configurationChangeLogDto.setAction_logged("Import");

							}
						}

						FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
						checkExeption = false;
					}
				}
			} else {
				return ImportLineResult.ERROR;
			}
		}

	}

	@Override
	protected boolean executeDefaultInvoke(PropertyDescriptor pd, Object element, String entry,
			String[] entryHeaderPath) throws InvocationTargetException, IllegalAccessException, ImportErrorException {

		final boolean invokingSuccessful = super.executeDefaultInvoke(pd, element, entry, entryHeaderPath);
		final Class<?> propertyType = pd.getPropertyType();
		return invokingSuccessful;
	}

	@Override
	protected String getErrorReportFileName() {
		return "cluster_Import_error_report.csv";
	}
}