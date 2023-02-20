package tech.henriquedev.instagramclone.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import tech.henriquedev.instagramclone.IgViewModel

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel) {
    Text(text = "Feed Screen")
}