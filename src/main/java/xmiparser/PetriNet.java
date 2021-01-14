package xmiparser;

import Model.ADNodesList;
import entities.DiagramElement;
import entities.ElementType;

import java.util.*;

public class PetriNet {
    public static final char NO_TOKEN = 0;
    public static final char TOKEN_WAS_USED = 1;
    public static final char TOKEN = 2;
    public static final char NEW_TOKEN = 3;

    private Map<Integer, Stack<Integer>> colors = new HashMap<>(); // для каждого элемента хранит стек цветов

    /**
     * Создает пустую маску
     * @param length необходимая длина
     * @return строка, заполненная нулями
     */
    private StringBuilder createEmptyMask(int length){
        StringBuilder mask = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            mask.append(NO_TOKEN);
        }
        return mask;
        // return String.format("%1$" + length + "s", "").replace(' ', '0');
    }

    public void petriCheck(ADNodesList adList) throws Exception {
        List<StringBuilder> leaves = new LinkedList<>();      // необработанные маски
        Set<Integer> masksInUsed = new HashSet<>(adList.getPetriElementsCount());   // использованные маски

        ADNodesList.ADNode current = null;
        // ищем начальное состояние
        ADNodesList.ADNode initNode = adList.findInitial();

        // создаем маску и добавляем ее в необработанные
        StringBuilder mask = createEmptyMask(adList.size());
        mask.setCharAt(((DiagramElement)initNode.getValue()).petriId, (char) TOKEN);
        leaves.add(mask);
        current = initNode;

        boolean cont = true;

        List<StringBuilder> resultMasks = new LinkedList<>();

        // главный цикл прохода по всем элементам, участвующих в проверке
        while (cont){
            resultMasks.clear();
            StringBuilder originMask = leaves.get(0);    // берем первый сверху элемент
            // маска, которую будем изменять по мере деактивации токенов
            StringBuilder maskCur = new StringBuilder(originMask);  // изначальна совпадает с оригинальной

            // проходим по всем элементам маски, находим активированные
            // и проверяем, можно ли активировать следующий элемент
            int i=0;

            resultMasks.add(maskCur);   // добавляем в список результирующих масок исходную (изменяем в последствии)
            // цикл по маске
            while (i < maskCur.length()) {
                if (maskCur.charAt(i) != TOKEN)
                    continue;   // индекс уже был использован на данном шаге или токена нет

                // нашли используемый элемент
                ADNodesList.ADNode curNode = adList.getNodeByPetriIndex(maskCur.charAt(i));

                // особо обрабатываем ситуации, когда элемент имеет несколько выходных переходов
                if(curNode.nextSize()>1) {
                    // если эл-т разветвитель, то последующее состояние единственно,
                    // однако надо установить несколько токенов за раз
                    if (curNode.getValue().getType() == ElementType.FORK) {
                        // пытаемся активировать следующие элементы
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            StringBuilder newMask = activate(curNode.getNext(j), maskCur);
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;
                        }
                    }
                    // если это условный оператор, то он порождает несколько возможных последующих состояний
                    else {
                        List<StringBuilder> tempMasks = new LinkedList<>();

                        // пытаемся активировать следующие элементы
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            StringBuilder newMask = activate(curNode.getNext(j), maskCur);
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;
                            // если эл-т был активирован, добавляем новые маски в промежуточный список
                            if (newMask.charAt(indexOfNewToken) == NEW_TOKEN) {
                                for (StringBuilder resultMask : resultMasks) {
                                    StringBuilder temp = new StringBuilder(resultMask);
                                    temp.setCharAt(indexOfNewToken, NEW_TOKEN);
                                    tempMasks.add(temp);
                                }
                                // подготавливаем результирующую маску для данного эл-та
                                resultMasks = tempMasks;
                                // удаляем из старой маски токены, которых нет в новой
                                maskCur = removeTokens(maskCur, newMask);
                            }
                        }
                    }
                }
                else{
                    StringBuilder newMask = activate(curNode.getNext(0), maskCur);
                    int indexOfNewToken = ((DiagramElement)curNode.getNext(0).getValue()).petriId;
                    // если эл-т был активирован, добавляем новые маски в промежуточный список
                    if(newMask.charAt(indexOfNewToken)==NEW_TOKEN){
                        resultMasks.forEach(x->x.setCharAt(indexOfNewToken, NEW_TOKEN));
                        // удаляем из старой маски токены, которых нет в новой
                        maskCur = removeTokens(maskCur, newMask);
                    }
                }
                i++;
            }

            // проверяем, что новой маски нет во множестве обработанных и добавляем в необработанные в таком случае

            // добавляем полученную маску в множество обработанных

            // заканчиваем тогда, когда активируем последний элемент или пока массив leaves не будет пустым
            if (current.getValue().getType() == ElementType.FINAL_NODE)
                cont = false;
        }
        // проверяем, что достигли конечной маркировки
        // проверяем, что в конечной маркировке остался только один нецветной токен
    }
    private StringBuilder removeTokens(StringBuilder oldMask, StringBuilder newMask){
        for (int i = 0; i < oldMask.length(); i++) {
            if(oldMask.charAt(i)!= newMask.charAt(i) && newMask.charAt(i)==NO_TOKEN)
                oldMask.setCharAt(i, NO_TOKEN);
        }
        return oldMask;
    }

    /**
     * Активирует элемент, если это возможно
     * из переданного массива индексов устанавливает индексы предшествующих элементов в -1, если эл-т был активирован
     * @param element
     * @param mask
     * @return измененную маску, в кот добавлен новый активный элемент или -1, если элемент не был активирован, или
     * -2, если было нарушено условие безопасности (два токена в одном эл-те)
     */
    public StringBuilder activate(ADNodesList.ADNode element, StringBuilder mask){
        StringBuilder newMask = new StringBuilder(mask);
        switch (element.getValue().getType()){
            case JOIN:
                // все предшествующие элементы должны содержать токен
                for (int i = 0; i < element.prevSize(); i++) {
                    int idPrev = ((DiagramElement)element.getPrev(i).getValue()).petriId;
                    if (mask.charAt(idPrev) == NO_TOKEN)
                        return new StringBuilder("-1");
                    newMask.setCharAt(idPrev, NO_TOKEN);    // удаляем токены из предшествующих
                }
                // если элемент активирован, устанавливаем в него токен
                newMask.setCharAt(((DiagramElement)element.getValue()).petriId, TOKEN);
                return newMask;
            // хотя бы один из предшествующих элементов должен содержать токен
            default:
                boolean foundPrevWithToken = false;
                for (int i = 0; i < element.prevSize(); i++) {
                    int idPrev = ((DiagramElement)element.getPrev(i).getValue()).petriId;
                    if (mask.charAt(idPrev) == TOKEN) {
                        // если токен уже был найден, то нарушено условие безопасности сети
                        if (foundPrevWithToken) return new StringBuilder("Exception");
                        foundPrevWithToken = true;
                        newMask.setCharAt(idPrev, NO_TOKEN);
                    }
                }
                newMask.setCharAt(((DiagramElement)element.getValue()).petriId, TOKEN);
        }
        return newMask;
    }

}
