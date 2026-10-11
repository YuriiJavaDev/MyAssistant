package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.files.ScanCache;

import java.io.File;
import java.util.function.Consumer;

/** Gets a photo to scan from the system camera or the photo picker. Create it while the host is being created. */
final class ScanPhotoSource {

    private final Context context;
    private final ActivityResultLauncher<Uri> cameraLauncher;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickerLauncher;

    /** onCancelled runs when the user leaves the camera or the picker without choosing a photo. */
    ScanPhotoSource(ActivityResultCaller host, Context context, Consumer<Uri> onPhoto, Runnable onCancelled) {
        this.context = context;
        cameraLauncher = host.registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                onPhoto.accept(ScanCache.uriFor(context, ScanCache.captureFile(context)));
            } else {
                onCancelled.run();
            }
        });
        pickerLauncher = host.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                onPhoto.accept(uri);
            } else {
                onCancelled.run();
            }
        });
    }

    void takePhoto() {
        File captureFile = ScanCache.captureFile(context);
        captureFile.delete();
        try {
            cameraLauncher.launch(ScanCache.uriFor(context, captureFile));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.scan_error_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    void pickPhoto() {
        pickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}
