package com.israel.achilles.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.israel.achilles.bluetooth.support.Variables;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity
        extends
                AppCompatActivity
        implements
                View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnTouchListener {

    // ---------------------------------------------------------  Variables ---------------------------------------------------------

    private Button buttonOn_Off,buttonEnableDescoverability, buttonDescover;
    private ImageButton buttonForward, buttonReverse, buttonLeft,buttonRight, buttonStop;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceListAdapter deviceListAdapter;
    private ListView listViewDevices;
    private BluetoothSocket clientSocket;

    private int preLeft=0;
    private int preRight=0;
    private int preForward=0;
    private int preStop=0;
    private int preGabarites=0;

    private ArrayList<BluetoothDevice> mBTDevices= new ArrayList<>();

    private final BroadcastReceiver mBroadcastReceiver1= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                        String action= intent.getAction();
                        if(action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){
                            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                            switch (state){
                                case BluetoothAdapter.STATE_OFF:
                                    Log.e(Variables.TAG_Deb,"STATE_OFF");
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    Log.e(Variables.TAG_Deb,"STATE_TURNING_OFF");
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    Log.e(Variables.TAG_Deb,"STATE_ON");
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    Log.e(Variables.TAG_Deb,"STATE_TURNING_ON");
                                    break;
                            }
                        }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver2= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.e(Variables.TAG_Deb,"Discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.e(Variables.TAG_Deb,"Discoverability Disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.e(Variables.TAG_Deb,"Discoverability Disabled. Not able to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.e(Variables.TAG_Deb,"Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.e(Variables.TAG_Deb,"Connected");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver3= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(Variables.TAG_Deb,"3");

            String action= intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.e(Variables.TAG_Deb,device.toString());
                deviceListAdapter= new DeviceListAdapter(context,R.layout.device_adapter_view,mBTDevices);
                listViewDevices.setAdapter(deviceListAdapter);
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver4= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(Variables.TAG_Deb,"4");

            String action= intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(mDevice.getBondState()==BluetoothDevice.BOND_BONDED){
                        Log.e(Variables.TAG_Deb,"BOND_BONDED");
                    }
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    Log.e(Variables.TAG_Deb,"BOND_BONDING");
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    Log.e(Variables.TAG_Deb,"BOND_NONE");
                }
            }
        }
    };

    // ---------------------------------------------------------  onCreate ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        buttonOn_Off= findViewById(R.id.buttonOn_Off);
        buttonEnableDescoverability=findViewById(R.id.buttonEnableDescoverability);
        buttonDescover=findViewById(R.id.buttonDescover);
        buttonForward=findViewById(R.id.buttonForward);
        buttonReverse=findViewById(R.id.buttonReverse);
        buttonLeft=findViewById(R.id.buttonLeft);
        buttonRight=findViewById(R.id.buttonRight);
        buttonStop=findViewById(R.id.buttonStop);
        listViewDevices=findViewById(R.id.listDevices);
        buttonOn_Off.setOnClickListener(this);
        buttonEnableDescoverability.setOnClickListener(this);
        buttonDescover.setOnClickListener(this);
        buttonForward.setOnTouchListener(this);
        buttonReverse.setOnTouchListener(this);
        buttonLeft.setOnTouchListener(this);
        buttonRight.setOnTouchListener(this);
        buttonStop.setOnTouchListener(this);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,intentFilter);
        listViewDevices.setOnItemClickListener(this);

    }
    // ---------------------------------------------------------  onDestroy ---------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        }catch (Exception e){
            Log.e(Variables.TAG_Deb, "Exeption1: "+e);
        }
        try {
            unregisterReceiver(mBroadcastReceiver2);
        }catch (Exception e){
            Log.e(Variables.TAG_Deb, "Exeption2: "+e);
        }
        try {
        unregisterReceiver(mBroadcastReceiver3);
        }catch (Exception e){
            Log.e(Variables.TAG_Deb, "Exeption3: "+e);
        }
        try {
        unregisterReceiver(mBroadcastReceiver4);
        }catch (Exception e){
            Log.e(Variables.TAG_Deb, "Exeption4: "+e);
        }
//
//        unregisterReceiver(mBroadcastReceiver2);
//        unregisterReceiver(mBroadcastReceiver3);
//        unregisterReceiver(mBroadcastReceiver4);

    }


    // ---------------------------------------------------------  onClick ---------------------------------------------------------

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.buttonOn_Off:
                Log.e(Variables.TAG_Deb,"Click buttonOn_Off");
                enableDesableBluetooth();
                break;
            case  R.id.buttonEnableDescoverability:
                enableDescoverability();
                break;
            case  R.id.buttonDescover:
//                clearListDevices();
                descoverDevices();
                break;


        }

    }

    // ---------------------------------------------------------  onTouch ---------------------------------------------------------

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                v.setScaleX((float) 0.9);
                v.setScaleY((float) 0.9);
                break;


            case MotionEvent.ACTION_UP:

                v.setScaleX((float) 1);
                v.setScaleY((float) 1);

                switch (v.getId()) {
                    case  R.id.buttonForward:
                        if(preForward==0){action(71);
                        preForward=1;

                        }
                        else{action(61);
                        preForward=0;

                        }
                        break;
                    case  R.id.buttonReverse:
                        if(preGabarites==0){action(72); preGabarites=1;}else{action(62); preGabarites=0;}
                        break;
                    case  R.id.buttonLeft:
                        if(preLeft==0){action(74); preLeft=1;}else{action(64); preLeft=0;}
                        break;
                    case  R.id.buttonRight:
                        if(preRight==0){action(75); preRight=1;}else{action(65); preRight=0;}

                        break;
                    case  R.id.buttonStop:
                        if(preStop==0){action(73); preStop=1;}else{action(63); preStop=0;}
                        break;


                }
                break;
        }
        return false;
    }


    // ---------------------------------------------------------  onItemClick ---------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();
        Log.e(Variables.TAG_Deb,"onItemClick: "+position);
        String name= mBTDevices.get(position).getName();
        String address= mBTDevices.get(position).getAddress();
        Log.e(Variables.TAG_Deb,"Name: "+name);
        Log.e(Variables.TAG_Deb,"Address: "+address);
        Log.e(Variables.TAG_Deb, "Trying create a bond");
//        mBTDevices.get(position).createBond();
        try{
        BluetoothDevice device = mBTDevices.get(position);
        Method m = device.getClass().getMethod(
                "createRfcommSocket", new Class[] {int.class});
        Log.e(Variables.TAG_Deb,"Connecting to: "+device.getAddress());

        clientSocket = (BluetoothSocket) m.invoke(device, 1);
        clientSocket.connect();
        } catch (IOException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (SecurityException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d("BLUETOOTH", e.getMessage());
        }

        //Выводим сообщение об успешном подключении
        Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();

    }


    // ---------------------------------------------------------  enableDesableBluetooth ---------------------------------------------------------

    private void enableDesableBluetooth() {
        if(bluetoothAdapter==null){
            Log.e(Variables.TAG_Deb,"Does not have bluetooth");
            return;
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);
        }

        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);
        }
    }
    // ---------------------------------------------------------  enableDescoverability ---------------------------------------------------------

    private void enableDescoverability() {
        Log.e(Variables.TAG_Deb,"Making descoverable on 300 seconds");
        Intent intentDiscoverable= new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(intentDiscoverable);

        IntentFilter intentFilter= new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    private void clearListDevices() {
        mBTDevices= new ArrayList<>();
        deviceListAdapter= new DeviceListAdapter(this,R.layout.device_adapter_view,mBTDevices);
        listViewDevices.setAdapter(deviceListAdapter);
    }

    // ---------------------------------------------------------  enableDescoverability ---------------------------------------------------------

    private void descoverDevices() {
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.e(Variables.TAG_Deb,"Canceling descovery");

            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter intentDescoveryDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,intentDescoveryDevices);
        }

        if(!bluetoothAdapter.isDiscovering()){
            Log.e(Variables.TAG_Deb,"Start descovery");
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter intentDescoveryDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,intentDescoveryDevices);
        }
    }

    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Log.e(Variables.TAG_Deb,"CheckPermissons needed");

            int permissionCheck = this.checkSelfPermission("Manifest.permisson.ACCESS_FINE_LOCATION");
            permissionCheck+=this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            Log.e(Variables.TAG_Deb,"checkPermissons result: "+permissionCheck);

            if(permissionCheck<=0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }else{
            Log.e(Variables.TAG_Deb,"No checkPermissons needed");
        }

    }

    // ---------------------------------------------------------  action ---------------------------------------------------------

    private void action(int value) {
        //Пытаемся послать данные
        if(clientSocket!=null){
        try {
            //Получаем выходной поток для передачи данных
            OutputStream outStream = clientSocket.getOutputStream();



            //В зависимости от того, какая кнопка была нажата,
            //изменяем данные для посылки

            //Пишем данные в выходной поток
            Log.e(Variables.TAG_Deb,"Sending BT: "+value);
            outStream.write(value);

        } catch (IOException e) {
            //Если есть ошибки, выводим их в лог
            Log.d("BLUETOOTH", e.getMessage());
        }
    }
    }



}
