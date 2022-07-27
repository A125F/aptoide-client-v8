package cm.aptoide.pt.download_view.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.aptoide.pt.download_view.domain.model.DownloadStateMapper
import cm.aptoide.pt.download_view.domain.usecase.DownloadAppUseCase
import cm.aptoide.pt.download_view.domain.usecase.ObserveDownloadUseCase
import cm.aptoide.pt.feature_apps.data.App
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewViewModel @Inject constructor(
  private val downloadAppUseCase: DownloadAppUseCase,
  private val observeDownloadUseCase: ObserveDownloadUseCase,
  private val downloadStateMapper: DownloadStateMapper
) :
  ViewModel() {

  private val viewModelState = MutableStateFlow(DownloadViewViewModelState())

  val uiState = viewModelState.map { it.toUiState() }
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value.toUiState()
    )

  init {
  }

  fun downloadApp(app: App) {
    viewModelScope.launch {
      //installManager.download(app.packageName)
    }
  }

  fun loadDownloadState(app: App) {
    viewModelScope.launch {
      observeDownloadUseCase.getDownload(app).collect { download ->
        viewModelState.update {
          it.copy(
            app = app,
            downloadViewState = downloadStateMapper.mapDownloadState(download.downloadState)
          )
        }
      }
    }
  }
}

private data class DownloadViewViewModelState(
  val app: App? = null,
  val downloadViewType: DownloadViewType = DownloadViewType.NO_APPCOINS,
  val downloadViewState: DownloadViewState = DownloadViewState.INSTALL
) {

  fun toUiState(): DownloadViewUiState =
    DownloadViewUiState(
      app, downloadViewType, downloadViewState
    )

}