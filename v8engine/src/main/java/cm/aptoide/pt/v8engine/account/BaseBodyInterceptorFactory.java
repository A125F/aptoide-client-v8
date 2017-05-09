package cm.aptoide.pt.v8engine.account;

import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.accountmanager.BasebBodyInterceptorFactory;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v7.BodyInterceptor;
import cm.aptoide.pt.v8engine.networking.BaseBodyInterceptorV3;
import cm.aptoide.pt.v8engine.networking.BaseBodyInterceptorV7;
import cm.aptoide.pt.v8engine.networking.IdsRepository;
import cm.aptoide.pt.v8engine.preferences.AdultContent;
import cm.aptoide.pt.v8engine.preferences.Preferences;
import cm.aptoide.pt.v8engine.preferences.SecurePreferences;

public class BaseBodyInterceptorFactory implements BasebBodyInterceptorFactory {

  private final IdsRepository idsRepository;
  private final Preferences preferences;
  private final SecurePreferences securePreferences;
  private final String aptoideMd5sum;
  private final String aptoidePackage;

  public BaseBodyInterceptorFactory(IdsRepository idsRepository, Preferences preferences,
      SecurePreferences securePreferences, String aptoideMd5sum, String aptoidePackage) {
    this.idsRepository = idsRepository;
    this.preferences = preferences;
    this.securePreferences = securePreferences;
    this.aptoideMd5sum = aptoideMd5sum;
    this.aptoidePackage = aptoidePackage;
  }

  @Override public BodyInterceptor<BaseBody> createV7(AptoideAccountManager accountManager) {
    return new BaseBodyInterceptorV7(idsRepository, accountManager,
        new AdultContent(accountManager, preferences, securePreferences), aptoideMd5sum,
        aptoidePackage);
  }

  @Override public BodyInterceptor<cm.aptoide.pt.dataprovider.ws.v3.BaseBody> createV3() {
    return new BaseBodyInterceptorV3(aptoideMd5sum, aptoidePackage, idsRepository);
  }
}