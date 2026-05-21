package com.tony.coreui.sample.data.repository

import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.sample.domain.model.DemoAlbum
import com.tony.coreui.sample.domain.model.DemoAlbumDetail
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailLoadMode
import com.tony.coreui.sample.domain.model.LibraryPreviewMode
import com.tony.coreui.sample.domain.repository.ShowcaseRepository
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class FakeShowcaseRepository : ShowcaseRepository {

    private val albums = listOf(
        DemoAlbum(
            id = 1L,
            title = "Focus Flow",
            artist = "CoreUI Sessions",
            summary = "The smallest amount of feature code with the biggest return in defaults.",
            trackCount = 12,
            badge = "Default-first",
            downloaded = true,
            focusReady = true
        ),
        DemoAlbum(
            id = 2L,
            title = "Night Shift",
            artist = "Signal Path",
            summary = "A slightly richer screen that keeps loading and errors predictable.",
            trackCount = 9,
            badge = "Overlay loading",
            downloaded = false,
            focusReady = true
        ),
        DemoAlbum(
            id = 3L,
            title = "Offline Mix",
            artist = "Local Cache",
            summary = "Perfect for showing cached content surviving dialog errors and retries.",
            trackCount = 15,
            badge = "Retry friendly",
            downloaded = true,
            focusReady = false
        ),
        DemoAlbum(
            id = 4L,
            title = "Control Room",
            artist = "Typed Routes",
            summary = "A good destination to demonstrate path args, query args, and custom screens.",
            trackCount = 7,
            badge = "Navigation lab",
            downloaded = false,
            focusReady = false
        )
    )

    private val refreshCounts = ConcurrentHashMap<Long, Int>()

    override suspend fun getAlbums(
        filter: DemoFilter,
        previewMode: LibraryPreviewMode
    ): Resource<List<DemoAlbum>> {
        delay(650)

        return when (previewMode) {
            LibraryPreviewMode.READY -> Resource.Success(filterAlbums(filter))
            LibraryPreviewMode.EMPTY -> Resource.Success(emptyList())
            LibraryPreviewMode.NETWORK_DIALOG -> Resource.Error(
                ResourceError.NetworkError(
                    message = "The sample catalog could not reach its remote source.",
                    httpCode = 503
                )
            )
            LibraryPreviewMode.VALIDATION_DIALOG -> Resource.Error(
                ResourceError.ValidationError(
                    message = "The current filter bundle is invalid. Reset or change the mode.",
                    field = "filter"
                )
            )
        }
    }

    override suspend fun getAlbumDetail(
        albumId: Long,
        mode: DetailLoadMode
    ): Resource<DemoAlbumDetail> {
        delay(
            when (mode) {
                DetailLoadMode.INITIAL -> 700
                DetailLoadMode.REFRESH -> 550
                DetailLoadMode.SERVICE_FAILURE -> 500
                DetailLoadMode.HOST_HANDLED_WARNING -> 300
            }
        )

        val album = albums.firstOrNull { it.id == albumId }
            ?: return Resource.Error(ResourceError.UnknownError)

        return when (mode) {
            DetailLoadMode.INITIAL -> Resource.Success(buildDetail(album, refreshed = false))
            DetailLoadMode.REFRESH -> {
                refreshCounts[albumId] = (refreshCounts[albumId] ?: 0) + 1
                Resource.Success(buildDetail(album, refreshed = true))
            }
            DetailLoadMode.SERVICE_FAILURE -> Resource.Error(
                ResourceError.ServiceError(
                    message = "The recommendation service is paused for maintenance.",
                    errorCode = "SERVICE_OUTAGE"
                )
            )
            DetailLoadMode.HOST_HANDLED_WARNING -> Resource.Error(
                ResourceError.LogicError(
                    errorMessage = "Background analytics are stale, but the cached screen is still usable.",
                    errorCode = "STALE_DEMO"
                )
            )
        }
    }

    private fun filterAlbums(filter: DemoFilter): List<DemoAlbum> = when (filter) {
        DemoFilter.ALL -> albums
        DemoFilter.FOCUS_READY -> albums.filter(DemoAlbum::focusReady)
        DemoFilter.DOWNLOADED -> albums.filter(DemoAlbum::downloaded)
    }

    private fun buildDetail(
        album: DemoAlbum,
        refreshed: Boolean
    ): DemoAlbumDetail {
        val refreshCount = refreshCounts[album.id] ?: 0
        val relatedAlbum = albums.firstOrNull { it.id != album.id }

        return DemoAlbumDetail(
            id = album.id,
            title = album.title,
            artist = album.artist,
            summary = album.summary,
            trackCount = album.trackCount,
            lastUpdatedLabel = if (refreshed) {
                "Refreshed $refreshCount time(s) during this session"
            } else {
                "Loaded from the default happy path"
            },
            defaultsHighlights = listOf(
                "BaseViewModel owns the full loading, success, and error lifecycle.",
                "The route came in through a typed path argument instead of a hand-built string.",
                "AppBaseScreen still provides the reusable shell even though the content is richer."
            ),
            customizationHighlights = listOf(
                "Overlay loading keeps the current content visible during refreshes.",
                "Service failures are remapped into a custom full-screen treatment.",
                "Host-managed warnings use UIErrorDisplayMode.NONE and render inline instead."
            ),
            relatedAlbumId = relatedAlbum?.id,
            relatedAlbumTitle = relatedAlbum?.title
        )
    }
}
