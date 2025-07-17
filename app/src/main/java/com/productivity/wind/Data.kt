package com.productivity.wind


import Screens.Achievements
import Screens.ConfigureScreen
import Screens.DayChecker
import Screens.Main
import Screens.SettingsP_Screen
import Screens.Settings_Difficulty
import Screens.isDeviceAdminEnabled
import androidx.compose.runtime.mutableStateOf

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.graphics.PixelFormat
import android.widget.Button
import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.Rect
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.view.Surface
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.productivity.wind.ui.theme.KeepAliveTheme

class Settings {
    var funTime by mutableStateOf(0)
    var showBlockScreen by mutableStateOf(true)
    var currentApp by mutableStateOf("")
    var HowMuchForGodMode by mutableStateOf(1)

    //region COPY PASTE THING Disipline

    var FirstEditText by mutableStateOf(true)
    var targetText by mutableStateOf("Let's get ready to work. Start by choosing one task that is the most important. Try to focus on that task only.  When the task is finished, you can take a short break to rest. If you finish more tasks after that, great job — keep going one step at a time. If permissions turned on (menu->settings) you can configure apps (Top bar, right side icon) to be blocked if do not have enough points (1 point = 1 second).")
    var LetterToTime by mutableStateOf(1)
    var DoneRetype_to_time by mutableStateOf(60)
    var HowManyDoneRetypes_InDay by mutableStateOf(0)
    var currentInput by mutableStateOf("")
    var highestCorrect by mutableStateOf(0)

    //endregion COPY PASTE Disipline


    //region COPY PASTE THING GERMAN

    var G_FirstEditText by mutableStateOf(true)
    var G_targetText by mutableStateOf("Let's get ready to work. Start by choosing one task that is the most important. Try to focus on that task only.  When the task is finished, you can take a short break to rest. If you finish more tasks after that, great job — keep going one step at a time. If permissions turned on (menu->settings) you can configure apps (Top bar, right side icon) to be blocked if do not have enough points (1 point = 1 second).")
    var G_LetterToTime by mutableStateOf(2)
    var G_DoneRetype_to_time by mutableStateOf(60)
    var G_currentInput by mutableStateOf("")
    var G_highestCorrect by mutableStateOf(0)

    //endregion COPY PASTE GERMAN



    //region BLOCKING

    var BlockingEnabled by mutableStateOf(false)

    var COUNT by mutableStateOf(0)
    var NewDay by mutableStateOf(true)

    //refreshs to 0 daily// if more than 50 seconds, get 600 time
    var WaterDOtime_spent by mutableStateOf(0)
    var secondsLeft by mutableStateOf(10)
    //endregion BLOCKING

    //region MISALANIOUS

    var halfHeight by mutableStateOf(0.dp)
    var halfWidth by mutableStateOf(0.dp)
    var ShowMenu by mutableStateOf(false)


    //endregion MISALANIOUS

    //region PERMISSIONS

    //region PERMISSIONS

    var App_Description by mutableStateOf(
        "A simple, gamified app that helps you build focus and habits. Just retype your goals, affirmations, or learning material. Earn points. Use points to unlock time on the apps you choose."
    )

    var NotificationPermission by mutableStateOf(false)
    var NotificationP_Description by mutableStateOf(
        "Keeps the app running in the background so you don’t miss any tracking or point updates."
    )

    var DrawOnTopPermission by mutableStateOf(false)
    var DrawOnTopP_Description by mutableStateOf(
        "Allows the app to show a screen over other apps. This is used to gently pause access to apps you’ve selected, if you have spent all earned points."
    )

    var OptimizationExclusionPermission by mutableStateOf(false)
    var OptimizationExclusionP_Description by mutableStateOf(
        "Stops your phone’s battery saver from shutting down the background service. This ensures the app runs smoothly."
    )

    var UsageStatsPermission by mutableStateOf(false)
    var UsageStatsP_Description by mutableStateOf(
        "Lets the app see which apps you open. Used only to track apps you selected, to help manage your focus."
    )

    var DeviceAdminPermission by mutableStateOf(false)
    var DeviceAdminP_Description by mutableStateOf(
        "Optional: Gives a stronger layer of control if you want extra discipline. Can be turned off anytime in settings."
    )

//endregion

    //endregion

    //region ACHIEVEMENTS

    var TotalTypedLetters by mutableStateOf(0)

    //endregion

}



data class apps(
    var id: String = UUID.randomUUID().toString(),
    var name: MutableState<String> = mutableStateOf(""),
    var done: MutableState<Boolean> = mutableStateOf(false),
    var packageName: MutableState<String> = mutableStateOf(""),
    var Block : MutableState<Boolean> = mutableStateOf(false),
    var TimeSpent : MutableState<Int> = mutableStateOf(0),
)
data class CopyPastes(
    var id: String = UUID.randomUUID().toString(),
    var text: MutableState<String> = mutableStateOf("Copy paste text"),
    var done: MutableState<Boolean> = mutableStateOf(false),
    var HowManyDones : MutableState<Int> = mutableStateOf(0),
)

object Blist {
    var apps = mutableStateListOf<apps>()
    var CopyPastes = mutableStateListOf<CopyPastes>()
}












//region BACKGROUND TASK

class WatchdogService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var OneJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("onBind: $intent")
        return null
    }

    //region Boring
    private var isOverlayShowing = false

    fun BlockScreen() {
        if (!Settings.canDrawOverlays(this)) { log("canDrawOverlays----PERMISSSION NOT GRANTED", "bad"); return}

        Bar.secondsLeft = 10 // ✅ always reset when showing the overlay
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val XmlBuilder = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val XmlView = XmlBuilder.inflate(R.layout.overlay_layout, null)

        //region SCREEN PRAMS-BORING

        if (XmlView.parent != null) {
            windowManager.removeView(XmlView) // ✅ ensures no duplicate view error
        }
        val rotation = windowManager.defaultDisplay.rotation
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            XmlView.rotation = -90f // ✅ visually lock to upright
        }


        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or // ✅ changed
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,      // ✅ changed
            PixelFormat.TRANSLUCENT
        )

        //endregion

        if (!isOverlayShowing) { log("Showing BlockScreen:::isOverlayShowing---${isOverlayShowing}", "bad"); windowManager.addView(XmlView, layoutParams); isOverlayShowing = true }
        if (XmlView?.isAttachedToWindow == true) { } else { isOverlayShowing = false;log("NOW SHOWING BlockScreen:::isOverlayShowing-----${isOverlayShowing}", "bad") }


        //region WAIT 10 SECONS TO LEAVE

        val leaveButton = XmlView.findViewById<Button>(R.id.leaveButton)
        leaveButton.isEnabled = false
        leaveButton.text = Bar.secondsLeft.toString()

        val handler = Handler(Looper.getMainLooper())
        handler.removeCallbacksAndMessages(null) // ✅ clear any previous countdowns

        val countdown = object : Runnable {
            override fun run() {
                log("BLOCK SCREEN: Counting down---secondsLeft---${Bar.secondsLeft}", "bad")
                Bar.secondsLeft--

                if (Bar.secondsLeft > 0) {
                    leaveButton.text = Bar.secondsLeft.toString()
                    handler.postDelayed(this, 1000) // wait 1 second, then run again
                } else {
                    leaveButton.text = "Leave"
                    leaveButton.isEnabled = true // now they can click it
                }
            }
        }
        handler.postDelayed(countdown, 1000) // start after 1 second


        //endregion

        leaveButton.setOnClickListener {
            Bar.secondsLeft = 10
            log("BLOCK SCREEN: Clicked leave button", "bad")
            val intent = Intent(Global1.context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Global1.context.startActivity(intent)

            log("Removing Overlay Screen+ isOverlayShowing = false", "bad")
            isOverlayShowing = false
            windowManager.removeView(XmlView)
        }

    }

    //endregion

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        NotificationHelper(this).createNotificationChannel()
        startForeground(1, NotificationHelper(this).buildNotification(),)
        Global1.context = this

        if (OneJob == null || OneJob?.isActive == false) {
            OneJob = serviceScope.launch {
                while (true) {
                    if (Bar.BlockingEnabled) {

                        //region SAFETY PURPOSES

                        delay(1000L)
                        Bar.COUNT += 1

                        //endregion


                        //region CURRENT APP

                        val usageStatsManager =
                            Global1.context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                        val NowTime = System.currentTimeMillis()

                        /*
                    * THIS IS NOT SUPER ACCURATE
                    ? If you want better precision, you’ll need an Accessibility Service.
                    !THIS REQUIRES NAVIGATING USER TO IT*/
                        val AppsUsed = usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            NowTime - 20_000,
                            NowTime
                        )
                        val currentApp = AppsUsed?.maxByOrNull { it.lastTimeUsed }?.packageName

                        //LOG WHEN WANT TOOO
                        if (currentApp == Global1.context.packageName || currentApp == null) {
                        } else {
                            log("BACKGROUND — CURRENT APP: $currentApp", "bad")
                        }

                        //endregion CURRENT APP

                        val blocked =
                            Blist.apps.any { it.packageName.value == currentApp && it.Block.value }

                        if (blocked) {
                            if (Bar.funTime > 0) {
                                Bar.funTime -= 1
                                log("BACKGROUND---Spending Time??:::${Bar.funTime};", "bad")
                            } else {
                                BlockScreen()
                                log(
                                    "BACKGROUND---Blocking APP:::${currentApp}; ${Bar.COUNT}",
                                    "bad"
                                )
                            }
                        }
//                        if (currentApp == ) {
//                            if (Bar.funTime > 0) {
//                                Bar.funTime -= 1
//                                log("BACKGROUND---Spending Time??:::${Bar.funTime};", "bad")
//                            } else {
//                                BlockScreen()
//                                log("BACKGROUND---Blocking APP:::${currentApp}; ${Bar.COUNT}", "bad")
//                            }
//                        }


                        if (currentApp == "com.android.settings") {

                            if (Bar.funTime > 1_000) {
                                log("BACKGROUND---Spending Time??:::${Bar.funTime};", "bad")
                            } else {
                                if (isDeviceAdminEnabled(Global1.context)) {
                                    BlockScreen()
                                    log(
                                        "BACKGROUND---Blocking APP:::${currentApp}; ${Bar.COUNT}",
                                        "bad"
                                    )
                                }
                            }
                        }

                        if (currentApp == "com.microsoft.office.onenote") {
                            if (Bar.funTime > 2_000) {
                                log("BACKGROUND---Spending Time??:::${Bar.funTime};", "bad")
                            } else {

                                BlockScreen()
                                log(
                                    "BACKGROUND---Blocking APP:::${currentApp}; ${Bar.COUNT}",
                                    "bad"
                                )

                            }
                        }

                        if (currentApp == "com.seekrtech.waterapp") {
                            log("NEW DAY??; ${Bar.NewDay}", "bad")
                            log("waterdo time; ${Bar.WaterDOtime_spent}", "bad")
                            if (Bar.NewDay) {
                                if (Bar.WaterDOtime_spent > 50) {
                                    Bar.funTime += 900
                                    Bar.NewDay = false
                                } else {
                                    Bar.WaterDOtime_spent += 1
                                }
                            }
                        }


                    }
                }
            }
        }


        return START_STICKY
    }

    override fun onDestroy() { super.onDestroy(); serviceScope.cancel() }
}


//endregion








//region NAVCONTROLLER AND OTHER

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "Main") {
        composable("Main") {
            Main()
        }
        composable("Achievements") {
            Achievements()
        }
        composable("ConfigureScreen") {
            ConfigureScreen()
        }
        composable("MainAddScreen") {
            //MainAddScreen()
        }

        //region SETTINGS

        composable("SettingsScreen") {
            Screens.SettingsScreen()
        }
        composable("SettingsP_Screen") {
            SettingsP_Screen()
        }
        composable("Settings_Difficulty") {
            Settings_Difficulty()
        }

        //endregion SETTINGS
    }
}



//!DISABLED
@Composable
fun AccessibilityPermission() {
    val context = LocalContext.current
    val serviceId = "${context.packageName}/${WatchdogAccessibilityService::class.java.name}"

    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""

    val isEnabled = enabledServices.contains(serviceId)

    log("Service id AccessibilityPermission${serviceId} ", "bad")
    log("isEnabled? AccessibilityPermission${isEnabled} ", "bad")
    if (!isEnabled) {
        //!Bar.AccesabilityPermission = false
        AlertDialog.Builder(context)
            .setTitle("Permission Needed")
            .setMessage("Please enable accessibility for full features.")
            .setPositiveButton("ok") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .show()
    } else {
        //!Bar.AccesabilityPermission = true
    }
}
class MyNotificationListener : NotificationListenerService()

//endregion NAVCONTROLLER AND OTHER

//region OnAppStart

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppStart_beforeUI(applicationContext)
        setContent {
            KeepAliveTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppStart()

                    Global1.navController = rememberNavController()
                    MyNavGraph(navController = Global1.navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun AppStart_beforeUI(context: Context) {
    Global1.context = context
    SettingsSaved.init()
    SettingsSaved.Bsave()

    UniversalListManager.initialize(context, Blist)


    //Background thing
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { context.startForegroundService(Intent(context, WatchdogService::class.java))} else { context.startService(Intent(context, WatchdogService::class.java)) }
}


@Composable
fun AppStart() {
    val halfWidth = LocalConfiguration.current.screenWidthDp.dp/2+30.dp; Bar.halfWidth = halfWidth
    val halfHeight = LocalConfiguration.current.screenHeightDp.dp/2; Bar.halfHeight = halfHeight
    LaunchedEffect(Unit) {
        DayChecker.start()
    }
}



//endregion

//region GLOBAL
//* CONTEXT from anywhere!!!
object Global1 {
    /* APP CONTEXT
    Context is weirt:
    there is application, ok for most things
    ?and local
    !which used for popup etc...
    * */
    lateinit var context: Context
    lateinit var navController: NavHostController


    /*POPUPS CRASHING??
    * USE LOCAL CONTEXT
    * ? YEA GOT NO BETTER SOLUTION SORRRY*/

}



//endregion

































/*?WHEN TESTING IS PAINFUL
* !AUTO DISABLED EACH TIME*/
/* IT AUTO CALLS ITSELF
YEA, WEIRD ONLY FEW FUNCTIONS IT CALLS
? YOU MUST WORK WITH IT WANT AND SET UP
! onAccessibilityEvent- IS A MUST AND ONLY ON CHANGE CALLED
* */
class WatchdogAccessibilityService  : AccessibilityService() {

    /* ON CHANGE DO X
    ! this is very important
    ? must be names onAccessibilityEvent

    * */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        CurrentApp(event)
        //GETEVERYTHING(event)
    }
    fun CurrentApp(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Bar.currentApp = event.packageName.toString()
            log("CURRENT APP: ${Bar.currentApp}", "bad")
        }
    }
    private fun exploreNodeTree(node: AccessibilityNodeInfo?, depth: Int) {
        if (node == null) return

        val indent = "  ".repeat(depth)

        val text = node.text
        val id = node.viewIdResourceName
        val desc = node.contentDescription
        val className = node.className
        val clickable = node.isClickable
        val enabled = node.isEnabled
        val focused = node.isFocused
        val focusable = node.isFocusable
        val visible = node.isVisibleToUser
        val bounds = Rect().apply { node.getBoundsInScreen(this) }

        // Header
        log("$indent Node:", "bad")

        // Properties
        if (!text.isNullOrEmpty()) log("$indent   Text: $text", "bad")
        if (!desc.isNullOrEmpty()) log("$indent   Description: $desc", "bad")
        if (!id.isNullOrEmpty()) log("$indent   ID: $id", "bad")
        log("$indent   Class: $className", "bad")
        log("$indent   Clickable: $clickable", "bad")
        log("$indent   Enabled: $enabled", "bad")
        log("$indent   Focused: $focused", "bad")
        log("$indent   Focusable: $focusable", "bad")
        log("$indent   Visible: $visible", "bad")
        log("$indent   Bounds: $bounds", "bad")

        // Children
        for (i in 0 until node.childCount) {
            exploreNodeTree(node.getChild(i), depth + 1)
        }
    }

    fun GETEVERYTHING(event: AccessibilityEvent?) {
        val root = rootInActiveWindow
        if (root != null) {
            exploreNodeTree(root, 0)
        }
    }
    override fun onInterrupt() {
        log("ACESABILITY Service interrupted.", "bad")
    }
}

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "WatchdogServiceChannel"
    }
    fun createNotificationChannel() {
        Timber.d("createNotificationChannel() called")
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name_watchdog_service),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(channel)
    }

    fun buildNotification(): Notification {
        Timber.d("buildNotification() called")

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntentFlags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent =
            PendingIntent.getActivity(context, 0, notificationIntent, pendingIntentFlags)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title_app_watchdog))
            .setContentText(context.getString(R.string.notification_content_monitoring_apps))
            .setSmallIcon(R.drawable.baseline_radar_24)
            .setContentIntent(pendingIntent)
            // Low priority for ongoing background service notification
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // Makes the notification persistent
            .setOngoing(true)
            .build()
    }
}
