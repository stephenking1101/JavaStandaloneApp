package example.servicediscovery.backend.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ConsulClient {

    public ConsulClient(){

    }

    static String getServiceList(final URL url) {

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {

            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setConnectTimeout(500);
            httpURLConnection.setReadTimeout(500);

            if (httpURLConnection.getResponseCode() >= 300) {
                System.out.println("HTTP Request is not success, Response code is: " + httpURLConnection.getResponseCode());
            }

            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            String tempLine;
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            httpURLConnection.disconnect();
        } catch (Exception e) {


        } finally {
            if (httpURLConnection !=null)
                httpURLConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {

                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                }
            }



        }
        return resultBuffer.toString();

    }



}
