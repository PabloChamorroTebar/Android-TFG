package com.pablochamorro.practicasupm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.pablochamorro.practicasupm.classes.Alumno;
import com.pablochamorro.practicasupm.classes.Asignatura;
import com.pablochamorro.practicasupm.classes.Notices;
import com.pablochamorro.practicasupm.classes.Practica;
import com.pablochamorro.practicasupm.ui.asignatura.AsignaturaFragment;
import com.pablochamorro.practicasupm.ui.calendar.CalendarFragment;
import com.pablochamorro.practicasupm.ui.home.HomeFragment;
import com.pablochamorro.practicasupm.ui.inbox.InboxFragment;
import com.pablochamorro.practicasupm.ui.my_account.MyAccountFragment;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    private AppBarConfiguration mAppBarConfiguration;
    private Semaphore semaphore;
    private FloatingActionButton float_button;
    private Alumno alumno;
    private NavigationView navigationView;
    private Request request;
    private OkHttpClient client;
    private ArrayList<Asignatura> asignaturas;
    private ArrayList<Practica> practicas;
    private Map<Asignatura, ArrayList<Practica>> practicas_by_asignatura;
    private Notices notices;
    private String asignatura_pointed;
    private Map<Integer, Map<String, ArrayList<String>>> map_grupos_practicas_alumnos = new HashMap<>();
    private ArrayList<Grupo> grupos;
    private MenuItem update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        semaphore = new Semaphore(1);

        getSupportActionBar().setTitle("Home");

        //Create button to go inbox fragment
        FloatingActionButton button= findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Fragment fragment = new InboxFragment();

                // set the toolbar title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Inbox");
                }

                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });


        //Recieve alumno from login
        Intent intent_recieve = getIntent();
        alumno = (Alumno) intent_recieve.getSerializableExtra("Alumno");

        //Change the default nav_header
        change_nav_header();

        //Create Array practicas from json_response
        create_data_practicas_alumnos();
        transform_json_to_practicas();

        //Create Array asignaturas from json_response
        create_data_asignaturas_alumnos();
        transform_json_to_asignaturas();

        notices = new Notices(practicas);

        try {
            semaphore.acquire();
        }
        catch (InterruptedException e){

        }

        create_map_asignaturas_practicas();

        semaphore.release();


        if (practicas.size()>0){

            //Call to receive groups
            create_data_groups();
            transform_json_groups();

            //this request give practica, groups and alumnos in groups for each practica
            create_data_group_alumnos_practica();
            transform_json_to_group_alumnos_practica();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public Alumno getAlumno() {
        return alumno;
    }

    private void change_nav_header(){

        View headerView = navigationView.getHeaderView(0);

        //Change email field
        TextView navEmail = headerView.findViewById(R.id.text_email);
        navEmail.setText(alumno.getEmail());

        //Change name field
        TextView navUsername = headerView.findViewById(R.id.text_name);
        navUsername.setText(alumno.getName());
    }

    private void create_data_asignaturas_alumnos () {
        try {
            //Intenta coger el semaforo
            semaphore.acquire();
            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            request = new Request.Builder()
                    .url(getString(R.string.url) + "asignaturas_alumno/" + Integer.toString(alumno.getIdusuario()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        } catch (InterruptedException e) {

        }
    }

    private void transform_json_to_asignaturas () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();

                //Suelta el semaforo
                semaphore.release();
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                asignaturas = new ArrayList<Asignatura>();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    if(json_response.length() == 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "Usuario sin asginaturas", Toast.LENGTH_LONG).show(); } });
                        semaphore.release();
                    }
                    else{
                        //Insert alumno asignaturas to array
                        for (int i = 0; i < json_response.length(); i++){
                            Asignatura asignatura = new Asignatura(json_response.getJSONObject(i));
                            asignaturas.add(asignatura);
                        }

                        //Suelta el semaforo
                        semaphore.release();
                    }
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo

                    //Suelta el semaforo
                    semaphore.release();
                }
            }
        });
    }

    public ArrayList<Asignatura> getAsignaturas () {
        return asignaturas;
    }

    private void create_data_practicas_alumnos () {
        try {
            semaphore.acquire();
            request = new Request.Builder()
                    .url(getString(R.string.url) + "alumnos/" + Integer.toString(alumno.getIdusuario()) + "/practicas_alumno")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
        catch (InterruptedException e){

        }
    }

    private void transform_json_to_practicas () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();

                //Suelta el semaforo
                semaphore.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                practicas = new ArrayList<>();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    if(json_response.length() == 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "Usuario sin practicas", Toast.LENGTH_LONG).show(); } });
                        semaphore.release();
                    }
                    else{
                        //Insert alumno practicas to array
                        for (int i = 0; i < json_response.length(); i++){
                            Practica practica = new Practica(json_response.getJSONObject(i));
                            practicas.add(practica);
                        }

                        //Suelta el semaforo
                        semaphore.release();
                    }
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                    e.printStackTrace();

                    //Suelta el semaforo
                    semaphore.release();
                }
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_my_subjects) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_submenu_drawer);
            Menu new_menu = navigationView.getMenu();
            insert_asignatura_menu(new_menu);
        } else if (id == R.id.back_to_main) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        } else if (id == R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        }else if (id == R.id.nav_calendar) {
            fragment = new CalendarFragment();
            title = "Calendar";
        }else if (id == R.id.nav_my_account){
            fragment = new MyAccountFragment();
            title = "My Account";
        }else if (id == R.id.nav_inbox){
            fragment = new InboxFragment();
            title = "Inbox";
        } else if (id == R.id.nav_close_session){
            delete_preferences();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }else {
            for (int i = 0; i < asignaturas.size(); i++){
                if (i == id-1) {
                    fragment = new AsignaturaFragment();
                    title = asignaturas.get(i).getName();
                    asignatura_pointed = title;
                    break;
                }
            }
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.update){
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("Alumno", alumno);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void insert_asignatura_menu (Menu menu){
        for (int i = 0 ; i< asignaturas.size(); i++) {
            menu.add(R.id.group_divider,i+1, Menu.NONE,asignaturas.get(i).getName());
            MenuItem settingsItem = menu.findItem(1);
            settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_subject));
        }
    }

    public Notices getNotices() {
        return notices;
    }

    public String get_asignatura_pointed () {
        return asignatura_pointed;
    }

    public void create_map_asignaturas_practicas () {

        practicas_by_asignatura = new HashMap<>();
        ArrayList<Practica> lista;

        for ( Asignatura asignatura : asignaturas ){
            lista = new ArrayList<Practica>();
            for (Practica practica : practicas){
                if (asignatura.getIdasignatura() == practica.getAsignatura_idasignatura()){
                    lista.add(practica);
                }
            }
            practicas_by_asignatura.put(asignatura, lista);
        }
    }

    private void create_data_group_alumnos_practica () {
        try {
            semaphore.acquire();

            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            ArrayList<Integer> idpracticas = new ArrayList<>();
            for (Practica practica : practicas){
                idpracticas.add(practica.getIdpractica());
            }


            String content = "{\"practicas\": "+ idpracticas.toString() +"}";

            RequestBody body = RequestBody.create(MEDIA_TYPE, content);

            request = new Request.Builder()
                    .url(getString(R.string.url) + "practica_grupo_alumno")
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
        catch (InterruptedException e){

        }

    }

    private void transform_json_to_group_alumnos_practica () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();
                //Suelta el semaforo
                semaphore.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    if(json_response.length() == 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "No tiene practicas", Toast.LENGTH_LONG).show(); } });
                        semaphore.release();
                    }
                    else{
                        json_to_grupos_practicas_alumnos(json_response);
                    }

                    //Suelta el semaforo
                    semaphore.release();
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo

                    //Suelta el semaforo
                    semaphore.release();
                }
            }
        });
    }

    private void create_data_groups () {

        try {
            semaphore.acquire();
            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            ArrayList<Integer> idpracticas = new ArrayList<>();
            for (Practica practica : practicas){
                idpracticas.add(practica.getIdpractica());
            }


            String content = "{\"practicas\": "+ idpracticas.toString() +"}";

            RequestBody body = RequestBody.create(MEDIA_TYPE, content);

            request = new Request.Builder()
                    .url(getString(R.string.url) + "grupos_por_practicas")
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();

        }
        catch (InterruptedException e){

        }
    }

    private void transform_json_groups () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();

                //Suelta el semaforo
                semaphore.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                grupos = new ArrayList<>();
                String string_response = response.body().string();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    if(json_response.length() == 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "Ususario sin practicas", Toast.LENGTH_LONG).show(); } });
                    }
                    else{
                        //Insert alumno practicas to array
                        for (int i = 0; i < json_response.length(); i++){
                            Grupo grupo = new Grupo(json_response.getJSONObject(i));
                            grupos.add(grupo);
                            if (map_grupos_practicas_alumnos.get(grupo.idgrupo) == null){
                                map_grupos_practicas_alumnos.put(grupo.idgrupo, new HashMap<String, ArrayList<String>>());
                            }
                        }
                    }

                    //Suelta el semaforo
                    semaphore.release();

                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                    e.printStackTrace();
                    //Suelta el semaforo
                    semaphore.release();
                }
            }
        });
    }

    public Map<Asignatura, ArrayList<Practica>> getPracticas_by_asignatura() {
        return practicas_by_asignatura;
    }

    public Asignatura getAsignatura_by_name(String name){
        for (Asignatura asignatura : getAsignaturas()) {
            if (asignatura.getName().equals(name)){
                return asignatura;
            }
        }
        return null;
    }

    public Practica getPractica_by_name(ArrayList<Practica> practicas, String name){
        for (Practica practica : practicas){
            if(practica.getName().equals(name))
                return practica;
        }
        return null;
    }

    private void json_to_grupos_practicas_alumnos (JSONArray jsonArray) {
        /*Por cada objeto dentro del array hay nombre del grupo, alumno, y id de la practica
        por lo que hay que añadir cada entra al mapa y ver si esta el nombre del grupo, si no esta
        lo metemos y creamos el array con el nombre del alumno, si esta, le añadimos el alumno al
        array y pa lante
         */
        map_grupos_practicas_alumnos = new HashMap<>();
        JSONObject jsonObject;
        Integer idpractica;
        String nombre_grupo, nombre_alumno;
        ArrayList<String> alumnos;
        Map<String, ArrayList<String>> map_group_alumnos;
        for (int i = 0; i< jsonArray.length(); i++){
            try {
                jsonObject = jsonArray.getJSONObject(i);
                idpractica = jsonObject.getInt("practica_idpractica");
                nombre_grupo = jsonObject.getString("nombre_grupo");
                nombre_alumno = jsonObject.getString("nombre");
                //Comprobamos que la practica existe

                map_group_alumnos = map_grupos_practicas_alumnos.get(idpractica);

                if(map_group_alumnos == null){
                    map_group_alumnos = new HashMap<>();
                    alumnos = new ArrayList<>();
                    alumnos.add(nombre_alumno);
                }
                else {
                    alumnos = map_grupos_practicas_alumnos.get(idpractica).get(nombre_grupo);
                    if (alumnos == null){
                        alumnos = new ArrayList<>();
                    }
                    alumnos.add(nombre_alumno);
                }
                map_group_alumnos.put(nombre_grupo,alumnos);
                map_grupos_practicas_alumnos.put(idpractica, map_group_alumnos);

            }catch (JSONException e){
                //Mal lo que devuelve
            }
        }
    }

    public Map<Integer, Map<String, ArrayList<String>>> getMap_grupos_practicas_alumnos() {
        return map_grupos_practicas_alumnos;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    private void delete_preferences () {
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("alumno", "false");
        editor.putString("pass", "false");

        editor.commit();

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
