package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件操作工具类
 * <p>仅保留项目实际使用的文件系统读写、移动、压缩能力。
 * classpath 资源读取由调用方自行处理，不在本类职责内。
 */
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 目录不存在时创建目录（含父目录）
     * @param path 目录路径
     * @return 已存在返回 true；不存在则创建并返回创建结果
     */
    public static boolean createIfNoDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return file.mkdirs();
    }

    /**
     * 判断路径是否为已存在的目录
     * @param path 文件系统路径
     * @return 是目录返回 true，否则返回 false
     */
    public static boolean dirExist(String path) {
        return new File(path).isDirectory();
    }

    /**
     * 判断文件是否存在
     * @param path 文件系统路径
     * @return 存在返回 true，否则返回 false
     */
    public static boolean fileExist(String path) {
        return new File(path).exists();
    }

    /**
     * 写入字符串内容到文件（覆盖写，UTF-8）
     * @param path 文件路径
     * @param content 待写入的字符串内容
     * @throws IOException 写入时发生 IO 异常
     */
    public static void writeFile(String path, String content) throws IOException {
        // writeStringToFile 本身为覆盖写，无需先删除
        org.apache.commons.io.FileUtils.writeStringToFile(new File(path), content, StandardCharsets.UTF_8);
    }

    /**
     * 按行写入字符串列表到文件（覆盖写）
     * @param path 文件路径
     * @param content 待写入的字符串列表，每个元素为一行
     * @throws Exception 写入时发生异常
     */
    public static void writeFile(String path, List<String> content) throws Exception {
        // writeLines 本身为覆盖写，无需先删除
        org.apache.commons.io.FileUtils.writeLines(new File(path), content);
    }

    /**
     * 读取文件内容为字符串
     * <p>补充说明：读取失败时返回空字符串而非抛出异常
     * @param path 文件路径
     * @return 文件内容字符串，读取异常时返回空串
     */
    public static String readString(String path) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("读取文件失败，返回空串: {}", path, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 按行读取文件内容
     * <p>补充说明：文件存在时返回按行读取的列表；不存在时若 touch 为 true 则创建空文件；读取异常返回空列表
     * @param path 文件路径
     * @param touch 文件不存在时是否创建空文件
     * @return 文件每行内容组成的列表，异常或不存在时返回空列表
     */
    public static List<String> readLine(String path, boolean touch) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return org.apache.commons.io.FileUtils.readLines(file);
            }
            if (touch) {
                org.apache.commons.io.FileUtils.touch(file);
            }
        } catch (Exception e) {
            LOGGER.error("file readLine error, path:{}", path, e);
        }
        return Lists.newArrayList();
    }

    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param keepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean keepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            try (FileInputStream in = new FileInputStream(sourceFile)) {
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (keepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), keepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), keepDirStructure);
                    }
                }
            }
        }
    }

    /**
     * 压缩成ZIP 方法
     * @param srcDir 压缩文件夹路径
     * @param out    压缩文件输出流
     * @param keepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, OutputStream out, boolean keepDirStructure)
            throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), keepDirStructure);
            LOGGER.info("== 压缩完成，耗时：{} ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    LOGGER.error("== 关闭 ZipOutputStream 失败", e);
                }
            }
        }
    }
}
