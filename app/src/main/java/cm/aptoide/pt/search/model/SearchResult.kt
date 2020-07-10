package cm.aptoide.pt.search.model

import cm.aptoide.pt.search.SearchResultDiffModel

sealed class SearchResult {
  data class Success(val result: SearchResultDiffModel) : SearchResult()
  data class Error(val error: SearchResultError) : SearchResult()
}

enum class SearchResultError {
  NO_NETWORK, GENERIC
}