
package com.period.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public abstract class MultiWeekView extends BaseWeekView {

    public MultiWeekView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mItems.size() == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();

        for (int i = 0; i < 7; i++) {
            int x = i * mItemWidth + mDelegate.getCalendarPadding();
            onLoopStart(x);
            Calendar calendar = mItems.get(i);
            boolean isSelected = isCalendarSelected(calendar);
            boolean isPreSelected = isSelectPreCalendar(calendar);
            boolean isNextSelected = isSelectNextCalendar(calendar);
            boolean hasScheme = calendar.hasScheme();
            if (hasScheme) {
                boolean isDrawSelected = false;
                if (isSelected) {
                    isDrawSelected = onDrawSelected(canvas, calendar, x, true, isPreSelected, isNextSelected);
                }
                if (isDrawSelected || !isSelected) {

                    mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                    onDrawScheme(canvas, calendar, x, isSelected);
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false, isPreSelected, isNextSelected);
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected);
        }
    }


    protected boolean isCalendarSelected(Calendar calendar) {
        return !onCalendarIntercept(calendar) && mDelegate.mSelectedCalendars.containsKey(calendar.toString());
    }

    @Override
    public void onClick(View v) {
        if (!isClick) {
            return;
        }
        Calendar calendar = getIndex();
        if (calendar == null) {
            return;
        }
        if (onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, true);
            return;
        }
        if (!isInRange(calendar)) {
            if (mDelegate.mCalendarMultiSelectListener != null) {
                mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelectOutOfRange(calendar);
            }
            return;
        }


        String key = calendar.toString();

        if (mDelegate.mSelectedCalendars.containsKey(key)) {
            mDelegate.mSelectedCalendars.remove(key);
        } else {
            if (mDelegate.mSelectedCalendars.size() >= mDelegate.getMaxMultiSelectSize()) {
                if (mDelegate.mCalendarMultiSelectListener != null) {
                    mDelegate.mCalendarMultiSelectListener.onMultiSelectOutOfSize(calendar,
                            mDelegate.getMaxMultiSelectSize());
                }
                return;
            }
            mDelegate.mSelectedCalendars.put(key, calendar);
        }

        mCurrentItem = mItems.indexOf(calendar);

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, true);
        }
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }

        if (mDelegate.mCalendarMultiSelectListener != null) {
            mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelect(
                    calendar,
                    mDelegate.mSelectedCalendars.size(),
                    mDelegate.getMaxMultiSelectSize());
        }

        invalidate();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    protected final boolean isSelectPreCalendar(Calendar calendar) {
        Calendar preCalendar = CalendarUtil.getPreCalendar(calendar);
        mDelegate.updateCalendarScheme(preCalendar);
        return isCalendarSelected(preCalendar);
    }

    protected final boolean isSelectNextCalendar(Calendar calendar) {
        Calendar nextCalendar = CalendarUtil.getNextCalendar(calendar);
        mDelegate.updateCalendarScheme(nextCalendar);
        return isCalendarSelected(nextCalendar);
    }

    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme,
                                              boolean isSelectedPre, boolean isSelectedNext);

    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, boolean isSelected);


    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected);
}
