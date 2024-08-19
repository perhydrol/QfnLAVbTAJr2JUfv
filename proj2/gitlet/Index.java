package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * The Index class represents a mapping of file paths to their corresponding SHA-1 hashes
 * in a version control system. It tracks the state of files and their modifications.
 * The Index is serialized and saved to disk for persistence.
 */
public class Index implements Serializable {
    private final static File INDEX_PATH = Repository.INDEX;
    // The timestamp of the last modification to the index.
    private long time;
    // A map of file paths to their corresponding SHA-1 hashes.
    private HashMap<String, String> entry;

    /**
     * Constructs a new Index object. If an existing index is found at the INDEX,
     * it is loaded and its contents are used. Otherwise, a new, empty index is created.
     */
    public Index() {
        this.time = System.currentTimeMillis();
        if (INDEX_PATH.exists()) {
            try {
                Index temp = Utils.readObject(INDEX_PATH, Index.class);
                this.entry = temp.entry;
            } catch (IllegalArgumentException e) {
                this.entry = new HashMap<>();
            }
        } else {
            this.entry = new HashMap<>();
        }
    }

    /**
     * Constructs a new Index object and adds the file specified by the file path to the index.
     *
     * @param filePath The path of the file to be added to the index.
     */
    public Index(String filePath) {
        this();
        this.entry.put(filePath, Repository.getFileSHA(filePath));
    }

    /**
     * Constructs a new Index object and adds the file specified by the File object to the index.
     *
     * @param filePath The File object representing the file to be added to the index.
     */
    public Index(File filePath) {
        this();
        this.entry.put(filePath.toString(), Repository.getFileSHA(filePath));
    }

    public void saveIndex() {
        if (!INDEX_PATH.exists()) {
            try {
                INDEX_PATH.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeObject(INDEX_PATH, this);
    }

    /**
     * Adds or updates the entry for the specified file path in the index.
     *
     * @param filePath The path of the file to be added or updated in the index.
     */
    public void put(String filePath) {
        time = System.currentTimeMillis();
        this.entry.put(filePath, Repository.getFileSHA(filePath));
    }

    /**
     * Adds or updates the entry for the specified File object in the index.
     *
     * @param filePath The File object representing the file to be added or updated in the index.
     */
    public void put(File filePath) {
        put(filePath.toString());
    }

    public String getFileSHA(String filePath) {
        return entry.get(filePath);
    }

    public String getFileSHA(File filePath) {
        return getFileSHA(filePath.toString());
    }

    public void remove(String filePath) {
        time = System.currentTimeMillis();
        this.entry.remove(filePath);
    }

    public void remove(File filePath) {
        remove(filePath.toString());
    }

    public long getTime() {
        return time;
    }

    public HashMap<String, String> getEntry() {
        return new HashMap<>(entry);
    }
}
