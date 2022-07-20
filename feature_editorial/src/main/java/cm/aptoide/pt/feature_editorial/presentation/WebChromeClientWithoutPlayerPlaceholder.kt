package cm.aptoide.pt.feature_editorial.presentation

import android.graphics.Bitmap
import android.webkit.WebChromeClient

class WebChromeClientWithoutPlayerPlaceholder : WebChromeClient() {
  override fun getDefaultVideoPoster(): Bitmap? {
    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
  }
}