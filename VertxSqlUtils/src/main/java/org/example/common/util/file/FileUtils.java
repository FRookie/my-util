package org.example.common.util.file;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * TD 文件上传删除
 *
 * @date 2021/1/11
 * @since v1.0.0
 */
public class FileUtils {

    private static final String AWS_ACCESS_KEY = "";
    private static final String AWS_SECRET_KEY = "";
    private static final String bucketName = "";
    private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    private static AmazonS3 s3;

    static {
        s3 = new AmazonS3Client(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        s3.setRegion(Region.getRegion(Regions.US_WEST_1));
    }

    /**
     * 上传文件到s3, 上传成功返回文件url
     */
    public static String uploadToS3(InputStream inputStream, String fileDir, FileUpload fileUpload) {
        String remoteFileName = createFilePath(fileUpload, fileDir);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(fileUpload.size());
        objectMetadata.setContentType(fileUpload.contentType());
        s3.putObject(new PutObjectRequest(bucketName, remoteFileName, inputStream, objectMetadata)
                         .withCannedAcl(CannedAccessControlList.PublicRead));
        return remoteFileName;
    }

    /**
     * 异步上传文件到s3
     */
    public static void uploadToS3Async(Vertx vertx, String fileName, InputStream inputStream,
                                       FileUpload fileUpload) {
        // 先返回URL
        vertx.executeBlocking(promise -> {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(fileUpload.size());
            objectMetadata.setContentType(fileUpload.contentType());
            s3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            promise.complete();
        }, res -> {
            if (res.succeeded()) {
                logger.info("upload file[{}] succeed", fileName);
            } else {
                logger.error("upload file[{}] failed", fileName);
            }

        });
    }

    /**
     * 删除
     *
     * @param path 文件路径 例如avatar/23123131.png
     */
    public static void deleteFile(String path) {
        s3.deleteObject(bucketName, path);
    }

    /**
     * 创建文件路径
     *
     * @param fileDir 文件保存的目录
     */
    public static String createFilePath(FileUpload fileUpload, String fileDir) {
        StringBuilder stringBuffer = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateStr = formatter.format(new Date());
        int num = (int) (Math.random() * (10000 - 1000) + 1000);
        String fileName = fileUpload.fileName();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return stringBuffer.append(fileDir)
            .append(dateStr)
            .append(num)
            .append(suffix)
            .toString();
    }

    /**
     * 获取指定路径下的所有文件，只返回指定目录下的文件， 不进行递归不返回目录
     */
    public static List<File> getFiles(String path) throws FileNotFoundException {
        File languageDirection = new File(path);
        if (!languageDirection.exists()) {
            logger.error("file's templates path is wrong,pleas check: {}", path);
            throw new FileNotFoundException(
                "file's templates path is wrong,pleas check: " + path);
        }
        File[] files = languageDirection.listFiles(File::isFile);
        return Arrays.asList(files != null ? files : new File[0]);
    }

}
