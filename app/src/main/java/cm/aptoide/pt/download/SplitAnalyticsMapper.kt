package cm.aptoide.pt.download

import cm.aptoide.pt.database.room.RoomFileToDownload

class SplitAnalyticsMapper {

  fun getSplitTypesForAnalytics(splitsList: List<RoomFileToDownload>): String {
    val hasBase = splitsList.isNotEmpty()
    var hasPFD = false
    var hasPAD = false

    for (roomFileToDownload in splitsList) {
      if (roomFileToDownload.subFileType == RoomFileToDownload.FEATURE) {
        hasPFD = true
      } else if (roomFileToDownload.subFileType == RoomFileToDownload.ASSET) {
        hasPAD = true
      }
    }
    return buildSplitTypesAnalyticsString(hasBase, hasPFD, hasPAD)
  }

  private fun buildSplitTypesAnalyticsString(hasBase: Boolean, hasPFD: Boolean,
                                             hasPAD: Boolean): String {
    var splits = "false"

    if (!hasBase) {
      splits = "false"
    } else if (hasBase && !hasPAD && !hasPFD) {
      splits = "base"
    } else if (hasBase && hasPAD && !hasPFD) {
      splits = "PAD"
    } else if (hasBase && !hasPAD && hasPFD) {
      splits = "PFD"
    } else if (hasBase && hasPAD && hasPFD) {
      splits = "PAD+PFD"
    }
    return splits
  }
}