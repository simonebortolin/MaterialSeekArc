package com.simonebortolin.seekarc

import android.animation.TimeAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.R as MaterialR
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.*

/**
 * A circular SeekBar styled with Material Design: the progress is selected by dragging a thumb
 * along an arc.
 *
 * The arc can be a full circle or any part of it ([startAngle], [sweepAngle], [arcRotation]),
 * and the track, thumb and halo can be styled from code or with the XML attributes of
 * `R.styleable.MaterialSeekArc`. Changes of the progress are notified to an
 * [OnSeekProgressChangeListener].
 *
 * ```xml
 * <com.simonebortolin.seekarc.MaterialSeekArc
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:valueFrom="0"
 *     android:valueTo="100"
 *     app:startAngle="30"
 *     app:sweepAngle="300"
 *     app:rotation="180" />
 * ```
 *
 * Inspired by [SeekArc](https://github.com/neild001/SeekArc), rewritten in Kotlin around a
 * MaterialShapeDrawable thumb.
 */
class MaterialSeekArc : View {

    /**
     * material drawable for draw the seek
     */
    private var thumbDrawable: MaterialShapeDrawable = MaterialShapeDrawable()

    /**
     * touch inside seekarc
     */
    private var touchInside: Boolean = true

    /**
     * enabled control
     */
    private var enabled: Boolean = true

    /**
     * radius of the arc
     */
    private var arcRadius: Int = 0

    /**
     * rect for seekarc
     */
    private val arcRect: RectF = RectF()

    /**
     * inactive track
     */
    private var inactiveTrackPart: Paint? = null

    /**
     * active track
     */
    private var activeTrackPart: Paint? = null

    /**
     * paint for thumb
     */
    private var thumbPaint: Paint? = null

    /**
     * paint for halo
     */
    private var haloPaint: Paint? = null

    /**
     * Horizontal translate
     */
    private var translateX: Int = 0

    /**
     * vertical translate
     */
    private var translateY: Int = 0

    /**
     * Horizontal position of thumb
     */
    private var thumbXPos: Int = 0

    /**
     * vertical position of thumb
     */
    private var thumbYPos: Int = 0

    /**
     * angle touch
     */
    private var touchAngle: Double = 0.0

    /**
     * radius ignore touch
     */
    private var touchIgnoreRadius: Float = 0f

    /**
     * Listener for when the value of progress changes
     */
    private var onSeekProgressChangeListener: OnSeekProgressChangeListener? = null

    /**
     * default thumb radius  size
     */
    private var defaultThumbRadius: Int = 0

    /**
     * time animator for seek bar update for audio
     */
    private var timeAnimator: TimeAnimator? = null

    /**
     * progress Sweep
     */
    private val progressSweep: Float
        get() = (progress.toFloat() - min) / (max - min) * sweepAngle

    /**
     * Whether the progress increases clockwise (`true`, the default) or anti-clockwise.
     */
    var isClockwise: Boolean = true

    /**
     * Maximum value of the [progress], 100 by default.
     */
    var max: Int = 100

    /**
     * Minimum value of the [progress], 0 by default.
     */
    var min: Int = 0

    /**
     * middle value
     *
     * the middle value of seekarc
     */
    private var middle: Int = (max + min) / 2

    /**
     * Current progress, in the range [min]..[max]. Set it with [setProgress].
     */
    var progress: Int = 0
        private set

    /**
     * Width of the active track (the part that shows the progress), in pixels.
     */
    var activeWidth: Int = 4
        set(value) {
            field = value
            activeTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    /**
     * Width of the inactive track (the rest of the arc), in pixels.
     */
    var inactiveWidth: Int = 4
        set(value) {
            field = value
            inactiveTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    /**
     * Rotation of the whole arc, in degrees: 0 starts the arc at twelve o'clock.
     */
    var arcRotation: Int = 0
        set(value) {
            field = value
            updateThumbPosition()
        }

    /**
     * Whether the ends of the tracks are rounded (`true`, the default) or square.
     */
    var roundedEdges: Boolean = true
        set(value) {
            field = value
            if (roundedEdges) {
                inactiveTrackPart!!.strokeCap = Paint.Cap.ROUND
                activeTrackPart!!.strokeCap = Paint.Cap.ROUND
            } else {
                inactiveTrackPart!!.strokeCap = Paint.Cap.SQUARE
                activeTrackPart!!.strokeCap = Paint.Cap.SQUARE
            }
            invalidate()
        }

    /**
     * Angle where the arc starts, in degrees in the range 0..360, relative to [arcRotation].
     */
    var startAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    /**
     * Angle covered by the arc, in degrees in the range 0..360: 360 draws a full circle.
     */
    var sweepAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    /**
     * Color of the active track (the part that shows the progress).
     */
    var activeColor: ColorStateList? = null
        set(value) {
            if (field == value) return

            field = value
            activeTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    /**
     * Color of the inactive track (the rest of the arc).
     */
    var inactiveColor: ColorStateList? = null
        set(value) {
            if (field == value) return

            field = value
            inactiveTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    /**
     * Fill color of the thumb, the circle dragged along the arc.
     */
    var thumbColor: ColorStateList?
        get() = thumbDrawable.fillColor
        set(value) {
            if (thumbDrawable.fillColor == value) return

            thumbDrawable.fillColor = value
            invalidate()
        }

    /**
     * Elevation of the thumb, in pixels, which draws its Material shadow.
     */
    var thumbElevation: Float
        get() = thumbDrawable.elevation
        set(value) {
            if (thumbDrawable.elevation == value) return

            thumbDrawable.elevation = value
            invalidate()
        }

    /**
     * Radius of the thumb, in pixels.
     */
    var thumbRadius: Int = 0
        set(value) {
            if (value == field) return

            field = value

            thumbDrawable.shapeAppearanceModel =
                ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, thumbRadius.toFloat()).build()
            thumbDrawable.setBounds(-thumbRadius, -thumbRadius, thumbRadius, thumbRadius)

            postInvalidate()
        }

    /**
     * Color of the thumb border.
     */
    var thumbStrokeColor: ColorStateList?
        get() = thumbDrawable.strokeColor
        set(value) {
            if (thumbDrawable.strokeColor == value) return

            thumbDrawable.strokeColor = value

            postInvalidate()
        }

    /**
     * Width of the thumb border, in pixels.
     */
    var thumbStrokeWidth: Float
        get() = thumbDrawable.strokeWidth
        set(value) {
            if (thumbDrawable.strokeWidth == value) return

            thumbDrawable.strokeWidth = value

            postInvalidate()
        }

    /**
     * Radius of the halo, the larger circle shown around the thumb while it is pressed, in pixels.
     */
    var haloRadius: Int = 0
        set(value) {
            if (field == value) return
            field = value
            if (background is RippleDrawable) {
                (background as RippleDrawable).radius = value
                return
            }

            postInvalidate()
        }

    /**
     * Color of the halo, the larger circle shown around the thumb while it is pressed.
     */
    var haloColor: ColorStateList? = null
        set(value) {
            if (field == value) return
            field = value
            if (background is RippleDrawable) {
                value?.let { (background as RippleDrawable).setColor(it) }
                return
            }

            haloPaint!!.color = getColorForState(value)
            haloPaint!!.alpha = HALO_ALPHA
            invalidate()

        }

    /**
     * Listener notified when the progress changes and when the user starts and stops dragging
     * the thumb. All the methods have an empty default implementation.
     *
     * @see setOnSeekArcChangeListener
     */
    interface OnSeekProgressChangeListener {
        /**
         * Called when the progress changes. Use [fromUser] to tell the changes made by the user
         * from the ones made from code.
         *
         * @param materialSeekArc The SeekArc whose progress has changed
         * @param progress The current progress, in the range  [MaterialSeekArc.min]...[MaterialSeekArc.max].
         * @param fromUser True if the progress change was initiated by the user.
         */
        fun onProgressChanged(materialSeekArc: MaterialSeekArc, progress: Int, fromUser: Boolean) {}

        /**
         * Called when the user starts dragging the thumb, for example to pause updates of the
         * progress from code.
         *
         * @param materialSeekArc The SeekArc in which the touch gesture began
         */
        fun onStartTrackingTouch(materialSeekArc: MaterialSeekArc) {}

        /**
         * Called when the user stops dragging the thumb.
         *
         * @param materialSeekArc The SeekArc in which the touch gesture ended
         */
        fun onStopTrackingTouch(materialSeekArc: MaterialSeekArc) {}
    }

    /**
     * Creates the view from code, without a style.
     */
    constructor(context: Context) : this(context, null, 0)

    /**
     * Creates the view from XML, with the theme `seekArcStyle` as default style.
     */
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.seekArcStyle)

    /**
     * Creates the view from XML with the given default style attribute.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        activeTrackPart = Paint()
        activeTrackPart!!.isAntiAlias = true
        activeTrackPart!!.style = Paint.Style.STROKE
        activeTrackPart!!.strokeCap = Paint.Cap.ROUND

        inactiveTrackPart = Paint()
        inactiveTrackPart!!.isAntiAlias = true
        inactiveTrackPart!!.style = Paint.Style.STROKE
        inactiveTrackPart!!.strokeCap = Paint.Cap.ROUND

        thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        thumbPaint!!.style = Paint.Style.FILL
        thumbPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        haloPaint!!.style = Paint.Style.FILL

        thumbDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS

        loadResources(context.resources)

        processAttributes(context, attrs, defStyle)

        isClickable = true

        isFocusable = true
    }

    /**
     * Whether the user can drag the thumb. A disabled SeekArc hides the thumb.
     */
    override fun isEnabled(): Boolean = enabled

    /**
     * Enables or disables dragging the thumb. A disabled SeekArc hides the thumb.
     */
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * Draw the view
     */
    override fun onDraw(canvas: Canvas) {
        if (!isClockwise) {
            canvas.scale(-1f, 1f, arcRect.centerX(), arcRect.centerY())
        }

        // Draw the arcs
        val arcStart: Int = startAngle + ANGLE_OFFSET + arcRotation
        val arcSweep: Int = sweepAngle
        canvas.drawArc(arcRect, arcStart.toFloat(), arcSweep.toFloat(), false, inactiveTrackPart!!)
        if (progress > min)
            canvas.drawArc(arcRect, arcStart.toFloat(), progressSweep, false, activeTrackPart!!)
        if (enabled) {
            // Draw the thumb nail
            canvas.translate(
                (translateX - thumbXPos).toFloat(),
                (translateY - thumbYPos).toFloat()
            )
            thumbDrawable.draw(canvas)
        }
        if (isPressed || isFocused && enabled) {
            val x = (translateX - thumbXPos) / width
            val y = (translateY - thumbYPos) / height

            if (background != null && background is RippleDrawable) {
                DrawableCompat.setHotspotBounds(
                    background,
                    x - haloRadius,
                    y - haloRadius,
                    x + haloRadius,
                    y + haloRadius
                )
            } else canvas.drawCircle(x.toFloat(), y.toFloat(), haloRadius.toFloat(), haloPaint!!)
        }
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height: Int = getDefaultSize(
            suggestedMinimumHeight,
            heightMeasureSpec
        )
        val width: Int = getDefaultSize(
            suggestedMinimumWidth,
            widthMeasureSpec
        )
        val min: Int = min(width, height) - 2 * haloRadius
        translateX = (width * 0.5f).toInt()
        translateY = (height * 0.5f).toInt()
        val arcDiameter = min - paddingLeft
        arcRadius = arcDiameter / 2
        val top = (height / 2 - (arcDiameter / 2)).toFloat()
        val left = (width / 2 - (arcDiameter / 2)).toFloat()
        arcRect.set(left, top, left + arcDiameter, top + arcDiameter)
        val arcStart: Int = progressSweep.toInt() + startAngle + arcRotation + 90
        thumbXPos = (arcRadius * cos(Math.toRadians(arcStart.toDouble()))).toInt()
        thumbYPos = (arcRadius * sin(Math.toRadians(arcStart.toDouble()))).toInt()
        setTouchValues(touchInside)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * event handle in case of touch
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enabled) {
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    onStartTrackingTouch()
                    updateOnTouch(event)
                }
                MotionEvent.ACTION_MOVE -> updateOnTouch(event)
                MotionEvent.ACTION_UP -> {
                    onStopTrackingTouch()
                    isPressed = false
                    parent.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_CANCEL -> {
                    onStopTrackingTouch()
                    isPressed = false
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
        return false
    }

    /**
     * This function is called whenever the state of the view changes in such
     * a way that it impacts the state of drawables being shown.
     */
    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (thumbDrawable.isStateful) {
            val state: IntArray = drawableState
            thumbDrawable.state = state
        }
        invalidate()
    }

    /**
     * Sets the [progress], without animation.
     *
     * @param progress the new value, in the range [min]..[max]
     */
    fun setProgress(progress: Int) {
        updateProgress(progress, false)
    }

    /**
     * Sets the [progress], optionally moving the thumb there with an animation.
     *
     * @param progress the new value, in the range [min]..[max]
     * @param animated whether to animate the change, stop the animation with [stopProgress]
     */
    fun setProgress(progress: Int, animated: Boolean) {
        if (animated) {
            val startProgress = this.progress.toLong()
            if (timeAnimator == null) {
                timeAnimator = TimeAnimator()
                timeAnimator?.currentPlayTime = startProgress
            }
            timeAnimator?.setTimeListener { animation, totalTime, _ ->
                setProgress(totalTime.toInt() + startProgress.toInt())

                if (totalTime.toInt() + startProgress.toInt() >= progress) animation.end()

            }
            timeAnimator?.start()
        } else {
            updateProgress(progress, false)
        }
    }

    /**
     * Stops the animation started by [setProgress] with `animated = true`.
     */
    fun stopProgress() {
        timeAnimator?.end()
    }

    /**
     * Sets the listener notified of the progress changes and of the start and end of the
     * user's touch gestures.
     *
     * @param l the listener
     */
    fun setOnSeekArcChangeListener(l: OnSeekProgressChangeListener) {
        onSeekProgressChangeListener = l
    }

    /**
     * Sets where touches move the thumb.
     *
     * @param isEnabled `true` to also accept touches inside the arc, `false` to accept only
     * touches near the arc
     */
    fun setTouchValues(isEnabled: Boolean) {
        val thumbHalfHeight: Int = thumbDrawable.intrinsicHeight / 2
        val thumbHalfWidth: Int = thumbDrawable.intrinsicWidth / 2
        touchInside = isEnabled
        touchIgnoreRadius = if (touchInside) {
            arcRadius.toFloat() / 4
        } else {
            arcRadius - min(thumbHalfWidth, thumbHalfHeight).toFloat()
        }
    }

    /**
     * converts colours from resource to rgb
     */
    @ColorInt
    private fun getColorForState(colorStateList: ColorStateList?): Int =
        colorStateList!!.getColorForState(drawableState, colorStateList.defaultColor)

    /**
     * sanitises the input eliminating values outside the range
     *
     * @param value the value to be sanitize
     * @param min the min value
     * @param max the max value
     */
    private fun sanitizeInput(value: Int, min: Int, max: Int): Int {
        var v = value
        v = if (v > max) max else v
        v = if (v < min) min else v
        return v
    }

    /**
     * load default resources
     */
    private fun loadResources(resources: Resources) {
        haloRadius = resources.getDimensionPixelSize(MaterialR.dimen.mtrl_slider_halo_radius)

        thumbElevation =
            resources.getDimensionPixelSize(MaterialR.dimen.mtrl_slider_thumb_elevation).toFloat()

        thumbRadius = resources.getDimensionPixelSize(MaterialR.dimen.mtrl_slider_thumb_radius)

        inactiveWidth = resources.getDimensionPixelSize(MaterialR.dimen.mtrl_slider_track_height)

        activeWidth = resources.getDimensionPixelSize(MaterialR.dimen.mtrl_slider_track_height)

        defaultThumbRadius = thumbRadius
    }

    /**
     * process the view attributes in xml
     */
    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (attrs == null) return

        val a: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialSeekArc,
            defStyle,
            R.style.BaseSeekArc
        )

        inactiveColor = a.getColorStateList(R.styleable.MaterialSeekArc_trackColorInactive)
            ?: AppCompatResources.getColorStateList(
                context,
                MaterialR.color.material_slider_inactive_track_color
            )

        inactiveWidth =
            a.getDimensionPixelSize(R.styleable.MaterialSeekArc_trackWidthInactive, inactiveWidth)

        activeColor = a.getColorStateList(R.styleable.MaterialSeekArc_trackColorActive)
            ?: AppCompatResources.getColorStateList(
                context,
                MaterialR.color.material_slider_active_track_color
            )

        activeWidth =
            a.getDimensionPixelSize(R.styleable.MaterialSeekArc_trackWidthActive, activeWidth)

        if (a.hasValue(R.styleable.MaterialSeekArc_thumbStrokeColor))
            thumbStrokeColor = a.getColorStateList(R.styleable.MaterialSeekArc_thumbStrokeColor)

        thumbStrokeWidth =
            a.getDimension(R.styleable.MaterialSeekArc_thumbStrokeWidth, thumbStrokeWidth)

        thumbColor = a.getColorStateList(R.styleable.MaterialSeekArc_thumbColor)
            ?: AppCompatResources.getColorStateList(context, MaterialR.color.material_slider_thumb_color)

        thumbRadius = a.getDimensionPixelSize(R.styleable.MaterialSeekArc_thumbRadius, thumbRadius)

        thumbElevation = a.getDimension(R.styleable.MaterialSeekArc_thumbElevation, thumbElevation)

        haloColor = a.getColorStateList(R.styleable.MaterialSeekArc_haloColor)
            ?: AppCompatResources.getColorStateList(context, MaterialR.color.material_slider_halo_color)

        haloRadius = a.getDimensionPixelSize(R.styleable.MaterialSeekArc_haloRadius, haloRadius)

        max = a.getInteger(R.styleable.MaterialSeekArc_android_valueTo, max)

        min = a.getInteger(R.styleable.MaterialSeekArc_android_valueFrom, min)

        progress = a.getInteger(R.styleable.MaterialSeekArc_android_value, progress)

        startAngle = a.getInt(R.styleable.MaterialSeekArc_startAngle, startAngle)

        sweepAngle = a.getInt(R.styleable.MaterialSeekArc_sweepAngle, sweepAngle)

        arcRotation = a.getInt(R.styleable.MaterialSeekArc_rotation, arcRotation)

        roundedEdges = a.getBoolean(R.styleable.MaterialSeekArc_roundEdges, roundedEdges)

        touchInside = a.getBoolean(R.styleable.MaterialSeekArc_touchInside, touchInside)

        isClockwise = a.getBoolean(R.styleable.MaterialSeekArc_clockwise, isClockwise)

        enabled = a.getBoolean(R.styleable.MaterialSeekArc_android_enabled, enabled)

        a.recycle()
    }

    /**
     * event handler of when touch start
     */
    private fun onStartTrackingTouch() {
        if (onSeekProgressChangeListener != null) {
            stopProgress()
            onSeekProgressChangeListener!!.onStartTrackingTouch(this)
        }
    }

    /**
     * event handler of when touch ends
     */
    private fun onStopTrackingTouch() {
        if (onSeekProgressChangeListener != null) {
            onSeekProgressChangeListener!!.onStopTrackingTouch(this)
        }
    }

    /**
     * update seekbar after touch
     */
    private fun updateOnTouch(event: MotionEvent) {
        val ignoreTouch: Boolean = ignoreTouch(event.x, event.y)
        if (ignoreTouch) {
            return
        }
        isPressed = true
        touchAngle = getTouchDegrees(event.x, event.y)

        val progress: Int = getProgressForAngle(touchAngle)
        updateProgress(progress, true)
    }

    /**
     * determines whether the touch action is to be ignored
     */
    private fun ignoreTouch(xPos: Float, yPos: Float): Boolean {
        var ignore = false
        val x: Float = xPos - translateX
        val y: Float = yPos - translateY
        val touchRadius: Float = sqrt((x * x) + (y * y).toDouble()).toFloat()
        if (touchRadius < touchIgnoreRadius) {
            ignore = true
        }
        return ignore
    }

    /**
     * calculates the angle at which the thumb has been moved
     */
    private fun getTouchDegrees(xPos: Float, yPos: Float): Double {
        var x: Float = xPos - translateX
        val y: Float = yPos - translateY

        x = if (isClockwise) x else -x  //invert the x-cord if we are rotating anti-clockwise

        var angle: Double = Math.toDegrees(
            atan2(y.toDouble(), x.toDouble()) + (Math.PI / 2)
                    - Math.toRadians(arcRotation.toDouble())
        )
        if (angle < 0) {
            angle += 360
        }
        angle -= startAngle.toDouble()
        return angle
    }

    /**
     * calculates the progress by angle
     */
    private fun getProgressForAngle(angle: Double): Int {
        var touchProgress: Int = (valuePerDegree() * angle).roundToLong().toInt() + min
        touchProgress =
            if (touchProgress < min) if (progress != max) min else max else touchProgress
        touchProgress =
            if (touchProgress > max) if (progress != min) max else min else touchProgress

        if (progress == max && touchProgress <= middle) touchProgress = max
        if (progress == min && touchProgress >= middle) touchProgress = min

        return touchProgress
    }

    /**
     * calculates how many values there are per angle
     */
    private fun valuePerDegree(): Float = (max - min).toFloat() / sweepAngle

    /**
     * update the thumb position
     */
    private fun updateThumbPosition() {
        val thumbAngle: Int = (startAngle + progressSweep + arcRotation + 90).toInt()
        thumbXPos = (arcRadius * cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        thumbYPos = (arcRadius * sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
    }

    /**
     * update the progress in the seekbar
     */
    private fun updateProgress(progress: Int, fromUser: Boolean) {
        if (progress == INVALID_PROGRESS_VALUE) {
            return
        }
        this.progress = sanitizeInput(progress, min, max)
        if (onSeekProgressChangeListener != null) {
            onSeekProgressChangeListener!!
                .onProgressChanged(this, progress, fromUser)
        }
        updateThumbPosition()
        invalidate()
    }

    /**
     * static variables
     */
    companion object {
        private const val INVALID_PROGRESS_VALUE: Int = -1

        private const val ANGLE_OFFSET: Int = -90

        private const val HALO_ALPHA = 63
    }
}