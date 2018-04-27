package com.example.fotoapparattest;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.error.CameraErrorListener;
import io.fotoapparat.exception.camera.CameraException;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.LensPositionSelectorsKt.front;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //    boolean CameraPermission = false;
    boolean isFront = false;
    CameraView cameraView;
    Fotoapparat fotoapparat;
    ConstraintLayout constraint_main_camera;
    ConstraintLayout constraint_main_save;
    ImageView tack_picture;
    ImageView camera_switch;
    TextView textView_main_confirm;
    TextView textView_main_cancel;
//    FocusView focusView;
//    ImageView imageView_main_preview;
    private static final int RequestPermissionCode = 1;
    private CameraConfiguration cameraConfiguration = CameraConfiguration
            .builder()
            .photoResolution(standardRatio(
                    highestResolution()
            ))
            .focusMode(firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),
                    fixed()
            ))
            .flash(firstAvailable(
                    autoRedEye(),
                    autoFlash(),
                    torch(),
                    off()
            ))
            .previewFpsRange(highestFps())
            .sensorSensitivity(highestSensorSensitivity())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        constraint_main_camera = findViewById(R.id.constraint_main_camera);
        constraint_main_save = findViewById(R.id.constraint_main_save);
        tack_picture = findViewById(R.id.tack_picture);
        camera_switch = findViewById(R.id.camera_switch);
        textView_main_confirm = findViewById(R.id.textView_main_confirm);
        textView_main_cancel = findViewById(R.id.textView_main_cancel);
        checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Objects.equals(fotoapparat, null)) {
            fotoapparat.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Objects.equals(fotoapparat, null)) {
            fotoapparat.stop();
        }
    }

    private void initCamera() {
        cameraView = findViewById(R.id.camera_view);


//        fotoapparat = Fotoapparat
//                .with(this)
//                .into(cameraView)           // view which will draw the camera preview
//                .previewScaleType(ScaleType.CenterCrop)  // we want the preview to fill the view
////                .photoSize(highestResolution())   // we want to have the biggest photo possible
//                .lensPosition(back())       // we want back camera
//                .focusMode(firstAvailable(  // (optional) use the first focus mode which is supported by device
////                        continuousFocus(),
//                        autoFocus(),        // in case if continuous focus is not available on device, auto focus will be used
//                        fixed()             // if even auto focus is not available - fixed focus mode will be used
//                ))
////                .flash(firstAvailable(      // (optional) similar to how it is done for focus mode, this time for flash
////                        autoRedEye(),
////                        autoFlash(),
////                        torch(),
////                        off()
////                ))
////                .frameProcessor(myFrameProcessor)   // (optional) receives each frame from preview stream
//                .logger(loggers(            // (optional) we want to log camera events in 2 places at once
//                        logcat(),           // ... in logcat
//                        fileLogger(this)    // ... and to file
//                ))
//                .previewFpsRange(highestFps())
//                .sensorSensitivity(highestSensorSensitivity())
//                .build();
        fotoapparat = createFotoapparat();
//        CameraConfiguration cameraConfiguration = new CameraConfiguration();
        camera_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFront = !isFront;
                if (isFront) {
                    fotoapparat.switchTo(front(), new CameraConfiguration());
                } else {
                    fotoapparat.switchTo(back(), new CameraConfiguration());
                }
            }
        });
        tack_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PhotoResult photoResult = fotoapparat.takePicture();
                photoResult.toBitmap(scaled(0.25f))
                        .whenDone(new WhenDoneListener<BitmapPhoto>() {
                            @Override
                            public void whenDone(BitmapPhoto bitmapPhoto) {
                                if (Objects.equals(bitmapPhoto, null)) {
                                    return;
                                }
                                ImageView imageView_main_preview = findViewById(R.id.imageView_main_preview);
                                Glide.with(MainActivity.this)
                                        .load(bitmapPhoto.bitmap)
                                        .into(imageView_main_preview);
                                imageView_main_preview.setRotation(-bitmapPhoto.rotationDegrees);
                                Log.i(TAG, "bitmapPhoto.rotationDegrees: " + bitmapPhoto.rotationDegrees);
                                constraint_main_save.setVisibility(View.VISIBLE);
                            }
                        });
                constraint_main_camera.setVisibility(View.INVISIBLE);
                textView_main_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        photoResult.saveToFile(new File(
                                getExternalFilesDir("photos"),
                                "photo.jpg"
                        ));
                        constraint_main_save.setVisibility(View.INVISIBLE);
                        constraint_main_camera.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
                textView_main_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        constraint_main_save.setVisibility(View.INVISIBLE);
                        constraint_main_camera.setVisibility(View.VISIBLE);
                    }
                });
    }
    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
//                .focusView(focusView)
                .previewScaleType(ScaleType.CenterCrop)
//                .photoResolution(highestResolution())
                .lensPosition(back())
//                .frameProcessor(new SampleFrameProcessor())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(new CameraErrorListener() {
                    @Override
                    public void onError(@NotNull CameraException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .photoResolution(standardRatio(
                        highestResolution()
                ))
                .build();
    }
    private void tackPicture(){
        PhotoResult photoResult = fotoapparat.takePicture();
        photoResult.toBitmap(scaled(0.25f))
                .whenDone(new WhenDoneListener<BitmapPhoto>() {
                    @Override
                    public void whenDone(BitmapPhoto bitmapPhoto) {
                        if (Objects.equals(bitmapPhoto, null)) {
                            return;
                        }
                        ImageView imageView_main_preview = findViewById(R.id.imageView_main_preview);
                        Glide.with(MainActivity.this)
                                .load(bitmapPhoto.bitmap)
                                .into(imageView_main_preview);
                        imageView_main_preview.setRotation(-bitmapPhoto.rotationDegrees);
                        constraint_main_save.setVisibility(View.VISIBLE);
                    }
                });
    }
    private void savePhoto(){
//        photoResult.saveToFile(new File(
//                getExternalFilesDir("photos"),
//                "photo.jpg"
//        ));
//        constraint_main_save.setVisibility(View.INVISIBLE);
//        constraint_main_camera.setVisibility(View.VISIBLE);
    }
    private void cancelSave(){
        constraint_main_save.setVisibility(View.INVISIBLE);
        constraint_main_camera.setVisibility(View.VISIBLE);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkAllPermission()) {
                requestPermission();
            } else {
                initCamera();
//                CameraPermission = true;
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        CAMERA,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE,
                        INTERNET,
                        ACCESS_FINE_LOCATION,
                        ACCESS_NETWORK_STATE,
                        //check more permissions if you want
//                     ........


                }, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadExternalStatePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadWriteStatePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean InternetPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessFineLocationPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessNetworkStatePermission = grantResults[5] == PackageManager.PERMISSION_GRANTED;

//                    .......


                    if (CameraPermission && ReadExternalStatePermission && ReadWriteStatePermission && InternetPermission && AccessFineLocationPermission && AccessNetworkStatePermission) {
                        initCamera();
                        fotoapparat.start();
//                        Toast.makeText(MainActivity.this, "Permissions acquired", Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(MainActivity.this, "One or more permissions denied", Toast.LENGTH_LONG).show();
                        requestPermission();
                    }
                }

                break;
            default:
                break;
        }
    }

    public boolean checkAllPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int sixPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
//        .....


        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                sixPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

}
