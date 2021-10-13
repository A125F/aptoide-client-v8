package cm.aptoide.pt.promotions;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.pt.aab.DynamicSplitsManager;
import cm.aptoide.pt.ads.MoPubAdsManager;
import cm.aptoide.pt.ads.WalletAdsOfferManager;
import cm.aptoide.pt.app.DownloadStateParser;
import cm.aptoide.pt.database.room.RoomDownload;
import cm.aptoide.pt.download.DownloadAnalytics;
import cm.aptoide.pt.download.DownloadFactory;
import cm.aptoide.pt.install.InstallAnalytics;
import cm.aptoide.pt.install.InstallManager;
import cm.aptoide.pt.install.InstalledRepository;
import cm.aptoide.pt.notification.NotificationAnalytics;
import cm.aptoide.pt.wallet.WalletAppProvider;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.Single;

public class PromotionsManager {

  private static final String WALLET_PACKAGE_NAME = "com.appcoins.wallet";
  private final PromotionViewAppMapper promotionViewAppMapper;
  private final InstallManager installManager;
  private final DownloadFactory downloadFactory;
  private final DownloadStateParser downloadStateParser;
  private final PromotionsAnalytics promotionsAnalytics;
  private final NotificationAnalytics notificationAnalytics;
  private final InstallAnalytics installAnalytics;
  private final PackageManager packageManager;
  private final PromotionsService promotionsService;
  private final InstalledRepository installedRepository;
  private final MoPubAdsManager moPubAdsManager;
  private final WalletAppProvider walletAppProvider;
  private final DynamicSplitsManager dynamicSplitsManager;

  public PromotionsManager(PromotionViewAppMapper promotionViewAppMapper,
      InstallManager installManager, DownloadFactory downloadFactory,
      DownloadStateParser downloadStateParser, PromotionsAnalytics promotionsAnalytics,
      NotificationAnalytics notificationAnalytics, InstallAnalytics installAnalytics,
      PackageManager packageManager, PromotionsService promotionsService,
      InstalledRepository installedRepository, MoPubAdsManager moPubAdsManager,
      WalletAppProvider walletAppProvider, DynamicSplitsManager dynamicSplitsManager) {
    this.promotionViewAppMapper = promotionViewAppMapper;
    this.installManager = installManager;
    this.downloadFactory = downloadFactory;
    this.downloadStateParser = downloadStateParser;
    this.promotionsAnalytics = promotionsAnalytics;
    this.notificationAnalytics = notificationAnalytics;
    this.installAnalytics = installAnalytics;
    this.packageManager = packageManager;
    this.promotionsService = promotionsService;
    this.installedRepository = installedRepository;
    this.moPubAdsManager = moPubAdsManager;
    this.walletAppProvider = walletAppProvider;
    this.dynamicSplitsManager = dynamicSplitsManager;
  }

  public Single<List<PromotionApp>> getPromotionApps(String promotionId) {
    return promotionsService.getPromotionApps(promotionId);
  }

  public Single<PromotionsModel> getPromotionsModel(String promotionsType) {
    return promotionsService.getPromotions(promotionsType)
        .flatMap(promotions -> {
          if (promotions.isEmpty()) {
            return Single.just(PromotionsModel.ofError());
          }
          PromotionMeta meta = promotions.get(0);
          return getPromotionApps(meta.getPromotionId()).map(
              appsList -> new PromotionsModel(meta.getPromotionId(), appsList, meta.getTitle(),
                  meta.getBackground(), isWalletInstalled(), false, meta.getDescription(),
                  meta.getDialogDescription()));
        });
  }

  public Observable<List<Promotion>> getPromotionsForPackage(String packageName) {
    return promotionsService.getPromotionsForPackage(packageName)
        .toObservable()
        .flatMapIterable(list -> list)
        .map(this::mapPromotionAction)
        .toList();
  }

  // This locally defines what action does the user need to do to claim a promotions
  // It should probably be refactored so it is given by the WS
  private Promotion mapPromotionAction(Promotion promotion) {
    switch (promotion.getPromotionId()) {
      case "BONUS_MIGRATION_19":
        promotion.setClaimActions(Collections.singletonList(Promotion.ClaimAction.MIGRATE));
        break;
      case "BONUS_GAME_WALLET_OFFER_19":
        promotion.setClaimActions(
            Arrays.asList(Promotion.ClaimAction.INSTALL, Promotion.ClaimAction.MIGRATE));
        break;
    }
    return promotion;
  }

  private boolean isWalletInstalled() {
    for (ApplicationInfo applicationInfo : packageManager.getInstalledApplications(0)) {
      if (applicationInfo.packageName.equals(WALLET_PACKAGE_NAME)) {
        return true;
      }
    }
    return false;
  }

  private int getTotalAppc(List<PromotionApp> appsList) {
    int total = 0;
    for (PromotionApp promotionApp : appsList) {
      total += promotionApp.getAppcValue();
    }
    return total;
  }

  public Observable<PromotionViewApp> getDownload(PromotionApp promotionApp) {
    return installManager.getInstall(promotionApp.getMd5(), promotionApp.getPackageName(),
        promotionApp.getVersionCode())
        .map(install -> promotionViewAppMapper.mapInstallToPromotionApp(install, promotionApp));
  }

  public boolean shouldShowRootInstallWarningPopup() {
    return installManager.showWarning();
  }

  public void allowRootInstall(Boolean answer) {
    installManager.rootInstallAllowed(answer);
  }

  public Completable downloadApp(PromotionViewApp promotionViewApp) {
    return RxJavaInterop.toV1Single(
        dynamicSplitsManager.getAppSplitsByMd5(promotionViewApp.getMd5()))
        .flatMapObservable(dynamicSplitsModel -> Observable.just(downloadFactory.create(
            downloadStateParser.parseDownloadAction(promotionViewApp.getDownloadModel()
                .getAction()), promotionViewApp.getName(), promotionViewApp.getPackageName(),
            promotionViewApp.getMd5(), promotionViewApp.getAppIcon(),
            promotionViewApp.getVersionName(), promotionViewApp.getVersionCode(),
            promotionViewApp.getDownloadPath(), promotionViewApp.getAlternativePath(),
            promotionViewApp.getObb(), promotionViewApp.hasAppc(), promotionViewApp.getSize(),
            promotionViewApp.getSplits(), promotionViewApp.getRequiredSplits(),
            promotionViewApp.getRank(), promotionViewApp.getStoreName(),
            dynamicSplitsModel.getDynamicSplitsList())))
        .flatMapSingle(download -> moPubAdsManager.getAdsVisibilityStatus()
            .doOnSuccess(offerResponseStatus -> setupDownloadEvents(download,
                promotionViewApp.getPackageName(), promotionViewApp.getAppId(),
                offerResponseStatus))
            .map(__ -> download))
        .flatMapCompletable(download -> installManager.install(download))
        .toCompletable();
  }

  private void setupDownloadEvents(RoomDownload download, String packageName, long appId,
      WalletAdsOfferManager.OfferResponseStatus offerResponseStatus) {
    int campaignId = notificationAnalytics.getCampaignId(packageName, appId);
    String abTestGroup = notificationAnalytics.getAbTestingGroup(packageName, appId);
    promotionsAnalytics.setupDownloadEvents(download, campaignId, abTestGroup,
        AnalyticsManager.Action.CLICK, offerResponseStatus,
        downloadStateParser.getOrigin(download.getAction()), download.hasSplits());
    installAnalytics.installStarted(download.getPackageName(), download.getVersionCode(),
        AnalyticsManager.Action.INSTALL, DownloadAnalytics.AppContext.PROMOTIONS,
        downloadStateParser.getOrigin(download.getAction()), campaignId, abTestGroup, false,
        download.hasAppc(), download.hasSplits(), offerResponseStatus.toString(),
        download.getTrustedBadge(), download.getStoreName(), false);
  }

  public Completable pauseDownload(String md5) {
    return installManager.pauseInstall(md5);
  }

  public Completable cancelDownload(String md5, String packageName, int versionCode) {
    return installManager.cancelInstall(md5, packageName, versionCode);
  }

  public Completable resumeDownload(String md5, String packageName, long appId) {
    return installManager.getDownload(md5)
        .flatMap(download -> moPubAdsManager.getAdsVisibilityStatus()
            .doOnSuccess(offerResponseStatus -> setupDownloadEvents(download, packageName, appId,
                offerResponseStatus))
            .map(__ -> download))
        .flatMapCompletable(download -> installManager.install(download));
  }

  public void saveWalletAddress(String walletAddress) {
    promotionsService.saveWalletAddress(walletAddress);
  }

  public String getWalletAddress() {
    return promotionsService.getWalletAddress();
  }

  public Single<ClaimStatusWrapper> claimPromotion(String walletAddress, String packageName,
      String promotionId) {
    return promotionsService.claimPromotion(walletAddress, packageName, promotionId);
  }

  public Observable<String> getPackageSignature(String packageName) {
    return installedRepository.getInstalled(packageName)
        .map(installed -> {
          if (installed != null) {
            return installed.getSignature();
          } else {
            return "";
          }
        });
  }

  public Observable<WalletApp> getWalletApp() {
    return walletAppProvider.getWalletApp();
  }

  /**
   * Retrieves the first claimable promotion for an action
   */
  public Promotion getClaimablePromotion(List<Promotion> promotions,
      Promotion.ClaimAction claimAction) {
    Promotion claimablePromotion = null;
    for (Promotion promotion : promotions) {
      if (promotion.getClaimActions()
          .contains(claimAction) && promotion.isClaimable()) {
        claimablePromotion = promotion;
        break;
      }
    }
    return claimablePromotion;
  }
}