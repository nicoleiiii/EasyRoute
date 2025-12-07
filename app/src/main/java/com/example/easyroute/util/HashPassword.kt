package com.example.easyroute.util

fun hashPassword(password: String): String {
    return password.hashCode().toString()
}
