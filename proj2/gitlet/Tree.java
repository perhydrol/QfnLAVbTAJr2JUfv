package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The Tree class represents a directory structure within a version control system.
 * Each Tree object can contain references to subdirectories (as other Tree objects)
 * and files (with their corresponding SHA-1 hashes). The class is serializable for
 * persistent storage.
 */
public class Tree implements Serializable {
    // The path of the current directory represented by this Tree object.
    private final String path;

    private String SHA;
    // Map of subdirectory names to their corresponding SHA-1 hashes.
    private HashMap<String, String> subDirs;
    // Map of file names to their blob corresponding SHA-1 hashes.
    private HashMap<String, String> files;

    /**
     * Constructs a Tree object representing a directory.
     * If the parent is null, this Tree represents the root directory.
     *
     * @param path The path of the directory.
     */
    public Tree(String path) {
        this.SHA = null;
        this.path = path;
        subDirs = new HashMap<>();
        files = new HashMap<>();
    }

    public Tree(String path, HashMap<String, String> entry) {
        this(path);
        BuildTree(entry);
    }

    /**
     * Recreates a Tree object from a file.
     *
     * @param treePath The file representing the serialized Tree object.
     * @return The deserialized Tree object.
     * @throws RuntimeException if the file does not exist.
     */
    public static Tree fromFile(File treePath) {
        if (treePath.exists()) {
            return Utils.readObject(treePath, Tree.class);
        } else {
            throw new RuntimeException();
        }
    }

    public static Tree fromFile(String SHA) {
        return Repository.fromSHAFile(SHA, Tree.class);
    }

    /**
     * Checks out files from the target tree to the current working directory.
     * Deletes files that are present in the current tree but not in the target tree.
     *
     * @param curTree    The current tree.
     * @param targetTree The target tree to check out.
     */
    public static void checkout(Tree curTree, Tree targetTree) {
        if (curTree.SHA.equals(targetTree.SHA)) {
            return;
        }
        HashMap<String, String> tarSubDirs = targetTree.getSubDirs();
        HashMap<String, String> curSubDirs = curTree.getSubDirs();
        for (String subDirPath : tarSubDirs.keySet()) {
            if (curSubDirs.get(subDirPath) != null) {
                checkout(curSubDirs.get(subDirPath), tarSubDirs.get(subDirPath));
            } else {
                fromFile(tarSubDirs.get(subDirPath)).recoveryTree();
            }
        }
        HashMap<String, String> curFiles = curTree.getFiles();
        HashMap<String, String> targetFiles = targetTree.getFiles();
        Set<String> delFiles = curFiles.keySet();
        delFiles.removeAll(targetFiles.keySet());
        for (String s : delFiles) {
            File path = Repository.StringToFile(s);
            Utils.restrictedDelete(path);
        }
        for (String s : targetFiles.keySet()) {
            if (!curFiles.get(s).equals(targetFiles.get(s))) {
                Blob fileBlob = Blob.fromFile(targetFiles.get(s));
                fileBlob.recovery();
            }
        }
    }

    public static void checkout(String curTreeSHA, String targetTreeSHA) {
        Tree cur = fromFile(curTreeSHA);
        Tree target = fromFile(targetTreeSHA);
        checkout(cur, target);
    }

    public static boolean ifReset(Tree curTree, Tree targetTree) {
        if (curTree.SHA.equals(targetTree.SHA)) {
            return true;
        }
        boolean ans = true;
        HashMap<String, String> tarSubDirs = targetTree.getSubDirs();
        HashMap<String, String> curSubDirs = curTree.getSubDirs();
        for (String subDirPath : tarSubDirs.keySet()) {
            if (curSubDirs.get(subDirPath) != null) {
                ans = ans && ifReset(curSubDirs.get(subDirPath), tarSubDirs.get(subDirPath));
            }
        }
        HashMap<String, String> curFiles = curTree.getFiles();
        HashMap<String, String> targetFiles = targetTree.getFiles();
        Set<String> untrackedFiles = targetFiles.keySet();
        untrackedFiles.removeAll(curFiles.keySet());
        if (!untrackedFiles.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return false;
        }
        return ans;
    }

    public static boolean ifReset(String curTreeSHA, String targetTreeSHA) {
        Tree cur = fromFile(curTreeSHA);
        Tree target = fromFile(targetTreeSHA);
        return ifReset(cur, target);
    }

    /**
     * Retrieves the SHA-1 hash of a file given its path.
     *
     * @param filePath The path of the file.
     * @return The SHA-1 hash of the file.
     */
    public String getFileSHA(String filePath) {
        filePath = Repository.toRelativePath(filePath);
        return getFileSHA(Repository.splitPath(filePath), filePath);
    }

    public String getFileSHA(File filePath) {
        return getFileSHA(filePath.toString());
    }

    /**
     * Recursively retrieves the SHA-1 hash of a file within subdirectories.
     *
     * @param filePathList The list of directory names leading to the file.
     * @param file         The name of the file.
     * @return The SHA-1 hash of the file.
     */
    public String getFileSHA(List<String> filePathList, String file) {
        if (filePathList.size() == 1) {
            return this.files.get(file);
        } else {
            Tree subDir = fromFile(subDirs.get(filePathList.get(0)));
            return subDir.getFileSHA(filePathList.subList(1, filePathList.size()), file);
        }
    }

    /**
     * Saves the current Tree object by serializing it and storing it using its SHA-1 hash as the filename.
     *
     * @return true if the Tree was successfully saved.
     */
    public boolean saveTree() {
        getSHA();
        return Repository.saveSHAFile(SHA, this);
    }

    /**
     * Builds the Tree object by iterating through the files and subdirectories of the directory it represents.
     * This method recursively builds and saves Tree objects for each subdirectory.
     *
     * @param entry A map from file names to their corresponding SHA-1 hashes.
     * @return The SHA-1 hash of the current Tree object after it has been built.
     */
    public String BuildTree(HashMap<String, String> entry) {
        List<String> fileList = Utils.plainFilenamesIn(Repository.StringToFile(path));
        List<String> subDirList = Utils.subdirectoriesIn(Repository.StringToFile(path));

        if (fileList != null) {
            for (String file : fileList) {
                String fileSHA = entry.get(file);
                if (fileSHA != null) {
                    Blob fileBlob = new Blob(file);
                    this.files.put(file, fileBlob.getSHA());
                    fileBlob.saveBlob();
                }
            }
        }

        if (subDirList != null) {
            for (String subDir : subDirList) {
                if (subDir.equals(".gitlet")) {
                    continue;
                }
                Tree subDirTree = new Tree(subDir, entry);
                subDirTree.saveTree();
                this.subDirs.put(subDir, subDirTree.SHA);
            }
        }
        return getSHA();
    }

    /**
     * Returns the SHA-1 hash of this Tree object. If it hasn't been generated yet, it will be created.
     *
     * @return The SHA-1 hash of this Tree object.
     */
    public String getSHA() {
        if (SHA == null) {
            if (files.values().isEmpty() && subDirs.values().isEmpty()) {
                SHA = Utils.sha1("There is no thing");
            } else if (files.values().isEmpty()) {
                SHA = Utils.sha1(subDirs.values());
            } else if (subDirs.values().isEmpty()) {
                SHA = Utils.sha1(files.values());
            } else {
                SHA = Utils.sha1(files.values(), subDirs.values());
            }
        }
        return SHA;
    }

    /**
     * Returns the path of the directory represented by this Tree object.
     *
     * @return The directory path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the map of subdirectory names to their corresponding SHA-1 hashes.
     *
     * @return The map of subdirectories.
     */
    public HashMap<String, String> getSubDirs() {
        return new HashMap<>(subDirs);
    }

    /**
     * Returns the map of file names to their corresponding SHA-1 hashes.
     *
     * @return The map of files.
     */
    public HashMap<String, String> getFiles() {
        return new HashMap<>(files);
    }

    public void recoveryTree() {
        File dirPath = Repository.StringToFile(path);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        if (subDirs != null) {
            for (String curTreeSHA : this.subDirs.values()) {
                fromFile(curTreeSHA).recoveryTree();
            }
        }
        for (String file : files.keySet()) {
            File filePath = Utils.join(path, file);
            Blob blob = Blob.fromFile(files.get(file));
            blob.recovery();
        }
    }
}
