package entities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ControlFlow extends BaseNode{
    private String src = "";
    private String targets = "";
    private String text;
    public ControlFlow(String id) {
        super(id);
    }

    public ControlFlow(String id, String text) {
        super(id);
        this.text = text;
    }

    //region Getter-Setter
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    //endregion


    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }
}
