package com.github.teocci.virtualjoystick.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017/Apr/17
 *
 * Interface definition for a callback to be invoked when a
 * JoystickView's button is moved
 */
public interface OnMoveListener
{
    /**
     * Called when a JoystickView's button has been moved
     *
     * @param angle    current angle
     * @param strength current strength
     */
    void onMove(int angle, int strength);
}
