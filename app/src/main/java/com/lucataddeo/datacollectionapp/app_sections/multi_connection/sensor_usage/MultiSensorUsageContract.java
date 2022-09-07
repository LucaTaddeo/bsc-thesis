package com.lucataddeo.datacollectionapp.app_sections.multi_connection.sensor_usage;


import com.lucataddeo.datacollectionapp.BasePresenter;
import com.lucataddeo.datacollectionapp.BaseView;

import io.reactivex.Observable;

public interface MultiSensorUsageContract {

    interface Presenter extends BasePresenter {
        Observable<String> subscribeLinearAcc(String uri);

    }

    interface View extends BaseView<MultiSensorUsageContract.Presenter> {

    }
}
