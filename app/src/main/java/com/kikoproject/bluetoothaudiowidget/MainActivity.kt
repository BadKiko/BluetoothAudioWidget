package com.kikoproject.bluetoothaudiowidget

import android.animation.ValueAnimator
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.IntentCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.internal.RxBleLog
import kotlinx.android.synthetic.main.activity_main.*
import no.nordicsemi.android.ble.BleManager


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Отключение темного режима MIUI тобы не менялись цвета
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val sharedPref = getSharedPreferences("IMain", 0)
        val iv = ImageView(applicationContext)
        downTheMainTitle()
        val calibrbutton = findViewById<AppCompatButton>(R.id.calibrate)
        val switcher = findViewById<Switch>(R.id.switch1)
        val textOfSwitcher = findViewById<TextView>(R.id.onOffAudioWidget)


        // Первоначальная проверка на сохранения
        if(sharedPref.contains("name")){
            editTextTextPersonName.setText(sharedPref.getString("name", "NoNamed"))
        }

        if(sharedPref.contains("adress")){
            calibrate.setText("Список наушников")
        }
        else // Если наушники еще не были добавлены то убираем панели настройки
        {
            show_widgets.visibility = View.INVISIBLE
            headset_name.visibility = View.INVISIBLE
            //Toast.makeText(this, calibrate.y.toString(), Toast.LENGTH_SHORT).show()
            calibrate.y = -windowManager.defaultDisplay.height.toFloat()/1.75f
            //Toast.makeText(this, calibrate.y.toString(), Toast.LENGTH_SHORT).show()
        }
        // Первоначальная проверка на сохранения


        // При нажатии на кнопку добавления наушников
        calibrbutton.setOnClickListener{
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            CreateBlurBackground(iv, getScreenShot(mainRoot))

            //visible
            calibr.visibility = View.VISIBLE
            gif2.bringToFront()
            titleText7.bringToFront()
            titleText3.bringToFront()
            closeBtn.bringToFront()

            // Если у нас сопряжены наушники при нажатии на кнопку добавления выдаем предупреждение
            if (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)){
                //headsetLayout.bringToFront()
                titleText3.setText("Извините!")
                titleText7.setText("Вам необходимо переподключить вашу Bluetooth гарнитуру, чтобы программа смогла запомнить ее!")
                registerReceiver(registHeadsetB, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
                registerReceiver(headsetIsDiscB, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            }
            else
            {
                //Сделать добавление множества наушников если будетн е в лом

                // Если у нас есть зареганные наушники
                if(sharedPref.contains("buttonNum")) {

                    for (buttonInt: Int in 1..sharedPref.getInt("buttonNum", 1)) {
                        val btnHeadsets = Button(this)
                        val deleteButton = Button(this)

                        btnHeadsets.setLayoutParams(
                            LinearLayout.LayoutParams(
                                calibrate.layoutParams.width/2,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        )
                        deleteButton.setLayoutParams(
                            LinearLayout.LayoutParams(
                                calibrate.layoutParams.width/6,
                                calibrate.layoutParams.width/6
                            )
                        )
                        deleteButton.background = resources.getDrawable(R.drawable.trash_icon_128726)
                        deleteLayout.addView(deleteButton)

                        btnHeadsets.setText(sharedPref.getString("name"+buttonInt, ""))
                        headsetLayout.addView(btnHeadsets)
                        btnHeadsets.background = resources.getDrawable(R.drawable.fullround)
                        btnHeadsets.isAllCaps=false
                        btnHeadsets.textSize=18f
                        btnHeadsets.typeface= ResourcesCompat.getFont(this, R.font.evob)
                        btnHeadsets.setTextColor(resources.getColor(R.color.text))

                        headsetLayout.bringToFront()
                        deleteLayout.bringToFront()

                        val mSpace = Space(this)
                        mSpace.setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                50
                            )
                        )
                        headsetLayout.addView(mSpace)
                    }

                    val addMore = Button(this)
                    addMore.setLayoutParams(
                        LinearLayout.LayoutParams(
                            calibrate.layoutParams.width/2,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                    addMore.background = resources.getDrawable(R.drawable.fullround)
                    addMore.setText("Добавить еще")
                    headsetLayout.addView(addMore)
                    addMore.isAllCaps=false
                    addMore.textSize=18f
                    addMore.typeface= ResourcesCompat.getFont(this, R.font.evob);
                    addMore.setTextColor(resources.getColor(R.color.text))
                    addMore.bringToFront()
                }
                else
                {
                    val showanim = AlphaAnimation(0.1f, 1.0f)
                    showanim.duration = 300
                    showanim.fillAfter = true

                    //animations
                    gif2.startAnimation(showanim)
                    titleText7.startAnimation(showanim)
                    titleText3.startAnimation(showanim)
                    closeBtn.startAnimation(showanim)

                    closeBtn.isEnabled = true
                    registerReceiver(registHeadsetB, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
                }
            }
        }

        closeBtn.setOnClickListener{
            val hideanim = AlphaAnimation(1f, 0f)
            hideanim.duration = 300
            hideanim.fillAfter = true

            gif2.startAnimation(hideanim)
            titleText7.startAnimation(hideanim)
            titleText3.startAnimation(hideanim)
            closeBtn.startAnimation(hideanim)

            calibr.visibility = View.INVISIBLE
            mainRoot.removeView(iv) //убирает размытый фон
            closeBtn.isEnabled = false
            unregisterReceiver(registHeadsetB)

            if(sharedPref.contains("adress") && calibrate.text != "Список наушников"){ //Как мы нажмем на кнопку закрытия у нас вылезут виджеты настроек и тд
                sharedPref.edit().putString("name", editTextTextPersonName.text.toString()).apply()
                val animSet = AnimationSet(true)
                animSet.setFillEnabled(true)
                show_widgets.visibility = View.VISIBLE
                choosePanel.visibility = View.VISIBLE
                headset_name.visibility = View.VISIBLE
                namePanel.visibility = View.VISIBLE
                val pananim_trans = TranslateAnimation(0f,0f,-windowManager.defaultDisplay.height.toFloat()+titlePanel.layoutParams.height,0f)
                pananim_trans.duration = 700
                pananim_trans.fillAfter = true
                animSet.addAnimation(pananim_trans)
                val pananim_apha = AlphaAnimation(0f, 1f)
                pananim_apha.duration = 1500
                pananim_apha.fillAfter = true
                animSet.addAnimation(pananim_apha)
                calibrate.setText("Список наушников")

                val calibr_trans = TranslateAnimation(0f,0f,-windowManager.defaultDisplay.height.toFloat()/1.75f,0f)
                calibr_trans.duration = 500
                calibr_trans.fillAfter = true
                choosePanelAnim(animSet)
                namePanelAnim(animSet)
                calibrate.y = calibrate.y + windowManager.defaultDisplay.height.toFloat()/1.75f
                calibrate.startAnimation(calibr_trans)
            }
        }

        switcher.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            // do something, the isChecked will be
            // true if the switch is in the On position

            if(switcher.isChecked) {
                if(canDrawOverlays && sharedPref.contains("adress")) {
                    startService(Intent(this, RecieveBluetooth::class.java))

                    sharedPref.edit().putString("name", editTextTextPersonName.text.toString()).apply()

                    textOfSwitcher.setText("Выключить виджет")
                    calibrbutton.visibility = View.INVISIBLE
                    headset_name.visibility = View.INVISIBLE
                    show_widgets.visibility = View.INVISIBLE
                }
                else if(canDrawOverlays == false){//Если мы нажимаем на свитчер и у нас нету прав доступа отрисовки
                    startManageDrawOverlaysPermission()
                    switcher.isChecked = false
                }
                else if(sharedPref.contains("adress") == false){ //Если мы нажимаем на свитчер и у нас нету адресса
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    CreateBlurBackground(iv, getScreenShot(mainRoot))

                    switcher.isChecked = false

                    val showanim = AlphaAnimation(0.1f, 1.0f)
                    showanim.duration = 300
                    showanim.fillAfter = true

                    //visible
                    calibr.visibility = View.VISIBLE
                    gif2.bringToFront()
                    titleText7.bringToFront()
                    titleText3.bringToFront()
                    closeBtn.bringToFront()

                    //animations
                    gif2.startAnimation(showanim)
                    titleText7.startAnimation(showanim)
                    titleText3.startAnimation(showanim)
                    closeBtn.startAnimation(showanim)

                    closeBtn.isEnabled = true

                    if (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)){
                        titleText3.setText("Извините!")
                        titleText7.setText("Вам необходимо переподключить вашу Bluetooth гарнитуру, чтобы программа смогла запомнить ее!")
                        registerReceiver(registHeadsetB, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
                        registerReceiver(headsetIsDiscB, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
                    }
                    else
                    {
                        registerReceiver(registHeadsetB, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
                    }
                }
            }
            else {

                stopService(Intent(this, RecieveBluetooth::class.java))
                stopService(Intent(this, BatteryLevel::class.java))
                textOfSwitcher.setText("Включить виджет")
                calibrbutton.visibility = View.VISIBLE
                headset_name.visibility = View.VISIBLE
                show_widgets.visibility = View.VISIBLE
            }
        })

        switcher.isChecked = isMyServiceRunning(RecieveBluetooth::class.java)
    }



    override fun onDestroy() {
        super.onDestroy()
        val switcher = findViewById<Switch>(R.id.switch1)
        if(switcher.isChecked){
            startService(Intent(this, RecieveBluetooth::class.java))
        }
    }

    private fun startManageDrawOverlaysPermission() {
        val intetnofover = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(intetnofover)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun CreateBlurBackground(iv: ImageView, scrennS: Bitmap){
        val va = ValueAnimator.ofFloat(0.1f, 25f)
        val mDuration = 500 //in millis
        mainRoot.addView(iv)
        va.duration = mDuration.toLong()
        va.addUpdateListener { animation -> animation.animatedValue as Float
            iv.setImageDrawable(blur(this, scrennS, animation.getAnimatedValue() as Float, 0.1f)?.toDrawable(resources))
        }
        va.start()
    }
    fun getScreenShot(view: View): Bitmap {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    fun downTheMainTitle(){
        titlePanel.layoutParams = ConstraintLayout.LayoutParams(titlePanel.layoutParams.width, titlePanel.layoutParams.height +getNavigationBarHeight())
    }

    fun blur(context: Context?, image: Bitmap, radius: Float, scaleFactor: Float): Bitmap? {
        val width = Math.round(image.width * scaleFactor).toInt()
        val height = Math.round(image.height * scaleFactor).toInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        var outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        val tempInterestic = theIntrinsic
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.setRadius(radius)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        outputBitmap = Bitmap.createScaledBitmap(outputBitmap, windowManager.defaultDisplay.width, windowManager.defaultDisplay.height+getNavigationBarHeight(), false)
        return outputBitmap
    }

    private fun getNavigationBarHeight(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            return if (realHeight > usableHeight) realHeight - usableHeight else 0
        }
        return 0
    }

    private val registHeadsetB: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            if (device != null) {
                val sharedPref = getSharedPreferences("IMain", 0)
                        gif2.clearAnimation()
                        closeBtn.clearAnimation()
                        gif2.visibility = View.INVISIBLE
                        gif3.visibility = View.VISIBLE
                        gif3.bringToFront()
                        editTextTextPersonName.setText(device.alias)
                        sharedPref.edit().putString("name", device.alias).apply()
                        //sharedPref.edit().putInt("buttonNum", 1).apply()
                        animateText(titleText3, 300, "Поздравляем!")
                        animateText(
                            titleText7,
                            300,
                            "Теперь вы можете настроить свой собственный виджет!"
                        )
                        sharedPref.edit().putString("adress", device.address).apply()
            }
        }
    }

    private val headsetIsDiscB: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            animateText(titleText3, 300, "Осталось немного!")
            animateText(titleText7, 300, "Отлично, теперь переподключите вашу гарнитуру и калибровка будет завершена!")
            unregisterReceiver(this)
        }
    }

    fun animateText(tview: TextView, dur:Long, mtext: String){
        val changetext = AlphaAnimation(1f, 0f)
        changetext.duration = dur
        changetext.fillAfter = true
        tview.startAnimation(changetext)
        changetext.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                tview.setText(mtext)
                val changetext = AlphaAnimation(0f, 1f)
                changetext.duration = dur
                changetext.fillAfter = true
                tview.startAnimation(changetext)
            }
            override fun onAnimationRepeat(animation: Animation?) {
                TODO("Not yet implemented")
            }
            // All the other override functions
        })
    }

    fun choosePanelAnim(anim:Animation){
        choosePanel.startAnimation(anim)
        gif.startAnimation(anim)
        chooseImageBtn.startAnimation(anim)
        titleText2.startAnimation(anim)
    }

    fun namePanelAnim(anim:Animation){
        namePanel.startAnimation(anim)
        titleText4.startAnimation(anim)
        editTextTextPersonName.startAnimation(anim)
    }

    fun restart(context: Context) {
        val mainIntent =
            IntentCompat.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_LAUNCHER)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.applicationContext.startActivity(mainIntent)
        System.exit(0)
    }

}