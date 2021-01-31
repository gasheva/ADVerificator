package result;

import verification.Level;

public abstract class Mistake {
    protected String mistake;
    protected Level level;
    protected int id;

    public Mistake(String mistake, Level level, int id) {
        this.mistake = mistake;
        this.level = level;
        this.id = id;
    }

    public String getMistake() {
        return mistake;
    }

    public Level getLevel() {
        return level;
    }

    public int getId() {
        return id;
    }

}
