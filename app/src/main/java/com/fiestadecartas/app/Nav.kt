package com.fiestadecartas.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Screen {
    data object Home : Screen()
    data object Settings : Screen()
    data object Profile : Screen()
    data object Review : Screen()
    data class Game(val id: String, val lan: Boolean = false, val hotseat: Boolean = false) : Screen()
    data class Lobby(val game: String) : Screen()
    data object Scanner : Screen()
    data object Share : Screen()
    data object Legends : Screen()
    data object About : Screen()
}

object Nav {
    var screen by mutableStateOf<Screen>(Screen.Home)
    fun go(target: Screen) { screen = target }
}
