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
val rFactor : Float = 5f
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

    override fun  onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

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
}