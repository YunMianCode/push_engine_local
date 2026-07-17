package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SafeZipHandler {
    private static final Logger logger = LoggerFactory.getLogger(SafeZipHandler.class);
    private static final String INVALID_CHARS = "/\\:*?\"<>|";

    /**
     * 安全解压 ZIP 文件到目标目录
     * <p>补充说明：先校验 ZIP 文件有效性与目标目录，逐条目规范化名称并过滤危险路径字符；通过 resolve 后 normalize 再 startsWith 目标目录的方式防范 Zip Slip 路径遍历攻击；目录条目创建目录，文件条目确保父目录存在后用缓冲区写入
     * @param zipFile 待解压的 ZIP 文件
     * @param destDir 目标解压目录
     * @throws IOException 文件不存在、不可读、条目路径越界或解压失败时抛出
     */
    public static void safeUnzip(File zipFile, String destDir) throws IOException {
        // 验证ZIP文件是否存在且有效
        validateZipFile(zipFile);

        // 规范化目标目录路径并确保其存在
        Path destinationDir = Paths.get(destDir).normalize().toAbsolutePath();
        ensureDirectoryExists(destinationDir);

        logger.info("== 开始解压ZIP文件: {} 到目录: {}", zipFile.getAbsolutePath(), destinationDir);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    // 处理条目名称，过滤危险路径
                    String normalizedEntryName = normalizeEntryName(entry.getName());

                    // 验证条目名称是否合法
                    if (!isValidEntryName(normalizedEntryName) || !hasInvalidChars(normalizedEntryName)) {
                        logger.warn("== 跳过不合法的ZIP条目: {}", entry.getName());
                        continue;
                    }

                    // 安全拼接路径
                    Path entryPath = destinationDir.resolve(normalizedEntryName).normalize();

                    // 确保解压路径不会跳出目标目录
                    if (!entryPath.startsWith(destinationDir)) {
                        logger.error("== 检测到路径遍历攻击尝试，条目: {}", entry.getName());
                        throw new IOException("不安全的ZIP条目: " + entry.getName() + "，可能包含路径遍历字符");
                    }

                    // 处理目录条目
                    if (entry.isDirectory()) {
                        logger.debug("== 创建目录: {}", entryPath);
                        Files.createDirectories(entryPath);
                    }
                    // 处理文件条目
                    else {
                        // 确保父目录存在
                        Files.createDirectories(entryPath.getParent());
                        // 写入文件内容
                        try (OutputStream os = Files.newOutputStream(entryPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                os.write(buffer, 0, len);
                            }
                        }
                        logger.debug("== 解压文件: {}", entryPath);
                    }
                } finally {
                    zis.closeEntry();
                }
            }
        }

        logger.info("== ZIP文件解压完成");
    }

    /**
     * 验证 ZIP 文件是否有效
     * <p>补充说明：检查文件存在、是否为普通文件、是否可读，任一条件不满足即抛出异常
     * @param zipFile 待校验的 ZIP 文件
     * @throws IOException 文件不存在、非文件或不可读时抛出 FileNotFoundException 或 IOException
     */
    private static void validateZipFile(File zipFile) throws IOException {
        if (!zipFile.exists()) {
            throw new FileNotFoundException("ZIP文件不存在: " + zipFile.getAbsolutePath());
        }
        if (!zipFile.isFile()) {
            throw new IOException("指定路径不是文件: " + zipFile.getAbsolutePath());
        }
        if (!zipFile.canRead()) {
            throw new IOException("没有权限读取ZIP文件: " + zipFile.getAbsolutePath());
        }
    }

    /**
     * 确保目录存在，如果不存在则创建
     * <p>补充说明：路径不存在时递归创建；若已存在但非目录则抛出异常
     * @param directory 目标目录路径
     * @throws IOException 路径存在但不是目录时抛出
     */
    private static void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            logger.info("== 创建目标目录: {}", directory);
        } else if (!Files.isDirectory(directory)) {
            throw new IOException("指定的目标路径不是目录: " + directory);
        }
    }


    /**
     * 规范化ZIP条目的名称
     * @param entryName 原始条目名称
     * @return 规范化后的条目名称
     */
    public static String normalizeEntryName(String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return "";
        }

        // 替换不同系统的路径分隔符为统一的 '/'
        String normalized = entryName.replace('\\', '/');

        // 过滤掉路径遍历字符序列
        // 循环替换直到没有../序列，防止嵌套的情况如....//
        while (normalized.contains("../")) {
            normalized = normalized.replace("../", "");
        }

        // 移除开头的斜杠，防止绝对路径
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    /**
     * 验证ZIP条目的名称是否合法
     * @param entryName 规范化后的条目名称
     * @return 如果合法返回true，否则返回false
     */
    public static boolean isValidEntryName(String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return false;
        }

        // 检查是否包含危险字符或路径
        if (entryName.contains("..") ||
                entryName.contains("/../") ||
                entryName.contains("\\..") ||
                entryName.contains("..\\") ||
                entryName.startsWith("/") ||
                entryName.startsWith("\\")) {
            return false;
        }

        // 检查是否包含空字符或其他非法字符
        for (char c : entryName.toCharArray()) {
            if (Character.isISOControl(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查文件名是否包含无效字符
     * <p>补充说明：校验是否含 INVALID_CHARS（/\:*?"<>|）及 ASCII 控制字符（0-31），用于解压前的安全过滤
     * @param filename 待校验的文件名
     * @return 含无效字符返回 true，否则返回 false；filename 为 null 时返回 true
     */
    public static boolean hasInvalidChars(String filename) {
        if (filename == null) {
            return true;
        }
        // 检查是否包含无效字符
        for (char c : filename.toCharArray()) {
            if (INVALID_CHARS.indexOf(c) != -1) {
                return true;
            }
            // 检查控制字符（ASCII 0-31）
            if (c < 32) {
                return true;
            }
        }
        return false;
    }

    /**
     * 规范化用户输入的文件路径
     * @param userInput 用户输入的路径
     * @param baseDir 基础目录，确保用户输入的路径不会超出此目录
     * @return 规范化后的安全路径
     * @throws IOException 如果路径不合法则抛出异常
     */
    public static Path normalizeUserInputPath(String userInput, String baseDir) throws IOException {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IOException("输入路径不能为空");
        }

        // 规范化基础目录
        Path basePath = Paths.get(baseDir).normalize().toAbsolutePath();

        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            throw new IOException("基础目录不存在或不是目录: " + basePath);
        }

        // 解析用户输入并规范化
        Path inputPath = Paths.get(userInput).normalize();

        // 拼接路径并再次规范化
        Path resolvedPath = basePath.resolve(inputPath).normalize();

        // 确保拼接后的路径在基础目录之内
        if (!resolvedPath.startsWith(basePath)) {
            throw new IOException("输入路径不合法，可能包含路径遍历字符: " + userInput);
        }

        return resolvedPath;
    }
}
