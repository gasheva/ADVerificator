package xmiparser;

import Model.ADNodesList;
import debugging.Debug;
import entities.ActivityNode;
import entities.DiagramElement;
import entities.ElementType;
import result.VerificationResult;

import java.util.*;

public class PetriNet {
    public static final char NO_TOKEN = '0';
    public static final char TOKEN = '1';
    public static final char NEW_TOKEN = '2';
    public final  int NO_COLOR = 0;
    private Random random = new Random();

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
    }

    /**
     * Генерация рандомного цвета для токена, проходящего через разветвитель
     * @return
     */
    private int generateRandomColor(){
        return random.nextInt(Integer.MAX_VALUE);
    }

    public void petriCheck(ADNodesList adList){
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
        Debug.println("Mask = " + mask);
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
            // маска, которую будем изменять по мере деактивации токенов
            StringBuilder stepMask = new StringBuilder(originMask);
            stepResultMasks.add(new StringBuilder(stepMask));   // добавляем в список результирующих масок исходную (изменится в процессе)

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
                // получаем цвета токена текущего эл-та
                Stack<Integer>newColors = colors.get(curNodeIndex);
//                colors.put(curNodeIndex, new Stack<>());

                // особо обрабатываем ситуации, когда элемент имеет несколько выходных переходов
                if(curNode.nextSize()>1) {

                    // если эл-т разветвитель, то последующее состояние единственно, однако надо установить несколько токенов за раз
                    if (curNode.getValue().getType() == ElementType.FORK) {
                        // удаляем из маски токен
                        stepMask.setCharAt(curNodeIndex, NO_TOKEN);
                        // добавляем новый цвет токена
                        newColors.push(generateRandomColor());

                        // активируем все следующие элементы
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            // сразу после форка не может быть join'a, поэтому все эл-ты будут активированы
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;
                            // проверка, что эл-т не был раннее активирован
                            if(wasAlreadyActive(indexOfNewToken, stepResultMasks)){
                                ElementType type = curNode.getNext(j).getValue().getType();
                                writeMistake("", type.toString(), type==ElementType.ACTIVITY? ((ActivityNode) curNode.getNext(j).getValue()).getName():"", MISTAKES.TWO_TOKENS.toString());
                                return;
                            }
                            // изменяем существующие результирующие маски
                            updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks);
                            // связываем цвета с эл-м
                            colors.put(indexOfNewToken, newColors);
                            colors.put(curNodeIndex, new Stack<>());
                        }
                    }
                    // если это условный оператор, то он порождает несколько возможных последующих состояний
                    else {
                        List<StringBuilder> decisionMasks = new LinkedList<>();
                        // активируем следующие элементы по одному
                        for (int j = 0; j < curNode.nextSize(); j++) {
                            int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;      // индекс активируемого эл-та
                            // проверка, что эл-т не был раннее активирован
                            if(wasAlreadyActive(indexOfNewToken, stepResultMasks))
                            {
                                ElementType type = curNode.getNext(j).getValue().getType();
                                writeMistake("", type.toString(), type==ElementType.ACTIVITY? ((ActivityNode) curNode.getNext(j).getValue()).getName():"", MISTAKES.TWO_TOKENS.toString());
                                return;
                            }
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
                                // связываем цвета с эл-м
                                colors.put(indexOfNewToken, newColors);
                                colors.put(curNodeIndex, new Stack<>());
                            }

                        }
                        // меняем результирующую маску
                        stepResultMasks = decisionMasks;
                    }
                }
                else{       // если выходной переход один
                    int indexOfNewToken = ((DiagramElement) curNode.getNext(0).getValue()).petriId;
                    // проверка, что эл-т не был раннее активирован
                    if(wasAlreadyActive(indexOfNewToken, stepResultMasks))
                    {
                        ElementType type = curNode.getNext(0).getValue().getType();
                        writeMistake("", type.toString(), type==ElementType.ACTIVITY? ((ActivityNode) curNode.getNext(0).getValue()).getName():"", MISTAKES.TWO_TOKENS.toString());
                        return;
                    }
                    // если это Join
                    if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
                        StringBuilder result = activateJoin(curNode.getNext(0), stepMask, stepResultMasks, newColors);
                        if (!result.toString().equals("-1"))
                            stepMask = result;
                    }
                    else {
                        stepMask.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                        updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks);
                        // связываем цвета с эл-м
                        colors.put(indexOfNewToken, newColors);
                        colors.put(curNodeIndex, new Stack<>());
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
                writeMistake("При активации элементов " + activateIndexes+" возник" + MISTAKES.DEAD_ROAD);
            }
            Debug.print("");
            // проверяем, что новой маски нет во множестве обработанных и добавляем в необработанные в таком случае
            for (StringBuilder resultMask : stepResultMasks) {
                if(!masksInUsed.contains(resultMask.toString())){
                    // проверяем, не достигли ли конечной маркировки (существует путь позволяющей до нее добраться)
                    if(resultMask.charAt(indexOfFinalNode) == TOKEN) {
                        canReachFinal = true;
                        if(colors.get(indexOfFinalNode).peek()!=NO_COLOR)
                            writeMistake(MISTAKES.FINAL_COLOR_TOKEN.toString());
                        //region Проверка, что не осталось токенов
                        int tokenCount = 0;
                        StringBuilder activateIndexes = new StringBuilder();
                        for (int j = 0; j < originMask.length(); j++) {
                            if(originMask.charAt(j)==TOKEN) {
                                tokenCount++;
                                activateIndexes.append(j).append(" ");
                            }
                        }
                        if (tokenCount>1)
                            writeMistake("Достижение конечного состояния с активированными эл-ми" +
                                    activateIndexes +". " + MISTAKES.MANY_TOKENS_IN_END);
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
            writeMistake(MISTAKES.COULD_NOT_REACH_FINAL.toString());
        }
        else{
            Debug.println("Достигли конечное состояние");
        }
    }

    //TODO: проверка, что на два токена (активируемый эл-т уже был активирован (1/2); тупиковые листья уже обрабатываются; +
    // цветные токены (изменить форк и джоин)
    // выводить таблицу ид (обычный и петри), тип эл-та, описание эл-та
    // при наведении на ошибку - возможные причины

    private void writeMistake(String mistake){
        VerificationResult.mistakes.add(mistake);
    }

    private void writeMistake(String level, String elType, String name, String mistake){
        VerificationResult.mistakes.add(level+" "+ elType+ " \""+name+"\": "+mistake);
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
                case TWO_TOKENS: return "в элементе пересеклись токены. Возможен тупик";
                case DEAD_ROAD: return "тупик";
                case MANY_TOKENS_IN_END: return "при достижении конечного состояния остались токены";
                case COULD_NOT_REACH_FINAL: return "недостижимое конечное состояние";
                case FINAL_COLOR_TOKEN: return "достигли конечное состояние с цветным токеном. Отсутствует парный синхронизатор";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private boolean wasAlreadyActive(int tokenIndex, List<StringBuilder> stepResultMasks){
        for (StringBuilder stepResultMask : stepResultMasks) {
            if(stepResultMask.charAt(tokenIndex)!=NO_TOKEN)
                return true;
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
            if(i==0) color = colors.get(idPrev).peek();
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

}
