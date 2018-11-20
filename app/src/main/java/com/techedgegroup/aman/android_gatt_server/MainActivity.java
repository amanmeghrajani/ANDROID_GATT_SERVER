package com.techedgegroup.aman.android_gatt_server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothManager      mBluetoothManager    = null;
    BluetoothAdapter      mBluetoothAdapter    = null;
    BluetoothLeAdvertiser mBluetoothAdvertiser = null;
    BluetoothGattServer   mGattServer          = null;


    public static final UUID SERVICE_UUID = UUID.fromString("49001249-c77e-4035-918c-a8ecf993ff6z");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
            return;
        }

        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            finish();
            return;
        }

        mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        GattServerCallback mGattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        setupServer();
        startAdvertising();
    }

    private void setupServer() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mGattServer.addService(service);
    }


    private void startAdvertising() {
        if(mBluetoothAdvertiser == null) {return;}
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(SERVICE_UUID);
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build();


        mBluetoothAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

    }


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("BLUETOOTH SERVER MAINACTIVITY", "Peripheral advertising started");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d("BLUETOOTH SERVER MAINACTIVITY", "Peripheral advertising failed: " + errorCode);
        }
    };

    protected void onPause() {
        super.onPause();
        stopAdvertising();
        stopServer();
    }
    private void stopServer() {
        if (mGattServer != null) {
            mGattServer.close();
        }
    }
    private void stopAdvertising() {
        if (mBluetoothAdvertiser != null) {
            mBluetoothAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }



    private class GattServerCallback extends BluetoothGattServerCallback {}
}
