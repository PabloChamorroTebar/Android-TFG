package com.pablochamorro.practicasupm.classes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Alumno implements Serializable {
    private String name, email, matricula, password;
    private int idusuario;

    public Alumno (){

    }

    public Alumno (JSONObject user_json_response) throws JSONException {
        //Check is key nombre exist
        if(user_json_response.has("nombre")){
            this.name = user_json_response.getString("nombre");
        }
        else{
            this.name = null;
        }
        //Check is key email exist
        if(user_json_response.has("email")){
            this.email = user_json_response.getString("email");
        }
        else{
            this.email = null;
        }
        //Check is key matricula exist
        if (user_json_response.has("matricula")){
            this.matricula = user_json_response.getString("matricula");
        }
        else{
            this.matricula = null;
        }
        //Check is key password exist
        if (user_json_response.has("password")){
            this.password = user_json_response.getString("password");
        }
        else{
            this.password = null;
        }
        //Check is key idusuario exist
        if(user_json_response.has("idusuario")){
            this.idusuario = user_json_response.getInt("idusuario");
        }
        else{
            this.idusuario = -1;
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEmail() {
        return email;
    }

    public String getMatricula() {
        return matricula;
    }

    public int getIdusuario() {
        return idusuario;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
