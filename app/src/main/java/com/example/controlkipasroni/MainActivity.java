package com.example.controlkipasroni;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private TextView valSuhu, valKelembapan, statusBluetooth;
    private Button btnOn, btnOff, btnSpeed1, btnSpeed2, btnSpeed3, btnPair, btnDiscover, btnOnAlat, btnOffAlat;
    private ListView listBT;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;


    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicomponents();

        listBT.setAdapter(mBTArrayAdapter); // assign model to view
        listBT.setOnItemClickListener(mDeviceClickListener);
        btnOff.setVisibility(View.INVISIBLE);
        Resources res = this.getResources();

        btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
        btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
        btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
        btnSpeed1.setEnabled(false);
        btnSpeed2.setEnabled(false);
        btnSpeed3.setEnabled(false);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    valKelembapan.setText(readMessage+"%");
                    valSuhu.setText(readMessage+"*C");
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        statusBluetooth.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        statusBluetooth.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            statusBluetooth.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        } else {
            btnOnAlat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("1");
                    btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed1.setEnabled(true);
                    btnSpeed2.setEnabled(true);
                    btnSpeed3.setEnabled(true);
                    btnOffAlat.setVisibility(View.VISIBLE);
                    btnOnAlat.setVisibility(View.INVISIBLE);
                }
            });

            btnOffAlat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("0");
                    btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                    btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                    btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                    btnSpeed1.setEnabled(false);
                    btnSpeed2.setEnabled(false);
                    btnSpeed3.setEnabled(false);
                    btnOnAlat.setVisibility(View.VISIBLE);
                    btnOffAlat.setVisibility(View.INVISIBLE);
                }
            });

            btnSpeed1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("2");
                    btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                    btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                }
            });

            btnSpeed2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("3");
                    btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                    btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                }
            });

            btnSpeed3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("4");
                    btnSpeed1.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed2.setBackground(res.getDrawable(R.drawable.btn_current_speed));
                    btnSpeed3.setBackground(res.getDrawable(R.drawable.btn_uncurrent_speed));
                }
            });

            btnOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                    btnOn.setVisibility(View.INVISIBLE);
                    btnOff.setVisibility(View.VISIBLE);
                }
            });

            btnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                    btnOff.setVisibility(View.INVISIBLE);
                    btnOn.setVisibility(View.VISIBLE);
                }
            });

            btnPair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            btnDiscover.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }

    private void inicomponents() {
        valSuhu = (TextView) findViewById(R.id.valSuhu);
        valKelembapan = (TextView) findViewById(R.id.valKelembapan);
        statusBluetooth = (TextView) findViewById(R.id.statusBluetooth);
//        readComunication = (TextView) findViewById(R.id.readComunication);
        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);
        btnOnAlat = (Button) findViewById(R.id.btnOnAlat);
        btnOffAlat = (Button) findViewById(R.id.btnOffAlat);
        btnSpeed1 = (Button) findViewById(R.id.btn_speed1);
        btnSpeed2 = (Button) findViewById(R.id.btn_speed2);
        btnSpeed3 = (Button) findViewById(R.id.btn_speed3);
        btnPair = (Button) findViewById(R.id.btnPair);
        btnDiscover = (Button) findViewById(R.id.btnDiscover);
        listBT = (ListView) findViewById(R.id.devicesListView);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
    }

    private void bluetoothOn(View view) {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            statusBluetooth.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff(View view) {
        mBTAdapter.disable(); // turn off
        statusBluetooth.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                statusBluetooth.setText("Enabled");
            } else {
                statusBluetooth.setText("Disabled");
            }
        }
    }

    private void discover(View view) {
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear();
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            for (BluetoothDevice device : mPairedDevices){
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            statusBluetooth.setText("Connecting...");
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            new Thread() {
                public void run() {
                    boolean fail = false;
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }

                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}