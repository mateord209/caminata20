package com.example.caminata20.ui.util

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String, long: Boolean = message.length > 40) =
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
