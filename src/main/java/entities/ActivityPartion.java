package entities;

public class ActivityPartion extends BaseNode{
    private String role;
    public ActivityPartion(String id) {
        super(id);
    }

    public ActivityPartion(String id, String role) {
        super(id);
        this.role = role;
    }

    //region Getter-Setter
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    //endregion
}
