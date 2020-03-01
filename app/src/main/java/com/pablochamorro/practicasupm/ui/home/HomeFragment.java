package com.pablochamorro.practicasupm.ui.home;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.classes.Notices;
import com.pablochamorro.practicasupm.R;
import com.pablochamorro.practicasupm.ui.calendar.CalendarFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.Map;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private int id_notify = 0;
    private TableLayout field_latest_notices;
    private TableLayout field_mini_calendar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //homeViewModel =
         //       ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        create_field_latest_notices();
        create_field_mini_calendar();
    }

    private void create_field_latest_notices () {
        Notices notices = ((MainActivity)getActivity()).getNotices();
        field_latest_notices = getActivity().findViewById(R.id.field_latest_notices);
        TableRow row;
        for (Map.Entry<Date, ArrayList<String>> entry : notices.getNotices().entrySet()) {
            // Compare with one because if its the same day compare minutes and seconds
            if(comparetoDates(entry.getKey())){
                for (String notice : entry.getValue()) {
                    TextView fecha_notice = new TextView(getActivity());
                    TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.3f);
                    fecha_notice.setLayoutParams(paramsExample);
                    fecha_notice.setText(date_to_String(entry.getKey()));
                    notifications(entry.getKey(), notice);
                    fecha_notice.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                    fecha_notice.setTextColor(Color.BLACK);
                    row = new TableRow(getActivity());
                    row.addView(fecha_notice);
                    TextView texto_notice = new TextView(getActivity());
                    paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,0.7f);
                    texto_notice.setLayoutParams(paramsExample);
                    texto_notice.setText(notice);
                    texto_notice.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                    texto_notice.setTextColor(Color.BLACK);
                    row.addView(texto_notice);
                    field_latest_notices.addView(row);
                }
            }
        }
    }

    private void create_field_mini_calendar () {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        //Obtain current day
        int current_day = calendar.get(Calendar.DATE);

        field_mini_calendar = getActivity().findViewById(R.id.field_mini_calendar);

        //When you click yo calendar it going to calendar
        field_mini_calendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new CalendarFragment();
                String title = "Calendar";
                ((MainActivity)getActivity()).getSupportActionBar().setTitle(title);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }
        });

        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT));

        TextView texto;

        //Create first 3 days
        for (int i = 3; i > 0; i--){
            texto = new TextView(getActivity());
            TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,1.0f);
            texto.setLayoutParams(paramsExample);
            texto.setTextColor(Color.parseColor("#0068AF"));
            //texto.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
            texto.setText(Integer.toString(current_day-i));
            texto.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            row.addView(texto);
        }

        //Insert current day

        texto = new TextView(getActivity());

        //Put the correct size
        TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,1.0f);

        //Create Spannable to subrayar text
        Spannable spannable = new SpannableString(Integer.toString(current_day));
        spannable.setSpan(new UnderlineSpan(),0, Integer.toString(current_day).length(), 0);

        texto.setLayoutParams(paramsExample);
        texto.setTextColor(Color.parseColor("#0A5283"));
        texto.setText(spannable);
        texto.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        texto.setTypeface(null, Typeface.BOLD);
        row.addView(texto);

        //Create second 3 days
        for (int i = 1; i < 4; i++){
            texto = new TextView(getActivity());
            paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT,1.0f);
            texto.setLayoutParams(paramsExample);
            texto.setTextColor(Color.parseColor("#0068AF"));
            //texto.setLayoutParams(new TableLayout.LayoutParams(100, 100, 1f));
            texto.setText(Integer.toString(current_day+i));
            texto.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            row.addView(texto);
        }

            field_mini_calendar.addView(row);
    }

    private String date_to_String (Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        if (cal.get(Calendar.MONTH) == 0){
            return cal.get(Calendar.DAY_OF_MONTH) +"/12";
        }
        return cal.get(Calendar.DAY_OF_MONTH) +"/"+cal.get(Calendar.MONTH);
    }

    private void notifications (Date date, String notification_text) {
        Calendar cal = Calendar.getInstance();
        Calendar current_date = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.MONTH) == current_date.get(Calendar.MONTH) && cal.get(Calendar.DAY_OF_MONTH) == current_date.get(Calendar.DAY_OF_MONTH)) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {

                NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "Canal 1";
                    String description = "Mi canal";
                    NotificationChannel channel = new NotificationChannel(name.toString(), name,  NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this

                    notificationManager.createNotificationChannel(channel);

                    notification_text = " "+ notification_text;


                    Notification builder = new NotificationCompat.Builder(getContext(), "Canal 1")
                            .setSmallIcon(R.drawable.ic_calendar)
                            .setContentTitle(getString(R.string.notification_title))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.Tomorrow)+ notification_text))
                            .setContentText(getString(R.string.Tomorrow)+ " " + notification_text)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(id_notify++, builder);
                }
            }
        }
    }

    private boolean comparetoDates(java.util.Date date){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar current_date = Calendar.getInstance();
        return cal1.get(Calendar.YEAR) >= current_date.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) >= current_date.get(Calendar.DAY_OF_YEAR);
    }
}