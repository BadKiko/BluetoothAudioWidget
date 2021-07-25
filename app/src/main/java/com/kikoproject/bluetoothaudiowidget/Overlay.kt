package com.kikoproject.bluetoothaudiowidget

import android.animation.ValueAnimator
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_overlay.view.*
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifOptions
import java.lang.Math.abs


/**
 * @author aminography
 */
class Overlay constructor(private val context: Context) {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = (context.getSystemService(WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var floatView: View =
        LayoutInflater.from(context).inflate(R.layout.activity_overlay, null)

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    private var isShowing = false
    private var touchConsumedByMove = false

    private val onTouchListener = View.OnTouchListener { view, event ->
        val totalDeltaX = lastX - firstX
        val totalDeltaY = lastY - firstY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
            }
            MotionEvent.ACTION_UP -> {
                view.performClick()
                if(floatView.alpha>=0.75f){
                    val va = ValueAnimator.ofFloat(floatView.alpha, 1f)
                    va.duration = 250
                    va.addUpdateListener { animation -> animation.animatedValue as Float
                        floatView.alpha = animation.animatedValue as Float
                    }
                    va.start()

                    val vt = ValueAnimator.ofFloat(floatView.y, 0f)
                    vt.duration = 250
                    vt.addUpdateListener { animation -> animation.animatedValue as Float
                        floatView.y = animation.animatedValue as Float
                    }
                    vt.start()
                }
                else if(floatView.alpha< 0.75f){
                    val va = ValueAnimator.ofFloat(floatView.alpha, 0f)
                    va.duration = 250
                    va.addUpdateListener { animation -> animation.animatedValue as Float
                        floatView.alpha = animation.animatedValue as Float
                    }
                    va.start()

                    val vt = ValueAnimator.ofFloat(floatView.y, 500f)
                    vt.duration = 250
                    vt.addUpdateListener { animation -> animation.animatedValue as Float
                        floatView.y = animation.animatedValue as Float
                        if(animation.animatedValue == 500f){
                            floatView.visibility = View.INVISIBLE
                            windowManager?.removeView(floatView)
                            isShowing = false
                        }
                    }
                    vt.start()
                }

            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX.toInt() - lastX
                val deltaY = event.rawY.toInt() - lastY
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()


                if ( deltaY >= 1) {
                    if (event.pointerCount == 1) {
                        layoutParams.y += deltaY
                        touchConsumedByMove = true
                        floatView.alpha-=deltaY.toFloat()/500f
                        floatView.y+=deltaY
                        if(floatView.alpha<=0.05f){
                            floatView.visibility = View.INVISIBLE
                            windowManager?.removeView(floatView)
                            isShowing = false
                        }
                        /*windowManager?.apply {
                            updateViewLayout(floatView, layoutParams)
                        }*/
                    } else {
                        touchConsumedByMove = false
                    }
                }
                else if( deltaY <= -1){
                    if(floatView.alpha<=1){
                        floatView.alpha+=abs(deltaY.toFloat())/500f
                    }
                    if(floatView.y>5){
                        floatView.y+=deltaY
                    }
                }
                else {
                    touchConsumedByMove = false
                }
            }
            else -> {
            }
        }
        touchConsumedByMove
    }

    init {
        with(floatView) {


            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            val sharedPref = context.getSharedPreferences("IMain", 0)
            nameOfHeadset.setText(sharedPref.getString("name", "NoNamed"))
            linecontact.setOnClickListener { dismiss() }

            val animSet = AnimationSet(true)
            animSet.setFillEnabled(true)
            val showpananim_trans = windowManager?.defaultDisplay?.height?.let { TranslateAnimation(0f,0f, it.toFloat(),0f) }
            showpananim_trans?.duration = 500
            showpananim_trans?.fillAfter = true
            animSet.addAnimation(showpananim_trans)
            val showpananim_apha = AlphaAnimation(0f, 1f)
            showpananim_apha.duration = 500
            showpananim_apha.fillAfter = true
            animSet.addAnimation(showpananim_apha)

            batteryLevel.startAnimation(animSet)
            battery.startAnimation(animSet)
            linecontact.startAnimation(animSet)
            mainPanel.startAnimation(animSet)
            nameOfHeadset.startAnimation(animSet)
            line.startAnimation(animSet)
            gif.startAnimation(animSet)
        }

        floatView.setOnTouchListener(onTouchListener)

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            @Suppress("DEPRECATION")
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_TOAST
            }

            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    fun show() {
        if (context.canDrawOverlays) {
            dismiss()
            isShowing = true
            windowManager?.addView(floatView, layoutParams)
        }
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(floatView)
            isShowing = false
        }
    }

    fun setBatteryLevel(level: Int){
        with(floatView){
            batteryLevel.setText(level.toString()+"%")
            setBatteryImage(level)
        }
    }

    fun setBatteryImage(level: Int){
        with(floatView){
            var gf = GifDrawable.createFromResource(resources, R.drawable.battery_unknown)

            when(level){
                in 76..100->gf = GifDrawable.createFromResource(resources, R.drawable.battery_max)
                in 51..75->gf = GifDrawable.createFromResource(resources, R.drawable.battery_75)
                in 26..50->gf = GifDrawable.createFromResource(resources, R.drawable.battery_half)
                in 10..25->gf = GifDrawable.createFromResource(resources, R.drawable.battery_25)
                in 0..9->gf = GifDrawable.createFromResource(resources, R.drawable.battery_low)
                -1 -> gf = GifDrawable.createFromResource(resources, R.drawable.battery_unknown)
            }
            gf!!.loopCount=1
            battery.setImageDrawable(gf)
        }
    }
}