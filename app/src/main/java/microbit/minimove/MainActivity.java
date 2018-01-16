package microbit.minimove;

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
    private Button up;
    private Button down;
    private Button left;
    private Button right;
    private Button connect;
    private Button disconnect;

    private BleConnection mBleConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);

        setTouchListeners();

        mBleConnection = new BleConnection(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
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
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand("forward", event);
                return true;
            }
        });

        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand("backward", event);
                return true;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand("left", event);
                return true;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleDirectionCommand("right", event);
                return true;
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBleConnection != null) {
                    mBleConnection.connect();
                }
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBleConnection != null) {
                    mBleConnection.disconnect();
                }
            }
        });
    }

    private void handleDirectionCommand(String direction, MotionEvent event) {
        if (mBleConnection != null && mBleConnection.isConnected()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ("forward".equalsIgnoreCase(direction)) {
                        driveForward();
                    } else if ("backward".equalsIgnoreCase(direction)) {
                        driveBackward();
                    } else if ("left".equalsIgnoreCase(direction)) {
                        turnLeft();
                    } else if ("right".equalsIgnoreCase(direction)) {
                        turnRight();
                    } else {
                        stop();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    stop();
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void driveForward() {
        mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_1_DOWN);
    }

    private void driveBackward() {
        mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_2_DOWN);
    }

    private void turnLeft() {
        mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_3_DOWN);
    }

    private void turnRight() {
        mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_4_DOWN);
    }

    private void stop() {
        mBleConnection.sendDirectionPacket(BleConnection.MES_DPAD_BUTTON_1_UP);
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
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
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
