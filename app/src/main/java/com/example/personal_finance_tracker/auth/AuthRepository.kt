package com.example.personal_finance_tracker.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser).isSuccess
        }
        auth.addAuthStateListener(listener)

        trySend(auth.currentUser).isSuccess

        awaitClose { auth.removeAuthStateListener(listener) }
    }
    fun signOut() = auth.signOut()
    fun firebaseAuth(): FirebaseAuth = auth
}
