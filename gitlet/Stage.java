package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Represents the state of a game of Lines of Action.
 *  @author Enzo Filangeri
 */
public class Stage implements Serializable {

    /** Stores the stage object. */
    static final File STAGE = new File(".gitlet/STAGE");

    /** Stage constructor. */
    public Stage() {
        _staged = new HashMap<String, String>();
        _rmstage = new ArrayList<String>();
        Utils.writeObject(STAGE, this);
    }

    /** Adds F to the staging area. */
    public void stageFile(File f) {
        _staged = Utils.readObject(STAGE, Stage.class).getStage();
        if (!f.exists()) {
            throw new GitletException("File does not exist");
        }

        Commit currCommit = Commit.getCurr();
        if (currCommit.getContents().size() > 0) {
            String currContents = currCommit.getContents().get(f.getName());
            String newContents = Main.sha(f);

            if (currContents != null && currContents.equals(newContents)) {
                if (_staged.containsKey(f.getName())) {
                    _staged.remove(f.getName());
                }
                Utils.writeObject(STAGE, this);
                return;
            }
        }
        _staged.put(f.getName(), Main.sha(f));
        Utils.writeObject(STAGE, this);

    }

    /** Stage the file with the name FILENAME for removal. */
    public void stageForRemoval(String fileName) {
        _rmstage = Utils.readObject(STAGE, Stage.class).getRmStage();
        _rmstage.add(fileName);
        Utils.writeObject(STAGE, this);
    }

    /** Returns the removal stage. */
    public ArrayList<String> getRmStage() {
        Stage unloaded = (Stage) Utils.readObject(STAGE, Stage.class);
        _rmstage = unloaded._rmstage;
        return _rmstage;
    }

    /** Returns the stage. */
    public HashMap<String, String> getStage() {
        Stage unloaded = (Stage)
                Utils.readObject(STAGE, Stage.class);
        _staged = unloaded._staged;
        return _staged;
    }

    /** Removes NAME from the stage. */
    public void remove(String name) {
        _staged.remove(name);
        Utils.writeObject(STAGE, this);
    }

    /** Clear the stage. */
    public void clear() {
        _staged = new HashMap<String, String>();
        _rmstage = new ArrayList<String>();
        Utils.writeObject(STAGE, this);
    }

    /** Staging area. */
    private HashMap<String, String> _staged;

    /** Removal staging area. */
    private ArrayList<String> _rmstage;
}
