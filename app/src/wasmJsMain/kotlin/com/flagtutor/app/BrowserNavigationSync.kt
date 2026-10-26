package com.flagtutor.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.flagtutor.app.ui.navigation.Route
import kotlinx.browser.window
import org.w3c.dom.events.Event
import kotlin.js.toInt
import kotlin.js.toJsNumber

/**
 * Keeps the browser history and the in-app back stack in step:
 * - navigating in the app pushes a history entry with the route's URL,
 * - popping in the app steps the history back, so the browser's forward/back stay consistent,
 * - the browser's back/forward buttons navigate the app.
 *
 * Each history entry's state holds its depth, so we know when there is no earlier entry of ours
 * to step back to (e.g. after opening a deep link).
 */
@Composable
fun BrowserNavigationSync(backStack: NavBackStack<NavKey>) {
    DisposableEffect(backStack) {
        if (historyDepth() == null) {
            window.history.replaceState(0.toJsNumber(), "", window.location.href)
        }
        val onPopState: (Event) -> Unit = { showBrowserLocation(backStack) }
        window.addEventListener("popstate", onPopState)
        onDispose { window.removeEventListener("popstate", onPopState) }
    }

    var previousSize by remember { mutableStateOf(backStack.size) }
    val lastRoute = backStack.lastOrNull()
    LaunchedEffect(backStack.size, lastRoute) {
        val path = (lastRoute as? Route)?.let(BrowserRoutes::pathFor)
        val grew = backStack.size > previousSize
        val shrank = backStack.size < previousSize
        previousSize = backStack.size

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
private fun showBrowserLocation(backStack: NavBackStack<NavKey>) {
    val target = BrowserRoutes.routeFor(BrowserRoutes.currentPath())
    if (backStack.lastOrNull() == target) return
    if (target in backStack) {
        while (backStack.lastOrNull() != target) backStack.removeLastOrNull()
    } else if (target == Route.Home) {
        backStack.clear()
        backStack.add(Route.Home)
    } else {
        backStack.add(target)
    }
}
