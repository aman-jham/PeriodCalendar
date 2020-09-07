
package com.period.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public abstract class YearView extends View {

    CalendarViewDelegate mDelegate;

    protected Paint mCurMonthTextPaint = new Paint();

    protected Paint mOtherMonthTextPaint = new Paint();

    protected Paint mCurMonthLunarTextPaint = new Paint();

    protected Paint mSelectedLunarTextPaint = new Paint();

    protected Paint mOtherMonthLunarTextPaint = new Paint();

    protected Paint mSchemeLunarTextPaint = new Paint();

    protected Paint mSchemePaint = new Paint();

    protected Paint mSelectedPaint = new Paint();

    protected Paint mSchemeTextPaint = new Paint();

    protected Paint mSelectTextPaint = new Paint();

    protected Paint mCurDayTextPaint = new Paint();

    protected Paint mCurDayLunarTextPaint = new Paint();

    protected Paint mMonthTextPaint = new Paint();

    protected Paint mWeekTextPaint = new Paint();

    List<Calendar> mItems;

    protected int mItemHeight;

    protected int mItemWidth;

    protected float mTextBaseLine;

    protected float mMonthTextBaseLine;

    protected float mWeekTextBaseLine;

    protected int mYear;

    protected int mMonth;

    protected int mNextDiff;

    protected int mWeekStart;

    protected int mLineCount;

    public YearView(Context context) {
        this(context, null);
    }

    public YearView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }


    private void initPaint() {
        mCurMonthTextPaint.setAntiAlias(true);
        mCurMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurMonthTextPaint.setColor(0xFF111111);
        mCurMonthTextPaint.setFakeBoldText(true);

        mOtherMonthTextPaint.setAntiAlias(true);
        mOtherMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mOtherMonthTextPaint.setColor(0xFFe1e1e1);
        mOtherMonthTextPaint.setFakeBoldText(true);

        mCurMonthLunarTextPaint.setAntiAlias(true);
        mCurMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSelectedLunarTextPaint.setAntiAlias(true);
        mSelectedLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mOtherMonthLunarTextPaint.setAntiAlias(true);
        mOtherMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mMonthTextPaint.setAntiAlias(true);
        mMonthTextPaint.setFakeBoldText(true);

        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setFakeBoldText(true);
        mWeekTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeLunarTextPaint.setAntiAlias(true);
        mSchemeLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeTextPaint.setAntiAlias(true);
        mSchemeTextPaint.setStyle(Paint.Style.FILL);
        mSchemeTextPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeTextPaint.setColor(0xffed5353);
        mSchemeTextPaint.setFakeBoldText(true);

        mSelectTextPaint.setAntiAlias(true);
        mSelectTextPaint.setStyle(Paint.Style.FILL);
        mSelectTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectTextPaint.setColor(0xffed5353);
        mSelectTextPaint.setFakeBoldText(true);

        mSchemePaint.setAntiAlias(true);
        mSchemePaint.setStyle(Paint.Style.FILL);
        mSchemePaint.setStrokeWidth(2);
        mSchemePaint.setColor(0xffefefef);

        mCurDayTextPaint.setAntiAlias(true);
        mCurDayTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayTextPaint.setColor(Color.RED);
        mCurDayTextPaint.setFakeBoldText(true);

        mCurDayLunarTextPaint.setAntiAlias(true);
        mCurDayLunarTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayLunarTextPaint.setColor(Color.RED);
        mCurDayLunarTextPaint.setFakeBoldText(true);

        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setStrokeWidth(2);
    }

    final void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        updateStyle();
    }

    final void updateStyle(){
        if(mDelegate == null){
            return;
        }
        this.mCurMonthTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mSchemeTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mOtherMonthTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mCurDayTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mSelectTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());

        this.mSchemeTextPaint.setColor(mDelegate.getYearViewSchemeTextColor());
        this.mCurMonthTextPaint.setColor(mDelegate.getYearViewDayTextColor());
        this.mOtherMonthTextPaint.setColor(mDelegate.getYearViewDayTextColor());
        this.mCurDayTextPaint.setColor(mDelegate.getYearViewCurDayTextColor());
        this.mSelectTextPaint.setColor(mDelegate.getYearViewSelectTextColor());
        this.mMonthTextPaint.setTextSize(mDelegate.getYearViewMonthTextSize());
        this.mMonthTextPaint.setColor(mDelegate.getYearViewMonthTextColor());
        this.mWeekTextPaint.setColor(mDelegate.getYearViewWeekTextColor());
        this.mWeekTextPaint.setTextSize(mDelegate.getYearViewWeekTextSize());
    }

    final void init(int year, int month) {
        mYear = year;
        mMonth = month;
        mNextDiff = CalendarUtil.getMonthEndDiff(mYear, mMonth, mDelegate.getWeekStart());
        int preDiff = CalendarUtil.getMonthViewStartDiff(mYear, mMonth, mDelegate.getWeekStart());

        mItems = CalendarUtil.initCalendarForMonthView(mYear, mMonth, mDelegate.getCurrentDay(), mDelegate.getWeekStart());

        mLineCount = 6;
        addSchemesFromMap();

    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    final void measureSize(int width, int height) {

        Rect rect = new Rect();
        mCurMonthTextPaint.getTextBounds("1", 0, 1, rect);
        int textHeight = rect.height();
        int mMinHeight = 12 * textHeight + getMonthViewTop();

        int h = height >= mMinHeight ? height : mMinHeight;

        getLayoutParams().width = width;
        getLayoutParams().height = h;
        mItemHeight = (h - getMonthViewTop()) / 6;

        Paint.FontMetrics metrics = mCurMonthTextPaint.getFontMetrics();
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2;

        Paint.FontMetrics monthMetrics = mMonthTextPaint.getFontMetrics();
        mMonthTextBaseLine = mDelegate.getYearViewMonthHeight() / 2 - monthMetrics.descent +
                (monthMetrics.bottom - monthMetrics.top) / 2;

        Paint.FontMetrics weekMetrics = mWeekTextPaint.getFontMetrics();
        mWeekTextBaseLine = mDelegate.getYearViewWeekHeight() / 2 - weekMetrics.descent +
                (weekMetrics.bottom - weekMetrics.top) / 2;

        invalidate();
    }

    private void addSchemesFromMap() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {
            return;
        }
        for (Calendar a : mItems) {
            if (mDelegate.mSchemeDatesMap.containsKey(a.toString())) {
                Calendar d = mDelegate.mSchemeDatesMap.get(a.toString());
                if(d == null){
                    continue;
                }
                a.setScheme(TextUtils.isEmpty(d.getScheme()) ? mDelegate.getSchemeText() : d.getScheme());
                a.setSchemeColor(d.getSchemeColor());
                a.setSchemes(d.getSchemes());
            } else {
                a.setScheme("");
                a.setSchemeColor(0);
                a.setSchemes(null);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mItemWidth = (getWidth() - 2 * mDelegate.getYearViewPadding()) / 7;
        onPreviewHook();
        onDrawMonth(canvas);
        onDrawWeek(canvas);
        onDrawMonthView(canvas);
    }

    private void onDrawMonth(Canvas canvas) {
        onDrawMonth(canvas,
                mYear, mMonth,
                mDelegate.getYearViewPadding(),
                mDelegate.getYearViewMonthMarginTop(),
                getWidth() - 2 * mDelegate.getYearViewPadding(),
                mDelegate.getYearViewMonthHeight() +
                        mDelegate.getYearViewMonthMarginTop());
    }

    private int getMonthViewTop() {
        return mDelegate.getYearViewMonthMarginTop() +
                mDelegate.getYearViewMonthHeight() +
                mDelegate.getYearViewMonthMarginBottom() +
                mDelegate.getYearViewWeekHeight();
    }

    private void onDrawWeek(Canvas canvas) {
        if (mDelegate.getYearViewWeekHeight() <= 0) {
            return;
        }
        int week = mDelegate.getWeekStart();
        if (week > 0) {
            week -= 1;
        }
        int width = (getWidth() - 2 * mDelegate.getYearViewPadding()) / 7;
        for (int i = 0; i < 7; i++) {
            onDrawWeek(canvas,
                    week,
                    mDelegate.getYearViewPadding() + i * width,
                    mDelegate.getYearViewMonthHeight() +
                            mDelegate.getYearViewMonthMarginTop() +
                            mDelegate.getYearViewMonthMarginBottom(),
                    width,
                    mDelegate.getYearViewWeekHeight());
            week += 1;
            if (week >= 7) {
                week = 0;
            }

        }
    }

    private void onDrawMonthView(Canvas canvas) {

        int count = mLineCount * 7;
        int d = 0;
        for (int i = 0; i < mLineCount; i++) {
            for (int j = 0; j < 7; j++) {
                Calendar calendar = mItems.get(d);
                if (d > mItems.size() - mNextDiff) {
                    return;
                }
                if (!calendar.isCurrentMonth()) {
                    ++d;
                    continue;
                }
                draw(canvas, calendar, i, j, d);
                ++d;
            }
        }
    }


    private void draw(Canvas canvas, Calendar calendar, int i, int j, int d) {
        int x = j * mItemWidth + mDelegate.getYearViewPadding();
        int y = i * mItemHeight + getMonthViewTop();

        boolean isSelected = calendar.equals(mDelegate.mSelectedCalendar);
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

    protected void onPreviewHook() {
        // TODO: 2017/11/16
    }


    protected abstract void onDrawMonth(Canvas canvas, int year, int month, int x, int y, int width, int height);


    protected abstract void onDrawWeek(Canvas canvas, int week, int x, int y, int width, int height);


    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme);

    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y);


    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected);
}
