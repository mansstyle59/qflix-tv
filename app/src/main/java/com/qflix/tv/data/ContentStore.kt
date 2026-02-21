package com.qflix.tv.data

import com.qflix.tv.model.MediaItemQ

object ContentStore {
    @Volatile
    var lastLoadedItems: List<MediaItemQ> = emptyList()

    @Volatile
    var epgPrograms: List<EpgProgram> = emptyList()
}
