package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /*

      List all instance variables of the Repository class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * The objects directory, where all versioned objects (commits, blobs, trees) are stored.
     */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    public static final File BLOB_DIR = join(OBJECTS_DIR, "blob");

    public static final File COMMIT_DIR = join(OBJECTS_DIR, "commit");
    /**
     * The HEAD file, a pointer to the current branch or commit.
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /**
     * The index file, used to stage changes before committing them to the repository.
     */
    public static final File STAGING_AREA = join(GITLET_DIR, "stagingArea");
    public static final File BRANCH = join(GITLET_DIR, "branch");

    public static void initDirFile() {
        // 创建必要的目录
        Repository.GITLET_DIR.mkdir();
        Repository.OBJECTS_DIR.mkdir();
        Repository.BLOB_DIR.mkdir();
        Repository.COMMIT_DIR.mkdir();

        // 创建必要的文件并处理异常
        try {
            Repository.HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create HEAD file", e);
        }

        try {
            Repository.STAGING_AREA.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Staging Area file", e);
        }

        try {
            Repository.BRANCH.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Branch file", e);
        }
    }
}
