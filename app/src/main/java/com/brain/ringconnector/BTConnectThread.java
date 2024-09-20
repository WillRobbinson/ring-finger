package com.brain.ringconnector;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

class BTConnectThread extends Thread {
    public static final String OUR_UUID = "10dea90c-a7a0-4ad8-8ec8-b1dfb29c0659";
    private final BluetoothServerSocket mmServerSocket;
    //private final BluetoothDevice mmDevice;
    private final String TAG = "BTConnectThread";
    private final BluetoothAdapter btAdaptor;
    private BluetoothSocket bluetoothSocket = null;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Handler handler; // handler that gets info from Bluetooth service
    InputStream mmInStream;
    OutputStream mmOutStream;
    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }
    public BTConnectThread(/*BluetoothDevice device,*/ BluetoothAdapter btAdaptor) {
        Log.i(TAG,"enter");
        this.btAdaptor = btAdaptor;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothServerSocket tmp = null;
        //mmDevice = device;

        try {
            tmp = btAdaptor.listenUsingInsecureRfcommWithServiceRecord("Android Ring Monitor",UUID.fromString(OUR_UUID));
            Log.i(TAG,"after listenUsingInsecureRfcommWithServiceRecord");
        } catch (SecurityException se) {
            Log.e(TAG, "createRfcommSocketToServiceRecord Security Exception", se);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmServerSocket = tmp;
        bluetoothSocket = getSocket(mmServerSocket);
        if (bluetoothSocket != null) {
            Log.i(TAG, "leaving constructor with non-null bluetoothSocket");
        }
    }

    private BluetoothSocket getSocket(BluetoothServerSocket serverSocket) {
        Log.i(TAG, "getSocket enter from server socket");
        BluetoothSocket socket;
        try {
            socket = mmServerSocket.accept();
            Log.i(TAG, "after accept socket");
        } catch (IOException e) {
            Log.e(TAG, "Socket's accept() method failed", e);
            return null;
        }

        if (socket != null) {
            Log.i(TAG, "socket not null");
            // A connection was accepted. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(socket);
            try {
                mmServerSocket.close();
                Log.i(TAG, "closed socket server");
            } catch (IOException i) {
                Log.e(TAG,i.getMessage());
            }
            Log.i(TAG, "returning non-null socket");
            return socket;
        }
        Log.i(TAG, "returning null socket");
        return null;
    }

    public void run() {

        Log.i(TAG,"running");
        if (bluetoothSocket != null) {
            Log.i(TAG,"bluetoothSocket still not null");
        } else {
            Log.i(TAG,"bluetoothSocket is null, returning from run");
            return;
        }

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            mmInStream = bluetoothSocket.getInputStream();
            Log.i(TAG,"got inputStream");
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            mmOutStream = bluetoothSocket.getOutputStream();
            Log.i(TAG,"got outputStream");
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        // Cancel discovery because it otherwise slows down the connection.
        try {
            btAdaptor.cancelDiscovery();
            Log.i(TAG,"canceled discovery");
        } catch (SecurityException se) {
            Log.e(TAG, "cancelDiscovery Security Exception", se);
        }

//        try {
//            // Connect to the remote device through the socket. This call blocks
//            // until it succeeds or throws an exception.
//            bluetoothSocket.connect();
//            Log.i(TAG,"connected");
//        } catch (SecurityException se) {
//            Log.e(TAG, "connect Security Exception", se);
//        } catch (IOException connectException) {
//            // Unable to connect; close the socket and return.
//            try {
//                bluetoothSocket.close();
//            } catch (IOException closeException) {
//                Log.e(TAG, "Could not close the client socket", closeException);
//            }
//            return;
//        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        getTheRingDataFromSocket(bluetoothSocket);
        Log.i(TAG,"after data retrieval");

    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public void getTheRingDataFromSocket(BluetoothSocket mmSocket) {
        Log.i(TAG, "getTheRingDataFromSocket");
        BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                Log.i(TAG, "Read Json:[" + line + "]");
                Gson gson = new GsonBuilder().setLenient().create();
                SensorData sensorData = gson.fromJson(line, SensorData.class);
                Log.i(TAG, "after Gson conversion: sensorData[" + sensorData + "]");
                // Process sensorData here
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading from socket", e);
        }
    }
//    public void getTheRingDataFromSocket(BluetoothSocket mmSocket) {
//        Log.i(TAG,"getTheRingDataFromSocket");
//
//        mmBuffer = new byte[1024];
//        int numBytes; // bytes returned from read()
//
//        // Keep listening to the InputStream until an exception occurs.
//        while (true) {
//            try {
//                Log.i(TAG,"pre read");
//                // Read from the InputStream.
//                //numBytes = mmInStream.read(mmBuffer);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
//                Log.i(TAG,"After BufferedReader constructions");
//                String jsonString = reader.readLine();
//                Log.i(TAG,"Read Json:["+jsonString+"]");
//
//                Gson gson = new GsonBuilder()
//                        .registerTypeAdapter(Long.class, new TimestampDeserializer())
//                        .create();
//                SensorData sensorData = gson.fromJson(jsonString, SensorData.class);
//                // Send the obtained bytes to the UI activity.
////                Message readMsg = handler.obtainMessage(
////                        MessageConstants.MESSAGE_READ, numBytes, -1,
////                        mmBuffer);
////                readMsg.sendToTarget();
//                Log.i(TAG,"after Gson conversion: sensorData["+sensorData+"]");
//            } catch (IOException e) {
//                Log.d(TAG, "Input stream was disconnected", e);
//                break;
//            }
//        }
//    }
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }
}