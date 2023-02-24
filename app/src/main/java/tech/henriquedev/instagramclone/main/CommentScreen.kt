package tech.henriquedev.instagramclone.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import tech.henriquedev.instagramclone.IgViewModel

@Composable
fun CommentScreen(navController: NavController, vm: IgViewModel, postId: String) {
    Text(text = "Comment Screen")
}