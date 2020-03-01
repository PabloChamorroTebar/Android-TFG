package com.pablochamorro.practicasupm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity implements Serializable {

    private ListView search_alumnos;
    private ArrayAdapter<String> adapter;
    private int idpractica, idgrupo, idalumno;
    private Request request;
    private OkHttpClient client;
    private String name;
    private  ArrayList<String> alumnos_name;
    private Semaphore semaphore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        semaphore = new Semaphore(1);

        idpractica = getIntent().getIntExtra("idpractica", -1);

        idgrupo = getIntent().getIntExtra("idgrupo", -1);

        idalumno = getIntent().getIntExtra("idalumno", -1);

        name = getIntent().getStringExtra("alumnos_nombre");

        search_alumnos = findViewById(R.id.search_alumnos);

        alumnos_name = new ArrayList<>();

        create_data_alumnos_name(name, idpractica);

        transform_json_alumnos_name();

        try{
            semaphore.acquire();
        }
        catch (InterruptedException e){

        }

        adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_expandable_list_item_1, alumnos_name);

        semaphore.release();

        search_alumnos.setAdapter(adapter);

        search_alumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Invitar al compañero
                create_data_create_invitacion(alumnos_name.get(position));

                transform_create_invitacion();

                //Volvermos a la anterior activity
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void create_data_alumnos_name(String name, int idpractica){

        try {
            semaphore.acquire();
            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            String content = "{\"nombre\": \""+ name + "\"}";

            RequestBody body = RequestBody.create(MEDIA_TYPE, content);

            request = new Request.Builder()
                    .url(getString(R.string.url) + "practicas/" + idpractica + "/alumnos")
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
        catch (InterruptedException e){

        }
    }

    private void transform_json_alumnos_name () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();
                semaphore.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                try {
                    JSONArray json_response = new JSONArray(string_response);

                        //Insert alumno into array
                        for (int i = 0; i < json_response.length(); i++){
                            alumnos_name.add(json_response.getJSONObject(i).getString("nombre"));
                        }
                        semaphore.release();
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                    e.printStackTrace();
                    semaphore.release();
                }
            }
        });
    }

    private void create_data_create_invitacion(String invitado){
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        String content = "{\"alumno_invitado\": \""+ invitado + "\", \"idsuario\": "+ idalumno +", " +
                "\"idgrupo\": "+ idgrupo +", \"idpractica\": "+ idpractica +"}";

        RequestBody body = RequestBody.create(MEDIA_TYPE, content);

        request = new Request.Builder()
                .url(getString(R.string.url) + "invitaciones/crear_invitacion")
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();
    }

    private void transform_create_invitacion() {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
