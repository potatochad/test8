package Screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.outlined.AppBlocking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.productivity.wind.Bar
import com.productivity.wind.Blist
import com.productivity.wind.Global1
import com.productivity.wind.Global1.context
import com.productivity.wind.LazyPopup
import com.productivity.wind.NoLagCompose
import com.productivity.wind.OnOffSwitch
import com.productivity.wind.SettingItem
import com.productivity.wind.apps
import com.productivity.wind.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.productivity.wind.SettingsScreen
import com.productivity.wind.Screen_Layout


@Composable
fun ConfigureScreen() = NoLagCompose {
    val iconMap = remember { mutableStateMapOf<String, ImageBitmap>() }
    var show = remember { mutableStateOf(false) }
    var showPick = remember { mutableStateOf(false) }


    // Load apps gradually in background
    LaunchedEffect(Unit) {

        withContext(Dispatchers.IO) {
            log("Blist.apps---${Blist.apps}", "bad")

            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

            val InstalledApps = pm.queryIntentActivities(intent, 0)

            //region REMOVE UNINSTALLED APPS

            val installedPackageNames = InstalledApps.map { it.activityInfo.packageName }.toSet()
            withContext(Dispatchers.Main) {
                Blist.apps.removeAll { it.packageName.value !in installedPackageNames }
            }

            //endregion REMOVE UNINSTALLED APPS

            for (info in InstalledApps) {
                val label = info.loadLabel(pm)?.toString() ?: continue
                val packageName = info.activityInfo.packageName ?: continue
                val iconDrawable = info.activityInfo.loadIcon(pm)

                if (packageName == "com.productivity.wind") {continue}
                if (Blist.apps.any { it.packageName.value == packageName }) continue



                val app = apps(
                    name = mutableStateOf(label),
                    packageName = mutableStateOf(packageName)
                )

                withContext(Dispatchers.Main) {
                    Blist.apps.add(app)
                }

                val iconBitmap = iconDrawable.toBitmap().asImageBitmap()
                withContext(Dispatchers.Main) {
                    iconMap[packageName] = iconBitmap
                }

                delay(20) // Small delay to smoothen UI loading
            }
        }
    }

    val BlockedApps = Blist.apps.filter { it.Block.value }


    Screen_Layout(title = {Configure_Header()}){
        if (!areAllPermissionsEnabled(context)) {
            show.value = true; LazyPopup(show = show, onDismiss = {
                Global1.navController.navigate("Main")},
                title = "Need Permissions",
                message = "Please enable all permissions first. They are necessary for the app to work ",
                showCancel = true,
                onConfirm = { Global1.navController.navigate("SettingsP_Screen")},
                onCancel = { Global1.navController.navigate("Main")}) }
        else {
            SettingItem(icon = Icons.Outlined.AppBlocking, title = "Blocked Apps", endContent = {
                Button(
                    onClick = { showPick.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)), // gold
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Text("+", fontSize = 24.sp, color = Color.White)
                }

            })
            LazyPopup(show = showPick, title = "Add Blocks", message = "", showCancel = false, showConfirm = false, content = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(Blist.apps, key = { it.id }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.Block.value,
                                onCheckedChange = { app.Block.value = it},
                            )
                            val icon = iconMap[app.packageName.value]
                            if (icon != null) {
                                Image(
                                    bitmap = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.name.value)
                        }
                    }
                }
            }, onConfirm = {},)
            if (BlockedApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().height(Bar.halfHeight*2-200.dp),) { Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height( Bar.halfHeight-190.dp))
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Blocked Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Blocks", fontSize = 18.sp, color = Color.Gray)
                }
                }

            }
            else {

                LazyColumn(modifier = Modifier.fillMaxSize().height(500.dp)) {
                    items(BlockedApps, key = { it.id }) { app ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

                            val icon = iconMap[app.packageName.value]
                            if (icon != null) {
                                Image(
                                    bitmap = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp)
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }


                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.name.value)
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun Configure_Header() = NoLagCompose {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Configure apps")
        Spacer(modifier = Modifier.weight(1f))
        StopBlockingButton()
    }
}

@Composable
fun StopBlockingButton() {
    var showEnablePopup = remember { mutableStateOf(false) }
    var showUnsuccessfulD_Popup = remember { mutableStateOf(false) }

    // Show BEFORE enabling blocking
    if (showEnablePopup.value) {
        LazyPopup(
            show = showEnablePopup,
            onDismiss = { showEnablePopup.value = false },
            title = "Enable?",
            message = "If you enable blocking, an overlay screen will appear over the selected apps when you run out of points. (1 point = 1 second)\n\nTo disable blocking, you’ll need at least 1 point.",
            showCancel = true,
            showConfirm = true,
            onConfirm = {
                Bar.BlockingEnabled = true
                showEnablePopup.value = false
            },
            onCancel = {
                showEnablePopup.value = false
            }
        )
    }

    // Show if disabling fails
    if (showUnsuccessfulD_Popup.value) {
        LazyPopup(
            show = showUnsuccessfulD_Popup,
            onDismiss = { showUnsuccessfulD_Popup.value = false },
            title = "Not enough points",
            message = "You need at least 1 point to disable blocking. Just type a letter to earn one.",
            showCancel = true,
            showConfirm = false,
            onConfirm = {},
            onCancel = {
                showUnsuccessfulD_Popup.value = false
            }
        )
    }

    // Main switch
    OnOffSwitch(
        isOn = Bar.BlockingEnabled,
        onToggle = { isNowOn ->
            if (isNowOn) {
                showEnablePopup.value = true
            } else {
                val hasPoints = Bar.funTime > 0
                if (hasPoints) {
                    Bar.BlockingEnabled = false
                } else {
                    showUnsuccessfulD_Popup.value = true
                }
            }
        }
    )
}
