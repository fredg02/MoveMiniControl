package microbit.movemini;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MoveMini";
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;
    private Button connectButton;

    private BleConnection mBleConnection;

    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        upButton = (Button) findViewById(R.id.up);
        downButton = (Button) findViewById(R.id.down);
        leftButton = (Button) findViewById(R.id.left);
        rightButton = (Button) findViewById(R.id.right);
        connectButton = (Button) findViewById(R.id.connect);

        setTouchListeners();

        mBleConnection = new BleConnection(this);
        mBleConnection.addListener(new ConnectionListener() {

            @Override
            public void connectionStateChanged(final BleConnection.State state) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (state == BleConnection.State.CONNECTED) {
                            isConnected = true;
                            connectButton.setText(getText(R.string.disconnect));
                        } else if (state == BleConnection.State.CONNECTING) {
                            isConnected = false;
                            connectButton.setText(getText(R.string.connecting));
                        } else {
                            isConnected = false;
                            connectButton.setText(getText(R.string.connect));
                        }
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can do a BLE scan.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    private void setTouchListeners() {
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand(BleConnection.MES_DPAD_BUTTON_1_DOWN, event);
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand(BleConnection.MES_DPAD_BUTTON_2_DOWN, event);
                return true;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand(BleConnection.MES_DPAD_BUTTON_3_DOWN, event);
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand(BleConnection.MES_DPAD_BUTTON_4_DOWN, event);
                return true;
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBleConnection != null) {
                    if (isConnected) {
                        mBleConnection.disconnect();
                    } else {
                        mBleConnection.connect();
                    }
                }
            }
        });

    }

    private void handleDirectionCommand(int buttonId, MotionEvent event) {
        if (mBleConnection != null && mBleConnection.isConnected()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // send direction
                    mBleConnection.sendDirectionPacket(buttonId);
                    break;
                case MotionEvent.ACTION_UP:
                    // send stop
                    mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_1_UP);
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover BLE devices when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

}
