package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Branch implements Serializable {
    /**
     * The sha-1 hash of the initial commit for this branch.
     */
    private final String STAR_COMMIT_SHA;
    /**
     * The name of the branch.
     */
    private final String name;
    /**
     * The sha-1 hash of the most recent commit in this branch.
     */
    private String END_COMMIT_SHA;
    /**
     * The sha-1 hash of the current commit pointed to by this branch.
     */
    private String currentCommitSHA;


    /**
     * Constructs a Branch object with the given name and initial commit sha.
     *
     * @param name          The name of the branch.
     * @param starCommitSHA The sha-1 hash of the initial commit.
     */
    public Branch(String name, String starCommitSHA) {
        STAR_COMMIT_SHA = starCommitSHA;
        END_COMMIT_SHA = starCommitSHA;
        currentCommitSHA = starCommitSHA;
        this.name = name;
    }

    /**
     * Loads a Branch object from the filesystem based on its name.
     *
     * @param branchName The name of the branch.
     * @return The Branch object corresponding to the given name.
     * @throws RuntimeException if the branch file does not exist or cannot be read.
     */
    public static Branch fromFile(String branchName) {
        File path = Utils.join(Repository.HEADS_DIR, branchName);
        if (path.exists()) {
            return Utils.readObject(path, Branch.class);
        } else {
            throw new RuntimeException("Branch file does not exist: " + path.getAbsolutePath());
        }
    }

    public static void merge(Branch current, Branch target) {
        Commit splitPoint = Commit.getSplitPoint(current.currentCommitSHA, target.currentCommitSHA);
        Commit currentCommit = Commit.fromFile(current.END_COMMIT_SHA);
        Commit targetCommit = Commit.fromFile(target.END_COMMIT_SHA);
        HashMap<String, String> split = Tree.fromFile(splitPoint.getTreeSHA()).getFiles();
        HashMap<String, String> cur = Tree.fromFile(currentCommit.getTreeSHA()).getFiles();
        HashMap<String, String> targ = Tree.fromFile(targetCommit.getTreeSHA()).getFiles();
        StagingArea stagingArea = new StagingArea();
        if (splitPoint.getSHA().equals(targetCommit.getSHA())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPoint.getSHA().equals(currentCommit.getSHA())) {
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Set<String> items = new HashSet<>(cur.keySet());
        items.addAll(targ.keySet());
        for (String item : items) {
            String tarSHA = targ.getOrDefault(item, "");
            String curSHA = cur.getOrDefault(item, "");
            String splitSHA = split.getOrDefault(item, "");
            // 3. 文件在给定分支中自分裂点起修改，但在当前分支中未修改
            boolean isModifiedInGivenBranchOnly = !tarSHA.equals(splitSHA)
                    && curSHA.equals(splitSHA);

            // 4. 文件在当前分支中自分裂点起修改，但在给定分支中未修改
            boolean isModifiedInCurrentBranchOnly = !curSHA.equals(splitSHA)
                    && tarSHA.equals(splitSHA);

            // 5. 文件在当前分支和给定分支中以相同方式修改或删除
            boolean isModifiedInSameWayInBothBranches = curSHA.equals(tarSHA);

            // 6. 文件在分裂点不存在，仅在当前分支存在
            boolean isFileOnlyInCurrentBranch = splitSHA.isEmpty() && !curSHA.isEmpty() && tarSHA.isEmpty();

            // 7. 文件在分裂点不存在，仅在给定分支存在
            boolean isFileOnlyInGivenBranch = splitSHA.isEmpty() && curSHA.isEmpty() && !tarSHA.isEmpty();

            // 8. 文件在分裂点存在，在当前分支未修改，在给定分支缺失
            boolean isUnmodifiedInCurrentAndAbsentInGiven = !splitSHA.isEmpty()
                    && splitSHA.equals(curSHA)
                    && tarSHA.isEmpty();

            // 9. 文件在分裂点存在，在给定分支未修改，在当前分支缺失
            boolean isUnmodifiedInGivenAndAbsentInCurrent = !splitSHA.isEmpty()
                    && curSHA.isEmpty()
                    && tarSHA.equals(splitSHA);

            // 10. 文件在当前分支和给定分支中以不同方式修改（发生冲突）
            boolean isConflict = !(curSHA.isEmpty() && tarSHA.isEmpty()) && !curSHA.equals(tarSHA);

            boolean willChange = isModifiedInGivenBranchOnly || isFileOnlyInGivenBranch || isUnmodifiedInCurrentAndAbsentInGiven;
            // 11. 存在未跟踪文件且会被合并覆盖或删除
            boolean isUntrackedFileInTheWay = !stagingArea.isTracked(item) && willChange;

            if (isModifiedInCurrentBranchOnly || isModifiedInSameWayInBothBranches || isFileOnlyInCurrentBranch || isUnmodifiedInGivenAndAbsentInCurrent) {
                continue;
            } else if (isModifiedInGivenBranchOnly || isFileOnlyInGivenBranch) {
                Blob file = Blob.fromFile(tarSHA);
                file.recovery();
                stagingArea.add(file.getFilePath());
                stagingArea.saveChanged();
            } else if (isUnmodifiedInCurrentAndAbsentInGiven) {
                stagingArea.rm(item);
            } else if (isConflict) {
                System.out.println("Encountered a merge conflict.");
                handleMergeConflict(item, curSHA, tarSHA);
                return;
            } else if (isUntrackedFileInTheWay) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        stagingArea.genNewCommit("Merged " + target.getName() + " into " + current.getName() + ".");
        stagingArea.saveChanged();
    }

    private static void handleMergeConflict(String item, String curSHA, String tarSHA) {
        Blob curBlob = Blob.fromFile(curSHA);
        Blob tarBlob = Blob.fromFile(tarSHA);
        String contect = "<<<<<<< HEAD\n" + curBlob.getCode() + "=======\n" + tarBlob.getCode() + ">>>>>>>\n";
        File file = Repository.StringToFile(item);
        Utils.writeContents(file, contect);
    }

    public static void merge(String current, String target) {
        merge(Branch.fromFile(current), Branch.fromFile(target));
    }

    /**
     * Returns the sha-1 hash of the initial commit.
     *
     * @return The initial commit sha-1 hash.
     */
    public String getSTAR_COMMIT_SHA() {
        return STAR_COMMIT_SHA;
    }

    /**
     * Returns the sha-1 hash of the end commit.
     *
     * @return The end commit sha-1 hash.
     */
    public String getEND_COMMIT_SHA() {
        return END_COMMIT_SHA;
    }

    /**
     * Returns the sha-1 hash of the current commit.
     *
     * @return The current commit sha-1 hash.
     */
    public String getCurrentCommitSHA() {
        return currentCommitSHA;
    }

    public String getName() {
        return name;
    }

    /**
     * Saves this branch to the filesystem. The branch is stored in a file
     * named after the branch's name, inside the heads' directory.
     *
     * @throws RuntimeException if there is an issue creating or writing the branch file.
     */
    public void saveBranch() {
        File path = Utils.join(Repository.HEADS_DIR, this.name);
        try {
            if (!path.exists()) {
                path.createNewFile();
            }
            Utils.writeObject(path, this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save branch: " + path.getAbsolutePath(), e);
        }
    }

    /**
     * Updates the end commit sha and the current commit sha to the given commit sha.
     *
     * @param commitSHA The sha-1 hash of the new commit.
     */
    public void newCommit(String commitSHA) {
        END_COMMIT_SHA = commitSHA;
        currentCommitSHA = commitSHA;
    }

    /**
     * Moves the current commit pointer to the given commit sha.
     *
     * @param commitSHA The sha-1 hash of the new commit.
     */
    public void moveCurrentCommit(String commitSHA) {
        currentCommitSHA = commitSHA;
    }

    /**
     * Retrieves the tree object corresponding to the current commit.
     *
     * @return The Tree object associated with the current commit.
     */
    public Tree getTree() {
        Commit commit = Commit.fromFile(currentCommitSHA);
        String treeSHA = commit.getTreeSHA();
        return Tree.fromFile(treeSHA);
    }
}
