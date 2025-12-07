package com.example.easyroute.database

import android.content.Context
import com.example.easyroute.util.hashPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val context: Context) {

    private val userDao = AppDatabase.getDatabase(context).userDao()

    // Register user
    suspend fun registerUser(username: String, email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("Email already registered"))
            } else {
                val user = User(
                    username = username,
                    email = email,
                    password = hashPassword(password)
                )
                userDao.registerUser(user)
                Result.success("Registration successful")
            }
        }
    }

    // Login user
    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            val hashedPassword = hashPassword(password)
            val user = userDao.login(email, hashedPassword)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        }
    }
}
