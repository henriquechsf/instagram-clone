package tech.henriquedev.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import tech.henriquedev.instagramclone.auth.SignUpScreen
import tech.henriquedev.instagramclone.ui.theme.InstagramCloneTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object SignUp: DestinationScreen("signup")
}

@Composable
fun InstagramApp() {
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController = navController, vm = vm)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstagramCloneTheme {
        InstagramApp()
    }
}