package com.example.ingenieria;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompatSideChannelService;

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
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

    BluetoothAdapter nBluetoothAdapter;
    Button btnVisibilidad;
    public ArrayList<BluetoothDevice> nBTDevices = new ArrayList<>();
    public DeviceListAdapter nDeviceListAdapter;
    ListView lvNewDevices;
    Button btnDescubrir;

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
                        Log.d(TAG,"nBroadcastReceiver2: DISPOSITIVO VISIBLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"nBroadcastReceiver2: INVISIBLE Y LISTO PARA RECIBIR CONEXIONES");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"nBroadcastReceiver2: INVISIBLE Y NO LISTO PARA RECIBIR CONEXIONES");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"nBroadcastReceiver2: CONECTANDO...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"nBroadcastReceiver2: CONECTADO");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver nBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                nBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ":" + device.getAddress());
                nDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, nBTDevices);
                lvNewDevices.setAdapter(nDeviceListAdapter);
            }
        }
    };

    private BroadcastReceiver nBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action= intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 casos
                //caso 1: bonded already
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDED){
                    Log.d(TAG,"BroadcastReceiver: BOND_BONDED");
                }
                //caso 2 : creating a bone
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    Log.d(TAG,"BroadcastReceiver: BOND_BONDING");
                }
                //caso 3 : nreaking a bond
                if(mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    Log.d(TAG,"BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnVisibilidad = (Button) findViewById(R.id.btnVisibilidad);
        lvNewDevices=(ListView) findViewById(R.id.lvNewDevices);
        nBTDevices = new ArrayList<>();
        btnDescubrir = (Button) findViewById(R.id.btnFindUnpairedDevices);
        nBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(nBroadcastReceiver4,filter);



        lvNewDevices.setOnItemClickListener(MainActivity.this);

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

                IntentFilter intentFilter = new IntentFilter(nBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(nBroadcastReceiver2, intentFilter);
            }

        });


        // Descubrir
        btnDescubrir.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)

            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnDescubrir:mirar distintos dispositivos");

                if (nBluetoothAdapter.isDiscovering()) {
                    nBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDescubrir: cancelar descubrimiento");
                    //chequeo permisos en el manifest
                    checkBTPermissions();
                    nBluetoothAdapter.startDiscovery();
                    IntentFilter descubrirDispositivosIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(nBroadcastReceiver3, descubrirDispositivosIntent);
                }
                if (!nBluetoothAdapter.isDiscovering()) {
                    checkBTPermissions();
                    nBluetoothAdapter.startDiscovery();
                    IntentFilter descubrirDispositivosIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(nBroadcastReceiver3, descubrirDispositivosIntent);
                }
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
    } // habilita bluetooth
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy:called");
        super.onDestroy();
        unregisterReceiver(nBroadcastReceiver1);
        unregisterReceiver(nBroadcastReceiver2);
        unregisterReceiver(nBroadcastReceiver3);
        unregisterReceiver(nBroadcastReceiver4);
    } /// Se destruye los receiver

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        nBluetoothAdapter.cancelDiscovery();
        Log.d(TAG,"onItemClick: el click en el dispositivo");
        String deviceName= nBTDevices.get(i).getName();
        String deviceAddress = nBTDevices.get(i).getAddress();
        Log.d(TAG, "onItemClick: deciveName =" + deviceName);
        Log.d(TAG, "onItemClick: deciveAddress =" + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            Log.d(TAG,"Trying to pair with "+ deviceName);
            nBTDevices.get(i).createBond();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    } // chekea version de android con permisos

}
