package com.pablochamorro.practicasupm.ui.practica;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pablochamorro.practicasupm.classes.Alumno;
import com.pablochamorro.practicasupm.Grupo;
import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.classes.Practica;
import com.pablochamorro.practicasupm.R;
import com.pablochamorro.practicasupm.SearchActivity;
import com.pablochamorro.practicasupm.ui.pdf.PdfFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PracticaFragment extends Fragment {

    private PracticaViewModel practicaViewModel;
    private ImageView button_invite;
    private String name_practica;
    private int idpractica, idgrupo;
    private ArrayList<Grupo> grupos;
    private Alumno alumno;
    private Map<String,ArrayList<String>> alumnos_grupo;
    private Request request;
    private OkHttpClient client;
    private boolean querry_correct = false;
    private Practica practica;
    private boolean receive_confirm= false;
    private boolean ingroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        grupos = new ArrayList<>();

        ingroup = false;

        name_practica = args.getString("Practica");
        idpractica = args.getInt("idpractica");
        View root = inflater.inflate(R.layout.fragment_practica, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView subject_name = getActivity().findViewById(R.id.practica_name);
        subject_name.setText(name_practica);

        LinearLayout enunciado_practica = getActivity().findViewById(R.id.linear_enunciado_practica);

        //Obtenemos las practicas de la asignatura para buscar la practica en cuestion
        String asignatura_name = ((MainActivity)getActivity()).get_asignatura_pointed();
        ArrayList<Practica> practicas = ((MainActivity)getActivity()).getPracticas_by_asignatura().
                get(((MainActivity)getActivity()).getAsignatura_by_name(asignatura_name));

        //Obtenemos nuestra practica
        practica = ((MainActivity)getActivity()).getPractica_by_name(practicas, name_practica);

        enunciado_practica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new PdfFragment();
                Bundle bundle = new Bundle();
                bundle.putString("pdf", practica.getEnunciado());
                int id = ((ViewGroup)getView().getParent()).getId();

                //Insert argument
                fragment.setArguments(bundle);

                //Replace fragment
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(id, fragment);
                ft.commit();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().
                        replace(id, fragment).
                        commit();

            }
        });

        //Grupos de esta practica
        for (Grupo grupo : ((MainActivity)getActivity()).getGrupos()){
            if(grupo.getPractica_idpractica() == idpractica)
                grupos.add(grupo);
        }

        // create a java calendar instance
        Calendar calendar = Calendar.getInstance();

        // get a java date (java.util.Date) from the Calendar instance.
        // this java date will represent the current date, or "now".
        java.util.Date currentDate = calendar.getTime();

        // now, create a java.sql.Date from the java.util.Date
        java.sql.Date date = new java.sql.Date(currentDate.getTime());


        //Si la fecha actual es menor a la crear grupos no lo hacemos
        if (practica != null && practica.getGroup_creation().compareTo(date) < 0) {
            button_invite = getActivity().findViewById(R.id.button_invite);

            button_invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ingroup){
                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putExtra("idpractica", idpractica);
                        intent.putExtra("alumnos_nombre", ((MainActivity) getActivity()).getAlumno().getName());
                        intent.putExtra("idgrupo", idgrupo);
                        intent.putExtra("idalumno", ((MainActivity) getActivity()).getAlumno().getIdusuario());
                        startActivity(intent);
                    }
                }
            });
            create_group_layout();
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void create_group_layout() {

        LinearLayout linear_practica = getActivity().findViewById(R.id.linear_practica);
        LinearLayout linear_groups;
        LinearLayout linear_names_group;
        TextView name_alumno;
        ArrayList<String> alumnos = new ArrayList<>();


        //Insert all empty groups
        insert_empty_groups();

        alumno = ((MainActivity) getActivity()).getAlumno();
        for (final Grupo grupo : grupos) {
            linear_groups = (LinearLayout) View.inflate(getActivity(),
                    R.layout.group_layout, null);

            TextView name_group = linear_groups.findViewById(R.id.name_group);

            name_group.setText("Grupo " + grupo.getName());

            LinearLayout.LayoutParams params_group_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params_group_layout.setMargins(0, 0, 0, 50);

            linear_groups.setLayoutParams(params_group_layout);

            alumnos = alumnos_grupo.get(grupo.getName());

            final Button join_button = linear_groups.findViewById(R.id.join_button);


            if (alumnos != null) {
                //Check if alumno is join
                Boolean unido = false;
                for (String alumno : alumnos) {

                    unido = false;

                    name_alumno = (TextView) View.inflate(getActivity(),
                            R.layout.name_person_group, null);

                    if (alumno.equals(this.alumno.getName())) {
                        ingroup = true;
                        idgrupo = grupo.getIdgrupo();
                        unido = true;
                        name_alumno.setId(0);
                        change_button(join_button,"Desunirse");
                    }

                    name_alumno.setText(alumno);

                    name_alumno.setTextColor(Color.BLACK);


                    linear_names_group = linear_groups.findViewById(R.id.linear_names_group);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(20, 0, 0, 0);
                    name_alumno.setLayoutParams(params);

                    linear_names_group.addView(name_alumno);
                }
                if (unido){
                }
            }

            //Button on click
            join_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout linear_group = (LinearLayout) join_button.getParent().getParent();
                    TextView textView_name_group = (TextView) linear_group.findViewById(R.id.name_group);
                    String name_group = remove_group(textView_name_group.getText().toString());
                    ArrayList<String> alumnos;
                    //Buscar el grupo comprobar que esta, si esta eliminarlo, tanto de la vista como del array
                    //Si esta en otro grupo poner que promero tiene que desunirse para uni
                    //Cuando creemos con el nombre, poner un id para cogerlo directamente
                    LinearLayout layout_names = (LinearLayout) join_button.getParent().getParent();
                    alumnos = alumnos_grupo.get(name_group);

                    if (join_button.getText().equals("Desunirse")){

                        if (alumnos != null && alumnos.contains(alumno.getName())){
                            create_data_delete_me(name_group, alumno.getIdusuario());
                            send_query();
                                TextView my_name = layout_names.findViewById(0);
                                alumnos.remove(alumno.getName());
                                alumnos_grupo.put(name_group, alumnos);
                                layout_names.removeView(my_name);
                                change_button(join_button, "Unirse");
                                ingroup = false;
                        }
                    }
                    else if (join_button.getText().equals("Unirse")){

                        if (getActivity().findViewById(0) == null){
                            create_data_insert_me(name_group, alumno.getIdusuario());
                            idgrupo = findIdGrupo(name_group);

                            receive_confirm = false;

                            send_query();

                            //Wait to receive data
                            while (!receive_confirm);


                            if (querry_correct && alumnos_grupo.get(name_group).size() < practica.getPersonas_grupo()){

                                TextView name_alumno = (TextView) View.inflate(getActivity(),
                                        R.layout.name_person_group, null);

                                name_alumno.setText(alumno.getName());

                                name_alumno.setTextColor(Color.BLACK);

                                alumnos.add(alumno.getName());

                                name_alumno.setId(0);

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(20, 0, 0, 0);
                                name_alumno.setLayoutParams(params);

                                layout_names.addView(name_alumno);
                                change_button(join_button, "Desunirse");
                                ingroup = true;

                            }
                            else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() { @Override public void run() { Toast.makeText(getActivity().getBaseContext(), "Grupo lleno", Toast.LENGTH_LONG).show(); } });
                            }
                        }
                    }
                    ((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().put(idpractica, alumnos_grupo);
                }
            });

            linear_practica.addView(linear_groups);
        }
    }

    private void change_button (Button join_button, String text){
        join_button.setText(text);
        RelativeLayout.LayoutParams button_params = new RelativeLayout.LayoutParams(dpToPx(90), dpToPx(35));
        button_params.addRule(RelativeLayout.CENTER_IN_PARENT);
        join_button.setLayoutParams(button_params);
    }

    private String remove_group (String name_practica){
        int pos_space = name_practica.indexOf(' ');
        return name_practica.substring(pos_space+1);
    }

    private void create_data_insert_me(String group_name, int idalumno){
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        int idgroup = name_group_to_idgroup(group_name);

        if(idgroup != -1){
            String content = "{\"grupo\": "+ idgroup +"," +
                                "\"idalumno\": "+ idalumno +"}";

            RequestBody body = RequestBody.create(MEDIA_TYPE, content);

            request = new Request.Builder()
                    .url(getString(R.string.url) + "practicas/" + idpractica + "/insert_alumno_group")
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    private void create_data_delete_me(String group_name, int idalumno){
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        int idgroup = name_group_to_idgroup(group_name);

        if(idgroup != -1){
            String content = "{\"grupo\": "+ idgroup +"," +
                    "\"idalumno\": "+ idalumno +"}";

            RequestBody body = RequestBody.create(MEDIA_TYPE, content);

            request = new Request.Builder()
                    .url(getString(R.string.url) + "practicas/" + idpractica + "/delete_alumno_group")
                    .delete(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    private void send_query(){
        client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                receive_confirm = true;
                //Opps ha habido un problema
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200){
                    querry_correct = true;
                }
                receive_confirm = true;
            }
        });
    }

    /*Method to change name_group to idgroup*/
    private int name_group_to_idgroup (String name_group){
        for (Grupo grupo: grupos){
            if (grupo.getName().equals(name_group))
                return grupo.getIdgrupo();
        }
        return -1;
    }

    private void insert_empty_groups (){
        //Insert groups in view

        //Insert practica if alumnos_grupo = null
        if (((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().get(idpractica) == null){
            ((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().put(idpractica, new HashMap<String, ArrayList<String>>());
        }

        alumnos_grupo = ((MainActivity) getActivity()).getMap_grupos_practicas_alumnos().get(idpractica);

        //Insert all empty groups
        if (grupos != null) {
            for (Grupo grupo : grupos){
                if (alumnos_grupo.get(grupo.getName()) == null){
                    alumnos_grupo.put(grupo.getName(), new ArrayList<String>());
                }
            }
        }
    }

    private int findIdGrupo (String name_group){
        int result = -1;
        for (Grupo grupo : grupos){
            if(grupo.getName().equals(name_group)){
                result = grupo.getIdgrupo();
                break;
            }
        }
        return  result;
    }
}
