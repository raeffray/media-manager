package jp.mediahub.stream.container;

import java.io.OutputStream;
import jp.mediahub.messages.MediaChunk;

/**
 * An interface for handling media container operations such as opening streams for upload and download.
 * <p>
 * This interface defines two methods for opening streams, one for uploading {@link MediaChunk} and another one for
 * downloading {@link DownloadStream}.
 * <p>
 * {@link #openStreamForUpload(MediaChunk)} method allows to open a stream for write the MediaChunk to the container.
 * <p>
 * {@link #openStreamForDownload(String)} method allows to open a stream for reading the MediaChunk from the container.
 *
 * @author Renato Raeffray
 *
 */
public interface MediaContainer {

  OutputStream openStreamForUpload(MediaChunk media);

  DownloadStream openStreamForDownload(String mediaName);

}
