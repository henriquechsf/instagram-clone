package tech.henriquedev.instagramclone.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import tech.henriquedev.instagramclone.DestinationScreen
import tech.henriquedev.instagramclone.IgViewModel

@Composable
fun NotificationMessage(vm: IgViewModel) {
    val notifyState = vm.popupNotification.value
    val notifyMessage = notifyState?.getContentOrNull()

    if (notifyState != null) {
        Toast.makeText(LocalContext.current, notifyMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun CommonProgressSpinner() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) { }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

fun navigateTo(navController: NavController, destination: DestinationScreen) {
    navController.navigate(destination.route) {
        popUpTo(destination.route)
        launchSingleTop = true
    }
}

@Composable
fun CheckSignedIn(navController: NavController, vm: IgViewModel) {
    val alreadyLoggedIn = remember { mutableStateOf(false) }
    val signedIn = vm.signedIn.value

    if (signedIn && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true

        navController.navigate(DestinationScreen.Feed.route) {
            popUpTo(0)
        }
    }
}