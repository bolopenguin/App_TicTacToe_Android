package com.example.progettoandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static BluetoothDevice serverDevice = GestioneBluetooth.serverDevice ;


    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                Log.d(TAG, "BroadcastReceiver4: Device Found.");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                Log.d(TAG, "BroadcastReceiver4: Device Connected.");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                Log.d(TAG, "BroadcastReceiver4: Stop Discovery.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                Log.d(TAG, "BroadcastReceiver4: Device Disconnecting.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.d(TAG, "BroadcastReceiver4: Device Connecting.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver4, filter2);
    }

    public void btnClient(View view) {
        //controllo di aver selezionato un device dalla lista
        if(serverDevice == null){
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
            ConnessioneBluetooth.ConnectThread connect = new ConnessioneBluetooth.ConnectThread(serverDevice, MY_UUID);
            connect.start();
        }
    }

    public void btnServer(View view) {
        Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
        ConnessioneBluetooth.AcceptThread server = new ConnessioneBluetooth.AcceptThread();
        server.start();
    }
}
