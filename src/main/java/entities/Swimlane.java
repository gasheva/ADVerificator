package entities;

public class Swimlane extends BaseNode{
    private String name;

    public Swimlane(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
