package jp.mediahub.repository;

import java.util.List;
import java.util.Optional;
import jp.mediahub.messages.Media;

public interface MediaRepository {

  String CONTENT_HASH_KEY = "contentHash";
  String FILENAME_KEY = "filename";

  Optional<List<Media>> getAllMedias();

  Optional<Media> findMedia(String fileName);

  void deleteMedia(String fileName);
}
