package cm.aptoide.pt.v8engine.networking;

import android.text.TextUtils;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.dataprovider.ws.Api;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.preferences.managed.ManagerPreferences;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.utils.q.QManager;
import cm.aptoide.pt.v8engine.preferences.AdultContent;
import rx.Single;
import rx.schedulers.Schedulers;

public class BaseBodyInterceptorV7 extends BaseBodyInterceptor<BaseBody> {

  private final AptoideAccountManager accountManager;
  private final AdultContent adultContent;

  public BaseBodyInterceptorV7(String aptoideMd5sum, String aptoidePackage,
      IdsRepository idsRepository, AptoideAccountManager accountManager, AdultContent adultContent,
      QManager qManager) {
    super(aptoideMd5sum, aptoidePackage, idsRepository, qManager);
    this.accountManager = accountManager;
    this.adultContent = adultContent;
  }

  public Single<BaseBody> intercept(BaseBody body) {
    return Single.zip(adultContent.enabled()
        .first()
        .toSingle(), accountManager.accountStatus()
        .first()
        .toSingle(), (adultContentEnabled, account) -> {
      if (account.isLoggedIn()) {
        body.setAccessToken(account.getAccessToken());
      }

      body.setAptoideId(idsRepository.getUniqueIdentifier());
      body.setAptoideVercode(AptoideUtils.Core.getVerCode());
      body.setCdn("pool");
      body.setLang(AptoideUtils.SystemU.getCountryCode());
      body.setMature(adultContentEnabled);
      if (ManagerPreferences.getHWSpecsFilter()) {
        body.setQ(Api.getQ());
      }
      if (ManagerPreferences.isDebug()) {
        String forceCountry = ManagerPreferences.getForceCountry();
        if (!TextUtils.isEmpty(forceCountry)) {
          body.setCountry(forceCountry);
        }
      }
      body.setAptoideMd5sum(aptoideMd5sum);
      body.setAptoidePackage(aptoidePackage);

      return body;
    })
        .subscribeOn(Schedulers.computation());
  }
}
