import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun StartButton(onClick: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current

    val permissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    ) {
        if (it.all { (_, granted) -> granted }) {
            onClick()
        }
    }

    Button(onClick = {
        if (permissions.allPermissionsGranted) {
            onClick()
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val permissionsRequested = prefs.getBoolean("permissionsRequested", false)
            if (!permissions.shouldShowRationale && permissionsRequested) {
                context.navigateToAppSettings()
                return@Button
            }

            prefs.edit { putBoolean("permissionsRequested", true) }
            permissions.launchMultiplePermissionRequest()
        }
    }, modifier = modifier) {
        Text("Start")
    }
}

private fun Context.navigateToAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
    }
    startActivity(intent)
}
