package jp.mediahub.configuration;


import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridFsConfiguration {

  @Value("${spring.data.mongodb.database}")
  private String databaseName;

  @Bean
  public GridFSBucket gridFSBucket(MongoClient mongoClient) {
    return GridFSBuckets.create(mongoClient.getDatabase(this.databaseName));
  }

}
