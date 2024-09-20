package com.brain.ringconnector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button scanPairedDevices;
    private Button exitButton;
    private ListView scannedDeviceslistView;
    private ArrayAdapter adapter;
    private List<String> itemList;
    private MainActivity thisMainActivity;
    private SensorDataSource dataSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"onCreate");

        dataSource = new SensorDataSource(this);
        dataSource.open();

        thisMainActivity = this;

        scanPairedDevices = findViewById(R.id.pairedDevicesButton);
        scanPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"scanPairedDevices clicked");
                if (ActivityCompat.checkSelfPermission(thisMainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Log.i(TAG,"Just passed fake permission check.");
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    Log.i(TAG,"Found at least one paired device.");
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        Log.i(TAG,"Connecting to Device if its ours: name["+deviceName+"] address["+deviceHardwareAddress+"]");
                        if (deviceName.equals("PiTime")) {
//                            device.createInsecureRfcommSocketToServiceRecord("")
                        }
                    }
                } else {
                    Log.i(TAG,"No paired Devices");
                    Toast.makeText(MainActivity.this, "No paired Devices", Toast.LENGTH_SHORT).show();
                }
            }
        });

        scannedDeviceslistView = findViewById(R.id.scannedDeviceslistView);
        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        scannedDeviceslistView.setAdapter(adapter);

        // Add initial items

        // If there is something on the list and they click on it, then try to connect to it.
        scannedDeviceslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = itemList.get(position);
                Log.i(TAG,"Clicked on scannedDeviceslistView item: "+selectedDevice);
                Toast.makeText(MainActivity.this, "Connect to device: " + selectedDevice, Toast.LENGTH_SHORT).show();
                // TODO Code to connect to device goes here, either match selected to paired, or find in itemList?
            }
        });

        exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(1);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
//        FragmentActivity activity = getActivity();
        if (mBluetoothAdapter == null) { // && activity != null) {
            Log.i(TAG,"Device does not support bluetooth");
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        } else {
            Log.i(TAG, "Bluetooth supported");
            if (!mBluetoothAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth NOT enabled, getting permissions");
                requestBluetoothPermissions();
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.i(TAG, "Bluetooth IS enabled, getting permissions");
                requestBluetoothPermissions();
            }
        }

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Log.i(TAG,"InsetsListener");
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method to handle received sensor data
    public void onSensorDataReceived(SensorData data) {
        dataSource.insertSensorData(data);
        // You can also update UI or perform other operations here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataSource != null) {
            dataSource.close();
        }
        // Make sure to properly close the Bluetooth connection as well
    }

    private void requestBluetoothPermissions() {
        Log.i(TAG,"requestBluetoothPermissions");
        requestPermissions(
                new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                },
                BLUETOOTH_PERMISSION_REQUEST_CODE
        );
        Log.i(TAG,"requestBluetoothPermissions exit");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.i(TAG,"onRequestPermissionsResult called, go advertise a connection, requestCode["+requestCode+"]");

        for (String permission:permissions) {
            Log.i(TAG,"Permission["+permission+"]");
        }
        for (int grantResult:grantResults) {
            Log.i(TAG,"GrantResult["+grantResult+"]");
        }
        // TODO - need to launch this everytime they say we have permissions to do it
        // TODO - but what is the go signal?
        // Start up connection server thread
        Log.i(TAG,"BTConnectThread allocated next");
        BTConnectThread btConnectThread = new BTConnectThread(mBluetoothAdapter);
        Log.i(TAG,"BTConnectThread created");
        btConnectThread.setOnDataReceivedListener(this::onSensorDataReceived);
        btConnectThread.start();
        Log.i(TAG,"BTConnectThread started");
    }

    private void addItem(String item) {
        itemList.add(item);
        adapter.notifyDataSetChanged();
    }
}