package com.github.teocci.virtualjoystick.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017/Apr/17
 *
 * Interface definition for a callback to be invoked when a JoystickView
 * is touched and held by multiple pointers.
 */

public interface OnMultipleLongPressListener
{
    /**
     * Called when a JoystickView has been touch and held enough time by multiple pointers.
     */
    void onMultipleLongPress();
}
