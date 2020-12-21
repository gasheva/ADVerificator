package Model;

import entities.ActivityNode;
import entities.BaseNode;
import entities.ElementType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Представляет собой коллекцию для хранения узлов AD
 */
public class ADNodesList {
    private List<ADNode> nodes;

    public ADNodesList() {
        nodes = new LinkedList<>();
    }

    /**
     * получить все активности из массива
     * @return
     */
    public ArrayList<ActivityNode> getAllActivities(){
        List<ActivityNode> activities = nodes.stream().filter(x->x.value.getType()==ElementType.ACTIVITY).collect(Collectors.toList()).stream().map(x->(ActivityNode)x.value).collect(Collectors.toList());
        return new ArrayList<>(activities);
    }

    //region Getter-Setter
    public void add(int index, BaseNode node){

    }
    public int size(){
        return nodes.size();
    }
    public void addLast(BaseNode node){
        nodes.add(new ADNode(node));
    }
    public void add(String id, BaseNode node){

    }
    public BaseNode get(int index){
        return nodes.get(index).getValue();
    }

    /**
     * @return значение узла или null если такой не найден
     */
    public BaseNode get(String id){
        Optional<ADNode> node = nodes.stream().filter(x->x.value.getId().equals(id)).findFirst();
        return node.map(adNode -> (adNode).getValue()).orElse(null);
    }

    //endregion


    private class ADNode<T>{
        private BaseNode value;
        private List<ADNode> next;
        private List<ADNode> prev;


        public ADNode(BaseNode value) {
            this.value = value;
        }

        //region Getter-Setter
        public BaseNode getValue() {
            return value;
        }

        public void setValue(BaseNode value) {
            this.value = value;
        }
        //endregion
    }
}
