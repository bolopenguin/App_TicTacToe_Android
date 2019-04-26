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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class GestioneBluetooth extends AppCompatActivity
        implements AdapterView.OnItemClickListener{

    final String TAG = "GestioneBluetooth";

    ListView lvNewDevices;
    public DeviceListAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    static BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBTDevice;

    public static BluetoothDevice serverDevice;

    //Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                //prende dispositivo dall'intent e lo salva in device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //aggiunge il device alla lista
                if (device.getName() != null) {
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
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver2: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    Toast.makeText(getApplicationContext(), "Bonded", Toast.LENGTH_SHORT).show();
                    mBTDevice = mDevice;
                }
                //case2: creating a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver2: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver2: BOND_NONE.");
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_bluetooth);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Lista dei device con la discovery
        mBTDevices = new ArrayList<>();
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(GestioneBluetooth.this);

        //Broadcasts when bond state changes pairing and connecting
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter);

    }


    //abilita la discoverability, se bt è spento lo accende
    public  void abilitaDiscoverabilty(){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
        startActivity(discoverableIntent);
    }

    //cosa si fa quando si abilita la discovery
    public void btnDiscover(View view) {

        Toast.makeText(getApplicationContext(), "Discovering", Toast.LENGTH_SHORT).show();

        addPermission();

        abilitaDiscoverabilty();

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver1, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver1, discoverDevicesIntent);
        }


    }

    private void addPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
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


    public void launchMainActivity(View view) {
        Log.d(TAG, "Avvio main Activity");
        if(serverDevice == null){
            Toast.makeText(getApplicationContext(), "Select a Device", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


}