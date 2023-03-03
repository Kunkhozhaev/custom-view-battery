package ru.nurdaulet.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private const val BATTERY_DEFAULT_PERCENT = 50
private const val BATTERY_CRITICAL_PERCENT = 30
private const val BATTERY_WIDTH = 200f
private const val BATTERY_HEIGHT = 100f
private const val BATTERY_ZERO_COORDINATE = 0f
private const val PAINT_BRUSH_STROKE_WIDTH = 3f
private const val MAIN_CONTENT_OFFSET = 4f
private const val BATTERY_WARNING_COLOR = Color.RED
private const val BATTERY_DEFAULT_COLOR = Color.GREEN

class BatteryChargeState
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.batteryChargeStateStyle,
    defStyleRs: Int = R.style.BatteryChargeStateStyle
) : View(context, attrs, defStyleAttr, defStyleRs) {

    private var batteryPercent = 0
    private var batteryCriticalPercent = 0
    private var mainContentOffset = context.toDp(MAIN_CONTENT_OFFSET).toInt()


    private lateinit var backgroundRect: Rect
    private lateinit var batteryLevelRect: Rect

    private val batteryStrokeColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }

    private val batteryPercentColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }

    init {
        attrs?.let { initAttrs(it, defStyleAttr, defStyleRs) }
        initRectangles()
    }

    private fun initAttrs(attrs: AttributeSet, defStyleAttr: Int, defStyleRs: Int) {
        context.theme
            .obtainStyledAttributes(attrs, R.styleable.BatteryChargeState, defStyleAttr, defStyleRs)
            .apply {
                try {
                    batteryPercent = getInteger(
                        R.styleable.BatteryChargeState_batteryPercent,
                        BATTERY_DEFAULT_PERCENT
                    )
                    batteryCriticalPercent = getInteger(
                        R.styleable.BatteryChargeState_batteryCriticalPercent,
                        BATTERY_CRITICAL_PERCENT
                    )

                    batteryPercentColor.color = getColor(
                        R.styleable.BatteryChargeState_batteryPercentColor,
                        BATTERY_DEFAULT_COLOR
                    )
                    batteryStrokeColor.color =
                        getColor(R.styleable.BatteryChargeState_batteryStrokeColor, Color.GRAY)
                } finally {
                    recycle()
                }
            }
    }

    private fun initRectangles() {
        backgroundRect = Rect(
            context.toDp(BATTERY_ZERO_COORDINATE).toInt(),
            context.toDp(BATTERY_ZERO_COORDINATE).toInt(),
            context.toDp(BATTERY_WIDTH).toInt(),
            context.toDp(BATTERY_HEIGHT).toInt(),
        )

        batteryLevelRect = Rect(
            backgroundRect.left + mainContentOffset,
            backgroundRect.top + mainContentOffset,
            ((backgroundRect.right - mainContentOffset) *
                    (this.batteryPercent.toDouble() / 100.toDouble())).toInt(),
            backgroundRect.bottom - mainContentOffset

        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = context.toDp(BATTERY_WIDTH).toInt()
        val desiredHeight = context.toDp(BATTERY_HEIGHT).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec) + paddingLeft + paddingRight,
            resolveSize(desiredHeight, heightMeasureSpec) + paddingTop + paddingBottom
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

        drawBattery(canvas)
        drawBatteryPercent(canvas)
    }

    private fun drawBattery(canvas: Canvas?) {
        canvas?.drawRect(backgroundRect, batteryStrokeColor)
    }

    private fun drawBatteryPercent(canvas: Canvas?) {

        if (batteryPercent == 0) {
            drawEmptyBattery(canvas)
        } else {
            canvas?.drawRect(batteryLevelRect, batteryPercentColor)
        }
    }

    private fun drawEmptyBattery(canvas: Canvas?) {
        // As I should draw fully empty battery, there is no point to convert values .toDp()
        canvas?.drawRect(
            0f,
            0f,
            0f,
            0f,
            batteryPercentColor
        )
    }

    fun setBatteryPercent(percent: Int) {

        batteryPercent = percent.coerceIn(0, 100)

        if (batteryPercent <= batteryCriticalPercent) {
            batteryPercentColor.color = BATTERY_WARNING_COLOR
        } else {
            batteryPercentColor.color = BATTERY_DEFAULT_COLOR
        }

        initRectangles()
        requestLayout()
        invalidate()
    }

    private fun Context.toDp(value: Float): Float {
        return resources.displayMetrics.density * value
    }


    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()

        return SavedState(
            batteryPercent,
            state
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as SavedState
        super.onRestoreInstanceState(state.superState)
        setBatteryPercent(state.batteryPercent)
    }

    @Parcelize
    class SavedState(
        val batteryPercent: Int,
        @IgnoredOnParcel val source: Parcelable? = null
    ) : BaseSavedState(source)
}
