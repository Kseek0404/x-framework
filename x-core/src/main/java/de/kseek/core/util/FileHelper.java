package de.kseek.core.util;

import lombok.extern.slf4j.Slf4j;

import java.beans.XMLEncoder;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class FileHelper {
    /**
     * 根据传入的文件名称读取文件,文件编码由系统自动判定
     *
     * @param fileName 文件名称
     * @return
     */
    public static String readFile(String fileName) {
        return readFile(fileName, null);
    }

    /**
     * 根据传入的文件名称读取文件
     *
     * @param fileName    文件名称
     * @param charsetName 编码，如果传入null，则由系统自动判定文件编码读取
     * @return
     */
    public static String readFile(String fileName, String charsetName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        if (charsetName == null) {
            return readFile(file, "UTF-8");
        } else {
            return readFile(file, charsetName);
        }
    }

    /**
     * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] read(String filename) {

        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new byte[0];
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 读取指定文件，返回文件内容
     *
     * @param file        需要被读取的文件
     * @param charsetName 读取文件采用的编码
     * @return
     */
    public static String readFile(File file, String charsetName) {
        FileInputStream fin = null;
        InputStreamReader inReader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            fin = new FileInputStream(file);
            inReader = new InputStreamReader(fin, charsetName);
            br = new BufferedReader(inReader);
            char[] charBuffer = new char[1024];
            int n = 0;
            while ((n = br.read(charBuffer)) != -1) {
                sb.append(charBuffer, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }


    public static String[] resolveFileContent(String fileContent,
                                              List<String[]> allValues) {
        String[] lineContent = fileContent.split("\r\n");
        if (lineContent.length <= 1) {
            return null;
        }
        String[] attributeNames = lineContent[1].split("\t", -1);
        if (attributeNames.length == 0) {
            return null;
        }
        for (int i = 2; i < lineContent.length; i++) {
            String[] attributeValues = lineContent[i].split("\t", -1);
            allValues.add(attributeValues);
        }
        return attributeNames;
    }

    /**
     * 将指定的的内容保存到文件中
     *
     * @param fileName 文件名（包括路径）
     * @param bytes    需要保存的内容
     * @param append   是否添加都文件后面
     * @throws Exception 抛出异常
     */
    public static void fileBytesWrite(String fileName, byte[] bytes,
                                      boolean append) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdir();
            }
            file.createNewFile();
        }
        fileBytesWrite(file, bytes, append);
    }

    /**
     * 保存指定的内容到文件中
     *
     * @param file   用于保存的文件
     * @param bytes  内容
     * @param append 是否添加到后面
     */
    public static void fileBytesWrite(File file, byte[] bytes, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(bytes);
            fos.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 保存指定的内容到文件中
     *
     * @param file    用于保存的文件
     * @param content 内容
     * @param append  是否添加到后面
     */
    public static void saveFile(File file, String content, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(content.getBytes("UTF-8"));
            fos.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 保存指定的内容到文件中
     *
     * @param fileName 用于保存的文件名称
     * @param content  内容
     * @param append   是否添加到后面
     */
    public static void saveFile(String fileName, String content,
                                boolean append) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.getParentFile()!=null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        saveFile(file, content, append);
    }

    public static void saveObjectToXml(Object obj, String fileName,
                                       boolean isAppend) {
        // 创建输出文件
        File fo = new File(fileName);
        // 文件不存在,就创建该文件
        if (!fo.exists()) {
            // 先创建文件的目录
            fo.getParentFile().mkdirs();
            try {
                fo.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        // 创建文件输出流
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fo, isAppend);
            // 创建XML文件对象输出类实例
            XMLEncoder encoder = new XMLEncoder(fos);
            // 对象序列化输出到XML文件
            encoder.writeObject(obj);
            encoder.flush();
            // 关闭序列化工具
            encoder.close();
            // 关闭输出流
            fos.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 解压到指定目录
     *
     * @param zipPath
     * @param descDir
     * @author isea533
     */
    public static void unZipFiles(String zipPath, String descDir)
            throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     *
     * @param zipFile
     * @param descDir
     * @author isea533
     */
    public static boolean unZipFiles(File zipFile, String descDir)
            throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);
        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
            // 判断路径是否存在,不存在则创建文件路径
            File file = new File(
                    outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }
            // 输出文件路径信息
            System.out.println(outPath);

            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解压完毕********************");
        return true;
    }

    public static void copy(String path, String copyPath) throws IOException {
        File filePath = new File(path);
        DataInputStream read;
        DataOutputStream write;
        if (filePath.isDirectory()) {
            File[] list = filePath.listFiles();
            for (int i = 0; i < list.length; i++) {
                String newPath = path + File.separator + list[i].getName();
                String newCopyPath = copyPath + File.separator
                        + list[i].getName();
                File file = new File(copyPath);
                file.mkdirs();
                copy(newPath, newCopyPath);
            }
        } else if (filePath.isFile()) {
            read = new DataInputStream(
                    new BufferedInputStream(Files.newInputStream(Paths.get(path))));
            write = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(Paths.get(copyPath))));
            byte[] buf = new byte[1024 * 512];
            int len;
            while ((len = read.read(buf)) > 0) {
                write.write(buf, 0, len);
            }
            read.close();
            write.close();
        } else {
            System.err.println("请输入正确的文件名或路径名");
        }
    }

    /**
     * 加载指定的属性文件，使用指定的编码
     *
     * @param fileName
     * @param charset
     * @return
     */
    public static Properties loadProperties(String fileName, String charset) {
        FileInputStream fin = null;
        InputStreamReader inReader = null;
        Properties properties = new Properties();
        try {
            fin = new FileInputStream(fileName);
            properties.load(fin);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return properties;
    }

}
