package entities;

import java.util.LinkedList;
import java.util.List;

public class DecisionNode extends DiagramElement {
    private String question;
    private List<String> alternatives = new LinkedList<>();     // хранит названия альтернатив

    public DecisionNode(String id, String inPartition, String question) {
        super(id, inPartition);
        this.question = question;
    }

    public List<String> findEqualAlternatives(){
        List<String> equals = new LinkedList<>();
        for (int i = 0; i < alternatives.size()-1; i++) {
            for (int j = i+1; j < alternatives.size(); j++) {
                if (alternatives.get(i).equals(alternatives.get(j)))
                    equals.add(alternatives.get(i));
            }
        }
        return equals;
    }

    public boolean findEmptyAlternative(){
        for (int i = 0; i < alternatives.size(); i++) {
            if (alternatives.get(i).equals("")) return true;
        }
        return false;
    }

    public void findSameTargets(){

    }

    //region Getter-Setter
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void addAlternative(String alternative){
        alternatives.add(alternative);
    }
    public String getAlternative(int index){
        return alternatives.get(index);
    }
    public int alternativeSize(){
        return alternatives.size();
    }

    //endregion
}
