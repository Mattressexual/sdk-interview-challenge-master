package com.pison.pisonsdkandroidskeleton

import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.badoo.reaktive.observable.subscribe
import com.pison.core.client.newPisonSdkInstance
import com.pison.core.generated.ActivationState
import com.pison.core.generated.ImuGesture
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    // IP Address and Port number
    private val hostAddress = "192.168.1.15"
    private val port = 8090

    // Constant for converting from 360 degrees to 255 RGB
    private val conversionConstant = 255.0 / 360.0

    // Booleans for current action
    private var drawing = false
    private var selectingColor = false

    // Boolean to determine if pen has just now touched down on paper, or if it has already been drawing
    private var firstDraw = true

    // Strings for current action TextView
    private val actionDrawing = "Action: Drawing"
    private val actionSelectingColor = "Action: Selecting Color"
    private val actionNone = "Action: None"

    // Strings for drawing mode TextView
    private val drawModePoints = "Points"
    private val drawModeLine = "Line"

    // Current RGB color selected (Default to white drawing on black background)
    private var currentColor = Color.parseColor("#FFFFFF")

    // Variable to determine current drawing mode
    // 0 is drawing in points, 1 is solid line
    private var drawMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // Device screen dimensions
        val deviceWidth = this.resources.displayMetrics.widthPixels
        val deviceHeight = this.resources.displayMetrics.heightPixels

        // LinearLayout is the parent container
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        // The colorBox is a View that displays the currently selected color
        val colorBox = findViewById<View>(R.id.colorBox)

        // TextViews for displaying EulerAngles
        val pitchTextView = findViewById<TextView>(R.id.pitch_textView)
        val yawTextView = findViewById<TextView>(R.id.yaw_textView)
        val rollTextView = findViewById<TextView>(R.id.roll_textView)

        // TextView shows current drawing mode (Defaults to points)
        val drawingModeTextView = findViewById<TextView>(R.id.drawing_mode_textView)
        drawingModeTextView.text = drawModePoints

        // TextView shows current action (Drawing, SelectingColor, None)
        val actionTextView = findViewById<TextView>(R.id.action_textView)

        // Variables for current (X, Y) and previous (X, Y) to draw
        var prevX: Float = deviceWidth / 2f
        var prevY: Float = deviceHeight / 2f
        var currX: Float
        var currY: Float

        // Custom View object to draw on
        val canvasView = CanvasView(this)
        canvasView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f)
        linearLayout.addView(canvasView, 0)

        // Pison SDK usage
        val pisonSdk = newPisonSdkInstance(this).bindToServer(hostAddress, port)
        val disposable = pisonSdk.monitorAnyDevice().subscribe(
            onNext = { pisonDevice ->

                // Monitoring ActivationStates
                pisonDevice.monitorActivationStates().subscribe(
                    onNext = { activationStates ->
                        when (activationStates.index) {
                            ActivationState.HOLD -> {
                                // Holding toggles drawing ON/OFF
                                drawing = !drawing
                                if (!drawing)
                                    // When drawing is toggled OFF, firstDraw becomes true again until next draw
                                    firstDraw = true

                                // Drawing and SelectingColor are mutually exclusive modes
                                // So turning one ON will turn the other OFF
                                if (selectingColor)
                                    selectingColor = false
                            }
                            ActivationState.NONE -> { }
                        }
                        if (drawing)
                            // UI Update must be made on main thread
                            this@MainActivity.runOnUiThread {
                                actionTextView.text = actionDrawing
                            }
                        else
                            this@MainActivity.runOnUiThread {
                                actionTextView.text = actionNone
                            }
                        // println("ActivationState: ${activationStates.index}, Drawing: $drawing, Selecting Color: $selectingColor")
                    }
                )
                // Monitoring ImuGestures
                pisonDevice.monitorGestures().subscribe(
                    onNext = { imuGesture ->
                        when (imuGesture) {
                            ImuGesture.SWIPE_UP -> {
                                // Swiping up toggles on color selection.
                                // If color selection was already active, selects the current color and toggles OFF
                                selectingColor = if (selectingColor) {
                                    canvasView.setPaintColor(currentColor)
                                    false
                                } else
                                    true

                                // UI updates to action TextView
                                if (selectingColor)
                                    this@MainActivity.runOnUiThread {
                                        actionTextView.text = actionSelectingColor
                                    }
                                else
                                    this@MainActivity.runOnUiThread {
                                        actionTextView.text = actionNone
                                    }

                                // If drawing was ON, toggle it off and reset firstDraw to true
                                if (drawing) {
                                    drawing = false
                                    firstDraw = true
                                }
                            }
                            ImuGesture.SWIPE_DOWN -> {
                                // Toggles drawing mode
                                if (drawMode == 0) {
                                    // If drawing with points, changes to drawing with lines
                                    drawMode = 1
                                    // UI update
                                    this@MainActivity.runOnUiThread {
                                        drawingModeTextView.text = drawModeLine
                                    }
                                } else {
                                    // If drawing with lines, changes to drawing with points
                                    drawMode = 0
                                    // UI update
                                    this@MainActivity.runOnUiThread {
                                        drawingModeTextView.text = drawModePoints
                                    }
                                }
                            }
                            // All the other possible gestures
                            ImuGesture.SWIPE_LEFT -> { }
                            ImuGesture.SWIPE_RIGHT -> { }
                            ImuGesture.ROLL_LEFT -> { }
                            ImuGesture.ROLL_RIGHT -> { }
                            ImuGesture.INDEX_HOLD -> { }
                            ImuGesture.INDEX_CLICK -> { }
                            ImuGesture.THUMB_SWIPE -> { }
                            ImuGesture.UNKNOWN_GESTURE -> { }
                        }
                        // println("ImuGesture: $imuGesture, Drawing: $drawing, Selecting:$selectingColor")
                    }
                )
                // Monitoring EulerAngles
                pisonDevice.monitorEulerAngles().subscribe(
                    onNext = { eulerAngles ->
                        // Observed angles
                        val pitch = eulerAngles.pitch
                        val yaw = eulerAngles.yaw
                        val roll = eulerAngles.roll

                        // Special conversion of Pitch to invert Y-axis drawing direction
                        val coordinatePitch = -(pitch - 180)

                        // Convert to value out of 360 for
                        val rawPitch = pitch + 180
                        val rawYaw = yaw + 180
                        val rawRoll = roll + 180

                        // Strings for UI update to TextViews
                        val pitchText = "Pitch: $pitch"
                        val yawText = "Yaw: $yaw"
                        val rollText = "Roll: $roll"

                        // UI update on main thread
                        this@MainActivity.runOnUiThread {
                            pitchTextView.text = pitchText
                            yawTextView.text = yawText
                            rollTextView.text = rollText
                        }

                        // If the ActivationState.HOLD was observed, drawing begins
                        if (drawing) {
                            // Current x and y based on where pitch and yaw point
                            // Coordinates bound within width and height of screen
                            currX = rawYaw / 360 * deviceWidth
                            currY = coordinatePitch / 360 * deviceHeight

                            // If first draw, draw a point to start (prevents line from jumping from default position)
                            if (firstDraw) {
                                canvasView.drawPoint(currX, currY)
                                // Turn firstDraw OFF
                                firstDraw = false
                            } else {
                                if (drawMode == 0)
                                    // If drawing with points
                                    canvasView.drawPoint(currX, currY)
                                else
                                    // If drawing with lines
                                    canvasView.drawLine(prevX, prevY, currX, currY)
                            }
                            // Update coordinates
                            prevX = currX
                            prevY = currY
                        }
                        // If color selection active
                        if (selectingColor) {
                            // Convert angles into RGB values
                            val rVal = (rawPitch * conversionConstant).roundToInt()
                            val gVal = (rawYaw * conversionConstant).roundToInt()
                            val bVal = (rawRoll * conversionConstant).roundToInt()
                            // Update current color
                            currentColor =
                                Color.parseColor(String.format("#%02x%02x%02x", rVal, gVal, bVal))
                            // UI update for colorBox
                            this@MainActivity.runOnUiThread {
                                colorBox.setBackgroundColor(currentColor)
                            }
                        }
                    }
                )
            }
        )
    }
}
