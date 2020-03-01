package com.pablochamorro.practicasupm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.*;

import com.pablochamorro.practicasupm.classes.Alumno;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.MessageDigest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText field_email_o_matricula;
    private EditText field_password;
    private Button button_login;

    private OkHttpClient client;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Init http client for api calls
        client = new OkHttpClient();

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        if (preferences != null && !preferences.getString("alumno", "false").equals("false") && !preferences.getString("pass", "false").equals("false")){
            create_data_and_go_main(preferences.getString("alumno", "false"),preferences.getString("pass", "false"), true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        button_login = findViewById(R.id.login);
        button_login.setOnClickListener(this);









    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                field_email_o_matricula = findViewById(R.id.email_o_matricula);
                field_password = findViewById(R.id.password);
                create_data_and_go_main(field_email_o_matricula.getText().toString(), field_password.getText().toString(), false);
                break;
        }

    }

    private void creata_data_login_api (String email_o_matricula){

        MediaType MEDIA_TYPE = MediaType.parse("application/json");

            String content = "{\"email_matricula\": \""+ email_o_matricula +"\"}";

        RequestBody body = RequestBody.create(MEDIA_TYPE, content);

        request = new Request.Builder()
                .url(getString(R.string.url) + "login_alumnos")
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();
    }

    private void show_Toast(String string_to_show){
        Toast.makeText(this, string_to_show, Toast.LENGTH_SHORT).show();
    }

    private void create_preferences (String name, String pass) {
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("alumno", name);
        editor.putString("pass", pass);

        editor.commit();

    }

    private void create_data_and_go_main (final String name, String pass, final boolean keep){
        String email_o_matricula = name;
        final String password = pass;

        creata_data_login_api(email_o_matricula);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    if(json_response.length() == 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "Contraseña o usuario incorrecta", Toast.LENGTH_LONG).show(); } });
                    }
                    else{
                        //Creamos un alumnos y comprobamos las contraseñas
                        Alumno alumno = new Alumno(json_response.getJSONObject(0));
                        String password_sha = "";
                        if (!keep){
                            try {
                                password_sha = hashSha256(password);
                            }
                            catch (NoSuchAlgorithmException e){
                                //No se ha encontrado el algoritmo sha
                            }
                        }
                        else {
                            password_sha = password;
                        }
                        //Pasamos la contraseña leida al hash de sha256

                        //Comprobamos si la password es la misma
                        if(password_sha.equals(alumno.getPassword())){
                            create_preferences(name, password_sha);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("Alumno", alumno);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getBaseContext(), "Contraseña o usuario incorrecto", Toast.LENGTH_LONG).show(); } });
                        }
                    }

                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                }
            }
        });
    }

    private String hashSha256 (String password) throws NoSuchAlgorithmException {

        // Create MessageDigest instance for MD5
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        //Add password bytes to digest
        md.update(password.getBytes());
        //Get the hash's bytes
        byte[] bytes = md.digest();
        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        //Get complete hashed password in hex format
        return sb.toString();
    }
}
