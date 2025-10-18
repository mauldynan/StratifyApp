package com.example.stratify

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // The percentage of positive reviews.
    var positivePercent: Float = 70f
    // The percentage of negative reviews.
    var negativePercent: Float = 30f
    // The total number of reviews.
    var totalReviews: Int = 1200

    // The width of the donut chart's stroke.
    private val strokeWidth = 15f

    // Paint for the background of the chart.
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.STROKE
        this.strokeWidth = this@DonutChartView.strokeWidth
    }

    // Paint for the positive percentage arc (Green for positive)
    private val positivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        this.strokeWidth = this@DonutChartView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    // Paint for the negative percentage arc (Red for positive)
    private val negativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        style = Paint.Style.STROKE
        this.strokeWidth = this@DonutChartView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    // Paint for the main text in the center of the chart.
    private val mainTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // Paint for the subtext in the center of the chart.
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 12f
        textAlign = Paint.Align.CENTER
    }

    // Paint for the positive percentage label.
    private val labelPositivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        textSize = 18f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }

    // Paint for the negative percentage label.
    private val labelNegativePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        textSize = 18f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }

    // The current sweep angle for the animation.
    private var animatedSweep = 0f

    init {
        // Enable software layer for better shadow rendering.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        // Set the background to transparent.
        setBackgroundColor(Color.TRANSPARENT)
        // Start the animation when the view is initialized.
        startAnimation()
    }

    /**
     * Starts the animation for the donut chart.
     */
    private fun startAnimation() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 1500
        animator.addUpdateListener {
            animatedSweep = it.animatedFraction
            invalidate() // Redraw the view on each animation frame.
        }
        animator.start()
    }

    /**
     * Draws the donut chart on the canvas.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate the center and radius of the chart.
        val cx = width / 4f
        val cy = height / 2f
        val radius = ((width.coerceAtMost(height) / 2f) - strokeWidth * 1.5f) * 0.9f
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw the background circle.
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Calculate the angles for the positive and negative arcs.
        val totalPercent = (positivePercent + negativePercent).coerceAtMost(100f)
        val positiveAngle = 360f * (positivePercent / totalPercent)
        val negativeAngle = 360f * (negativePercent / totalPercent)

        // Draw the positive (green) arc.
        canvas.drawArc(rect, 270f, positiveAngle * animatedSweep, false, positivePaint)
        // Draw the negative (red) arc.
        canvas.drawArc(rect, 270f + positiveAngle, negativeAngle * animatedSweep, false, negativePaint)

        // Calculate the vertical position for the center text.
        val mainTextY = cy - ((mainTextPaint.descent() + mainTextPaint.ascent()) / 2)
        val subTextY = cy + ((-mainTextPaint.descent() - mainTextPaint.ascent())/2) + subTextPaint.textSize

        // Draw the center text.
        canvas.drawText(totalReviews.toString(), cx, mainTextY, mainTextPaint)
        canvas.drawText("Total Review", cx, subTextY, subTextPaint)

        // Draw the labels on the right side of the chart.
        val rightTextX = cx + radius + 30f
        canvas.drawText("${positivePercent.toInt()}% Positive", rightTextX, cy - 10f, labelPositivePaint)
        canvas.drawText("${negativePercent.toInt()}% Negative", rightTextX, cy + 20f, labelNegativePaint)
    }
}
