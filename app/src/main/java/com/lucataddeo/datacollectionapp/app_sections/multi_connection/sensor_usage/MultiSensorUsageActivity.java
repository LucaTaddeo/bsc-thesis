package com.lucataddeo.datacollectionapp.app_sections.multi_connection.sensor_usage;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import com.google.gson.Gson;
import com.lucataddeo.datacollectionapp.model.MagneticField;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.FileWriter;
import com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements.Measurement;
import com.lucataddeo.datacollectionapp.app_sections.main_view.MainViewActivity;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;
import com.lucataddeo.datacollectionapp.BaseActivity;
import com.lucataddeo.datacollectionapp.R;
import com.lucataddeo.datacollectionapp.bluetooth.MdsRx;
import com.lucataddeo.datacollectionapp.model.AngularVelocity;
import com.lucataddeo.datacollectionapp.model.LinearAcceleration;
import com.lucataddeo.datacollectionapp.model.MdsConnectedDevice;
import com.lucataddeo.datacollectionapp.utils.FormatHelper;
import com.lucataddeo.datacollectionapp.utils.ThrowableToastingAction;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import java.util.ArrayList;

// TODO: remove commented code
public class MultiSensorUsageActivity extends BaseActivity implements MultiSensorUsageContract.View {

    private final String TAG = MultiSensorUsageActivity.class.getSimpleName();
    private final String LINEAR_ACC_PATH = "Meas/Acc/26";
    private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/26";
    private final String MAGNETIC_FIELD_PATH = "Meas/Magn/26";
    @BindView(R.id.multiSensorUsage_numberOfSensors)
    TextView mMultiSensorUsageNumberOfSensorsTextView;
    @BindView(R.id.multiSensorUsage_linearAcc_textView)
    TextView mMultiSensorUsageLinearAccTextView;
    @BindView(R.id.multiSensorUsage_linearAcc_switch)
    SwitchCompat mMultiSensorUsageLinearAccSwitch;
    @BindView(R.id.multiSensorUsage_linearAcc_containerLl)
    LinearLayout mMultiSensorUsageLinearAccContainerLl;
    @BindView(R.id.multiSensorUsage_angularVelocity_textView)
    TextView mMultiSensorUsageAngularVelocityTextView;
    @BindView(R.id.multiSensorUsage_angularVelocity_switch)
    SwitchCompat mMultiSensorUsageAngularVelocitySwitch;
    @BindView(R.id.multiSensorUsage_angularVelocity_containerLl)
    LinearLayout mMultiSensorUsageAngularVelocityContainerLl;
    @BindView(R.id.multiSensorUsage_magneticField_textView)
    TextView mMultiSensorUsageMagneticFieldTextView;
    @BindView(R.id.multiSensorUsage_magneticField_switch)
    SwitchCompat mMultiSensorUsageMagneticFieldSwitch;
    @BindView(R.id.multiSensorUsage_magneticField_containerLl)
    LinearLayout mMultiSensorUsageMagneticFieldContainerLl;
    JsonWriter linearAccelerationJsonWriter = null;
    JsonWriter angularVelocityJsonWriter = null;
    JsonWriter magneticFieldJsonWriter = null;
    private MultiSensorUsagePresenter mPresenter;
    private CompositeDisposable mCompositeSubscription;
    private ArrayList<MdsSubscription> mMdsLinearAccSubscriptions = new ArrayList<>();
    private ArrayList<MdsSubscription> mMdsAngularVelocitySubscriptions = new ArrayList<>();
    private ArrayList<MdsSubscription> mMdsMagneticFieldSubscriptions = new ArrayList<>();

    // TODO: check that this works!!
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileWriter.closeAllArrays();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_sensor_usage);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Multi Sensor Usage");
        }

        mPresenter = new MultiSensorUsagePresenter(this);
        mCompositeSubscription = new CompositeDisposable();

        int numberOfConnectedSensors = MovesenseConnectedDevices.getConnectedDevices().size();
        mMultiSensorUsageNumberOfSensorsTextView.setText("Connected Sensors: " + numberOfConnectedSensors);

        mCompositeSubscription.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Consumer<MdsConnectedDevice>() {
                    @Override
                    public void accept(MdsConnectedDevice mdsConnectedDevice) {
                        if (mdsConnectedDevice.getConnection() == null) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(MultiSensorUsageActivity.this, MainViewActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }, 1000);
                        }
                    }
                }, new ThrowableToastingAction(this)));
    }

    /**
     * Linear Acceleration Switch
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.multiSensorUsage_linearAcc_switch)
    public void onLinearAccCheckedChange(final CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked) {
                Log.d(TAG, "=== Linear Acceleration Subscribe ===");

                //linearAccelerationJsonWriter = createFile("LinearAcceleration");

                for (MovesenseDevice device : MovesenseConnectedDevices.getConnectedDevices()) {
                    mMdsLinearAccSubscriptions.add(Mds.builder().build(this).subscribe("suunto://MDS/EventListener",
                            FormatHelper.formatContractToJson(device
                                    .getSerial(), LINEAR_ACC_PATH), new MdsNotificationListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onNotification(String s) {
                                    LinearAcceleration linearAccelerationData = new Gson().fromJson(
                                            s, LinearAcceleration.class);

                                    if (linearAccelerationData != null) {
                                        LinearAcceleration.Array arrayData = linearAccelerationData.body.array[0];
                                        if (Math.abs(arrayData.x) > 2 || Math.abs(arrayData.y) > 2 || Math.abs(arrayData.z) > 2) {
//                                            appendToFile(new HashMap<String, Double>() {{
//                                                put("X", arrayData.x);
//                                                put("Y", arrayData.y);
//                                                put("Z", arrayData.z);
//                                            }}, device.getSerial(), linearAccelerationJsonWriter);
                                            FileWriter.writeMeasurementToJsonFile(device.getSerial(), new Measurement(linearAccelerationData));
                                        }
                                    }
                                }

                                @Override
                                public void onError(MdsException e) {
                                    buttonView.setChecked(false);
                                    Toast.makeText(MultiSensorUsageActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            } else {
                for (MdsSubscription subscription : mMdsLinearAccSubscriptions)
                    subscription.unsubscribe();
                if (linearAccelerationJsonWriter != null)
//                    closeFile(linearAccelerationJsonWriter, MultiSensorUsageActivity.this);
                Log.d(TAG, "=== Linear Acceleration Unsubscribe ===");
            }
        } catch (Exception e) {
            if (linearAccelerationJsonWriter != null)
//                closeFile(linearAccelerationJsonWriter, MultiSensorUsageActivity.this);
            throw e;
        }
    }


    /**
     * Angular Velocity Switch
     *
     * @param buttonView
     * @param isChecked
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @OnCheckedChanged(R.id.multiSensorUsage_angularVelocity_switch)
    public void onAngularVelocityCheckedChange(final CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked) {
                Log.d(TAG, "=== Angular Velocity Subscribe ===");

//                angularVelocityJsonWriter = createFile("AngularVelocity");

                for (MovesenseDevice device : MovesenseConnectedDevices.getConnectedDevices()) {
                    mMdsAngularVelocitySubscriptions.add(Mds.builder().build(this).subscribe("suunto://MDS/EventListener",
                            FormatHelper.formatContractToJson(device
                                    .getSerial(), ANGULAR_VELOCITY_PATH), new MdsNotificationListener() {
                                @Override
                                public void onNotification(String s) {
                                    AngularVelocity angularVelocity = new Gson().fromJson(
                                            s, AngularVelocity.class);
                                    if (angularVelocity != null) {
                                        AngularVelocity.Array arrayData = angularVelocity.body.array[0];
                                        if (Math.abs(arrayData.x) > 2 || Math.abs(arrayData.y) > 2 || Math.abs(arrayData.z) > 2) {
//                                            appendToFile(new HashMap<String, Double>() {{
//                                                put("X", arrayData.x);
//                                                put("Y", arrayData.y);
//                                                put("Z", arrayData.z);
//                                            }}, device.getSerial(), angularVelocityJsonWriter);
                                            FileWriter.writeMeasurementToJsonFile(device.getSerial(), new Measurement(angularVelocity));
                                        }
                                    }
                                }

                                @Override
                                public void onError(MdsException e) {
                                    buttonView.setChecked(false);
                                    Toast.makeText(MultiSensorUsageActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            } else {
                for (MdsSubscription subscription : mMdsAngularVelocitySubscriptions)
                    subscription.unsubscribe();
                if (angularVelocityJsonWriter != null)
//                    closeFile(angularVelocityJsonWriter, MultiSensorUsageActivity.this);
                Log.d(TAG, "=== Angular Velocity Unsubscribe ===");
            }
        } catch (Exception e) {
            if (angularVelocityJsonWriter != null)
//                closeFile(angularVelocityJsonWriter, MultiSensorUsageActivity.this);
            throw e;
        }
    }

    /**
     * Magnetic Field Switch
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.multiSensorUsage_magneticField_switch)
    public void onMagneticFieldCheckedChange(final CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked) {
                Log.d(TAG, "=== Magnetic Field Subscribe ===");

//                magneticFieldJsonWriter = createFile("MagneticField");

                for (MovesenseDevice device : MovesenseConnectedDevices.getConnectedDevices()) {
                    mMdsMagneticFieldSubscriptions.add(Mds.builder().build(this).subscribe("suunto://MDS/EventListener",
                            FormatHelper.formatContractToJson(device
                                    .getSerial(), MAGNETIC_FIELD_PATH), new MdsNotificationListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onNotification(String s) {
                                    MagneticField magneticField = new Gson().fromJson(
                                            s, MagneticField.class);
                                    if (magneticField != null) {
                                        MagneticField.Array arrayData = magneticField.body.array[0];
                                        if (Math.abs(arrayData.x) > 2 || Math.abs(arrayData.y) > 2 || Math.abs(arrayData.z) > 2) {
//                                            appendToFile(new HashMap<String, Double>() {{
//                                                put("X", arrayData.x);
//                                                put("Y", arrayData.y);
//                                                put("Z", arrayData.z);
//                                            }}, device.getSerial(), magneticFieldJsonWriter);
                                            FileWriter.writeMeasurementToJsonFile(device.getSerial(), new Measurement(magneticField));
                                        }
                                    }
                                }

                                @Override
                                public void onError(MdsException e) {
                                    buttonView.setChecked(false);
                                    Toast.makeText(MultiSensorUsageActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            } else {
                for (MdsSubscription subscription : mMdsMagneticFieldSubscriptions)
                    subscription.unsubscribe();
                if (magneticFieldJsonWriter != null)
//                    closeFile(magneticFieldJsonWriter, MultiSensorUsageActivity.this);
                Log.d(TAG, "=== Magnetic Field Unsubscribe ===");
            }
        } catch (Exception e) {
            if (magneticFieldJsonWriter != null)
//                closeFile(magneticFieldJsonWriter, MultiSensorUsageActivity.this);
            throw e;
        }
    }

    @Override
    public void setPresenter(MultiSensorUsageContract.Presenter presenter) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage(R.string.disconnect_dialog_text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "TEST Disconnecting...");
                        BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(0));
                        BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(1));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
