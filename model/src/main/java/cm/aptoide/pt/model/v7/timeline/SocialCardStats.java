package cm.aptoide.pt.model.v7.timeline;

import java.util.List;
import lombok.Data;

/**
 * Created by jdandrade on 31/01/2017.
 */
@Data public class SocialCardStats {
  private long likes;
  private long comments;
  private List<UserTimeline> usersLikes;
}
