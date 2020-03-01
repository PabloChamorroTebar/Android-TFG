package com.pablochamorro.practicasupm.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.pablochamorro.practicasupm.MainActivity;
import com.pablochamorro.practicasupm.classes.Notices;
import com.pablochamorro.practicasupm.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private CompactCalendarView calendarViewModel;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarViewModel = getActivity().findViewById(R.id.calendarView);
        calendarViewModel.setUseThreeLetterAbbreviation(true);

        final Notices notices = ((MainActivity)getActivity()).getNotices();
        Calendar c = Calendar.getInstance();

        for (Map.Entry<Date, ArrayList<String>> entry : notices.getNotices().entrySet()) {
                Event event;
                for (String notice : entry.getValue()) {
                    c.setTime(entry.getKey());
                    c.add(Calendar.DAY_OF_MONTH,1);
                           event = new Event(Color.BLACK, new java.sql.Date(c.getTimeInMillis()).getTime(), notice);
                        calendarViewModel.addEvent(event);
                    }
            }

        calendarViewModel.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(java.util.Date dateClicked) {
                Calendar c = Calendar.getInstance();
                c.setTime(dateClicked);
                c.add(Calendar.DAY_OF_MONTH,-1);
                ArrayList<String> day_notice = getNoticesbyday(notices, c.getTime());
                TableLayout tableLayout = getActivity().findViewById(R.id.table_calendar);
                tableLayout.removeAllViews();
                if(day_notice!=null){
                    TableRow row;
                    for (String notice : day_notice){
                        row = new TableRow(getActivity());
                        TextView texto_notice = new TextView(getActivity());
                        TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0.7f);
                        texto_notice.setTextColor(Color.BLACK);
                        texto_notice.setLayoutParams(paramsExample);
                        texto_notice.setText(notice);
                        texto_notice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        row.addView(texto_notice);
                        tableLayout.addView(row);
                    }
                }
            }

            @Override
            public void onMonthScroll(java.util.Date firstDayOfNewMonth) {
                // Set title bar
                ((MainActivity) getActivity())
                        .setActionBarTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });
    }

    private ArrayList<String> getNoticesbyday(Notices notices, java.util.Date date){
        Calendar calendar_notice, day_click;
        day_click = Calendar.getInstance();
        day_click.setTime(date);
        for (Map.Entry<Date, ArrayList<String>> entry : notices.getNotices().entrySet()) {
            calendar_notice = Calendar.getInstance();
            calendar_notice.setTime(entry.getKey());
            if(calendar_notice.get(Calendar.DAY_OF_MONTH) == day_click.get(Calendar.DAY_OF_MONTH) && calendar_notice.get(Calendar.MONTH) == day_click.get(Calendar.MONTH)){
                return entry.getValue();
            }
        }
        return null;
    }
}