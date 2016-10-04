package net.casaclaude.actionapp;

import android.os.AsyncTask;
import android.os.Environment;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FtpClass extends AsyncTask<String, Integer, Long> {
    private String args = "";
    ProgressInputStream progressInput;
    long downloadedBytes = 0;
    long totalDownloadedBytes = 0;
    long totalTime = System.currentTimeMillis();
    long startTime = System.currentTimeMillis();
    long stopTime = System.currentTimeMillis();
    long timeElapsed = System.currentTimeMillis();
    long totalTimeElapsed = 0;
    long downloadSpeedMs;
    long downloadSpeedS;
    double timeElapsedSecond = 0.0;

    public void setArgs(String args) {
        this.args = args;
    }

    protected Long doInBackground(String... args) {
        long mylong = 533424342;
        try {
            String argsFinal = this.args;
            FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_L8);
            File sdcard = Environment.getExternalStorageDirectory();
            String fileNameSrc = "generated.txt";
            String fileNameDst = "generated.txt";
            File myFile = new File(sdcard,fileNameSrc);

            System.out.println("ARGUMENTE FINAL"+argsFinal);
            String [] inputData = argsFinal.split(";");
            String server = inputData[0];
            //if (!server.startsWith("ftp://") && !server.startsWith("ftps://")){
            //    server = "ftp://" + server;
            //}
            String username = inputData[1];
            String password = inputData[2];
            String directoryName = "/upload";

            FTPClient ftpClient = new FTPClient();
            ftpClient.configure(config);

            ftpClient.connect(server);
            ftpClient.login(username, password);
            System.out.println("status :: " + ftpClient.getStatus());

            ftpClient.changeWorkingDirectory(directoryName);

            //Your File path set here
            //ProgressInputStream progressInputStream = new ProgressInputStream(buffIn);
            //CountingInputStream countingInputStream = new CountingInputStream(buffIn);

            //Call onUpdateProgress
            //publishProgress(countingInputStream.getCount());
            //publishProgress(1232334);
            while (true) {
                Integer buffSize = 10000000;
                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(myFile),buffSize);
                ftpClient.enterLocalPassiveMode();
                //ProgressInputStream progressInputStream = new ProgressInputStream(buffIn);
                CountingInputStream countingInputStream = new CountingInputStream(buffIn);

                startTime = System.currentTimeMillis();
                boolean result = ftpClient.storeFile(fileNameDst, countingInputStream);
                mylong = mylong + countingInputStream.getCount();
                stopTime = System.currentTimeMillis();

                timeElapsed = stopTime - startTime;
                totalTimeElapsed += timeElapsed;

                downloadedBytes = countingInputStream.getCount();
                totalDownloadedBytes += countingInputStream.getCount();

                downloadSpeedMs = totalDownloadedBytes / totalTimeElapsed;
                downloadSpeedS = downloadSpeedMs * 1000;
                System.out.println("Time elapsed mili:" + timeElapsed);
                System.out.println("Time elapsed sec:" + timeElapsed / 1000);
                final String downloadData = "{\"uploadedSoFar\": \""
                        + downloadedBytes + "\", \"uploadedSpeed\": \""
                        + downloadSpeedS + "\", \"totalUploadedSoFar\": \""
                        + totalDownloadedBytes + "\", \"timeElapsed\" : \""
                        + timeElapsed + "\", \"totalTimeElapsed\" : \""
                        + totalTimeElapsed + "\"}";
                System.out.println(downloadData);
                buffIn.close();
            }
            //System.out.println("result is  :: " + result);
            //buffIn.close();
            //ftpClient.logout();
            //ftpClient.disconnect();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return mylong;
    }

    protected void onProgressUpdate(Integer... progress) {
        System.out.println("BAAAAAAAAAAA"+progress.toString());

    }

    protected void onPostExecute(Long result) {

    }




}
