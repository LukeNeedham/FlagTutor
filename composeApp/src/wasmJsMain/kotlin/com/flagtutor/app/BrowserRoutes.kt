package com.flagtutor.app

import cafe.adriel.voyager.core.screen.Screen
import com.flagtutor.app.ui.navigation.CreditsScreen
import com.flagtutor.app.ui.navigation.HomeScreen
import com.flagtutor.app.ui.navigation.PickCountryNameGameScreen
import kotlinx.browser.window

/**
 * Maps screens to URL paths so the browser's address bar and back button follow in-app navigation.
 * Screens without a route (e.g. debug pages, which aren't available on web) are left off the URL.
 */
object BrowserRoutes {

    private val routes: List<Pair<String, Screen>> = listOf(
        "/play" to PickCountryNameGameScreen,
        "/credits" to CreditsScreen,
    )

    /**
     * The path the app is hosted under, with no trailing slash. The site may not be served from
     * the domain root (GitHub Pages serves it under /<repo>/ or /<repo>/pr-<n>/), so this is
     * whatever precedes the route in the URL the page was first loaded with.
     */
    val basePath: String = window.location.pathname
        .removeSuffix("/index.html")
        .let { path -> routes.map { it.first }.firstOrNull { path.endsWith(it) }?.let(path::removeSuffix) ?: path }
        .trimEnd('/')

    /** The route path for [screen], e.g. "/play"; "/" for home; null if it has no route. */
    fun pathFor(screen: Screen): String? = when (screen) {
        HomeScreen -> "/"
        else -> routes.firstOrNull { it.second == screen }?.first
    }

    fun screenFor(path: String): Screen =
        routes.firstOrNull { it.first == path }?.second ?: HomeScreen

    /** The current route path, e.g. "/play", or "/" for anything unrecognised. */
    fun currentPath(): String {
        val path = window.location.pathname.removeSuffix("/index.html").removePrefix(basePath).trimEnd('/')
        return path.ifEmpty { "/" }
    }

    fun urlFor(path: String): String = basePath + path

    /** The back stack to start with, so that "back" from a deep link goes to the home screen. */
    fun initialScreens(): List<Screen> {
        val screen = screenFor(currentPath())
        return if (screen == HomeScreen) listOf(HomeScreen) else listOf(HomeScreen, screen)
    }
}
