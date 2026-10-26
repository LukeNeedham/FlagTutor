package com.flagtutor.app

import com.flagtutor.app.ui.navigation.Route
import kotlinx.browser.window

/**
 * Maps routes to URL paths so the browser's address bar and back button follow in-app navigation.
 * Routes without a browser path (e.g. the debug game opened from the countries overview, which can't be rebuilt from a URL) are left
 * off the URL.
 *
 * A route's parents are the routes for each shorter path prefix, so "/debug/countries" sits on top
 * of "/debug", which sits on top of home.
 */
object BrowserRoutes {

    private val debugRoutes: List<Pair<String, Route>> = listOf(
        "/debug" to Route.AppDebug,
        "/debug/countries" to Route.CountriesOverview,
        "/debug/excluded" to Route.ExcludedCountries,
        "/debug/attempts" to Route.FlagAttempts,
        "/debug/sdk" to Route.Debug,
        "/debug/sdk/logs" to Route.Logs,
        "/debug/sdk/crashes" to Route.Crashes,
    )

    // Debug pages are only reachable from debug builds, so their URLs only work there too.
    private val routes: List<Pair<String, Route>> = listOf(
        "/play" to Route.PickCountryNameGame,
        "/credits" to Route.Credits,
        "/settings" to Route.Settings,
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

    /** The browser path for [route], e.g. "/play"; "/" for home; null if it has no browser path. */
    fun pathFor(route: Route): String? = when (route) {
        Route.Home -> "/"
        else -> routes.firstOrNull { it.second == route }?.first
    }

    fun routeFor(path: String): Route =
        routes.firstOrNull { it.first == path }?.second ?: Route.Home

    /** The current route path, e.g. "/play", or "/" for anything unrecognised. */
    fun currentPath(): String {
        val path = window.location.pathname.removeSuffix("/index.html").removePrefix(basePath).trimEnd('/')
        return path.ifEmpty { "/" }
    }

    fun urlFor(path: String): String = basePath + path

    /** The back stack to start with, so that "back" from a deep link walks up through its parents. */
    fun initialRoutes(): List<Route> {
        val path = currentPath()
        val parents = routes
            .filter { (route, _) -> path.startsWith("$route/") }
            .sortedBy { it.first.length }
            .map { it.second }
        val route = routeFor(path)
        return if (route == Route.Home) listOf(Route.Home) else listOf(Route.Home) + parents + route
    }
}
