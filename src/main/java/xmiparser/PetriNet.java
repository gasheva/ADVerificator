package xmiparser;

import Model.ADNodesList;
import entities.BaseNode;
import entities.DiagramElement;
import entities.ElementType;
import jdk.nashorn.internal.parser.Token;

import java.util.*;
import java.util.regex.Matcher;

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

    public void petriCheck(ADNodesList adList){
        Queue<StringBuilder> leaves = new LinkedList<>();       // необработанные маски
        Set<StringBuilder> masksInUsed = new HashSet<>(adList.getPetriElementsCount());   // использованные маски

        ADNodesList.ADNode current = null;
        // ищем начальное состояние
        ADNodesList.ADNode initNode = adList.findInitial();
        ADNodesList.ADNode finalNode = adList.findFinal();
        int indexOfFinalNode = ((DiagramElement)finalNode.getValue()).petriId;


        // создаем маску и добавляем ее в необработанные
        StringBuilder mask = createEmptyMask(adList.size());
        mask.setCharAt(((DiagramElement)initNode.getValue()).petriId, (char) TOKEN);
        System.out.println("Mask = "+mask);
        leaves.add(mask);

        boolean cont = true;
        boolean canReachFinal = false;

        List<StringBuilder> resultMasks = new LinkedList<>();

        // главный цикл прохода по всем элементам, участвующих в проверке
        while (cont){
            resultMasks.clear();
            StringBuilder originMask = leaves.poll();    // берем первый сверху элемент
            // маска, которую будем изменять по мере деактивации токенов
            StringBuilder maskCur = new StringBuilder(originMask);  // изначальна совпадает с оригинальной
            resultMasks.add(maskCur);   // добавляем в список результирующих масок исходную (изменится в процессе)
            masksInUsed.add(maskCur);   // добавляем в множество использованных

            int i=0;

            // проходим по всем элементам маски, находим активированные
            // и проверяем, можно ли активировать следующий элемент
            while (i < maskCur.length()) {
                if (maskCur.charAt(i) != TOKEN)
                    continue;   // интересуют только эл-ты, содержащие токены на данном шаге

                // нашли используемый элемент
                ADNodesList.ADNode curNode = adList.getNodeByPetriIndex(maskCur.charAt(i));

                // особо обрабатываем ситуации, когда элемент имеет несколько выходных переходов
                if(curNode.nextSize()>1) {
                    // если эл-т разветвитель, то последующее состояние единственно, однако надо установить несколько токенов за раз
                    if (curNode.getValue().getType() == ElementType.FORK) {
                        // удаляем из эл-та токен
                        maskCur.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                        // активируем все следующие элементы
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            // сразу после форка не может быть join'a, поэтому все эл-ты будут активированы
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;

                            // изменяем существующие результирующие маски
                            updateStepMasks(indexOfNewToken, ((DiagramElement) curNode.getValue()).petriId, resultMasks);
                        }
                    }
                    // если это условный оператор, то он порождает несколько возможных последующих состояний
                    else {
                        List<StringBuilder> tempMasks = new LinkedList<>();
                        // активируем следующие элементы по одному
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            // Join обрабатывается с помощью reverse проверки
                            if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
                                StringBuilder result = changeJoin(curNode.getNext(0), maskCur, resultMasks);
                                if (result!=new StringBuilder("-1"))
                                    maskCur = result;
                            } else {        // если это не join
                                // удаляем из маски шага
                                maskCur.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                                StringBuilder newMask = new StringBuilder(maskCur);
                                int indexOfNewToken = ((DiagramElement) curNode.getNext(0).getValue()).petriId;      // индекс активируемого эл-та
                                newMask.setCharAt(indexOfNewToken, NEW_TOKEN);

                                // в каждой существующей результирующей маске меняем токен и сохраняем в промежуточный список
                                for (StringBuilder resultMask : resultMasks) {
                                    StringBuilder temp = new StringBuilder(resultMask);
                                    temp.setCharAt(indexOfNewToken, NEW_TOKEN);     // добавляем новый токен
                                    temp.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);        // удаляем токен текущего эл-та
                                    tempMasks.add(temp);
                                }
                            }
                            // меняем результирующую маску
                            resultMasks = tempMasks;
                        }
                    }
                }
                else{       // если выходной переход один
                    // если это Join
                    if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
                        StringBuilder result = changeJoin(curNode.getNext(0), maskCur, resultMasks);
                        if (result!=new StringBuilder("-1"))
                            maskCur = result;
                    }
                    else {
                        maskCur.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                        int indexOfNewToken = ((DiagramElement) curNode.getNext(0).getValue()).petriId;
                        updateStepMasks(indexOfNewToken, ((DiagramElement) curNode.getValue()).petriId, resultMasks);
                    }
                }
                i++;
            }
            // в результирующих масках заменяем NEW_Token на TOKEN
            resultMasks.forEach(x->x = new StringBuilder(x.toString().replace(String.valueOf(NEW_TOKEN), String.valueOf(TOKEN))));

            // заканчиваем тогда, когда leaves пустой или невозможно передвинуть ни один токен
            // не был передвинут ни один токен, но не все листья просмотрены
            if(resultMasks.isEmpty() && leaves.isEmpty()) cont = false;
            if (resultMasks.isEmpty() && !leaves.isEmpty()){
                // Todo: не был передвинут ни один токен (сообщаем об этом, продолжаем обрабатывать другие листья)
                //throw new Exception("Возникла тупиковая ситуация");
            }
            
            // проверяем, что новой маски нет во множестве обработанных и добавляем в необработанные в таком случае
            for (StringBuilder resultMask : resultMasks) {
                if(!masksInUsed.contains(resultMask)){
                    // проверяем, не достигли ли конечной маркировки (существует путь позволяющей до нее добраться)
                    if(resultMask.charAt(indexOfFinalNode) == TOKEN) {
                        canReachFinal = true;
                        // TODO: проверка на колво токенов в маске (колво=1)
                    }
                    else leaves.add(resultMask);
                    masksInUsed.add(resultMask);    // добавляем полученную маску в множество обработанных
                }
            }
        }
        // проверяем, что конечное состояние было достигнуто
        if (!canReachFinal){
            // TODO
        }
    }

    //TODO: проверка, что на два токена (активируемый эл-т уже был активирован (1/2); тупиковые листья уже обрабатываются;
    // цветные токены (изменить форк и джоин)

    /**
     * Обновляет маски списка, удаляя токен и устанавливая новый
     * @param removedIndex
     * @param newIndex
     * @param stepMasks
     */
    private void updateStepMasks(int removedIndex, int newIndex, List<StringBuilder> stepMasks){
        stepMasks.forEach(x -> {
                    x.setCharAt(newIndex, NEW_TOKEN);
                    x.setCharAt(removedIndex, NO_TOKEN);
                }
        );
    }

    /**
     * Активирует Join, если это возможно, возвращая маску с удаленными\добавленным токенами,
     * изменяет результирующие маски
     * @param curNode активируемые Join
     * @param mask маска шага
     * @param resultMasks результирующие маски шага
     * @return новая маска шага или -1, если эл-т не активирован
     */
    private StringBuilder changeJoin(ADNodesList.ADNode curNode, StringBuilder mask, List<StringBuilder> resultMasks){
        StringBuilder maskCur = new StringBuilder(mask);
        if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
            StringBuilder newMask = activateJoin(curNode.getNext(0), maskCur);
            if (newMask == new StringBuilder("-1")) return new StringBuilder("-1");     // эл-т не удалось активировать

            // изменяем маски в соответствии с новой
            for (int i1 = 0; i1 < maskCur.length(); i1++) {
                int indexOfToken = i1;
                if (maskCur.charAt(i1) != newMask.charAt(i1)) {
                    // меняем результирующие маски
                    resultMasks.forEach(x -> x.setCharAt(indexOfToken, newMask.charAt(indexOfToken)));
                    // меняем маску текущего шага
                    if (newMask.charAt(i1) == NO_TOKEN)
                        maskCur.setCharAt(i1, NO_TOKEN);
                }

            }
        }
        return maskCur;
    }

    /**
     * Активирует элемент, если это возможно
     * из переданного массива индексов устанавливает индексы предшествующих элементов в -1, если эл-т был активирован
     * @param element
     * @param mask
     * @return измененную маску, в кот добавлен новый активный элемент или -1, если элемент не был активирован, или
     * -2, если было нарушено условие безопасности (два токена в одном эл-те)
     */
    private StringBuilder activateJoin(ADNodesList.ADNode element, StringBuilder mask){
        StringBuilder newMask = new StringBuilder(mask);
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
    }

}
