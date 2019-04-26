package com.example.progettoandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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

    public static BluetoothDevice serverDevice;

    //Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
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
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
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
        Log.d(TAG, "Paired with " + deviceName);

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

    public void Info (View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        alertDialogBuilder.setTitle("Istruzioni");
        alertDialogBuilder.setIcon(R.drawable.icona);
        alertDialogBuilder.setMessage("1)cliccare il pulsante Cerca e attendere la ricerca dei dispositivi bluetooth," +
                "\n2)selezionare il device del tuo avversario," +
                "\n3)cliccare Gioca");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(GestioneBluetooth.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}