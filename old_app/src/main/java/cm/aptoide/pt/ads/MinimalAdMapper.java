package cm.aptoide.pt.ads;

import cm.aptoide.pt.database.room.RoomStoredMinimalAd;
import cm.aptoide.pt.dataprovider.model.MinimalAdInterface;
import cm.aptoide.pt.dataprovider.model.v2.GetAdsResponse;
import cm.aptoide.pt.search.model.SearchAdResult;

public class MinimalAdMapper {

  public MinimalAd map(GetAdsResponse.Ad ad) {
    GetAdsResponse.Partner partner = ad.getPartner();
    int id = 0;
    String clickUrl = null;
    if (partner != null) {
      id = partner.getInfo()
          .getId();
      clickUrl = partner.getData()
          .getClickUrl();
    }
    GetAdsResponse.Info.Payout payout = ad.getInfo()
        .getPayout();
    double appcAmount = -1;
    double fiatAmount = -1;
    String currency = "";
    String currencySymbol = "";
    boolean hasAppc = false;
    if (payout != null) {
      appcAmount = payout.getAppc();
      fiatAmount = payout.getFiat()
          .getAmount();
      currency = payout.getFiat()
          .getCurrency();
      currencySymbol = payout.getFiat()
          .getSymbol();
      hasAppc = true;
    }

    return new MinimalAd(ad.getData()
        .getPackageName(), id, clickUrl, ad.getInfo()
        .getCpcUrl(), ad.getInfo()
        .getCpdUrl(), ad.getData()
        .getId(), ad.getInfo()
        .getAdId(), ad.getInfo()
        .getCpiUrl(), ad.getData()
        .getName(), ad.getData()
        .getIcon(), ad.getData()
        .getDescription(), ad.getData()
        .getDownloads(), ad.getData()
        .getStars(), ad.getData()
        .getModified()
        .getTime(), hasAppc, appcAmount, fiatAmount, currency, currencySymbol);
  }

  public RoomStoredMinimalAd map(SearchAdResult searchAdResult, String referrer) {

    String packageName = searchAdResult.getPackageName();
    String cpcUrl = searchAdResult.getCpcUrl();
    String cpdUrl = searchAdResult.getClickPerDownloadUrl();
    String cpiUrl = searchAdResult.getClickPerInstallUrl();
    long adId = searchAdResult.getAdId();

    return new RoomStoredMinimalAd(packageName, referrer, cpcUrl, cpdUrl, cpiUrl, adId);
  }

  public MinimalAdInterface map(RoomStoredMinimalAd storedMinimalAd) {
    return new MinimalAdInterface() {

      public String cpdUrl;

      @Override public String getCpcUrl() {
        return storedMinimalAd.getCpcUrl();
      }

      @Override public String getCpdUrl() {
        return (cpdUrl == null) ? storedMinimalAd.getCpdUrl() : cpdUrl;
      }

      @Override public void setCpdUrl(String cpdUrl) {
        this.cpdUrl = cpdUrl;
      }

      @Override public String getCpiUrl() {
        return storedMinimalAd.getCpiUrl();
      }
    };
  }

  public MinimalAdInterface map(SearchAdResult searchAdResult) {
    return new MinimalAdInterface() {

      public String cpdUrl;

      @Override public String getCpcUrl() {
        return searchAdResult.getCpcUrl();
      }

      @Override public String getCpdUrl() {
        return (cpdUrl == null) ? searchAdResult.getClickPerDownloadUrl() : cpdUrl;
      }

      @Override public void setCpdUrl(String cpdUrl) {
        this.cpdUrl = cpdUrl;
      }

      @Override public String getCpiUrl() {
        return searchAdResult.getClickPerInstallUrl();
      }
    };
  }
}
