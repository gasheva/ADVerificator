package xmiparser;

import Model.ADNodesList;
import debugging.Debug;
import entities.ActivityNode;
import entities.DiagramElement;
import entities.ElementType;
import result.MistakeFactory;
import result.VerificationResult;
import verification.Level;
import verification.lexical.LexicalAnalizator;

import java.util.*;

public class PetriNet {
    public static final char NO_TOKEN = '0';
    public static final char TOKEN = '1';
    public static final char NEW_TOKEN = '2';
    public final  int NO_COLOR = 0;
    private final Random random = new Random();
    private ADNodesList list;

    private Map<Integer, Stack<Integer>> colors = new HashMap<>(); // для каждого элемента хранит стек цветов

    /**
     * Создает маску, в которой все эл-ты неактивны
     * @param length необходимая длина
     * @return строка, заполненная нулями
     */
    private StringBuilder createEmptyMask(int length){
        StringBuilder mask = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            mask.append(NO_TOKEN);
        }
        return mask;
    }

    /**
     * Генерация рандомного цвета для токена, проходящего через разветвитель
     * @return
     */
    private int generateRandomColor(){
        return random.nextInt(Integer.MAX_VALUE);
    }

    public void petriCheck(ADNodesList adList){
        list = adList;
        Queue<StringBuilder> leaves = new LinkedList<>();       // необработанные маски
        Set<String> masksInUsed = new HashSet<>(adList.getPetriElementsCount());   // использованные маски

        // для каждого эл-та создаем стек цветов
        for (int i = 0; i < adList.getPetriElementsCount(); i++) {
            Stack<Integer> temp = new Stack<>();
            colors.put(i, temp);
        }

        // ищем начальное состояние
        ADNodesList.ADNode initNode = adList.findInitial();
        ADNodesList.ADNode finalNode = adList.findFinal();
        int indexOfFinalNode = ((DiagramElement)finalNode.getValue()).petriId;


        // создаем маску и добавляем ее в необработанные
        StringBuilder mask = createEmptyMask(adList.getPetriElementsCount());
        mask.setCharAt(((DiagramElement)initNode.getValue()).petriId, TOKEN);
        leaves.add(mask);
        masksInUsed.add(mask.toString());   // добавляем в множество использованных
        colors.get(((DiagramElement)initNode.getValue()).petriId).push(NO_COLOR);   // добавляем цвет токена

        boolean cont = true;
        boolean canReachFinal = false;

        List<StringBuilder> stepResultMasks = new LinkedList<>();

        // главный цикл прохода по всем элементам, участвующих в проверке
        while (cont){
            StringBuilder originMask = leaves.poll();  // берем первый сверху элемент
            stepResultMasks.clear();
            StringBuilder stepMask = new StringBuilder(originMask); // маска, которую будем изменять по мере деактивации токенов
            stepResultMasks.add(new StringBuilder(stepMask));   // добавляем в список результирующих масок исходную (изменится в процессе)
            Debug.println(originMask);

            int i=0;

            // проходим по всем элементам маски, находим активированные
            // и проверяем, можно ли активировать следующий элемент
            while (i < stepMask.length()) {
                if (stepMask.charAt(i) != TOKEN) {
                    i++;
                    continue;   // интересуют только эл-ты, содержащие токены на данном шаге
                }
                // нашли активный элемент
                ADNodesList.ADNode curNode = adList.getNodeByPetriIndex(i);     //индекс в списке совпадает с петри ид эл-та
                int curNodeIndex = ((DiagramElement) curNode.getValue()).petriId;
                Stack<Integer>newColors = colors.get(curNodeIndex);     // получаем цвета токена текущего эл-та

                // особо обрабатываем ситуации, когда элемент имеет несколько выходных переходов
                if(curNode.nextSize()>1) {
                    // если эл-т разветвитель, то последующее состояние единственно, однако надо установить несколько токенов за раз
                    if (curNode.getValue().getType() == ElementType.FORK) {
                        stepMask.setCharAt(curNodeIndex, NO_TOKEN);     // удаляем из маски токен
                        newColors.push(generateRandomColor());          // добавляем новый цвет токена

                        // активируем все следующие элементы
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            // сразу после форка не может быть join'a, поэтому все эл-ты будут активированы
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;
                            // проверка, что эл-т не был раннее активирован
                            if(wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(j))) return;
                            // изменяем существующие результирующие маски
                            updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks);
                            moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                        }
                    }
                    // если это условный оператор, то он порождает несколько возможных последующих состояний
                    else {
                        List<StringBuilder> decisionMasks = new LinkedList<>();
                        // активируем следующие элементы по одному
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;      // индекс активируемого эл-та
                            // проверка, что эл-т не был раннее активирован
                            if(wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(j))) return;
                            // Join обрабатывается с помощью reverse проверки
                            if ((curNode.getNext(j).getValue()).getType() == ElementType.JOIN) {
                                StringBuilder result = activateJoin(curNode.getNext(j), stepMask, stepResultMasks, newColors);
                                if (!result.toString().equals("-1"))
                                    stepMask = result;
                            } else {        // если это не join
                                // удаляем из маски шага
                                stepMask.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);

                                // в каждой существующей результирующей маске меняем токен и сохраняем в промежуточный список
                                for (StringBuilder resultMask : stepResultMasks) {
                                    StringBuilder temp = new StringBuilder(resultMask);
                                    temp.setCharAt(indexOfNewToken, NEW_TOKEN);     // добавляем новый токен
                                    temp.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);        // удаляем токен текущего эл-та
                                    decisionMasks.add(temp);
                                }
                                moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                            }

                        }
                        // меняем результирующую маску
                        stepResultMasks = decisionMasks;
                    }
                }
                else{       // если выходной переход один
                    int indexOfNewToken = ((DiagramElement) curNode.getNext(0).getValue()).petriId;
                    // проверка, что эл-т не был раннее активирован
                    if(wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(0))) return;
                    // если следующий Join
                    if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
                        StringBuilder result = activateJoin(curNode.getNext(0), stepMask, stepResultMasks, newColors);
                        if (!result.toString().equals("-1"))
                            stepMask = result;
                    }
                    else {
                        stepMask.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                        updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks);
                        moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                    }
                }
                i++;
            }
            // в результирующих масках заменяем NEW_Token на TOKEN
            stepResultMasks.forEach(x->x.replace(0, x.length(), x.toString().replace(String.valueOf(NEW_TOKEN), String.valueOf(TOKEN))));

            // заканчиваем тогда, когда leaves пустой или невозможно передвинуть ни один токен
            // не был передвинут ни один токен, но не все листья просмотрены
            if (stepResultMasks.isEmpty() && !leaves.isEmpty()){
                StringBuilder activateIndexes = new StringBuilder();
                for (int j = 0; j < originMask.length(); j++) {
                    if(originMask.charAt(j)==TOKEN) activateIndexes.append(j).append(" ");
                }
//                writeMistake("При активации элементов " + activateIndexes+" возник" + MISTAKES.DEAD_ROAD);
                MistakeFactory.createMistake(Level.HARD, activateIndexes + " "+ MISTAKES.DEAD_ROAD.toString());
            }
            Debug.print("");
            // проверяем, что новой маски нет во множестве обработанных и добавляем в необработанные в таком случае
            for (StringBuilder resultMask : stepResultMasks) {
                if(!masksInUsed.contains(resultMask.toString())){
                    // проверяем, не достигли ли конечной маркировки (существует путь позволяющей до нее добраться)
                    if(resultMask.charAt(indexOfFinalNode) == TOKEN) {
                        canReachFinal = true;
                        if(colors.get(indexOfFinalNode).peek()!=NO_COLOR) {
//                            writeMistake(MISTAKES.FINAL_COLOR_TOKEN.toString());
                            MistakeFactory.createMistake(Level.HARD, MISTAKES.FINAL_COLOR_TOKEN.toString());
                            return;
                        }
                        //region Проверка, что не осталось токенов
                        int tokenCount = 0;
                        StringBuilder activateIndexes = new StringBuilder();
                        for (int j = 0; j < originMask.length(); j++) {
                            if(originMask.charAt(j)==TOKEN) {
                                tokenCount++;
                                activateIndexes.append(j).append(" ");
                            }
                        }
                        if (tokenCount>1) {
//                            writeMistake("Достижение конечного состояния с активированными эл-ми" +
//                                    activateIndexes +". " + MISTAKES.MANY_TOKENS_IN_END);
//                            MistakeFactory.createMistake(Level.HARD, MISTAKES.MANY_TOKENS_IN_END.toString());
                        }
                        //endregion
                    }
                    else leaves.add(new StringBuilder(resultMask));
                    masksInUsed.add(resultMask.toString());    // добавляем полученную маску в множество обработанных
                }
            }
            if(leaves.isEmpty()) cont = false;
        }

        // проверяем, что конечное состояние было достигнуто
        if (!canReachFinal){
//            writeMistake(MISTAKES.COULD_NOT_REACH_FINAL.toString());
            MistakeFactory.createMistake(Level.HARD, MISTAKES.COULD_NOT_REACH_FINAL.toString());

        }
        else{
            Debug.println("Достигли конечное состояние");
        }
    }

    //TODO: проверка, что на два токена (активируемый эл-т уже был активирован (1/2); тупиковые листья уже обрабатываются; +
    // цветные токены (изменить форк и джоин)  +
    // выводить таблицу ид (обычный и петри), тип эл-та, описание эл-та
    // при наведении на ошибку - возможные причины


    private void moveColors(int indexOfNewToken, int curNodeIndex, Stack<Integer> newColors){
        colors.put(indexOfNewToken, (Stack<Integer>)newColors.clone());
        colors.put(curNodeIndex, new Stack<>());
    }

    /**
     * Проверка был ли данный элемент раннее активирован. Если да, то проверка завершается
     * @param tokenIndex
     * @param stepResultMasks
     * @param curNode
     * @return
     */
    private boolean wasAlreadyActive(int tokenIndex, List<StringBuilder> stepResultMasks, ADNodesList.ADNode curNode){
        for (StringBuilder stepResultMask : stepResultMasks) {
            if(stepResultMask.charAt(tokenIndex)!=NO_TOKEN) {
                //writeMistake("", type.toString(), type==ElementType.ACTIVITY? ((ActivityNode) curNode.getValue()).getName():"", MISTAKES.TWO_TOKENS.toString());
                if(curNode.getValue().getType()!=ElementType.FINAL_NODE)
                    MistakeFactory.createMistake(Level.HARD, MISTAKES.TWO_TOKENS.toString(), curNode);
                else
                    MistakeFactory.createMistake(Level.HARD, MISTAKES.MANY_TOKENS_IN_END.toString(), curNode);
                return true;
            }
        }
        return false;
    }

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
     * @param joinNode активируемые Join
     * @param mask маска шага
     * @param resultMasks результирующие маски шага
     * @return новая маска шага или -1, если эл-т не активирован
     */
    private StringBuilder activateJoin(ADNodesList.ADNode joinNode, StringBuilder mask, List<StringBuilder> resultMasks, Stack<Integer>newColors){
        StringBuilder maskCur = new StringBuilder(mask);
        StringBuilder newMask = changeJoinMask(joinNode, maskCur);
        if (newMask.toString().equals("-1")) return new StringBuilder("-1");     // эл-т не удалось активировать

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
    private StringBuilder changeJoinMask(ADNodesList.ADNode element, StringBuilder mask){
        StringBuilder newMask = new StringBuilder(mask);
        int color = 0;
        // все предшествующие элементы должны содержать токен одного цвета

        for (int i = 0; i < element.prevSize(); i++) {
            int idPrev = ((DiagramElement)element.getPrev(i).getValue()).petriId;
            if (mask.charAt(idPrev) == NO_TOKEN)
                return new StringBuilder("-1");
            try {
                if(i==0) color = colors.get(idPrev).peek(); // TODO
            }
            catch (Exception e){
                int k=0;
            }

            if(colors.get(idPrev).peek()!=color)
                return new StringBuilder("-1");
            newMask.setCharAt(idPrev, NO_TOKEN);    // удаляем токены из предшествующих

        }

        // связываем эл-т со стеком цветов и удаляем цвета из предыдущих эл-в
        for (int i = 0; i < element.prevSize(); i++) {
            int idPrev = ((DiagramElement) element.getPrev(i).getValue()).petriId;
            if (i == 0) {
                colors.get(idPrev).pop();
                colors.put(((DiagramElement) element.getValue()).petriId, colors.get(idPrev));  // переносим цвета
            }
            colors.put(idPrev, new Stack<>());  // удаляем цвета
        }
        // если элемент активирован, устанавливаем в него токен
        newMask.setCharAt(((DiagramElement)element.getValue()).petriId, NEW_TOKEN);
        return newMask;
    }

    /**
     * Ошибки, которые могут возникнуть на данном этапе
     */
    private enum MISTAKES{
        TWO_TOKENS,
        DEAD_ROAD,
        MANY_TOKENS_IN_END,
        COULD_NOT_REACH_FINAL,
        FINAL_COLOR_TOKEN;
        @Override
        public String toString() {
            switch (this) {
                // просто пересечение двух токенов
                case TWO_TOKENS: return "в элементе пересеклись токены. Возможно отсутствие синхронизатора";
                case DEAD_ROAD: return "тупик";
                // возможно пересечение двух токенов в конечном состоянии из-за отсутствия синхронизатора
                case MANY_TOKENS_IN_END: return "при достижении конечного состояния остались токены";
                case COULD_NOT_REACH_FINAL: return "недостижимое конечное состояние. Возможно имеется синхронизатор, " +
                        "который невозможно активировать";       // TODO: вывод масок-тупиков
                case FINAL_COLOR_TOKEN: return "достигли конечное состояние с цветным токеном. Отсутствует парный синхронизатор";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
