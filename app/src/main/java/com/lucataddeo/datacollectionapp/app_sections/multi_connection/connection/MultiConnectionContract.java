package com.lucataddeo.datacollectionapp.app_sections.multi_connection.connection;

import com.lucataddeo.datacollectionapp.BasePresenter;
import com.lucataddeo.datacollectionapp.BaseView;
import com.polidea.rxandroidble2.RxBleDevice;

public interface MultiConnectionContract {

    interface Presenter extends BasePresenter {
        void scanFirstDevice();
        void scanSecondDevice();
        void connect(RxBleDevice rxBleDevice);
        void disconnect(RxBleDevice rxBleDevice);

    }

    interface View extends BaseView<MultiConnectionContract.Presenter> {
        void onFirsDeviceSelectedResult(RxBleDevice rxBleDevice);
        void onSecondDeviceSelectedResult(RxBleDevice rxBleDevice);
        void onThirdDeviceSelectedResult(RxBleDevice rxBleDevice);
        void displayErrorMessage(String message);
    }
}
