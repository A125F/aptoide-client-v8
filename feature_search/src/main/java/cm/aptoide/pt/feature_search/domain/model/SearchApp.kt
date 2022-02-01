package cm.aptoide.pt.feature_search.domain.model

data class SearchApp(
  val appName: String,
  val packageName: String,
  val rating: Double,
  val downloads: Int
)