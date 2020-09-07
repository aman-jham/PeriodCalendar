
package com.period.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public abstract class MultiMonthView extends BaseMonthView {

    public MultiMonthView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLineCount == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();
        int count = mLineCount * 7;
        int d = 0;
        for (int i = 0; i < mLineCount; i++) {
            for (int j = 0; j < 7; j++) {
                Calendar calendar = mItems.get(d);
                if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH) {
                    if (d > mItems.size() - mNextDiff) {
                        return;
                    }
                    if (!calendar.isCurrentMonth()) {
                        ++d;
                        continue;
                    }
                } else if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_FIT_MONTH) {
                    if (d >= count) {
                        return;
                    }
                }
                draw(canvas, calendar, i, j);
                ++d;
            }
        }
    }

    private void draw(Canvas canvas, Calendar calendar, int i, int j) {
        int x = j * mItemWidth + mDelegate.getCalendarPadding();
        int y = i * mItemHeight;
        onLoopStart(x, y);
        boolean isSelected = isCalendarSelected(calendar);
        boolean hasScheme = calendar.hasScheme();
        boolean isPreSelected = isSelectPreCalendar(calendar);
        boolean isNextSelected = isSelectNextCalendar(calendar);

        if (hasScheme) {
            //标记的日子
            boolean isDrawSelected = false;//是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true, isPreSelected, isNextSelected);
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                onDrawScheme(canvas, calendar, x, y, true);
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false, isPreSelected, isNextSelected);
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected);
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

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
                && !calendar.isCurrentMonth()) {
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

        if (!calendar.isCurrentMonth() && mMonthViewPager != null) {
            int cur = mMonthViewPager.getCurrentItem();
            int position = mCurrentItem < 7 ? cur - 1 : cur + 1;
            mMonthViewPager.setCurrentItem(position);
        }

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onMonthDateSelected(calendar, true);
        }

        if (mParentLayout != null) {
            if (calendar.isCurrentMonth()) {
                mParentLayout.updateSelectPosition(mItems.indexOf(calendar));
            } else {
                mParentLayout.updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart()));
            }
        }
        if (mDelegate.mCalendarMultiSelectListener != null) {
            mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelect(
                    calendar,
                    mDelegate.mSelectedCalendars.size(),
                    mDelegate.getMaxMultiSelectSize());
        }
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

    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme,
                                              boolean isSelectedPre, boolean isSelectedNext);

    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y, boolean isSelected);


    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected);
}
