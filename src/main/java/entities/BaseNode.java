package entities;

/**
 * Элемент, являющийся родительским для других узлов AD
 */
public abstract class BaseNode {
    protected String id;
    protected ElementType type;
    public BaseNode(String id) {
        this.id = id;
    }

    //region Getter-Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }
    //endregion
}
