package com.example.myapplication.lesson1;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.lesson1.ListAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private ListAdapter adapter;
    private SensorManager sensorManager;
    private Sensor temperature;
    private Float tempValue;
    private TextView textTemp;
    private TextView textHumidity;
    private TextView bindTemp;
    private Button button;
    private boolean isBound = false;
    private BoundService.ServiceBinder boundService;
    Intent intent;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = initToolbar();
        initDrawer(toolbar);
        initList();
        initByID();
        initSensor();
        HumiditySensor humiditySensor = new HumiditySensor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(this, BoundService.class);
        bindService(intent, boundServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        button.setOnClickListener((v) -> {
            Log.d("TAG", "onResume: ");
            if (boundService == null) {
                bindTemp.setText("Unbound service");
            } else {

                float fibo = boundService.getWeatherRequest();
                Log.d("TAG", "onResume: ");
                bindTemp.setText(Float.toString(fibo));
                stopService(intent);
            }

        });
    }


    ServiceConnection boundServiceConnection = new ServiceConnection() {

        // При соединении с сервисом
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundService = (BoundService.ServiceBinder) service;
            isBound = boundService != null;
        }

        // При разъдинении с сервисом
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            boundService = null;
        }
    };


    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initByID() {
        textTemp = findViewById(R.id.temp);
        textHumidity = findViewById(R.id.humi);
        bindTemp = findViewById(R.id.bindtemp);
        button = findViewById(R.id.button);
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    private void initList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_list);

        // Эта установка служит для повышения производительности системы
        recyclerView.setHasFixedSize(true);

        // Будем работать со встроенным менеджером
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Установим адаптер
        adapter = new ListAdapter(initData(), this);
        recyclerView.setAdapter(adapter);
    }

    private List<String> initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(String.format("Element %d", i));
        }
        return list;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработка выбора пункта меню приложения (активити)
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_add) {
            adapter.addItem("New element");
            return true;
        }

        if (id == R.id.action_clear) {
            adapter.clearItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Здесь определяем меню приложения (активити)
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.action_search); // поиск пункта меню поиска
        final SearchView searchText = (SearchView) search.getActionView(); // строка поиска
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // реагирует на конец ввода поиска
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(searchText, query, Snackbar.LENGTH_LONG).show();
                return true;
            }

            // реагирует на нажатие каждой клавиши
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        tempValue = sensorEvent.values[0];
        textTemp.setText(String.valueOf(tempValue));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    class HumiditySensor implements SensorEventListener {

        private SensorManager sensorManager;
        private Sensor humidity;
        private Float humValue;

        HumiditySensor() {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            humValue = sensorEvent.values[0];
            textHumidity.setText(String.valueOf(humValue));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

    }


}