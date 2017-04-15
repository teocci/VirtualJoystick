package com.github.teocci.virtualjoystick.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.teocci.virtualjoystick.R;
import com.github.teocci.virtualjoystick.interfaces.OnMoveListener;
import com.github.teocci.virtualjoystick.view.JoystickView;

import java.io.IOException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017/Apr/17
 */
public class MainActivity extends AppCompatActivity
{
    public static final String TAG = MainActivity.class.getSimpleName();

    private final int CENTER_VALUE = 200; // this represent the value of the center
    private final int MAX_RANGE_VALUE = 100; // this represent the maximum value from the center
    private static final int LOOP_INTERVAL = 200; // in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView angleTextView = (TextView) findViewById(R.id.angle_value);
        final TextView strengthTextView = (TextView) findViewById(R.id.strength_value);
        final TextView xTextView = (TextView) findViewById(R.id.x_value);
        final TextView yTextView = (TextView) findViewById(R.id.y_value);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new OnMoveListener()
        {
            @Override
            public void onMove(int angle, int strength)
            {
                // Do whatever you want
                Long rawX = Math.round(Math.cos(Math.toRadians(angle)) * strength * MAX_RANGE_VALUE / 100);
                Long rawY = Math.round(Math.sin(Math.toRadians(angle)) * strength * MAX_RANGE_VALUE / 100);
                final int x = CENTER_VALUE + rawX.intValue();
                final int y = CENTER_VALUE + rawY.intValue();

                updateTextViews(angle, strength, x, y);


                Log.e(TAG, "Angle: " + angle + " Strength: " + strength + "% rawX: " + rawX + " rawY: " + rawY + " x: " + x + " y: " + y);
            }
        }, LOOP_INTERVAL);
    }

    public void updateTextViews(final int angle, final int strength, final int x, final int y)
    {
        final TextView angleTextView = (TextView) findViewById(R.id.angle_value);
        final TextView strengthTextView = (TextView) findViewById(R.id.strength_value);
        final TextView xTextView = (TextView) findViewById(R.id.x_value);
        final TextView yTextView = (TextView) findViewById(R.id.y_value);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    angleTextView.setText(angle + "°");
                    strengthTextView.setText(strength + "%");
                    xTextView.setText(x + "");
                    yTextView.setText(y + "");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
