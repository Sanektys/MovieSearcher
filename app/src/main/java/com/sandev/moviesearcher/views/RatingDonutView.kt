package com.sandev.moviesearcher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
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
    private var backgroundColorAttr = Color.DKGRAY
    private var elementsShadowColorAttr = Color.LTGRAY

    private var progress = 50
    private var isStaticElementsDrawn = false
    private var isAllElementsDrawn = false

    private lateinit var strokePaint: Paint
    private lateinit var digitPaint: Paint
    private lateinit var circlePaint: Paint

    private lateinit var viewBitmap: Bitmap

    companion object {
        const val DEGREES_PER_ONE_POINT: Float = 360f / 100
        const val DIGITS_VERTICAL_POSITION_CORRECTION = 1F
        const val DIGITS_HORIZONTAL_POSITION_CORRECTION = 0.95F
    }


    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.RatingDonutView,
            0, 0)
        try {
            backgroundColorAttr = attributes.getColor(R.styleable.RatingDonutView_background_color, backgroundColorAttr)
            elementsShadowColorAttr = attributes.getColor(R.styleable.RatingDonutView_elements_shadow_color, elementsShadowColorAttr)
            strokeWidthAttr = attributes.getDimension(R.styleable.RatingDonutView_stroke_width, strokeWidthAttr)
            strokeOffsetAttr = attributes.getFloat(R.styleable.RatingDonutView_stroke_offset, strokeOffsetAttr)
            digitsSizeAttr = attributes.getDimension(R.styleable.RatingDonutView_digits_size, digitsSizeAttr)
            progress = attributes.getInt(R.styleable.RatingDonutView_progress, progress)
        } finally {
            attributes.recycle()
        }

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
        this.progress = progress

        updatePaintsColors()
        invalidate()
    }

    private fun chooseDimension(mode: Int, size: Int) = when (mode) {
        MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> size
        else -> resources.getInteger(R.integer.rating_view_minimum_size)
    }

    private fun drawRating(canvas: Canvas) {
        canvas.save()

        if (!isStaticElementsDrawn) {
            viewBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(viewBitmap)
            bitmapCanvas.translate(centerX, centerY)

            val ringOffset = radius * strokeOffsetAttr
            oval.set(0f - ringOffset, 0f - ringOffset, ringOffset, ringOffset)

            bitmapCanvas.drawCircle(0f, 0f, radius, circlePaint)

            isStaticElementsDrawn = true
        }
        canvas.drawBitmap(viewBitmap, 0f, 0f, null)

        canvas.translate(centerX, centerY)
        canvas.drawArc(oval, -90f, convertProgressToDegrees(progress), false, strokePaint)

        canvas.restore()
    }

    private fun convertProgressToDegrees(progress: Int) = progress * DEGREES_PER_ONE_POINT

    private fun drawText(canvas: Canvas) {
        val message = if (progress < 100) {
            String.format("%.1f", progress / 10f)
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

    private fun initPaint() {
        strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthAttr
            setShadowLayer(1f, 0f, 0f, elementsShadowColorAttr)
            color = getPaintColor(progress)
            isAntiAlias = true
        }
        digitPaint = Paint().apply {
            style = Paint.Style.FILL
            setShadowLayer(2f, 0f, 0f, elementsShadowColorAttr)
            textSize = digitsSizeAttr
            typeface = Typeface.DEFAULT_BOLD
            color = getPaintColor(progress)
            isAntiAlias = true
        }
        circlePaint = Paint().apply {
            style = Paint.Style.FILL
            color = backgroundColorAttr
        }
    }

    private fun updatePaintsColors() {
        strokePaint.color = getPaintColor(progress)
        digitPaint.color = getPaintColor(progress)
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