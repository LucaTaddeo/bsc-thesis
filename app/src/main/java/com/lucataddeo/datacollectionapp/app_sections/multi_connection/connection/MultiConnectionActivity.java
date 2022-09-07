package com.lucataddeo.datacollectionapp.app_sections.multi_connection.connection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.lucataddeo.datacollectionapp.model.*;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.FileWriter;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.sensor_usage.MultiSensorUsageActivity;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements.Measurement;
import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.lucataddeo.datacollectionapp.R;
import com.lucataddeo.datacollectionapp.bluetooth.MdsRx;
import com.lucataddeo.datacollectionapp.app_sections.dfu.ScannerFragment;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MultiConnectionActivity extends AppCompatActivity implements ScannerFragment.DeviceSelectionListener {

    private final String TAG = MultiConnectionActivity.class.getSimpleName();
    private final ArrayList<RxBleDevice> rxBleDeviceArrayList = new ArrayList<>();
    @BindView(R.id.multiConnection_connect_Tv)
    TextView mMultiConnectionConnectTv;
    @BindView(R.id.multiConnection_add_device)
    TextView mMultiConnectionAddDevice;
    @BindView(R.id.multiConnection_status_tv)
    TextView mMultiConnectionStatusTv;
    private ScannerFragment scannerFragment;
    private CompositeDisposable mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_connection);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Multi Connection");
        }

        mCompositeSubscription = new CompositeDisposable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (FileWriter.initializeParentFolderForLogs()) {
                FileWriter.fakeInitializeSensors();
                FileWriter.writeMeasurementToJsonFile(
                        "serialA",
                        new Measurement(
                                new LinearAcceleration(
                                        new LinearAcceleration.Body(124124124,
                                                new LinearAcceleration.Array[]{
                                                        new LinearAcceleration.Array(1, 2, 3)},
                                                new LinearAcceleration.Headers(2)
                                        )
                                )
                        )
                );
                FileWriter.writeMeasurementToJsonFile("serialA", new Measurement(new AngularVelocity(new AngularVelocity.Body(124124124, new AngularVelocity.Array[]{new AngularVelocity.Array(1, 2, 3)}, new AngularVelocity.Headers(2)))));
                FileWriter.writeMeasurementToJsonFile("serialB", new Measurement(new LinearAcceleration(new LinearAcceleration.Body(124124124, new LinearAcceleration.Array[]{new LinearAcceleration.Array(1, 2, 3)}, new LinearAcceleration.Headers(2)))));
                FileWriter.writeMeasurementToJsonFile("serialA", new Measurement(new MagneticField(new MagneticField.Body(124124124, new MagneticField.Array[]{new MagneticField.Array(1, 2, 3)}, new MagneticField.Headers(2)))));
                FileWriter.writeMeasurementToJsonFile("serialA", new Measurement(new LinearAcceleration(new LinearAcceleration.Body(124124124, new LinearAcceleration.Array[]{new LinearAcceleration.Array(1, 2, 3)}, new LinearAcceleration.Headers(2)))));
                FileWriter.closeAllArrays();
            }
        }

        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MdsConnectedDevice>() {
            int connectedDevices = 0;

            @Override
            public void accept(MdsConnectedDevice mdsConnectedDevice) {
                if (mdsConnectedDevice.getConnection() != null) {

                    if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                        MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();

                        MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(), mdsDeviceInfoNewSw.getDescription(), mdsDeviceInfoNewSw.getSerial(), mdsDeviceInfoNewSw.getSw()));

                        connectedDevices++;
                        Log.e(TAG, "call: " + connectedDevices + "th device connected");

                    } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                        MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();

                        MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(mdsDeviceInfoOldSw.getAddressInfoOld(), mdsDeviceInfoOldSw.getDescription(), mdsDeviceInfoOldSw.getSerial(), mdsDeviceInfoOldSw.getSw()));

                        connectedDevices++;
                        Log.e(TAG, "call: " + connectedDevices + "th device connected");

                    }

                    if (connectedDevices == rxBleDeviceArrayList.size()) {
                        startActivity(new Intent(MultiConnectionActivity.this, MultiSensorUsageActivity.class));
                    }
                }
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.dispose();
    }

    @OnClick({R.id.multiConnection_connect_Tv, R.id.multiConnection_add_device})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.multiConnection_add_device:
                scannerFragment = new ScannerFragment();
                scannerFragment.show(getSupportFragmentManager(), ScannerFragment.class.getName());
                break;
            case R.id.multiConnection_connect_Tv:
                if (rxBleDeviceArrayList.size() < 1) {
                    Toast.makeText(this, "Add at least one sensor!", Toast.LENGTH_SHORT).show();
                } else {
                    mMultiConnectionStatusTv.setText("Connecting to sensors...");
                    blockUI();
                    for (RxBleDevice device : rxBleDeviceArrayList) {
                        Mds.builder().build(MultiConnectionActivity.this).connect(device.getMacAddress(), null);
                    }

                    // Initialize a folder to contain all logs files, and initialize all logs files for the sensors
                    // TODO: check if here it works, otherwise move in MultiSensorUsageActivity
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileWriter.initializeParentFolderForLogs();
                        FileWriter.initializeSensorsLogFiles();
                    }
                }
                break;
        }
    }

    @Override
    public void onDeviceSelected(RxBleDevice device) {
        Log.d(TAG, "onDeviceSelected: " + device.getName() + " " + device.getMacAddress()/*+ isAddDevice1Pressed*/);

        scannerFragment.dismiss();
        if (!rxBleDeviceArrayList.contains(device)) {
            rxBleDeviceArrayList.add(device);
            LinearLayout sensorsContainer = findViewById(R.id.devicesText);
            if (rxBleDeviceArrayList.size() == 1) {
                mMultiConnectionStatusTv.setText("Tap on a sensor to deselect it!");
                TextView noDevicesTextView = findViewById(R.id.noDevices_text);
                if (noDevicesTextView != null) sensorsContainer.removeView(noDevicesTextView);
            }

            TextView sensorTextView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(0, 10, 0, 10);
            sensorTextView.setLayoutParams(params);
            sensorTextView.setText(device.getName());
            sensorTextView.setTextSize(15);
            sensorTextView.setTextColor(Color.WHITE);
            sensorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sensorsContainer.removeView(v);
                    rxBleDeviceArrayList.remove(device);
                    Toast.makeText(MultiConnectionActivity.this, device.getName() + " removed", Toast.LENGTH_LONG).show();
                }
            });

            if (sensorsContainer != null) {
                sensorsContainer.addView(sensorTextView);
            }

        } else {
            Toast.makeText(MultiConnectionActivity.this, "Can't add same sensor multiple times!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void blockUI() {
        mMultiConnectionConnectTv.setEnabled(false);
        mMultiConnectionConnectTv.setTextColor(getResources().getColor(R.color.colorGrey));
        mMultiConnectionConnectTv.setBackground(getResources().getDrawable(R.drawable.grey_stroke));
        mMultiConnectionAddDevice.setEnabled(false);
        mMultiConnectionAddDevice.setTextColor(getResources().getColor(R.color.colorGrey));
        mMultiConnectionAddDevice.setBackground(getResources().getDrawable(R.drawable.grey_stroke));
    }

    private void clearUI() {
        mMultiConnectionConnectTv.setEnabled(true);
        mMultiConnectionConnectTv.setTextColor(getResources().getColor(R.color.colorText));
        mMultiConnectionConnectTv.setBackground(getResources().getDrawable(R.drawable.white_stroke));

        mMultiConnectionAddDevice.setEnabled(true);
        mMultiConnectionAddDevice.setTextColor(getResources().getColor(R.color.colorText));
        mMultiConnectionAddDevice.setBackground(getResources().getDrawable(R.drawable.white_stroke));

        mMultiConnectionConnectTv.setBackground(ContextCompat.getDrawable(this, R.drawable.black_stroke));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
