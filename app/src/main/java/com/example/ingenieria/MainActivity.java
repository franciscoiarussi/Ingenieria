package com.example.ingenieria;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    BluetoothAdapter nBluetoothAdapter;
    Button btnVisibilidad;
    public ArrayList<BluetoothDevice> nBTDevices = new ArrayList<>();
    public DeviceListAdapter nDeviceListAdapter;
    ListView lvNewDevices;

    //Crear BroadcasteReceiver
    private final BroadcastReceiver nBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(nBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, nBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"nBroadcastReceiver1: APAGADO");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"nBroadcastReceiver1: APAGANDOSE");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"nBroadcastReceiver1: PRENDIDO");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"nBroadcastReceiver1: PRENDIENDO");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver nBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"nBroadcastReceiver1: DISPOSITIVO VISIBLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"nBroadcastReceiver1: INVISIBLE Y LISTO PARA RECIBIR CONEXIONES");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"nBroadcastReceiver1: INVISIBLE Y NO LISTO PARA RECIBIR CONEXIONES");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"nBroadcastReceiver1: CONECTANDO...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"nBroadcastReceiver1: CONECTADO");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy:called");
        super.onDestroy();
        unregisterReceiver(nBroadcastReceiver1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnVisibilidad= (Button) findViewById(R.id.btnVisibilidad);

        nBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //PRENDER-APAGAR BLUETOOTH
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onCLick: prendidendo/apagando");
                enableDisableBT();
            }
        });
        //VISIBILIDAD
        btnVisibilidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnVisibilidad: Haciendo el dispositivo visible pr 300 segundos");

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                IntentFilter intentFilter=new IntentFilter(nBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(nBroadcastReceiver2,intentFilter);

            }
        });


    }

    public void enableDisableBT(){
        if(nBluetoothAdapter == null){
            Log.d(TAG, "enbleDisableBT: No tenes un adaptador Bluetooth");
        }
        if(!nBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: prendidendo");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(nBroadcastReceiver1, BTIntent);
        }
        if(nBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: apagando");
            nBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(nBroadcastReceiver1, BTIntent);
        }
    }

}
