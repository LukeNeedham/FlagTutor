package com.flagtutor.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import com.flagtutor.app.ui.navigation.HomeScreen
import kotlinx.browser.window
import org.w3c.dom.events.Event
import kotlin.js.toInt
import kotlin.js.toJsNumber

/**
 * Keeps the browser history and the in-app back stack in step:
 * - navigating in the app pushes a history entry with the screen's URL,
 * - popping in the app steps the history back, so the browser's forward/back stay consistent,
 * - the browser's back/forward buttons navigate the app.
 *
 * Each history entry's state holds its depth, so we know when there is no earlier entry of ours
 * to step back to (e.g. after opening a deep link).
 */
@Composable
fun BrowserNavigationSync(navigator: Navigator) {
    var currentNavigator by remember { mutableStateOf(navigator) }
    SideEffect { currentNavigator = navigator }

    DisposableEffect(Unit) {
        if (historyDepth() == null) {
            window.history.replaceState(0.toJsNumber(), "", window.location.href)
        }
        val onPopState: (Event) -> Unit = { showBrowserLocation(currentNavigator) }
        window.addEventListener("popstate", onPopState)
        onDispose { window.removeEventListener("popstate", onPopState) }
    }

    var previousSize by remember { mutableStateOf(navigator.size) }
    LaunchedEffect(navigator.size, navigator.lastItem) {
        val path = BrowserRoutes.pathFor(navigator.lastItem)
        val grew = navigator.size > previousSize
        val shrank = navigator.size < previousSize
        previousSize = navigator.size

        if (path == null || path == BrowserRoutes.currentPath()) return@LaunchedEffect
        val depth = historyDepth() ?: 0
        when {
            grew -> window.history.pushState((depth + 1).toJsNumber(), "", BrowserRoutes.urlFor(path))
            // Step back through our own history entry rather than adding another on top of it.
            shrank && depth > 0 -> window.history.back()
            // Nothing earlier of ours to go back to (e.g. a deep link), so rewrite the entry.
            else -> window.history.replaceState(depth.toJsNumber(), "", BrowserRoutes.urlFor(path))
        }
    }
}

private fun historyDepth(): Int? = (window.history.state as? kotlin.js.JsNumber)?.toInt()

/** Brings the app in line with the URL after the browser's back/forward buttons were used. */
private fun showBrowserLocation(navigator: Navigator) {
    val target = BrowserRoutes.screenFor(BrowserRoutes.currentPath())
    if (navigator.lastItem == target) return
    if (navigator.items.contains(target)) {
        navigator.popUntil { it == target }
    } else if (target == HomeScreen) {
        navigator.replaceAll(HomeScreen)
    } else {
        navigator.push(target)
    }
}
