package com.lucataddeo.datacollectionapp.app_sections.single_connection;


import android.content.BroadcastReceiver;
import android.content.Intent;

import com.lucataddeo.datacollectionapp.BasePresenter;
import com.lucataddeo.datacollectionapp.BaseView;
import com.polidea.rxandroidble2.RxBleDevice;

public interface MovesenseContract {

    interface Presenter extends BasePresenter {
        void startScanning();

        void stopScanning();

        void onBluetoothResult(int requestCode, int resultCode, Intent data);
    }

    interface View extends BaseView<Presenter> {
        void displayScanResult(RxBleDevice bluetoothDevice, int rssi);

        void displayErrorMessage(String message);

        void registerReceiver(BroadcastReceiver broadcastReceiver);

        void unregisterReceiver(BroadcastReceiver broadcastReceiver);

        boolean checkLocationPermissionIsGranted();
    }
}
