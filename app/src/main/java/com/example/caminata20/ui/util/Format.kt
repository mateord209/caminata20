package com.example.caminata20.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Mismo formato que usaban los historiales XML ("dd/MM/yyyy hh:mm a"). */
fun formatDateTime(ts: Long): String = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(ts))
