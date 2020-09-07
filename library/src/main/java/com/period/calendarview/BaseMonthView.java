
package com.period.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;

public abstract class BaseMonthView extends BaseView {

    MonthViewPager mMonthViewPager;

    protected int mYear;

    protected int mMonth;


    protected int mLineCount;

    protected int mHeight;


    protected int mNextDiff;

    public BaseMonthView(Context context) {
        super(context);
    }

    final void initMonthWithDate(int year, int month) {
        mYear = year;
        mMonth = month;
        initCalendar();
        mHeight = CalendarUtil.getMonthViewHeight(year, month, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());

    }

    @SuppressLint("WrongConstant")
    private void initCalendar() {

        mNextDiff = CalendarUtil.getMonthEndDiff(mYear, mMonth, mDelegate.getWeekStart());
        int preDiff = CalendarUtil.getMonthViewStartDiff(mYear, mMonth, mDelegate.getWeekStart());
        int monthDayCount = CalendarUtil.getMonthDaysCount(mYear, mMonth);

        mItems = CalendarUtil.initCalendarForMonthView(mYear, mMonth, mDelegate.getCurrentDay(), mDelegate.getWeekStart());

        if (mItems.contains(mDelegate.getCurrentDay())) {
            mCurrentItem = mItems.indexOf(mDelegate.getCurrentDay());
        } else {
            mCurrentItem = mItems.indexOf(mDelegate.mSelectedCalendar);
        }

        if (mCurrentItem > 0 &&
                mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(mDelegate.mSelectedCalendar)) {
            mCurrentItem = -1;
        }

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ALL_MONTH) {
            mLineCount = 6;
        } else {
            mLineCount = (preDiff + monthDayCount + mNextDiff) / 7;
        }
        addSchemesFromMap();
        invalidate();
    }

    protected Calendar getIndex() {
        if (mItemWidth == 0 || mItemHeight == 0) {
            return null;
        }
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

    final void setSelectedCalendar(Calendar calendar) {
        mCurrentItem = mItems.indexOf(calendar);
    }


    final void updateShowMode() {
        mLineCount = CalendarUtil.getMonthViewLineCount(mYear, mMonth,
                mDelegate.getWeekStart(),mDelegate.getMonthViewShowMode());
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
        invalidate();
    }

    final void updateWeekStart() {
        initCalendar();
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
    }

    @Override
    void updateItemHeight() {
        super.updateItemHeight();
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
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


    protected final int getSelectedIndex(Calendar calendar) {
        return mItems.indexOf(calendar);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLineCount != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onPreviewHook() {

    }


    protected void onLoopStart(int x, int y) {

    }

    @Override
    protected void onDestroy() {

    }
}
