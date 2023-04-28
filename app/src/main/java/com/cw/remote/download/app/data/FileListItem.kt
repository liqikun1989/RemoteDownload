package com.cw.remote.download.app.data

data class FileListItem(
    var icon: Int,
    var name: String,
    var path: String,
    var isShowPath: Boolean,
    var isDirectory: Boolean
)
