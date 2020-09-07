package com.period.calendar.colorful;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.period.calendarview.Calendar;
import com.period.calendarview.WeekView;


public class ColorfulWeekView extends WeekView {

    private int mRadius;

    private Paint mDashPaint = new Paint();

    private Paint mLinePaint = new Paint();
    private int mPadding;
    private float mCircleRadius;

    private float mInnerCircleRadius;

    private Paint mSchemeBasicPaint = new Paint();
    private float mSchemeBaseLine;
    private Paint mTextPaint = new Paint();

    public ColorfulWeekView(Context context) {
        super(context);


        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);

       // DashPathEffect dashPath = new DashPathEffect(new float[]{12, 12}, (float) 1.0);
        mSchemePaint.setColor(Color.RED);
       // mSchemePaint.setPathEffect(dashPath);
        mSchemePaint.setStyle(Paint.Style.STROKE);

        mLinePaint.setColor(Color.GREEN);
       // mLinePaint.setPathEffect(dashPath);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mPadding = dipToPx(getContext(), 3);

        mCircleRadius = dipToPx(getContext(), 9);

        mInnerCircleRadius =  dipToPx(getContext(), 18);

        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mCircleRadius - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);
        mSchemeBasicPaint.setColor(Color.RED);

    }

    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 11 * 5;


    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        int cx = x + mItemWidth / 2;
        int cy = mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        int cx = x + mItemWidth / 2;
        int cy = mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSchemePaint);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = -mItemHeight / 8;
        int cy = mItemHeight / 2;

        if (isSelected) {

            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine,
                    calendar.isCurrentDay() ? mCurDayTextPaint : mSelectTextPaint);

            canvas.drawLine(0, (float) (canvas.getHeight() / 1.25), canvas.getWidth(), (float) (canvas.getHeight() / 1.25), mSchemePaint);

            canvas.drawCircle(x + mCircleRadius * 2, mPadding + mCircleRadius, mCircleRadius, mSchemeBasicPaint);

            mTextPaint.setColor(Color.WHITE);

            canvas.drawText(String.valueOf(calendar.getDay()), x + mCircleRadius * 2 - mPadding, mPadding + mSchemeBaseLine, mTextPaint);


            canvas.drawCircle(cx , cy , mInnerCircleRadius, mSchemePaint);

//            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10,
//                    calendar.isCurrentDay() ? mCurDayLunarTextPaint : mSelectedLunarTextPaint);


        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() ? mSchemeTextPaint : mSchemeTextPaint);

//            canvas.drawCircle(cx, cy, mRadius, mSchemePaint);

//            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10, mSchemeLunarTextPaint);
            canvas.drawLine(0, (float) (canvas.getHeight() / 1.25), canvas.getWidth(), (float) (canvas.getHeight() / 1.25), mSchemePaint);


            //  canvas.drawCircle(x + mItemWidth - mPadding - mCircleRadius / 2, mPadding + mCircleRadius, mCircleRadius, mSchemeBasicPaint);

            //   mTextPaint.setColor(calendar.getSchemeColor());

            //    canvas.drawText(String.valueOf(calendar.getDay()), x + mItemWidth - mPadding - mCircleRadius, mPadding + mSchemeBaseLine, mTextPaint);


        } else {

            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthTextPaint : mCurMonthTextPaint);


            canvas.drawCircle(cx , cy, mInnerCircleRadius, mSchemePaint);
//            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10,
//                    calendar.isCurrentDay() ? mCurDayLunarTextPaint : mCurMonthLunarTextPaint);
            //    canvas.drawLine(0, (float) (canvas.getHeight() / 1.25), canvas.getWidth(), (float) (canvas.getHeight() / 1.25), mSchemePaint);


            canvas.drawCircle(x + mCircleRadius * 2, mPadding + mCircleRadius, mCircleRadius, mSchemeBasicPaint);

            mTextPaint.setColor(Color.WHITE);

            canvas.drawText(String.valueOf(calendar.getDay()), x + mCircleRadius * 2 - mPadding, mPadding + mSchemeBaseLine, mTextPaint);


        }
    }
}
