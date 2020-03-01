package com.pablochamorro.practicasupm.classes;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

public class Practica {

    private Date group_creation, group_close, finish_practica;
    private String name, enunciado;
    private int asignatura_idasignatura, idpractica, personas_grupo;

    public Practica(){
    }

    public Practica(JSONObject practice_json_response)  throws JSONException {
        String aux;
        if (practice_json_response.has("idpractica")){
            idpractica = practice_json_response.getInt("idpractica");
        }
        else {
            idpractica = -1;
        }
        if (practice_json_response.has("creacion_grupo")){
            aux = practice_json_response.getString("creacion_grupo");
            group_creation = transform_string_to_date(aux);
        }
        else {
            group_creation = null;
        }
        if (practice_json_response.has("cierre_grupo")){
            aux = practice_json_response.getString("cierre_grupo");
            group_close = transform_string_to_date(aux);
        }
        else {
            group_close = null;
        }
        if (practice_json_response.has("entrega_practica")){
            aux = practice_json_response.getString("entrega_practica");
            finish_practica = transform_string_to_date(aux);
        }
        else {
            finish_practica = null;
        }if (practice_json_response.has("nombre")){
            name = practice_json_response.getString("nombre");
        }if (practice_json_response.has("asignatura_idasignatura")){
            asignatura_idasignatura = practice_json_response.getInt("asignatura_idasignatura");
        }
        else {
            asignatura_idasignatura = -1;
        }if (practice_json_response.has("personas_grupo")){
            personas_grupo = practice_json_response.getInt("personas_grupo");
        }
        else {
            personas_grupo = -1;
        }
        if (practice_json_response.has("enunciado")){
            enunciado = practice_json_response.getString("enunciado");
        }
        else {
            enunciado = null;
        }
    }

    private Date transform_string_to_date (String cadena){
        int pos = cadena.indexOf('T');
        cadena = cadena.substring(0, pos);
        return Date.valueOf(cadena);

    }

    public Date getGroup_creation() {
        return group_creation;
    }

    public void setGroup_creation(Date group_creation) {
        this.group_creation = group_creation;
    }

    public Date getGroup_close() {
        return group_close;
    }

    public void setGroup_close(Date group_close) {
        this.group_close = group_close;
    }

    public Date getFinish_practica() {
        return finish_practica;
    }

    public void setFinish_practica(Date finish_practica) {
        this.finish_practica = finish_practica;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public int getAsignatura_idasignatura() {
        return asignatura_idasignatura;
    }

    public int getIdpractica() {
        return idpractica;
    }

    public int getPersonas_grupo() {
        return personas_grupo;
    }


}
