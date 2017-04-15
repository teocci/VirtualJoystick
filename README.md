## Virtual Joystick

This is a very simple "view" inspired by the project [JoystickView](https://github.com/zerokol/JoystickView). This view provides a very simple and **ready-to-use** custom view which emulates a joystick for Android.

![Alt text](/images/virtual-joystick-activity.png?raw=true "Double Joystick with custom size and colors")

### Disclaimer

This repository contains sample code intended to demonstrate the capabilities a simple custom `View`. It is not intended to be used as-is in applications as a library dependency, and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

### Specifications

The **angle** follow the rules of a simple **counter-clock** protractor. The **strength is percentage** of how far the button is **from the center to the border**.

![Alt text](/images/theory-joystick.png?raw=true "Theory Specifications")

By default the **refresh rate** to get the data is **20/sec (every 50ms)**. If you want more or less just set the listener with a parameters to set the refresh rate in milliseconds. I the next example the value for `LOOP_INTERVAL` is 200ms.

```java
joystick.setOnMoveListener(new JoystickView.OnMoveListener() { ... }, LOOP_INTERVAL); // around 5/sec
```

### Code Sample

Here is a very simple code sample to use it. Just set the `onMoveListener` to retrieve its angle and strength.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
    joystick.setOnMoveListener(new OnMoveListener() {
        @Override
        public void onMove(int angle, int strength) {
            // Do whatever you want
            Long rawX = Math.round(Math.cos(Math.toRadians(angle)) * strength);
            Long rawY = Math.round(Math.sin(Math.toRadians(angle)) * strength);
            int x = rawX.intValue();
            int y = rawY.intValue();
            Log.e(TAG, "Angle: " + angle + " Strength: " + strength + "% rawX: " + rawX + " rawY: " + rawY + " x: " + x + " y: " + y);
        }
    }, LOOP_INTERVAL);
}
```

### Attributes

You can customize the joystick according to these attributes `JV_buttonImage`, `JV_buttonColor`, `JV_borderColor`, `JV_backgroundColor`, `JV_borderWidth` and `JV_fixedCenter`

If you specified `JV_buttonImage` you don't need `JV_buttonColor`

Here is an example for your layout resources:
```xml
<com.github.teocci.virtualjoystick.view.JoystickView
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    custom:JV_buttonColor="#FF6E40"
    custom:JV_borderColor="#00796B"
    custom:JV_backgroundColor="#009688"
    custom:JV_borderWidth="4dp"
    custom:JV_fixedCenter="false"/>
```
### Contributing
If you would like to contribute code, you can do so through GitHub by forking the repository and sending a pull request.
When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

### Pre-requisites

* Android SDK 25
* Android Build Tools v25.0.2
* Android Support Repository

## License and third party libraries

The code supplied here is covered under the MIT Open Source License..
