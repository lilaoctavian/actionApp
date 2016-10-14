package net.casaclaude.actionapp;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;


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

    String uuid = UUID.randomUUID().toString().substring(0,10);
    String fileNameSrc = String.format("%s.iof",uuid);
    String fileNameDst = fileNameSrc;
    long mylong = 0L;

    //Get the sdcard path
    File sdcard = Environment.getExternalStorageDirectory();
    //Input file
    File myFile = new File(sdcard,fileNameSrc);

    public void setArgs(String args) {
        this.args = args;
    }

    protected Long doInBackground(String... args) {

        String argsFinal = this.args;

        String [] inputData = argsFinal.split(";");
        String server = inputData[0].split("=")[1];
        String username = inputData[1].split("=")[1];
        String password = inputData[2].split("=")[1];
        Integer fileSize = Integer.parseInt(inputData[3].split("=")[1]);
        Integer maxUploadSize = Integer.parseInt(inputData[4].split("=")[1]);
        String directoryName = inputData[5].split("=")[1];


        //if (!server.startsWith("ftp://") && !server.startsWith("ftps://")){
        //    server = "ftp://" + server;
        //}

        Integer uploadSizeBytes = maxUploadSize * 1024 * 1024;
        Integer count = 1024 * fileSize;
        String osCommandGenerate = String.format("dd if=/dev/zero of=/sdcard/%s count=%s bs=1024",fileNameSrc,count);
        System.out.println("FILE GENERARTED"+osCommandGenerate);
        String osCommandRemove = String.format("rm /sdcard/%s","*.iof");
        System.out.println("FILE GENERARTED"+osCommandRemove);

        //Check if file exists
        try {
            if (!MainActivity.isFileExists(fileNameSrc)) {
                Process process1 = Runtime.getRuntime().exec(osCommandRemove);
                Process process2 = Runtime.getRuntime().exec(osCommandGenerate);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            }
        }catch (IOException e) {
                Log.e("Exception", "Generated file fail: " + e.toString());
            }
        //Upload file to ftp
        try {
            //Config the ftp connection
            FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_L8);

            //Open ftpClient connection
            FTPClient ftpClient = new FTPClient();
            ftpClient.configure(config);
            ftpClient.connect(server);
            ftpClient.login(username, password);
            //Change working directory
            ftpClient.changeWorkingDirectory(directoryName);

            while (totalDownloadedBytes < uploadSizeBytes) {

                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(myFile));
                ftpClient.enterLocalPassiveMode();
                //ProgressInputStream progressInputStream = new ProgressInputStream(buffIn);
                CountingInputStream countingInputStream = new CountingInputStream(buffIn);

                startTime = System.currentTimeMillis();
                try {
                    ftpClient.storeFile(this.fileNameDst, countingInputStream);
                } catch (IOException | RuntimeException e) {
                    String uuid = UUID.randomUUID().toString().substring(0,10);
                    this.fileNameDst = String.format("%s.iof",uuid);
                    String downloadData = "{\"status\":\"PAUSED\"}";
                    MainActivity.writeToFile(downloadData);
                    System.out.println(downloadData);
                    //Config the ftp connection
                    config = new FTPClientConfig(FTPClientConfig.SYST_L8);
                    ftpClient = new FTPClient();
                    ftpClient.configure(config);
                    try {
                        //Open ftpClient connection
                        ftpClient.connect(server);
                        ftpClient.login(username, password);
                        //Change working directory
                        ftpClient.changeWorkingDirectory(directoryName);
                    } catch (NullPointerException np) {
                        np.printStackTrace();
                    } catch (IOException io) {
                        System.out.println("No connection available");
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (Exception exc) {
                        System.out.println(exc);
                    }
                    continue;
                }
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
                MainActivity.writeToFile(downloadData);
                buffIn.close();
            }
            //System.out.println("result is  :: " + result);
            ftpClient.logout();
            ftpClient.disconnect();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return mylong;
    }

    private FTPClient ftpConnect(String server, String username, String password, String directory) {
        try {
            //Config the ftp connection
            FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_L8);
            //Open ftpClient connection
            FTPClient ftpClient = new FTPClient();
            ftpClient.configure(config);
            ftpClient.connect(server);
            ftpClient.login(username, password);
            ftpClient.changeWorkingDirectory(directory);
            return ftpClient;
        } catch (IOException e) {
            System.out.println("Can not connect to FTP Server");
            FTPClient ftpClient = new FTPClient();
            return ftpClient;
        }
    }

    private boolean testConnection() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
            urlConnect.setConnectTimeout(1000);
            urlConnect.getContent();
            System.out.println("Connection established.");
            return true;
        } catch (NullPointerException np) {
            np.printStackTrace();
            return true;
        } catch (IOException io) {
            io.printStackTrace();
            return true;
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        System.out.println(progress.toString());

    }

    protected void onPostExecute(Long result) {

    }




}
