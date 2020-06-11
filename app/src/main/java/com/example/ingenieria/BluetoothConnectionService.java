package com.example.ingenieria;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }




    /**
     *Este hilo se ejecuta mientras escucha las conexiones entrantes.
     * Se comporta como un cliente del lado del servidor.
     * Se ejecuta hasta que se acepta una conexión
     *       (o hasta que se cancele)
     */
    private class AcceptThread extends Thread {

        // El socket del servidor local
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Crear un nuevo socket de servidor de escucha
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                //  llamada de bloqueo y que solo regresará con una conexión exitosa o una excepción
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * Este hilo se ejecuta al intentar establecer una conexión saliente con un dispositivo.
     * la conexión tiene éxito o falla.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket para una conexión con el dispositivo Bluetooth dado
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Siempre cancele el descubrimiento porque ralentizará una conexión
            mBluetoothAdapter.cancelDiscovery();

            // Haga una conexión al BluetoothSocket

            try {
                // Esta es una llamada de bloqueo y solo regresará con una
                // conexión exitosa o una excepción
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // cerrar el socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }


            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }



    /**
     * Inicia el servicio de chat. Específicamente,
     * inicie AcceptThread para comenzar una sesión en modo de escucha (servidor).
     * Llamado por la actividad enResume ()
     */

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancelar cualquier hilo que intente hacer una conexión
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     AcceptThread comienza y espera una conexión.
     Luego, ConnectThread se inicia e intenta establecer una conexión con los otros dispositivos AcceptThread
     **/

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     el ConnectedThread que es responsable de mantener la conexión BTC,
     enviar los datos y recibir los datos entrantes a
     través de flujos de entrada / salida, respectivamente.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //descartar el diálogo de progreso cuando se establece la conexión
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store para la corriente

            int bytes; // bytes returned del read()

            // Siga escuchando InputStream hasta que ocurra una excepción
            while (true) {
                // leer de InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);

                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent incomingMessageIntent = new Intent("MensajeEntrante");
                    incomingMessageIntent.putExtra("elMensaje",incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);


                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //se llama desde la actividad principal para enviar datos al dispositivo remoto
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* se llama de actividad principal para cerrar la conexión */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Inicie el hilo para administrar la conexión y realizar transmisiones
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Escribir en ConnectedThread de manera no sincronizada
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Crear objeto temporal
        ConnectedThread r;

        // Sincronice una copia de ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //realizar la escritura
        mConnectedThread.write(out);
    }

}

