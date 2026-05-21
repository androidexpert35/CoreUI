package com.tony.coreui.sample.domain.repository

import com.tony.coreui.domain.resource.Resource
import com.tony.coreui.sample.domain.model.DemoAlbum
import com.tony.coreui.sample.domain.model.DemoAlbumDetail
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailLoadMode
import com.tony.coreui.sample.domain.model.LibraryPreviewMode

interface ShowcaseRepository {
    suspend fun getAlbums(
        filter: DemoFilter,
        previewMode: LibraryPreviewMode
    ): Resource<List<DemoAlbum>>

    suspend fun getAlbumDetail(
        albumId: Long,
        mode: DetailLoadMode = DetailLoadMode.INITIAL
    ): Resource<DemoAlbumDetail>
}
