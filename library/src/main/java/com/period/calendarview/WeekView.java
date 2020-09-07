package com.period.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public abstract class WeekView extends BaseWeekView {

    public WeekView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mItems.size() == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();

        for (int i = 0; i < mItems.size(); i++) {
            int x = i * mItemWidth + mDelegate.getCalendarPadding();
            onLoopStart(x);
            Calendar calendar = mItems.get(i);
            boolean isSelected = i == mCurrentItem;
            boolean hasScheme = calendar.hasScheme();
            if (hasScheme) {
                boolean isDrawSelected = false;//是否继续绘制选中的onDrawScheme
                if (isSelected) {
                    isDrawSelected = onDrawSelected(canvas, calendar, x, true);
                }
                if (isDrawSelected || !isSelected) {
                    //将画笔设置为标记颜色
                    mSchemePaint.setColor(calendar.getSchemeColor() != 0 ?
                            calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                    onDrawScheme(canvas, calendar, x);
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false);
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected);
        }
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
            if (mDelegate.mCalendarSelectListener != null) {
                mDelegate.mCalendarSelectListener.onCalendarOutOfRange(calendar);
            }
            return;
        }

        mCurrentItem = mItems.indexOf(calendar);

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, true);
        }
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, true);
        }

        invalidate();
    }


    @Override
    public boolean onLongClick(View v) {
        if (mDelegate.mCalendarLongClickListener == null)
            return false;
        if (!isClick) {
            return false;
        }
        Calendar calendar = getIndex();
        if (calendar == null) {
            return false;
        }
        if (onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, true);
            return true;
        }
        boolean isCalendarInRange = isInRange(calendar);

        if (!isCalendarInRange) {
            if (mDelegate.mCalendarLongClickListener != null) {
                mDelegate.mCalendarLongClickListener.onCalendarLongClickOutOfRange(calendar);
            }
            return true;
        }

        if (mDelegate.isPreventLongPressedSelected()) {//如果启用拦截长按事件不选择日期
            if (mDelegate.mCalendarLongClickListener != null) {
                mDelegate.mCalendarLongClickListener.onCalendarLongClick(calendar);
            }
            return true;
        }


        mCurrentItem = mItems.indexOf(calendar);

        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, true);
        }
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, true);
        }

        if (mDelegate.mCalendarLongClickListener != null) {
            mDelegate.mCalendarLongClickListener.onCalendarLongClick(calendar);
        }

        invalidate();
        return true;
    }

    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme);

    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x);


    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected);
}
