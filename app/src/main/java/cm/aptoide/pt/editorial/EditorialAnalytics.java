package cm.aptoide.pt.editorial;

import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.implementation.navigation.NavigationTracker;
import cm.aptoide.pt.ads.WalletAdsOfferManager;
import cm.aptoide.pt.app.AppViewAnalytics;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.download.DownloadAnalytics;
import cm.aptoide.pt.install.InstallAnalytics;
import java.util.HashMap;

/**
 * Created by franciscocalado on 03/09/2018.
 */

public class EditorialAnalytics {
  public static final String CURATION_CARD_INSTALL = "Curation_Card_Install";
  public static final String EDITORIAL_BN_CURATION_CARD_INSTALL =
      "Editorial_BN_Curation_Card_Install";
  public static final String REACTION_INTERACT = "Reaction_Interact";

  private static final String APPLICATION_NAME = "Application Name";
  private static final String TYPE = "type";
  private static final String WHERE = "where";
  private static final String ACTION = "action";
  private static final String CURATION_DETAIL = "curation_detail";
  private static final String CONTEXT = "context";
  private final DownloadAnalytics downloadAnalytics;
  private final InstallAnalytics installAnalytics;
  private final AnalyticsManager analyticsManager;
  private final NavigationTracker navigationTracker;
  private final boolean fromHome;

  public EditorialAnalytics(DownloadAnalytics downloadAnalytics, AnalyticsManager analyticsManager,
      NavigationTracker navigationTracker, boolean fromHome, InstallAnalytics installAnalytics) {
    this.downloadAnalytics = downloadAnalytics;
    this.analyticsManager = analyticsManager;
    this.navigationTracker = navigationTracker;
    this.fromHome = fromHome;
    this.installAnalytics = installAnalytics;
  }

  public void setupDownloadEvents(Download download, int campaignId, String abTestGroup,
      AnalyticsManager.Action action, WalletAdsOfferManager.OfferResponseStatus offerResponseStatus,
      String trustedBadge, String storeName, String installType) {
    downloadAnalytics.installClicked(download.getMd5(), download.getPackageName(), action,
        offerResponseStatus, false, download.hasAppc(), download.hasSplits(), trustedBadge, null,
        storeName, installType);

    downloadAnalytics.downloadStartEvent(download, campaignId, abTestGroup,
        DownloadAnalytics.AppContext.EDITORIAL, action, false, false);
  }

  public void sendDownloadPauseEvent(String packageName) {
    downloadAnalytics.downloadInteractEvent(packageName, "pause");
  }

  public void sendDownloadCancelEvent(String packageName) {
    downloadAnalytics.downloadInteractEvent(packageName, "cancel");
  }

  public void clickOnInstallButton(String packageName, String type, boolean hasSplits,
      boolean hasBilling, boolean isMigration, String rank, String origin, String store) {
    String context = getViewName(true);
    String installEvent = CURATION_CARD_INSTALL;
    if (!fromHome) {
      installEvent = EDITORIAL_BN_CURATION_CARD_INSTALL;
    }
    HashMap<String, Object> map = new HashMap<>();
    map.put(APPLICATION_NAME, packageName);
    map.put(TYPE, type);
    map.put(CONTEXT, context);

    installAnalytics.clickOnInstallEvent(packageName, type, hasSplits, hasBilling, isMigration,
        rank, "unknown", origin, store, false);
    analyticsManager.logEvent(map, installEvent, AnalyticsManager.Action.CLICK, context);

    analyticsManager.logEvent(map, AppViewAnalytics.CLICK_INSTALL, AnalyticsManager.Action.CLICK,
        context);
  }

  private String getViewName(boolean isCurrent) {
    return navigationTracker.getViewName(isCurrent);
  }
}
