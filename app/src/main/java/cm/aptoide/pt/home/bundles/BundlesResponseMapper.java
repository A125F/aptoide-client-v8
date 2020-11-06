package cm.aptoide.pt.home.bundles;

import cm.aptoide.pt.ads.WalletAdsOfferCardManager;
import cm.aptoide.pt.blacklist.BlacklistManager;
import cm.aptoide.pt.bonus.BonusAppcModel;
import cm.aptoide.pt.dataprovider.model.v2.GetAdsResponse;
import cm.aptoide.pt.dataprovider.model.v7.AppCoinsCampaign;
import cm.aptoide.pt.dataprovider.model.v7.Event;
import cm.aptoide.pt.dataprovider.model.v7.GetStoreWidgets;
import cm.aptoide.pt.dataprovider.model.v7.Layout;
import cm.aptoide.pt.dataprovider.model.v7.ListAppCoinsCampaigns;
import cm.aptoide.pt.dataprovider.model.v7.ListApps;
import cm.aptoide.pt.dataprovider.model.v7.Type;
import cm.aptoide.pt.dataprovider.model.v7.listapp.App;
import cm.aptoide.pt.dataprovider.model.v7.listapp.AppCoinsInfo;
import cm.aptoide.pt.dataprovider.ws.v7.home.ActionItemData;
import cm.aptoide.pt.dataprovider.ws.v7.home.ActionItemResponse;
import cm.aptoide.pt.dataprovider.ws.v7.home.BonusAppcBundle;
import cm.aptoide.pt.dataprovider.ws.v7.home.EditorialActionItem;
import cm.aptoide.pt.home.bundles.ads.AdBundle;
import cm.aptoide.pt.home.bundles.ads.AdsTagWrapper;
import cm.aptoide.pt.home.bundles.apps.RewardApp;
import cm.aptoide.pt.home.bundles.base.ActionBundle;
import cm.aptoide.pt.home.bundles.base.ActionItem;
import cm.aptoide.pt.home.bundles.base.AppBundle;
import cm.aptoide.pt.home.bundles.base.EditorialActionBundle;
import cm.aptoide.pt.home.bundles.base.FeaturedAppcBundle;
import cm.aptoide.pt.home.bundles.base.HomeBundle;
import cm.aptoide.pt.install.InstallManager;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.view.app.Application;
import cm.aptoide.pt.view.app.FeatureGraphicApplication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jdandrade on 08/03/2018.
 */

public class BundlesResponseMapper {

  private final InstallManager installManager;
  private final WalletAdsOfferCardManager walletAdsOfferCardManager;
  private final BlacklistManager blacklistManager;

  public BundlesResponseMapper(InstallManager installManager,
      WalletAdsOfferCardManager walletAdsOfferCardManager, BlacklistManager blacklistManager) {
    this.installManager = installManager;
    this.walletAdsOfferCardManager = walletAdsOfferCardManager;
    this.blacklistManager = blacklistManager;
  }

  public List<HomeBundle> fromWidgetsToBundles(List<GetStoreWidgets.WSWidget> widgetBundles) {
    List<HomeBundle> appBundles = new ArrayList<>();

    for (GetStoreWidgets.WSWidget widget : widgetBundles) {
      AppBundle.BundleType type;
      try {
        if (widget.getType()
            .equals(Type.ACTION_ITEM)) {
          type = actionItemTypeMapper(widget);
        } else {
          type = bundleTypeMapper(widget.getType(), widget.getData());
        }

        if (type.equals(HomeBundle.BundleType.UNKNOWN)) continue;

        Event event = getEvent(widget);

        String widgetTag = widget.getTag();
        String widgetActionTag = getWidgetActionTag(widget);

        Object viewObject = widget.getViewObject();
        String title = widget.getTitle();
        if (event != null && event.getName()
            .equals(Event.Name.getStoreWidgets)) {
          event.setName(Event.Name.getMoreBundle);
        }
        if (type.equals(HomeBundle.BundleType.APPS)
            || type.equals(HomeBundle.BundleType.EDITORS)
            || type.equals(HomeBundle.BundleType.TOP)) {
          List<Application> apps = null;
          if (viewObject != null) {
            apps = map(((ListApps) viewObject).getDataList()
                .getList(), type, widgetTag);
          }
          appBundles.add(new AppBundle(title, apps, type, event, widgetTag, widgetActionTag));
        } else if (type.equals(HomeBundle.BundleType.FEATURED_BONUS_APPC)) {
          List<Application> apps = null;
          int percentage = -1;
          boolean hasBonus = true;
          if (viewObject instanceof BonusAppcBundle) {
            BonusAppcBundle bundle = (BonusAppcBundle) viewObject;
            hasBonus = bundle.getBonusAppcModel()
                .getHasBonusAppc();
            apps = map(bundle.getListApps()
                .getDataList()
                .getList(), type, widgetTag);
            percentage = bundle.getBonusAppcModel()
                .getBonusPercentage();
          }
          if (hasBonus) {
            appBundles.add(
                new FeaturedAppcBundle(title, apps, type, event, widgetTag, widgetActionTag,
                    percentage));
          } else {
            appBundles.add(new AppBundle(title, apps, HomeBundle.BundleType.APPS, event, widgetTag,
                widgetActionTag));
          }
        } else if (type.equals(HomeBundle.BundleType.APPCOINS_ADS)) {
          List<Application> applicationList = null;
          if (viewObject != null) {
            applicationList = map(((ListAppCoinsCampaigns) viewObject).getDataList()
                .getList(), widgetTag);
          }
          if (applicationList == null || !applicationList.isEmpty()) {
            appBundles.add(new AppBundle(title, applicationList, HomeBundle.BundleType.APPCOINS_ADS,
                new Event().setName(Event.Name.getAppCoinsAds), widgetTag, widgetActionTag));
          }
        } else if (type.equals(HomeBundle.BundleType.ADS)) {
          List<GetAdsResponse.Ad> adsList = null;
          if (viewObject != null) {
            adsList = ((GetAdsResponse) viewObject).getAds();
          }
          appBundles.add(new AdBundle(title, new AdsTagWrapper(adsList, widgetTag),
              new Event().setName(Event.Name.getAds), widgetTag));
        } else if (type.equals(HomeBundle.BundleType.EDITORIAL)) {

          if (viewObject instanceof EditorialActionItem) {
            EditorialActionItem editorialActionItem = ((EditorialActionItem) viewObject);
            BonusAppcModel bonusAppcModel = editorialActionItem.getBonusAppcModel();
            appBundles.add(new EditorialActionBundle(title, type, event, widgetTag,
                map(editorialActionItem.getActionItemResponse()), bonusAppcModel));
          } else {
            appBundles.add(new ActionBundle(title, type, event, widgetTag,
                map((ActionItemResponse) viewObject)));
          }
        } else if (type.equals(HomeBundle.BundleType.INFO_BUNDLE)) {
          ActionItem actionItem = map((ActionItemResponse) viewObject);
          if (actionItem == null || !blacklistManager.isBlacklisted(type.toString(),
              actionItem.getCardId())) {
            appBundles.add(new ActionBundle(title, type, event, widgetTag, actionItem));
          }
        } else if (type.equals(HomeBundle.BundleType.WALLET_ADS_OFFER)) {
          ActionItem actionItem = map((ActionItemResponse) viewObject);
          if (actionItem == null || walletAdsOfferCardManager.shouldShowWalletOfferCard(
              type.toString(), actionItem.getCardId())) {
            appBundles.add(new ActionBundle(title, type, event, widgetTag, actionItem));
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        Logger.getInstance()
            .d(this.getClass()
                    .getName(),
                "Something went wrong with widget to bundle mapping : " + e.getMessage());
      }
    }

    return appBundles;
  }

  private String getWidgetActionTag(GetStoreWidgets.WSWidget widget) {
    String widgetActionTag = "";
    if (widget.hasActions()) {
      widgetActionTag = widget.getActions()
          .get(0)
          .getTag();
    }
    return widgetActionTag;
  }

  private ActionItem map(ActionItemResponse viewObject) {
    if (viewObject == null) return null;

    ActionItemData item = viewObject.getDataList()
        .getList()
        .get(0);
    return new ActionItem(item.getId(), item.getType() != null ? item.getType() : "",
        item.getTitle(), item.getCaption(), item.getIcon(), item.getUrl(), item.getViews(),
        item.getDate(), item.getAppearance() != null ? item.getAppearance()
        .getCaption()
        .getTheme() : "", item.getFlair() != null ? item.getFlair() : "");
  }

  private HomeBundle.BundleType actionItemTypeMapper(GetStoreWidgets.WSWidget widget) {
    if (widget.getData() != null) {
      switch (widget.getData()
          .getLayout()) {
        case APPC_INFO:
          return HomeBundle.BundleType.INFO_BUNDLE;
        case CURATION_1:
          return HomeBundle.BundleType.EDITORIAL;
        case WALLET_ADS_OFFER:
          return HomeBundle.BundleType.WALLET_ADS_OFFER;
      }
    }
    return HomeBundle.BundleType.UNKNOWN;
  }

  private Event getEvent(GetStoreWidgets.WSWidget widget) {
    return widget.getActions() != null
        && widget.getActions()
        .size() > 0 ? widget.getActions()
        .get(0)
        .getEvent() : null;
  }

  private HomeBundle.BundleType bundleTypeMapper(Type type, GetStoreWidgets.WSWidget.Data data) {
    if (type == null) {
      return HomeBundle.BundleType.UNKNOWN;
    }
    switch (type) {
      case APPS_GROUP:
        if (data == null) {
          return HomeBundle.BundleType.UNKNOWN;
        }
        if (data.getLayout()
            .equals(Layout.BRICK)) {
          return HomeBundle.BundleType.EDITORS;
        } else {
          return HomeBundle.BundleType.APPS;
        }
      case APPCOINS_ADS:
        return HomeBundle.BundleType.APPCOINS_ADS;
      case APPCOINS_FEATURED:
        return HomeBundle.BundleType.FEATURED_BONUS_APPC;
      case ADS:
        return HomeBundle.BundleType.ADS;
      case APPS_TOP_GROUP:
        return HomeBundle.BundleType.TOP;
      default:
        return HomeBundle.BundleType.APPS;
    }
  }

  private List<Application> map(List<App> apps, AppBundle.BundleType type, String tag) {
    if (apps == null || apps.isEmpty()) {
      return Collections.emptyList();
    }
    List<Application> applications = new ArrayList<>();
    for (App app : apps) {
      try {
        if (type.equals(HomeBundle.BundleType.EDITORS)) {
          AppCoinsInfo appc = app.getAppcoins();
          applications.add(new FeatureGraphicApplication(app.getName(), app.getIcon(),
              app.getStats()
                  .getRating()
                  .getAvg(), app.getStats()
              .getPdownloads(), app.getPackageName(), app.getId(), app.getGraphic(), tag,
              appc != null && appc.hasBilling(), appc != null && appc.hasAdvertising()));
        } else {
          AppCoinsInfo appc = app.getAppcoins();
          applications.add(new Application(app.getName(), app.getIcon(), app.getStats()
              .getRating()
              .getAvg(), app.getStats()
              .getPdownloads(), app.getPackageName(), app.getId(), tag,
              appc != null && appc.hasBilling()));
        }
      } catch (Exception e) {
        Logger.getInstance()
            .d(this.getClass()
                    .getName(),
                "Something went wrong while parsing apps to applications: " + e.getMessage());
      }
    }

    return applications;
  }

  private List<Application> map(List<AppCoinsCampaign> appsList, String tag) {
    List<Application> rewardAppsList = new ArrayList<>();
    for (AppCoinsCampaign campaign : appsList) {
      AppCoinsCampaign.CampaignApp app = campaign.getApp();
      if (!installManager.wasAppEverInstalled(app.getPackageName())) {
        rewardAppsList.add(new RewardApp(app.getName(), app.getIcon(), app.getStats()
            .getRating()
            .getAvg(), app.getStats()
            .getPdownloads(), app.getPackageName(), app.getId(), tag, app.getAppcoins() != null,
            app.getAppcoins()
                .getClicks()
                .getClick(), app.getAppcoins()
            .getClicks()
            .getInstall(), mapReward(campaign.getReward()), app.getGraphic()));
      }
    }
    return rewardAppsList;
  }

  private RewardApp.Reward mapReward(AppCoinsCampaign.Reward reward) {
    AppCoinsCampaign.Fiat fiat = reward.getFiat();
    return new RewardApp.Reward(reward.getAppc(),
        new RewardApp.Fiat(fiat.getAmount(), fiat.getCurrency(), fiat.getSymbol()));
  }
}
