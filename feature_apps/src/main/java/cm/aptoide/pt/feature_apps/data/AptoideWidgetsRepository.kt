package cm.aptoide.pt.feature_apps.data

import cm.aptoide.pt.feature_apps.data.network.model.WidgetsJSON
import cm.aptoide.pt.feature_apps.data.network.service.WidgetsRemoteService
import cm.aptoide.pt.feature_apps.domain.Widget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class AptoideWidgetsRepository @Inject constructor(private val widgetsRemoteDataSource: WidgetsRemoteService) :
  WidgetsRepository {

  override fun getStoreWidgets() = flow {
    val widgetsList = widgetsRemoteDataSource.getStoreWidgets()
    if (widgetsList.isSuccessful) {
      widgetsList.body()?.datalist?.list?.let { emit(Result.Success(it.map { widgetNetwork -> widgetNetwork.toDomainModel() })) }
    } else {
      emit(Result.Error(IllegalStateException()))
    }
  }.flowOn(Dispatchers.IO)

  private fun WidgetsJSON.WidgetNetwork.toDomainModel(): Widget {
    return Widget(
      title = this.title!!
    )
  }
}