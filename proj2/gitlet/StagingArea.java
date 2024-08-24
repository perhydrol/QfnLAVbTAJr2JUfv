package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;


public class StagingArea implements Serializable {
    private static final File STAGING_AREA = Repository.STAGING_AREA;
    private HashMap<String, String> trackedFiles;
    private HashSet<String> removalFiles;
    private HashMap<String, String> changedFiles;

    public StagingArea() throws IOException {
        trackedFiles = new HashMap<>();
        removalFiles = new HashSet<>();
        changedFiles = new HashMap<>();
        saveStagingArea();
    }

    public HashSet<String> getRemovalFiles() {
        return new HashSet<>(removalFiles);
    }

    public HashMap<String, String> getTrackedFiles() {
        return new HashMap<>(trackedFiles);
    }

    public HashMap<String, String> getChangedFiles() {
        return new HashMap<>(changedFiles);
    }

    public void add(File filePath) throws IOException {
        String sha = Base.getFileSHA(filePath);
        Head head = Head.fromFile();
        Commit commit = head.getCommit();
        trackedFiles.put(filePath.toString(), sha);
        if (removalFiles.contains(filePath.toString()) && commit.fileExits(filePath.toString())) {
            if (!filePath.exists()) {
                Blob blob = Blob.fromFile(sha);
                blob.recovery();
            }
            removalFiles.remove(filePath.toString());
        }
        if (commit.fileExits(filePath.toString())) {
            if (commit.fileChanged(filePath)) {
                changedFiles.put(filePath.toString(), sha);
            } else {
                changedFiles.remove(filePath.toString());
            }
        }
        saveStagingArea();
    }

    public void add(String filePath) throws IOException {
        add(Base.stringToFile(filePath));
    }

    public void rm(File filePath) throws IOException {
        Head head = Head.fromFile();
        Commit commit = head.getCommit();
        boolean trackedFile = changedFiles.containsKey(filePath.toString())
                || trackedFiles.containsKey(filePath.toString())
                || commit.fileExits(filePath.toString());
        if (!trackedFile) {
            System.out.println("No reason to remove the file.");
            return;
        }
        changedFiles.remove(filePath.toString());
        trackedFiles.remove(filePath.toString());
        if (filePath.exists() && commit.fileExits(filePath.toString())) {
            Utils.restrictedDelete(filePath);
        }
        if (commit.fileExits(filePath.toString())) {
            removalFiles.add(filePath.toString());
        }
        saveStagingArea();
    }

    public void rm(String filePath) throws IOException {
        rm(Base.stringToFile(filePath));
    }

    public void cleanStagingArea() throws IOException {
        changedFiles.clear();
        removalFiles.clear();
        trackedFiles.clear();
        saveStagingArea();
    }

    public void restToCommit(Commit commit) throws IOException {
        // trackedFiles = commit.getTrackedFiles();
        cleanStagingArea();
    }

    private void saveStagingArea() throws IOException {
        if (!STAGING_AREA.exists()) {
            STAGING_AREA.createNewFile();
        }
        Utils.writeObject(STAGING_AREA, this);
    }

    public static StagingArea fromFile() {
        return Utils.readObject(STAGING_AREA, StagingArea.class);
    }
}
