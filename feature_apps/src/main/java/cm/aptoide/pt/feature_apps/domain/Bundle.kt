package cm.aptoide.pt.feature_apps.domain

import cm.aptoide.pt.feature_apps.data.App
import cm.aptoide.pt.feature_editorial.data.ArticleType

open class Bundle(val title: String, val appsList: List<App>, val type: Type)

data class EditorialBundle(
  val editorialTitle: String,
  val summary: String,
  val image: String,
  val subtype: ArticleType,
  val date: String,
  val views: Long,
) :
  Bundle(editorialTitle, emptyList(), Type.EDITORIAL)

enum class Type {
  FEATURE_GRAPHIC, APP_GRID, ESKILLS, FEATURED_APPC, EDITORIAL, UNKNOWN_BUNDLE
}