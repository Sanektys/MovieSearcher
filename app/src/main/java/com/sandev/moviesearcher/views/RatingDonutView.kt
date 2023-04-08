package com.sandev.moviesearcher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.postDelayed
import com.sandev.moviesearcher.R


class RatingDonutView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : View(context, attrs) {
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
    private var displayingProgress = 0
    private var interpolatedProgress = 0f

    private var isStaticElementsDrawn = false
    private var isAllElementsDrawn = false
    private var isAnimationRunning = false

    private lateinit var strokePaint: Paint
    private lateinit var digitPaint: Paint
    private lateinit var outerCirclePaint: Paint
    private lateinit var innerCirclePaint: Paint

    private lateinit var staticPartBitmap: Bitmap
    private lateinit var dynamicPartBitmap: Bitmap

    companion object {
        private val decelerateInterpolator = DecelerateInterpolator(3.0f)

        private const val ANIMATION_ITERATION_DELAY = 16L
        private const val DEGREES_PER_ONE_POINT: Float = 360f / 100
        private const val DIGITS_VERTICAL_POSITION_CORRECTION = 1F
        private const val DIGITS_HORIZONTAL_POSITION_CORRECTION = 0.95F
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

        displayingProgress = factualProgress
        interpolatedProgress = factualProgress.toFloat()

        initPaint()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = if (w > h) h / 2f else w / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val minSide = chosenWidth.coerceAtMost(chosenHeight)
        centerX = minSide / 2f
        centerY = minSide / 2f

        setMeasuredDimension(minSide, minSide)
    }

    override fun onDraw(canvas: Canvas) {
        drawRating(canvas)
        drawText(canvas)
    }

    fun setProgress(progress: Int) {
        post {
            factualProgress = progress
            displayingProgress = 0
            interpolatedProgress = 0f

            isAllElementsDrawn = false

            if (!isAnimationRunning) {
                isAnimationRunning = true
                loadingProgressAnimation()
            }
        }
    }

    private fun chooseDimension(mode: Int, size: Int) = when (mode) {
        MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> size
        else -> resources.getInteger(R.integer.rating_view_minimum_size)
    }

    private fun drawRating(canvas: Canvas) {
        canvas.save()

        if (!isStaticElementsDrawn) {
            staticPartBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(staticPartBitmap)
            bitmapCanvas.translate(centerX, centerY)

            val innerRing = radius * strokeOffsetAttr
            oval.set(0f - innerRing, 0f - innerRing, innerRing, innerRing)

            bitmapCanvas.drawCircle(0f, 0f, radius, outerCirclePaint)
            bitmapCanvas.drawCircle(0f, 0f, innerRing, innerCirclePaint)

            isStaticElementsDrawn = true
        }
        canvas.drawBitmap(staticPartBitmap, 0f, 0f, null)

        canvas.translate(centerX, centerY)
        canvas.drawArc(oval, -90f, convertProgressToDegrees(interpolatedProgress), false, strokePaint)

        canvas.restore()
    }

    private fun convertProgressToDegrees(progress: Float) = progress * DEGREES_PER_ONE_POINT

    private fun drawText(canvas: Canvas) {
        val message = if (displayingProgress < 100) {
            String.format("%.1f", interpolatedProgress / 10f)
        } else {
            "10"
        }

        digitPaint.getTextBounds(message, 0, message.length, textRect)
        canvas.drawText(
            message,
            (centerX - textRect.width() / 2f) * DIGITS_HORIZONTAL_POSITION_CORRECTION,
            (centerY + textRect.height() / 2f) * DIGITS_VERTICAL_POSITION_CORRECTION,
            digitPaint
        )
    }

    private fun loadingProgressAnimation() {
        fun increaseProgress() {
            ++displayingProgress
            interpolatedProgress = decelerateInterpolator
                .getInterpolation(displayingProgress / factualProgress.toFloat()) * factualProgress
            updatePaintsColors()
            invalidate()

            if (displayingProgress < factualProgress) {
                postDelayed(ANIMATION_ITERATION_DELAY) { increaseProgress() }
            } else {
                isAnimationRunning = false
            }
        }

        increaseProgress()
    }

    private fun initPaint() {
        strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthAttr
            setShadowLayer(1f, 0f, 0f, elementsShadowColorAttr)
            color = getPaintColor(interpolatedProgress.toInt())
            isAntiAlias = true
        }
        digitPaint = Paint().apply {
            style = Paint.Style.FILL
            setShadowLayer(2f, 0f, 0f, elementsShadowColorAttr)
            textSize = digitsSizeAttr
            typeface = Typeface.DEFAULT_BOLD
            color = getPaintColor(interpolatedProgress.toInt())
            isAntiAlias = true
        }
        outerCirclePaint = Paint().apply {
            style = Paint.Style.FILL
            color = outerBackgroundColorAttr
        }
        innerCirclePaint = Paint().apply {
            style = Paint.Style.FILL
            color = innerBackgroundColorAttr
        }
    }

    private fun updatePaintsColors() {
        strokePaint.color = getPaintColor(interpolatedProgress.toInt())
        digitPaint.color = getPaintColor(interpolatedProgress.toInt())
    }

    private fun getPaintColor(progress: Int) = when (progress) {
        in 0 .. 19 -> resources.getColor(R.color.rating_color_awful, context.theme)
        in 20 .. 39 -> resources.getColor(R.color.rating_color_bad, context.theme)
        in 40 .. 59 -> resources.getColor(R.color.rating_color_neutral, context.theme)
        in 60 .. 79 -> resources.getColor(R.color.rating_color_good, context.theme)
        in 80 .. 100 -> resources.getColor(R.color.rating_color_excellent, context.theme)
        else -> Color.DKGRAY
    }
}