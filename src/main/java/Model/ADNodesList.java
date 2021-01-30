package Model;

import debugging.Debug;
import entities.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Adler32;

/**
 * Представляет собой коллекцию для хранения узлов AD
 */
public class ADNodesList {
    private List<ADNode> nodes;
    private int diagramElementId = 0;       // Петри ид, присваиваемый элементу

    public ADNodesList() {
        nodes = new LinkedList<>();
    }

    /**
     * Копировать объект (по факту просто копирует массив ссылок на объекты массива nodes)
     * @param old
     */
    public ADNodesList(ADNodesList old){
        //this.nodes = old.nodes.stream().map(x->new ADNode(x)).collect(Collectors.toList());
        this.nodes = old.nodes;
        this.diagramElementId = old.diagramElementId;
    }

    /**
     * Возвращает колво элементов, используемых для проверки сетью Петри
     * @return
     */
    public int getPetriElementsCount(){
        return diagramElementId;
    }

    /**
     * получить все активности из массива
     * @return
     */
    public ArrayList<ActivityNode> getAllActivities(){
        List<ActivityNode> activities = nodes.stream().filter(x->x.value.getType()==ElementType.ACTIVITY).collect(Collectors.toList()).stream().map(x->(ActivityNode)x.value).collect(Collectors.toList());
        return new ArrayList<>(activities);
    }

    /**
     * Найти начальное состояние
     * @return ссылка на узел начального состояние
     */
    public ADNode findInitial(){
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getValue().getType()==ElementType.INITIAL_NODE) {
                return nodes.get(i);
            }
        }

        try {
            throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Найти конеченое состояние
     * @return ссылка на узел конеченого состояния
     */
    public ADNode findFinal(){
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getValue().getType()==ElementType.FINAL_NODE) {
                return nodes.get(i);
            }
        }

        try {
            throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Установить связи между элементами ДА
     */
    public void connect(){
        for (ADNode node : nodes) {
            // связываем все элементы, кроме переходов
            if (node.getValue() instanceof DiagramElement){
                findNext((DiagramElement) node.getValue(), node);
            }
        }
    }


    /**
     * Найти элементы для связи
     * @param cur текущий элемент, кот надо связать
     * @param curNode
     */
    private void findNext(DiagramElement cur, ADNode curNode){
        // для всех выходный переходов находим таргеты и добавляем ссылки в текущий элемент на таргеты
        for (int i = 0; i < cur.outSize(); i++) {
            ControlFlow flow = (ControlFlow) this.get(cur.getOutId(i));
            ADNode target = this.getNode(flow.getTargets());
            curNode.next.add(target);       // прямая связь
            target.prev.add(curNode);        // обратная связь
        }
    }

    /**
     * Печать связей между элементами
     */
    public void print(){
        for (ADNode node : nodes) {
            if (node.getValue() instanceof DiagramElement) {
                Debug.print("Cur: "+node.getValue().getType()+" | ");
                for (int i = 0; i < node.next.size(); i++) {
                    Debug.print(node.getNext(i).getValue().getType() + " ");
                }
                Debug.print(" || ");
                for (int i = 0; i < node.prev.size(); i++) {
                    Debug.print(node.prev.get(i).getValue().getType() + " ");
                }
                Debug.println("");
            }
        }
    }

    //region Getter-Setter
    public void add(int index, BaseNode node){

    }
    public int size(){
        return nodes.size();
    }
    public void addLast(BaseNode node){
        if (node instanceof DiagramElement) {
            ((DiagramElement) node).petriId = diagramElementId;
            diagramElementId++;
        }
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

    public ADNode getNode(String id){
        Optional<ADNode> node = nodes.stream().filter(x->x.value.getId().equals(id)).findFirst();
        return node.orElse(null);
    }
    public ADNode getNode(int index){
        return nodes.get(index);
    }

    public ADNode getNodeByPetriIndex(int id){
        Optional<ADNode> node = nodes.stream().filter(x-> {if (x.getValue() instanceof DiagramElement) return ((DiagramElement)x.getValue()).petriId == id;
            return false;
        }).findFirst();
        return node.orElse(null);
    }
    //endregion


    public class ADNode{
        private BaseNode value;
        private List<ADNode> next = new LinkedList<>();
        private List<ADNode> prev = new LinkedList<>();


        public ADNode(BaseNode value) {
            this.value = value;
        }

        public ADNode (ADNode old) {
            this.value = old.value;
            this.next = old.next;
            this.prev = old.prev;
        }

        //region Getter-Setter
        public BaseNode getValue() {
             return value;
        }

        public void setValue(BaseNode value) {
            this.value = value;
        }

        public int prevSize(){return prev.size();}
        public int nextSize(){return next.size();}

        public ADNode getNext(int index){
            return next.get(index);
        }
        public ADNode getPrev(int index){return prev.get(index);}

        public List<String> getNextIds(){
            return next.stream().map(x-> x.getValue().getId()).collect(Collectors.toList());
        }
        public List<String> getPrevIds(){
            return prev.stream().map(x-> x.getValue().getId()).collect(Collectors.toList());
        }
        //endregion
    }
}
