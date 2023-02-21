package tech.henriquedev.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.henriquedev.instagramclone.data.Event
import tech.henriquedev.instagramclone.data.PostData
import tech.henriquedev.instagramclone.data.UserData
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"

@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid = uid)
        }
    }

    fun onSignUp(username: String, email: String, pass: String) {
        if (username.isEmpty() or email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true

        db.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true

                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
    }

    fun onLogin(email: String, pass: String) {
        if (email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        //handleException(customMessage = "Login success")
                        getUserData(uid)
                    }
                } else {
                    handleException(task.exception, "Login failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Login failed")
                inProgress.value = false
            }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged Out")
    }

    fun updateProfileData(name: String, username: String, bio: String) {
        createOrUpdateProfile(name, username, bio)
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                                inProgress.value = false
                            }
                    } else {
                        db.collection(USERS).document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot create user")
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false

                refreshPosts()
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve user data")
                inProgress.value = false
            }
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exception ->
                handleException(exception)
                inProgress.value = false
            }
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
    }

    // POSTS

    fun onNewPost(uri: Uri, description: String, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, onPostSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, description: String, onPostSuccess: () -> Unit) {
        inProgress.value = true

        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null) {

            val postUuid = UUID.randomUUID().toString()
            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis()
            )

            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event("Post successfully created")
                    inProgress.value = false

                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Unable to create post")
                    inProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: username unavailable. Unable to create post")
            onLogout()
        }
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            refreshPostsProgress.value = true

            db.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exception ->
                    handleException(exception, "Cannot fetch posts")
                    refreshPostsProgress.value = false
                }

        } else {
            handleException(customMessage = "Error: username unavailable. Unable to refresh posts")
            onLogout()
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()

        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }

        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

}