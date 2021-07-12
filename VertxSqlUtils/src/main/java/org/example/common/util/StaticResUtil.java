package org.example.common.util;

import io.vertx.core.json.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

/**
 * 更新静态资源文件
 *
 * @date 2021/03/11 14:47:40
 */
public class StaticResUtil {
    private static final Logger logger = LoggerFactory.getLogger(StaticResUtil.class);

    private static String basePath = "/../%s/webroot/";
    private static String storePath = "/../common/config/%s/version.json";
    private static final HashSet<String> IGNORE_TYPE = new HashSet<>();
    private static final HashSet<String> IGNORE_PATH = new HashSet<>();
    private static final JsonObject VALUES = new JsonObject();

    static {
        IGNORE_PATH.add("js" + File.separator + "lib" + File.separator + "charting_library");
        IGNORE_TYPE.add("ico");
        VALUES.put("web", new JsonObject());
        VALUES.put("mobile", new JsonObject());
        VALUES.put("console", new JsonObject());
    }

    /**
     * 更新
     * @throws IOException ioexception
     */
    public static void update(String curPath,String env) throws IOException {
        String path = curPath + basePath;
        getFiles(path, "web");
        getFiles(path, "mobile");
        getFiles(path, "console");
        logger.debug(VALUES.encodePrettily());
        Files.write(Paths.get(curPath,String.format(storePath, env), ""), VALUES.encodePrettily().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void getFiles(String path, String type) throws IOException {
        String pathname = String.format(path, type);
        File file = new File(pathname);

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File value : files) {
                if (value.isDirectory()) {
                    if (!checkPath(value)) {
                        getFiles(value.getPath(), type);
                    }
                } else {
                    if (!checkType(value)) {
                        String replace;
                        if (isWindows()) {
                            replace = value.getCanonicalPath().substring(value.getCanonicalPath().indexOf("webroot")+ 8).replace("\\", "/");
                        } else {
                            replace = value.getCanonicalPath().substring(value.getCanonicalPath().indexOf("webroot")+ 8);
                        }
                        FileInputStream fileStream = new FileInputStream(value);
                        String md5Hex = DigestUtils.md5Hex(fileStream);
                        fileStream.close();
                        VALUES.getJsonObject(type).put(replace, md5Hex.substring(0, 8));
                    }
                }
            }
        }
    }
    public static void getFiles2(String path, String type) throws IOException {
        String pathname = String.format(path, type);
        final Path baseDir = Path.of(pathname);
        Files.walkFileTree(baseDir,new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return checkPath(dir.toFile())?FileVisitResult.SKIP_SUBTREE:FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(checkType(file.toFile())){
                    return FileVisitResult.CONTINUE;
                }
                final String relativePath = baseDir.relativize(file).toString().replace("\\", "/");
                String md5Hex = DigestUtils.md5Hex(Files.readAllBytes(file));
                VALUES.getJsonObject(type).put(relativePath, md5Hex.substring(0, 8));
                return super.visitFile(file, attrs);
            }
        });
    }
    private static boolean checkPath(File value) {
        for (String s : IGNORE_PATH) {
            if (value.getPath().contains(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkType(File value) {
        for (String s : IGNORE_TYPE) {
            if (value.getPath().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

}
