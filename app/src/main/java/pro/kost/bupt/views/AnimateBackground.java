package pro.kost.bupt.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import pro.kost.bupt.R;

/**
 * Created by kost on 14/11/22.
 */
public class AnimateBackground extends LinearLayout {
    public enum AnimState {
        INIT, OPENNING, OPENED, CLOSING
    }

    ;
    private float DIVID = 4f;
    private float INTERVAL = 0.05f;
    private float LOGO_BG_PORTION = 0.125F;
    private float LOGO_RADIUS_PORTION = 0.20f;
    private float LOADING_RADIUS_PORTION = 0.225f;
    private int INVALIDATE = 1;
    private AnimState mState = AnimState.INIT;
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();
    private Path mBottomBackPath = null;
    private Path mAllPath = null;
    private Paint mBottomPaint = new Paint();
    private Paint mCircleBackPaint = new Paint();
    private float frac = 0;
    private Bitmap logo = null;

    public AnimateBackground(Context context) {
        super(context);
        init();
    }

    public AnimateBackground(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public AnimState getState() {
        return mState;
    }

    public void open() {
        mState = AnimState.OPENNING;
        onStateChanged(AnimState.OPENNING);
        postInvalidate();
    }

    public void close() {
        mState = AnimState.CLOSING;
        onStateChanged(AnimState.CLOSING);
        postInvalidate();
    }

    public void toggle() {
        if (mState == AnimState.INIT || mState == AnimState.CLOSING)
            open();
        else if (mState == AnimState.OPENED || mState == AnimState.OPENNING) {
            close();
        }
    }

    private void init() {
        this.setBackgroundColor(Color.TRANSPARENT);
        mBottomPaint.setColor(Color.parseColor("#e6ffffff"));
        mBottomPaint.setStyle(Paint.Style.FILL);
        mBottomPaint.setStrokeWidth(50f);
        mBottomPaint.setAntiAlias(true);

        mCircleBackPaint.setColor(Color.parseColor("#e6ffffff"));
        mCircleBackPaint.setStyle(Paint.Style.FILL);
        mCircleBackPaint.setStrokeWidth(50f);
        mCircleBackPaint.setAntiAlias(true);

        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void drawProgress(Canvas canvas, AnimState state) {
        if (state == AnimState.OPENNING) {
            frac = frac + INTERVAL;
            if (frac >= 1) {
                frac = 1f;
                mState = AnimState.OPENED;
            }
        } else {
            frac = frac - INTERVAL;
            if (frac <= 0) {
                frac = 0f;
                mState = AnimState.INIT;
            }
        }
        float value = interpolator.getInterpolation(frac);

        if (mState == AnimState.CLOSING || mState == AnimState.OPENNING) {
            mBottomPaint.setAlpha((int) (255 * (1 - value)));
            canvas.drawPath(getBottomPath(0, canvas.getHeight(), canvas.getWidth()), mBottomPaint);
            float circleRadius = canvas.getWidth() * LOGO_BG_PORTION + (canvas.getHeight() * value - canvas.getWidth() * LOGO_BG_PORTION) * value;
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() * 3 / (2 * DIVID), circleRadius, mCircleBackPaint);
            float logoHeight = canvas.getHeight() * 3 / (2 * DIVID) - getLogo(canvas).getHeight() / 2 - value * canvas.getHeight() / 4f;
            canvas.drawBitmap(getLogo(canvas), canvas.getWidth() / 2 - getLogo(canvas).getWidth() / 2, logoHeight, mCircleBackPaint);
            this.postInvalidateDelayed(INVALIDATE);
        }
        if (mState == AnimState.INIT) {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() * 3 / (2 * DIVID), canvas.getWidth() * LOGO_BG_PORTION, mBottomPaint);
            canvas.drawPath(getBottomPath(0, canvas.getHeight(), canvas.getWidth()), mBottomPaint);
            canvas.drawBitmap(getLogo(canvas), canvas.getWidth() / 2 - getLogo(canvas).getWidth() / 2,
                    canvas.getHeight() * 3 / (2 * DIVID) - getLogo(canvas).getHeight() / 2, mBottomPaint);
            onStateChanged(AnimState.INIT);
        }
        if (mState == AnimState.OPENED) {
            canvas.drawPath(getmAllPath(canvas.getHeight(), canvas.getWidth()), mCircleBackPaint);
            float logoHeight = canvas.getHeight() * 3 / (2 * DIVID) - getLogo(canvas).getHeight() / 2 - canvas.getHeight() / 4f;
            canvas.drawBitmap(getLogo(canvas), canvas.getWidth() / 2 - getLogo(canvas).getWidth() / 2, logoHeight, mCircleBackPaint);
            onStateChanged(AnimState.OPENED);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas, mState);
    }

    private Bitmap getLogo(Canvas canvas) {
        if (logo == null) {
            logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

            logo = Bitmap.createScaledBitmap(logo, (int) (canvas.getWidth() * LOGO_RADIUS_PORTION),
                    (int) (canvas.getWidth() * LOGO_RADIUS_PORTION), false);
        }
        return logo;
    }

    private Path getmAllPath(int height, int width) {
        if (mAllPath == null) {
            mAllPath = new Path();
            mAllPath.moveTo(0, 0);
            mAllPath.lineTo(width, 0);
            mAllPath.lineTo(width, height);
            mAllPath.lineTo(0, height);
            mAllPath.lineTo(0, 0);
            mAllPath.close();
        }
        return mAllPath;
    }

    private Path getBottomPath(float input, int height, int width) {
        if (mBottomBackPath == null) {
            mBottomBackPath = new Path();
            float right = height * ((1 - input) / (float) DIVID);
            float left = 2 * right;
            mBottomBackPath.moveTo(width, right);
            mBottomBackPath.lineTo(width, height);
            mBottomBackPath.lineTo(0, height);
            mBottomBackPath.lineTo(0, left);
            mBottomBackPath.lineTo(width, right);
            mBottomBackPath.close();
        }
        return mBottomBackPath;
    }

    private void onStateChanged(AnimState state) {
        if (mOnStateChangeListener != null)
            mOnStateChangeListener.onChanged(state);
    }

    private OnStateChangeListener mOnStateChangeListener = null;

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.mOnStateChangeListener = listener;
    }

    public interface OnStateChangeListener {
        void onChanged(AnimState state);
    }

}
