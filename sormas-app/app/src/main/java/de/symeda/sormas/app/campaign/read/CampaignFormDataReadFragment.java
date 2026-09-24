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

package de.symeda.sormas.app.campaign.read;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementOptions;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaGeographyLevel;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.createControlImageReadField;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.app.BaseReadFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormData;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.component.controls.ControlDateField;
import de.symeda.sormas.app.component.controls.ControlPropertyField;
import de.symeda.sormas.app.component.controls.ControlSpinnerField;
import de.symeda.sormas.app.component.controls.ControlTextReadField;
import de.symeda.sormas.app.databinding.FragmentCampaignDataReadLayoutBinding;
import de.symeda.sormas.app.util.TextViewBindingAdapters;

import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.createControlDateEditField;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.createControlTextReadField;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.createControlImageReadField;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.getExpressionValue;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.getUserTranslations;
import static de.symeda.sormas.app.campaign.CampaignFormDataFragmentUtils.setVisibilityDependency;
import de.symeda.sormas.app.component.controls.ControlImageEditField;

public class CampaignFormDataReadFragment extends BaseReadFragment<FragmentCampaignDataReadLayoutBinding, CampaignFormData, CampaignFormData> {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private CampaignFormData record;

    private Map<String, String> optionsValues;

    private String caption_1 = "";
    private String caption_2 = "";
    private String caption_3 = "";
    private String caption_4 = "";
    private String caption_5 = "";
    private String caption_6 = "";
    private String caption_7 = "";
    private String caption_8 = "";

   // private List<CampaignFormTranslations> translationsOpt;
    private Map<String, String> userOptTranslations = null;

    public static CampaignFormDataReadFragment newInstance(CampaignFormData activityRootData) {
        return newInstance(
                CampaignFormDataReadFragment.class, null, activityRootData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);


// TODO :
        //This line causes a fatal app crash when listed form entry of a closed campaign is
        //opened , add a check to review the status of the campaign first and if it is closed
        //return a dialog notification to user informing them the campaign is closed for data entry
        final CampaignFormMeta campaignFormMeta = DatabaseHelper.getCampaignFormMetaDao().queryForId(record.getCampaignFormMeta().getId());
        final List<CampaignFormDataEntry> formValues = record.getFormValues();

        final List<CampaignFormTranslations> translationsOpt = record.getCampaignFormMeta().getCampaignFormTranslations();

        final Map<String, String> formValuesMap = new HashMap<>();
        final Map<String, Object> rawFormValuesMap = new HashMap<>();
        formValues.forEach(campaignFormDataEntry -> {
            formValuesMap.put(campaignFormDataEntry.getId(), DataHelper.toStringNullable(campaignFormDataEntry.getValue()));
            rawFormValuesMap.put(campaignFormDataEntry.getId(), campaignFormDataEntry.getValue());
        });
        final Map<String, ControlPropertyField> fieldMap = new HashMap<>();

        boolean daywise = false;
        int dayy = 0;
        int countr = 0;

        for (CampaignFormElement campaignFormElement : campaignFormMeta.getCampaignFormElements()) {
            CampaignFormElementType type = CampaignFormElementType.fromString(campaignFormElement.getType());

            if (type == CampaignFormElementType.DAYWISE) {
             daywise = true;
                System.out.println("checking dayesie --------------------------- "+daywise);
             break;
            }

        }

        Resources res = getResources();
        TabHost mTabHost = (TabHost) view.findViewById(R.id.tabhostxxxRd);
        mTabHost.setup();

        TabHost.TabSpec spec;

        countr = 0;

        for (CampaignFormElement campaignFormElement : campaignFormMeta.getCampaignFormElements()) {
            CampaignFormElementType type = CampaignFormElementType.fromString(campaignFormElement.getType());

            CampaignFormElementOptions campaignFormElementOptions = new CampaignFormElementOptions();
           if (campaignFormElement.getOptions() != null) {
                final Locale locale = I18nProperties.getUserLanguage().getLocale();

                if (locale != null) {
                    translationsOpt.stream().filter(t -> t.getLanguageCode().equals(locale.toString()))
                            .findFirst().ifPresent(filteredTranslations -> filteredTranslations.getTranslations().stream()
                            .filter(cd -> cd.getOptions() != null)
                            .findFirst().ifPresent(optionsList -> userOptTranslations = optionsList.getOptions().stream()
                                    .filter(c -> c.getCaption() != null).collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption))));
                }


                optionsValues = campaignFormElement.getOptions().stream().collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption));  // .collect(Collectors.toList());

                if (userOptTranslations == null) {
                    campaignFormElementOptions.setOptionsListValues(optionsValues);
                    //get18nOptCaption(formElement.getId(), optionsValues));
                } else {
                    campaignFormElementOptions.setOptionsListValues(userOptTranslations);

                }
            } else {
                optionsValues = new HashMap<String, String>();
            }




            if (daywise) {
                if (type == CampaignFormElementType.DAYWISE) {

                    countr++;

                 } else if (countr == 1) {
                    final LinearLayout dynamicLayout = view.findViewById(R.id.tabSheet1);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);
                        String yes_no = "";
                        if (value != null) {

                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                    for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                        if (part.equalsIgnoreCase(entry.getKey())){
                                                            selectedValue.add(entry.getValue());
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else{
                                System.out.println(value +" ControlTextReadFieldControlTextReadFieldControlTextReadField");
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
//                            if (type == CampaignFormElementType.YES_NO) {
//                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
//                            } else {
                                if(expressionValue != null){
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                }
//                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                // }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }

                } else if (countr == 2) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet2);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());

//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;

                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);
                        String yes_no = "";

                        if (value != null) {

                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }
                            else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            }else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();

                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }

                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }
                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 3) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet3);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 4) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet4);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if ( type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO  || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 5) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet5);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO  || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 6) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet6);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if ( type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO  || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 7) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet7);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO  || type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            }else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            } else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                } else if (countr == 8) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet8);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO ||type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            } else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            }else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                }
                else if (countr == 9) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet9);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO ||type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            } else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            }else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                }

                else if (countr == 10) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet10);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO ||type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            } else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            }else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                }
                else if (countr == 11) {
                    final LinearLayout dynamicLayout = mTabHost.findViewById(R.id.tabSheet11);
                    if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                        String value = formValuesMap.get(campaignFormElement.getId());
//                        value = value == null ? null : value.endsWith(".0") ?  value.replace(".0", "") : value;
                        String yes_no = "";
                        ControlPropertyField dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);


                        if (value != null) {
                            if (type == CampaignFormElementType.YES_NO) {
                                if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                    yes_no = "Yes";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                    yes_no = "No";
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                                }
                            } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO ||type == CampaignFormElementType.RADIOBASIC) {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                            } else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                                List<String> selectedKeys = Collections.singletonList(value);
                                List<String> selectedValue = new ArrayList<>();
                                if (selectedKeys instanceof List) {
                                    List<?> valueList = (List<?>) selectedKeys;
                                    for (Object element : valueList) {
                                        if (element instanceof String) {
                                            String str = ((String) element).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                str = str.substring(1, str.length() - 1);
                                            }
                                            String[] parts = str.split("\\s*,\\s*");//
                                            for (String part : parts) {
                                                for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                    if (part.equalsIgnoreCase(entry.getKey())){
                                                        selectedValue.add(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                            }else if(type == CampaignFormElementType.DROPDOWN){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                                //optionsValues.get(value)
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                            }
                        }
                        dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        fieldMap.put(campaignFormElement.getId(), dynamicField);
                        final String dependingOn = campaignFormElement.getDependingOn();
                        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                        List<String> constraints;
                        String depenValuexd = null;
                        if (dependingOnValues != null) {
                            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                            ListIterator<String> lstItemsx = constraints.listIterator();
                            while (lstItemsx.hasNext()) {
                                depenValuexd = lstItemsx.next().toString();
                            }
                        }
                        final String depenValuex = depenValuexd;

                        if (dependingOn != null && depenValuex != null) {
                            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                        }
                        final String expressionString = campaignFormElement.getExpression();
                        if (expressionString != null) {
                            try {
                                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                                if (type == CampaignFormElementType.YES_NO) {
                                    ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
                                } else {
                                    if(expressionValue != null){
                                        ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                                    }                                }
                            } catch (SpelEvaluationException e) {
                                Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                            }
                        }
                    } else if (type == CampaignFormElementType.SECTION) {
                        dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                    } else if (type == CampaignFormElementType.LABEL) {
                        TextView textView = new TextView(requireContext());
                        TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                        dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                }
            }
            else {
                final LinearLayout dynamicLayout = view.findViewById(R.id.dynamicLayoutxXRd);
                if (type != CampaignFormElementType.SECTION && type != CampaignFormElementType.LABEL) {
                    String value = formValuesMap.get(campaignFormElement.getId());
                    ControlPropertyField dynamicField;

                    if (type == CampaignFormElementType.IMAGE) {
                        ControlImageEditField imageField = createControlImageReadField(
                                campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        imageField.setShowCaption(true);
                        Object imageValue = rawFormValuesMap.get(campaignFormElement.getId());
                        if (imageValue != null) {
                            imageField.setValue(imageValue);      // raw Map/List, not the stringified value
                        }
                        dynamicField = imageField;
                    }else{
                        dynamicField = createControlTextReadField(campaignFormElement, requireContext(), getUserTranslations(campaignFormMeta));
                        dynamicField.setShowCaption(true);
                    }
//                    dynamicField.setShowCaption(true);


                    Resources resources = this.getContext().getResources();

                    String yes_no = "";


                    if (value != null) {
                        if (type == CampaignFormElementType.YES_NO) {
                            if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")){
                                yes_no = resources.getString(R.string.yes);
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                            } else if(value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")){
                                yes_no = resources.getString(R.string.no);
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null);
                            }
                        } else if (type == CampaignFormElementType.CHECKBOX || type == CampaignFormElementType.RADIO ||type == CampaignFormElementType.RADIOBASIC) {
                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null);
                        }
                        else if (type == CampaignFormElementType.CHECKBOXBASIC) {

                            List<String> selectedKeys = Collections.singletonList(value);
                            List<String> selectedValue = new ArrayList<>();
                            if (selectedKeys instanceof List) {
                                List<?> valueList = (List<?>) selectedKeys;
                                for (Object element : valueList) {
                                    if (element instanceof String) {
                                        String str = ((String) element).trim();
                                        if (str.startsWith("[") && str.endsWith("]")) {
                                            str = str.substring(1, str.length() - 1);
                                        }
                                        String[] parts = str.split("\\s*,\\s*");//
                                        for (String part : parts) {
                                            for (Map.Entry<String, String> entry : optionsValues.entrySet()) {
                                                if (part.equalsIgnoreCase(entry.getKey())){
                                                    selectedValue.add(entry.getValue());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, selectedValue.toString().replace("[", "").replace("]", ""), null, null);
                        }else if (type == CampaignFormElementType.DATE) {
//value = getDateValue(value).toString();

//                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, getDateValue(value).toString(), null, null);
                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, getDateValue(value), null, null);

                        } else if(type == CampaignFormElementType.DROPDOWN){
                            //TODO get the tranlated version

                            if(optionsValues.get(value).equalsIgnoreCase("yes")){
                                yes_no = resources.getString(R.string.yes);
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null, null);
                            }else if(optionsValues.get(value).equalsIgnoreCase("no")){
                                yes_no = resources.getString(R.string.no);
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, yes_no, null, null, null);
                            } else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, optionsValues.get(value), null, null, null);
                            }

                            //optionsValues.get(value)
                        }else if ( type == CampaignFormElementType.NUMBER){
                            if (campaignFormElement.getId().equalsIgnoreCase("villagecode")){
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value+"", null, null, null, false);
                            }else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null, false);
                            }
                        }else if (type == CampaignFormElementType.RANGE){
                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null, true);
                        }
                        else if (type == CampaignFormElementType.IMAGE){
                            ControlImageEditField.setValue((ControlImageEditField) dynamicField, rawFormValuesMap.get(campaignFormElement.getId()));
                        }
                        else{
                            ControlTextReadField.setValue((ControlTextReadField) dynamicField, value, null, null, null);
                        }
                    }
                    dynamicLayout.addView(dynamicField, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    fieldMap.put(campaignFormElement.getId(), dynamicField);
                    final String dependingOn = campaignFormElement.getDependingOn();
                    final String[] dependingOnValues = campaignFormElement.getDependingOnValues();
                    List<String> constraints;
                    String depenValuexd = null;
                    if (dependingOnValues != null) {
                        constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
                        ListIterator<String> lstItemsx = constraints.listIterator();
                        while (lstItemsx.hasNext()) {
                            depenValuexd = lstItemsx.next().toString();
                        }
                    }
                    final String depenValuex = depenValuexd;

                    if (dependingOn != null && depenValuex != null) {
                        ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
                        setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue(), fieldMap);
                    }
                    final String expressionString = campaignFormElement.getExpression();
                    if (expressionString != null) {
                        try {
                            final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
//                            if (type == CampaignFormElementType.YES_NO) {
//                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, (Boolean) expressionValue, null, null);
//                            } else {
                                ControlTextReadField.setValue((ControlTextReadField) dynamicField, expressionValue.toString(), null, null, null);
                           // }
                        } catch (SpelEvaluationException e) {
                            Log.e("Error evaluating expression: " + expressionString, e.getMessage());
                        }
                    }
                } else if (type == CampaignFormElementType.SECTION) {
                    dynamicLayout.addView(new ImageView(requireContext(), null, R.style.FullHorizontalDividerStyle));
                } else if (type == CampaignFormElementType.LABEL) {
                    TextView textView = new TextView(requireContext());
                    TextViewBindingAdapters.setHtmlValue(textView, campaignFormElement.getCaption());
                    dynamicLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }

            }
        }

        dayy = countr;

        if (daywise) {
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++: "+countr);
            if (countr > 0) {
                spec = mTabHost.newTabSpec("tab1").setIndicator("D1",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet1);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(0).getLayoutParams().width = 140;
            }
            if (countr > 1) {
                spec = mTabHost.newTabSpec("tab2").setIndicator("D2",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet2);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(1).getLayoutParams().width = 140;
            }
            if (countr > 2) {
                spec = mTabHost.newTabSpec("tab3").setIndicator("D3",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet3);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(2).getLayoutParams().width = 140;
            }
            if (countr > 3) {
                spec = mTabHost.newTabSpec("tab4").setIndicator("D4",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet4);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(3).getLayoutParams().width = 140;
            }

            if (countr == 6) { //>5 (four day form with summary
                spec = mTabHost.newTabSpec("tab5").setIndicator(" ",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet5);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(4).getLayoutParams().width = 140;
                mTabHost.getTabWidget().getChildAt(4).setVisibility(View.GONE);
            }

            if (countr > 4 && countr != 6) {
                spec = mTabHost.newTabSpec("tab5").setIndicator("D5",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet5);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(4).getLayoutParams().width = 140;
            }

            if (countr > 5 && countr != 6) {
                spec = mTabHost.newTabSpec("tab6").setIndicator("D6",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet6);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(5).getLayoutParams().width = 140;
            }

            if (countr > 6) {
                spec = mTabHost.newTabSpec("tab7").setIndicator("D7",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet7);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(6).getLayoutParams().width = 140;
            }

            if (countr > 7) {
                spec = mTabHost.newTabSpec("tab8").setIndicator("D8",//caption_1,
                        res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet8);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(7).getLayoutParams().width = 140;
//                mTabHost.getTabWidget().getChildAt(7).setVisibility(View.GONE);
            }

            if (countr > 8) {
                spec = mTabHost.newTabSpec("tab9").setIndicator("D9",//caption_1,
                                res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet9);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(8).getLayoutParams().width = 140;
//                mTabHost.getTabWidget().getChildAt(8).setVisibility(View.GONE);
            }
            if (countr > 9) {
                spec = mTabHost.newTabSpec("tab10").setIndicator("D10",//caption_1,
                                res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet10);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(9).getLayoutParams().width = 140;
//                mTabHost.getTabWidget().getChildAt(9).setVisibility(View.GONE);
            }
            if (countr > 10 ) {
                spec = mTabHost.newTabSpec("tab11").setIndicator("D11",//caption_1,
                                res.getDrawable(R.drawable.ic_clear_black_24dp))
                        .setContent(R.id.tabSheet11);
                mTabHost.addTab(spec);
                mTabHost.getTabWidget().getChildAt(10).getLayoutParams().width = 140;
//                mTabHost.getTabWidget().getChildAt(10).setVisibility(View.GONE);
            }

        }
        if (daywise) {
            setupTabScrolling(view);
        }
        return view;
    }


    @Override
    protected String getSubHeadingTitle() {
        if (record != null && record.getCampaign().getName() != null) {
            return record.getCampaign().getName() + " | " + record.getCampaignFormMeta().getFormName();
        }
        return super.getSubHeadingTitle();
    }


    private void setupTabScrolling(View view) {
        try {
            HorizontalScrollView tabsScroll = view.findViewById(R.id.tabs_scroll);
            ImageView leftArrow = view.findViewById(R.id.tabs_left_arrow);
            ImageView rightArrow = view.findViewById(R.id.tabs_right_arrow);

            if (tabsScroll == null || leftArrow == null || rightArrow == null) {
                return;
            }

            // Post the initial visibility update to ensure layout is measured
            tabsScroll.post(() -> updateArrowVisibility(tabsScroll, leftArrow, rightArrow));

            tabsScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                updateArrowVisibility(tabsScroll, leftArrow, rightArrow);
            });

            leftArrow.setOnClickListener(v -> scrollTabs(tabsScroll, leftArrow, rightArrow, -200));
            rightArrow.setOnClickListener(v -> scrollTabs(tabsScroll, leftArrow, rightArrow, 200));
        } catch (Exception e) {
            Log.e("TabScrolling", "Error setting up tab scrolling", e);
        }
    }

    private void updateArrowVisibility(HorizontalScrollView scrollView,
                                       ImageView leftArrow,
                                       ImageView rightArrow) {

        if (scrollView.getChildCount() == 0) {
            return;
        }

        int scrollX = scrollView.getScrollX();
        int maxScroll = Math.max(0, scrollView.getChildAt(0).getWidth() - scrollView.getWidth());

        boolean canScrollRight = scrollX < maxScroll;
        boolean hasScrolled = scrollX > 0;

        // Right arrow
        rightArrow.setVisibility(canScrollRight ? View.VISIBLE : View.GONE);

        // Left arrow:
        // Show if we've scrolled OR if the right arrow is no longer visible.
        leftArrow.setVisibility((hasScrolled || !canScrollRight) ? View.VISIBLE : View.GONE);
    }

    private void scrollTabs(HorizontalScrollView scrollView,
                            ImageView leftArrow,
                            ImageView rightArrow,
                            int distance) {

        if (distance > 0) {
            // User clicked the right arrow
            leftArrow.setVisibility(View.VISIBLE);
        }

        scrollView.smoothScrollBy(distance, 0);

        scrollView.postDelayed(() ->
                updateArrowVisibility(scrollView, leftArrow, rightArrow), 300);
    }



    protected String getDateValueString(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedDate = dateFormat.parse(input);
            // Clear time components
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDate);
            return parsedDate.toString();
        } catch (ParseException e) {
            Log.e(getClass().getName(), "Error parsing date: " + input, e);
            return null;
        }
    }


    protected String getDateValue(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedDate = dateFormat.parse(input);

            // Clear time components
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDate);
            return cal.getTime().toString();
        } catch (ParseException e) {
//            Log.e(getClass().getName(), "Error parsing date: " + input, e);
//            return null;
            try{
                String normalizedDateString = normalizeRawDateString(input); // this gives "03-08-2025"
                if (normalizedDateString != null) {
                    SimpleDateFormat fallbackFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                    return fallbackFormat.parse(normalizedDateString).toString();
                }
            }catch(Exception ee){
                Log.e(getClass().getName(), "Error parsing date: " + input, ee);
                return null;
            }
            return null;

        }
    }
    public static String normalizeRawDateString(String rawDateStr) {
        try {
            // First parse the raw string
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(rawDateStr);

            // Then format it to dd-MM-yyyy
            return formatDateToDdMMyyyy(date);
        } catch (ParseException e) {
            Log.e("DateParse", "Could not parse date: " + rawDateStr, e);
            return null;
        }
    }
    public static String formatDateToDdMMyyyy(Date date) {
        if (date == null) return null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return formatter.format(date);
    }



    @Override
    protected void prepareFragmentData(Bundle savedInstanceState) {
        record = getActivityRootData();
    }

    @Override
    protected void onLayoutBinding(FragmentCampaignDataReadLayoutBinding contentBinding) {
        if (record.getRegion() != null) {
            record.setArea(record.getRegion().getArea());
        }
        contentBinding.setData(record);
        applyGeographyLevelVisibility(contentBinding);
    }

    private void applyGeographyLevelVisibility(FragmentCampaignDataReadLayoutBinding contentBinding) {
        String geographyLevel = record.getCampaignFormMeta() != null
                ? record.getCampaignFormMeta().getGeographylevel()
                : null;

        if (geographyLevel == null) {
            return;
        }

        if (CampaignFormMetaGeographyLevel.REGION.toString().equals(geographyLevel)) {
            contentBinding.campaignFormDataRegion.setVisibility(View.GONE);
            contentBinding.campaignFormDataDistrict.setVisibility(View.GONE);
            contentBinding.campaignFormDataCommunity.setVisibility(View.GONE);
        } else if (CampaignFormMetaGeographyLevel.PROVINCE.toString().equals(geographyLevel)) {
            contentBinding.campaignFormDataDistrict.setVisibility(View.GONE);
            contentBinding.campaignFormDataCommunity.setVisibility(View.GONE);
        } else if (CampaignFormMetaGeographyLevel.DISTRICT.toString().equals(geographyLevel)) {
            contentBinding.campaignFormDataCommunity.setVisibility(View.GONE);
        }
    }

    @Override
    public int getReadLayout() {
        return R.layout.fragment_campaign_data_read_layout;
    }

    @Override
    public CampaignFormData getPrimaryData() {
        return record;
    }
}
