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
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.*

/**
 * Material Circular SeekBar (SeekArc) view for Android
 *
 * the code was inspired by this old library for android 4.x https://github.com/neild001/SeekArc
 * obviously the code beyond being in kotlin has been completely remade to level of logic, until
 * for the use of a MaterialShapeDrawable inside a simple Drawable
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
     * is clockwise or anti-clockwise
     *
     * Will the progress increase clockwise or anti-clockwise
     */
    var isClockwise: Boolean = true

    /**
     * maximum value
     *
     * The Maximum value that this SeekArc can be set to
     */
    var max: Int = 100

    /**
     * minimum value
     *
     * The Minimum value that this SeekArc can be set to
     */
    var min: Int = 0

    /**
     * middle value
     *
     * the middle value of seekarc
     */
    private var middle: Int = (max + min) / 2

    /**
     * actual progress value
     */
    var progress: Int = 0
        private set

    /**
     * active bar border size
     */
    var activeWidth: Int = 4
        set(value) {
            field = value
            activeTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    /**
     * inactive bar border size
     */
    var inactiveWidth: Int = 4
        set(value) {
            field = value
            inactiveTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    /**
     * arc rotation
     *
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    var arcRotation: Int = 0
        set(value) {
            field = value
            updateThumbPosition()
        }

    /**
     * rounded edges
     *
     * Give the SeekArc rounded edges
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
     * start angle of seekarc
     *
     * Give the SeekArc start angle [0..360]
     */
    var startAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    /**
     * angle of seekarc
     *
     * The Angle through which to draw the arc [0..360]
     */
    var sweepAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    /**
     * active color (i.e. the area indicating progress) the of the circle progress bar
     */
    var activeColor: ColorStateList? = null
        set(value) {
            if (field == value) return

            field = value
            activeTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    /**
     * inactive (i.e. the area missing at the end) color of the circle progress bar
     */
    var inactiveColor: ColorStateList? = null
        set(value) {
            if (field == value) return

            field = value
            inactiveTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    /**
     * thumb (i.e. the seekbar circle) color
     */
    var thumbColor: ColorStateList?
        get() = thumbDrawable.fillColor
        set(value) {
            if (thumbDrawable.fillColor == value) return

            thumbDrawable.fillColor = value
            invalidate()
        }

    /**
     * thumb (i.e. the seekbar circle) material elevation (i.e. the edging that makes it look high)
     */
    var thumbElevation: Float
        get() = thumbDrawable.elevation
        set(value) {
            if (thumbDrawable.elevation == value) return

            thumbDrawable.elevation = value
            invalidate()
        }

    /**
     * thumb (i.e. the seekbar circle) radius
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
     * thumb (i.e. the seekbar circle) stoke (border) color
     */
    var thumbStrokeColor: ColorStateList?
        get() = thumbDrawable.strokeColor
        set(value) {
            if (thumbDrawable.strokeColor == value) return

            thumbDrawable.strokeColor = value

            postInvalidate()
        }

    /**
     * thumb (i.e. the seekbar circle) stoke (border) width
     */
    var thumbStrokeWidth: Float
        get() = thumbDrawable.strokeWidth
        set(value) {
            if (thumbDrawable.strokeWidth == value) return

            thumbDrawable.strokeWidth = value

            postInvalidate()
        }

    /**
     * halo (i.e. the seekbar onclick bigger circle) radius
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
     * halo (i.e. the seekbar onclick bigger circle) color
     */
    var haloColor: ColorStateList? = null
        set(value) {
            if (field == value) return
            field = value
            if (background is RippleDrawable) {
                (background as RippleDrawable).setColor(value)
                return
            }

            haloPaint!!.color = getColorForState(value)
            haloPaint!!.alpha = HALO_ALPHA
            invalidate()

        }

    /**
     * on seek change listener
     */
    interface OnSeekProgressChangeListener {
        /**
         * on progress changed handle event
         *
         * event that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param materialSeekArc The SeekArc whose progress has changed
         * @param progress The current progress, in the range  [MaterialSeekArc.min]...[MaterialSeekArc.max].
         * @param fromUser True if the progress change was initiated by the user.
         */
        fun onProgressChanged(materialSeekArc: MaterialSeekArc, progress: Int, fromUser: Boolean) {}

        /**
         * on start tracking touch handle event
         *
         * event that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param materialSeekArc The SeekArc in which the touch gesture began
         */
        fun onStartTrackingTouch(materialSeekArc: MaterialSeekArc) {}

        /**
         * on stop tracking touch handle event
         *
         * event that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param materialSeekArc The SeekArc in which the touch gesture began
         */
        fun onStopTrackingTouch(materialSeekArc: MaterialSeekArc) {}
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.seekArcStyle)

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
     * get if the view is enabled
     */
    override fun isEnabled(): Boolean = enabled

    /**
     * sets whether the view is enabled
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
     * set progress
     * @param progress the new value
     */
    fun setProgress(progress: Int) {
        updateProgress(progress, false)
    }

    /**
     * set animator progress
     * @param progress the new value
     * @param animated if the value is to be arrived at in an animated way
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
     * stop animator
     */
    fun stopProgress() {
        timeAnimator?.end()
    }

    /**
     * on seek arc change listener
     *
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l The seek bar notification listener
     */
    fun setOnSeekArcChangeListener(l: OnSeekProgressChangeListener) {
        onSeekProgressChangeListener = l
    }

    /**
     * sets the touch and ignore touch radius values
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
        haloRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_halo_radius)

        thumbElevation =
            resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_elevation).toFloat()

        thumbRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_radius)

        inactiveWidth = resources.getDimensionPixelSize(R.dimen.mtrl_slider_track_height)

        activeWidth = resources.getDimensionPixelSize(R.dimen.mtrl_slider_track_height)

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
                R.color.material_slider_inactive_track_color
            )

        inactiveWidth =
            a.getDimensionPixelSize(R.styleable.MaterialSeekArc_trackWidthInactive, inactiveWidth)

        activeColor = a.getColorStateList(R.styleable.MaterialSeekArc_trackColorActive)
            ?: AppCompatResources.getColorStateList(
                context,
                R.color.material_slider_active_track_color
            )

        activeWidth =
            a.getDimensionPixelSize(R.styleable.MaterialSeekArc_trackWidthActive, activeWidth)

        if (a.hasValue(R.styleable.MaterialSeekArc_thumbStrokeColor))
            thumbStrokeColor = a.getColorStateList(R.styleable.MaterialSeekArc_thumbStrokeColor)

        thumbStrokeWidth =
            a.getDimension(R.styleable.MaterialSeekArc_thumbStrokeWidth, thumbStrokeWidth)

        thumbColor = a.getColorStateList(R.styleable.MaterialSeekArc_thumbColor)
            ?: AppCompatResources.getColorStateList(context, R.color.material_slider_thumb_color)

        thumbRadius = a.getDimensionPixelSize(R.styleable.MaterialSeekArc_thumbRadius, thumbRadius)

        thumbElevation = a.getDimension(R.styleable.MaterialSeekArc_thumbElevation, thumbElevation)

        haloColor = a.getColorStateList(R.styleable.MaterialSeekArc_haloColor)
            ?: AppCompatResources.getColorStateList(context, R.color.material_slider_halo_color)

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