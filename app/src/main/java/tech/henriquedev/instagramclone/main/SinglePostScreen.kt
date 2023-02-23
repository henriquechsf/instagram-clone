package tech.henriquedev.instagramclone.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import tech.henriquedev.instagramclone.IgViewModel
import tech.henriquedev.instagramclone.data.PostData

@Composable
fun SinglePostScreen(navController: NavController, vm: IgViewModel, post: PostData) {
    Text(text = "Single post screen ${post.postDescription}")
}