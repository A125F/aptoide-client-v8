/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 22/11/2016.
 */

package cm.aptoide.pt.v8engine.payment.repository;

import cm.aptoide.accountmanager.AptoideAccountManager;
import cm.aptoide.pt.database.accessors.PaymentAuthorizationAccessor;
import cm.aptoide.pt.dataprovider.ws.v3.BaseBody;
import cm.aptoide.pt.dataprovider.ws.v3.CreatePaymentAuthorizationRequest;
import cm.aptoide.pt.dataprovider.ws.v3.V3;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.v8engine.payment.Authorization;
import cm.aptoide.pt.v8engine.payment.Payer;
import cm.aptoide.pt.v8engine.payment.repository.sync.PaymentSyncScheduler;
import cm.aptoide.pt.v8engine.repository.exception.RepositoryIllegalArgumentException;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Completable;
import rx.Observable;

public class PaymentAuthorizationRepository {

  private final PaymentAuthorizationAccessor authotizationAccessor;
  private final PaymentSyncScheduler backgroundSync;
  private final PaymentAuthorizationFactory authorizationFactory;
  private final AptoideAccountManager accountManager;
  private final BodyInterceptor<BaseBody> bodyInterceptorV3;
  private final OkHttpClient httpClient;
  private final Converter.Factory converterFactory;
  private final Payer payer;

  public PaymentAuthorizationRepository(PaymentAuthorizationAccessor authorizationAccessor,
      PaymentSyncScheduler backgroundSync, PaymentAuthorizationFactory authorizationFactory,
      AptoideAccountManager accountManager, BodyInterceptor<BaseBody> bodyInterceptorV3,
      OkHttpClient httpClient, Converter.Factory converterFactory, Payer payer) {
    this.authotizationAccessor = authorizationAccessor;
    this.backgroundSync = backgroundSync;
    this.authorizationFactory = authorizationFactory;
    this.accountManager = accountManager;
    this.bodyInterceptorV3 = bodyInterceptorV3;
    this.httpClient = httpClient;
    this.converterFactory = converterFactory;
    this.payer = payer;
  }

  public Completable createPaymentAuthorization(int paymentId) {
    return CreatePaymentAuthorizationRequest.of(paymentId,
        bodyInterceptorV3, httpClient, converterFactory).observe().flatMap(response -> {
      if (response != null && response.isOk()) {
        return Observable.just(null);
      }
      return Observable.<Void> error(
          new RepositoryIllegalArgumentException(V3.getErrorMessage(response)));
    }).toCompletable().andThen(syncAuthorization(paymentId));
  }

  public Completable saveAuthorization(Authorization authorization) {
    return Completable.fromAction(() -> authotizationAccessor.save(
        authorizationFactory.convertToDatabasePaymentAuthorization(authorization)));
  }

  public Observable<Authorization> getPaymentAuthorization(int paymentId) {
    return payer.getId()
        .flatMapObservable(payerId -> syncAuthorization(paymentId).andThen(
            authotizationAccessor.getPaymentAuthorization(payerId, paymentId)
                .map(paymentAuthorization -> authorizationFactory.convertToPaymentAuthorization(
                    paymentAuthorization))));
  }

  private Completable syncAuthorization(int paymentId) {
    return backgroundSync.syncAuthorization(paymentId);
  }
}
