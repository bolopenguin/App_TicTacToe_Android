package com.example.progettoandroid;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class GestioneBluetooth extends AppCompatActivity
        implements AdapterView.OnItemClickListener{

    static final String TAG = "GestioneBluetooth";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    ImageButton btnONOFF;

    ListView lvNewDevices;
    public DeviceListAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    static BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBTDevice;

    static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public BluetoothDevice serverDevice;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    //Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                //prende dispositivo dall'intent e lo salva in device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //aggiunge il device alla lista se ha un nome ben definito
                if(!device.getName().isEmpty()){
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    //collegamento fra dati e layout diretto, prende l'oggetto di tipo layout e lo mette nella
                    //list view
                    lvNewDevices.setAdapter(mDeviceListAdapter);
                }
            }
        }
    };

    //Broadcast Receiver that detects bond state changes (Pairing status changes)
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_bluetooth);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Button OnOFf
        btnONOFF = (ImageButton) findViewById(R.id.btnONOFF);
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                btnOnOff();
            }
        });


        //Lista dei device con la discovery
        mBTDevices = new ArrayList<>();
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(GestioneBluetooth.this);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, filter);
    }


    //cosa si fa quando si accende o spegne il bluetooth
    public void btnOnOff(){
        if(mBluetoothAdapter == null){

            Toast.makeText(getApplicationContext(), "Not Possible", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){

            Toast.makeText(getApplicationContext(), "Enabling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "enableDisableBT: enabling BT and make device discoverable.");

            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
            startActivity(discoverableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){

            Toast.makeText(getApplicationContext(), "Disabling", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    //cosa si fa quando si abilita la discovery
    public void btnDiscover(View view) {

        Toast.makeText(getApplicationContext(), "Discovering", Toast.LENGTH_SHORT).show();

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }
    }

    //cosa succede quando si clicca su un elemento della lista
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        Log.d(TAG, "btnDiscover: Canceling discovery.");
        mBluetoothAdapter.cancelDiscovery();

        Toast.makeText(getApplicationContext(), "Bonding", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //cerca di fare il bond
        Log.d(TAG, "Trying to pair with " + deviceName);
        mBTDevices.get(i).createBond();

        serverDevice = mBTDevices.get(i);

        Toast.makeText(getApplicationContext(), "Bonded", Toast.LENGTH_SHORT).show();
    }

    public void btnClient(View view) {
        //controllo di aver selezionato un device dalla lista
        if (serverDevice != null) {
            ConnectThread connect = new ConnectThread(serverDevice, MY_UUID);
            connect.start();
        }
    }

    public void btnServer(View view) {

        AcceptThread server = new AcceptThread();
        server.start();
    }

    public void launchSecondActivity(View view) {
        Log.d(TAG, "Avvio seconda Activity");
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
}