package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a gitlet commit object.
 * does at a high level.
 */
public class Commit implements Serializable {
    /*

      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private final String message;
    private final HashMap<String, String> trackedFiles;
    private final long time;
    private final List<String> parentsSha;
    private final String sha;

    private void genBlob(HashMap<String, String> files) {
        for (String file : files.keySet()) {
            Blob blob = new Blob(file);
        }
    }

    // 主要构造函数
    public Commit(String m, List<String> perShaList, String perSha, HashMap<String, String> files, HashSet<String> remove) {
        message = m;
        parentsSha = perShaList != null ? perShaList : new ArrayList<>();
        if (perSha == null || perSha.isEmpty()) {
            time = 0;
            trackedFiles = files;
        } else {
            Commit perCommit = Commit.fromFile(perSha);
            if (perCommit.trackedFiles.equals(files)) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            time = System.currentTimeMillis();
            parentsSha.add(perSha);
            trackedFiles = new HashMap<>(perCommit.trackedFiles);
            if (remove != null) {
                for (String rm : remove) {
                    trackedFiles.remove(rm);
                }
            }
            trackedFiles.putAll(files);
        }
        sha = genSha();
        genBlob(files);
        saveCommit();
    }

    // 重载的构造函数1
    public Commit(String m, List<String> perShaList, HashMap<String, String> files, HashSet<String> remove) {
        this(m, perShaList, null, files, remove);
    }

    // 重载的构造函数2
    public Commit(String m, String perSha, HashMap<String, String> files, HashSet<String> remove) {
        this(m, null, perSha, files, remove);
    }

    public static Commit fromFile(String commitSha) {
        File dir = Utils.join(Repository.COMMIT_DIR, commitSha.substring(0, 2));
        if (dir.exists()) {
            List<String> files = Utils.plainFilenamesIn(dir);
            if (files != null) {
                for (String i : files) {
                    if (commitSha.equals(i.substring(0, commitSha.length()))) {
                        return Base.fromSHAFile(i, Commit.class);
                    }
                }
            }
        }
        return null;
    }

    public static Commit getSplitPoint(Commit current, Commit target) {
        // 初始化队列和访问集合
        Queue<String> queueA = new LinkedList<>(current.parentsSha);
        Queue<String> queueB = new LinkedList<>(target.parentsSha);
        Set<String> visitedA = new HashSet<>();
        Set<String> visitedB = new HashSet<>();
        visitedA.add(current.sha);
        visitedB.add(target.sha);
        // BFS 搜索公共祖先
        while (!queueA.isEmpty() || !queueB.isEmpty()) {
            if (!queueA.isEmpty()) {
                String shaA = queueA.poll();
                if (visitedB.contains(shaA)) {
                    return Commit.fromFile(shaA);
                }
                Commit commitA = Commit.fromFile(shaA);
                if (commitA != null) {
                    for (String parentSHA : commitA.parentsSha) {
                        if (visitedA.add(shaA)) {
                            queueA.add(parentSHA);
                        }
                    }
                }
            }
            if (!queueB.isEmpty()) {
                String shaB = queueB.poll();
                if (visitedA.contains(shaB)) {
                    return Commit.fromFile(shaB);
                }
                Commit commitB = Commit.fromFile(shaB);
                if (commitB != null) {
                    for (String parentSHA : commitB.parentsSha) {
                        if (visitedB.add(shaB)) {
                            queueB.add(parentSHA);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Commit getSplitPoint(String currentSha, String targetSha) {
        return getSplitPoint(Commit.fromFile(currentSha), Commit.fromFile(targetSha));
    }

    private void saveCommit() {
        Base.saveSHAFile(sha, this);
    }

    private String genSha() {
        String connect;
        if (trackedFiles.isEmpty()) {
            connect = "";
        } else {
            connect = String.join(";", trackedFiles.values());
        }
        return Utils.sha1(message, connect);
    }

    public String getSha() {
        return sha;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public List<String> getParentsSha() {
        return new ArrayList<>(parentsSha);
    }

    public boolean fileChanged(File file) {
        String curSha = Base.getFileSHA(file);
        return !trackedFiles.getOrDefault(file.toString(), "").equals(curSha);
    }

    public String getFileSha(File file) {
        return trackedFiles.getOrDefault(file.toString(), null);
    }

    public String getFileSha(String filePath) {
        return getFileSha(Base.stringToFile(filePath));
    }

    public boolean fileExits(String filePath) {
        return trackedFiles.containsKey(filePath);
    }

    public HashMap<String, String> getTrackedFiles() {
        return new HashMap<>(trackedFiles);
    }

    public static void rest(Commit current, Commit target) {
        if (current.sha.equals(target.sha)) {
            return;
        }
        HashMap<String, String> curFiles = current.getTrackedFiles();
        HashMap<String, String> targetFiles = target.getTrackedFiles();
        Set<String> delFiles = new HashSet<>(curFiles.keySet());
        delFiles.removeAll(targetFiles.keySet());
        //存在未跟踪且将被覆盖的文件
        boolean hasUntrackedFilesInWay = false;
        List<String> untrackedFiles = Utils.plainFilenamesIn(Repository.CWD);
        untrackedFiles.removeAll(curFiles.keySet());
        for (String item : targetFiles.keySet()) {
            if (untrackedFiles.contains(item)) {
                hasUntrackedFilesInWay = true;
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        //存在当前已跟踪且提交，但目标并未跟踪的文件
        for (String file : delFiles) {
            File path = Base.stringToFile(file);
            Utils.restrictedDelete(path);
        }
        //用目标覆盖其余文件
        for (String s : targetFiles.keySet()) {
            if (!targetFiles.get(s).equals(curFiles.get(s))) {
                Blob fileBlob = Blob.fromFile(targetFiles.get(s));
                fileBlob.recovery();
            }
        }
    }

    public void rest(Commit target) {
        rest(this, target);
    }

    public void printCommitLog() {
        System.out.println("===");
        List<String> parents = this.getParentsSha();
        System.out.println("commit " + this.getSha());
        if (parents.size() > 1) {
            System.out.println("Merge: " + parents.get(0).substring(0, 8) + " " + parents.get(1).substring(0, 8));
        }
        System.out.println(Base.timeFormat(this.getTime()));
        System.out.println(this.getMessage());
        System.out.println("");
    }
}
