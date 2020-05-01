package com.anwesh.uiprojects.ballexpandtoedgeview

/**
 * Created by anweshmishra on 02/05/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val parts : Int = 4
val sizeFactor : Float = 2.9f
val rFactor : Float = 4f
val scGap : Float = 0.02f / parts
val delay : Long = 20
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i  * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBallExpandToEdge(i : Int, scale : Float, size : Float, w : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sc1 : Float = sf.divideScale(0, parts)
    val sc2 : Float = sf.divideScale(1, parts)
    val sc3 : Float = sf.divideScale(2, parts)
    val sc4 : Float = sf.divideScale(3, parts)
    val r : Float = size / rFactor
    save()
    rotate(90f * sc3)
    scale(1f, 1f - 2 * i)
    drawCircle(0f, size * sc2 + (w / 2 - size - r) * sc4, r * sc1, paint)
    restore()
}

fun Canvas.drawBallsExpandToEdge(scale : Float, size : Float, w : Float, paint : Paint) {
    for (j in 0..1) {
        save()
        drawBallExpandToEdge(j, scale, size, w, paint)
        restore()
    }
}

fun Canvas.drawBEENode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    save()
    translate(w / 2, gap * (i + 1))
    drawBallsExpandToEdge(scale, size, w, paint)
    restore()
}

class BallExpandToEdgeView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun  onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BEENode(var i : Int, val state : State = State()) {

        private var next : BEENode? = null
        private var prev : BEENode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BEENode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBEENode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BEENode {
            var curr : BEENode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BallExpandToEdge(var i : Int) {

        private var curr : BEENode = BEENode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BallExpandToEdgeView) {

        private val animator : Animator = Animator(view)
        private val bee : BallExpandToEdge = BallExpandToEdge(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bee.draw(canvas, paint)
            animator.animate {
                bee.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bee.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BallExpandToEdgeView {
            val view : BallExpandToEdgeView = BallExpandToEdgeView(activity)
            activity.setContentView(view)
            return view
        }
    }
}