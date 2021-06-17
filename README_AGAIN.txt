# Pison SDK Android Challenge

This app allows you to draw on a canvas based on the EulerAngles picked up from the Pison device.
The drawing canvas is done through a custom View class defined in "CanvasView.kt".
App begins in MainActivity.

Controls
1a. ActivationStates HOLD to toggle drawing.
- I would have liked to implement drawing while holding, and stop on release.
- However, this would have been impossible to test on the simulated device.

	1b. With drawing toggled on, use EulerAngles to draw.
	- Where it draws is determined by the pitch and yaw of the Pison device.
	- The coordinates are constrained to the width and height of the Android device's screen.
	- Pitch determines the Y-coordinate
	- Yaw determines the X-coordinate
	- For example, at 0 for pitch and yaw, drawing will occur in the middle of the screen.
	- At maximum pitch and yaw, drawing will occur in the top right corner.
	- Roll is not used in drawing.

2a. ImuGesture SWIPE_UP to toggle color selection.
- Toggling drawing or color selection will turn the other mode off.
- This is to prevent accidental drawing during color selection.

	2b. Use EulerAngles to select color.
	- Currently selected color will be displayed in the small rectangle.
	- Color based on RGB values.
	- Pitch determines R value, Yaw the G value, and Roll the B value
	- Use SWIPE_UP again to toggle off and select current color.

3. ImuGesture SWIPE_DOWN to toggle drawing mode.
- Toggle between drawing with sparse points and drawing with a solid line.
- Defaults to points.

In my testing I could only draw in straight lines along the X or Y-axis.
This was due to the simulated device requiring dragging the sliders one at a time.
In practice with a Pison device, I assume it would be much more elegant.