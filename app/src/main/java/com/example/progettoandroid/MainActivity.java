package com.example.progettoandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
    private Button revengebtn;

    private Button[] btnslots = new Button[9];

    private TextView info;
    private TextView punteggio;

    private VideoView HaiVinto;
    private VideoView HaiPerso;
    private VideoView HaiPareggiato;

    private TableLayout Tabella;

    private boolean cliccati[] = new boolean[9];
    private boolean occupati[] = new boolean[9];

    // se vale 0 è il server se vale 1 è il client
    private boolean ruolo;

    private int score1=0;
    private int score2=0;

    static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    AcceptThread server;
    ConnectThread client;


    static BluetoothAdapter mBluetoothAdapter = GestioneBluetooth.mBluetoothAdapter;
    ConnectedThread mConnectedThread;

    public static BluetoothDevice serverDevice = GestioneBluetooth.serverDevice ;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
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
                    info.setText("Tocca a te");
                } else{
                    info.setText("Aspetta");
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
                revengebtn.setEnabled(false);
                revengebtn.setVisibility(View.INVISIBLE);
                Log.d(TAG, "BroadcastReceiver: Device Disconnected.");
                info.setText ("Scegli il tuo simbolo... (prima chi è O poi X)");
                HaiPareggiato.setVisibility(View.INVISIBLE);
                HaiVinto.setVisibility(View.INVISIBLE);
                HaiPerso.setVisibility(View.INVISIBLE);

                for(int i=0; i<9; i++){
                    btnslots[i].setBackgroundResource(R.drawable.trasparente);
                    cliccati[i] = false;
                    occupati[i] = false;
                }

                setButtonsBluetooth(true);
            }
        }
    };


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();

        mBluetoothAdapter.disable();

        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(client != null) {
            client.cancel();
            client = null;
        }
        if( server != null){
            server.cancel();
            server = null;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

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
        revengebtn = (Button)findViewById(R.id.revenge);

        revengebtn.setVisibility(View.INVISIBLE);

        info = (TextView)findViewById(R.id.textView);
        info.setText("Scegli il tuo simbolo... (prima chi è O poi X)");
        punteggio = (TextView) findViewById(R.id.textView2);

        Tabella=(TableLayout) findViewById(R.id.tableLayout);

        HaiVinto = (VideoView)findViewById(R.id.videoView1);
        HaiVinto.setVideoPath("android.resource://com.example.progettoandroid/"+R.raw.haivinto);
        HaiVinto.setVisibility(View.INVISIBLE);

        HaiPerso = (VideoView)findViewById(R.id.videoView2);
        HaiPerso.setVideoPath("android.resource://com.example.progettoandroid/"+R.raw.haiperso);
        HaiPerso.setVisibility(View.INVISIBLE);

        HaiPareggiato = (VideoView)findViewById(R.id.videoView3);
        HaiPareggiato.setVideoPath("android.resource://com.example.progettoandroid/"+R.raw.haipareggiato);
        HaiPareggiato.setVisibility(View.INVISIBLE);

        clientbtn.setOnClickListener(this);
        serverbtn.setOnClickListener(this);
        for(Button listen: btnslots) listen.setOnClickListener(this);
        revengebtn.setOnClickListener(this);
        revengebtn.setEnabled(false);
        setButtonsSlots(false);

        try {
            //set time in mili
            Thread.sleep(500);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view){

            switch (view.getId()) {

                case R.id.a:
                    cliccati[0] = true;
                    btnClicked(0);
                    break;

                case R.id.b:
                    cliccati[1] = true;
                    btnClicked(1);
                    break;

                case R.id.c:
                    cliccati[2] = true;
                    btnClicked(2);
                    break;

                case R.id.d:
                    cliccati[3] = true;
                    btnClicked(3);
                    break;

                case R.id.e:
                    cliccati[4] = true;
                    btnClicked(4);
                    break;

                case R.id.f:
                    cliccati[5] = true;
                    btnClicked(5);
                    break;

                case R.id.g:
                    cliccati[6] = true;
                    btnClicked(6);
                    break;

                case R.id.h:
                    cliccati[7] = true;
                    btnClicked(7);
                    break;

                case R.id.i:
                    cliccati[8] = true;
                    btnClicked(8);
                    break;

                case R.id.Client:
                    btnClient();
                    break;

                case R.id.Server:
                    btnServer();
                    break;

                case R.id.revenge:
                    reset();
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
        if (b) {
            clientbtn.setVisibility(View.VISIBLE);
            serverbtn.setVisibility(View.VISIBLE);
        }else{
            clientbtn.setVisibility(View.INVISIBLE);
            serverbtn.setVisibility(View.INVISIBLE);
        }
    }

    //metodo per diventare un client
    public void btnClient() {
        ruolo = true;
        setButtonsBluetooth(false);

        Log.d(TAG, "Starting Client Socket");
        client = new ConnectThread(serverDevice, MY_UUID);

        client.start();
    }

    //metodo per diventare un server
    public void btnServer() {
        ruolo = false;
        setButtonsBluetooth(false);

        Log.d(TAG, "Starting Server Socket");
        server = new AcceptThread();
        server.start();

    }

    //metodo per mandare un messaggio
    @SuppressLint("SetTextI18n")
    public void btnClicked(int position) {
        Log.d(TAG, "Clicked: " + position);

        //rendo i bottoni non cliccabili
        int esito = 0;
        setButtonsSlots(false);

        info.setText("Aspetta");

            if(ruolo) {
                btnslots[position].setBackgroundResource(R.drawable.x);
            } else {
                btnslots[position].setBackgroundResource(R.drawable.o);
            }

            //controllo se ci è stata vittora o pareggio
            if(victory()) {
                setButtonsSlots(false);
                info.setText("");
                esito = 1;
                HaiVinto.setVisibility(View.VISIBLE);
                HaiVinto.start();
                Tabella.setVisibility(View.INVISIBLE);
                HaiVinto.postDelayed(new Runnable() {
                    public void run() {
                        HaiVinto.setVisibility(View.GONE);
                        Tabella.setVisibility(View.VISIBLE);
                        revengebtn.setEnabled(true);
                        revengebtn.setVisibility(View.VISIBLE);
                    }
                }, 6000);
                score1++;
                punteggio.setText(score1+" : "+score2);
            } else if(draw()){
                setButtonsSlots(false);
                info.setText("");
                esito = 2;
                HaiPareggiato.setVisibility(View.VISIBLE);
                HaiPareggiato.start();
                Tabella.setVisibility(View.INVISIBLE);
                HaiPareggiato.postDelayed(new Runnable() {
                    public void run() {
                        HaiPareggiato.setVisibility(View.GONE);
                        Tabella.setVisibility(View.VISIBLE);
                        revengebtn.setEnabled(true);
                        revengebtn.setVisibility(View.VISIBLE);
                    }
                }, 6000);
            }

        String messaggio = Integer.toString(esito) + Integer.toString(position);
        byte[] bytes = messaggio.getBytes() ;

        try {
            mConnectedThread.write(bytes);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            reset();
        }
    }

    //funzione che verifica se il giocatore ha vinto (da rivedere)
    private boolean victory(){
        if(cliccati[0] && cliccati [1] && cliccati[2]) return true;
        if(cliccati[3] && cliccati [4] && cliccati[5]) return true;
        if(cliccati[6] && cliccati [7] && cliccati[8]) return true;
        if(cliccati[0] && cliccati [3] && cliccati[6]) return true;
        if(cliccati[1] && cliccati [4] && cliccati[7]) return true;
        if(cliccati[2] && cliccati [5] && cliccati[8]) return true;
        if(cliccati[0] && cliccati [4] && cliccati[8]) return true;
        if(cliccati[2] && cliccati [4] && cliccati[6]) return true;

        return false;
    }
    //funzione che verifica se c'è pareggio

    private boolean draw(){
        for(int i=0; i<9; i++){
            if(!cliccati[i] & !occupati[i]) return false;
        }
        return true;
    }

    //funzione per gestire il messaggio ricevuto
    @SuppressLint("SetTextI18n")
    private void messageReceived(String message) {
        //prendere il primo carattere e metterlo in un bool
        int esito = Character.getNumericValue(message.charAt(0));
        Log.d(TAG, "Esito: " + esito);
        //prendere il secondo carattere in un char
        int position = Character.getNumericValue(message.charAt(1));
        Log.d(TAG, "Position: " + position);

        info.setText("Tocca a te");

        occupati[position] = true;

        if(!ruolo) {
            btnslots[position].setBackgroundResource(R.drawable.x);
        } else {
            btnslots[position].setBackgroundResource(R.drawable.o);
        }


        //controllo l'esito
        switch (esito){
            case 0:
                for(int i=0; i<9; i++){
                    if(cliccati[i] | occupati[i]) btnslots[i].setEnabled(false);
                    else btnslots[i].setEnabled(true);
                }
                break;

            case 1:
                setButtonsSlots(false);
                info.setText("");
                score2++;
                punteggio.setText(score1+" : "+score2);
                HaiPerso.setVisibility(View.VISIBLE);
                HaiPerso.start();
                Tabella.setVisibility(View.INVISIBLE);
                HaiPerso.postDelayed(new Runnable() {
                    public void run() {
                        HaiPerso.setVisibility(View.GONE);
                        Tabella.setVisibility(View.VISIBLE);
                        revengebtn.setEnabled(true);
                        revengebtn.setVisibility(View.VISIBLE);
                    }
                }, 6000);
                break;

            case 2:
                setButtonsSlots(false);
                info.setText("");
                HaiPareggiato.setVisibility(View.VISIBLE);
                HaiPareggiato.start();
                Tabella.setVisibility(View.INVISIBLE);
                HaiPareggiato.postDelayed(new Runnable() {
                    public void run() {
                        HaiPareggiato.setVisibility(View.GONE);
                        Tabella.setVisibility(View.VISIBLE);
                        revengebtn.setEnabled(true);
                        revengebtn.setVisibility(View.VISIBLE);
                    }
                }, 6000);
                break;

            default:
                break;
        }
    }

    private void reset(){
        info.setText("Attendere...");
        revengebtn.setEnabled(false);
        revengebtn.setVisibility(View.INVISIBLE);

        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if(client != null) {
            client.cancel();
            client = null;
        }
        if( server != null){
            server.cancel();
            server = null;
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
                Log.d(TAG, "Closing client socket");
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
                Log.d(TAG, "Closing server socket");
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

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //funzione per gestire il messaggio ricevuto
                            messageReceived(incomingMessage);

                        }
                    });

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
                reset();
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                Log.d(TAG, "Closing mConncectThread socket");
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

    public void Info (View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        alertDialogBuilder.setTitle("Istruzioni");
        alertDialogBuilder.setIcon(R.drawable.bluetoothconnection);
        alertDialogBuilder.setMessage("1)Decidere chi tra te e i tuo avversario inizia a giocare," +

                "\n2)Colui che inizia sarà giocatore 1 e l'altro giocatore 2," +
                "\n3)Vince il giocatore che riesce a disporre tre dei propri simboli in linea retta orizzontale, verticale o diagonale"+
                "\n4)Per continuare il gioco cliccate entrambi Rivincita, in ogni caso il punteggio continuerà ad essere presente nella parte altra dello schermo"+
                "\nATTENZIONE!!! NON CLICCARE NESSUN BOTTONE ALL'INFUORI DI QUELLI PRESENTI NELL'AREA DI GIOCO");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
