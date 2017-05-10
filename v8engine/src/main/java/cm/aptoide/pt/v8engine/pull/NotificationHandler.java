package cm.aptoide.pt.v8engine.pull;

import android.support.annotation.NonNull;
import cm.aptoide.pt.database.realm.AptoideNotification;
import cm.aptoide.pt.dataprovider.ws.notifications.GetPullNotificationsResponse;
import cm.aptoide.pt.dataprovider.ws.notifications.PullCampaignNotificationsRequest;
import cm.aptoide.pt.dataprovider.ws.notifications.PullSocialNotificationRequest;
import cm.aptoide.pt.v8engine.networking.IdsRepository;
import com.jakewharton.rxrelay.PublishRelay;
import java.util.LinkedList;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import rx.Observable;
import rx.Single;

/**
 * Created by trinkes on 09/05/2017.
 */

public class NotificationHandler implements NotificationNetworkService {
  private final PublishRelay<AptoideNotification> handler;
  private String applicationId;
  private OkHttpClient httpClient;
  private Converter.Factory converterFactory;
  private IdsRepository idsRepository;
  private String versionName;

  public NotificationHandler(String applicationId, OkHttpClient httpClient,
      Converter.Factory converterFactory, IdsRepository idsRepository, String versionName) {
    this.applicationId = applicationId;
    this.httpClient = httpClient;
    this.converterFactory = converterFactory;
    this.idsRepository = idsRepository;
    this.versionName = versionName;
    handler = PublishRelay.create();
  }

  @Override public Single<List<AptoideNotification>> getSocialNotifications() {
    return PullSocialNotificationRequest.of(idsRepository.getUniqueIdentifier(), versionName,
        applicationId, httpClient, converterFactory)
        .observe()
        .map(response -> convertSocialNotifications(response))
        .flatMap(notifications -> handle(notifications))
        .toSingle();
  }

  @Override public Single<List<AptoideNotification>> getCampaignNotifications() {
    return PullCampaignNotificationsRequest.of(idsRepository.getUniqueIdentifier(), versionName,
        applicationId, httpClient, converterFactory)
        .observe()
        .map(response -> convertCampaignNotifications(response))
        .first()
        .flatMap(notifications -> handle(notifications))
        .toSingle();
  }

  @NonNull private Observable<List<AptoideNotification>> handle(List<AptoideNotification> aptoideNotifications) {
    return Observable.from(aptoideNotifications)
        .doOnNext(notification -> handler.call(notification))
        .toList();
  }

  public Observable<AptoideNotification> getHandlerNotifications() {
    return handler;
  }

  private List<AptoideNotification> convertSocialNotifications(
      List<GetPullNotificationsResponse> response) {
    List<AptoideNotification> aptoideNotifications = new LinkedList<>();
    for (final GetPullNotificationsResponse notification : response) {
      aptoideNotifications.add(
          new AptoideNotification(notification.getBody(), notification.getImg(), notification.getTitle(),
              notification.getUrl(), notification.getType()));
    }
    return aptoideNotifications;
  }

  private List<AptoideNotification> convertCampaignNotifications(
      List<GetPullNotificationsResponse> response) {
    List<AptoideNotification> aptoideNotifications = new LinkedList<>();
    for (final GetPullNotificationsResponse notification : response) {
      aptoideNotifications.add(new AptoideNotification(notification.getAbTestingGroup(),
          notification.getBody(),
          notification.getCampaignId(), notification.getImg(), notification.getLang(),
          notification.getTitle(), notification.getUrl(), notification.getUrlTrack()));
    }
    return aptoideNotifications;
  }
}
