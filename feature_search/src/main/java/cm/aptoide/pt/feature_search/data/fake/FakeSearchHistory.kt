package cm.aptoide.pt.feature_search.data.fake

import android.util.Log
import cm.aptoide.pt.feature_search.data.database.SearchHistoryRepository
import cm.aptoide.pt.feature_search.data.database.model.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Singleton
class FakeSearchHistory : SearchHistoryRepository {
  override fun getSearchHistory(): Flow<List<SearchHistoryEntity>> {
    val fakeList = arrayListOf(
      SearchHistoryEntity("Ana of the Larenz"),
      SearchHistoryEntity("Joao of the Andreide"),
      SearchHistoryEntity("Filips of the gonc of alves")
    )
    return flowOf(fakeList)
  }

  override fun addAppToSearchHistory(searchHistory: SearchHistoryEntity) {
    Log.d("FakeLocalSearchHistory", "Saved app " + searchHistory.appName)
  }

  override fun removeAppFromSearchHistory(searchHistory: SearchHistoryEntity) {
    Log.d("FakeLocalSearchHistory", "Removed app " + searchHistory.appName)
  }
}