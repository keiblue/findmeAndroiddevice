package com.example.findmedevice;

import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connections {

    private String urlBase = "https://find-me-back-end.herokuapp.com/api/";

    public User serviceGetUserData(String url) {
        User out = new User();
        String route = urlBase.concat(url);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection conn;
        try {
            URL urlt = new URL(route);
            conn = (HttpURLConnection) urlt.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String json = response.toString();
            JSONObject object = new JSONObject(json);
            for (int i = 0; i < object.length(); i++) {
                JSONObject jsonObject = object.getJSONObject("Person");
                out.setId((long) jsonObject.optInt("id"));
                out.setName(jsonObject.optString("firstName"));
                out.setLastName(jsonObject.optString("lastName"));
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return out;
    }


    public void createUserLocation(final String url, final DataExport data) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    User out = new User();
                    String route = urlBase.concat(url);
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    HttpURLConnection conn;
                    URL urlt = new URL(route);
                    conn = (HttpURLConnection) urlt.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.connect();

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("latitude", data.getLatitude().toString());
                    jsonParam.put("longitude", data.getLongitude().toString());

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    conn.disconnect();


                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    thread.start();
    }
}