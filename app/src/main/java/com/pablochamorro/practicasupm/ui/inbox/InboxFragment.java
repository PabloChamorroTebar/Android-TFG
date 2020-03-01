package com.pablochamorro.practicasupm.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.pablochamorro.practicasupm.classes.Invitacion;
import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class InboxFragment extends Fragment implements Serializable {
    private Request request;
    private OkHttpClient client;
    private ArrayList<Invitacion> invitacions;
    private LinearLayout linear_inbox;
    private RelativeLayout mail_block;
    private Button accept;
    private Button deny;
    private TextView message_name, message_inbox;
    private String message;
    private Semaphore semaphore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);

        semaphore = new Semaphore(1);

        //Obtenemos todas las invitaciones del ususario
        create_data_invitations_alumnos(((MainActivity)getActivity()).getAlumno().getName());
        transform_json_to_invitations();


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            semaphore.acquire();
        }catch (InterruptedException e){

        }
        //Insert block into fragment
        linear_inbox = getActivity().findViewById(R.id.linear_inbox);

        //Create each mail
        for (final Invitacion invitacion : invitacions){
            mail_block = (RelativeLayout) View.inflate(getActivity(),
                    R.layout.mail_block, null);

            //Find message_name
            message_name = mail_block.findViewById(R.id.message_name);
            message_name.setText(invitacion.getAlumno_invitante());

            //Find message_inbox
            message = "Me gustaria que te unieses a mi grupo " + invitacion.getNombre_grupo() + " en la practica "+ invitacion.getNombre_practica();
            message_inbox = mail_block.findViewById(R.id.message_inbox);
            message_inbox.setText(message);

            //Button actions
            accept = mail_block.findViewById(R.id.accept_invitation);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Change group
                    create_data_insert_invitations(invitacion);
                    transform_json_to_insert_invitations(invitacion);

                    //Delete invitation
                    create_data_delete_invitations(invitacion.getIdinvitacion());
                    transform_json_to_delete_invitations();
                    linear_inbox.removeView(mail_block);
                }
            });

            deny = mail_block.findViewById(R.id.deny_invitation);
            deny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Delete invitation
                    create_data_delete_invitations(invitacion.getIdinvitacion());
                    transform_json_to_delete_invitations();
                    linear_inbox.removeView(mail_block);
                }
            });

            linear_inbox.addView(mail_block);

        }
        semaphore.release();
    }

    private void create_data_invitations_alumnos (String nombre_alumno) {
        try {
            semaphore.acquire();
            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            request = new Request.Builder()
                    .url(getString(R.string.url) + "invitaciones/" + nombre_alumno)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
        catch (InterruptedException e){

        }
    }

    private void transform_json_to_invitations () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
                semaphore.release();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string_response = response.body().string();
                invitacions = new ArrayList<>();
                try {
                    JSONArray json_response = new JSONArray(string_response);
                    //Insert invitacion into array
                    for (int i = 0; i < json_response.length(); i++){
                        Invitacion invitacion = new Invitacion(json_response.getJSONObject(i));
                        invitacions.add(invitacion);
                    }
                    semaphore.release();
                }catch (JSONException e){
                    //Si es vacio quiere decir que no existe usuario con ese correo
                    semaphore.release();

                }
            }
        });
    }

    private void create_data_delete_invitations (int idinvitation) {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        request = new Request.Builder()
                .url(getString(R.string.url) + "invitaciones/" + String.valueOf(idinvitation))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .delete()
                .build();
    }

    private void transform_json_to_delete_invitations () {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void create_data_insert_invitations (Invitacion invitacion) {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        int idalumno = ((MainActivity)getActivity()).getAlumno().getIdusuario();

        String content = "{\"idalumno\": "+ idalumno +", " +
                "\"idgrupo\":" + invitacion.getAlumno_has_grupo_grupo_idgrupo() + ", " +
                "\"idasignatura\":" + invitacion.getAlumno_has_grupo_grupo_practica_idpractica() + "}";

        RequestBody body = RequestBody.create(MEDIA_TYPE, content);

        request = new Request.Builder()
                .url(getString(R.string.url) + "invitaciones/")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private void transform_json_to_insert_invitations (final Invitacion invitacion) {

        //Init http client for api calls
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                remove_name(invitacion.getAlumno_has_grupo_grupo_practica_idpractica(), invitacion.getAlumno_invitado());
                ((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().get
                        (invitacion.getAlumno_has_grupo_grupo_practica_idpractica()).get
                        (invitacion.getNombre_grupo()).add(invitacion.getAlumno_invitado());
            }
        });
    }

    private void remove_name (int idpractica, String nombre){
        for (Map.Entry<String, ArrayList<String>> entry : ((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().get
                (idpractica).entrySet()) {
            if (entry.getValue().contains(nombre)){
                entry.getValue().remove(nombre);
                break;
            }
        }
    }
}
