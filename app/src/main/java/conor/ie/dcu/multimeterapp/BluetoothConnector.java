package conor.ie.dcu.multimeterapp;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnector extends AppCompatActivity {
    private BluetoothAdapter BTAdapter;

    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inputStream = null;
    private String message = "", result;
    TextView resultTextView;
//    TextView currentResultTextView;
//    TextView resistanceResultTextView;
    Thread receiveThread;
    private int messageLength = 9;
    private boolean messageReceived = false;
    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    BluetoothConnector (Activity activity, BluetoothAdapter bluetoothAdapter){

        resultTextView = (TextView) activity.findViewById(R.id.measurementResult);
//        currentResultTextView = (TextView) activity.findViewById(R.id.currentResult);
//        resistanceResultTextView = (TextView) activity.findViewById(R.id.resistanceResult);
        BTAdapter = bluetoothAdapter;
    }


    public Set<BluetoothDevice> getPairedDevices(){
        return  BTAdapter.getBondedDevices();
    }


    public void connectToDevice(String adr) {

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = BTAdapter.getRemoteDevice(adr);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d("ConnectToDevice", "Error in creating" + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        BTAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        try {
            outStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();
        } catch (IOException e) {
            //errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d ("SendData()","In onResume() and an exception occurred during write: " + e.getMessage());
        }
    }

    public void receiveData() {
        final Handler handler = new Handler();
        final byte[] buffer = new byte[1024];
        Log.i("ReceiveData", "Started Listening");

        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
//                    if(buffer.length == 1024){
//
//                    }
                    try {
                        int bytes = inputStream.read(buffer);
                        Log.i("ReceiveData", "Reading Buffer, recieved bytes = " + bytes );
                        if (bytes >= messageLength) {
                            for (int i = 0; i < bytes; i++) {
                                String s = new String(buffer, i, 1);
                                if (s.equals("#")) {

                                    message = new String(buffer, i , messageLength);
                                    Log.i("ReceiveData", "Message received: " + message);
                                    //get sensor value from string using sub strings
                                    result = message.substring(1, 9);
//                                    currentResult  = message.substring(10, 18);
//                                    resistanceResult = message.substring(19, 27);
                                    messageReceived = true;
                                    break;
                                }
                            }
                        }
                    }catch(Exception ex){
                        Log.d("ReceiveData", "Exception occurred during read: " + ex.getMessage());
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            //update the text views with sensor values
                            if(messageReceived) {
                                resultTextView.setText(result + " V");
//                                currentResultTextView.setText(currentResult + " A");
//                                resistanceResultTextView.setText(resistanceResult + " Ohms");
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        receiveThread = new Thread(runnable);
        receiveThread.start();
    }

    public void stopReceivingData() {
        if(receiveThread.isAlive()) {
            Log.i("MultimeterApp", " Measurement Disabled");
            receiveThread.interrupt();
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        connectToDevice(address);
        BluetoothDevice device = BTAdapter.getRemoteDevice(address);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (BTAdapter.isDiscovering()) {
            BTAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        BTAdapter.startDiscovery();
    }

    public String getMessage() {
        return message;
    }
}

