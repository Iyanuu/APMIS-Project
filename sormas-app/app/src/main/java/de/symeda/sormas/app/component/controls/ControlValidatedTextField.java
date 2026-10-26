/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package de.symeda.sormas.app.component.controls;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Objects;

/**
 * Read-only text control whose value is supplied by a campaign-form expression.
 *
 * Keeping this separate from {@link ControlTextEditField} prevents calculated
 * text behavior from changing the existing editable TEXT form element.
 */
public class ControlValidatedTextField extends ControlTextEditField {

    public ControlValidatedTextField(Context context) {
        super(context);
    }

    public ControlValidatedTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlValidatedTextField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Applies a calculated result, including the empty string used to clear the
     * field. The equality guard avoids triggering value listeners recursively
     * when recalculation produces the same label.
     */
    public void setExpressionValue(Object expressionValue) {
        String newValue = expressionValue == null ? "" : expressionValue.toString();
        String currentValue = getValue() == null ? "" : getValue();

        if (!Objects.equals(currentValue, newValue)) {
            ControlTextEditField.setValue(this, newValue);
        }
    }
}

