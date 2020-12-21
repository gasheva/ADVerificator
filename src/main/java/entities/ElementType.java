package entities;

public enum ElementType {
    STRANGE,
    FLOW,
    ACTIVITY,
    FORK,
    JOIN,
    DECISION,
    MERGE,
    INITIAL_NODE,
    FINAL_NODE,
    SWIMLANE;


    @Override
    public String toString() {
        switch (this){
            case DECISION: return "Условный переход";
            case ACTIVITY: return "Активность";
            case MERGE: return "Узел слияния";
            case JOIN: return "Синхронизатор";
            case FORK: return "Разветвитель";
            case FLOW: return "Переход";
            case FINAL_NODE: return "Конечное состояние";
            case INITIAL_NODE: return "Начальное состояние";
            case SWIMLANE: return "Дорожка участника";
            case STRANGE: return "";
            default: throw new IllegalArgumentException();
        }
    }
}
