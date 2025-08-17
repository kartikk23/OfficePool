package com.agile.officepool.responseDTO

data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean,
    val number: Int,
    val size: Int,
    val numberOfElements: Int
)

