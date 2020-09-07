
package com.period.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Constructor;
import java.util.List;

public final class WeekViewPager extends ViewPager {
    private boolean isUpdateWeekView;
    private int mWeekCount;
    private CalendarViewDelegate mDelegate;

    CalendarLayout mParentLayout;

    private boolean isUsingScrollToCalendar = false;

    public WeekViewPager(Context context) {
        this(context, null);
    }

    public WeekViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        init();
    }

    private void init() {
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        setAdapter(new WeekViewPagerAdapter());
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //默认的显示星期四，周视图切换就显示星期4
                if (getVisibility() != VISIBLE) {
                    isUsingScrollToCalendar = false;
                    return;
                }
                if (isUsingScrollToCalendar) {
                    isUsingScrollToCalendar = false;
                    return;
                }
                BaseWeekView view = findViewWithTag(position);
                if (view != null) {
                    view.performClickCalendar(mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT ?
                            mDelegate.mIndexCalendar : mDelegate.mSelectedCalendar, !isUsingScrollToCalendar);
                    if (mDelegate.mWeekChangeListener != null) {
                        mDelegate.mWeekChangeListener.onWeekChange(getCurrentWeekCalendars());
                    }
                }
                isUsingScrollToCalendar = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    List<Calendar> getCurrentWeekCalendars() {
        List<Calendar> calendars = CalendarUtil.getWeekCalendars(mDelegate.mIndexCalendar,
                mDelegate);
        mDelegate.addSchemesFromMap(calendars);
        return calendars;
    }


    void notifyDataSetChanged() {
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        notifyAdapterDataSetChanged();
    }

    void updateWeekViewClass() {
        isUpdateWeekView = true;
        notifyAdapterDataSetChanged();
        isUpdateWeekView = false;
    }

    void updateRange() {
        isUpdateWeekView = true;
        notifyDataSetChanged();
        isUpdateWeekView = false;
        if (getVisibility() != VISIBLE) {
            return;
        }
        isUsingScrollToCalendar = true;
        Calendar calendar = mDelegate.mSelectedCalendar;
        updateSelected(calendar, false);
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, false);
        }

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, false);
        }

        int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    void scrollToCalendar(int year, int month, int day, boolean smoothScroll, boolean invokeListener) {
        isUsingScrollToCalendar = true;
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setCurrentDay(calendar.equals(mDelegate.getCurrentDay()));
        // LunarCalendar.setupLunarCalendar(calendar);
        mDelegate.mIndexCalendar = calendar;
        mDelegate.mSelectedCalendar = calendar;
        mDelegate.updateSelectCalendarScheme();
        updateSelected(calendar, smoothScroll);
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, false);
        }
        if (mDelegate.mCalendarSelectListener != null && invokeListener) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, false);
        }
        int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    void scrollToCurrent(boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        int position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(mDelegate.getCurrentDay(),
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getWeekStart()) - 1;
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }
        setCurrentItem(position, smoothScroll);
        BaseWeekView view = findViewWithTag(position);
        if (view != null) {
            view.performClickCalendar(mDelegate.getCurrentDay(), false);
            view.setSelectedCalendar(mDelegate.getCurrentDay());
            view.invalidate();
        }

        if (mDelegate.mCalendarSelectListener != null && getVisibility() == VISIBLE) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
        }

        if (getVisibility() == VISIBLE) {
            mDelegate.mInnerListener.onWeekDateSelected(mDelegate.getCurrentDay(), false);
        }
        int i = CalendarUtil.getWeekFromDayInMonth(mDelegate.getCurrentDay(), mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    void updateSelected(Calendar calendar, boolean smoothScroll) {
        int position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(calendar,
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getWeekStart()) - 1;
        int curItem = getCurrentItem();
        isUsingScrollToCalendar = curItem != position;
        setCurrentItem(position, smoothScroll);
        BaseWeekView view = findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(calendar);
            view.invalidate();
        }
    }


    void updateSingleSelect() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateSingleSelect();
        }
    }

    void updateDefaultSelect() {
        BaseWeekView view = findViewWithTag(getCurrentItem());
        if (view != null) {
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            view.invalidate();
        }
    }

    void updateSelected() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            view.invalidate();
        }
    }

    final void updateStyle() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateStyle();
            view.invalidate();
        }
    }

    void updateScheme() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.update();
        }
    }

    void updateCurrentDate() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateCurrentDate();
        }
    }

    void updateShowMode() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateShowMode();
        }
    }

    void updateWeekStart() {
        if (getAdapter() == null) {
            return;
        }
        int count = getAdapter().getCount();
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        /*
         * 如果count发生变化，意味着数据源变化，则必须先调用notifyDataSetChanged()，
         * 否则会抛出异常
         */
        if (count != mWeekCount) {
            isUpdateWeekView = true;
            getAdapter().notifyDataSetChanged();
        }
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateWeekStart();
        }
        isUpdateWeekView = false;
        updateSelected(mDelegate.mSelectedCalendar, false);
    }

    final void updateItemHeight() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateItemHeight();
            view.requestLayout();
        }
    }

    final void clearSelectRange() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.invalidate();
        }
    }

    final void clearSingleSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    final void clearMultiSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    private void notifyAdapterDataSetChanged() {
        if (getAdapter() == null) {
            return;
        }
        getAdapter().notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDelegate.isWeekViewScrollable() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDelegate.isWeekViewScrollable() && super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getCalendarItemHeight(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class WeekViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mWeekCount;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return isUpdateWeekView ? POSITION_NONE : super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Calendar calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(mDelegate.getMinYear(),
                    mDelegate.getMinYearMonth(),
                    mDelegate.getMinYearDay(),
                    position + 1,
                    mDelegate.getWeekStart());
            BaseWeekView view;
            try {
                Constructor constructor = mDelegate.getWeekViewClass().getConstructor(Context.class);
                view = (BaseWeekView) constructor.newInstance(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultWeekView(getContext());
            }
            view.mParentLayout = mParentLayout;
            view.setup(mDelegate);
            view.setup(calendar);
            view.setTag(position);
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            BaseWeekView view = (BaseWeekView) object;
            view.onDestroy();
            container.removeView(view);
        }
    }
}
