
//mettere timer fra la connessione e i bottoni cliccabili

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
import android.text.TextUtils;
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

public class MainActivity extends AppCompatActivity
        implements  View.OnClickListener{
    final String TAG = "MainActivity";

    private Button clientbtn;
    private Button serverbtn;

    private Button[] btnslots = new Button[9];

    private boolean cliccati[] = new boolean[9];
    private boolean occupati[] = new boolean[9];

    // se vale 0 è il server se vale 1 è il client
    private boolean ruolo;

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

                setButtonsBluetooth(false);

                if(ruolo){
                    setButtonsSlots(true);
                    //scrivere che è il suo turno
                } else{
                    //scrivere che non è il suo turno
                }

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

        btnslots[0] = (Button)findViewById(R.id.a);
        btnslots[1] = (Button)findViewById(R.id.b);
        btnslots[2] = (Button)findViewById(R.id.c);
        btnslots[3] = (Button)findViewById(R.id.d);
        btnslots[4] = (Button)findViewById(R.id.e);
        btnslots[5] = (Button)findViewById(R.id.f);
        btnslots[6] = (Button)findViewById(R.id.g);
        btnslots[7] = (Button)findViewById(R.id.h);
        btnslots[8] = (Button)findViewById(R.id.i);

        clientbtn = (Button)findViewById(R.id.Client);
        serverbtn = (Button)findViewById(R.id.Server);

        clientbtn.setOnClickListener(this);
        serverbtn.setOnClickListener(this);
        for(Button listen: btnslots) listen.setOnClickListener(this);

        setButtonsSlots(false);
    }

    @Override
    public void onClick(View view){

            switch (view.getId()) {

                case R.id.a:
                    cliccati[0] = true;
                    occupati[0] = true;
                    btnClicked(0);
                    break;

                case R.id.b:
                    cliccati[1] = true;
                    occupati[1] = true;
                    btnClicked(1);
                    break;

                case R.id.c:
                    cliccati[2] = true;
                    occupati[2] = true;
                    btnClicked(2);
                    break;

                case R.id.d:
                    cliccati[3] = true;
                    occupati[3] = true;
                    btnClicked(3);
                    break;

                case R.id.e:
                    cliccati[4] = true;
                    occupati[4] = true;
                    btnClicked(4);
                    break;

                case R.id.f:
                    cliccati[5] = true;
                    occupati[5] = true;
                    btnClicked(5);
                    break;

                case R.id.g:
                    cliccati[6] = true;
                    occupati[6] = true;
                    btnClicked(6);
                    break;

                case R.id.h:
                    cliccati[7] = true;
                    occupati[7] = true;
                    btnClicked(7);
                    break;

                case R.id.i:
                    cliccati[8] = true;
                    occupati[8] = true;
                    btnClicked(8);
                    break;

                case R.id.Client:
                    btnClient();
                    break;

                case R.id.Server:
                    btnServer();
                    break;

                default:
                    break;
            }
        }


    //metodo per rendere i bottoni dello slots cliccabili ( b = true) o non cliccabili ( b = false)
    private void setButtonsSlots(boolean b) {
        Log.d(TAG, "Changing btns slots enable to " + b);
        for(Button change : btnslots) change.setEnabled(b);
    }

    //metodo per rendere i bottoni del client e server cliccabili ( b=true) o non cliccabili ( b= false)
    private void setButtonsBluetooth(boolean b) {
        Log.d(TAG, "Changing btns server & client enable to " + b);
        clientbtn.setEnabled(b);
        serverbtn.setEnabled(b);
    }

    //metodo per diventare un client
    public void btnClient() {

        if(!ruolo) {
            Log.d(TAG, "Closing Server Socket");
            server.cancel();
        } else {
            Log.d(TAG, "Closing Client Socket");
            client.cancel();
        }

        ruolo = true;
        Log.d(TAG, "Starting Client Socket");
        Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
        client.start();
    }

    //metodo per diventare un server
    public void btnServer() {
        if(!ruolo) {
            Log.d(TAG, "Closing Server Socket");
            server.cancel();
        } else {
            Log.d(TAG, "Closing Client Socket");
            client.cancel();
        }

        ruolo = false;
        Log.d(TAG, "Starting Server Socket");
        Toast.makeText(getApplicationContext(), "Trying to connect", Toast.LENGTH_SHORT).show();
        server.start();

    }

    //metodo per mandare un messaggio
    public void btnClicked(int position) {
        Log.d(TAG, "Clicked: " + position);
        //rendo i bottoni non cliccabili
        int esito = 0;
        setButtonsSlots(false);

            if(ruolo) {
                btnslots[position].setText("X");
            } else {
                btnslots[position].setText("O");
            }

            //controllo se ci è stata vittora o pareggio
            if(victory()) {
                esito = 1;
            } else if(draw()){
                esito = 2;
            }

        String messaggio = Integer.toString(esito) + Integer.toString(position);
        byte[] bytes = messaggio.getBytes() ;
        mConnectedThread.write(bytes);
    }

    //funzione che verifica se il giocatore ha vinto (da rivedere)
    private boolean victory(){
        for(int i=0; i<9; i+=3){
            if(cliccati[i] && cliccati[i+1] && cliccati[i+2]) return true;
            if(cliccati[i] && cliccati[i+3] && cliccati[i+6]) return true;
        }

        if(cliccati[0] && cliccati[4] && cliccati[8]) return true;
        if(cliccati[2] && cliccati[4] && cliccati[6]) return true;

        return false;
    }
    //funzione che verifica se c'è pareggio

    private boolean draw(){
        for(Boolean clicked : occupati){
            if(!clicked)  return false;
        }
        return true;
    }

    //funzione per gestire il messaggio ricevuto
    private void messageReceived(String message) {
        //prendere il primo carattere e metterlo in un bool
        int esito = Character.getNumericValue(message.charAt(0));
        //prendere il secondo carattere in un char
        int position = Character.getNumericValue(message.charAt(1));

        occupati[position] = true;

        if(!ruolo) {
            btnslots[position].setText("X");
        } else {
            btnslots[position].setText("O");
        }


        //controllo l'esito
        switch (esito){
            case 0:
                for(int i=0; i<occupati.length; i++){
                    if(!cliccati[i] || !occupati[i]) btnslots[i].setEnabled(true);
                    // change.setEnabled(!cliccati[i] || !occupati[i]);
                }
                break;

            case 1:
                //gestire pareggio
                break;

            case 2:
                //gestire perdita
                break;

            default:
                break;
        }

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

                    //funzione per gestire il messaggio ricevuto
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
