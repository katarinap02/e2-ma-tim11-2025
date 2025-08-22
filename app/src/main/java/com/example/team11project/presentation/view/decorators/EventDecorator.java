package com.example.team11project.presentation.view.decorators;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//koje dane kalendar da oboji
public class EventDecorator implements DayViewDecorator {

    private final CalendarDay date;
    private final List<Integer> colors;

    public EventDecorator(CalendarDay date, List<Integer> colors) {
        this.date = date;
        this.colors = colors;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        // Ukrasi samo ako se dan poklapa sa onim za koji je ovaj dekorator napravljen
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (colors != null && !colors.isEmpty()) {
            view.addSpan(new MultiDotSpan(5, colors));
        }
    }
}
