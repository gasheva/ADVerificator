package result;

import verification.Level;

public abstract class Mistake {
    protected String mistake;
    protected Level level;

    public Mistake(String mistake, Level level) {
        this.mistake = mistake;
        this.level = level;
    }

    public String getMistake() {
        return mistake;
    }

    public void setMistake(String mistake) {
        this.mistake = mistake;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
