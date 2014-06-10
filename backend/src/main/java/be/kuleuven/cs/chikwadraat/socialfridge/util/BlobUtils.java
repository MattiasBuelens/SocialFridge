package be.kuleuven.cs.chikwadraat.socialfridge.util;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.Collection;

/**
 * Created by Mattias on 10/06/2014.
 */
public class BlobUtils {

    private static final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    private BlobUtils() {
    }

    public static boolean isImage(BlobInfo blobInfo) {
        return blobInfo.getSize() > 0 && blobInfo.getContentType().startsWith("image/");
    }

    public static void deleteBlobKeys(Collection<BlobKey> blobKeys) {
        if (blobKeys != null && !blobKeys.isEmpty()) {
            blobstoreService.delete(blobKeys.toArray(new BlobKey[blobKeys.size()]));
        }
    }

    public static void deleteBlobInfos(Collection<BlobInfo> blobInfos) {
        if (blobInfos != null && !blobInfos.isEmpty()) {
            deleteBlobKeys(Collections2.transform(blobInfos, blobKeyFunction));
        }
    }

    private static final Function<BlobInfo, BlobKey> blobKeyFunction = new Function<BlobInfo, BlobKey>() {
        @Override
        public BlobKey apply(BlobInfo blobInfo) {
            return blobInfo.getBlobKey();
        }
    };

}
