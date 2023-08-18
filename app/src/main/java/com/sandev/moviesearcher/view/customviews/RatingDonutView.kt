package com.sandev.moviesearcher.view.customviews

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Rect
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.postDelayed
import com.sandev.moviesearcher.R


class RatingDonutView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : View(context, attrs) {

    var isRatingAnimationEnabled: Boolean = false

    private val oval = RectF()
    private val textRect = Rect()

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private var strokeWidthAttr = 10f
    private var strokeOffsetAttr = 0.8f
    private var digitsSizeAttr = 60f

    private var outerBackgroundColorAttr = Color.DKGRAY
    private var innerBackgroundColorAttr = Color.LTGRAY
    private var elementsShadowColorAttr = Color.LTGRAY

    private var factualProgress = 0
    private var progressAnimation = 0
    private var displayingProgress = 0f
    private var progressAnimationStartOffset = 0

    private var isStaticElementsDrawn = false
    private var isAllElementsDrawn = false
    private var isAnimationRunning = false

    private val strokePaint = Paint()
    private val digitPaint  = Paint()
    private val outerCirclePaint = Paint()
    private val innerCirclePaint = Paint()

    private val staticPartBitmap by lazy(LazyThreadSafetyMode.NONE) { Bitmap.createBitmap(width, height ,Bitmap.Config.ARGB_8888) }
    private val allPartsBitmap   by lazy(LazyThreadSafetyMode.NONE) { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }
    private val staticPartCanvas by lazy(LazyThreadSafetyMode.NONE) { Canvas(staticPartBitmap) }
    private val allPartsCanvas   by lazy(LazyThreadSafetyMode.NONE) { Canvas(allPartsBitmap) }

    companion object {
        private val decelerateInterpolator = DecelerateInterpolator(1.6f)
        private val accelerateInterpolator = AccelerateInterpolator(1.2f)

        private const val FULL_PROGRESS = 100
        private const val PROGRESS_ANIMATION_START_OFFSET = 25
        private const val ANIMATION_INTERPOLATION_THRESHOLD = FULL_PROGRESS - PROGRESS_ANIMATION_START_OFFSET
        private const val ANIMATION_ITERATION_DELAY = 25L

        private const val DEGREES_PER_ONE_POINT: Float = 360f / FULL_PROGRESS

        private const val DIGITS_VERTICAL_POSITION_CORRECTION = 1F
        private const val DIGITS_HORIZONTAL_POSITION_CORRECTION = 0.95F

        private const val DIVIDER_TO_CENTER = 2F
        private const val DIVIDER_TO_DECIMAL = 10F
        private const val CIRCLE_START_ANGLE = -90F

        private const val PROGRESS_LINE_SHADOW_RADIUS = 1F
        private const val DIGITS_SHADOW_RADIUS = 2F

        private val RATING_RANGE_AWFUL      = 0 .. 19
        private val RATING_RANGE_BAD       = 20 .. 39
        private val RATING_RANGE_NEUTRAL   = 40 .. 59
        private val RATING_RANGE_GOOD      = 60 .. 79
        private val RATING_RANGE_EXCELLENT = 80 .. FULL_PROGRESS

        private var isColorsInitialized = false
        private var RATING_COLOR_AWFUL:     Int? = null
        private var RATING_COLOR_BAD:       Int? = null
        private var RATING_COLOR_NEUTRAL:   Int? = null
        private var RATING_COLOR_GOOD:      Int? = null
        private var RATING_COLOR_EXCELLENT: Int? = null
    }


    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.RatingDonutView,
            0, 0)
        try {
            outerBackgroundColorAttr = attributes.getColor(R.styleable.RatingDonutView_outer_background_color, outerBackgroundColorAttr)
            innerBackgroundColorAttr = attributes.getColor(R.styleable.RatingDonutView_inner_background_color, innerBackgroundColorAttr)
            elementsShadowColorAttr = attributes.getColor(R.styleable.RatingDonutView_elements_shadow_color, elementsShadowColorAttr)
            strokeWidthAttr = attributes.getDimension(R.styleable.RatingDonutView_stroke_width, strokeWidthAttr)
            strokeOffsetAttr = attributes.getFloat(R.styleable.RatingDonutView_stroke_offset, strokeOffsetAttr)
            digitsSizeAttr = attributes.getDimension(R.styleable.RatingDonutView_digits_size, digitsSizeAttr)
            factualProgress = attributes.getInt(R.styleable.RatingDonutView_progress, factualProgress)
        } finally {
            attributes.recycle()
        }

        progressAnimation = factualProgress
        displayingProgress = factualProgress.toFloat()

        if (!isColorsInitialized) {
            initColors()
        }

        initPaint()
    }


    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = if (width > height) {
            height / DIVIDER_TO_CENTER
        } else {
            width / DIVIDER_TO_CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val minSide = chosenWidth.coerceAtMost(chosenHeight)
        centerX = minSide / DIVIDER_TO_CENTER
        centerY = minSide / DIVIDER_TO_CENTER

        setMeasuredDimension(minSide, minSide)
    }

    override fun onDraw(canvas: Canvas) {
        if (isAllElementsDrawn) {
            canvas.drawBitmap(allPartsBitmap, 0f, 0f, null)
        } else if (isAnimationRunning) {
            drawRating(canvas)
            drawText(canvas)
        } else {
            drawRating(allPartsCanvas)
            drawText(allPartsCanvas)
            canvas.drawBitmap(allPartsBitmap, 0f, 0f, null)
            isAllElementsDrawn = true
        }
    }

    fun setProgress(progress: Int) {
        if (isRatingAnimationEnabled) {
            post {
                progressAnimationStartOffset = if (progress < ANIMATION_INTERPOLATION_THRESHOLD) {
                    (accelerateInterpolator.getInterpolation(progress.toFloat() / FULL_PROGRESS) *
                            FULL_PROGRESS).toInt()
                } else {
                    progress - PROGRESS_ANIMATION_START_OFFSET
                }

                factualProgress = progress
                progressAnimation = progressAnimationStartOffset
                displayingProgress = 0f

                isAllElementsDrawn = false

                if (!isAnimationRunning) {
                    isAnimationRunning = true
                    loadingProgressAnimation()
                }
            }
        } else {
            instantProgressDrawing(progress)
        }
    }

    private fun chooseDimension(mode: Int, size: Int) = when (mode) {
        MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> size
        else -> resources.getInteger(R.integer.rating_view_minimum_size)
    }

    private fun drawRating(canvas: Canvas) {
        canvas.save()

        if (!isStaticElementsDrawn) {
            staticPartCanvas.translate(centerX, centerY)

            val innerRing = radius * strokeOffsetAttr
            oval.set(0f - innerRing, 0f - innerRing, innerRing, innerRing)

            staticPartCanvas.drawCircle(0f, 0f, radius, outerCirclePaint)
            staticPartCanvas.drawCircle(0f, 0f, innerRing, innerCirclePaint)

            isStaticElementsDrawn = true
        }
        canvas.drawBitmap(staticPartBitmap, 0f, 0f, null)

        canvas.translate(centerX, centerY)
        canvas.drawArc(oval, CIRCLE_START_ANGLE, convertProgressToDegrees(displayingProgress),
            false, strokePaint)

        canvas.restore()
    }

    private fun convertProgressToDegrees(progress: Float) = progress * DEGREES_PER_ONE_POINT

    private fun drawText(canvas: Canvas) {
        val message = if (progressAnimation < FULL_PROGRESS) {
            String.format("%.1f", displayingProgress / DIVIDER_TO_DECIMAL)
        } else {
            "10"
        }

        digitPaint.getTextBounds(message, 0, message.length, textRect)
        canvas.drawText(
            message,
            (centerX - textRect.width() / DIVIDER_TO_CENTER) * DIGITS_HORIZONTAL_POSITION_CORRECTION,
            (centerY + textRect.height() / DIVIDER_TO_CENTER) * DIGITS_VERTICAL_POSITION_CORRECTION,
            digitPaint
        )
    }

    private fun loadingProgressAnimation() {
        fun increaseProgress() {
            val progressAnimationSpan = (factualProgress - progressAnimationStartOffset).toFloat()
            displayingProgress = progressAnimationStartOffset + decelerateInterpolator.getInterpolation(
                    (progressAnimation - progressAnimationStartOffset) / progressAnimationSpan
                ) * progressAnimationSpan

            updatePaintsColors()
            invalidate()

            if (progressAnimation < factualProgress) {
                ++progressAnimation
                postDelayed(ANIMATION_ITERATION_DELAY) { increaseProgress() }
            } else {
                isAnimationRunning = false
            }
        }

        increaseProgress()
    }

    private fun instantProgressDrawing(progress: Int) {
        isAnimationRunning = false
        isAllElementsDrawn = false

        progressAnimation = progress
        displayingProgress = progress.toFloat()

        updatePaintsColors()
        invalidate()
    }

    private fun initPaint() {
        strokePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthAttr
            setShadowLayer(PROGRESS_LINE_SHADOW_RADIUS, 0f, 0f, elementsShadowColorAttr)
            color = getPaintColor(displayingProgress.toInt())
            isAntiAlias = true
        }
        digitPaint.apply {
            style = Paint.Style.FILL
            setShadowLayer(DIGITS_SHADOW_RADIUS, 0f, 0f, elementsShadowColorAttr)
            textSize = digitsSizeAttr
            typeface = Typeface.DEFAULT_BOLD
            color = getPaintColor(displayingProgress.toInt())
            isAntiAlias = true
        }
        outerCirclePaint.apply {
            style = Paint.Style.FILL
            color = outerBackgroundColorAttr
            isAntiAlias = true
        }
        innerCirclePaint.apply {
            style = Paint.Style.FILL
            color = innerBackgroundColorAttr
        }
    }

    private fun initColors() {
        RATING_COLOR_AWFUL     = RATING_COLOR_AWFUL     ?: resources.getColor(R.color.rating_color_awful, context.theme)
        RATING_COLOR_BAD       = RATING_COLOR_BAD       ?: resources.getColor(R.color.rating_color_bad, context.theme)
        RATING_COLOR_NEUTRAL   = RATING_COLOR_NEUTRAL   ?: resources.getColor(R.color.rating_color_neutral, context.theme)
        RATING_COLOR_GOOD      = RATING_COLOR_GOOD      ?: resources.getColor(R.color.rating_color_good, context.theme)
        RATING_COLOR_EXCELLENT = RATING_COLOR_EXCELLENT ?: resources.getColor(R.color.rating_color_excellent, context.theme)

        isColorsInitialized = true
    }

    private fun updatePaintsColors() {
        strokePaint.color = getPaintColor(displayingProgress.toInt())
        digitPaint.color = getPaintColor(displayingProgress.toInt())
    }

    private fun getPaintColor(progress: Int) = when (progress) {
        in RATING_RANGE_AWFUL     -> RATING_COLOR_AWFUL!!
        in RATING_RANGE_BAD       -> RATING_COLOR_BAD!!
        in RATING_RANGE_NEUTRAL   -> RATING_COLOR_NEUTRAL!!
        in RATING_RANGE_GOOD      -> RATING_COLOR_GOOD!!
        in RATING_RANGE_EXCELLENT -> RATING_COLOR_EXCELLENT!!
        else -> Color.DKGRAY
    }
}