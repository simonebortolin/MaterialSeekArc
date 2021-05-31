package com.triggertrap.seekarc

import android.animation.TimeAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.*


/**
 *
 * SeekArc.java
 *
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 *
 * @author Neil Davies
 */
class SeekArc : View {

    private var thumbDrawable : MaterialShapeDrawable = MaterialShapeDrawable()

    /**
     * The Maximum value that this SeekArc can be set to
     */
    var max: Int = 100
    var min: Int = 0

    var half: Int = (max + min)/2

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private var mRotation: Int = 0

    /**
     * Give the SeekArc rounded edges
     */
    private var mRoundedEdges: Boolean = false

    /**
     * Enable touch inside the SeekArc
     */
    private var mTouchInside: Boolean = true

    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    var isClockwise: Boolean = true

    /**
     * is the control enabled/touchable
     */
    private var mEnabled: Boolean = true

    // Internal variables
    private var mArcRadius: Int = 0
    private val mArcRect: RectF = RectF()
    private var mInactiveTrackPart: Paint? = null
    private var mActiveTrackPart: Paint? = null
    private var mThumbPaint: Paint? = null
    private var mHaloPaint: Paint? = null
    private var mTranslateX: Int = 0
    private var mTranslateY: Int = 0
    private var mThumbXPos: Int = 0
    private var mThumbYPos: Int = 0
    private var mTouchAngle: Double = 0.0
    private var mTouchIgnoreRadius: Float = 0f
    private var mOnSeekProgressChangeListener: OnSeekProgressChangeListener? = null
    private var defaultThumbRadius: Int = 0

    interface OnSeekProgressChangeListener {
        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc
         * The SeekArc whose progress has changed
         * @param progress
         * The current progress level. This will be in the range
         * min..max where min was set by
         * [SeekArc.min]. (The default value for
         * max is 0.) and max was set by
         * [SeekArc.max]. (The default value for
         * max is 100.)
         * @param fromUser
         * True if the progress change was initiated by the user.
         */
        fun onProgressChanged(seekArc: SeekArc?, progress: Int, fromUser: Boolean)

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param seekArc
         * The SeekArc in which the touch gesture began
         */
        fun onStartTrackingTouch(seekArc: SeekArc?)

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param seekArc
         * The SeekArc in which the touch gesture began
         */
        fun onStopTrackingTouch(seekArc: SeekArc?)
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.seekArcStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super( context, attrs, defStyle ) {
        Log.d(TAG, "Initialising SeekArc")


        mActiveTrackPart = Paint()
        mActiveTrackPart!!.isAntiAlias = true
        mActiveTrackPart!!.style = Paint.Style.STROKE
        mActiveTrackPart!!.strokeCap = Paint.Cap.ROUND

        mInactiveTrackPart = Paint()
        mInactiveTrackPart!!.isAntiAlias = true
        mInactiveTrackPart!!.style = Paint.Style.STROKE
        mInactiveTrackPart!!.strokeCap = Paint.Cap.ROUND

        mThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mThumbPaint!!.style = Paint.Style.FILL
        mThumbPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        mHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHaloPaint!!.style = Paint.Style.FILL

        thumbDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS

        loadResources(context.resources)

        processAttributes(context, attrs, defStyle)

        isClickable = true

        isFocusable = true
    }

    private fun loadResources(resources: Resources) {
        haloRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_halo_radius)

        thumbElevation = resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_elevation).toFloat()

        thumbRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_radius)

        inactiveWidth = resources.getDimensionPixelSize(R.dimen.mtrl_slider_track_height)

        activeWidth = resources.getDimensionPixelSize(R.dimen.mtrl_slider_track_height)

        defaultThumbRadius = thumbRadius
    }

    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (attrs == null) return

        val a: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SeekArc, defStyle, R.style.BaseSeekArc
        )

        inactiveColor = a.getColorStateList(R.styleable.SeekArc_trackColorInactive) ?: AppCompatResources.getColorStateList(context, R.color.material_slider_inactive_track_color)

        inactiveWidth = a.getDimensionPixelSize(R.styleable.SeekArc_trackWidthInactive, inactiveWidth)

        activeColor = a.getColorStateList(R.styleable.SeekArc_trackColorActive) ?: AppCompatResources.getColorStateList(context, R.color.material_slider_active_track_color)

        activeWidth = a.getDimensionPixelSize(R.styleable.SeekArc_trackWidthActive, activeWidth)

        if(a.hasValue(R.styleable.SeekArc_thumbStrokeColor))
            thumbStrokeColor = a.getColorStateList(R.styleable.SeekArc_thumbStrokeColor)

        thumbStrokeWidth = a.getDimension(R.styleable.SeekArc_thumbStrokeWidth, thumbStrokeWidth)

        thumbColor = a.getColorStateList(R.styleable.SeekArc_thumbColor) ?: AppCompatResources.getColorStateList(context, R.color.material_slider_thumb_color)

        thumbRadius = a.getDimensionPixelSize(R.styleable.SeekArc_thumbRadius, thumbRadius)

        thumbElevation = a.getDimension(R.styleable.SeekArc_thumbElevation, thumbElevation)

        haloColor = a.getColorStateList(R.styleable.SeekArc_haloColor) ?: AppCompatResources.getColorStateList(context, R.color.material_slider_halo_color)

        haloRadius = a.getDimensionPixelSize(R.styleable.SeekArc_haloRadius, haloRadius)

        max = a.getInteger(R.styleable.SeekArc_android_valueTo, max)

        min = a.getInteger(R.styleable.SeekArc_android_valueFrom, min)

        progress = a.getInteger(R.styleable.SeekArc_android_value, progress)

        startAngle = a.getInt(R.styleable.SeekArc_startAngle, startAngle)

        sweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, sweepAngle)

        mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation)

        mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges, mRoundedEdges )

        mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside, mTouchInside )

        isClockwise = a.getBoolean( R.styleable.SeekArc_clockwise, isClockwise )

        mEnabled = a.getBoolean(R.styleable.SeekArc_android_enabled, mEnabled)

        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        if (!isClockwise) {
            canvas.scale(-1f, 1f, mArcRect.centerX(), mArcRect.centerY())
        }

        // Draw the arcs
        val arcStart: Int = startAngle + ANGLE_OFFSET + mRotation
        val arcSweep: Int = sweepAngle
        canvas.drawArc(mArcRect, arcStart.toFloat(), arcSweep.toFloat(), false, mInactiveTrackPart!!)
        if (progress > min)
            canvas.drawArc(mArcRect, arcStart.toFloat(), progressSweep, false, mActiveTrackPart!!)
        if (mEnabled) {
            // Draw the thumb nail
            canvas.translate(
                (mTranslateX - mThumbXPos).toFloat(),
                (mTranslateY - mThumbYPos).toFloat()
            )
            thumbDrawable.draw(canvas)
        }
        if (isPressed || isFocused && mEnabled) {
            val x = (mTranslateX - mThumbXPos)/ width
            val y = (mTranslateY - mThumbYPos) / height

            if(background != null && background is RippleDrawable) {
                DrawableCompat.setHotspotBounds(
                    background,
                    x - haloRadius,
                    y - haloRadius,
                    x + haloRadius,
                    y + haloRadius
                )
            } else canvas.drawCircle(x.toFloat(),y.toFloat(), haloRadius.toFloat(), mHaloPaint!!)
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height: Int = getDefaultSize(
            suggestedMinimumHeight,
            heightMeasureSpec
        )
        val width: Int = getDefaultSize(
            suggestedMinimumWidth,
            widthMeasureSpec
        )
        val min: Int = min(width, height) - 2*haloRadius
        mTranslateX = (width * 0.5f).toInt()
        mTranslateY = (height * 0.5f).toInt()
        val arcDiameter = min - paddingLeft
        mArcRadius = arcDiameter / 2
        val top = (height / 2 - (arcDiameter / 2)).toFloat()
        val left = (width / 2 - (arcDiameter / 2)).toFloat()
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter)
        val arcStart: Int = progressSweep.toInt() + startAngle + mRotation + 90
        mThumbXPos = (mArcRadius * cos(Math.toRadians(arcStart.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * sin(Math.toRadians(arcStart.toDouble()))).toInt()
        setTouchInSide(mTouchInside)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {
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

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (thumbDrawable.isStateful) {
            val state: IntArray = drawableState
            thumbDrawable.state = state
        }
        invalidate()
    }


    private fun onStartTrackingTouch() {
        if (mOnSeekProgressChangeListener != null) {
            mOnSeekProgressChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        if (mOnSeekProgressChangeListener != null) {
            mOnSeekProgressChangeListener!!.onStopTrackingTouch(this)
        }
    }

    private fun updateOnTouch(event: MotionEvent) {
        val ignoreTouch: Boolean = ignoreTouch(event.x, event.y)
        if (ignoreTouch) {
            return
        }
        isPressed = true
        mTouchAngle = getTouchDegrees(event.x, event.y)

        val progress: Int = getProgressForAngle(mTouchAngle)
        updateProgress(progress, true)
    }

    private fun ignoreTouch(xPos: Float, yPos: Float): Boolean {
        var ignore = false
        val x: Float = xPos - mTranslateX
        val y: Float = yPos - mTranslateY
        val touchRadius: Float = sqrt((x * x) + (y * y).toDouble()).toFloat()
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true
        }
        return ignore
    }

    private fun getTouchDegrees(xPos: Float, yPos: Float): Double {
        var x: Float = xPos - mTranslateX
        val y: Float = yPos - mTranslateY

        //invert the x-coord if we are rotating anti-clockwise
        x = if (isClockwise) x else -x

        // convert to arc Angle
        var angle: Double = Math.toDegrees(atan2(y.toDouble(), x.toDouble()) + (Math.PI / 2)
                    - Math.toRadians(mRotation.toDouble()))
        if (angle < 0) {
            angle += 360
        }
        angle -= startAngle.toDouble()
        return angle
    }

    private fun getProgressForAngle(angle: Double): Int {
        var touchProgress: Int = (valuePerDegree() * angle).roundToLong().toInt() + min
        touchProgress = if (touchProgress < min) if(progress != max) min else max else touchProgress
        touchProgress = if (touchProgress > max) if(progress != min) max else min else touchProgress

        if(progress == max && touchProgress <= half)  touchProgress = max
        if(progress == min && touchProgress >= half)  touchProgress = min

        return touchProgress
    }

    private fun valuePerDegree(): Float {
        return (max-min).toFloat() / sweepAngle
    }

    private fun updateThumbPosition() {
        val thumbAngle: Int = (startAngle + progressSweep + mRotation + 90).toInt()
        mThumbXPos = (mArcRadius * cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
    }

    fun setProgress(progress: Int) {
        updateProgress(progress, false)
    }

    private var timeAnimator : TimeAnimator? = null

    fun setProgress(progress: Int, animated: Boolean)  {
        if (animated) {
            val startProgress = this.progress.toLong()
            if(timeAnimator == null) {
                timeAnimator = TimeAnimator()
                timeAnimator?.currentPlayTime = startProgress
            }
            timeAnimator?.setTimeListener{ animation, totalTime, _ ->
                setProgress(totalTime.toInt()/1000 + startProgress.toInt())

                if(totalTime.toInt()/1000 + startProgress.toInt() >= progress) animation.end()

            }
            timeAnimator?.start()
        } else {
            updateProgress(progress, false)
        }
    }

    fun stopProgress() {
        timeAnimator?.end()
    }




    private fun updateProgress(progress: Int, fromUser: Boolean) {
        if (progress == INVALID_PROGRESS_VALUE) {
            return
        }
        this.progress = sanitizeInput(progress, min, max)
        if (mOnSeekProgressChangeListener != null) {
            mOnSeekProgressChangeListener!!
                .onProgressChanged(this, progress, fromUser)
        }
        updateThumbPosition()
        invalidate()
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l
     * The seek bar notification listener
     *
     * @see SeekArc.OnSeekProgressChangeListener
     */
    fun setOnSeekArcChangeListener(l: OnSeekProgressChangeListener?) {
        mOnSeekProgressChangeListener = l
    }

    fun setRoundedEdges(isEnabled: Boolean) {
        mRoundedEdges = isEnabled
        if (mRoundedEdges) {
            mInactiveTrackPart!!.strokeCap = Paint.Cap.ROUND
            mActiveTrackPart!!.strokeCap = Paint.Cap.ROUND
        } else {
            mInactiveTrackPart!!.strokeCap = Paint.Cap.SQUARE
            mActiveTrackPart!!.strokeCap = Paint.Cap.SQUARE
        }
    }

    fun setTouchInSide(isEnabled: Boolean) {
        val thumbHalfHeight: Int = thumbDrawable.intrinsicHeight / 2
        val thumbHalfWidth: Int = thumbDrawable.intrinsicWidth / 2
        mTouchInside = isEnabled
        mTouchIgnoreRadius = if (mTouchInside) {
            mArcRadius.toFloat() / 4
        } else {
            mArcRadius - min(thumbHalfWidth, thumbHalfHeight).toFloat()
        }
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    private fun sanitizeInput(value: Int, min: Int, max: Int) : Int {
        var v = value
        v = if (v > max) max else v
        v = if (v < min) min else v
        return v
    }
    var progress: Int = 0
        private set

    var activeWidth: Int = 4
        set(value) {
            field = value
            mActiveTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    var inactiveWidth: Int = 4
        set(value) {
            field = value
            mInactiveTrackPart!!.strokeWidth = field.toFloat()
            invalidate()
        }

    var arcRotation: Int
        get() {
            return mRotation
        }
        set(value) {
            this.mRotation = value
            updateThumbPosition()
        }

    var startAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    var sweepAngle: Int = 0
        set(value) {
            field = sanitizeInput(value, 0, 360)
            updateThumbPosition()
        }

    var activeColor: ColorStateList? = null
        set(value) {
            if(field == value) return

            field = value
            mActiveTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    var inactiveColor: ColorStateList? = null
        set(value) {
            if(field == value) return

            field = value
            mInactiveTrackPart!!.color = getColorForState(value)
            invalidate()
        }

    var thumbColor: ColorStateList?
        get() {
            return thumbDrawable.fillColor
        }
        set(value) {
            if(thumbDrawable.fillColor == value) return

            thumbDrawable.fillColor = value
            invalidate()
        }

    var thumbElevation: Float
        get() { return thumbDrawable.elevation}
        set(value) {
            if(thumbDrawable.elevation == value) return

            thumbDrawable.elevation = value
            invalidate()
        }

    var thumbRadius: Int = 0
        set(value) {
            if (value == field) return

            field = value

            thumbDrawable.shapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, thumbRadius.toFloat()).build()
            thumbDrawable.setBounds(-thumbRadius, -thumbRadius, thumbRadius, thumbRadius)

            postInvalidate()
        }

    var thumbStrokeColor: ColorStateList?
        get() {
            return thumbDrawable.strokeColor
        }
        set(value) {
            if (thumbDrawable.strokeColor == value) return

            thumbDrawable.strokeColor = value

            postInvalidate()
        }

    var thumbStrokeWidth: Float
        get() {
            return thumbDrawable.strokeWidth
        }
        set(value) {
            if (thumbDrawable.strokeWidth == value) return

            thumbDrawable.strokeWidth = value

            postInvalidate()
        }

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

    var haloColor: ColorStateList? = null
        set(value) {
            if(field == value) return
            field = value
            if (background is RippleDrawable) {
                (background as RippleDrawable).setColor(value)
                return
            }

            mHaloPaint!!.color = getColorForState(value)
            mHaloPaint!!.alpha = HALO_ALPHA
            invalidate()

        }

    private val progressSweep: Float
        get() {
            return (progress.toFloat()-min) / (max-min) * sweepAngle
        }


    @ColorInt
    private fun getColorForState(colorStateList: ColorStateList?): Int {
        return colorStateList!!.getColorForState(drawableState, colorStateList.defaultColor)
    }

    companion object {
        private val TAG: String = SeekArc::class.java.simpleName
        private const val INVALID_PROGRESS_VALUE: Int = -1

        private const val ANGLE_OFFSET: Int = -90

        private const val HALO_ALPHA = 63
    }
}