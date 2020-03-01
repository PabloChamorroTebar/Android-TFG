package com.pablochamorro.practicasupm.ui.my_account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.pablochamorro.practicasupm.classes.Alumno;
import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAccountFragment extends Fragment {

    private MyAccountViewModel myAccountViewModel;
    private TextView field_user, field_registration, field_email;
    private Alumno alumno ;
    private Request request;
    private OkHttpClient client;
    private ImageView imageView;
    private final int REQUEST_CAMERA = 1, SELECT_FILE = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myAccountViewModel =
                ViewModelProviders.of(this).get(MyAccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_my_account, container, false);

        alumno = ((MainActivity)getActivity()).getAlumno();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        field_user = getActivity().findViewById(R.id.field_user);
        field_user.setText(alumno.getName());

        field_email = getActivity().findViewById(R.id.field_email);
        field_email.setText(alumno.getEmail());

        field_registration = getActivity().findViewById(R.id.field_registration);
        field_registration.setText(alumno.getMatricula());

        final Button button = getActivity().findViewById(R.id.change_password_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText password_field = getActivity().findViewById(R.id.field_current_password);
                EditText new_password_field = getActivity().findViewById(R.id.field_new_password);
                String current_password = hashSha256(password_field.getText().toString());
                String new_password = hashSha256(new_password_field.getText().toString());
                if(alumno.getPassword().equals(current_password)){
                    //Mandar password para acutalizarla
                    create_data_practicas_alumnos(new_password);
                    transform_json_to_practicas();
                    alumno.setPassword(new_password);
                    password_field.setText("");
                    new_password_field.setText("");
                }
                else {
                    Toast.makeText(getActivity(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
                }
            }
        });

        imageView = getView().findViewById(R.id.image_profile);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_camera_dialog();
            }
        });

    }

    private void create_data_practicas_alumnos (String password) {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String json_field = "{ \"password\" : \"" + password + "\" }";
        RequestBody body = RequestBody.create(MEDIA_TYPE, json_field);
        request = new Request.Builder()
                .url(getString(R.string.url) + "alumnos/" + Integer.toString(alumno.getIdusuario()) + "/change_password")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .put(body)
                .build();
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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                try {
                    JSONObject json_response = new JSONObject(string_response);
                    String status = json_response.getString("Status");
                    if(status.equals("Password changed")){
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getActivity().getBaseContext(), getString(R.string.password_changed), Toast.LENGTH_LONG).show(); } });
                    }
                    else{
                        new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getActivity().getBaseContext(), getString(R.string.something_was_wrong), Toast.LENGTH_LONG).show(); } });
                    }
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                    e.printStackTrace();
                }
            }
        });
    }

    private void  create_camera_dialog () {

        final CharSequence [] options = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Image");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Camera")){

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    getActivity().startActivityForResult(intent, REQUEST_CAMERA);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if ( requestCode == REQUEST_CAMERA) {

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");

                imageView.setImageBitmap(bmp);
            }
        }
    }


    private String hashSha256 (String password){

        // Create MessageDigest instance for SHA-256
        try {
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
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        return null;
    }
}