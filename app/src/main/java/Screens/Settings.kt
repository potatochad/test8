package Screens

import android.app.AppOpsManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.core.app.NotificationManagerCompat
import com.productivity.wind.Bar
import com.productivity.wind.Global1
import com.productivity.wind.LazyPopup
import com.productivity.wind.NoLagCompose
import com.productivity.wind.PermissionsButton
import com.productivity.wind.Screen_Layout
import com.productivity.wind.SettingsScreen
import com.productivity.wind.SettingItem
import com.productivity.wind.SmartInputField
import kotlinx.coroutines.delay

//region Settings

@Composable
fun SettingsScreen() {
    SettingsScreen(titleContent = { Text("Settings") }, showSearch = false) {

        SettingItem(
            icon = Icons.Outlined.AdminPanelSettings,
            title = "Permissions",
            onClick = { Global1.navController.navigate("SettingsP_Screen") }
        )
        SettingItem(
            icon = Icons.Outlined.AdminPanelSettings,
            title = "Difficullty",
            onClick = { Global1.navController.navigate("Settings_Difficulty") }
        )
    }
}



//region PERMISSIONS

//region POPUP

@Composable
fun showPermissionDialog(
    show: MutableState<Boolean>,
    context: Context,
    title: String,
    message: String,
    settingsAction: String,
    dataUri: Uri? = null
) {
    LazyPopup(
        show = show,
        onDismiss = { },
        onConfirm = {
            Intent(settingsAction).apply {
                dataUri?.let { data = it }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { context.startActivity(it) }
        },
        title = title,
        message = message,
    )
}
@Composable
fun showPermissionDialog2(
    show: MutableState<Boolean>,
    context: Context,
    title: String,
    message: String,
    onConfirm: () -> Unit,
) {
    LazyPopup(
        show = show,
        onDismiss = { },
        onConfirm = {
            onConfirm()
        },
        title = title,
        message = message,
    )
}



@Composable
fun DrawOnTopP_PopUp(context: Context, show: MutableState<Boolean>) =
    showPermissionDialog(
        show,
        context,
        "Grant Draw on top permission",
        Bar.DrawOnTopP_Description,
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )

@Composable
fun NotificationP_PopUp(context: Context, show: MutableState<Boolean>) =
    showPermissionDialog(
        show,
        context,
        "Grant Notification access",
        Bar.NotificationP_Description,
        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
    )

@Composable
fun OptimizationExclusionP_PopUp(context: Context, show: MutableState<Boolean>) =
    showPermissionDialog2(
        show,
        context,
        "Exclude from battery optimization",
        Bar.OptimizationExclusionP_Description,
        onConfirm = {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    )


@Composable
fun UsageStatsP_PopUp(context: Context, show: MutableState<Boolean>) =
    showPermissionDialog(
        show,
        context,
        "Grant Usage-Access permission",
        Bar.UsageStatsP_Description,
        Settings.ACTION_USAGE_ACCESS_SETTINGS
    )

//!Doesn't work
@Composable
fun DeviceAdminP_PopUp(ctx: Context, show: MutableState<Boolean>) {
    if (!show.value) return

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = { show.value = false }
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .width(300.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Admin Permission", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("We need device admin access to block apps properly.")
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { show.value = false }) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        show.value = false
                        try {
                            val comp = ComponentName(ctx, MyDeviceAdminReceiver::class.java)
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Used to block apps.")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Failed to open admin screen.", Toast.LENGTH_LONG).show()
                            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            ctx.startActivity(fallback)
                        }
                    }) {
                        Text("Allow")
                    }
                }
            }
        }
    }
}
class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.w("DeviceAdmin", "✅ Admin activated!")
        Toast.makeText(context, "Admin access granted!", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w("DeviceAdmin", "❌ Admin removed!")
        Toast.makeText(context, "Admin access removed!", Toast.LENGTH_SHORT).show()
    }
}

//endregion POPUP

//region ENABLED??

fun isDrawOnTopEnabled(ctx: Context): Boolean =
    Settings.canDrawOverlays(ctx)

fun isNotificationEnabled(ctx: Context): Boolean =
    NotificationManagerCompat
        .getEnabledListenerPackages(ctx)
        .contains(ctx.packageName)

fun isBatteryOptimizationDisabled(ctx: Context): Boolean {
    val pm = ctx.getSystemService(PowerManager::class.java)
    return pm.isIgnoringBatteryOptimizations(ctx.packageName)
}

fun isUsageStatsP_Enabled(ctx: Context): Boolean {
    val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    return appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        ctx.packageName
    ) == AppOpsManager.MODE_ALLOWED
}

fun isDeviceAdminEnabled(ctx: Context): Boolean =
    (ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
        .isAdminActive(ComponentName(ctx, MyDeviceAdminReceiver::class.java))

fun areAllPermissionsEnabled(ctx: Context): Boolean {
    return isDrawOnTopEnabled(ctx)
            && isNotificationEnabled(ctx)
            && isBatteryOptimizationDisabled(ctx)
            && isUsageStatsP_Enabled(ctx)
}


//endregion ENABLED??

@Composable
fun SettingsP_Screen()= NoLagCompose {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {

        while(true) {
            Bar.NotificationPermission = isNotificationEnabled(ctx)
            Bar.DrawOnTopPermission = isDrawOnTopEnabled(ctx)
            Bar.OptimizationExclusionPermission = isBatteryOptimizationDisabled(ctx)
            Bar.UsageStatsPermission = isUsageStatsP_Enabled(ctx)
            Bar.DeviceAdminPermission = isDeviceAdminEnabled(ctx)
            delay(200L)
        }
    }
    var showNotificationPopup = remember { mutableStateOf(false) }
    var showDrawOnTopPopup = remember { mutableStateOf(false) }
    var showOptimizationPopup = remember { mutableStateOf(false) }
    var showUsagePopup = remember { mutableStateOf(false) }
    var showDeviceAdminPopup = remember { mutableStateOf(false) }


    NotificationP_PopUp(ctx, showNotificationPopup)
    DrawOnTopP_PopUp(ctx, showDrawOnTopPopup)
    OptimizationExclusionP_PopUp(ctx, showOptimizationPopup)
    UsageStatsP_PopUp(ctx, showUsagePopup)

    SettingsScreen(titleContent = { Text("Permissions") }, showSearch = false) {

        SettingItem(
            icon = Icons.Outlined.Notifications,
            title = "Notification",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.NotificationPermission,
                    onEnable = {
                        showNotificationPopup.value= true
                    }
                )
            }
        )
        SettingItem(
            icon = Icons.Outlined.Visibility,
            title = "Draw On Top",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.DrawOnTopPermission,
                    onEnable = {showDrawOnTopPopup.value = true}
                )
            }
        )
        SettingItem(
            icon = Icons.Outlined.BatterySaver,
            title = "Optimization Exclusion",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.OptimizationExclusionPermission,
                    onEnable = { showOptimizationPopup.value = true }
                )
            }
        )
        SettingItem(
            icon = Icons.Outlined.BarChart,
            title = "Usage Stats",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.UsageStatsPermission,
                    onEnable = { showUsagePopup.value = true }
                )
            }
        )
    }
}

//endregion PERMISSIONS

@Composable
fun Settings_Difficulty(){
    Screen_Layout(title = { Text("Difficulty") }) { }
    SettingItem(
        icon = Icons.Outlined.AdminPanelSettings,
        title = "How many points to bypass everything",
        endContent = {
            SmartInputField(
                value = Bar.HowMuchForGodMode,
                onValueChange ={ Bar.HowMuchForGodMode = it },
            )
        }
    )
}
//endregion Settings
