package cm.aptoide.pt.installedapps.data.database

import cm.aptoide.pt.installedapps.data.database.model.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

interface LocalInstalledAppsRepository {

  fun getInstalledApps(): Flow<List<InstalledAppEntity>>

  fun addInstalledApp(installedAppEntity: InstalledAppEntity)

  fun addListInstalledApps(installedAppEntityList: List<InstalledAppEntity>)

  fun removeInstalledApp(installedAppEntity: InstalledAppEntity)

  fun getInstalledApp(versionCode: Int, packageName: String): Flow<InstalledAppEntity>
}