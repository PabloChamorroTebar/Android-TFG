package com.pablochamorro.practicasupm.classes;

import java.util.ArrayList;
import java.sql.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class Notices {

    //String is date and ArrayList are notices
    Map<Date, ArrayList<String>> notices;

    public Notices (ArrayList<Practica> practicas){
        if (practicas != null){
            notices = new TreeMap<>();
            for (Practica practica : practicas){
                insert_creation_group(practica);
                insert_close_group(practica);
                insert_close_practica(practica);
                //order_notices();
            }
        }
    }

    private String date_to_String (Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.get(Calendar.DAY_OF_MONTH) +"/"+cal.get(Calendar.MONTH);
    }

    private void insert_creation_group(Practica practica){

        String date = date_to_String(practica.getGroup_creation());
        if(notices.containsKey(practica.getGroup_creation())){
            notices.get(practica.getGroup_creation()).add("OPENING OF GROUP IN "+ practica.getName().toUpperCase());
        }
        else {
            ArrayList<String> array_notices = new ArrayList<>();
            array_notices.add("OPENING OF GROUP IN "+ practica.getName().toUpperCase());
            notices.put(practica.getGroup_creation(), array_notices);
        }
    }

    private void insert_close_group(Practica practica){

        String date = date_to_String(practica.getGroup_close());
        if(notices.containsKey(practica.getGroup_close())){
            notices.get(practica.getGroup_close()).add("CLOSING OF GROUP IN "+ practica.getName().toUpperCase());
        }
        else {
            ArrayList<String> array_notices = new ArrayList<>();
            array_notices.add("CLOSING OF GROUP IN "+ practica.getName().toUpperCase());
            notices.put(practica.getGroup_close(), array_notices);
        }
    }

    private void insert_close_practica(Practica practica){

        String date = date_to_String(practica.getFinish_practica());
        if(notices.containsKey(practica.getFinish_practica())){
            notices.get(practica.getFinish_practica()).add(practica.getName().toUpperCase() + " PRACTICA DELIVERY");
        }
        else {
            ArrayList<String> array_notices = new ArrayList<>();
            array_notices.add(practica.getName().toUpperCase() + " PRACTICA DELIVERY");
            notices.put(practica.getFinish_practica(), array_notices);
        }
    }


    public Map<Date, ArrayList<String>> getNotices() {
        return notices;
    }
}
