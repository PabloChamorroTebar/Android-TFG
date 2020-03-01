package com.pablochamorro.practicasupm.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Invitacion {
    private int idinvitacion, alumno_has_grupo_alumno_idusuario, alumno_has_grupo_grupo_idgrupo,
            alumno_has_grupo_grupo_practica_idpractica;
    private String alumno_invitado, alumno_invitante, nombre_practica, nombre_grupo;

    public Invitacion (){}

    public Invitacion (JSONObject invitacion_json_response) throws JSONException {
        if (invitacion_json_response.has("idinvitacion")){
            idinvitacion = invitacion_json_response.getInt("idinvitacion");
        }
        else {
            idinvitacion = -1;
        }
        if (invitacion_json_response.has("alumno_has_grupo_alumno_idusuario")){
            alumno_has_grupo_alumno_idusuario = invitacion_json_response.getInt
                    ("alumno_has_grupo_alumno_idusuario");
        }
        else {
            alumno_has_grupo_alumno_idusuario = -1;
        }
        if (invitacion_json_response.has("alumno_has_grupo_grupo_idgrupo")){
            alumno_has_grupo_grupo_idgrupo = invitacion_json_response.getInt
                    ("alumno_has_grupo_grupo_idgrupo");
        }
        else {
            alumno_has_grupo_grupo_idgrupo = -1;
        }
        if (invitacion_json_response.has("alumno_has_grupo_grupo_practica_idpractica")){
            alumno_has_grupo_grupo_practica_idpractica = invitacion_json_response.getInt("alumno_has_grupo_grupo_practica_idpractica");
        }
        else {
            alumno_has_grupo_grupo_practica_idpractica = -1;
        }
        if (invitacion_json_response.has("alumno_invitado")){
            alumno_invitado = invitacion_json_response.getString("alumno_invitado");
        }
        else {
            alumno_invitado = null;
        }
        if (invitacion_json_response.has("alumno_invitante")){
            alumno_invitante = invitacion_json_response.getString("alumno_invitante");
        }
        else {
            alumno_invitante = null;
        }
        if (invitacion_json_response.has("nombre_practica")){
            nombre_practica = invitacion_json_response.getString("nombre_practica");
        }
        else {
            nombre_practica = null;
        }
        if (invitacion_json_response.has("nombre_grupo")){
            nombre_grupo = invitacion_json_response.getString("nombre_grupo");
        }
        else {
            nombre_grupo = null;
        }
    }

    public int getIdinvitacion() {
        return idinvitacion;
    }

    public int getAlumno_has_grupo_alumno_idusuario() {
        return alumno_has_grupo_alumno_idusuario;
    }

    public int getAlumno_has_grupo_grupo_idgrupo() {
        return alumno_has_grupo_grupo_idgrupo;
    }

    public int getAlumno_has_grupo_grupo_practica_idpractica() {
        return alumno_has_grupo_grupo_practica_idpractica;
    }

    public String getAlumno_invitado() {
        return alumno_invitado;
    }

    public String getAlumno_invitante() {
        return alumno_invitante;
    }

    public String getNombre_practica() {
        return nombre_practica;
    }

    public String getNombre_grupo() {
        return nombre_grupo;
    }
}
