//classe per gestire la connessione del server

package com.example.progettoandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    private static final String TAG = GestioneBluetooth.TAG;
    BluetoothAdapter mBluetoothAdapter = GestioneBluetooth.mBluetoothAdapter;
    static final UUID MY_UUID = GestioneBluetooth.MY_UUID;

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
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.

                //cambiare activity

                break;
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
