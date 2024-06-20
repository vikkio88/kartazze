package io.github.vikkio88.kartazze

data class FilterParams(
    val pageSize: Int = 100,
    val pageNumber: Int = 1,
    val orderBy: String? = null,
    val desc: Boolean = false
) {
    val offset: Int = (pageNumber - 1) * pageSize
    val limit: Int = pageSize
}

fun filters(
    pageSize: Int = 100, pageNumber: Int = 1,
    orderBy: String? = null, desc: Boolean = false
): FilterParams {
    return FilterParams(pageSize, pageNumber, orderBy, desc)
}
