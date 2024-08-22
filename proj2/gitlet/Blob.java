package gitlet;

import java.io.File;
import java.io.Serializable;


/**
 * Represents a blob in the version control system.
 * A blob stores the content of a file, its sha-1 hash,
 * the time it was created, and its file path.
 */
public class Blob implements Serializable {
    /**
     * The file path from which this blob was created.
     */
    private final String filePath;
    /**
     * The timestamp when this blob was created, in milliseconds since epoch.
     */
    private final long time;
    /**
     * The sha-1 hash of the file content, uniquely identifying this blob.
     */
    private String sha;
    /**
     * The content of the file as a string.
     */
    private String code;

    /**
     * Constructs a Blob from a File object.
     *
     * @param codeFile The file from which the blob is created.
     */
    public Blob(File codeFile) {
        time = System.currentTimeMillis();
        filePath = Repository.toRelativePath(codeFile.toString());
        if (codeFile.canWrite()) {
            code = Utils.readContentsAsString(codeFile);
            this.sha = Utils.sha1(code+filePath);
        } else {

        }
    }

    /**
     * Constructs a Blob from a file path string.
     *
     * @param codeFile The path of the file.
     */
    public Blob(String codeFile) {
        this(Repository.StringToFile(codeFile));
    }

    /**
     * Loads a Blob object from the filesystem based on its sha-1 hash.
     *
     * @param sha The sha-1 hash of the blob to load.
     * @return The Blob object corresponding to the given sha-1 hash.
     * @throws RuntimeException if the blob file does not exist or cannot be read.
     */
    public static Blob fromFile(String sha) {
        return Repository.fromSHAFile(sha, Blob.class);
    }

    /**
     * Returns the file path from which this blob was created.
     *
     * @return The file path as a string.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the content of the file stored in this blob.
     *
     * @return The file content as a string.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the sha-1 hash of the file content.
     *
     * @return The sha-1 hash string.
     */
    public String getSHA() {
        return this.sha;
    }

    /**
     * Returns the timestamp when this blob was created.
     *
     * @return The timestamp in milliseconds since epoch.
     */
    public long getTime() {
        return time;
    }

    /**
     * Saves this blob to the filesystem. The blob is stored in a directory
     * named after the first character of its sha-1 hash, inside the objects' directory.
     *
     * @return true if the blob was successfully saved.
     * @throws RuntimeException if there is an issue creating the blob file.
     */
    public boolean saveBlob() {
        return Repository.saveSHAFile(sha, this);
    }

    /**
     * Recovers the file from this blob and writes its content to the filesystem.
     */
    public void recovery() {
        Repository.dataToFile(Repository.StringToFile(filePath), code);
    }
}
