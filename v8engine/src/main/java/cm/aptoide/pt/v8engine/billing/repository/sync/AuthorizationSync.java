/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 22/11/2016.
 */

package cm.aptoide.pt.v8engine.billing.repository.sync;

import android.content.SyncResult;
import cm.aptoide.pt.database.accessors.PaymentAuthorizationAccessor;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.dataprovider.ws.v3.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v3.GetPaymentAuthorizationsRequest;
import cm.aptoide.pt.v8engine.billing.Authorization;
import cm.aptoide.pt.v8engine.billing.Payer;
import cm.aptoide.pt.v8engine.billing.PaymentAnalytics;
import cm.aptoide.pt.v8engine.billing.repository.AuthorizationFactory;
import cm.aptoide.pt.v8engine.sync.RepositorySync;
import java.io.IOException;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Single;

public class AuthorizationSync extends RepositorySync {

  private final int paymentId;
  private final PaymentAuthorizationAccessor authorizationAccessor;
  private final AuthorizationFactory authorizationFactory;
  private final Payer payer;
  private final BodyInterceptor<BaseBody> bodyInterceptorV3;
  private final OkHttpClient httpClient;
  private final Converter.Factory converterFactory;
  private final PaymentAnalytics paymentAnalytics;
  private final String authorizationType;

  public AuthorizationSync(int paymentId, PaymentAuthorizationAccessor authorizationAccessor,
      AuthorizationFactory authorizationFactory, Payer payer,
      BodyInterceptor<BaseBody> bodyInterceptorV3, OkHttpClient httpClient,
      Converter.Factory converterFactory, PaymentAnalytics paymentAnalytics,
      String authorizationType) {
    this.paymentId = paymentId;
    this.authorizationAccessor = authorizationAccessor;
    this.authorizationFactory = authorizationFactory;
    this.payer = payer;
    this.bodyInterceptorV3 = bodyInterceptorV3;
    this.httpClient = httpClient;
    this.converterFactory = converterFactory;
    this.paymentAnalytics = paymentAnalytics;
    this.authorizationType = authorizationType;
  }

  @Override public void sync(SyncResult syncResult) {
    try {
      payer.getId()
          .flatMapCompletable(payerId -> getServerAuthorizations(payerId).doOnSuccess(
              authorizations -> saveAndReschedulePendingAuthorizations(authorizations, syncResult,
                  payerId))
              .toCompletable()
              .doOnError(
                  throwable -> saveAndRescheduleOnNetworkError(syncResult, throwable, payerId))
              .onErrorComplete())
          .await();
    } catch (RuntimeException e) {
      rescheduleSync(syncResult);
    }
  }

  private Single<List<Authorization>> getServerAuthorizations(String payerId) {
    return GetPaymentAuthorizationsRequest.of(bodyInterceptorV3, httpClient, converterFactory)
        .observe()
        .toSingle()
        .map(response -> authorizationFactory.convertToPaymentAuthorizations(response, payerId,
            paymentId, authorizationType));
  }

  private void saveAndReschedulePendingAuthorizations(List<Authorization> authorizations,
      SyncResult syncResult, String payerId) {

    for (Authorization authorization : authorizations) {

      if (authorization.isPending()) {
        rescheduleSync(syncResult);
      }

      authorizationAccessor.save(
          authorizationFactory.convertToDatabasePaymentAuthorization(authorization));

      paymentAnalytics.sendAuthorizationCompleteEvent(authorization);
    }
  }

  private void saveAndRescheduleOnNetworkError(SyncResult syncResult, Throwable throwable,
      String payerId) {
    if (throwable instanceof IOException) {
      paymentAnalytics.sendPaymentAuthorizationNetworkRetryEvent();
      rescheduleSync(syncResult);
    } else {
      final Authorization authorization =
          authorizationFactory.create(paymentId, Authorization.Status.UNKNOWN_ERROR, payerId,
              authorizationType);
      authorizationAccessor.save(
          authorizationFactory.convertToDatabasePaymentAuthorization(authorization));
      paymentAnalytics.sendAuthorizationCompleteEvent(authorization);
    }
  }
}