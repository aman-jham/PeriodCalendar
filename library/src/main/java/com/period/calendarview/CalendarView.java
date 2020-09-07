
package com.period.calendarview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class CalendarView extends FrameLayout {

    private final CalendarViewDelegate mDelegate;

    private MonthViewPager mMonthPager;

    private WeekViewPager mWeekPager;

    private View mWeekLine;

    private YearViewPager mYearViewPager;

    private WeekBar mWeekBar;

    CalendarLayout mParentLayout;


    public CalendarView(@NonNull Context context) {
        this(context, null);
    }

    public CalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDelegate = new CalendarViewDelegate(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cv_layout_calendar_view, this, true);
        FrameLayout frameContent = findViewById(R.id.frameContent);
        this.mWeekPager = findViewById(R.id.vp_week);
        this.mWeekPager.setup(mDelegate);

        try {
            Constructor constructor = mDelegate.getWeekBarClass().getConstructor(Context.class);
            mWeekBar = (WeekBar) constructor.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frameContent.addView(mWeekBar, 2);
        mWeekBar.setup(mDelegate);
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());

        this.mWeekLine = findViewById(R.id.line);
        this.mWeekLine.setBackgroundColor(mDelegate.getWeekLineBackground());
        LayoutParams lineParams = (LayoutParams) this.mWeekLine.getLayoutParams();
        lineParams.setMargins(mDelegate.getWeekLineMargin(),
                mDelegate.getWeekBarHeight(),
                mDelegate.getWeekLineMargin(),
                0);
        this.mWeekLine.setLayoutParams(lineParams);

        this.mMonthPager = findViewById(R.id.vp_month);
        this.mMonthPager.mWeekPager = mWeekPager;
        this.mMonthPager.mWeekBar = mWeekBar;
        LayoutParams params = (LayoutParams) this.mMonthPager.getLayoutParams();
        params.setMargins(0, mDelegate.getWeekBarHeight() + CalendarUtil.dipToPx(context, 1), 0, 0);
        mWeekPager.setLayoutParams(params);


        mYearViewPager = findViewById(R.id.selectLayout);
        mYearViewPager.setBackgroundColor(mDelegate.getYearViewBackground());
        mYearViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mWeekPager.getVisibility() == VISIBLE) {
                    return;
                }
                if (mDelegate.mYearChangeListener != null) {
                    mDelegate.mYearChangeListener.onYearChange(position + mDelegate.getMinYear());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDelegate.mInnerListener = new OnInnerDateSelectedListener() {
            /**
             * 月视图选择事件
             * @param calendar calendar
             * @param isClick  是否是点击
             */
            @Override
            public void onMonthDateSelected(Calendar calendar, boolean isClick) {

                if (calendar.getYear() == mDelegate.getCurrentDay().getYear() &&
                        calendar.getMonth() == mDelegate.getCurrentDay().getMonth()
                        && mMonthPager.getCurrentItem() != mDelegate.mCurrentMonthViewItem) {
                    return;
                }
                mDelegate.mIndexCalendar = calendar;
                if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) {
                    mDelegate.mSelectedCalendar = calendar;
                }
                mWeekPager.updateSelected(mDelegate.mIndexCalendar, false);
                mMonthPager.updateSelected();
                if (mWeekBar != null &&
                        (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick)) {
                    mWeekBar.onDateSelected(calendar, mDelegate.getWeekStart(), isClick);
                }
            }


            @Override
            public void onWeekDateSelected(Calendar calendar, boolean isClick) {
                mDelegate.mIndexCalendar = calendar;
                if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                        || mDelegate.mIndexCalendar.equals(mDelegate.mSelectedCalendar)) {
                    mDelegate.mSelectedCalendar = calendar;
                }
                int y = calendar.getYear() - mDelegate.getMinYear();
                int position = 12 * y + mDelegate.mIndexCalendar.getMonth() - mDelegate.getMinYearMonth();
                mWeekPager.updateSingleSelect();
                mMonthPager.setCurrentItem(position, false);
                mMonthPager.updateSelected();
                if (mWeekBar != null &&
                        (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT
                                || isClick
                                || mDelegate.mIndexCalendar.equals(mDelegate.mSelectedCalendar))) {
                    mWeekBar.onDateSelected(calendar, mDelegate.getWeekStart(), isClick);
                }
            }
        };


        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (isInRange(mDelegate.getCurrentDay())) {
                mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();
            } else {
                mDelegate.mSelectedCalendar = mDelegate.getMinRangeCalendar();
            }
        } else {
            mDelegate.mSelectedCalendar = new Calendar();
        }

        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;

        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);

        mMonthPager.setup(mDelegate);
        mMonthPager.setCurrentItem(mDelegate.mCurrentMonthViewItem);
        mYearViewPager.setOnMonthSelectedListener(new YearRecyclerView.OnMonthSelectedListener() {
            @Override
            public void onMonthSelected(int year, int month) {
                int position = 12 * (year - mDelegate.getMinYear()) + month - mDelegate.getMinYearMonth();
                closeSelectLayout(position);
                mDelegate.isShowYearSelectedLayout = false;
            }
        });
        mYearViewPager.setup(mDelegate);
        mWeekPager.updateSelected(mDelegate.createCurrentDate(), false);
    }

    public void setRange(int minYear, int minYearMonth, int minYearDay,
                         int maxYear, int maxYearMonth, int maxYearDay) {
        if (CalendarUtil.compareTo(minYear, minYearMonth, minYearDay,
                maxYear, maxYearMonth, maxYearDay) > 0) {
            return;
        }
        mDelegate.setRange(minYear, minYearMonth, minYearDay,
                maxYear, maxYearMonth, maxYearDay);
        mWeekPager.notifyDataSetChanged();
        mYearViewPager.notifyDataSetChanged();
        mMonthPager.notifyDataSetChanged();
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            mDelegate.mSelectedCalendar = mDelegate.getMinRangeCalendar();
            mDelegate.updateSelectCalendarScheme();
            mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
        }
        mWeekPager.updateRange();
        mMonthPager.updateRange();
        mYearViewPager.updateRange();
    }

    public int getCurDay() {
        return mDelegate.getCurrentDay().getDay();
    }

    public int getCurMonth() {
        return mDelegate.getCurrentDay().getMonth();
    }

    public int getCurYear() {
        return mDelegate.getCurrentDay().getYear();
    }


    public void showYearSelectLayout(final int year) {
        showSelectLayout(year);
    }

    private void showSelectLayout(final int year) {
        if (mParentLayout != null && mParentLayout.mContentView != null) {
            if (!mParentLayout.isExpand()) {
                mParentLayout.expand();
                //return;
            }
        }
        mWeekPager.setVisibility(GONE);
        mDelegate.isShowYearSelectedLayout = true;
        if (mParentLayout != null) {
            mParentLayout.hideContentView();
        }
        mWeekBar.animate()
                .translationY(-mWeekBar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(260)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(GONE);
                        mYearViewPager.setVisibility(VISIBLE);
                        mYearViewPager.scrollToYear(year, false);
                        if (mParentLayout != null && mParentLayout.mContentView != null) {
                            mParentLayout.expand();
                        }
                    }
                });

        mMonthPager.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(260)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mDelegate.mYearViewChangeListener != null) {
                            mDelegate.mYearViewChangeListener.onYearViewChange(false);
                        }
                    }
                });
    }


    public boolean isYearSelectLayoutVisible() {
        return mYearViewPager.getVisibility() == VISIBLE;
    }

    public void closeYearSelectLayout() {
        if (mYearViewPager.getVisibility() == GONE) {
            return;
        }
        int position = 12 * (mDelegate.mSelectedCalendar.getYear() - mDelegate.getMinYear()) +
                mDelegate.mSelectedCalendar.getMonth() - mDelegate.getMinYearMonth();
        closeSelectLayout(position);
        mDelegate.isShowYearSelectedLayout = false;
    }

    private void closeSelectLayout(final int position) {
        mYearViewPager.setVisibility(GONE);
        mWeekBar.setVisibility(VISIBLE);
        if (position == mMonthPager.getCurrentItem()) {
            if (mDelegate.mCalendarSelectListener != null &&
                    mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_SINGLE) {
                mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
            }
        } else {
            mMonthPager.setCurrentItem(position, false);
        }
        mWeekBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(280)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(VISIBLE);
                    }
                });
        mMonthPager.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(180)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mDelegate.mYearViewChangeListener != null) {
                            mDelegate.mYearViewChangeListener.onYearViewChange(true);
                        }
                        if (mParentLayout != null) {
                            mParentLayout.showContentView();
                            if (mParentLayout.isExpand()) {
                                mMonthPager.setVisibility(VISIBLE);
                            } else {
                                mWeekPager.setVisibility(VISIBLE);
                                mParentLayout.shrink();
                            }
                        } else {
                            mMonthPager.setVisibility(VISIBLE);
                        }
                        mMonthPager.clearAnimation();
                    }
                });
    }

    public void scrollToCurrent() {
        scrollToCurrent(false);
    }

    public void scrollToCurrent(boolean smoothScroll) {
        if (!isInRange(mDelegate.getCurrentDay())) {
            return;
        }
        Calendar calendar = mDelegate.createCurrentDate();
        if (mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false);
            return;
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();
        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
        mDelegate.updateSelectCalendarScheme();
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
        if (mMonthPager.getVisibility() == VISIBLE) {
            mMonthPager.scrollToCurrent(smoothScroll);
            mWeekPager.updateSelected(mDelegate.mIndexCalendar, false);
        } else {
            mWeekPager.scrollToCurrent(smoothScroll);
        }
        mYearViewPager.scrollToYear(mDelegate.getCurrentDay().getYear(), smoothScroll);
    }


    public void scrollToNext() {
        scrollToNext(false);
    }

    public void scrollToNext(boolean smoothScroll) {
        if (isYearSelectLayoutVisible()) {
            mYearViewPager.setCurrentItem(mYearViewPager.getCurrentItem() + 1, smoothScroll);
        } else if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() + 1, smoothScroll);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() + 1, smoothScroll);
        }

    }

    public void scrollToPre() {
        scrollToPre(false);
    }

    public void scrollToPre(boolean smoothScroll) {
        if (isYearSelectLayoutVisible()) {
            mYearViewPager.setCurrentItem(mYearViewPager.getCurrentItem() - 1, smoothScroll);
        } else if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() - 1, smoothScroll);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() - 1, smoothScroll);
        }
    }

    public void scrollToSelectCalendar() {
        if (!mDelegate.mSelectedCalendar.isAvailable()) {
            return;
        }
        scrollToCalendar(mDelegate.mSelectedCalendar.getYear(),
                mDelegate.mSelectedCalendar.getMonth(),
                mDelegate.mSelectedCalendar.getDay(),
                false,
                true);
    }

    public void scrollToCalendar(int year, int month, int day) {
        scrollToCalendar(year, month, day, false, true);
    }

    public void scrollToCalendar(int year, int month, int day, boolean smoothScroll) {
        scrollToCalendar(year, month, day, smoothScroll, true);
    }

    public void scrollToCalendar(int year, int month, int day, boolean smoothScroll, boolean invokeListener) {

        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        if (!calendar.isAvailable()) {
            return;
        }
        if (!isInRange(calendar)) {
            return;
        }
        if (mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false);
            return;
        }

        if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener);
        } else {
            mMonthPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener);
        }
    }

    public void scrollToYear(int year) {
        scrollToYear(year, false);
    }

    public void scrollToYear(int year, boolean smoothScroll) {
        if (mYearViewPager.getVisibility() != VISIBLE) {
            return;
        }
        mYearViewPager.scrollToYear(year, smoothScroll);
    }

    public final void setMonthViewScrollable(boolean monthViewScrollable) {
        mDelegate.setMonthViewScrollable(monthViewScrollable);
    }


    public final void setWeekViewScrollable(boolean weekViewScrollable) {
        mDelegate.setWeekViewScrollable(weekViewScrollable);
    }

    public final void setYearViewScrollable(boolean yearViewScrollable) {
        mDelegate.setYearViewScrollable(yearViewScrollable);
    }


    public final void setDefaultMonthViewSelectDay() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.FIRST_DAY_OF_MONTH);
    }

    public final void setLastMonthViewSelectDay() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY);
    }

    public final void setLastMonthViewSelectDayIgnoreCurrent() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT);
    }

    public final void clearSelectRange() {
        mDelegate.clearSelectRange();
        mMonthPager.clearSelectRange();
        mWeekPager.clearSelectRange();
    }

    public final void clearSingleSelect() {
        mDelegate.mSelectedCalendar = new Calendar();
        mMonthPager.clearSingleSelect();
        mWeekPager.clearSingleSelect();
    }

    public final void clearMultiSelect() {
        mDelegate.mSelectedCalendars.clear();
        mMonthPager.clearMultiSelect();
        mWeekPager.clearMultiSelect();
    }

    public final void putMultiSelect(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) {
            return;
        }
        for (Calendar calendar : calendars) {
            if (calendar == null || mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                continue;
            }
            mDelegate.mSelectedCalendars.put(calendar.toString(), calendar);
        }
        update();
    }

    @SuppressWarnings("RedundantCollectionOperation")
    public final void removeMultiSelect(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) {
            return;
        }
        for (Calendar calendar : calendars) {
            if (calendar == null) {
                continue;
            }
            if (mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                mDelegate.mSelectedCalendars.remove(calendar.toString());
            }
        }
        update();
    }


    public final List<Calendar> getMultiSelectCalendars() {
        List<Calendar> calendars = new ArrayList<>();
        if (mDelegate.mSelectedCalendars.size() == 0) {
            return calendars;
        }
        calendars.addAll(mDelegate.mSelectedCalendars.values());
        Collections.sort(calendars);
        return calendars;
    }

    public final List<Calendar> getSelectCalendarRange() {
        return mDelegate.getSelectCalendarRange();
    }

    public final void setCalendarItemHeight(int calendarItemHeight) {
        if (mDelegate.getCalendarItemHeight() == calendarItemHeight) {
            return;
        }
        mDelegate.setCalendarItemHeight(calendarItemHeight);
        mMonthPager.updateItemHeight();
        mWeekPager.updateItemHeight();
        if (mParentLayout == null) {
            return;
        }
        mParentLayout.updateCalendarItemHeight();
    }


    public final void setMonthView(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getMonthViewClass().equals(cls)) {
            return;
        }
        mDelegate.setMonthViewClass(cls);
        mMonthPager.updateMonthViewClass();
    }

    public final void setWeekView(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getWeekBarClass().equals(cls)) {
            return;
        }
        mDelegate.setWeekViewClass(cls);
        mWeekPager.updateWeekViewClass();
    }

    public final void setWeekBar(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getWeekBarClass().equals(cls)) {
            return;
        }
        mDelegate.setWeekBarClass(cls);
        FrameLayout frameContent = findViewById(R.id.frameContent);
        frameContent.removeView(mWeekBar);

        try {
            Constructor constructor = cls.getConstructor(Context.class);
            mWeekBar = (WeekBar) constructor.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        frameContent.addView(mWeekBar, 2);
        mWeekBar.setup(mDelegate);
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
        this.mMonthPager.mWeekBar = mWeekBar;
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
    }


    public final void setOnCalendarInterceptListener(OnCalendarInterceptListener listener) {
        if (listener == null) {
            mDelegate.mCalendarInterceptListener = null;
        }
        if (listener == null || mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        mDelegate.mCalendarInterceptListener = listener;
        if (!listener.onCalendarIntercept(mDelegate.mSelectedCalendar)) {
            return;
        }
        mDelegate.mSelectedCalendar = new Calendar();
    }

    public void setOnYearChangeListener(OnYearChangeListener listener) {
        this.mDelegate.mYearChangeListener = listener;
    }

    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        this.mDelegate.mMonthChangeListener = listener;
    }


    public void setOnWeekChangeListener(OnWeekChangeListener listener) {
        this.mDelegate.mWeekChangeListener = listener;
    }

    public void setOnCalendarSelectListener(OnCalendarSelectListener listener) {
        this.mDelegate.mCalendarSelectListener = listener;
        if (mDelegate.mCalendarSelectListener == null) {
            return;
        }
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            return;
        }
        mDelegate.updateSelectCalendarScheme();
    }


    public final void setOnCalendarRangeSelectListener(OnCalendarRangeSelectListener listener) {
        this.mDelegate.mCalendarRangeSelectListener = listener;
    }

    public final void setOnCalendarMultiSelectListener(OnCalendarMultiSelectListener listener) {
        this.mDelegate.mCalendarMultiSelectListener = listener;
    }

    public final void setSelectRange(int minRange, int maxRange) {
        if (minRange > maxRange) {
            return;
        }
        mDelegate.setSelectRange(minRange, maxRange);
    }


    public final void setSelectStartCalendar(int startYear, int startMonth, int startDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        Calendar startCalendar = new Calendar();
        startCalendar.setYear(startYear);
        startCalendar.setMonth(startMonth);
        startCalendar.setDay(startDay);
        setSelectStartCalendar(startCalendar);
    }

    public final void setSelectStartCalendar(Calendar startCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (startCalendar == null) {
            return;
        }
        if (!isInRange(startCalendar)) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(startCalendar, true);
            }
            return;
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(startCalendar, false);
            }
            return;
        }
        mDelegate.mSelectedEndRangeCalendar = null;
        mDelegate.mSelectedStartRangeCalendar = startCalendar;
        scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
    }

    public final void setSelectEndCalendar(int endYear, int endMonth, int endDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return;
        }
        Calendar endCalendar = new Calendar();
        endCalendar.setYear(endYear);
        endCalendar.setMonth(endMonth);
        endCalendar.setDay(endDay);
        setSelectEndCalendar(endCalendar);
    }

    public final void setSelectEndCalendar(Calendar endCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return;
        }
        setSelectCalendarRange(mDelegate.mSelectedStartRangeCalendar, endCalendar);
    }

    public final void setSelectCalendarRange(int startYear, int startMonth, int startDay,
                                             int endYear, int endMonth, int endDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        Calendar startCalendar = new Calendar();
        startCalendar.setYear(startYear);
        startCalendar.setMonth(startMonth);
        startCalendar.setDay(startDay);

        Calendar endCalendar = new Calendar();
        endCalendar.setYear(endYear);
        endCalendar.setMonth(endMonth);
        endCalendar.setDay(endDay);
        setSelectCalendarRange(startCalendar, endCalendar);
    }

    public final void setSelectCalendarRange(Calendar startCalendar, Calendar endCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (startCalendar == null || endCalendar == null) {
            return;
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(startCalendar, false);
            }
            return;
        }
        if (onCalendarIntercept(endCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(endCalendar, false);
            }
            return;
        }
        int minDiffer = endCalendar.differ(startCalendar);
        if (minDiffer < 0) {
            return;
        }
        if (!isInRange(startCalendar) || !isInRange(endCalendar)) {
            return;
        }



        if (mDelegate.getMinSelectRange() != -1 && mDelegate.getMinSelectRange() > minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, true);
            }
            return;
        } else if (mDelegate.getMaxSelectRange() != -1 && mDelegate.getMaxSelectRange() <
                minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, false);
            }
            return;
        }
        if (mDelegate.getMinSelectRange() == -1 && minDiffer == 0) {
            mDelegate.mSelectedStartRangeCalendar = startCalendar;
            mDelegate.mSelectedEndRangeCalendar = null;
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(startCalendar, false);
            }
            scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
            return;
        }

        mDelegate.mSelectedStartRangeCalendar = startCalendar;
        mDelegate.mSelectedEndRangeCalendar = endCalendar;
        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(startCalendar, false);
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(endCalendar, true);
        }
        scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
    }

    protected final boolean onCalendarIntercept(Calendar calendar) {
        return mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar);
    }


    public final int getMaxMultiSelectSize() {
        return mDelegate.getMaxMultiSelectSize();
    }

    public final void setMaxMultiSelectSize(int maxMultiSelectSize) {
        mDelegate.setMaxMultiSelectSize(maxMultiSelectSize);
    }

    public final int getMinSelectRange() {
        return mDelegate.getMinSelectRange();
    }

    public final int getMaxSelectRange() {
        return mDelegate.getMaxSelectRange();
    }

    public void setOnCalendarLongClickListener(OnCalendarLongClickListener listener) {
        this.mDelegate.mCalendarLongClickListener = listener;
    }

    public void setOnCalendarLongClickListener(OnCalendarLongClickListener listener, boolean preventLongPressedSelect) {
        this.mDelegate.mCalendarLongClickListener = listener;
        this.mDelegate.setPreventLongPressedSelected(preventLongPressedSelect);
    }

    public void setOnViewChangeListener(OnViewChangeListener listener) {
        this.mDelegate.mViewChangeListener = listener;
    }


    public void setOnYearViewChangeListener(OnYearViewChangeListener listener) {
        this.mDelegate.mYearViewChangeListener = listener;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        if (mDelegate == null) {
            return super.onSaveInstanceState();
        }
        Bundle bundle = new Bundle();
        Parcelable parcelable = super.onSaveInstanceState();
        bundle.putParcelable("super", parcelable);
        bundle.putSerializable("selected_calendar", mDelegate.mSelectedCalendar);
        bundle.putSerializable("index_calendar", mDelegate.mIndexCalendar);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superData = bundle.getParcelable("super");
        mDelegate.mSelectedCalendar = (Calendar) bundle.getSerializable("selected_calendar");
        mDelegate.mIndexCalendar = (Calendar) bundle.getSerializable("index_calendar");
        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
        }
        if (mDelegate.mIndexCalendar != null) {
            scrollToCalendar(mDelegate.mIndexCalendar.getYear(),
                    mDelegate.mIndexCalendar.getMonth(),
                    mDelegate.mIndexCalendar.getDay());
        }
        update();
        super.onRestoreInstanceState(superData);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getParent() != null && getParent() instanceof CalendarLayout) {
            mParentLayout = (CalendarLayout) getParent();
            mMonthPager.mParentLayout = mParentLayout;
            mWeekPager.mParentLayout = mParentLayout;
            mParentLayout.mWeekBar = mWeekBar;
            mParentLayout.setup(mDelegate);
            mParentLayout.initStatus();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mDelegate == null ||
                !mDelegate.isFullScreenCalendar()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setCalendarItemHeight((height -
                mDelegate.getWeekBarHeight()) / 6);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setSchemeDate(Map<String, Calendar> mSchemeDates) {
        this.mDelegate.mSchemeDatesMap = mSchemeDates;
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    public final void clearSchemeDate() {
        this.mDelegate.mSchemeDatesMap = null;
        this.mDelegate.clearSelectedScheme();
        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    public final void addSchemeDate(Calendar calendar) {
        if (calendar == null || !calendar.isAvailable()) {
            return;
        }
        if (mDelegate.mSchemeDatesMap == null) {
            mDelegate.mSchemeDatesMap = new HashMap<>();
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString());
        mDelegate.mSchemeDatesMap.put(calendar.toString(), calendar);
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    public final void addSchemeDate(Map<String, Calendar> mSchemeDates) {
        if (this.mDelegate == null || mSchemeDates == null || mSchemeDates.size() == 0) {
            return;
        }
        if (this.mDelegate.mSchemeDatesMap == null) {
            this.mDelegate.mSchemeDatesMap = new HashMap<>();
        }
        this.mDelegate.addSchemes(mSchemeDates);
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    public final void removeSchemeDate(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {
            return;
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString());
        if (mDelegate.mSelectedCalendar.equals(calendar)) {
            mDelegate.clearSelectedScheme();
        }

        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    public void setBackground(int yearViewBackground, int weekBackground, int lineBg) {
        mWeekBar.setBackgroundColor(weekBackground);
        mYearViewPager.setBackgroundColor(yearViewBackground);
        mWeekLine.setBackgroundColor(lineBg);
    }


    public void setTextColor(
            int currentDayTextColor,
            int curMonthTextColor,
            int otherMonthColor,
            int curMonthLunarTextColor,
            int otherMonthLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setTextColor(currentDayTextColor, curMonthTextColor,
                otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    public void setSelectedColor(int selectedThemeColor, int selectedTextColor, int selectedLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    public void setThemeColor(int selectedThemeColor, int schemeColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setThemeColor(selectedThemeColor, schemeColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    public void setSchemeColor(int schemeColor, int schemeTextColor, int schemeLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    public void setYearViewTextColor(int yearViewMonthTextColor, int yearViewDayTextColor, int yarViewSchemeTextColor) {
        if (mDelegate == null || mYearViewPager == null) {
            return;
        }
        mDelegate.setYearViewTextColor(yearViewMonthTextColor, yearViewDayTextColor, yarViewSchemeTextColor);
        mYearViewPager.updateStyle();
    }

    public void setWeeColor(int weekBackground, int weekTextColor) {
        if (mWeekBar == null) {
            return;
        }
        mWeekBar.setBackgroundColor(weekBackground);
        mWeekBar.setTextColor(weekTextColor);
    }

    public final void setSelectDefaultMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        mDelegate.mSelectedCalendar = mDelegate.mIndexCalendar;
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_DEFAULT);
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
        mMonthPager.updateDefaultSelect();
        mWeekPager.updateDefaultSelect();

    }

    public void setSelectRangeMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_RANGE);
        clearSelectRange();
    }

    public void setSelectMultiMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_MULTI) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_MULTI);
        clearMultiSelect();
    }

    public void setSelectSingleMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_SINGLE);
        mWeekPager.updateSelected();
        mMonthPager.updateSelected();
    }

    public void setWeekStarWithSun() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SUN);
    }

    public void setWeekStarWithMon() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_MON);
    }

    public void setWeekStarWithSat() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SAT);
    }

    private void setWeekStart(int weekStart) {
        if (weekStart != CalendarViewDelegate.WEEK_START_WITH_SUN &&
                weekStart != CalendarViewDelegate.WEEK_START_WITH_MON &&
                weekStart != CalendarViewDelegate.WEEK_START_WITH_SAT)
            return;
        if (weekStart == mDelegate.getWeekStart())
            return;
        mDelegate.setWeekStart(weekStart);
        mWeekBar.onWeekStartChange(weekStart);
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, weekStart, false);
        mWeekPager.updateWeekStart();
        mMonthPager.updateWeekStart();
        mYearViewPager.updateWeekStart();
    }

    public boolean isSingleSelectMode() {
        return mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE;
    }

    public void setAllMode() {
        setShowMode(CalendarViewDelegate.MODE_ALL_MONTH);
    }

    public void setOnlyCurrentMode() {
        setShowMode(CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH);
    }

    public void setFixMode() {
        setShowMode(CalendarViewDelegate.MODE_FIT_MONTH);
    }

    private void setShowMode(int mode) {
        if (mode != CalendarViewDelegate.MODE_ALL_MONTH &&
                mode != CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH &&
                mode != CalendarViewDelegate.MODE_FIT_MONTH)
            return;
        if (mDelegate.getMonthViewShowMode() == mode)
            return;
        mDelegate.setMonthViewShowMode(mode);
        mWeekPager.updateShowMode();
        mMonthPager.updateShowMode();
        mWeekPager.notifyDataSetChanged();
    }

    public final void update() {
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    public void updateWeekBar() {
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
    }


    public final void updateCurrentDate() {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        if (getCurDay() == day) {
            return;
        }
        mDelegate.updateCurrentDay();
        mMonthPager.updateCurrentDate();
        mWeekPager.updateCurrentDate();
    }

    public List<Calendar> getCurrentWeekCalendars() {
        return mWeekPager.getCurrentWeekCalendars();
    }


    public List<Calendar> getCurrentMonthCalendars() {
        return mMonthPager.getCurrentMonthCalendars();
    }

    public Calendar getSelectedCalendar() {
        return mDelegate.mSelectedCalendar;
    }

    public Calendar getMinRangeCalendar() {
        return mDelegate.getMinRangeCalendar();
    }


    public Calendar getMaxRangeCalendar() {
        return mDelegate.getMaxRangeCalendar();
    }

    public MonthViewPager getMonthViewPager() {
        return mMonthPager;
    }

    public WeekViewPager getWeekViewPager() {
        return mWeekPager;
    }

    protected final boolean isInRange(Calendar calendar) {
        return mDelegate != null && CalendarUtil.isCalendarInRange(calendar, mDelegate);
    }


    public interface OnYearChangeListener {
        void onYearChange(int year);
    }

    public interface OnMonthChangeListener {
        void onMonthChange(int year, int month);
    }


    public interface OnWeekChangeListener {
        void onWeekChange(List<Calendar> weekCalendars);
    }

    interface OnInnerDateSelectedListener {
        void onMonthDateSelected(Calendar calendar, boolean isClick);

        void onWeekDateSelected(Calendar calendar, boolean isClick);
    }


    public interface OnCalendarRangeSelectListener {

        void onCalendarSelectOutOfRange(Calendar calendar);

        void onSelectOutOfRange(Calendar calendar, boolean isOutOfMinRange);

        void onCalendarRangeSelect(Calendar calendar, boolean isEnd);
    }


    public interface OnCalendarMultiSelectListener {

        void onCalendarMultiSelectOutOfRange(Calendar calendar);

        void onMultiSelectOutOfSize(Calendar calendar, int maxSize);

        void onCalendarMultiSelect(Calendar calendar, int curSize, int maxSize);
    }

    public interface OnCalendarSelectListener {

        void onCalendarOutOfRange(Calendar calendar);

        void onCalendarSelect(Calendar calendar, boolean isClick);
    }

    public interface OnCalendarLongClickListener {

        void onCalendarLongClickOutOfRange(Calendar calendar);

        void onCalendarLongClick(Calendar calendar);
    }

    public interface OnViewChangeListener {
        void onViewChange(boolean isMonthView);
    }

    public interface OnYearViewChangeListener {
        void onYearViewChange(boolean isClose);
    }

    public interface OnCalendarInterceptListener {
        boolean onCalendarIntercept(Calendar calendar);

        void onCalendarInterceptClick(Calendar calendar, boolean isClick);
    }
}
