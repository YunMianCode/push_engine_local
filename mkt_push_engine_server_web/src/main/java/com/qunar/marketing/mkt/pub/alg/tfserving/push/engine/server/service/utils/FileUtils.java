package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils;

import com.google.common.collect.Lists;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelInfoSnapshot;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 * <p>提供文件读写、目录操作、压缩解压等工具方法
 */
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);


    /**
     * 获取文件读取器
     * @param path 文件路径
     * @return BufferedReader
     * @throws FileNotFoundException 文件不存在
     */
    public static BufferedReader getFileReader(String path) throws FileNotFoundException {
        InputStream stream = FileUtils.class.getClassLoader().getResourceAsStream(path);

        if (stream == null) {
            stream = new FileInputStream(path);
        }
        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * 判断文件是否存在
     * @param path 文件路径
     * @return true表示存在
     */
    public static boolean exists(String path) {
        URL url = FileUtils.class.getClassLoader().getResource(path);
        return url != null || new File(path).isFile();
    }

    /**
     * 读取文件内容为字符串列表（按行）
     * @param path 文件路径
     * @return 字符串列表
     * @throws IOException 读取失败
     */
    public static List<String> getFileListContent(String path) throws IOException {
        BufferedReader reader = getFileReader(path);
        try {
            List<String> list = new ArrayList<String>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            return list;
        } catch (Exception e) {
            throw e;
        } finally {
            reader.close();
        }
    }

    public static List<List<String>> getFileListListContent(String path) throws IOException {
        BufferedReader reader = getFileReader(path);
        try {
            List<List<String>> list = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] toks = line.split("\\t");
                list.add(Arrays.asList(toks));
            }
            return list;
        } catch (Exception e) {
            throw e;
        } finally {
            reader.close();
        }
    }

    public static String getFileContent(String path) throws IOException {
        BufferedReader reader = getFileReader(path);
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            reader.close();
        }
    }



    public static List<String> getFileNameList(String dir) throws IOException {
        File folder = new File(dir);
        if (folder.isDirectory()) {
            List<String> fileNames = new ArrayList<>();
            for (File file : folder.listFiles()) {
                fileNames.add(file.getName());
            }
            return fileNames;
        } else {
            return getResourceFileNameList(dir);
        }
    }

    public static List<String> getResourceFileNameList(String dir) throws IOException {
        return getFileListContent(dir);
    }

    //FeaturesManager解析modelInfo.list采用新方法，兼容模式
    public static List<String> getFeaturesFileNameList(String dir) throws IOException {
        List<String> modelLines = getFileNameList(dir);
        List<String> tmp = new ArrayList<>();
        for (String modelLine : modelLines) {
            ModelInfoSnapshot modelInfoSnapshot = JSON.parseObject(modelLine, ModelInfoSnapshot.class);
            tmp.add(modelInfoSnapshot.getModelName());
        }
        modelLines = tmp;
        return modelLines;
    }


    private static final int  BUFFER_SIZE = 2 * 1024;

    /**
     * 判断目录是否存在
     * @param path 目录路径
     * @return true表示存在
     */
    public static boolean dirExist(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    /**
     * 判断文件是否存在
     * @param path 文件路径
     * @return true表示存在
     */
    public static boolean fileExist(String path) {
        File file = new File(path);
        return file.exists();
    }


    /**
     * 如果目录不存在则创建
     * @param path 目录路径
     * @return true表示创建成功或已存在
     */
    public static boolean createIfNoDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }

    public static void writeFile(String path, String content) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            //清除内容
            file.delete();
        }
        org.apache.commons.io.FileUtils.writeStringToFile(file, content);
    }

    public static void writeFile(String path, List<String> content) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            //清除内容
            file.delete();

        }
        org.apache.commons.io.FileUtils.writeLines(file, content);
    }

    public static String readString(String path) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(new File(path));
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
    }

    public static void moveFile(String origin, String dest) throws IOException {
        org.apache.commons.io.FileUtils.moveFile(new File(origin), new File(dest));
    }

    public static void moveFileToDir(String origin, String destDir) throws IOException {
        boolean ifNoDir = createIfNoDir(destDir);
        if (ifNoDir) {
            File destFile = new File(destDir + File.separator + origin + "." + System.currentTimeMillis());
            org.apache.commons.io.FileUtils.moveFile(new File(origin), destFile);
        }
    }

    public static List<String> readLine(String path, boolean touch) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return org.apache.commons.io.FileUtils.readLines(new File(path));
            }
            if (touch) {
                org.apache.commons.io.FileUtils.touch(file);
            }
        } catch (Exception e) {
            LOGGER.error("file readLine error, path:{}", path, e);
        }
        return Lists.newArrayList();
    }

//    public static void unzip(String destPath, String originPath) {
//        File srcFile = new File(originPath);
//        if (srcFile.exists()) {
//            Project prj = new Project();
//            Expand expand = new Expand();
//            expand.setProject(prj);
//            expand.setSrc(srcFile);
//            expand.setDest(new File(destPath));
//            expand.execute();
//        }
//    }

    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }

                }
            }
        }
    }

    /**
     * 压缩成ZIP 方法
     * @param srcDir 压缩文件夹路径
     * @param out    压缩文件输出流
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure)
            throws RuntimeException {

        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
