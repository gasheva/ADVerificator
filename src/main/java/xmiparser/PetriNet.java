package xmiparser;

import Model.ADNodesList;
import entities.DiagramElement;
import entities.ElementType;

import java.util.*;

public class PetriNet {
    public static final int NO_TOKEN = -1;
    public static final int UNCOLORED_TOKEN = 0;
    private Position header = null;      // позиция, соответствующая начальному состоянию
    private Position tail = null;        // позиция, соответствующая конечному состоянию
    private Position current = null;

    /**
     * Маска в индексы
     * @return индексы используемых элементов
     */
    private List<Integer> maskToIndexes(int mask){
        List<Integer> indexes = new LinkedList<>();
        String binMask = Integer.toBinaryString(mask);
        for (int i=binMask.length()-1; i>=0; i--){
            if (binMask.charAt(i)=='1'){
                indexes.add(i);
            }
        }
        return indexes;
    }

    /**
     * Индексы в маску
     * @param indexes массив индексов используемы элементов
     * @return маску число
     */
    private int indexesToMask(List<Integer> indexes){
        int mask = 0;
        for (Integer index : indexes) {
            mask+=Math.pow(2, index);
        }
        return mask;
    }

    public void petriCheck(ADNodesList adList) throws Exception {
        List<List<Integer>> leaves = new LinkedList<>();      // необработанные маски
        Set<Integer> masksInUsed = new HashSet<>(adList.getPetriElementsCount());   // использованные маски

        ADNodesList.ADNode current = null;
        // ищем начальное состояние
        ADNodesList.ADNode initNode = adList.findInitial();
        // кладем токен в начальное состояние
//        ((DiagramElement)initNode.getValue()).token = UNCOLORED_TOKEN;

        // создаем маску и добавляем ее в необработанные
        leaves.add((int) Math.pow(2, ((DiagramElement)initNode.getValue()).petriId));
        current = initNode;

        boolean cont = true;

        // главный цикл прохода по всем элементам, участвующих в проверке
        while (cont){
            int maskInt = leaves.get(0);    // берем первый сверху элемент
            List<Integer> indexes = maskToIndexes(maskInt);     // получаем индексы используемых элементов
            List<Integer> newIndexes = new LinkedList();        // массив индексов новых используемых элементов

            // проходим по всем используемым элементам и проверяем можно ли активировать следующий элемент
            int j = 0;
            int i=0;
            while (i < indexes.size()) {
                if (indexes.get(i)==-1) continue;   // индекс уже был использован

                // нашли используемый элемент
                ADNodesList.ADNode curNode = adList.getNodeByPetriIndex(indexes.get(i));
                // проверяем можно ли активировать какой-нибудь следующий и получаем новую маску или -1 в случае невозможности
                curNode.getNext()

                if (((DiagramElement)adList.getNode(j).getValue()).petriId==indexes.get(i)){
                    ADNodesList.ADNode curNode = adList.getNode(j);
                    for (int k = 0; k < curNode.nextSize(); k++) {
                        // проверяем, что следующий элемент не имеет токена с помощью маски
                        if(indexes.get(((DiagramElement)curNode.getNext(i).getValue()).petriId)!=NO_TOKEN)
                        {
                            throw new Exception("Элемент после "+curNode.getValue().getType()+" уже имеет токен");
                        }
                        // проверяем активирован ли следующий элемент
                        if (activate(curNode.getNext(i), indexes)){
                            // если активирован, устанавливаем на него токен
                            
                        }
                    }

                    i++;
                }
                j++;
            }

            // скидываем токены с текущих элементов


            // заканчиваем тогда, когда активируем последний элемент или пока массив leaves не будет пустым
            if (current.getValue().getType() == ElementType.FINAL_NODE)
                cont = false;
        }
        // проверяем, что достигли конечной маркировки
        // проверяем, что в конечной маркировке остался только один нецветной токен
    }

    /**
     * Активирует элемент, если это возможно
     * из переданного массива индексов устанавливает индексы предшествующих элементов в -1, если эл-т был активирован
     * @param element
     * @param indexes
     * @return измененную маску, в кот добавлен новый активный элемент или -1, если элемент не был активирован, или
     * -2, если было нарушено условие безопасности (два токена в одном эл-те)
     */
    public int activate(ADNodesList.ADNode element, List<Integer> indexes){
        // синхронизатор обрабатывается отдельно
        if (element.getValue().getType() == ElementType.JOIN){
            // все предшествующие элементы должны содержать токен
            for (int i = 0; i < element.prevSize(); i++) {

                if (indexes.get(((DiagramElement)element.getPrev(i).getValue()).petriId) == NO_TOKEN)
                    return -1;
            }
            // если элемент активирован, устанавливаем в него токен и удаляем из предыдущих
            for (int i = 0; i < element.prevSize(); i++) {
                ((DiagramElement)element.getPrev(i).getValue()).token = NO_TOKEN;
                indexes.remove(((DiagramElement)element.getPrev(i).getValue()).petriId);    // удаляем из массива индексы элементов без токенов


            }
            return true;
        }
        else{
            // хотя бы один из предшествующих элементов должен содержать токен
            for (int i = 0; i < element.prevSize(); i++) {
                if (indexes.get(((DiagramElement)element.getPrev(i).getValue()).petriId) != NO_TOKEN)
                    return true;

            }
            return false;
        }

    }

    private void addInitial(){
        header = new Position();
        current = header;
    }
    private void addActive(){

    }
    private void addFinal(){

    }

    private void addPosition(){

    }
    private void addTransition(){}


    /**
     * Позиция в сети Петри
     */
    private class Position{
        private Transition next;
        private int token = NO_TOKEN;      // токены разных цветов

    }

    /**
     * Переход в сети Петри
     */
    private class Transition{
        private List<Position> next;
        private List<Position> prev;

        /**
         * Переход активирован, когда все входные позиции имеют по токену
         * @return активирован ли переход
         */
        private boolean isActive(){
//            for (int i = 0; i < prev.size(); i++) {
//                if (prev.get(i).token==NO_TOKEN) return false;
//            }
            return true;
        }

    }
}
