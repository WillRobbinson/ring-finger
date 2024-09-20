package com.brain.ringconnector;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
    private List<SensorData> dataList = new ArrayList<>();
    private static final int BATCH_SIZE = 100;

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }
    public BTConnectThread(BluetoothAdapter btAdaptor) {
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

    private OnDataReceivedListener dataListener;

    public interface OnDataReceivedListener {
        void onDataReceived(SensorData data);
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.dataListener = listener;
    }

    @Override
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

                if (dataListener != null) {
                    dataListener.onDataReceived(sensorData);
                    dataList.add(sensorData);
                }

                if (dataList.size() >= BATCH_SIZE) {
                    Log.i(TAG,"Just sent another 100 readings");
                    uploadData(dataList);
                    dataList.clear();
                }

                Log.i(TAG, "after Gson conversion: sensorData[" + sensorData + "]");
                // Process sensorData here
                //dataSource.insertSensorData(sensorData);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading from socket", e);
        }
    }

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

    private void uploadData(List<SensorData> data) {
        // Convert data to a single string
        StringBuilder sb = new StringBuilder();
        for (SensorData sd : data) {
            sb.append(new Gson().toJson(sd)).append("\n");
        }
        String dataString = sb.toString();

        // Upload to a public storage (e.g., Pastebin)
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("https://pastebin.com/api/api_post.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String postData = "api_dev_key=ephi3_YTVWqG9FFklNgOKDyuUnn2PE8r" +
                            "&api_option=paste" +
                            "&api_paste_code=" + URLEncoder.encode(params[0], "UTF-8") +
                            "&api_paste_private=0" +
                            "&api_paste_name=SensorData_" + System.currentTimeMillis();

                    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                        wr.writeBytes(postData);
                    }

                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder result = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();

                    return result.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Error uploading data", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.i(TAG, "Data uploaded: " + result);
                } else {
                    Log.e(TAG, "Failed to upload data");
                }
            }
        }.execute(dataString);
    }
}