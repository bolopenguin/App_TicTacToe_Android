package com.example.progettoandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    private Button a;

    private boolean turno = false;

    static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    AcceptThread server = new AcceptThread();
    ConnectThread client = new ConnectThread(serverDevice, MY_UUID);


    static BluetoothAdapter mBluetoothAdapter = GestioneBluetooth.mBluetoothAdapter;
    ConnectedThread mConnectedThread;

    public static BluetoothDevice serverDevice = GestioneBluetooth.serverDevice ;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                Log.d(TAG, "BroadcastReceiver: Device Found.");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                Log.d(TAG, "BroadcastReceiver: Device Connected.");

                if(server.isAlive()){
                    turno = false;
                } else if(client.isAlive()){
                    turno = true;
                }

                //aggiungere funzione per rendere bottoni cliccabili

                Toast.makeText(getApplicationContext(), "Game Start", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                Log.d(TAG, "BroadcastReceiver: Stop Discovery.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                Log.d(TAG, "BroadcastReceiver: Device Disconnecting.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Log.d(TAG, "BroadcastReceiver: Device Connecting.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver, filter);

        a = (Button) findViewById(R.id.a);

    }


    //metodo per diventare un client
    public void btnClient(View view) {
        if(client.isAlive()){
            Log.d(TAG, "Closing Client Socket");
            client.cancel();
        }else if(server.isAlive()) {
            Log.d(TAG, "Closing Server Socket");
            server.cancel();
        }

        Log.d(TAG, "Starting Client Socket");
        Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
        client.start();
    }

    //metodo per diventare un server
    public void btnServer(View view) {
        if(client.isAlive()) {
            Log.d(TAG, "Closing Client Socket");
            client.cancel();
        }else if(server.isAlive()) {
            Log.d(TAG, "Closing Server Socket");
            server.cancel();
        }
        Log.d(TAG, "Starting Server Socket");
        Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
        server.start();

    }

    //metodo per mandare un messaggio
    public void btnA(View v) {

        //funzione per bloccare i bottoni

        if(turno == false){
            Toast.makeText(getApplicationContext(), "Wait your turn", Toast.LENGTH_SHORT).show();
        }else {
            turno = false;

            //funzione per salvare bottone cliccato in nell'array dei bottoni non cliccabili

            if(server.isAlive()) {
                //funzione per mettere il cerchio
            } else {
                //funzione per mettere la ics
            }

            //mettere nell'if la funzione per controllare la vittoria
            if(victory()) {
                //mandare messaggio di vittoria
            } else {
                //mandare messaggio con info sulla posizione cliccata
            }

        }

        /*String messaggio = "Ciao";
        byte[] bytes = messaggio.getBytes() ;
        mConnectedThread.write(bytes);*/
    }

    //funzione per gestire il messaggio ricevuto
    private void messageReceived(String message) {
        //prendere il primo carattere e metterlo in un bool
        //prendere il secondo carattere in un char

        //switch del secondo carattere per salvare il bottone nell'array dei bottoni non cliccabili

        //mettere nell'if il parametro appena salvato nel bool
        if(true){
            //gestire la perdita
        } else{
            turno = true;
        }
    }

    private boolean victory(){

        //funzione per verificare se il giocatore ha vinto

        return true;
    }

    //Classe per Gestire il Client
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device, UUID myUuid) {
            Log.d(TAG, "ConnectThread: started.");
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(myUuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();
            BluetoothSocket socket = null;

            Log.i(TAG, "RUN mConnectThread ");

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException closeException) {
                    Log.e(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID);
                }
                return;
            }


            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connected(mmSocket);

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    //classe per gestire il Server
    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME", MY_UUID);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    Log.d(TAG, "run: RFCOM server socket start...");

                    socket = mmServerSocket.accept();

                    Log.d(TAG, "run: RFCOM server socket accepted connection.");

                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
                    break;
                }


                if (socket != null) {
                    connected(socket);
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

    //questa classe gestisce lo scambio di messaggi
    private class ConnectedThread extends Thread {
        private Handler handler;

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    messageReceived(incomingMessage);


                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }
}
