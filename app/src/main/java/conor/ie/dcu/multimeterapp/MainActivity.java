package conor.ie.dcu.multimeterapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_BLUETOOTH = 2;

    BluetoothConnector bluetoothConnector = null;
    Button startMeasurementButton;
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startMeasurementButton = (Button)findViewById(R.id.measurementButton);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothConnector = new BluetoothConnector(this,btAdapter);
        checkBluetoothCompatibility(btAdapter);

    }


   boolean measurementStarted = false;
    public void startMeasurement(View view){
        Button measurementButton = (Button) this.findViewById(R.id.measurementButton);
        if(!measurementStarted){
            measurementButton.setText("Stop Measuring");
            bluetoothConnector.connectToDevice("20:15:12:08:46:11");
            bluetoothConnector.receiveData();
            measurementStarted = true;
        }else{
            bluetoothConnector.stopReceivingData();
            measurementButton.setText("Start Measuring");
            measurementStarted = false;
        }
    }


    // Phone does not support Bluetooth so let the user know and exit.
    private void checkBluetoothCompatibility(BluetoothAdapter bluetoothAdapter){
        if(bluetoothAdapter != null){
            Log.i("MultimeterApp", "Bluetooth is enabled");
            if (!bluetoothAdapter.isEnabled() ) { //prompt to turn on Bluetooth
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
        }else{
            Log.d("MULTIMETER", "Bluetooth Not supported. Aborting.");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
        }
        return false;
    }


}


