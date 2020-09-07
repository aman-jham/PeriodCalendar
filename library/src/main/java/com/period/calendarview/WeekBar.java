
package com.period.calendarview;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeekBar extends LinearLayout {
    private CalendarViewDelegate mDelegate;

    public WeekBar(Context context) {
        super(context);
        if ("com.period.calendarview.WeekBar".equals(getClass().getName())) {
            LayoutInflater.from(context).inflate(R.layout.cv_week_bar, this, true);
        }
    }

    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        if ("com.period.calendarview.WeekBar".equalsIgnoreCase(getClass().getName())) {
            setTextSize(mDelegate.getWeekTextSize());
            setTextColor(delegate.getWeekTextColor());
            setBackgroundColor(delegate.getWeekBackground());
            setPadding(delegate.getCalendarPadding(), 0, delegate.getCalendarPadding(), 0);
        }
    }

    protected void setTextColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextColor(color);
        }
    }


    protected void setTextSize(int size) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    protected void onDateSelected(Calendar calendar, int weekStart, boolean isClick) {

    }

    protected void onWeekStartChange(int weekStart) {
        if (!"com.period.calendarview.WeekBar".equalsIgnoreCase(getClass().getName())) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setText(getWeekString(i, weekStart));
        }
    }


    protected int getViewIndexByCalendar(Calendar calendar, int weekStart) {
        int week = calendar.getWeek() + 1;
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == CalendarViewDelegate.WEEK_START_WITH_SUN ? 6 : week - 2;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }

    private String getWeekString(int index, int weekStart) {
        String[] weeks = getContext().getResources().getStringArray(R.array.week_string_array);

        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return weeks[index];
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return weeks[index == 6 ? 0 : index + 1];
        }
        return weeks[index == 0 ? 6 : index - 1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDelegate != null) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getWeekBarHeight(), MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(CalendarUtil.dipToPx(getContext(), 40), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
