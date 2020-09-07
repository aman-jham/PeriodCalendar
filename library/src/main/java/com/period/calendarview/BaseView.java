
package com.period.calendarview;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public abstract class BaseView extends View implements View.OnClickListener, View.OnLongClickListener {

    CalendarViewDelegate mDelegate;

    protected Paint mCurMonthTextPaint = new Paint();

    protected Paint mOtherMonthTextPaint = new Paint();

//    protected Paint mCurMonthLunarTextPaint = new Paint();

//    protected Paint mSelectedLunarTextPaint = new Paint();

//    protected Paint mOtherMonthLunarTextPaint = new Paint();

//    protected Paint mSchemeLunarTextPaint = new Paint();

    protected Paint mSchemePaint = new Paint();

    protected Paint mSelectedPaint = new Paint();

    protected Paint mSchemeTextPaint = new Paint();

    protected Paint mSelectTextPaint = new Paint();

    protected Paint mCurDayTextPaint = new Paint();

   // protected Paint mCurDayLunarTextPaint = new Paint();

    CalendarLayout mParentLayout;

    List<Calendar> mItems;

    protected int mItemHeight;

    protected int mItemWidth;

    protected float mTextBaseLine;

    float mX, mY;

    boolean isClick = true;

    static final int TEXT_SIZE = 14;

    int mCurrentItem = -1;

    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    private void initPaint(Context context) {
        mCurMonthTextPaint.setAntiAlias(true);
        mCurMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurMonthTextPaint.setColor(0xFF111111);
        mCurMonthTextPaint.setFakeBoldText(true);
        mCurMonthTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mOtherMonthTextPaint.setAntiAlias(true);
        mOtherMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mOtherMonthTextPaint.setColor(0xFFe1e1e1);
        mOtherMonthTextPaint.setFakeBoldText(true);
        mOtherMonthTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

      //  mCurMonthLunarTextPaint.setAntiAlias(true);
      //  mCurMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

      //  mSelectedLunarTextPaint.setAntiAlias(true);
      //  mSelectedLunarTextPaint.setTextAlign(Paint.Align.CENTER);

      //  mOtherMonthLunarTextPaint.setAntiAlias(true);
      //  mOtherMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);


      //  mSchemeLunarTextPaint.setAntiAlias(true);
     //   mSchemeLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeTextPaint.setAntiAlias(true);
        mSchemeTextPaint.setStyle(Paint.Style.FILL);
        mSchemeTextPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeTextPaint.setColor(0xffed5353);
        mSchemeTextPaint.setFakeBoldText(true);
        mSchemeTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSelectTextPaint.setAntiAlias(true);
        mSelectTextPaint.setStyle(Paint.Style.FILL);
        mSelectTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectTextPaint.setColor(0xffed5353);
        mSelectTextPaint.setFakeBoldText(true);
        mSelectTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSchemePaint.setAntiAlias(true);
        mSchemePaint.setStyle(Paint.Style.FILL);
        mSchemePaint.setStrokeWidth(2);
        mSchemePaint.setColor(0xffefefef);

        mCurDayTextPaint.setAntiAlias(true);
        mCurDayTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayTextPaint.setColor(Color.RED);
        mCurDayTextPaint.setFakeBoldText(true);
        mCurDayTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

       // mCurDayLunarTextPaint.setAntiAlias(true);
       // mCurDayLunarTextPaint.setTextAlign(Paint.Align.CENTER);
       // mCurDayLunarTextPaint.setColor(Color.RED);
       // mCurDayLunarTextPaint.setFakeBoldText(true);
       // mCurDayLunarTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setStrokeWidth(2);

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    final void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        updateStyle();
        updateItemHeight();

        initPaint();
    }


    final void updateStyle(){
        if(mDelegate == null){
            return;
        }
        this.mCurDayTextPaint.setColor(mDelegate.getCurDayTextColor());
       // this.mCurDayLunarTextPaint.setColor(mDelegate.getCurDayLunarTextColor());
        this.mCurMonthTextPaint.setColor(mDelegate.getCurrentMonthTextColor());
        this.mOtherMonthTextPaint.setColor(mDelegate.getOtherMonthTextColor());
       // this.mCurMonthLunarTextPaint.setColor(mDelegate.getCurrentMonthLunarTextColor());
       // this.mSelectedLunarTextPaint.setColor(mDelegate.getSelectedLunarTextColor());
        this.mSelectTextPaint.setColor(mDelegate.getSelectedTextColor());
      //  this.mOtherMonthLunarTextPaint.setColor(mDelegate.getOtherMonthLunarTextColor());
       // this.mSchemeLunarTextPaint.setColor(mDelegate.getSchemeLunarTextColor());
        this.mSchemePaint.setColor(mDelegate.getSchemeThemeColor());
        this.mSchemeTextPaint.setColor(mDelegate.getSchemeTextColor());
        this.mCurMonthTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mOtherMonthTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mCurDayTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mSchemeTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mSelectTextPaint.setTextSize(mDelegate.getDayTextSize());

      //  this.mCurMonthLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
      //  this.mSelectedLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
       // this.mCurDayLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
      //  this.mOtherMonthLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
       // this.mSchemeLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());

        this.mSelectedPaint.setStyle(Paint.Style.FILL);
        this.mSelectedPaint.setColor(mDelegate.getSelectedThemeColor());
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    void updateItemHeight() {
        this.mItemHeight = mDelegate.getCalendarItemHeight();
        Paint.FontMetrics metrics = mCurMonthTextPaint.getFontMetrics();
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2;
    }


    final void removeSchemes() {
        for (Calendar a : mItems) {
            a.setScheme("");
            a.setSchemeColor(0);
            a.setSchemes(null);
        }
    }

    final void addSchemesFromMap() {
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
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX = event.getX();
                mY = event.getY();
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float mDY;
                if (isClick) {
                    mDY = event.getY() - mY;
                    isClick = Math.abs(mDY) <= 50;
                }
                break;
            case MotionEvent.ACTION_UP:
                mX = event.getX();
                mY = event.getY();
                break;
        }
        return super.onTouchEvent(event);
    }


    protected void onPreviewHook() {
        // TODO: 2017/11/16
    }

    protected boolean isSelected(Calendar calendar) {
        return mItems != null && mItems.indexOf(calendar) == mCurrentItem;
    }

    final void update() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {//清空操作
            removeSchemes();
            invalidate();
            return;
        }
        addSchemesFromMap();
        invalidate();
    }


    protected final boolean onCalendarIntercept(Calendar calendar) {
        return mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar);
    }

    protected final boolean isInRange(Calendar calendar) {
        return mDelegate != null && CalendarUtil.isCalendarInRange(calendar, mDelegate);
    }

    abstract void updateCurrentDate();

    protected abstract void onDestroy();

    protected void initPaint() {

    }
}
