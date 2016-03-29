package conor.ie.dcu.multimeterapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity   {

    private final static int REQUEST_BLUETOOTH = 2;

    BluetoothConnector bluetoothConnector = null;
    Button startMeasurementButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startMeasurementButton = (Button)findViewById(R.id.measurementButton);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothConnector = new BluetoothConnector(this,btAdapter);
        checkBluetoothCompatibility(btAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}


