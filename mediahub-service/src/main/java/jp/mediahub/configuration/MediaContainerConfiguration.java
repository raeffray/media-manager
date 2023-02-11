package jp.mediahub.configuration;

import com.mongodb.client.gridfs.GridFSBucket;
import jp.mediahub.stream.container.MediaContainer;
import jp.mediahub.stream.container.MongoDbMediaContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaContainerConfiguration {

  @Bean
  public MediaContainer createMediaContainer(GridFSBucket gridFSBucket) {
    return new MongoDbMediaContainer(gridFSBucket);
  }

}
