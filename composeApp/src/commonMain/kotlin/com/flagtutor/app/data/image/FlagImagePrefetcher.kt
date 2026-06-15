package com.flagtutor.app.data.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import com.flagtutor.app.domain.model.Country

/** Queues flag images for download so they're cached on disk for offline play. */
class FlagImagePrefetcher(
    private val imageLoader: ImageLoader,
    private val platformContext: PlatformContext,
) {

    fun prefetch(countries: List<Country>) {
        countries.forEach { country ->
            val request = ImageRequest.Builder(platformContext)
                .data(country.flagUrl)
                .build()
            imageLoader.enqueue(request)
        }
    }
}
