package verification;

/**
 * Какой базовый набор правил использовать
 */
public enum Level {
    EASY,       // некоторые ошибки будут выдаваться с надписью WARNING!
    HARD,
    FATAL;       // любая ошибка считается за серьезную


    @Override
    public String toString() {
        switch (this){
            case EASY: return "[WARNING!]";
            case HARD: return "[EXCEPTION!] ";
            case FATAL: return "[FATAL]";
            default: throw new IllegalArgumentException();
        }
    }
}
