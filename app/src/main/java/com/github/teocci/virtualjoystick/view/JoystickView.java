package com.github.teocci.virtualjoystick.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.github.teocci.virtualjoystick.R;
import com.github.teocci.virtualjoystick.interfaces.OnMoveListener;
import com.github.teocci.virtualjoystick.interfaces.OnMultipleLongPressListener;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017/Apr/17
 */

public class JoystickView extends View implements Runnable
{
    /**
     * Default refresh rate as a time in milliseconds to send move values through callback
     */
    private static final int DEFAULT_LOOP_INTERVAL = 50; // in milliseconds

    /**
     * Used to allow a slight move without cancelling MultipleLongPress
     */
    private static final int MOVE_TOLERANCE = 10;

    /**
     * Default color for button
     */
    private static final int DEFAULT_COLOR_BUTTON = Color.BLACK;

    /**
     * Default color for border
     */
    private static final int DEFAULT_COLOR_BORDER = Color.TRANSPARENT;

    /**
     * Default background color
     */
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

    /**
     * Default View's size
     */
    private static final int DEFAULT_SIZE = 200;

    /**
     * Ratio use to define the size of the button
     */
    private static final double RATIO_SIZE_BUTTON = 0.25;

    /**
     * Ratio use to define the size of border (as the distance from the center)
     */
    private static final double RATIO_SIZE_BORDER = 0.75;

    /**
     * Default border's width
     */
    private static final int DEFAULT_WIDTH_BORDER = 3;

    /**
     * Default behavior to fixed center (not auto-defined)
     */
    private static final boolean DEFAULT_FIXED_CENTER = true;

    // Drawing
    private Paint paintCircleButton;
    private Paint paintCircleBorder;
    private Paint paintBackground;

    private Paint paintBitmapButton;
    private Bitmap buttonBitmap;

    // Coordinate
    private int posX = 0;
    private int posY = 0;
    private int centerX = 0;
    private int centerY = 0;

    private int fixedCenterX = 0;
    private int fixedCenterY = 0;

    /**
     * Used to adapt behavior whether it is auto-defined center (false) or fixed center (true)
     */
    private boolean fixedCenter;

    // Joystick Size
    private int buttonRadius;
    private int borderRadius;

    /**
     * Listener used to dispatch OnMove event
     */
    private OnMoveListener callback;

    private long loopInterval = DEFAULT_LOOP_INTERVAL;
    private Thread thread = new Thread(this);

    /**
     * Listener used to dispatch MultipleLongPress event
     */
    private OnMultipleLongPressListener onMultipleLongPressListener;

    private final Handler handlerMultipleLongPress = new Handler();
    private Runnable runnableMultipleLongPress;
    private int moveTolerance;

    /**
     * Simple constructor to use when creating a JoystickView from code.
     * Call another constructor passing null to Attribute.
     *
     * @param context The Context the JoystickView is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public JoystickView(Context context)
    {
        this(context, null);
    }


    public JoystickView(Context context, AttributeSet attribute, int definitionStyleAttribute)
    {
        this(context, attribute);
    }

    /**
     * Constructor that is called when inflating a JoystickView from XML. This is called
     * when a JoystickView is being constructed from an XML file, supplying attributes
     * that were specified in the XML file.
     *
     * @param context The Context the JoystickView is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the JoystickView.
     */
    public JoystickView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JoystickView,
                0, 0
        );

        int buttonColor;
        int borderColor;
        int backgroundColor;
        int borderWidth;
        Drawable buttonDrawable;
        try {
            buttonColor = styledAttributes.getColor(R.styleable.JoystickView_JV_buttonColor, DEFAULT_COLOR_BUTTON);
            borderColor = styledAttributes.getColor(R.styleable.JoystickView_JV_borderColor, DEFAULT_COLOR_BORDER);
            backgroundColor = styledAttributes.getColor(R.styleable.JoystickView_JV_backgroundColor, DEFAULT_BACKGROUND_COLOR);
            borderWidth = styledAttributes.getDimensionPixelSize(R.styleable.JoystickView_JV_borderWidth, DEFAULT_WIDTH_BORDER);
            fixedCenter = styledAttributes.getBoolean(R.styleable.JoystickView_JV_fixedCenter, DEFAULT_FIXED_CENTER);
            buttonDrawable = styledAttributes.getDrawable(R.styleable.JoystickView_JV_buttonImage);
        } finally {
            styledAttributes.recycle();
        }

        // Initialize the drawing according to attributes
        paintCircleButton = new Paint();
        paintCircleButton.setAntiAlias(true);
        paintCircleButton.setColor(buttonColor);
        paintCircleButton.setStyle(Paint.Style.FILL);

        if (buttonDrawable != null) {
            if (buttonDrawable instanceof BitmapDrawable) {
                buttonBitmap = ((BitmapDrawable) buttonDrawable).getBitmap();
                paintBitmapButton = new Paint();
            }
        }

        paintCircleBorder = new Paint();
        paintCircleBorder.setAntiAlias(true);
        paintCircleBorder.setColor(borderColor);
        paintCircleBorder.setStyle(Paint.Style.STROKE);
        paintCircleBorder.setStrokeWidth(borderWidth);

        paintBackground = new Paint();
        paintBackground.setAntiAlias(true);
        paintBackground.setColor(backgroundColor);
        paintBackground.setStyle(Paint.Style.FILL);

        // Init Runnable for MultiLongPress
        runnableMultipleLongPress = new Runnable()
        {
            @Override
            public void run()
            {
                if (onMultipleLongPressListener != null)
                    onMultipleLongPressListener.onMultipleLongPress();
            }
        };
    }

    private void initPosition()
    {
        // get the center of view to position circle
        fixedCenterX = centerX = posX = getWidth() / 2;
        fixedCenterY = centerY = posY = getWidth() / 2;
    }

    /**
     * Draw the background, the border and the button
     *
     * @param canvas the canvas on which the shapes will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        // Draw the background
        canvas.drawCircle(fixedCenterX, fixedCenterY, borderRadius, paintBackground);

        // Draw the circle border
        canvas.drawCircle(fixedCenterX, fixedCenterY, borderRadius, paintCircleBorder);

        if (buttonBitmap != null) { // Draw the button from image
            canvas.drawBitmap(
                    buttonBitmap,
                    posX + fixedCenterX - centerX - buttonRadius,
                    posY + fixedCenterY - centerY - buttonRadius,
                    paintBitmapButton
            );
        } else { // Draw the button as simple circle
            canvas.drawCircle(
                    posX + fixedCenterX - centerX,
                    posY + fixedCenterY - centerY,
                    buttonRadius,
                    paintCircleButton
            );
        }
    }

    /**
     * This is called during layout when the size of this view has changed.
     * Here we get the center of the view and the radius to draw all the shapes.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldW Old width of this view.
     * @param oldH Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH)
    {
        super.onSizeChanged(w, h, oldW, oldH);

        initPosition();

        // radius based on smallest size : height OR width
        int d = Math.min(w, h);
        buttonRadius = (int) (d / 2 * RATIO_SIZE_BUTTON);
        borderRadius = (int) (d / 2 * RATIO_SIZE_BORDER);

        if (buttonBitmap != null)
            buttonBitmap = Bitmap.createScaledBitmap(buttonBitmap, buttonRadius * 2, buttonRadius * 2, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // setting the measured values to resize the view to a certain width and height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }


    private int measure(int measureSpec)
    {
        if (MeasureSpec.getMode(measureSpec) == MeasureSpec.UNSPECIFIED) {
            // if no bounds are specified return a default size (200)
            return DEFAULT_SIZE;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            return MeasureSpec.getSize(measureSpec);
        }
    }

    /**
     * Handle touch screen motion event. Move the button according to the
     * finger coordinate and detect longPress by multiple pointers only.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Moves the button according to the finger coordinate
        posX = (int) event.getX();
        posY = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            resetButtonPosition();

            thread.interrupt();

            if (callback != null)
                callback.onMove(getAngle(), getStrength());
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }

            thread = new Thread(this);
            thread.start();

            if (callback != null)
                callback.onMove(getAngle(), getStrength());
        }

        // Handles first touch and long press with multiple touch only
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // When the first touch occurs we update the center (if set to auto-defined center)
                if (!fixedCenter) {
                    centerX = posX;
                    centerY = posY;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                // When the second finger touch
                if (event.getPointerCount() == 2) {
                    handlerMultipleLongPress.postDelayed(runnableMultipleLongPress, ViewConfiguration.getLongPressTimeout() * 2);
                    moveTolerance = MOVE_TOLERANCE;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:
                moveTolerance--;
                if (moveTolerance == 0) {
                    handlerMultipleLongPress.removeCallbacks(runnableMultipleLongPress);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // When the last multiple touch is released
                if (event.getPointerCount() == 2) {
                    handlerMultipleLongPress.removeCallbacks(runnableMultipleLongPress);
                }
                break;
            }
        }

        double abs = Math.sqrt((posX - centerX) * (posX - centerX)
                + (posY - centerY) * (posY - centerY));

        if (abs > borderRadius) {
            posX = (int) ((posX - centerX) * borderRadius / abs + centerX);
            posY = (int) ((posY - centerY) * borderRadius / abs + centerY);
        }

        // Forces a new draw
        invalidate();

        return true;
    }

    /**
     * Process the angle following the 360° counter-clock protractor rules.
     *
     * @return the angle of the button
     */
    private int getAngle()
    {
        int angle = (int) Math.toDegrees(Math.atan2(centerY - posY, posX - centerX));
        return angle < 0 ? angle + 360 : angle; // make it as a regular counter-clock protractor
    }

    /**
     * Process the strength as a percentage of the distance between the center and the border.
     *
     * @return the strength of the button
     */
    private int getStrength()
    {
        return (int) (100 * Math.sqrt((posX - centerX)
                * (posX - centerX) + (posY - centerY)
                * (posY - centerY)) / borderRadius);
    }

    /**
     * Reset the button position to the center.
     */
    public void resetButtonPosition()
    {
        posX = centerX;
        posY = centerY;
    }

    /**
     * Set an image to the button with a drawable
     *
     * @param d drawable to pick the image
     */
    public void setButtonDrawable(Drawable d)
    {
        if (d != null) {
            if (d instanceof BitmapDrawable) {
                buttonBitmap = ((BitmapDrawable) d).getBitmap();

                if (buttonRadius != 0) {
                    buttonBitmap = Bitmap.createScaledBitmap(
                            buttonBitmap,
                            buttonRadius * 2,
                            buttonRadius * 2,
                            false);
                }

                if (paintBitmapButton != null)
                    paintBitmapButton = new Paint();
            }
        }
    }

    /**
     * Set the button color for this JoystickView.
     *
     * @param color the color of the button
     */
    public void setButtonColor(int color)
    {
        paintCircleButton.setColor(color);
        invalidate();
    }

    /**
     * Set the border color for this JoystickView.
     *
     * @param color the color of the border
     */
    public void setBorderColor(int color)
    {
        paintCircleBorder.setColor(color);
        invalidate();
    }

    /**
     * Set the background color for this JoystickView.
     *
     * @param color the color of the background
     */
    @Override
    public void setBackgroundColor(int color)
    {
        paintBackground.setColor(color);
        invalidate();
    }

    /**
     * Set the border width for this JoystickView.
     *
     * @param width the width of the border
     */
    public void setBorderWidth(int width)
    {
        paintCircleBorder.setStrokeWidth(width);
        invalidate();
    }

    /**
     * Register a callback to be invoked when this JoystickView's button is moved
     *
     * @param l The callback that will run
     */
    public void setOnMoveListener(OnMoveListener l)
    {
        setOnMoveListener(l, DEFAULT_LOOP_INTERVAL);
    }

    /**
     * Register a callback to be invoked when this JoystickView's button is moved
     *
     * @param l            The callback that will run
     * @param loopInterval Refresh rate to be invoked in milliseconds
     */
    public void setOnMoveListener(OnMoveListener l, int loopInterval)
    {
        callback = l;
        this.loopInterval = loopInterval;
    }

    /**
     * Register a callback to be invoked when this JoystickView is touch and held by multiple pointers
     *
     * @param l The callback that will run
     */
    public void setOnMultiLongPressListener(OnMultipleLongPressListener l)
    {
        onMultipleLongPressListener = l;
    }

    /**
     * Set the joystick center's behavior (fixed or auto-defined)
     *
     * @param fixedCenter True for fixed center, False for auto-defined center based on touch down
     */
    public void setFixedCenter(boolean fixedCenter)
    {
        // If we set to "fixed" we make sure to re-init position related to the width of the joystick
        if (fixedCenter) {
            initPosition();
        }
        this.fixedCenter = fixedCenter;
        invalidate();
    }

    @Override // Runnable
    public void run()
    {
        while (!Thread.interrupted()) {
            post(new Runnable()
            {
                public void run()
                {
                    if (callback != null)
                        callback.onMove(getAngle(), getStrength());
                }
            });

            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}