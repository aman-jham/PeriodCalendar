
package com.period.calendarview;

import android.content.Context;

public abstract class BaseWeekView extends BaseView {

    public BaseWeekView(Context context) {
        super(context);
    }

    final void setup(Calendar calendar) {
        mItems = CalendarUtil.initCalendarForWeekView(calendar, mDelegate, mDelegate.getWeekStart());
        addSchemesFromMap();
        invalidate();
    }


    final void setSelectedCalendar(Calendar calendar) {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE &&
                !calendar.equals(mDelegate.mSelectedCalendar)) {
            return;
        }
        mCurrentItem = mItems.indexOf(calendar);
    }


    final void performClickCalendar(Calendar calendar, boolean isNotice) {

        if (mParentLayout == null ||
                mDelegate.mInnerListener == null ||
                mItems == null || mItems.size() == 0) {
            return;
        }

        int week = CalendarUtil.getWeekViewIndexFromCalendar(calendar, mDelegate.getWeekStart());
        if (mItems.contains(mDelegate.getCurrentDay())) {
            week = CalendarUtil.getWeekViewIndexFromCalendar(mDelegate.getCurrentDay(), mDelegate.getWeekStart());
        }

        int curIndex = week;

        Calendar currentCalendar = mItems.get(week);
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (mItems.contains(mDelegate.mSelectedCalendar)) {
                currentCalendar = mDelegate.mSelectedCalendar;
            } else {
                mCurrentItem = -1;
            }
        }

        if (!isInRange(currentCalendar)) {
            curIndex = getEdgeIndex(isMinRangeEdge(currentCalendar));
            currentCalendar = mItems.get(curIndex);
        }


        currentCalendar.setCurrentDay(currentCalendar.equals(mDelegate.getCurrentDay()));
        mDelegate.mInnerListener.onWeekDateSelected(currentCalendar, false);
        int i = CalendarUtil.getWeekFromDayInMonth(currentCalendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);


        if (mDelegate.mCalendarSelectListener != null
                && isNotice
                && mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(currentCalendar, false);
        }

        mParentLayout.updateContentViewTranslateY();
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            mCurrentItem = curIndex;
        }

        if (!mDelegate.isShowYearSelectedLayout &&
                mDelegate.mIndexCalendar != null &&
                calendar.getYear() != mDelegate.mIndexCalendar.getYear() &&
                mDelegate.mYearChangeListener != null) {
            mDelegate.mYearChangeListener.onYearChange(mDelegate.mIndexCalendar.getYear());
        }

        mDelegate.mIndexCalendar = currentCalendar;
        invalidate();
    }

    final boolean isMinRangeEdge(Calendar calendar) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(mDelegate.getMinYear(), mDelegate.getMinYearMonth() - 1, mDelegate.getMinYearDay());
        long minTime = c.getTimeInMillis();
        c.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
        long curTime = c.getTimeInMillis();
        return curTime < minTime;
    }

    final int getEdgeIndex(boolean isMinEdge) {
        for (int i = 0; i < mItems.size(); i++) {
            Calendar item = mItems.get(i);
            boolean isInRange = isInRange(item);
            if (isMinEdge && isInRange) {
                return i;
            } else if (!isMinEdge && !isInRange) {
                return i - 1;
            }
        }
        return isMinEdge ? 6 : 0;
    }


    protected Calendar getIndex() {

        int indexX = (int) (mX - mDelegate.getCalendarPadding()) / mItemWidth;
        if (indexX >= 7) {
            indexX = 6;
        }
        int indexY = (int) mY / mItemHeight;
        int position = indexY * 7 + indexX;
        if (position >= 0 && position < mItems.size())
            return mItems.get(position);
        return null;
    }


    final void updateShowMode() {
        invalidate();
    }

    final void updateWeekStart() {

        int position = (int) getTag();
        Calendar calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                position + 1,
                mDelegate.getWeekStart());
        setSelectedCalendar(mDelegate.mSelectedCalendar);
        setup(calendar);
    }

    final void updateSingleSelect() {
        if (!mItems.contains(mDelegate.mSelectedCalendar)) {
            mCurrentItem = -1;
            invalidate();
        }
    }

    @Override
    void updateCurrentDate() {
        if (mItems == null)
            return;
        if (mItems.contains(mDelegate.getCurrentDay())) {
            for (Calendar a : mItems) {
                a.setCurrentDay(false);
            }
            int index = mItems.indexOf(mDelegate.getCurrentDay());
            mItems.get(index).setCurrentDay(true);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onPreviewHook() {
        // TODO: 2017/11/16
    }


    @SuppressWarnings("unused")
    protected void onLoopStart(int x) {
        // TODO: 2017/11/16
    }

    @Override
    protected void onDestroy() {

    }
}
