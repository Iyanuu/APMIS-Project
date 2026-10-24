/*
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
 */

package de.symeda.sormas.app.component.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import de.symeda.sormas.api.utils.FieldConstraints;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.component.VisualState;
import de.symeda.sormas.app.component.VisualStateControlType;

/**
 * A textarea / multi‑line text input control.
 * Binds only to {@link String} values; numeric or other typed bindings are not supported.
 */
public class ControlTextBoxField extends ControlPropertyEditField<String> {

    // Views

    protected EditText input;

    // Attributes

    private boolean singleLine;
    private int maxLines;
    private int maxLength;
    private int minLength;
    private boolean textArea;
    private int inputType = InputType.TYPE_CLASS_TEXT;

    // Listeners

    protected InverseBindingListener inverseBindingListener;
    private OnClickListener onClickListener;

    // Constructors

    public ControlTextBoxField(Context context) {
        super(context);
    }

    public ControlTextBoxField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlTextBoxField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Instance methods

    public void setCursorToRight() {
        input.setSelection(input.getText().length());
    }

    /**
     * Handles the IME "next" action to move focus to the next field.
     */
    private void setUpOnEditorActionListener() {
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int definedActionId = v.getImeActionId();
                if (definedActionId == EditorInfo.IME_ACTION_NONE) {
                    return false;
                }

                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    int id = getNextFocusForwardId();
                    if (id != NO_ID) {
                        View nextView = v.getRootView().findViewById(id);
                        if (nextView != null && nextView.getVisibility() == VISIBLE) {
                            if (nextView instanceof ControlTextBoxField) {
                                requestFocusForContentView(nextView);
                            } else if (nextView instanceof ControlPropertyField) {
                                ((ControlPropertyField) nextView).requestFocusForContentView(nextView);
                            } else {
                                nextView.requestFocus();
                            }
                        }
                    }
                    return true;
                }

                return false;
            }
        });
    }

    private void setUpOnFocusChangeListener() {
        input.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!v.isEnabled()) {
                    return;
                }

                showOrHideNotifications(hasFocus);

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm == null) {
                    return;
                }

                if (hasFocus) {
                    changeVisualState(VisualState.FOCUSED);
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                    // Prevent the content from being automatically selected
                    input.setSelection(input.getText().length(), input.getText().length());
                    if (onClickListener != null) {
                        input.setOnClickListener(onClickListener);
                    }
                } else {
                    changeVisualState(hasError ? VisualState.ERROR : VisualState.NORMAL);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    input.setOnClickListener(null);
                }
            }
        });
    }

    private void initializeOnClickListener() {
        if (onClickListener != null) {
            return;
        }

        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isEnabled()) {
                    return;
                }

                showOrHideNotifications(v.hasFocus());

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm == null) {
                    return;
                }

                if (v.hasFocus()) {
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        };
    }

    // Overrides

    @Override
    public String getValue() {
        return super.getValue().toString();
    }

    @Override
    protected String getFieldValue() {
        if (input.getText() == null) {
            return null;
        }
        return input.getText().toString();
    }

    @Override
    protected void setFieldValue(String value) {
        input.setText(value == null ? "" : value);
    }


    protected void setReadOnlyFieldValue(String value) {
        input.setEnabled(false);
        input.setText(value == null ? "" : value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); // must be called first
        input.setEnabled(enabled);
        label.setEnabled(enabled);
    }

    @Override
    public void setHint(String hint) {
        this.hint = hint;
        input.setHint(hint);
    }

    @Override
    protected void initialize(Context context, AttributeSet attrs, int defStyle) {
        if (attrs == null) {
            return;
        }

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ControlTextEditField, 0, 0);

        try {
            textArea = a.getBoolean(R.styleable.ControlTextEditField_textArea, false);

            singleLine = textArea
                    ? false
                    : a.getBoolean(R.styleable.ControlTextEditField_singleLine, true);

            maxLines = a.getInt(
                    R.styleable.ControlTextEditField_maxLines,
                    textArea ? 5 : 1);

            maxLength = a.getInt(
                    R.styleable.ControlTextEditField_maxLength,
                    textArea ? FieldConstraints.CHARACTER_LIMIT_BIG
                            : FieldConstraints.CHARACTER_LIMIT_UUID_MIN);

            minLength = a.getInt(
                    R.styleable.ControlTextEditField_minLength,
                    textArea ? FieldConstraints.CHARACTER_LIMIT_BIG
                            : FieldConstraints.CHARACTER_LIMIT_DEFAULT);

            inputType = a.getInt(
                    R.styleable.ControlTextEditField_inputType,
                    InputType.TYPE_CLASS_TEXT);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Unable to inflate layout in " + getClass().getName());
        }

            inflater.inflate(R.layout.control_textbox_edit_layout, this);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initInput(false);
    }

    /**
     * Wires up the inner {@link EditText}. Only text semantics apply.
     *
     * @param isRequired whether an empty value should be considered an error
     */
    protected void initInput(boolean isRequired) {
        input = findViewById(R.id.text_input);
        input.setTextAlignment(getTextAlignment());
        if (getTextAlignment() == View.TEXT_ALIGNMENT_GRAVITY) {
            input.setGravity(getGravity());
        }

        applyInputType();
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setHorizontallyScrolling(false);

        applySingleLineSettings();
        applyMaxLengthFilter();

        required = isRequired;

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (inverseBindingListener != null) {
                    inverseBindingListener.onChange();
                }
                onValueChanged();
            }
        });

        addValueChangedListener(new ValueChangeListener() {
            @Override
            public void onChange(ControlPropertyField field) {
                if (!isLiveValidationDisabled()) {
                    ((ControlTextBoxField) field).setErrorIfEmptyRange();
                }
            }
        });

        setUpOnEditorActionListener();
        setUpOnFocusChangeListener();
        initializeOnClickListener();
    }

    private void applyInputType() {
        int type = getInputType();

        // Only augment the default text type; respect explicit values from XML.
        if ((type & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
            type |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
            if (!singleLine) {
                type |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            }
        }
        input.setInputType(type);
    }

    private void applySingleLineSettings() {
        input.setMinLines(1);                 // start at one line and grow
        input.setMaxLines(getMaxLines());     // grow up to this many lines, then scroll (0 = unlimited)
        input.setVerticalScrollBarEnabled(true);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setHorizontallyScrolling(false);
    }

    private void applyMaxLengthFilter() {
        if (getMaxLength() >= 0) {
            input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(getMaxLength()) });
        } else {
            input.setFilters(new InputFilter[0]);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (getHint() == null) {
            setHint(getPrefixCaption());
        }
    }

    @Override
    protected void requestFocusForContentView(View nextView) {
        if (nextView instanceof ControlTextBoxField) {
            ControlTextBoxField field = (ControlTextBoxField) nextView;
            if (field.input != null) {
                field.input.requestFocus();
                field.setCursorToRight();
            }
        }
    }

    @Override
    protected void changeVisualState(VisualState state) {
        if (getUserEditRight() != null && !ConfigProvider.hasUserRight(getUserEditRight())) {
            state = VisualState.DISABLED;
        }

        if (this.visualState == state) {
            return;
        }

        visualState = state;

        int labelColor = ContextCompat.getColor(getContext(), state.getLabelColor());
        Drawable drawable = ContextCompat.getDrawable(getContext(),
                state.getBackground(VisualStateControlType.TEXT_FIELD));
        int textColor = ContextCompat.getColor(getContext(), state.getTextColor());
        int hintColor = ContextCompat.getColor(getContext(), state.getHintColor());

        if (drawable != null) {
            drawable = drawable.mutate();
        }

        label.setTextColor(labelColor);
        setBackground(drawable);

        if (state != VisualState.ERROR) {
            input.setTextColor(textColor);
            input.setHintTextColor(hintColor);
        }

        setEnabled(state != VisualState.DISABLED);
    }

    @Override
    public void setBackgroundResource(int resId) {
        setBackgroundResourceFor(input, resId);
    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundFor(input, background);
    }

    // ---------------------------------------------------------------------
    // Data binding — String only
    // ---------------------------------------------------------------------

    @BindingAdapter("value")
    public static void setValue(ControlTextBoxField view, @Nullable String text) {
        if (view == null) {
            return;
        }
        if (text == null || text.trim().isEmpty() || "NaN".equalsIgnoreCase(text)) {
            view.setFieldValue("");
        } else {
            view.setFieldValue(text);
        }
    }

    @BindingAdapter("value")
    public static void setValueReadOnly(ControlTextBoxField view, @Nullable String text) {
        if (view == null) {
            return;
        }
        if (text == null || text.trim().isEmpty() || "NaN".equalsIgnoreCase(text)) {
            view.setReadOnlyFieldValue("");
        } else {
            view.setReadOnlyFieldValue(text);
        }
    }



    @BindingAdapter({"value", "errorMessage"})
    public static void setValue(ControlTextBoxField view, @Nullable String text, String errorMessage) {
        if (view == null) {
            return;
        }
        view.setFieldValue(text == null ? "" : text);
    }

    @InverseBindingAdapter(attribute = "value", event = "valueAttrChanged")
    public static String getValue(ControlTextBoxField view) {
        return view.getFieldValue();
    }

    @BindingAdapter("valueAttrChanged")
    public static void setListener(ControlTextBoxField view, InverseBindingListener listener) {
        view.inverseBindingListener = listener;
    }

    // Getters & setters

    public String getHint() {
        return input.getHint() == null ? null : input.getHint().toString();
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
        if (input != null) {
            applySingleLineSettings();
        }
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
        if (input != null) {
            applyInputType();
        }
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        if (input != null) {
            applySingleLineSettings();
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (input != null) {
            applyMaxLengthFilter();
        }
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public EditText getInput() {
        return input;
    }

    public void setInput(EditText input) {
        this.input = input;
    }

    public void setTextArea(boolean textArea) {
        this.textArea = textArea;
    }
}