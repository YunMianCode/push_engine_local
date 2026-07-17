package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.SafeZipHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AWS3Service {
    private static Logger LOGGER = LoggerFactory.getLogger(AWS3Service.class);
    private final Map<String, String> configMap;

    private  S3Client s3Client;
    private  String bucketName;

    /**
     * 构造 AWS3 服务，注入配置
     * <p>通过 Spring 自动注入获取 AWS3Config，并将其 configMap 保存到当前实例
     * @param aWS3Config AWS3 配置 Bean
     */
    @Autowired
    public AWS3Service(AWS3Config aWS3Config) {
        // 不打印含密钥的全量 config，避免 accessKey/secretKey 落盘
        LOGGER.info("== aws3 config loaded");
        this.configMap = aWS3Config.getConfigMap();
    }

    /**
     * 初始化 S3 客户端
     * <p>@PostConstruct 生命周期方法，在构造函数完成、configMap 就绪后触发；
     * 从 configMap 读取 endPoint、bucket、accessKey、secretKey，创建带静态凭证与
     * UrlConnectionHttpClient 的 S3Client 实例并保存，供后续下载使用
     */
    @PostConstruct
    public void init() {
        String endPoint = configMap.get("endPoint");
        Region region = Region.AP_SOUTHEAST_4;
        bucketName = configMap.get("bucket");
        String accessKey = configMap.get("accessKey");
        String secretKey = configMap.get("secretKey");

        log.info("== aws3 init: endPoint={} bucket={}", endPoint, bucketName);
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder().endpointOverride(URI.create(endPoint))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }

    /**
     * 从S3下载文件到本地
     * @param s3Key S3上的文件路径（键）
     * @param localFilePath 本地保存路径（包括文件名）
     * @throws IOException 处理过程中的IO异常
     */
    public void downloadFile(String s3Key, String localFilePath) throws IOException {
        try {
            LOGGER.info("== downloadFile: s3Key={} localFilePath={}", s3Key, localFilePath);
            // 创建获取对象的请求
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // 获取S3对象的输入流
            try (ResponseInputStream responseInputStream = s3Client.getObject(getObjectRequest)) {
                // 确保本地目录存在
                Path localPath = Paths.get(localFilePath);
                if (localPath.getParent() != null) {
                    Files.createDirectories(localPath.getParent());
                }

                // 将S3文件内容写入本地文件
                try (OutputStream outputStream = Files.newOutputStream(localPath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = responseInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                LOGGER.info("== download success: {} size={}bytes", localFilePath, Files.size(localPath));
            }
        } catch (S3Exception e) {
            LOGGER.error("== download S3 error: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("下载S3文件失败", e);
        } catch (IOException e) {
            LOGGER.error("== download IO error: {}", e.getMessage());
            throw new RuntimeException("保存文件到本地失败", e);
        }
    }

    /**
     * 关闭S3客户端
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    /**
     * 探测单个 OSS 对象的元信息（是否存在、大小、最后修改时间）
     * <p>补充说明：使用 HeadObject 接口，权限要求与 GetObject 同组，适合在仅有读对象权限、无 ListBucket 权限的场景下
     * 核验特定文件是否已上传；对象不存在时返回 exists=false 而非抛异常
     * @param s3Key 对象 key
     * @return 含 exists/size/lastModified 的元信息 Map；不存在时 exists=false
     */
    public Map<String, Object> headObject(String s3Key) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", s3Key);
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            HeadObjectResponse response = s3Client.headObject(headRequest);
            item.put("exists", true);
            item.put("size", response.contentLength() == null ? -1 : response.contentLength());
            item.put("lastModified", response.lastModified() == null ? null : response.lastModified().toString());
        } catch (NoSuchKeyException e) {
            item.put("exists", false);
        } catch (S3Exception e) {
            // HeadObject 对不存在的 key 多数实现返回 404 S3Exception 而非 NoSuchKeyException，按状态码区分
            int status = e.statusCode();
            if (status == 404) {
                item.put("exists", false);
            } else {
                String detail = e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage();
                LOGGER.error("== probe oss object failed: key={} status={} detail={}", s3Key, status, detail);
                item.put("exists", false);
                item.put("error", "status=" + status + ", detail=" + detail);
            }
        } catch (Exception e) {
            LOGGER.error("== probe oss object failed: key={} {}", s3Key, e.getMessage());
            item.put("exists", false);
            item.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return item;
    }

    /**
     * 解压 ZIP 文件到指定目录
     * <p>委托 SafeZipHandler.safeUnzip 执行安全解压（防 Zip Slip 等），完成后记录解压日志
     * @param zipFilePath ZIP 文件路径
     * @param destDirectory 目标解压目录
     * @throws IOException 解压过程中的 IO 异常
     */
    public void unzipFile(String zipFilePath, String destDirectory) throws IOException{
        SafeZipHandler.safeUnzip(new File(zipFilePath), destDirectory);
        LOGGER.info("== unzip success: {} -> {}", zipFilePath, destDirectory);
    }



}
