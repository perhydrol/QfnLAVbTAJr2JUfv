package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Gitlet commit object.
 * <p>
 * A Commit object records the state of a repository at a given time.
 * Each commit has a message describing the changes made, a reference to a tree object that
 * represents the file system snapshot, a reference to the parent commit (if any),
 * and a timestamp. The Commit object is also identified by a unique sha-1 hash.
 * <p>
 * Commits are stored in a version control system to track the history of a project.
 * This class provides methods for creating a commit, saving it to persistent storage,
 * and retrieving it from storage.
 *
 * @author TODO
 */
public class Commit implements Serializable {

    /**
     * The message of this Commit.
     */
    private final String message;
    /**
     * The sha-1 hash of the tree object associated with this commit,
     * which represents the file system state at the time of this commit.
     */
    private final String treeSHA;
    /**
     * The sha-1 hash of the parent commits. The list may contain one or more sha-1 hashes.
     * If this commit is the root commit, the list will be empty.
     */
    private final List<String> parentSHAs;
    /**
     * The timestamp of this commit in milliseconds since the Unix epoch.
     */
    private final long time;
    private final String sha;

    /**
     * Constructs a new Commit object with the specified message, tree sha, and parent sha.
     *
     * @param message   The commit message.
     * @param treeSHA   The sha-1 hash of the associated tree object.
     * @param parentSHA The sha-1 hash of the parent commit (can be null for root commits).
     */
    public Commit(String message, String treeSHA, String parentSHA) {
        if (parentSHA.isEmpty()) {
            this.time = 0; // Unix Epoch time
        } else {
            this.time = System.currentTimeMillis();
        }
        this.message = message;
        this.treeSHA = treeSHA;
        this.parentSHAs = new ArrayList<>();
        this.parentSHAs.add(parentSHA);
        this.sha = Utils.sha1(message, treeSHA, parentSHA);
    }

    /**
     * Constructs a new Commit object with the specified message, tree sha, and a list of parent SHAs.
     *
     * @param message    The commit message.
     * @param treeSHA    The sha-1 hash of the associated tree object.
     * @param parentSHAs The list of sha-1 hashes of parent commits.
     */
    public Commit(String message, String treeSHA, List<String> parentSHAs) {
        this.time = System.currentTimeMillis();
        this.message = message;
        this.treeSHA = treeSHA;
        this.parentSHAs = parentSHAs;
        this.sha = Utils.sha1(message, treeSHA, parentSHAs);
    }

    /**
     * Loads a Commit object from the filesystem based on its sha-1 hash.
     *
     * @param sha The sha-1 hash of the commit to load.
     * @return The Commit object corresponding to the given sha-1 hash.
     * @throws RuntimeException if the commit file does not exist or cannot be read.
     */
    public static Commit fromFile(String sha) {
        return Repository.fromSHAFile(sha, Commit.class);
    }

    /**
     * Loads a Commit object from the filesystem based on a short sha-1 hash.
     * This method searches the appropriate directory for the full sha-1 hash.
     *
     * @param sha The short sha-1 hash of the commit to load.
     * @return The Commit object corresponding to the given short sha-1 hash, or null if not found.
     */
    public static Commit fromFileShortSHA(String sha) {
        File dir = Utils.join(Repository.OBJECTS_DIR, sha.substring(0, 2));
        if (dir.exists()) {
            List<String> files = Utils.plainFilenamesIn(dir);
            if (files != null) {
                for (String i : files) {
                    if (sha.equals(i.substring(0, sha.length()))) {
                        return fromFile(i);
                    }
                }
            }
        }
        return null;
    }

    public static boolean isRoot(Commit a) {
        return a.message.equals("Init commit") &&
                (a.parentSHAs == null || a.parentSHAs.isEmpty() || a.parentSHAs.get(0) == null);
    }

    public static Commit getSplitPoint(Commit a, Commit b) {
        if (a.sha.equals(b.sha)) {
            return a;
        }
        for (String curA : a.parentSHAs) {
            for (String curB : b.parentSHAs) {
                Commit ret = getSplitPoint(curA, curB);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    public static Commit getSplitPoint(String a, String b) {
        Commit curACommit = fromFile(a);
        Commit curBCommit = fromFile(b);
        return getSplitPoint(curACommit, curBCommit);
    }

    public boolean isRoot() {
        return isRoot(this);
    }

    public String getMessage() {
        return message;
    }

    public String getTreeSHA() {
        return treeSHA;
    }

    public List<String> getParentSHAs() {
        return new ArrayList<>(parentSHAs);
    }

    public long getTime() {
        return time;
    }

    public String getSHA() {
        return sha;
    }

    /**
     * Saves the current Commit object by serializing it and storing it using its sha-1 hash as the filename.
     *
     * @throws RuntimeException if there is an issue saving the commit file.
     */
    public void saveCommit() {
        Repository.saveSHAFile(sha, this);
    }

    /**
     * Recovers a file from the commit's tree based on the given file path.
     *
     * @param filePath The relative path of the file to recover.
     * @return true if the file was successfully recovered, false otherwise.
     */
    public boolean recoveryFile(String filePath) {
        filePath = Repository.toRelativePath(filePath);
        try {
            Tree tree = Tree.fromFile(treeSHA);
            String blobSHA = tree.getFileSHA(filePath);
            if (blobSHA != null) {
                Blob blob = Blob.fromFile(blobSHA);
                blob.recovery();
                return true;
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } catch (Exception e) {
            // Handle exceptions related to file recovery.
            System.err.println("Error recovering file: " + e.getMessage());
        }
        return false;
    }
}
