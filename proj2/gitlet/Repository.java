package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.join;

// TODO: any imports you need here

/**
 * Represents a Gitlet repository.
 * <p>
 * This class manages the Gitlet repository's file structure and provides utility methods
 * for handling files, hashing, and saving or loading versioned objects.
 * </p>
 *
 * @author TODO
 */
public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * The logs directory, where commit history and references are stored.
     */
    public static final File LOGS_DIR = join(GITLET_DIR, "logs");
    /**
     * The refs directory, a subdirectory of logs, where branch references are stored.
     */
    public static final File REFS_DIR = join(LOGS_DIR, "refs");
    /**
     * The heads directory, a subdirectory of refs
     */
    public static final File HEADS_DIR = join(REFS_DIR, "HEADS");
    /**
     * The head log file, tracking the history of the HEAD reference.
     */
    public static final File HEAD_LOG = join(LOGS_DIR, "head.gitlet");
    public static final File COMMIT_LOG = join(LOGS_DIR, "commitLog");
    /**
     * The objects directory, where all versioned objects (commits, blobs, trees) are stored.
     */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /**
     * The HEAD file, a pointer to the current branch or commit.
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD.gitlet");
    /**
     * The index file, used to stage changes before committing them to the repository.
     */
    public static final File INDEX = join(GITLET_DIR, "index");
    public static final File IS_FILE_CHANGED = join(GITLET_DIR, "flag");

    /**
     * Converts a relative file path string to a File object by joining it with the current working directory.
     *
     * @param path The relative path as a string.
     * @return The resulting File object that represents the specified path relative to the current working directory.
     */
    public static File StringToFile(String path) {
        path = toRelativePath(path);
        return join(CWD, path);
    }

    /**
     * Joins a parent directory path with a directory or file name to form a complete path string.
     *
     * @param parent  The parent directory path as a string.
     * @param dirName The directory or file name to be joined with the parent path.
     * @return The resulting path as a string.
     */
    public static String joinFileString(String parent, String dirName) {
        return join(parent, dirName).toString();
    }

    /**
     * Converts an absolute path to a relative path based on the current working directory.
     *
     * @param absolutePath The absolute file path to convert.
     * @return The relative path as a string.
     */
    public static String toRelativePath(String absolutePath) {
        File absolute = new File(absolutePath);
        return CWD.toURI().relativize(absolute.toURI()).getPath();
    }


    /**
     * Generates the SHA-1 hash of a file's contents.
     *
     * @param filePath The file whose SHA-1 hash is to be computed.
     * @return The SHA-1 hash as a String.
     * @throws RuntimeException if the file is not writable or cannot be read.
     */
    public static String getFileSHA(File filePath) {
        if (filePath.canWrite()) {
            String content = Utils.readContentsAsString(filePath);
            return Utils.sha1(content);
        } else {
            throw new RuntimeException("File cannot be read: " + filePath.getAbsolutePath());
        }
    }

    /**
     * Generates the SHA-1 hash of a file's contents, given the file path as a String.
     *
     * @param filePath The path to the file whose SHA-1 hash is to be computed.
     * @return The SHA-1 hash as a String.
     */
    public static String getFileSHA(String filePath) {
        return getFileSHA(join(CWD, filePath));
    }

    /**
     * Saves a serializable object to disk using its SHA-1 hash as the filename.
     * The object is saved under a directory structure determined by the two ahead character
     * of the SHA-1 hash to avoid storing too many files in a single directory.
     *
     * @param <T> The type of the object, which must implement Serializable.
     * @param SHA The SHA-1 hash of the object, used as the filename.
     * @param obj The object to save.
     * @return true if the object was successfully saved.
     * @throws RuntimeException if an IOException occurs during file creation or writing.
     */
    public static <T extends Serializable> boolean saveSHAFile(String SHA, T obj) {
        String preTwo = SHA.substring(0, 2);
        File fileDir = Utils.join(Repository.OBJECTS_DIR, preTwo);
        File file = Utils.join(fileDir, SHA);
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
            throw new RuntimeException("Failed to save SHA file: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a serializable object from disk based on its SHA-1 hash.
     *
     * @param <T>  The type of the object to load.
     * @param SHA  The SHA-1 hash of the object.
     * @param type The class type of the object.
     * @return The loaded object.
     * @throws RuntimeException if an error occurs during loading.
     */
    public static <T extends Serializable> T fromSHAFile(String SHA, Class<T> type) {
        String preTwo = SHA.substring(0, 2);
        File FileDire = Utils.join(Repository.OBJECTS_DIR, preTwo);
        File file = Utils.join(FileDire, SHA);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }
        return Utils.readObject(file, type);
    }

    /**
     * Deletes a file based on its SHA-1 hash.
     *
     * @param SHA The SHA-1 hash of the file to delete.
     */
    public static void delSHAFile(String SHA) {
        String preTwo = SHA.substring(0, 2);
        File FileDire = Utils.join(Repository.OBJECTS_DIR, preTwo);
        File file = Utils.join(FileDire, SHA);
        Utils.restrictedDelete(file);
    }

    /**
     * Splits a file path into its constituent directories and file name.
     * The path is split based on the system's file separator ("/" or "\\").
     *
     * @param path The file path to split.
     * @return A list of strings representing the directories and file name.
     */
    public static List<String> splitPath(String path) {
        return Arrays.asList(path.split("[/\\\\]"));
    }

    /**
     * Formats a timestamp into a human-readable date string.
     *
     * @param time The timestamp in milliseconds since the Unix epoch.
     * @return The formatted date string.
     */
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

    /**
     * Recursively gets all files in a directory and its subdirectories.
     *
     * @param dir The directory to start searching from.
     * @return A set of paths to all files found in the directory and its subdirectories.
     */
    public static HashSet<String> getAllFilesInSubdirectories(String dir) {
        List<String> files = Utils.plainFilenamesIn(dir);
        HashSet<String> filesSet = new HashSet<>();
        if (files != null) {
            for (String s : files) {
                filesSet.add(toRelativePath(dir + System.getProperty("file.separator") + s));
            }
        }
        List<String> subDir = Utils.subdirectoriesIn(dir);
        for (String s : subDir) {
            if (s.equals(".gitlet")) {
                continue;
            }
            String path = dir +  System.getProperty("file.separator") + s;
            filesSet.addAll(getAllFilesInSubdirectories(path));
        }
        return filesSet;
    }

    /**
     * Writes a string to a file. If the file does not exist, it is created.
     *
     * @param file The file to write to.
     */
    public static void dataToFile(File file, String content) {
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
    public static void dataToFile(String filePath, String content) {
        dataToFile(StringToFile(filePath), content);
    }
}
