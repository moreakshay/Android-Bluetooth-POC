package moreakshay.com.androidbluetoothpoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothHelper {

    private Context context;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private String macAddress;
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public BluetoothHelper(Context context, String macAddress) {
        this.macAddress = macAddress;
        this.context = context;
        setBluetooth();
    }

    private void setBluetooth() {
        Log.d("BLUETOOTH", "initializing device");
        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
        if (device != null) {
            setSocket();
        } else {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_LONG).show();
        }
    }

    private void setSocket() {
        try {
            Log.d("BLUETOOTH", "initializing socket");
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                socket = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
            } else {
                Log.d("BLUETOOTH", "creating secure socket");
                socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
            }
            Log.d("BLUETOOTH", "connecting to socket");
                connectSocket();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("BLUETOOTH", "Socket failed");
        }
    }

    private void connectSocket() {

     Runnable runnable = new Runnable() {
         @Override
         public void run() {
             try {
                 if (socket != null && !socket.isConnected()) {
                     socket.connect();
                 }
             } catch (IOException e) {
                 try {
                     Class<?> clazz = device.getClass();
                     Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                     Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                     Object[] params = new Object[]{Integer.valueOf(1)};

                     socket = (BluetoothSocket) m.invoke(device, params);
                     socket.connect();
                 } catch (Exception ee) {
                     ee.printStackTrace();
                     e.printStackTrace();
                     Log.d("BLUETOOTH", e.getMessage());
                     try {
                         socket.close();
                     } catch (IOException e1) {
                         e1.printStackTrace();
                         Log.d("BLUETOOTH", e1.getMessage());
                         return;
                     }
                 }
             }
             if (socket != null && !socket.isConnected()) {
                 connectSocket();
             }
         }
     };

     scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
//        new Handler().post();
    }

    public void closeSocket() {
        if (socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean sendData(final String payload) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(payload.getBytes());
            outputStream.flush();
            outputStream.close();
            Log.d("DATA SEND", true + payload);
            Toast.makeText(context, "Order Placed", Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DATA FAILED", false + "");
            Toast.makeText(context, "Order Failed", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
