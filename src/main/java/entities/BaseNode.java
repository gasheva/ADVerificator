package entities;

import java.util.Objects;

/**
 * Элемент, являющийся родительским для других узлов AD
 */
public abstract class BaseNode {
    protected String id;
    protected ElementType type;
    public BaseNode(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseNode baseNode = (BaseNode) o;
        return id.equals(baseNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
