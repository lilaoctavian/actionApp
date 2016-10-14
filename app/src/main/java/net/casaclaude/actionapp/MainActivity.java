package net.casaclaude.actionapp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;




public class MainActivity extends AppCompatActivity {


    //Write to input.txt for testing application
    private void writeToInputFile(String data) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard,"input.txt");
            //File myFile = new File("/sdcard/input.txt");
            myFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(myFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write input file failed: " + e.toString());
        }
    }

    //Write output info to output.txt
    public static void writeToFile(String data) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard,"output.txt");
            myFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(myFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Write output info to output.txt
    private void appendToFile(String data) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File myFile = new File(sdcard,"output.txt");
            myFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(myFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void listFileContent() {
        File sdcard = Environment.getExternalStorageDirectory();
        File myFile = new File(sdcard,"output.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(myFile))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("List output: "+line);
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File not found: " + e.toString());
        }
    }

    private String readUrl() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"ftptransfer-url.txt");

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }

        return text.toString();
    }

    public static boolean isFileExists(String filename){
        File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder1.exists();
    }

    private boolean removeFile(String filename){
        File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder1.delete();
    }

    //Read input.txt for action command and arguments
    //input.txt should contain 1 line with action command and ":" and arguments
    private String[] readInput() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"input.txt");
        StringBuilder text = new StringBuilder();
        String myString = "";
        String action = "";
        String argument = "";
        String[] inputData = {};
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                myString = myString + line;
            }
            br.close();
            inputData = myString.split(":");
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }

        System.out.println(inputData.toString());
        return inputData;
    }

    //Function implement FTP download functionality
    //TBD


    //Function implement the list of cell
    private String getCellIdentity() {
        final TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        String getCellId = "NO NETWORK/NOT REGISTERED";

        if (cellInfoList != null) {
            for (CellInfo cellInfo : cellInfoList)
            {
                if (cellInfo.isRegistered())
                {
                    getCellId = cellInfo.toString();
                }
            }
        }
        else if (cellLocation != null) {
            getCellId = "CellLocation:{mCid=" + cellLocation.getCid() + " mLac=" + cellLocation.getLac() + " mPsc=" + cellLocation.getPsc() + "}";
        }
        return getCellId;
    }

    private ArrayList<ArrayList<String>> getAllCellIdentity() {

        final TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();

        String getCellIdentity = "NO NETWORK/NOT REGISTERED";
        ArrayList<ArrayList<String>> allCellInfoList = new ArrayList<ArrayList<String>>();
        ArrayList<String> allCellInfoListTmp = new ArrayList<String>();

        if (cellInfoList != null) {
            for (CellInfo cellInfo : cellInfoList) {
                allCellInfoListTmp = new ArrayList<String>();
                if (cellInfo.isRegistered())
                {
                    getCellIdentity = cellInfo.toString();
                    allCellInfoListTmp.add("REGISTERED:");
                    allCellInfoListTmp.add(getCellIdentity);
                } else {
                    getCellIdentity = cellInfo.toString();
                    allCellInfoListTmp.add("NOT REGISTERED:");
                    allCellInfoListTmp.add(getCellIdentity);
                }
                allCellInfoList.add(allCellInfoListTmp);
            }
        }
        else if (cellLocation != null) {
            getCellIdentity = "CellLocation:{mCid=" + cellLocation.getCid() + " mLac=" + cellLocation.getLac() + " mPsc=" + cellLocation.getPsc() + "}";
            allCellInfoListTmp.add("LOCATION:");
            allCellInfoListTmp.add(getCellIdentity);
            allCellInfoList.add(allCellInfoListTmp);
        }
        return allCellInfoList;
    }

    private void goToUrl (String url) {
        final TextView tv=(TextView)findViewById(R.id.myText);
        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        Intent webOpen = new Intent(android.content.Intent.ACTION_VIEW);
        webOpen.setData(Uri.parse(url));
        startActivity(webOpen);
    }

    private String getDataActivity() {
        String dataActivityName = "";
        final TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Integer dataState = telephonyManager.getDataActivity();
        if (dataState == 0) {
            dataActivityName = "DATA_ACTIVITY_NONE";
        } else if (dataState == 1) {
            dataActivityName = "DATA_ACTIVITY_IN";
        } else if (dataState == 2) {
            dataActivityName = "DATA_ACTIVITY_OUT";
        } else if (dataState == 3) {
            dataActivityName = "DATA_ACTIVITY_INOUT";
        } else if (dataState == 4) {
            dataActivityName = "DATA_ACTIVITY_DORMANT";
        }
        return dataActivityName;
    }

    private String getDataState() {
        String dataStateName = "";
        final TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Integer dataState = telephonyManager.getDataState();
        if (dataState == 0) {
            dataStateName = "DATA_DISCONNECTED";
        } else if (dataState == 1) {
            dataStateName = "DATA_CONNECTING";
        } else if (dataState == 2) {
            dataStateName = "DATA_CONNECTED";
        } else if (dataState == 3) {
            dataStateName = "DATA_SUSPENDED";
        }
        return dataStateName;
    }

    public void putHttp(String args) throws Exception {
        String [] inputData = args.split(";");
        String url = inputData[0];
        Long totalDownloadedBytesStop = Long.parseLong(inputData[1]);
        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        URL urlConv = new URL(url);
        HttpURLConnection urlConnection = null;
        try {

            urlConnection = (HttpURLConnection) urlConv.openConnection();
        } catch (Exception e) {
            System.out.println("ERROR CONNECTING TO URL");
        }
        finally {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }

    }

    private void ftpDownload(String args, TextView tv) {

        String url = readUrl();
        Long totalDownloadedBytesStop = Long.parseLong(args);
        totalDownloadedBytesStop = totalDownloadedBytesStop * 1024 * 1024;

        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        final TextView tv1 = tv;

        request.setDescription("ftp download @ " + url);
        request.setTitle("download");
        request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath(), "download-trash.dat");

        // get download service and enqueue file
        final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        int currentStatus = DownloadManager.STATUS_PAUSED;
        long downloadedBytes = 0;
        long totalDownloadedBytes = 0;
        long startingTime = System.currentTimeMillis();
        long downloadId = downloadManager.enqueue(request);
        long downloadSpeed;
        long timeElapsed;

        while (currentStatus != DownloadManager.STATUS_FAILED) {
            String dataActivityName = getDataActivity();
            String dataStateName = getDataState();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);

            Cursor cursor = downloadManager.query(q);
            cursor.moveToFirst();

            if (downloadedBytes != cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) &&
                    downloadedBytes < cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) && (totalDownloadedBytes < totalDownloadedBytesStop) ) {
                timeElapsed = System.currentTimeMillis() - startingTime + 1;
                totalDownloadedBytes = totalDownloadedBytes + cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) - downloadedBytes;
                downloadSpeed = totalDownloadedBytes / timeElapsed * 1000;

                downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                final String downloadData = "{\"downloadedSoFar\": \""
                        + downloadedBytes + "\", \"downloadSpeed\": \""
                        + downloadSpeed + "\", \"totalDownloadedSoFar\": \""
                        + totalDownloadedBytes + "\", \"timeElapsed\" : \""
                        + timeElapsed + "\"}";

                writeToFile(downloadData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv1.setText(downloadData);;
                    }
                });
                System.out.println("totalDownloadedBytes:"+totalDownloadedBytes + " " + totalDownloadedBytesStop);

            } else if (totalDownloadedBytes > totalDownloadedBytesStop) {
                final String downloadData = "{\"status\":\"DOWNLOAD FINISHED\"}";
                removeFile("download.*\\.dat");
                writeToFile(downloadData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv1.setText(downloadData);;
                    }
                });
                downloadManager.remove(downloadId);
                break;
            }

            currentStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            //tv.setText("Dupa primul if"+url);
            if (currentStatus == DownloadManager.STATUS_SUCCESSFUL) {
                removeFile("download.*\\.dat");
                downloadId = downloadManager.enqueue(request);
                downloadedBytes = 0;
            }
            else if (currentStatus == DownloadManager.STATUS_PAUSED) {
                final String downloadData = "{\"status\":\"PAUSED\"}";
                writeToFile(downloadData);
                tv.setText(downloadData);
            }
            cursor.close();
        }
        if (currentStatus == DownloadManager.STATUS_FAILED) {
            final String downloadData = "{\"status\":\"ERROR\"}";
            writeToFile(downloadData);
            tv.setText(downloadData);
        }
    }

    private void sendCall(String args) {
        String num = args;
        String number = "tel:" + num.toString().trim();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }
    }


    private void endCall() {
        //iTelephony
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getapp1",(Class[]) null);
            getITelephonyMethod.setAccessible(true);
        //    app1 iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
        //    iTelephony.endCall();
            Log.v(this.getClass().getName(), "endCall......");
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "endCallError", e);
        }
    }

    //Function implement sending a sms
    //arguments phone_nr and smsBody are sparated by ";"
    private void sendSms(String args) {
        String [] inputData = args.split(";");
        String phoneNumber = inputData[0];
        String smsBody = inputData[1];
        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
        // Send a text based SMS
        smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);
    }

    //Funtion implements reading the last sms
    private String readSms() {
        String msgData = "";
        //Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
                null, null, null, "date desc limit 1");
        try {
            cursor.moveToFirst();
            System.out.println("Cursor Object dump is: "+DatabaseUtils.dumpCursorToString(cursor));
            msgData += " " + cursor.getColumnName(2) + ":" + cursor.getString(2) + "\n" +
                    cursor.getColumnCount() + "\n" +
                    " " + cursor.getColumnName(8) + ":" + cursor.getString(12);
            cursor.close();
            return msgData;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgData = "null";
            return msgData;
        }
    }

    //Function implements enable or disable wireless connectivity
    private void wirelessMode(String argument) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if ((wifiEnabled) && (argument.equals("on"))) {
            System.out.println("Wifi already enabled");
        } else if ((wifiEnabled) && (argument.equals("off"))) {
            System.out.println("Wifi enabled. Switch to disabled mode.");
            wifiManager.setWifiEnabled(false);
        } else if ((!wifiEnabled) && (argument.equals("off"))) {
            System.out.println("Wifi already disabled");
        } else if ((!wifiEnabled) && (argument.equals("on"))) {
            System.out.println("Wifi disabled. Switch to enabled mode.");
            wifiManager.setWifiEnabled(true);
        }
    }

    //Function implements enable and disable data connectivity
    //This function will work up to android version 19
    public void mobileDataMode(boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class<?> conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove input.txt file
        //if (isFileExists("input.txt")) { removeFile("input.txt"); }
        //writeToInputFile("ftpUpload:url=speedtest.tele2.net;username=anonymous;password=varza@yahoo.com;fileSize=30;totalUpload=56;uploadPath=/upload");
        //writeToInputFile("ftpUpload:url=speedtest.tele2.net;username=anonymous;password=albala@yahoo.com;fileSize=15;maxTotalUpload=10000;ftpUploadPath=/upload");
        //Test with some input data
        //writeToInputFile("ftpDownload:www.google.com;10000");
        //writeToInputFile("ftpDownload:cdimage.ubuntu.com/kubuntu/releases/12.04.4/release/kubuntu-12.04.5-alternate-i386.iso;1000");

        //writeToInputFile("ftpUpload:test.talia.net;anonymous;varza@yahoo.com");
        //Read input.txt file for actionName and actionArgs


        String fileName = "output.txt";
        String[] inputData = readInput();
        final String actionName = inputData[0].trim();
        final String actionArgs = inputData[1].trim();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tv=(TextView)findViewById(R.id.myText);


        switch (actionName) {
            case "ftpDownload":
                System.out.println("ftpDownload action");
                ftpDownload(actionArgs, tv);
                break;

            case "ftpUpload":
                System.out.println("ftpUpload action");
                //ftpUpload(actionArgs);
                FtpClass myFtpClass = new FtpClass();
                myFtpClass.setArgs(actionArgs);
                myFtpClass.execute();
                break;

            case "dataDetails":
                System.out.println("Data details action");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String fileName = "dataDetalis.txt";
                                String dataState = getDataState();
                                dataState = dataState + "\n";
                                String dataActivity = getDataActivity();
                                writeToFile(dataState);
                                appendToFile(dataActivity + "\n");
                                if (tv != null) {
                                    tv.setText(dataState + "\t" + dataActivity);
                                }
                            }
                        });
                    }
                }, 0, 2000);
                break;

            case ("getCellIdentity"):
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String cellInfo = getCellIdentity();
                                String fileName = "getCellIdentity.txt";
                                writeToFile(cellInfo);
                                if (tv != null) {
                                    tv.setText(cellInfo);
                                }
                            }
                        });
                    }
                }, 0, 2000);
                break;

            case ("getAllCellIdentity"):
                fileName = "getAllCellIdentity";
                ArrayList<ArrayList<String>> allCellInfoList = getAllCellIdentity();
                String textDataAll = "";
                String status = "";
                String cellId = "";
                String writeData = "";
                writeToFile("BEGIN");
                for (ArrayList<String> cellInfoArray:allCellInfoList) {
                    status = cellInfoArray.get(0);
                    cellId = cellInfoArray.get(1);
                    writeData = status + ":" + cellId + "\\n";
                    appendToFile(writeData);
                }
                tv.setText(textDataAll);
                break;

            case ("ariplane"):
                //Needs os dev permission on the phone / TBD
                if (actionArgs.equals("on")) {
                    System.out.println("Switch on airplane mode");
                    tv.setText("Switch on airplane mode");
                } else if (actionArgs.equals("off")){
                    System.out.println("Switch off airplane mode");
                    tv.setText("Switch off airplane mode");
                }
                break;

            case ("openUrl"):
                //Needs os dev permission on the phone / TBD
                String url = actionArgs;
                goToUrl(url);
                break;

            case ("wireless"):
                if (actionArgs.equals("on")) {
                    System.out.println("Switch on wireless connection");
                    wirelessMode(actionArgs);
                    tv.setText("Switch on wireless connection");
                    writeData = "Switch on wireless connection";
                    writeToFile(writeData);
                } else if (actionArgs.equals("off")) {
                    System.out.println("Switch off wireless connection");
                    wirelessMode(actionArgs);
                    tv.setText("Switch off wireless connection");
                    writeData = "Switch off wireless connection";
                    writeToFile(writeData);
                }
                break;

            case ("data"):
                if (actionArgs.equals("on")) {
                    System.out.println("Data is on");
                    mobileDataMode(true);
                    tv.setText("Data is on");
                    writeData = "Data is on";
                    writeToFile(writeData);
                } else if (actionArgs.equals("off")) {
                    System.out.println("Data is off");
                    mobileDataMode(false);
                    tv.setText("Data is off");
                    writeData = "Data is off";
                    writeToFile(writeData);
                }
                break;

            case ("sendSms"):
                String destSmsPhoneNumber = actionArgs;
                System.out.println("Send sms to phone_nr "+destSmsPhoneNumber);
                sendSms(destSmsPhoneNumber);
                tv.setText("Send sms to phone_nr "+ destSmsPhoneNumber);
                writeData = "Send sms to phone_nr "+ destSmsPhoneNumber;
                writeToFile(writeData);
                break;

            case ("readSms"):
                System.out.println("Read the last sms ");
                String dataSms = readSms();
                tv.setText(dataSms);
                writeToFile(dataSms);
                break;

            case ("sendCall"):
                String destCallPhoneNumber = actionArgs;
                System.out.println("Send call to: " + destCallPhoneNumber);
                sendCall(destCallPhoneNumber);
                tv.setText("Send call to: " + destCallPhoneNumber);
                writeData = "Send call to: " + destCallPhoneNumber;
                writeToFile(writeData);
                break;

            case ("receiveCall"):
                //Not available from version > 4
                //Call interception is not allowed
                System.out.println("Answer phone call");
                break;

            case ("endCall"):
                endCall();
                System.out.println("End active call");
                writeData = "End active call";
                writeToFile(writeData);
                break;
        }
    }

}
