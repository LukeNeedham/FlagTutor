package com.flagtutor.app

import cafe.adriel.voyager.core.screen.Screen
import com.flagtutor.app.ui.navigation.CountriesOverviewNavKey
import com.flagtutor.app.ui.navigation.ExcludedCountriesNavKey
import com.flagtutor.app.ui.navigation.CrashesNavKey
import com.flagtutor.app.ui.navigation.CreditsNavKey
import com.flagtutor.app.ui.navigation.AppDebugNavKey
import com.flagtutor.app.ui.navigation.FlagAttemptsNavKey
import com.flagtutor.app.ui.navigation.HomeNavKey
import com.flagtutor.app.ui.navigation.LogsNavKey
import com.flagtutor.app.ui.navigation.PickCountryNameGameNavKey
import com.flagtutor.app.ui.navigation.DebugNavKey
import com.flagtutor.app.ui.navigation.SettingsNavKey
import kotlinx.browser.window

/**
 * Maps screens to URL paths so the browser's address bar and back button follow in-app navigation.
 * Screens without a route (e.g. the debug game opened from the countries overview, which can't be rebuilt from a URL) are left
 * off the URL.
 *
 * A route's parents are the routes for each shorter path prefix, so "/debug/countries" sits on top
 * of "/debug", which sits on top of home.
 */
object BrowserRoutes {

    private val debugRoutes: List<Pair<String, Screen>> = listOf(
        "/debug" to AppDebugNavKey,
        "/debug/countries" to CountriesOverviewNavKey,
        "/debug/excluded" to ExcludedCountriesNavKey,
        "/debug/attempts" to FlagAttemptsNavKey,
        "/debug/sdk" to DebugNavKey,
        "/debug/sdk/logs" to LogsNavKey,
        "/debug/sdk/crashes" to CrashesNavKey,
    )

    // Debug pages are only reachable from debug builds, so their URLs only work there too.
    private val routes: List<Pair<String, Screen>> = listOf(
        "/play" to PickCountryNameGameNavKey,
        "/credits" to CreditsNavKey,
        "/settings" to SettingsNavKey,
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
        HomeNavKey -> "/"
        else -> routes.firstOrNull { it.second == screen }?.first
    }

    fun screenFor(path: String): Screen =
        routes.firstOrNull { it.first == path }?.second ?: HomeNavKey

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
        return if (screen == HomeNavKey) listOf(HomeNavKey) else listOf(HomeNavKey) + parents + screen
    }
}
