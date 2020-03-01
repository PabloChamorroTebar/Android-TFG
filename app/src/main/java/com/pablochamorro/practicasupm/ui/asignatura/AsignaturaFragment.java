package com.pablochamorro.practicasupm.ui.asignatura;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pablochamorro.practicasupm.classes.Asignatura;
import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.classes.Practica;
import com.pablochamorro.practicasupm.R;
import com.pablochamorro.practicasupm.ui.practica.PracticaFragment;
import com.pablochamorro.practicasupm.ui.pdf.PdfFragment;


public class AsignaturaFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_asignatura, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView subject_name = getActivity().findViewById(R.id.subject_name);
        subject_name.setText(((MainActivity)getActivity()).get_asignatura_pointed());

        LinearLayout guia_docente = getActivity().findViewById(R.id.linear_guia_docente);

        // Find asignatura
        String name_asignatura = ((MainActivity)getActivity()).get_asignatura_pointed();
        final Asignatura asignatura = ((MainActivity)getActivity()).getAsignatura_by_name(name_asignatura);

        guia_docente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new PdfFragment();
                Bundle bundle = new Bundle();
                bundle.putString("pdf", asignatura.getGuia_docente());
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

        //Creacion de los text field de las practicas
        TableLayout tableLayout = getActivity().findViewById(R.id.table_subjects);
        TableRow row;
        for (final Practica practica : ((MainActivity)getActivity()).getPracticas_by_asignatura().get(asignatura)) {
            row = new TableRow(getActivity());
            TextView texto_practica = new TextView(getActivity());
            TableRow.LayoutParams params_row = new TableRow.LayoutParams(200, TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params_row);
            TableRow.LayoutParams params_texto_practica = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f);
            params_texto_practica.setMargins(50, 0, 0, 100);
            texto_practica.setLayoutParams(params_texto_practica);
            texto_practica.setText(practica.getName());
            texto_practica.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            texto_practica.setTextColor(Color.parseColor("#0A5283"));
            texto_practica.setTypeface(null, Typeface.BOLD);
            //Creamos el onClick
            texto_practica.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Practica", practica.getName());
                    bundle.putInt("idpractica", practica.getIdpractica());
                    // set Fragmentclass Arguments
                    Fragment fragment = new PracticaFragment();
                    fragment.setArguments(bundle);
                    String title = practica.getName();
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle(title);
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }
            });
            row.addView(texto_practica);
            tableLayout.addView(row);
        }
    }
}
