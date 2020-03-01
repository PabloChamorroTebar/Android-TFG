package com.pablochamorro.practicasupm;

import org.json.JSONException;
import org.json.JSONObject;

public class Grupo {
    String name;
    int idgrupo, practica_idpractica;

    public Grupo(){}

    public Grupo(JSONObject response) throws JSONException {
        if (response.has("nombre")){
            name = response.getString("nombre");
        }
        else {
            name = null;
        }
        if (response.has("idgrupo")){
            idgrupo = response.getInt("idgrupo");
        }
        else {
            idgrupo = -1;
        }
        if (response.has("practica_idpractica")){
            practica_idpractica = response.getInt("practica_idpractica");
        }
        else {
            practica_idpractica = -1;
        }
    }

    public String getName() {
        return name;
    }

    public int getIdgrupo() {
        return idgrupo;
    }

    public int getPractica_idpractica() {
        return practica_idpractica;
    }
}
