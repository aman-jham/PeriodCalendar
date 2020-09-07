/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.period.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public abstract class MonthView extends BaseMonthView {

    public MonthView(Context context) {
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
                draw(canvas, calendar, i, j, d);
                ++d;
            }
        }
    }


    private void draw(Canvas canvas, Calendar calendar, int i, int j, int d) {
        int x = j * mItemWidth + mDelegate.getCalendarPadding();
        int y = i * mItemHeight;
        onLoopStart(x, y);
        boolean isSelected = d == mCurrentItem;
        boolean hasScheme = calendar.hasScheme();

        if (hasScheme) {
            //标记的日子
            boolean isDrawSelected = false;//是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true);
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                onDrawScheme(canvas, calendar, x, y);
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false);
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected);
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

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH &&
                !calendar.isCurrentMonth()) {
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

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, true);
        }
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

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH &&
                !calendar.isCurrentMonth()) {
            return false;
        }


        if (onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, true);
            return false;
        }

        boolean isCalendarInRange = isInRange(calendar);

        if (!isCalendarInRange) {
            if (mDelegate.mCalendarLongClickListener != null) {
                mDelegate.mCalendarLongClickListener.onCalendarLongClickOutOfRange(calendar);
            }
            return true;
        }

        if (mDelegate.isPreventLongPressedSelected()) {
            if (mDelegate.mCalendarLongClickListener != null) {
                mDelegate.mCalendarLongClickListener.onCalendarLongClick(calendar);
            }
            return true;
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

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, true);
        }

        if (mDelegate.mCalendarLongClickListener != null) {
            mDelegate.mCalendarLongClickListener.onCalendarLongClick(calendar);
        }
        invalidate();
        return true;
    }

    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme);

    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y);


    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected);
}
