package job.grabber;

import java.time.LocalDateTime;
import java.util.List;

public interface Parse {

    List<Post> list(String title, String link, String description, LocalDateTime createdDate);
}
