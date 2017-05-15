package cm.aptoide.pt.v8engine.pull;

public class NotificationIdsMapper {
  public NotificationIdsMapper() {
  }

  int getNotificationId(@AptoideNotification.NotificationType int notificationType)
      throws RuntimeException {
    switch (notificationType) {
      case AptoideNotification.CAMPAIGN:
        return 0;
      case AptoideNotification.COMMENT:
      case AptoideNotification.LIKE:
        return 1;
      case AptoideNotification.POPULAR:
        return 2;
      default:
        throw new RuntimeException("unknown notification type " + notificationType);
    }
  }

  @AptoideNotification.NotificationType Integer[] getNotificationType(int notificationId)
      throws RuntimeException {
    switch (notificationId) {
      case 0:
        return new Integer[] {
            AptoideNotification.CAMPAIGN
        };
      case 1:
        return new Integer[] {
            AptoideNotification.LIKE, AptoideNotification.COMMENT
        };
      case 2:
        return new Integer[] {
            AptoideNotification.POPULAR,
        };
      default:
        throw new RuntimeException("unknown notification notificationId " + notificationId);
    }
  }
}