package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Base {
    /**
     * Converts a file path string to a File object.
     *
     * @param path The path of the file.
     * @return The corresponding File object.
     */
    public static File stringToFile(String path) {
        return new File(path);
    }

    /**
     * Saves an object to a SHA-named file.
     *
     * @param sha The SHA identifier.
     * @param obj The object to save.
     * @param <T> The type of the object (must be Serializable).
     * @return True if the file was successfully saved, false if it already exists.
     * @throws RuntimeException If saving the file fails.
     */
    public static <T extends Serializable> boolean saveSHAFile(String sha, T obj) {
        File shaDir;
        if (obj instanceof Blob) {
            shaDir = Repository.BLOB_DIR;
        } else if (obj instanceof Commit) {
            shaDir = Repository.COMMIT_DIR;
        } else {
            throw new RuntimeException("It is not a SHA file.");
        }
        String preTwo = sha.substring(0, 2);
        File fileDir = Utils.join(shaDir, preTwo);
        File file = Utils.join(fileDir, sha);
        try {
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
                Utils.writeObject(file, obj);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save sha file: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves an object from a SHA-named file.
     *
     * @param sha  The SHA identifier.
     * @param type The class type of the object.
     * @param <T>  The type of the object (must be Serializable).
     * @return The deserialized object.
     * @throws RuntimeException If the file does not exist.
     */
    public static <T extends Serializable> T fromSHAFile(String sha, Class<T> type) {
        File shaDir;
        if (Blob.class.isAssignableFrom(type)) {
            shaDir = Repository.BLOB_DIR;
        } else if (Commit.class.isAssignableFrom(type)) {
            shaDir = Repository.COMMIT_DIR;
        } else {
            throw new RuntimeException("It is not a SHA file.");
        }
        String preTwo = sha.substring(0, 2);
        File fileDir = Utils.join(shaDir, preTwo);
        File file = Utils.join(fileDir, sha);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }
        return Utils.readObject(file, type);
    }

    public static String getFileSHA(File filePath) {
        if (filePath.canWrite()) {
            String content = Utils.readContentsAsString(filePath);
            return Utils.sha1(content + filePath.toString());
        } else {
            throw new RuntimeException("File cannot be read: " + filePath.getAbsolutePath());
        }
    }

    public static String getFileSHA(String filePath) {
        return getFileSHA(stringToFile(filePath));
    }

    public static String timeFormat(long time) {
        Date date = new Date(time);
        // 创建一个Calendar对象，设置时区
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-08:00"));
        calendar.setTime(date);

        // 使用Formatter格式化Date
        Formatter formatter = new Formatter(Locale.US);
        String formattedDate = formatter.format("Date: %ta %tb %td %tT %tY %tz",
                calendar, calendar, calendar,
                calendar, calendar, calendar).toString();
        formatter.close();
        return formattedDate;
    }

    public static void contentToFile(File file, String content) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Utils.writeContents(file, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write data to file: " + e.getMessage(), e);
        }
    }

    /**
     * Writes a string to a file, given the file path as a String.
     *
     * @param filePath The path to the file.
     * @param content  The content to write.
     */
    public static void contentToFile(String filePath, String content) {
        contentToFile(stringToFile(filePath), content);
    }
}
