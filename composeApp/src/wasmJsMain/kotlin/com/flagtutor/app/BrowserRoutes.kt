package com.flagtutor.app

import cafe.adriel.voyager.core.screen.Screen
import com.flagtutor.app.ui.navigation.CountriesOverviewScreen
import com.flagtutor.app.ui.navigation.CreditsScreen
import com.flagtutor.app.ui.navigation.CrashesScreen
import com.flagtutor.app.ui.navigation.DebugScreen
import com.flagtutor.app.ui.navigation.DebugSettingsScreen
import com.flagtutor.app.ui.navigation.FlagAttemptsScreen
import com.flagtutor.app.ui.navigation.HomeScreen
import com.flagtutor.app.ui.navigation.PickCountryNameGameScreen
import kotlinx.browser.window

/**
 * Maps screens to URL paths so the browser's address bar and back button follow in-app navigation.
 * Screens without a route (e.g. a crash's detail page, which can't be rebuilt from a URL) are left
 * off the URL.
 *
 * A route's parents are the routes for each shorter path prefix, so "/debug/countries" sits on top
 * of "/debug", which sits on top of home.
 */
object BrowserRoutes {

    private val debugRoutes: List<Pair<String, Screen>> = listOf(
        "/debug" to DebugScreen,
        "/debug/countries" to CountriesOverviewScreen,
        "/debug/attempts" to FlagAttemptsScreen,
        "/debug/crashes" to CrashesScreen,
        "/debug/settings" to DebugSettingsScreen,
    )

    // Debug pages are only reachable from debug builds, so their URLs only work there too.
    private val routes: List<Pair<String, Screen>> = listOf(
        "/play" to PickCountryNameGameScreen,
        "/credits" to CreditsScreen,
    ) + if (isDebugBuild) debugRoutes else emptyList()

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

    /** The back stack to start with, so that "back" from a deep link walks up through its parents. */
    fun initialScreens(): List<Screen> {
        val path = currentPath()
        val parents = routes
            .filter { (route, _) -> path.startsWith("$route/") }
            .sortedBy { it.first.length }
            .map { it.second }
        val screen = screenFor(path)
        return if (screen == HomeScreen) listOf(HomeScreen) else listOf(HomeScreen) + parents + screen
    }
}
