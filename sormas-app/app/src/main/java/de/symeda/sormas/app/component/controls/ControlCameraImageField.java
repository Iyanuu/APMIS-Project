package de.symeda.sormas.app.component.controls;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.core.content.FileProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import de.symeda.sormas.api.campaign.data.CampaignFormImageSource;
import de.symeda.sormas.api.campaign.data.CampaignFormImageValue;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.app.BuildConfig;

public class ControlCameraImageField extends ControlTextEditField {

    public interface CameraResultCallback {
        void onCameraResult(int resultCode);
    }
    private final Gson gson = new Gson();
    private List<CampaignFormImageValue> imageValues;

    private CampaignFormElement element;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private CameraResultCallback resultCallback;
    private Uri pendingOutputUri;
    private File pendingOutputFile;
    private String pendingLocalId;

    public ControlCameraImageField(android.content.Context context) {
        super(context);
            imageValues = new ArrayList<>();
    }

    public ControlCameraImageField(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
        imageValues = new ArrayList<>();

    }


    public ControlCameraImageField(android.content.Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        imageValues = new ArrayList<>();

    }

    public void configureForImageCapture(CampaignFormElement campaignFormElement,
                                         ActivityResultLauncher<Intent> launcher,
                                         CameraResultCallback callback) {
        this.element = campaignFormElement;
        this.cameraLauncher = launcher;
        this.resultCallback = callback;

        input.setFocusable(false);
        input.setFocusableInTouchMode(false);
        input.setCursorVisible(false);
        input.setLongClickable(false);
        input.setKeyListener(null);
//        input.setOnClickListener(v -> launchBackCameraOnly());

//        if (cameraLauncher == null && getContext() instanceof ComponentActivity activity) {
//            cameraLauncher = activity.registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> onCameraResult(result.getResultCode())
//            );
//        }

        refreshDisplayText();
    }

    public void launchCamera() {
        launchBackCameraOnly(); // this method already exists and is private; you can make it public or keep private
    }

    @Override
    protected String getFieldValue() {
        if (imageValues == null || imageValues.isEmpty()) {
            return null;
        }

        boolean multiple = isMultipleAllowed();
        if (!multiple && imageValues.size() == 1) {
            return gson.toJson(imageValues.get(0).toMap());
        }

        return gson.toJson(imageValues.stream().map(CampaignFormImageValue::toMap).collect(Collectors.toList()));
    }

    @Override
    protected void setFieldValue(String value) {
        if (imageValues == null) imageValues = new ArrayList<>();
        imageValues.clear();

        if (value != null && !value.trim().isEmpty()) {
            try {
                Object parsed = gson.fromJson(value, Object.class);
                if (parsed instanceof Map<?, ?> map) {
                    CampaignFormImageValue v = CampaignFormImageValue.fromMap(map);
                    if (v != null) imageValues.add(v);
                } else if (parsed instanceof List<?> list) {
                    for (Object o : list) {
                        if (o instanceof Map<?, ?> map) {
                            CampaignFormImageValue v = CampaignFormImageValue.fromMap(map);
                            if (v != null) imageValues.add(v);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        refreshDisplayText();
    }

    private void launchBackCameraOnly() {
        if (cameraLauncher == null) {
            Toast.makeText(getContext(), "Camera is not available here", Toast.LENGTH_SHORT).show();
            return;
        }
      if (imageValues == null || imageValues.isEmpty()) imageValues = new ArrayList<>();

        if (isMultipleAllowed() && imageValues.size() >= getMaxCount()) {
            Toast.makeText(getContext(), "Maximum image count reached", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            pendingLocalId = UUID.randomUUID().toString();
            File baseDir = new File(
                    getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "campaign_images"
            );
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                Toast.makeText(getContext(), "Unable to prepare storage", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingOutputFile = new File(baseDir, pendingLocalId + ".jpg");
            pendingOutputUri = FileProvider.getUriForFile(
                    getContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    pendingOutputFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingOutputUri);

            // Force back camera where device camera app supports it
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 0);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extras.LENS_FACING_BACK", 0);

            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getContext().getPackageManager()) == null) {
                Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
                return;
            }

            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }
    public void handleCameraResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK || pendingOutputFile == null || !pendingOutputFile.exists()) {
            clearPending();
            return;
        }

        CampaignFormImageValue value = new CampaignFormImageValue();
        value.setLocalId(pendingLocalId);
        value.setOriginalFileName(pendingOutputFile.getName());
        value.setGeneratedFileName(pendingOutputFile.getName());
        value.setMimeType("image/jpeg");
        value.setSource(CampaignFormImageSource.MOBILE_CAMERA);
        value.setCapturedAt(System.currentTimeMillis());
        value.setDeviceType(Build.MODEL);
        value.setOsVersion(Build.VERSION.RELEASE);
        value.setOriginalSizeBytes(pendingOutputFile.length());
        value.setCompressedSizeBytes(pendingOutputFile.length());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pendingOutputFile.getAbsolutePath(), options);
        if (options.outWidth > 0) value.setWidth(options.outWidth);
        if (options.outHeight > 0) value.setHeight(options.outHeight);

        if (imageValues == null || imageValues.isEmpty()) imageValues = new ArrayList<>();
        if (!isMultipleAllowed()) {
            imageValues.clear();
        }
        imageValues.add(value);

        refreshDisplayText();
        onValueChanged();
        clearPending();
    }

    private void refreshDisplayText() {
        if (imageValues == null || imageValues.isEmpty()) {
            input.setText("");
            input.setHint("Tap to capture image (back camera)");
        } else if (imageValues.size() == 1) {
            input.setText("1 image captured");
        } else {
            input.setText(imageValues.size() + " images captured");
        }
    }

    private int getMaxCount() {
        if (element == null || element.getImageMaxCount() == null || element.getImageMaxCount() < 1) {
            return 1;
        }
        return Math.min(5, element.getImageMaxCount());
    }

    private boolean isMultipleAllowed() {
        boolean multipleFlag = element != null && Boolean.TRUE.equals(element.getImageMultiple());
        return multipleFlag || getMaxCount() > 1;
    }

    private void clearPending() {
        pendingOutputUri = null;
        pendingOutputFile = null;
        pendingLocalId = null;
    }

    public void onCameraActivityResult(int resultCode) {
        handleCameraResult(resultCode);
    }
}