package com.tony.coreui.sample.domain.repository

import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.sample.domain.model.DemoAlbum
import com.tony.coreui.sample.domain.model.DemoAlbumDetail
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailLoadMode
import com.tony.coreui.sample.domain.model.LibraryPreviewMode

/**
 * Domain contract for the sample app's showcase data.
 *
 * The API is intentionally small but covers both the defaults-first list example and the more
 * customizable detail screen.
 */
interface ShowcaseRepository {
    /** Returns the album list for the given filter and demo preview mode. */
    suspend fun getAlbums(
        filter: DemoFilter,
        previewMode: LibraryPreviewMode
    ): Resource<List<DemoAlbum>>

    /** Returns the detail payload for a single album and demo load mode. */
    suspend fun getAlbumDetail(
        albumId: Long,
        mode: DetailLoadMode = DetailLoadMode.INITIAL
    ): Resource<DemoAlbumDetail>
}
