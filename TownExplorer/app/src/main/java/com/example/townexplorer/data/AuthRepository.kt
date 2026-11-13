package com.example.townexplorer.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/**
 * Errors shown on the auth screens.
 */
sealed class AuthError(val message: String) {
    data object InvalidCredentials : AuthError("Incorrect e-mail or password")
    data object InvalidEmail : AuthError("Enter a valid e-mail address")
    data object EmailInUse : AuthError("A user already exists that is using this e-mail")
    data object WeakPassword : AuthError("Password must be at least 6 characters")
    data object Network : AuthError("No internet connection. Try again.")
    data object TooManyRequests : AuthError("Too many attempts. Try again later.")
    data object Unknown : AuthError("Something went wrong. Try again.")
}

/**
 * Firebase Authentication with e-mail and password.
 * Each function returns null on success or an AuthError on failure.
 *   Source: https://firebase.google.com/docs/auth/android/password-auth
 */
object AuthRepository {

    private const val TAG = "AuthRepository"

    private val auth: FirebaseAuth
        get() = Firebase.auth

    // Signed-in user, or null. Firebase keeps the session after the app is closed.
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): AuthError? = runAuth {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    // Creates the account and its users/{uid} document, then signs out so the user logs in on the Login screen.
    suspend fun signUp(email: String, password: String): AuthError? = runAuth(isSignUp = true) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
        if (user != null) {
            try {
                UserRepository.createProfile(user.uid, user.email ?: email)
            } catch (e: FirebaseException) {
                Log.w(TAG, "Could not create the user profile document", e)
            }
        }
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): AuthError? = runAuth {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() = auth.signOut()

    // Runs a Firebase call and maps its exception to an AuthError.
    private suspend fun runAuth(isSignUp: Boolean = false, block: suspend () -> Unit): AuthError? {
        return try {
            block()
            null
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthError.WeakPassword
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (isSignUp) AuthError.InvalidEmail else AuthError.InvalidCredentials
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthError.InvalidCredentials
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthError.EmailInUse
        } catch (e: FirebaseNetworkException) {
            AuthError.Network
        } catch (e: FirebaseTooManyRequestsException) {
            AuthError.TooManyRequests
        } catch (e: FirebaseException) {
            Log.w(TAG, "Firebase Auth call failed", e)
            AuthError.Unknown
        }
    }
}
