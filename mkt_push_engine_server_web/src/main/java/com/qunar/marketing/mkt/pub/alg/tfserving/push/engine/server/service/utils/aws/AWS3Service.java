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
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
     * AWS S3服务
     * <p>提供S3文件下载和解压功能，用于从OSS获取模型文件
     */
@Slf4j
@Service
public class AWS3Service {
    private static Logger LOGGER = LoggerFactory.getLogger(AWS3Service.class);
    private final Map<String, String> configMap;

    private  S3Client s3Client;
    private  String bucketName;

    /**
     * 构造函数
     * @param aWS3Config S3配置
     */
    @Autowired
    public AWS3Service(AWS3Config aWS3Config) {
        LOGGER.info("aws3ConfigMap: {}", aWS3Config.toString());
        this.configMap = aWS3Config.getConfigMap();
    }

    /**
     * 初始化S3客户端
     */
    @PostConstruct
    public void init() {
        String endPoint = configMap.get("endPoint");
        Region region = Region.AP_SOUTHEAST_4;
        bucketName = configMap.get("bucket");
        String accessKey = configMap.get("accessKey");
        String secretKey = configMap.get("secretKey");

        log.info("accessKey:{} secretKey:{}, endPoint: {}", accessKey, secretKey, endPoint);
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
            LOGGER.info("downloadFile s3Key:{}, localFilePath:{}", s3Key,localFilePath);
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
                try (OutputStream outputStream = new FileOutputStream(localFilePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = responseInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                LOGGER.info("文件下载成功: " + localFilePath);
                LOGGER.info("文件大小: " + Files.size(localPath) + " 字节");
            }

        } catch (S3Exception e) {
            LOGGER.error("S3错误: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("下载S3文件失败", e);
        } catch (IOException e) {
            LOGGER.error("IO错误: " + e.getMessage());
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

//    /**
//     * 解压ZIP文件到指定目录
//     */
//    public void unzipFile(String zipFilePath, String destDirectory) throws IOException {
//        File destDir = new File(destDirectory);
//        if (!destDir.exists()) {
//            destDir.mkdirs();
//        }
//
//        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
//            ZipEntry entry = zipIn.getNextEntry();
//
//            while (entry != null) {
//                String filePath = destDirectory + File.separator + entry.getName();
//                if (!entry.isDirectory()) {
//                    // 如果是文件，创建父目录并写入文件
//                    Path parentDir = Paths.get(filePath).getParent();
//                    if (parentDir != null) {
//                        Files.createDirectories(parentDir);
//                    }
//                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
//                        byte[] bytesIn = new byte[4096];
//                        int read;
//                        while ((read = zipIn.read(bytesIn)) != -1) {
//                            bos.write(bytesIn, 0, read);
//                        }
//                    }
//                } else {
//                    // 如果是目录，创建目录
//                    Files.createDirectories(Paths.get(filePath));
//                }
//                zipIn.closeEntry();
//                entry = zipIn.getNextEntry();
//            }
//            LOGGER.info("ZIP文件解压成功到: " + destDirectory);
//        }
//    }

    /**
     * 解压ZIP文件到指定目录
     * @param zipFilePath ZIP文件路径
     * @param destDirectory 目标目录
     * @throws IOException 解压失败
     */
    public void unzipFile(String zipFilePath, String destDirectory) throws IOException{
        SafeZipHandler.safeUnzip(new File(zipFilePath), destDirectory);
        LOGGER.info("ZIP文件解压成功: {} -> {}", zipFilePath, destDirectory);
    }



}
