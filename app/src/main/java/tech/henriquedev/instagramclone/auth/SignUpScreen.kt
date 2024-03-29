package tech.henriquedev.instagramclone.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import tech.henriquedev.instagramclone.DestinationScreen
import tech.henriquedev.instagramclone.IgViewModel
import tech.henriquedev.instagramclone.R
import tech.henriquedev.instagramclone.main.CheckSignedIn
import tech.henriquedev.instagramclone.main.CommonProgressSpinner
import tech.henriquedev.instagramclone.main.navigateTo

@Composable
fun SignUpScreen(navController: NavController, vm: IgViewModel) {

    CheckSignedIn(navController = navController, vm = vm)

    val focus = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val usernameState = remember { mutableStateOf(TextFieldValue()) }
            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passState = remember { mutableStateOf(TextFieldValue()) }

            Image(
                painter = painterResource(id = R.drawable.instagram),
                contentDescription = "Instagram logo image",
                modifier = Modifier
                    .width(84.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = "SignUp",
                modifier = Modifier.padding(8.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif
            )
            
            OutlinedTextField(
                value = usernameState.value,
                onValueChange = { usernameState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Username") }
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Email") }
            )
            OutlinedTextField(
                value = passState.value,
                onValueChange = { passState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Password") }
            )

            Button(
                onClick = {
                    focus.clearFocus(force = true)

                          vm.onSignUp(
                              username = usernameState.value.text,
                              email = emailState.value.text,
                              pass = passState.value.text
                          )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Sign Up")
            }
            
            Text(
                text = "Already a user? Go to link ->",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Login)
                    }
            )

        }

        val isLoading = vm.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}