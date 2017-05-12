/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 22/11/2016.
 */

package cm.aptoide.pt.v8engine.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import cm.aptoide.pt.database.accessors.PaymentAuthorizationAccessor;
import cm.aptoide.pt.database.accessors.PaymentConfirmationAccessor;
import cm.aptoide.pt.dataprovider.NetworkOperatorManager;
import cm.aptoide.pt.dataprovider.ws.v3.BaseBody;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.v8engine.payment.Payer;
import cm.aptoide.pt.v8engine.payment.PaymentAnalytics;
import cm.aptoide.pt.v8engine.payment.Product;
import cm.aptoide.pt.v8engine.payment.repository.PaymentAuthorizationFactory;
import cm.aptoide.pt.v8engine.payment.repository.PaymentConfirmationFactory;
import cm.aptoide.pt.v8engine.payment.repository.sync.AuthorizationSync;
import cm.aptoide.pt.v8engine.payment.repository.sync.ConfirmationSync;
import cm.aptoide.pt.v8engine.payment.repository.sync.ProductBundleMapper;
import cm.aptoide.pt.v8engine.repository.RepositoryFactory;
import okhttp3.OkHttpClient;
import retrofit2.Converter;

/**
 * Created by marcelobenites on 18/11/16.
 */

public class AptoideSyncAdapter extends AbstractThreadedSyncAdapter {

  public static final String EXTRA_PAYMENT_CONFIRMATION_ID =
      "cm.aptoide.pt.v8engine.repository.sync.PAYMENT_CONFIRMATION_ID";
  public static final String EXTRA_PAYMENT_ID =
      "cm.aptoide.pt.v8engine.repository.sync.PAYMENT_ID";
  public static final String EXTRA_PAYMENT_AUTHORIZATIONS =
      "cm.aptoide.pt.v8engine.repository.sync.EXTRA_PAYMENT_AUTHORIZATIONS";
  public static final String EXTRA_PAYMENT_CONFIRMATIONS =
      "cm.aptoide.pt.v8engine.repository.sync.EXTRA_PAYMENT_CONFIRMATIONS";

  private final ProductBundleMapper productConverter;
  private final NetworkOperatorManager operatorManager;
  private final PaymentConfirmationFactory confirmationConverter;
  private final PaymentAuthorizationFactory authorizationConverter;
  private final PaymentConfirmationAccessor confirmationAccessor;
  private final PaymentAuthorizationAccessor authorizationAcessor;
  private final BodyInterceptor<BaseBody> bodyInterceptorV3;
  private final OkHttpClient httpClient;
  private final Converter.Factory converterFactory;
  private final PaymentAnalytics paymentAnalytics;
  private final Payer payer;

  public AptoideSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs,
      PaymentConfirmationFactory confirmationConverter,
      PaymentAuthorizationFactory authorizationConverter, ProductBundleMapper productConverter,
      NetworkOperatorManager operatorManager, PaymentConfirmationAccessor confirmationAccessor,
      PaymentAuthorizationAccessor authorizationAcessor, BodyInterceptor<BaseBody> bodyInterceptorV3, OkHttpClient httpClient,
      Converter.Factory converterFactory, PaymentAnalytics paymentAnalytics, Payer payer) {
    super(context, autoInitialize, allowParallelSyncs);
    this.confirmationConverter = confirmationConverter;
    this.authorizationConverter = authorizationConverter;
    this.productConverter = productConverter;
    this.operatorManager = operatorManager;
    this.confirmationAccessor = confirmationAccessor;
    this.authorizationAcessor = authorizationAcessor;
    this.bodyInterceptorV3 = bodyInterceptorV3;
    this.converterFactory = converterFactory;
    this.httpClient = httpClient;
    this.paymentAnalytics = paymentAnalytics;
    this.payer = payer;
  }

  @Override public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    final boolean authorizations = extras.getBoolean(EXTRA_PAYMENT_AUTHORIZATIONS);
    final boolean confirmations = extras.getBoolean(EXTRA_PAYMENT_CONFIRMATIONS);

    final int paymentId = extras.getInt(EXTRA_PAYMENT_ID);

    if (confirmations) {
      final Product product = productConverter.mapToProduct(extras);
      final String paymentConfirmationId = extras.getString(EXTRA_PAYMENT_CONFIRMATION_ID);

      if (paymentConfirmationId == null) {
        new ConfirmationSync(
            RepositoryFactory.getPaymentConfirmationRepository(getContext(), product), product,
            operatorManager, confirmationAccessor, confirmationConverter, payer,
            bodyInterceptorV3, converterFactory, httpClient, paymentAnalytics).sync(syncResult);
      } else {
        new ConfirmationSync(
            RepositoryFactory.getPaymentConfirmationRepository(getContext(), product), product,
            operatorManager, confirmationAccessor, confirmationConverter, paymentConfirmationId,
            paymentId, payer, bodyInterceptorV3, converterFactory,
            httpClient, paymentAnalytics).sync(syncResult);
      }
    } else if (authorizations) {
      new AuthorizationSync(paymentId, authorizationAcessor, authorizationConverter, payer,
          bodyInterceptorV3, httpClient, converterFactory, paymentAnalytics).sync(syncResult);
    }
  }
}