package tech.henriquedev.instagramclone.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import tech.henriquedev.instagramclone.IgViewModel

@Composable
fun NotificationMessage(vm: IgViewModel) {
    val notifyState = vm.popupNotification.value
    val notifyMessage = notifyState?.getContentOrNull()

    if (notifyState != null) {
        Toast.makeText(LocalContext.current, notifyMessage, Toast.LENGTH_LONG).show()
    }
}