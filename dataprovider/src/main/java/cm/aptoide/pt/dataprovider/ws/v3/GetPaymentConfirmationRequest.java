/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 17/10/2016.
 */

package cm.aptoide.pt.dataprovider.ws.v3;

import cm.aptoide.pt.dataprovider.NetworkOperatorManager;
import cm.aptoide.pt.dataprovider.ws.BodyInterceptor;
import cm.aptoide.pt.model.v3.PaymentConfirmationResponse;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Observable;

/**
 * Created by marcelobenites on 17/10/16.
 */

public class GetPaymentConfirmationRequest extends V3<PaymentConfirmationResponse> {

  public GetPaymentConfirmationRequest(BaseBody baseBody, BodyInterceptor<BaseBody> bodyInterceptor,
      OkHttpClient httpClient, Converter.Factory converterFactory) {
    super(baseBody, httpClient, converterFactory, bodyInterceptor);
  }

  public static GetPaymentConfirmationRequest of(int productId,
      NetworkOperatorManager operatorManager, int apiVersion, BodyInterceptor<BaseBody> bodyInterceptor, OkHttpClient httpClient,
      Converter.Factory converterFactory) {
    final BaseBody args = getBaseBody(productId, operatorManager);
    args.put("reqtype", "iabpurchasestatus");
    args.put("apiversion", String.valueOf(apiVersion));
    return new GetPaymentConfirmationRequest(args, bodyInterceptor, httpClient, converterFactory);
  }

  private static BaseBody getBaseBody(int productId, NetworkOperatorManager operatorManager) {
    final BaseBody args = new BaseBody();
    args.put("mode", "json");
    args.put("payreqtype", "rest");
    args.put("productid", String.valueOf(productId));

    addNetworkInformation(operatorManager, args);
    return args;
  }

  public static GetPaymentConfirmationRequest of(int productId,
      NetworkOperatorManager operatorManager, BodyInterceptor<BaseBody> bodyInterceptor,
      OkHttpClient httpClient, Converter.Factory converterFactory) {
    final BaseBody args = getBaseBody(productId, operatorManager);
    args.put("reqtype", "apkpurchasestatus");
    return new GetPaymentConfirmationRequest(args, bodyInterceptor, httpClient, converterFactory);
  }

  @Override
  protected Observable<PaymentConfirmationResponse> loadDataFromNetwork(Interfaces interfaces,
      boolean bypassCache) {
    return interfaces.getPaymentConfirmation(map);
  }
}
