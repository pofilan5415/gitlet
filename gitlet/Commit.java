package gitlet;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Represents a snapshot of the CWD.
 *  @author Enzo Filangeri
 */
public class Commit implements Serializable {

    /** Stores all commits. */
    static final File COMMITS = new File(".gitlet/COMMITS");

    /** Initializes a commit with TS, MESSAGE, STAGE, PARENT,
     * and ISMERGE, and calls saveCommit(). */
    public Commit(String ts, String message, Stage stage,
                  Commit[] parent, boolean isMerge) {
        if (message.equals("")) {
            throw new GitletException("Please enter a commit message.");
        }
        _timeStamp = ts;
        if (!message.equals("initial commit")) {
            boolean emptyStage = stage.getStage().size() == 0;
            boolean emptyrmStage = stage.getRmStage().size() == 0;
            if (emptyrmStage && emptyStage) {
                throw new GitletException("No changes added to the commit.");
            }
            updateContents(parent[0], stage);
        } else {
            _contents = new HashMap<String, String>();
        }
        _isMerge = isMerge;
        _message = message;
        _parent = parent;
        saveCommit();
    }

    /** Updates contents of this according to PARENT and S. */
    private void updateContents(Commit parent, Stage s) {
        HashMap<String, String> stage = s.getStage();
        ArrayList<String> rmstage = s.getRmStage();
        if (parent._contents == null) {
            _contents = hashCopy(stage);
        } else {
            _contents = hashCopy(parent.getContents());
            for (Map.Entry<String, String> entry : stage.entrySet()) {
                File file = new File(entry.getKey());
                _contents.put(entry.getKey(), entry.getValue());
            }
        }
        for (String name : rmstage) {
            _contents.remove(name);
        }
    }

    /** Returns true if this has a parent. */
    public boolean hasParent() {
        return _parent != null;
    }

    /** Returns a copy of ORIGINAL. */
    static HashMap<String, String> hashCopy(HashMap<String, String> original) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : original.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /** Takes in a serialized file that has been staged and saves it to a
     * hashmap and serializes that hashmap to be stored in a file called
     * COMMITS. Also moves the branch head to this commit. */
    @SuppressWarnings("unchecked")
    private void saveCommit() {

        HashMap<String, Commit> unloadedSet = new HashMap<>();
        if (COMMITS.length() > 0) {
            HashMap<String, Branch> branchMap = Branch.getBranches();
            Branch currBranch = Branch.getActiveBranch();
            currBranch.movePointer(this);
            unloadedSet = (HashMap<String, Commit>)
                    Utils.readObject(COMMITS, HashMap.class);
            unloadedSet.put(Utils.sha1(Utils.serialize(this)), this);
            Utils.writeObject(COMMITS, unloadedSet);
        } else {
            new Branch(this, "master", true);
            unloadedSet.put(Utils.sha1(Utils.serialize(this)), this);
            Utils.writeObject(COMMITS, unloadedSet);
            _initial = this;
        }
    }

    /** Returns the current commit. */
    public static Commit getCurr() {
        return getCommits().get(Branch.getActiveBranch().id());
    }

    /** Returns the initial commit. */
    public static Commit getInitial() {
        return _initial;
    }

    /** Returns all commits. */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Commit> getCommits() {
        return (HashMap<String, Commit>)
                Utils.readObject(COMMITS, HashMap.class);
    }

    /** Returns the contents of this. */
    public HashMap<String, String> getContents() {
        return _contents;
    }

    /** Returns whether or not this is a merge. */
    public boolean isMerge() {
        return _isMerge;
    }

    /** Returns the timestamp. */
    public String getTime() {
        return _timeStamp;
    }

    /** Returns the commit. */
    public String getMessage() {
        return _message;
    }

    /** Returns the parent. */
    public Commit[] getParent() {
        if (_parent == null) {
            return new Commit[]{null, null};
        }
        return _parent;
    }

    /** Message. */
    private String _message;

    /** Contents. */
    private HashMap<String, String> _contents;

    /** Timestamp. */
    private String _timeStamp;

    /** Parent. */
    private Commit[] _parent;

    /** True if this is a merge. */
    private boolean _isMerge;

    /** Initial commit. */
    private static Commit _initial;
}
