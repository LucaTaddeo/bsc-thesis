package com.lucataddeo.datacollectionapp.bluetooth;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.lucataddeo.datacollectionapp.app_sections.single_connection.MovesenseActivity;
import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;

public enum  ConnectionLostDialog {
    INSTANCE;

    private AlertDialog mAlertDialog;

    public void showDialog(final Context context) {
        Log.e("ConnectionLostDialog", "showDialog: ");
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("Connection Lost")
                    .setMessage("Appliaction will connect automatically with Movesense device" +
                            " when it will be available.")
                    .setPositiveButton("Connect with other Movesense", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BleManager.INSTANCE.isReconnectToLastConnectedDeviceEnable = false;

                            BleManager.INSTANCE.disconnect(MovesenseConnectedDevices.getConnectedRxDevice(0));

                            context.startActivity(new Intent(context, MovesenseActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                    });

            mAlertDialog = builder.show();
        } else {
            Log.e("ConnectionLostDialog", "showDialog: DIALOG NOT NULL");
        }
    }


    public void dismissDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
