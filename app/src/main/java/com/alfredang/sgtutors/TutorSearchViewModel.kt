package com.alfredang.sgtutors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TutorSearchViewModel : ViewModel() {
    var query by mutableStateOf("")
        private set
    var subject by mutableStateOf<Subject?>(null)
        private set
    var level by mutableStateOf<Level?>(null)
        private set
    var region by mutableStateOf<String?>(null)
        private set
    var gender by mutableStateOf<String?>(null)
        private set

    var tutors by mutableStateOf<List<PublicTutor>>(emptyList())
        private set
    var featured by mutableStateOf<List<PublicTutor>>(emptyList())
        private set
    var subjects by mutableStateOf<List<Subject>>(emptyList())
        private set
    var levels by mutableStateOf<List<Level>>(emptyList())
        private set
    var total by mutableStateOf(0)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    private var page = 1
    private var searchJob: Job? = null
    private var loadedOnce = false

    val hasFilters: Boolean
        get() = subject != null || level != null || region != null ||
            gender != null || query.isNotEmpty()

    val canLoadMore: Boolean
        get() = tutors.size < total

    fun initialLoad() {
        if (loadedOnce) return
        loadedOnce = true
        viewModelScope.launch {
            val f = async { runCatching { ApiClient.featuredTutors() } }
            val s = async { runCatching { ApiClient.subjects() } }
            val l = async { runCatching { ApiClient.levels() } }
            featured = f.await().getOrDefault(emptyList())
            subjects = s.await().getOrDefault(emptyList())
            levels = l.await().getOrDefault(emptyList())
            search(reset = true)
        }
    }

    fun setQueryText(value: String) {
        query = value
        debouncedSearch()
    }

    fun setSubjectFilter(value: Subject?) {
        subject = value
        debouncedSearch()
    }

    fun setLevelFilter(value: Level?) {
        level = value
        debouncedSearch()
    }

    fun setRegionFilter(value: String?) {
        region = value
        debouncedSearch()
    }

    fun setGenderFilter(value: String?) {
        gender = value
        debouncedSearch()
    }

    fun retry() {
        viewModelScope.launch { search(reset = true) }
    }

    fun refresh() {
        viewModelScope.launch { search(reset = true) }
    }

    fun loadMore() {
        if (isLoading || !canLoadMore) return
        page += 1
        viewModelScope.launch { search(reset = false) }
    }

    private fun debouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            search(reset = true)
        }
    }

    private suspend fun search(reset: Boolean) {
        if (reset) page = 1
        isLoading = true
        loadError = null
        try {
            val result = ApiClient.searchTutors(
                q = query, subject = subject?.slug, level = level?.slug,
                region = region, gender = gender, page = page,
            )
            total = result.total
            tutors = if (reset) result.items else tutors + result.items
        } catch (e: Exception) {
            loadError = e.message ?: "Something went wrong"
        }
        isLoading = false
    }
}
