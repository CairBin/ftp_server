package top.cairbin.ftp.server.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ListLine {
    public static String getListLines(Path path, boolean nameOnly) throws Exception {
        StringBuilder list = new StringBuilder();

        if (!Files.isDirectory(path)) {
            // 如果是文件，直接添加文件的信息
            list.append(pathToListItem(path, nameOnly));
        } else {
            // 如果是目录，列出目录下的所有文件
            Files.list(path).forEach(p -> {
                try {
                    list.append(pathToListItem(p, nameOnly));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return list.toString();
    }

    public static String pathToListItem(Path path, boolean nameOnly) throws Exception {
        String filename = path.getFileName().toString();
        if (filename == null || filename.isEmpty()) {
            throw new Exception("Invalid filename");
        }

        if (nameOnly) {
            return filename + "\r\n";
        }

        // 获取文件大小
        long fileSize = Files.size(path);

        // 获取文件类型，- 表示文件，d 表示目录
        String fileType = Files.isDirectory(path) ? "d" : "-";

        // 获取文件权限 (使用 Posix 文件权限)
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
        String permission = PosixFilePermissions.toString(permissions)
                .replace("r", "r").replace("w", "w").replace("x", "x").replace("-", "-");

        // 获取文件的基础属性（包括最后修改时间）
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        // 获取文件的最后修改时间，转换为 Instant
        Instant modifiedTime = attr.lastModifiedTime().toInstant();

        // 将时间转换为本地时区的 ZonedDateTime
        ZonedDateTime time = modifiedTime.atZone(ZoneId.systemDefault());

        // 格式化时间为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        String fileTime = time.format(formatter);

        // 返回格式化后的信息
        return String.format("%s%s 1 owner group %d %s %s\r\n",
                fileType, permission, fileSize, fileTime, filename);
    }
}
