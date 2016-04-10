package conor.ie.dcu.multimeterapp;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnector extends AppCompatActivity {
    final int MESSAGE_LENGTH = 9;
    //SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private String message = "", result;
    private TextView resultTextView;
    private Thread receiveThread;
    private boolean messageReceived = false;

    BluetoothConnector (Activity activity, BluetoothAdapter bluetoothAdapter){
        resultTextView = (TextView) activity.findViewById(R.id.measurementResult);
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public Set<BluetoothDevice> getPairedDevices(){
        return  bluetoothAdapter.getBondedDevices();
    }

    public void connectToDevice(String adr) {

        // Set up a Bluetooth device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(adr);

        // Two things are needed to make a connection:A UUID and MAC address
        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d("ConnectToDevice", "Error in creating" + e.getMessage() + ".");
        }
        //Cancels discovery to save resources
        bluetoothAdapter.cancelDiscovery();

        //Creates a connection to the device
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
//                bluetoothSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                //bluetoothSocket.connect();
                bluetoothSocket.close();
            } catch (IOException e2) {
                Log.d("ConnectToDevice", "Error,unable to close socket during connection failure" + e.getMessage());
            }
        }
        if(bluetoothSocket.isConnected()){
            // Create two data streams so we can talk back and forth to the device
            try {
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.d("ConnectToDevice", "Error, unable to open input and output stream" + e.getMessage());
            }
        }
    }

    //send data to the BT device
    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            outputStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d ("sendData","Error, an exception occurred during write: " + e.getMessage());
        }
    }

    //creates a thread to receive data from the BT device
    public void receiveData() {
        final Handler handler = new Handler();
        final byte[] buffer = new byte[1024];
        Log.i("receiveData", "Started Listening");

        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        int bytes = inputStream.read(buffer);
                        Log.i("receiveData", "Reading Buffer, received bytes = " + bytes );
                        //checks if the message if long enough
                        if (bytes >= MESSAGE_LENGTH) {
                            for (int i = 0; i < bytes; i++) {
                                //loops through the message chars and checks for the start of the message packet
                                String s = new String(buffer, i, 1);
                                if (s.equals("#")) {
                                    message = new String(buffer, i , MESSAGE_LENGTH);
                                    Log.i("ReceiveData", "Message received: " + message);
                                    //get sensor value from message using sub strings
                                    result = message.substring(1, 9);
                                    messageReceived = true;
                                    break;
                                }
                            }
                        }
                    }catch(Exception ex){
                        Log.d("receiveData", "Exception occurred during read: " + ex.getMessage());
                    }
                    //Updates the UI with the received result
                    handler.post(new Runnable() {
                        public void run() {
                            //update the text views with sensor values
                            if(messageReceived) {
                                resultTextView.setText(result + " V");
                            }
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.d("receiveData", "Error, in putting Thread to sleep" + e.getMessage());
                    }
                }

            }
        };

        receiveThread = new Thread(runnable);
        receiveThread.start();
    }

    //stop receiving data from the BT device
    public void stopReceivingData() {
        if(receiveThread.isAlive()) {
            Log.i("stopReceivingData", " Measurement Disabled");
            receiveThread.interrupt();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

