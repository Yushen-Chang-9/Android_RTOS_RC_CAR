package com.example.yushen.myapplication;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

public class MainActivity extends AppCompatActivity implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);

        bluetoothSerial.setup();

        //calling widgets
        final Button forward_button      = (Button) findViewById(R.id.forward_button);
        final Button backward_button      = (Button) findViewById(R.id.backward_button);
        final Button left_button   = (Button) findViewById(R.id.left_button);
        final Button right_button   = (Button) findViewById(R.id.right_button);
        final Button stop_button   = (Button) findViewById(R.id.stop_button);
        final Button bt_connect     = (Button) findViewById(R.id.bt_connect);
        final TextView status_text  = (TextView) findViewById(R.id.textView);

        bt_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                showDeviceListDialog();
            }
        });


        forward_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //DO ERROR HANDLING!!!!

                bluetoothSerial.write("forward#",false);
                status_text.setText("forward Enabled");
            }
        });

        backward_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                bluetoothSerial.write("backward#",false);
                status_text.setText("backward Enabled");


            }
        });

        left_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                bluetoothSerial.write("left#",false);
                status_text.setText("left Enabled");

            }
        });

        right_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                bluetoothSerial.write("right#",false);
                status_text.setText("right Enabled");

            }
        });

        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                bluetoothSerial.write("stop#",false);
                status_text.setText("stop Enabled");

            }
        });

    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle("paired devices");
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
    }

    //compile time check: method to be overriden
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = "Connecting";
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = bluetoothSerial.getConnectedDeviceName();
                break;
            default:
                subtitle = "Disconnected";
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public void onBluetoothNotSupported() {
        Log.v("MyAPP", "Drat! Bluetooth not supported!");
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        Log.v("MyAPP", "got message: " + message);
    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        Log.v("MyAPP", "sent message: " + message);
    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

}
