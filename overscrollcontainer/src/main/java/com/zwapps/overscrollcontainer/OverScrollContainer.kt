package com.zwapps.overscrollcontainer

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * OverScrollContainer contains a child ViewGroup and enable over-scroll functionality
 * - Enable over-scroll with friction if child is at top and is scrolling up
 * - Disable over-scroll and do regular scroll if child is not at top or is scrolling down
 * - Bounce if release touch or swipe up
 * - Slow down and bounce if swipe down
 * */
class OverScrollContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var frictionRate = 0.25F
    var bounceAccelerator = 0.1F
    var slowDownDecelerator = 0.3F

    val child: ViewGroup by lazy {
        val v = if (childCount > 0) getChildAt(0) as? ViewGroup else null
        requireNotNull(v) { "OverScrollContainer must contain a ViewGroup" }
    }

    var overScrollListener: (() -> Unit)? = null

    private val firstItemView: View?
        get() = if (child.childCount > 0) child.getChildAt( 0) else null

    private var initY = 0F
    private var prevY = 0F
    private var prevTime = 0L
    private var isOverScrolling: Boolean? = null
    private var isBouncing = false

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.OverScrollContainer)

            frictionRate = ta.getFloat(R.styleable.OverScrollContainer_frictionRate, 0.25F)
            bounceAccelerator = ta.getFloat(R.styleable.OverScrollContainer_bounceAccelerator, 0.1F)
            slowDownDecelerator = ta.getFloat(R.styleable.OverScrollContainer_slowDownDecelerator, 0.3F)

            ta.recycle()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isBouncing) return true

        val isHandled = when (ev?.action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(ev)
            MotionEvent.ACTION_MOVE -> handleActionMove(ev)
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel(ev)
            else -> false
        }

        return if (isHandled) true else super.dispatchTouchEvent(ev)
    }

    private fun handleActionDown(ev: MotionEvent): Boolean {
        initY = ev.y
        prevY = initY
        prevTime = ev.eventTime
        return false
    }

    private fun handleActionMove(ev: MotionEvent): Boolean {
        if (isOverScrolling == null) {
            isOverScrolling = !child.canScrollVertically(-1) && // Must scroll to top
                ev.y >= firstItemView?.top ?: 0 &&  // Must touch item area
                ev.y > initY    // Must be scrolling up (pulling)
        }

        if (isOverScrolling == false) {
            return false
        }

        // Do not exceed original position when over-pulling
        child.translationY = max(ev.y - initY, 0F) * frictionRate
        overScrollListener?.invoke()

        return true
    }

    private fun handleActionUpOrCancel(ev: MotionEvent): Boolean {
        if (isOverScrolling == true && ev.y != initY) {
            val tY = child.translationY
            val animator = if (ev.y <= prevY) {
                createBounceAnimator(child, tY)
            } else {
                createSlowDownAndBounceAnimator(child, ev, tY)
            }

            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {}

                override fun onAnimationStart(p0: Animator?) {
                    isBouncing = true
                }

                override fun onAnimationEnd(p0: Animator?) {
                    isBouncing = false
                    overScrollListener?.invoke()
                }

                override fun onAnimationCancel(p0: Animator?) {
                    isBouncing = false
                }
            })

            animator.start()
        }

        isOverScrolling = null
        return false
    }

    private fun createBounceAnimator(view: View, startY: Float): Animator {
        // Va: V average, Vs: V start, Ve: V end, a: accelerator, t: time, d: distance
        // Va = (Vs + Ve) / 2, Ve = Vs + a*t, Vs = 0 -> Va = a*t / 2
        // d = Va*t = a*t^2 / 2 -> t = sqrt(2*d / a)
        val time = sqrt(2 * startY / bounceAccelerator)

        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0F).apply {
            duration = time.roundToLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener { overScrollListener?.invoke() }
        }
    }

    private fun createSlowDownAndBounceAnimator(view: View, ev: MotionEvent, startY: Float): Animator {
        assert(ev.y > prevY)

        val vStart = (ev.y - prevY) / (ev.eventTime - prevTime)
        // Va: V average, Vs: V start, Ve: V end, a: accelerator, t: time, d: distance
        // t = (Ve - Vs) / -a, Ve = 0 -> t = Vs / a
        val time = vStart / slowDownDecelerator
        // d = Va*t, Va = (Vs + Ve) / 2, t = (Ve - Vs) / -a -> d = (Ve^2 - Vs^2) / -a*2
        // Ve = 0 -> d = Vs^2 / 2*a
        val tYExtra = vStart * vStart / (2 * slowDownDecelerator)

        val slowDownAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY + tYExtra).apply {
            duration = time.roundToLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener { overScrollListener?.invoke() }
        }

        val bounceAnimator = createBounceAnimator(view, startY + tYExtra)

        return AnimatorSet().apply { playSequentially(slowDownAnimator, bounceAnimator) }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isOverScrolling == true || isBouncing) true else super.onInterceptTouchEvent(ev)
    }
}