package com.example.myapplication.lesson1;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class BoundService extends Service {


    private final IBinder binder = new ServiceBinder();
    private final String KEY = "61e5cbccdc6985b69b92b24028a5dfc5";
    private final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=Moscow,RU&appid=";
    final Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public float getWeatherRequest() {
        final Float[] f = {0.0f};
        try {
            final URL uri = new URL(WEATHER_URL + KEY);
            Thread thread = new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    HttpsURLConnection urlConnection = null;
                    try {
                        urlConnection = (HttpsURLConnection) uri.openConnection();
                        urlConnection.setRequestMethod("GET"); // установка метода получения данных -GET
                        urlConnection.setReadTimeout(10000); // установка таймаута - 10 000 миллисекунд
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); // читаем  данные в поток
                        String result = getLines(in);
                        // преобразование данных запроса в модель
                        Gson gson = new Gson();
                        final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);
                        // Возвращаемся к основному потоку
                        f[0] = weatherRequest.getMain().getTemp();
                        Log.d("TAG", "run: " + f[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != urlConnection) {
                            urlConnection.disconnect();
                        }
                    }
                }
            });
            thread.start();
            thread.join();
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
        }
        return f[0];
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getLines(BufferedReader in) {
        return in.lines().collect(Collectors.joining("\n"));
    }

    class ServiceBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        float getWeatherRequest() {
            return getService().getWeatherRequest();
        }
    }
}
