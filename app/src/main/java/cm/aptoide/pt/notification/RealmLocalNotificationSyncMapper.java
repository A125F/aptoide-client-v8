package cm.aptoide.pt.notification;

import cm.aptoide.pt.database.realm.RealmLocalNotificationSync;
import cm.aptoide.pt.notification.sync.LocalNotificationSync;

public class RealmLocalNotificationSyncMapper {

  public RealmLocalNotificationSyncMapper() {
  }

  public RealmLocalNotificationSync map(LocalNotificationSync sync) {
    return new RealmLocalNotificationSync(sync.getId(), sync.getTitle(), sync.getBody(),
        sync.getImage(), sync.getActionString(), sync.getNavigationUrl(), sync.getTrigger(),
        sync.getId(), sync.getType());
  }

  public LocalNotificationSync map(RealmLocalNotificationSync realmSync,
      NotificationProvider provider) {
    return new LocalNotificationSync(provider, false, false, 0, realmSync.getTrigger(),
        realmSync.getTitle(), realmSync.getBody(), realmSync.getImage(),
        realmSync.getActionStringRes(), realmSync.getNavigationUrl(), realmSync.getId(),
        realmSync.getType());
  }
}
