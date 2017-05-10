/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 02/09/2016.
 */

package cm.aptoide.pt.v8engine.repository;

import android.content.Context;
import android.telephony.TelephonyManager;
import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.database.accessors.AccessorFactory;
import cm.aptoide.pt.database.realm.Download;
import cm.aptoide.pt.database.realm.Installed;
import cm.aptoide.pt.database.realm.PaymentAuthorization;
import cm.aptoide.pt.database.realm.PaymentConfirmation;
import cm.aptoide.pt.database.realm.Rollback;
import cm.aptoide.pt.database.realm.Scheduled;
import cm.aptoide.pt.database.realm.Store;
import cm.aptoide.pt.database.realm.Update;
import cm.aptoide.pt.dataprovider.NetworkOperatorManager;
import cm.aptoide.pt.dataprovider.ws.v7.BaseBody;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.iab.InAppBillingSerializer;
import cm.aptoide.pt.networkclient.WebService;
import cm.aptoide.pt.v8engine.V8Engine;
import cm.aptoide.pt.v8engine.app.AppRepository;
import cm.aptoide.pt.v8engine.download.ScheduledDownloadRepository;
import cm.aptoide.pt.v8engine.install.rollback.RollbackRepository;
import cm.aptoide.pt.v8engine.networking.IdsRepository;
import cm.aptoide.pt.v8engine.payment.AccountPayer;
import cm.aptoide.pt.v8engine.payment.Payer;
import cm.aptoide.pt.v8engine.payment.PaymentFactory;
import cm.aptoide.pt.v8engine.payment.Product;
import cm.aptoide.pt.v8engine.payment.ProductRepository;
import cm.aptoide.pt.v8engine.payment.PurchaseFactory;
import cm.aptoide.pt.v8engine.payment.products.InAppBillingProduct;
import cm.aptoide.pt.v8engine.payment.products.PaidAppProduct;
import cm.aptoide.pt.v8engine.payment.repository.InAppBillingProductRepository;
import cm.aptoide.pt.v8engine.payment.repository.InAppBillingRepository;
import cm.aptoide.pt.v8engine.payment.repository.InAppPaymentConfirmationRepository;
import cm.aptoide.pt.v8engine.payment.repository.PaidAppPaymentConfirmationRepository;
import cm.aptoide.pt.v8engine.payment.repository.PaidAppProductRepository;
import cm.aptoide.pt.v8engine.payment.repository.PaymentAuthorizationFactory;
import cm.aptoide.pt.v8engine.payment.repository.PaymentAuthorizationRepository;
import cm.aptoide.pt.v8engine.payment.repository.PaymentConfirmationFactory;
import cm.aptoide.pt.v8engine.payment.repository.PaymentConfirmationRepository;
import cm.aptoide.pt.v8engine.payment.repository.sync.PaymentSyncScheduler;
import cm.aptoide.pt.v8engine.store.StoreCredentialsProviderImpl;
import cm.aptoide.pt.v8engine.updates.UpdateRepository;
import okhttp3.OkHttpClient;

/**
 * Created by sithengineer on 02/09/16.
 */
public final class RepositoryFactory {

  public static ScheduledDownloadRepository getScheduledDownloadRepository() {
    return new ScheduledDownloadRepository(AccessorFactory.getAccessorFor(Scheduled.class));
  }

  public static RollbackRepository getRollbackRepository() {
    return new RollbackRepository(AccessorFactory.getAccessorFor(Rollback.class));
  }

  public static UpdateRepository getUpdateRepository(Context context) {
    return new UpdateRepository(AccessorFactory.getAccessorFor(Update.class),
        AccessorFactory.getAccessorFor(Store.class), getAccountManager(context),
        getIdsRepository(context), getBaseBodyInterceptorV7(context), getHttpClient(context),
        WebService.getDefaultConverter());
  }

  private static IdsRepository getIdsRepository(Context context) {
    return ((V8Engine) context.getApplicationContext()).getIdsRepository();
  }

  private static OkHttpClient getHttpClient(Context context) {
    return ((V8Engine) context.getApplicationContext()).getDefaultClient();
  }

  private static AptoideAccountManager getAccountManager(Context context) {
    return ((V8Engine) context.getApplicationContext()).getAccountManager();
  }

  public static cm.aptoide.pt.v8engine.repository.InstalledRepository getInstalledRepository() {
    return new cm.aptoide.pt.v8engine.repository.InstalledRepository(
        AccessorFactory.getAccessorFor(Installed.class));
  }

  public static cm.aptoide.pt.v8engine.repository.StoreRepository getStoreRepository() {
    return new cm.aptoide.pt.v8engine.repository.StoreRepository(
        AccessorFactory.getAccessorFor(Store.class));
  }

  public static cm.aptoide.pt.v8engine.repository.DownloadRepository getDownloadRepository() {
    return new cm.aptoide.pt.v8engine.repository.DownloadRepository(
        AccessorFactory.getAccessorFor(Download.class));
  }

  private static NetworkOperatorManager getNetworkOperatorManager(Context context) {
    return new NetworkOperatorManager(
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
  }

  public static AppRepository getAppRepository(Context context) {
    return new AppRepository(getNetworkOperatorManager(context), getAccountManager(context),
        getBaseBodyInterceptorV7(context), getBaseBodyInterceptorV3(context),
        new StoreCredentialsProviderImpl(), getHttpClient(context),
        WebService.getDefaultConverter());
  }

  private static BodyInterceptor<BaseBody> getBaseBodyInterceptorV7(Context context) {
    return ((V8Engine) context.getApplicationContext()).getBaseBodyInterceptorV7();
  }

  private static BodyInterceptor<cm.aptoide.pt.dataprovider.ws.v3.BaseBody> getBaseBodyInterceptorV3(
      Context context) {
    return ((V8Engine) context.getApplicationContext()).getBaseBodyInterceptorV3();
  }
}
