package com.pablochamorro.practicasupm.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;

public class Asignatura {
    private int idasignatura;
    private String name, guia_docente;

    public Asignatura(){

    }

    public Asignatura (JSONObject asignatura_json) throws JSONException {
        if (asignatura_json.has("idasignatura")){
            idasignatura = asignatura_json.getInt("idasignatura");
        }
        else{
            idasignatura = -1;
        }
        if (asignatura_json.has("nombre")){
            name = asignatura_json.getString("nombre");
        }
        else{
            name = null;
        }
        if (asignatura_json.has("guia_docente")){
            guia_docente = asignatura_json.getString("guia_docente");
        }
    }

    public int getIdasignatura() {
        return idasignatura;
    }

    public String getName() {
        return name;
    }

    public String getGuia_docente() {
        return guia_docente;
    }
}
