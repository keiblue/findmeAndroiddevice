package com.example.findmedevice.connection;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.findmedevice.models.DataExport;
import com.example.findmedevice.models.Person;
import com.example.findmedevice.models.Smartphone;
import com.example.findmedevice.utils.ConstantSQLite;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionsBackend {

    private String urlBase = "https://find-me-back-end.herokuapp.com/api/";

    public Person serviceGetUserData(String url) {
        Person out = new Person();
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
                out.setId(jsonObject.optString("id"));
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

                    Person out = new Person();
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

    public void createSmartphone(final String url, final DataExport data, final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String mensaje ="";
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
                    jsonParam.put("androidId", data.getAndroidId());
                    jsonParam.put("BeaconId", data.getBeaconUID());


                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Smartphone smartphone = new Smartphone();
                    String json = response.toString();
                    JSONObject object = new JSONObject(json);

                    for (int i = 0; i < object.length(); i++) {
                        try {
                            JSONObject jsonObject = object.getJSONObject("data");
                            smartphone.setId(jsonObject.optString("id"));
                            if(ConstantSQLite.ConsultarSmartphone(context).getId() == null){
                                ConstantSQLite.RegisterSmartphoneSQL(smartphone, context);
                            }
                            mensaje= object.optString("menssage");
                        }catch (Exception e){
                            mensaje= object.optString("menssage");

                        }
                    }

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

    public void updateSmartphone(final String url, final DataExport data, final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String mensaje ="";
                    String route = urlBase.concat(url);
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    HttpURLConnection conn;
                    URL urlt = new URL(route);
                    conn = (HttpURLConnection) urlt.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.connect();

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("androidId", data.getAndroidId());
                    jsonParam.put("BeaconId", data.getBeaconUID());


                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Smartphone smartphone = new Smartphone();
                    String json = response.toString();
                    JSONObject object = new JSONObject(json);

                    /*for (int i = 0; i < object.length(); i++) {
                        try {
                            JSONObject jsonObject = object.getJSONObject("data");
                            smartphone.setId(jsonObject.optString("id"));
                            ConstantSQLite.RegisterSmartphoneSQL(smartphone, context);
                            mensaje= object.optString("menssage");
                        }catch (Exception e){
                            mensaje= object.optString("menssage");

                        }
                    }*/

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