package Screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.productivity.wind.Bar
import com.productivity.wind.NoLagCompose
import com.productivity.wind.PermissionsButton
import com.productivity.wind.SettingItem
import kotlinx.coroutines.delay


@Composable
fun Achievements()= NoLagCompose {
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
    DeviceAdminP_PopUp(ctx, showDeviceAdminPopup)


    com.productivity.wind.SettingsScreen(titleContent = { Text("Permissions") }, showSearch = false) {

        SettingItem(
            icon = Icons.Outlined.Notifications,
            title = "Notification",
            subtitle = "Necessary",
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
            subtitle = "Necessary",
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
            subtitle = "Necessary",
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
            subtitle = "Necessary",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.UsageStatsPermission,
                    onEnable = { showUsagePopup.value = true }
                )
            }
        )
        SettingItem(
            icon = Icons.Outlined.Security,
            title = "Device Admin",
            subtitle = "Optional-for discipline",
            endContent = {
                PermissionsButton(
                    isEnabled = Bar.DeviceAdminPermission,
                    onEnable = { showDeviceAdminPopup.value = true }
                )
            }
        )

    }
}
