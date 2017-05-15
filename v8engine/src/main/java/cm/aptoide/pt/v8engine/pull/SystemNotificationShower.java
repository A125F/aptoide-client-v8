package cm.aptoide.pt.v8engine.pull;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import cm.aptoide.pt.imageloader.ImageLoader;
import cm.aptoide.pt.v8engine.R;
import com.bumptech.glide.request.target.NotificationTarget;
import rx.Completable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by trinkes on 09/05/2017.
 */

public class SystemNotificationShower {
  private static final String TAG = SystemNotificationShower.class.getSimpleName();
  private Context context;
  private NotificationManager notificationManager;

  public SystemNotificationShower(Context context, NotificationManager notificationManager) {
    this.context = context;
    this.notificationManager = notificationManager;
  }

  public Completable showNotification(AptoideNotification aptoideNotification, int notificationId) {
    return mapToAndroidNotification(aptoideNotification, notificationId).doOnSuccess(
        notification -> notificationManager.notify(notificationId, notification))
        .toCompletable();
  }

  private Single<Notification> mapToAndroidNotification(AptoideNotification aptoideNotification,
      int notificationId) {
    return getPressIntentAction(aptoideNotification.getUrlTrack(), aptoideNotification.getUrl(),
        aptoideNotification.getType(), context).flatMap(
        pressIntentAction -> buildNotification(context, aptoideNotification.getTitle(),
            aptoideNotification.getBody(), aptoideNotification.getImg(), pressIntentAction,
            notificationId, getOnDismissAction(notificationId), aptoideNotification.getAppName(),
            aptoideNotification.getGraphic()));
  }

  private Single<PendingIntent> getPressIntentAction(String trackUrl, String url,
      int notificationId, Context context) {
    return Single.fromCallable(() -> {
      Intent resultIntent = new Intent(context, PullingContentReceiver.class);
      resultIntent.setAction(PullingContentReceiver.NOTIFICATION_PRESSED_ACTION);

      if (!TextUtils.isEmpty(trackUrl)) {
        resultIntent.putExtra(PullingContentReceiver.PUSH_NOTIFICATION_TRACK_URL, trackUrl);
      }
      if (!TextUtils.isEmpty(url)) {
        resultIntent.putExtra(PullingContentReceiver.PUSH_NOTIFICATION_TARGET_URL, url);
      }

      return PendingIntent.getBroadcast(context, notificationId, resultIntent,
          PendingIntent.FLAG_UPDATE_CURRENT);
    })
        .subscribeOn(Schedulers.computation());
  }

  @NonNull private Single<android.app.Notification> buildNotification(Context context, String title,
      String body, String iconUrl, PendingIntent pressIntentAction, int notificationId,
      PendingIntent onDismissAction, String appName, String graphic) {
    return Single.fromCallable(() -> {
      android.app.Notification notification =
          new NotificationCompat.Builder(context).setContentIntent(pressIntentAction)
              .setOngoing(false)
              .setSmallIcon(R.drawable.ic_stat_aptoide_notification)
              .setLargeIcon(ImageLoader.with(context)
                  .loadBitmap(iconUrl))
              .setContentTitle(title)
              .setContentText(body)
              .setDeleteIntent(onDismissAction)
              .build();
      notification.flags =
          android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;
      return notification;
    })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .map(notification -> setExpandedView(context, title, body, notificationId, notification,
            appName, graphic));
  }

  private android.app.Notification setExpandedView(Context context, String title, String body,
      int notificationId, Notification notification, String appName, String graphic) {

    if (Build.VERSION.SDK_INT >= 16 && !TextUtils.isEmpty(graphic)) {
      RemoteViews expandedView =
          new RemoteViews(context.getPackageName(), R.layout.pushnotificationlayout);
      //in this case, large icon is loaded already, so instead of reloading it, we just reuse it
      expandedView.setImageViewBitmap(R.id.icon, notification.largeIcon);
      expandedView.setTextViewText(R.id.title, title);
      expandedView.setTextViewText(R.id.app_name, appName);
      expandedView.setTextViewText(R.id.description, body);
      notification.bigContentView = expandedView;

      NotificationTarget notificationTarget =
          new NotificationTarget(context, expandedView, R.id.push_notification_graphic,
              notification, notificationId);
      ImageLoader.with(context)
          .loadImageToNotification(notificationTarget, graphic);
    }
    return notification;
  }

  public PendingIntent getOnDismissAction(int notificationId) {
    Intent resultIntent = new Intent(context, PullingContentReceiver.class);
    resultIntent.setAction(PullingContentReceiver.PUSH_NOTIFICATION_DISMISSED);
    resultIntent.putExtra(PullingContentReceiver.PUSH_NOTIFICATION_NOTIFICATION_ID, notificationId);

    return PendingIntent.getBroadcast(context, notificationId, resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
