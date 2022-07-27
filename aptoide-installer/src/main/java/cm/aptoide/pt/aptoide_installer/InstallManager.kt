package cm.aptoide.pt.aptoide_installer

import cm.aptoide.pt.aptoide_installer.model.Download
import cm.aptoide.pt.feature_apps.data.App
import kotlinx.coroutines.flow.Flow

interface InstallManager {

  fun start()

  fun download(download: Download)

  fun install(packageName: String)

  fun getDownload(app: App): Flow<Download>

  fun cancelDownload(packageName: String)

  fun getActiveDownloads(packageName: String)

}