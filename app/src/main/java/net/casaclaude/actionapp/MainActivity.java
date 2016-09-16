package net.casaclaude.actionapp;

import android.Manifest;
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
    private void writeToFile(String data) {
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

    private boolean isFileExists(String filename){
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tv=(TextView)findViewById(R.id.myText);
        //Remove input.txt file
        //if (isFileExists("input.txt")) { removeFile("input.txt"); }
        //Test with some input data
        //writeToInputFile("readSms:0765704753");
        //Read input.txt file for actionName and actionArgs
        String[] inputData = readInput();
        String actionName = inputData[0].trim();
        String actionArgs = inputData[1].trim();

        switch (actionName) {
            case "ftpDownload":
                System.out.println("ftpDownload action");
                break;

            case ("getCellIdentity"):
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String cellInfo = getCellIdentity();
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
                ArrayList<ArrayList<String>> allCellInfoList = getAllCellIdentity();
                String textDataAll = "";
                String status = "";
                String cellId = "";
                String writeData = "";
                for (ArrayList<String> cellInfoArray:allCellInfoList) {
                    status = cellInfoArray.get(0);
                    cellId = cellInfoArray.get(1);
                    writeData = status + ":" + cellId + "\\n";
                    textDataAll = textDataAll + writeData;
                    writeToFile(writeData);
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

            case ("wireless"):
                if (actionArgs.equals("on")) {
                    System.out.println("Switch on wireless connection");
                    wirelessMode(actionArgs);
                    tv.setText("Switch on wireless connection");
                } else if (actionArgs.equals("off")) {
                    System.out.println("Switch off wireless connection");
                    wirelessMode(actionArgs);
                    tv.setText("Switch off wireless connection");
                }
                break;

            case ("data"):
                if (actionArgs.equals("on")) {
                    System.out.println("Data is on");
                    mobileDataMode(true);
                    tv.setText("Data is on");
                } else if (actionArgs.equals("off")) {
                    System.out.println("Data is off");
                    mobileDataMode(false);
                    tv.setText("Data is off");
                }
                break;

            case ("sendSms"):
                String destSmsPhoneNumber = actionArgs;
                System.out.println("Send sms to phone_nr "+destSmsPhoneNumber);
                sendSms(destSmsPhoneNumber);
                tv.setText("Send sms to phone_nr "+ destSmsPhoneNumber);
                break;

            case ("readSms"):
                System.out.println("Read the last sms ");
                String dataSms = readSms();
                tv.setText(dataSms);
                break;

            case ("sendCall"):
                String destCallPhoneNumber = actionArgs;
                System.out.println("Send call to: " + destCallPhoneNumber);
                sendCall(destCallPhoneNumber);
                tv.setText("Send call to: " + destCallPhoneNumber);
                break;

            case ("receiveCall"):
                //Not available from version > 4
                //Call interception is not allowed
                System.out.println("Answer phone call");
                break;

            case ("endCall"):
                endCall();
                System.out.println("End active call");
                break;
        }
    }

}
