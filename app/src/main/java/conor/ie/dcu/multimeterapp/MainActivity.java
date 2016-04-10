package conor.ie.dcu.multimeterapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_BLUETOOTH = 2;

    BluetoothConnector bluetoothConnector = null;
    Button startMeasurementButtonVoltage, startMeasurementButtonCurrent, startMeasurementButtonResistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startMeasurementButtonVoltage = (Button)findViewById(R.id.measurementButtonV);
        startMeasurementButtonCurrent = (Button)findViewById(R.id.measurementButtonC);
        startMeasurementButtonResistance = (Button)findViewById(R.id.measurementButtonR);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothConnector = new BluetoothConnector(this,btAdapter);
        checkBluetoothCompatibility(btAdapter);

    }


   boolean measurementStarted = false;
    public void startMeasurement(View view){
        Button measurementButton = null;
        String modeSelector = "0";
        switch (view.getId()) {
            case R.id.measurementButtonV:
                measurementButton = (Button)findViewById(R.id.measurementButtonV);
                modeSelector = "1";
                break;
            case R.id.measurementButtonC:
                measurementButton = (Button)findViewById(R.id.measurementButtonC);
                modeSelector = "2";
                break;
            case R.id.measurementButtonR:
                measurementButton = (Button)findViewById(R.id.measurementButtonR);
                modeSelector = "3";
                break;
        }

        if(!measurementStarted){
            measurementButton.setText("Stop Measuring");
            bluetoothConnector.connectToDevice("20:15:12:08:46:11");
            bluetoothConnector.sendData(modeSelector);
            bluetoothConnector.receiveData();
            measurementStarted = true;
        }else{
            bluetoothConnector.sendData("0");
            bluetoothConnector.stopReceivingData();
            measurementButton.setText("Start Measuring");
            measurementStarted = false;
        }
    }



    private void checkBluetoothCompatibility(BluetoothAdapter bluetoothAdapter){
        if(bluetoothAdapter != null){
            Log.i("BluetoothCompatibility", "Bluetooth is enabled");
            if (!bluetoothAdapter.isEnabled() ) { //prompt to turn on Bluetooth
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
        }else{ // Phone does not support Bluetooth so let the user know and exit.
            Log.d("BluetoothCompatibility", "Bluetooth Not supported. Aborting.");
            finish();
        }

    }

}


