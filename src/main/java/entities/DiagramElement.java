package entities;

import java.util.LinkedList;
import java.util.List;

public class DiagramElement extends BaseNode{
    protected String inPartition = "";
    protected List<String> idsOut = new LinkedList<>();       // массив ид входящих переходов
    protected List<String> idsIn = new LinkedList<>();        // массив ид выходящих переходов
    protected String description = "";

    public int petriId;


    public DiagramElement(String id, String inPartition, String description) {
        super(id);
        this.inPartition = inPartition;
        this.description = description;
    }


    //region Getter-Setter
    public String getInPartition() {
        return inPartition;
    }

    public void setInPartition(String inPartition) {
        this.inPartition = inPartition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addIn(String allId){
        String[] ids = allId.split(" ");
        for (String id : ids) {
            if (!id.equals(""))idsIn.add(id);
        }
    }
    public void addOut(String allId){
        String[] ids = allId.split(" ");
        for (String id : ids) {
            if (!id.equals(""))idsOut.add(id);
        }
    }

    public String getInId(int index){
        return idsIn.get(index);
    }

    public String getOutId(int index){
        return idsOut.get(index);
    }

    public int inSize(){
        return idsIn.size();
    }

    public int outSize(){
        return idsOut.size();
    }
    //endregion
}
