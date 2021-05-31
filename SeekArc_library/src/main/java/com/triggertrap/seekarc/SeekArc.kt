/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.triggertrap.seekarc

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
    /**
     * The Drawable for the seek arc thumbnail
     */
    private var mThumb: Drawable? = null

    /**
     * The Maximum value that this SeekArc can be set to
     */
    var max: Int = 100

    private var mHaloRadius: Int = 0

    private var mHaloColor: Int = 0

    /**
     * The Current value that the SeekArc is set to
     */
    private var mProgress: Int = 0

    /**
     * The width of the progress line for this SeekArc
     */
    private var mProgressWidth: Int = 4

    /**
     * The width of the progress line for this SeekArc
     */
    private var mArcWidth: Int = 4

    /**
     * The Angle to start drawing this Arc from
     */
    private var mStartAngle: Int = 0

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private var mSweepAngle: Int = 360

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
    private var mProgressSweep: Float = 0f
    private val mArcRect: RectF = RectF()
    private var mArcPaint: Paint? = null
    private var mProgressPaint: Paint? = null
    private var mTranslateX: Int = 0
    private var mTranslateY: Int = 0
    private var mThumbXPos: Int = 0
    private var mThumbYPos: Int = 0
    private var mTouchAngle: Double = 0.0
    private var mTouchIgnoreRadius: Float = 0f
    private var mOnSeekArcChangeListener: OnSeekArcChangeListener? = null

    interface OnSeekArcChangeListener {
        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc
         * The SeekArc whose progress has changed
         * @param progress
         * The current progress level. This will be in the range
         * 0..max where max was set by
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

    constructor(context: Context) : super(context) {
        processAttributes(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        processAttributes(context, attrs, R.attr.seekArcStyle)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        processAttributes(context, attrs, defStyle)
    }

    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
        Log.d(TAG, "Initialising SeekArc")
        val res: Resources = resources
        val density: Float = context.resources.displayMetrics.density

        // Defaults, may need to link this into theme settings
        var arcColor: Int = ContextCompat.getColor(context,R.color.material_slider_active_track_color)
        var progressColor: Int = ContextCompat.getColor(context,R.color.material_slider_thumb_color)


        // Convert progress width to pixels for current density
        this.mArcWidth = (this.mArcWidth * density).toInt()
        if (attrs != null) {
            // Attribute initialization
            val a: TypedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.SeekArc, defStyle, 0
            )

            thumb = a.getDrawable(R.styleable.SeekArc_seekArc_thumb) ?: ResourcesCompat.getDrawable(res, R.drawable.seek_arc_control_selector, context.theme)

            max = a.getInteger(R.styleable.SeekArc_seekArc_max, max)
            mProgress = a.getInteger(R.styleable.SeekArc_seekArc_progress, mProgress)
            mProgressWidth = a.getDimension(
                R.styleable.SeekArc_seekArc_progressWidth, mProgressWidth.toFloat()
            ).toInt()
            mArcWidth = a.getDimension(
                R.styleable.SeekArc_seekArc_arcWidth,
                this.mArcWidth.toFloat()
            ).toInt()
            mStartAngle = a.getInt(R.styleable.SeekArc_seekArc_startAngle, mStartAngle)
            mSweepAngle = a.getInt(R.styleable.SeekArc_seekArc_sweepAngle, mSweepAngle)
            mRotation = a.getInt(R.styleable.SeekArc_seekArc_rotation, mRotation)
            mRoundedEdges = a.getBoolean(
                R.styleable.SeekArc_seekArc_roundEdges,
                mRoundedEdges
            )
            mTouchInside = a.getBoolean(
                R.styleable.SeekArc_seekArc_touchInside,
                mTouchInside
            )
            isClockwise = a.getBoolean(
                R.styleable.SeekArc_seekArc_clockwise,
                isClockwise
            )
            mEnabled = a.getBoolean(R.styleable.SeekArc_seekArc_enabled, mEnabled)
            arcColor = a.getColor(R.styleable.SeekArc_seekArc_arcColor, arcColor)
            progressColor = a.getColor(
                R.styleable.SeekArc_seekArc_progressColor,
                progressColor
            )
            a.recycle()
        }
        mProgress = if (mProgress > max) max else mProgress
        mProgress = if (mProgress < 0) 0 else mProgress
        mSweepAngle = if (mSweepAngle > 360) 360 else mSweepAngle
        mSweepAngle = if (mSweepAngle < 0) 0 else mSweepAngle
        mProgressSweep = mProgress.toFloat() / max * mSweepAngle
        mStartAngle = if (mStartAngle > 360) 0 else mStartAngle
        mStartAngle = if (mStartAngle < 0) 0 else mStartAngle
        mArcPaint = Paint()
        mArcPaint!!.color = arcColor
        mArcPaint!!.isAntiAlias = true
        mArcPaint!!.style = Paint.Style.STROKE
        mArcPaint!!.strokeWidth = this.mArcWidth.toFloat()

        mProgressPaint = Paint()
        mProgressPaint!!.color = progressColor
        mProgressPaint!!.isAntiAlias = true
        mProgressPaint!!.style = Paint.Style.STROKE
        mProgressPaint!!.strokeWidth = this.mArcWidth.toFloat()
        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isClockwise) {
            canvas.scale(-1f, 1f, mArcRect.centerX(), mArcRect.centerY())
        }

        // Draw the arcs
        val arcStart: Int = mStartAngle + mAngleOffset + mRotation
        val arcSweep: Int = mSweepAngle
        canvas.drawArc(mArcRect, arcStart.toFloat(), arcSweep.toFloat(), false, mArcPaint!!)
        if (mProgress > 0)
            canvas.drawArc(mArcRect, arcStart.toFloat(), mProgressSweep, false, mProgressPaint!!)
        if (mEnabled) {
            // Draw the thumb nail
            canvas.translate(
                (mTranslateX - mThumbXPos).toFloat(),
                (mTranslateY - mThumbYPos).toFloat()
            )
            mThumb!!.draw(canvas)
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
        val min: Int = min(width, height)
        mTranslateX = (width * 0.5f).toInt()
        mTranslateY = (height * 0.5f).toInt()
        val arcDiameter = min - paddingLeft
        mArcRadius = arcDiameter / 2
        val top = (height / 2 - (arcDiameter / 2)).toFloat()
        val left = (width / 2 - (arcDiameter / 2)).toFloat()
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter)
        val arcStart: Int = mProgressSweep.toInt() + mStartAngle + mRotation + 90
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
        if (mThumb != null && mThumb!!.isStateful) {
            val state: IntArray = drawableState
            mThumb!!.state = state
        }
        invalidate()
    }

    private fun onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStopTrackingTouch(this)
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
        onProgressRefresh(progress, true)
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
        angle -= mStartAngle.toDouble()
        return angle
    }

    private fun getProgressForAngle(angle: Double): Int {
        var touchProgress: Int = (valuePerDegree() * angle).roundToLong().toInt()
        touchProgress = if (touchProgress < 0) 0 else touchProgress
        touchProgress = if (touchProgress > max) max else touchProgress


        return touchProgress
    }

    private fun valuePerDegree(): Float {
        return max.toFloat() / mSweepAngle
    }

    private fun onProgressRefresh(progress: Int, fromUser: Boolean) {
        updateProgress(progress, fromUser)
    }

    private fun updateThumbPosition() {
        val thumbAngle: Int = (mStartAngle + mProgressSweep + mRotation + 90).toInt()
        mThumbXPos = (mArcRadius * cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
    }

    private fun updateProgress(progress: Int, fromUser: Boolean) {
        var progress: Int = progress
        if (progress == INVALID_PROGRESS_VALUE) {
            return
        }
        progress = if (progress > max) max else progress
        progress = if (progress < 0) 0 else progress
        mProgress = progress
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!
                .onProgressChanged(this, progress, fromUser)
        }
        mProgressSweep = progress.toFloat() / max * mSweepAngle
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
     * @see SeekArc.OnSeekArcChangeListener
     */
    fun setOnSeekArcChangeListener(l: OnSeekArcChangeListener?) {
        mOnSeekArcChangeListener = l
    }
    fun setRoundedEdges(isEnabled: Boolean) {
        mRoundedEdges = isEnabled
        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        } else {
            mArcPaint!!.strokeCap = Paint.Cap.SQUARE
            mProgressPaint!!.strokeCap = Paint.Cap.SQUARE
        }
    }

    fun setTouchInSide(isEnabled: Boolean) {
        val thumbHalfHeight: Int = mThumb!!.intrinsicHeight / 2
        val thumbHalfWidth: Int = mThumb!!.intrinsicWidth / 2
        mTouchInside = isEnabled
        mTouchIgnoreRadius = if (mTouchInside) {
            mArcRadius.toFloat() / 4
        } else {
            // Don't use the exact radius makes interaction too tricky
            mArcRadius - min(thumbHalfWidth, thumbHalfHeight).toFloat()
        }
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    var progress: Int
        get() {
            return mProgress
        }
        set(progress) {
            updateProgress(progress, false)
        }
    var progressWidth: Int
        get() {
            return mProgressWidth
        }
        set(mProgressWidth) {
            this.mProgressWidth = mProgressWidth
            mProgressPaint!!.strokeWidth = mProgressWidth.toFloat()
        }
    var arcWidth: Int
        get() {
            return this.mArcWidth
        }
        set(mArcWidth) {
            this.mArcWidth = mArcWidth
            mArcPaint!!.strokeWidth = mArcWidth.toFloat()
        }
    var arcRotation: Int
        get() {
            return mRotation
        }
        set(mRotation) {
            this.mRotation = mRotation
            updateThumbPosition()
        }
    var startAngle: Int
        get() {
            return mStartAngle
        }
        set(mStartAngle) {
            this.mStartAngle = mStartAngle
            updateThumbPosition()
        }
    var sweepAngle: Int
        get() {
            return mSweepAngle
        }
        set(mSweepAngle) {
            this.mSweepAngle = mSweepAngle
            updateThumbPosition()
        }
    var progressColor: Int
        get() {
            return mProgressPaint!!.color
        }
        set(color) {
            mProgressPaint!!.color = color
            invalidate()
        }
    var arcColor: Int
        get() {
            return mArcPaint!!.color
        }
        set(color) {
            mArcPaint!!.color = color
            invalidate()
        }
    var haloRadius: Int
        get() {
            TODO()
        }
        set(value) {

        }

    var haloColor: ColorStateList
        get() {
            TODO()
        }
        set(value) {

        }

    var thumb: Drawable?
        get() {
            return mThumb
        }
        set(value) {
            mThumb = value
            val thumbHalfHeight = value!!.intrinsicHeight / 2
            val thumbHalfWidth = value.intrinsicWidth / 2
            value.setBounds(
                -thumbHalfWidth, -thumbHalfHeight, thumbHalfWidth,
                thumbHalfHeight
            )
        }

    companion object {
        private val TAG: String = SeekArc::class.java.simpleName
        private val INVALID_PROGRESS_VALUE: Int = -1

        // The initial rotational offset -90 means we start at 12 o'clock
        private val mAngleOffset: Int = -90
    }
}