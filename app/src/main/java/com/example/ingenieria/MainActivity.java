package com.example.ingenieria;


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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

    BluetoothAdapter nBluetoothAdapter;
    Button btnVisibilidad;
    public ArrayList<BluetoothDevice> nBTDevices = new ArrayList<>();
    public DeviceListAdapter nDeviceListAdapter;
    ListView lvNewDevices;
    Button btnDescubrir;
    Button btnSend;
    Button btnPlay;
    Button btnSpeed;
    Button btnReady;
    Switch switchOnOff;
    TextView incomingMessage;
    StringBuilder messages;
    Button btnStartConnection;
    EditText editText;
    EditText editTextSpeed;
    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothDevice mBTDevice;

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
                    mBTDevice = mDevice;
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


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text=intent.getStringExtra("elMensaje");
            messages.append(text + "\n");
            incomingMessage.setText(messages);
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
        editText = (EditText) findViewById(R.id.editText);
        editTextSpeed = (EditText) findViewById(R.id.editTextSpeed);

        final Switch switchOnOff=(Switch) findViewById(R.id.switchOnOff);
        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnSpeed = (Button) findViewById(R.id.btnSpeed);
        btnReady = (Button) findViewById(R.id.btnReady);
        incomingMessage=(TextView)findViewById(R.id.MensajeEntrante);
        messages = new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,  new IntentFilter("MensajeEntrante"));

        //cuando el bond realice cambios
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

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                StartConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                byte[] bytes = editText.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                editText.setText("");
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String play="play";
                byte[] bytes = play.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });
        btnReady.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String ready="ready";
                byte[] bytes = ready.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });
        btnSpeed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                byte[] bytes = editTextSpeed.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                editTextSpeed.setText(0);
            }
        });
        switchOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchOnOff.isChecked()){
                    String on="on";
                    byte[] bytes = on.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }
                else{
                    String off="off";
                    byte[] bytes = off.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }
            }
        });

    }

    public void StartConnection() {
        StartBTConnection(mBTDevice,MY_UUID_INSECURE);
    }


    public void StartBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Inicializando conexion de bluetooth RFCOM ");
        mBluetoothConnection.startClient(device, uuid);
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

            mBTDevice = nBTDevices.get(i);
            mBluetoothConnection =  new  BluetoothConnectionService ( MainActivity . this );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
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
