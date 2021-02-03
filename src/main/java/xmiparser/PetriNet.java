package xmiparser;

import Model.ADNodesList;
import debugging.Debug;
import entities.DiagramElement;
import entities.ElementType;
import result.MistakeFactory;
import verification.Level;

import java.util.*;

public class PetriNet {
    public static final char NO_TOKEN = '0';
    public static final char TOKEN = '1';
    public static final char NEW_TOKEN = '2';
    public final  int NO_COLOR = 0;
    private final Random random = new Random();
    private ADNodesList list;

//    private Map<Integer, Stack<Integer>> colors = new HashMap<>(); // для каждого элемента хранит стек цветов

    /**
     * Создает маску, в которой все эл-ты неактивны
     * @param length необходимая длина
     * @return строка, заполненная нулями
     */
    private List<Token> createEmptyMask(int length){
        List<Token> mask = new LinkedList<>();
        for (int i = 0; i < length; i++) {
            mask.add(new Token());
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
        Queue<List<Token>> leaves = new LinkedList<>();       // необработанные маски
        Set<String> masksInUsed = new HashSet<>(adList.getPetriElementsCount());   // использованные маски

        // для каждого эл-та создаем стек цветов
//        for (int i = 0; i < adList.getPetriElementsCount(); i++) {
//            Stack<Integer> temp = new Stack<>();
//            colors.put(i, temp);
//        }

        // ищем начальное состояние
        ADNodesList.ADNode initNode = adList.findInitial();
        ADNodesList.ADNode finalNode = adList.findFinal();
        int indexOfFinalNode = ((DiagramElement)finalNode.getValue()).petriId;


        // создаем маску и добавляем ее в необработанные
        List<Token> maskTmp = createEmptyMask(adList.getPetriElementsCount());
        setNewPaleToken(maskTmp, ((DiagramElement)initNode.getValue()).petriId);

        leaves.add(maskTmp);
        masksInUsed.add(maskToString(maskTmp));   // добавляем в множество использованных
//        colors.get(((DiagramElement)initNode.getValue()).petriId).push(NO_COLOR);   // добавляем цвет токена

        boolean cont = true;
        boolean canReachFinal = false;

        List<List<Token>> stepResultMasks = new LinkedList<>();         // содержит маски, кот могут получиться на каждом шаге

        // главный цикл прохода по всем элементам, участвующих в проверке
        while (cont) {
            List<Token> originMask = leaves.poll();  // берем первую маску
            stepResultMasks.clear();
            List<Token> stepMask = copyMask(originMask); // маска, которую будем изменять по мере деактивации токенов
            stepResultMasks.add(copyMask(stepMask));   // Если список масок не изменится, то будет тупик, тк текущая маска уже добавлена в использованные

            int i = 0;
            Debug.println("Or: " + maskToString(originMask));

            // проходим по всем элементам маски, находим активированные
            // и проверяем, можно ли активировать следующий элемент
            for (int stepProhod = 0; stepProhod < 2; stepProhod++){     // сначала проходим по условным, затем по остальным эл-м
                i=0;
                while (i < stepMask.size()) {
                    if (stepProhod == 0 && adList.getNodeByPetriIndex(i).getValue().getType() != ElementType.DECISION) {
                        i++;
                        continue;   // интересуют только эл-ты, содержащие токены на данном шаге
                    }
                    if (stepProhod == 0 && adList.getNodeByPetriIndex(i).getValue().getType() == ElementType.DECISION && stepMask.get(i).type != TOKEN) {
                        i++;
                        continue;   // интересуют только эл-ты, содержащие токены на данном шаге
                    }
                    if (stepProhod == 1 && stepMask.get(i).type != TOKEN) {
                        i++;
                        continue;   // интересуют только эл-ты, содержащие токены на данном шаге
                    }
//                if (stepMask.get(i).type != TOKEN) {
//                    i++;
//                    continue;   // интересуют только эл-ты, содержащие токены на данном шаге
//                }
                    // нашли активный элемент
                    ADNodesList.ADNode curNode = adList.getNodeByPetriIndex(i);     //индекс в списке совпадает с петри ид эл-та
                    int curNodeIndex = ((DiagramElement) curNode.getValue()).petriId;
//                Stack<Integer>newColors = colors.get(curNodeIndex);     // получаем цвета токена текущего эл-та
                    List<Integer> colorsCurToken = new LinkedList<>(stepMask.get(curNodeIndex).colors);

                    // особо обрабатываем ситуации, когда элемент имеет несколько выходных переходов
                    if (curNode.nextSize() > 1) {
                        // если эл-т разветвитель, то последующее состояние единственно, однако надо установить несколько токенов за раз
                        if (curNode.getValue().getType() == ElementType.FORK) {
//                        stepMask.setCharAt(curNodeIndex, NO_TOKEN);     // удаляем из маски токен
//                        newColors.push(generateRandomColor());          // добавляем новый цвет токена
                            colorsCurToken.add(generateRandomColor());

                            // активируем все следующие элементы
                            for (int j = 0; j < curNode.nextSize(); j++) {
                                // сразу после форка не может быть join'a, поэтому все эл-ты будут активированы
                                int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;
                                // проверка, что эл-т не был раннее активирован
                                if (wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(j))) return;
                                // изменяем существующие результирующие маски
                                updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks, colorsCurToken);
                                // moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                            }
                            setNewEmptyToken(stepMask, curNodeIndex);
                        }
                        // если это условный оператор, то он порождает несколько возможных последующих состояний
                        else {
                            List<List<Token>> decisionMasks = new LinkedList<>();   // содержит все возможные маски вариантов перемещения токена
                            boolean tokenWasRemoved = false;
                            // активируем следующие элементы по одному
                            for (int j = 0; j < curNode.nextSize(); j++) {
                                int indexOfNewToken = ((DiagramElement) curNode.getNext(j).getValue()).petriId;      // индекс активируемого эл-та
                                // проверка, что эл-т не был раннее активирован
                                if (wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(j))) return;
                                // Join обрабатывается с помощью reverse проверки
                                if ((curNode.getNext(j).getValue()).getType() == ElementType.JOIN) {    //TODO: не уверена
                                    List<List<Token>> temp = copyMasks(stepResultMasks);
                                    List<Token> result = activateJoin(curNode.getNext(j), stepMask, temp, colorsCurToken);
                                    if (result != null) {
                                        stepMask = result;
                                        decisionMasks.addAll(temp);
                                        tokenWasRemoved = true;
                                    }
                                } else {        // если это не join
                                    // удаляем из маски шага
//                                stepMask.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);

                                    // в каждой существующей результирующей маске меняем токен и сохраняем в промежуточный список
                                    // в итоге получится новый список, содержащий несколько вариантов результирующих масок,
                                    // в каждом из которых создан новый токен в зависимости от активированного следующего эл-та
                                    for (List<Token> resultMask : stepResultMasks) {
                                        List<Token> temp = copyMask(resultMask);
//                                    temp.setCharAt(indexOfNewToken, NEW_TOKEN);
                                        setToken(temp, indexOfNewToken, NEW_TOKEN, colorsCurToken);     // добавляем новый токен
//                                    temp.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                                        setNewEmptyToken(temp, curNodeIndex);           // удаляем токен текущего эл-та
                                        decisionMasks.add(temp);
                                        tokenWasRemoved = true;
                                    }
//                                moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                                }

                            }
                            // изменяем маску шага и список результирующих масок, если токен был удален
                            if (tokenWasRemoved) {
                                setNewEmptyToken(stepMask, ((DiagramElement) curNode.getValue()).petriId);
                                // меняем результирующую маску
                                stepResultMasks = decisionMasks;
                            }
                        }
                    } else {       // если выходной переход один
                        int indexOfNewToken = ((DiagramElement) curNode.getNext(0).getValue()).petriId;
                        // проверка, что эл-т не был раннее активирован
                        if (wasAlreadyActive(indexOfNewToken, stepResultMasks, curNode.getNext(0))) return;
                        // если следующий Join
                        if ((curNode.getNext(0).getValue()).getType() == ElementType.JOIN) {
                            List<Token> result = activateJoin(curNode.getNext(0), stepMask, stepResultMasks, colorsCurToken);
                            if (result != null)
                                stepMask = result;
                        } else {
//                        stepMask.setCharAt(((DiagramElement) curNode.getValue()).petriId, NO_TOKEN);
                            updateStepMasks(((DiagramElement) curNode.getValue()).petriId, indexOfNewToken, stepResultMasks, colorsCurToken);
                            setNewEmptyToken(stepMask, curNodeIndex);
//                        moveColors(indexOfNewToken, curNodeIndex, newColors);   // связываем цвета с эл-м
                        }
                    }
                    i++;
                }
        }
            // в результирующих масках заменяем NEW_Token на TOKEN
//            stepResultMasks.forEach(x->x.replace(0, x.length(), x.toString().replace(String.valueOf(NEW_TOKEN), String.valueOf(TOKEN))));

            for (List<Token> stepResultMask : stepResultMasks) {
                stepResultMask.forEach(x->x.type=x.type==NEW_TOKEN?TOKEN:x.type);
                Debug.println("Re: "+maskToString(stepResultMask));
            }

            // заканчиваем тогда, когда leaves пустой или невозможно передвинуть ни один токен
            // не был передвинут ни один токен, но не все листья просмотрены
            if (stepResultMasks.isEmpty() && !leaves.isEmpty()){
                StringBuilder activateIndexes = new StringBuilder();
                for (int j = 0; j < originMask.size(); j++) {
                    if(originMask.get(j).type==TOKEN)
                        activateIndexes.append(j).append(" ");      // сохраняем маску тупика
                }
//                writeMistake("При активации элементов " + activateIndexes+" возник" + MISTAKES.DEAD_ROAD);
                MistakeFactory.createMistake(Level.HARD, activateIndexes + " "+ MISTAKES.DEAD_ROAD.toString());
            }
            Debug.print("");
            // проверяем, что новой маски нет во множестве обработанных и добавляем в необработанные в таком случае
            for (List<Token> resultMask : stepResultMasks) {
//                if(!masksInUsed.contains(resultMask.toString())){
                if(!findInMasksInUsed(masksInUsed, resultMask)){
                    // проверяем, не достигли ли конечной маркировки (существует путь позволяющей до нее добраться)
                    if(resultMask.get(indexOfFinalNode).type == TOKEN) {
                        canReachFinal = true;
//                        if(colors.get(indexOfFinalNode).peek()!=NO_COLOR) {
                        if(resultMask.get(indexOfFinalNode).peekLastColor()!=NO_COLOR){
//                            writeMistake(MISTAKES.FINAL_COLOR_TOKEN.toString());
                            MistakeFactory.createMistake(Level.HARD, MISTAKES.FINAL_COLOR_TOKEN.toString());
                            return;
                        }
                        //region Проверка, что не осталось токенов
                        int tokenCount = 0;
                        StringBuilder activateIndexes = new StringBuilder();
                        for (int j = 0; j < resultMask.size(); j++) {
                            if(resultMask.get(j).type==TOKEN) {
                                tokenCount++;
                                activateIndexes.append(j).append(" ");
                            }
                        }
                        if (tokenCount>1) {
//                            writeMistake("Достижение конечного состояния с активированными эл-ми" +
//                                    activateIndexes +". " + MISTAKES.MANY_TOKENS_IN_END);
                            MistakeFactory.createMistake(Level.HARD, MISTAKES.MANY_TOKENS_IN_END.toString());
                            return;
                        }
                        //endregion
                    }
                    else leaves.add(copyMask(resultMask));
//                    masksInUsed.add(resultMask.toString());
                    masksInUsed.add(maskToString(resultMask));      // добавляем полученную маску в множество обработанных
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

    private void setNewPaleToken(List<Token> mask, int index) {
        mask.set(index, new Token(PetriNet.TOKEN, new LinkedList<>(Arrays.asList(NO_COLOR))));
    }

    private void setNewEmptyToken(List<Token> mask, int index){
        mask.set(index, new Token());
    }

    private void setToken(List<Token> mask, int index, Token old) {
        mask.set(index, new Token(old));
    }
    private void setToken(List<Token> mask, int index, char token) {
        mask.get(index).type = token;
    }

    private void setToken(List<Token> mask, int index, char token, List<Integer>colors) {
        mask.set(index, new Token(token, colors));
    }

    private List<Token> copyMask(List<Token> old){
        List<Token> newlst = new LinkedList<>();
        old.forEach(x->newlst.add(new Token(x)));
        return newlst;
    }
    private List<List<Token>> copyMasks(List<List<Token>> old){
        List<List<Token>> newlst = new LinkedList<>();
        for (List<Token> masks : old) {
            newlst.add(copyMask(masks));
        }
        return newlst;
    }

    //TODO: проверка, что на два токена (активируемый эл-т уже был активирован (1/2); тупиковые листья уже обрабатываются; +
    // цветные токены (изменить форк и джоин)  +
    // выводить таблицу ид (обычный и петри), тип эл-та, описание эл-та
    // при наведении на ошибку - возможные причины


//    private void moveColors(int indexOfNewToken, int curNodeIndex, Stack<Integer> newColors){
//        colors.put(indexOfNewToken, (Stack<Integer>)newColors.clone());
//        colors.put(curNodeIndex, new Stack<>());
//    }

    private String maskToString(List<Token> mask){
        final String[] maskStr = {""};
        mask.forEach(x-> maskStr[0] +=x.type);
        return maskStr[0];
    }
    private boolean findInMasksInUsed(Set<String> inUsed, List<Token> mask){
        return inUsed.contains(maskToString(mask));
    }

    /**
     * Проверка был ли данный элемент раннее активирован. Если да, то проверка завершается
     * @param tokenIndex
     * @param stepResultMasks
     * @param curNode
     * @return
     */
    private boolean wasAlreadyActive(int tokenIndex, List<List<Token>> stepResultMasks, ADNodesList.ADNode curNode){
        for (List<Token> stepResultMask : stepResultMasks) {
            if(stepResultMask.get(tokenIndex).type!=NO_TOKEN) {
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
     * Обновляет маски списка, создавая новые токены по указанному индексу в списке масок, копирует список цветов,
     * удаляет токен по индексу в каждой маске из списка
     * @param removedIndex
     * @param newIndex
     * @param stepMasks
     */
    private void updateStepMasks(int removedIndex, int newIndex, List<List<Token>> stepMasks, List<Integer>colors){
        stepMasks.forEach(x -> {
            setToken(x, newIndex, NEW_TOKEN, colors);
            setNewEmptyToken(x, removedIndex);
//                    x.setCharAt(newIndex, NEW_TOKEN);
//                    x.setCharAt(removedIndex, NO_TOKEN);
                }
        );
    }
    private void updateStepMasksWithoutColorRemoving(int removedIndex, int newIndex, List<List<Token>> stepMasks){
        stepMasks.forEach(x -> {
            setToken(x, newIndex, NEW_TOKEN, x.get(removedIndex).colors);
            x.get(removedIndex).type = NO_TOKEN;
        });
    }

    private List<Token> activateJoin(ADNodesList.ADNode joinNode, List<Token> mask, List<List<Token>> resultMasks, List<Integer> curColors){
        List<Token> newMask = copyMask(mask);
        List<Token> maskCur = copyMask(mask);
        int color = 0;
        int curIndex = ((DiagramElement)joinNode.getValue()).petriId;
        List<Integer> idsPrev = new LinkedList<>();         // список ид элементов перед join
        List<Integer> newColors = new LinkedList<>(curColors);
        newColors.remove(newColors.size()-1);           // подготовили список цветов для join


        // все предшествующие элементы должны содержать токен одного цвета
        for (int i = 0; i < joinNode.prevSize(); i++) {
            int idPrev = ((DiagramElement)joinNode.getPrev(i).getValue()).petriId;
            idsPrev.add(idPrev);
            if (newMask.get(idPrev).type == NO_TOKEN)
                return null;
            if(i==0) color = newMask.get(idPrev).peekLastColor();
            if(newMask.get(idPrev).peekLastColor()!=color)
                return null;
        }
        // если все предыдущие элементы были активны, активируем текущий
        // для каждой результирующей маски шага удаляем токены и добавляем токен в join
        for (Integer id : idsPrev) {
            updateStepMasks(id, curIndex, resultMasks, newColors);
            setNewEmptyToken(maskCur, id);      // удаляем токены из маски шага
        }


//        List<Integer> newColors = new LinkedList<>();   // список цветов для синхронизатора
//        // удаляем токены из предыдущих эл-в
//        for (int i = 0; i < joinNode.prevSize(); i++) {
//            int idPrev = ((DiagramElement)joinNode.getPrev(i).getValue()).petriId;
//            if (i==0) {
//                newColors = newMask.get(idPrev).colors;
//                newColors.remove(newColors.size()-1);// удаляем последний цвет и сохраняем цвета
//            }
//            setNewEmptyToken(newMask, idPrev);
//        }
//        // устанавливаем новый токен и переписываем в него цвет, удалив один
//        setToken(newMask, ((DiagramElement) joinNode.getValue()).petriId, NEW_TOKEN, newColors);


//        // изменяем маски в соответствии с новой
//        for (int i1 = 0; i1 < maskCur.size(); i1++) {
//            int indexOfToken = i1;
//            if (maskCur.get(i1).type != newMask.get(i1).type) {
//                // меняем результирующие маски
//                resultMasks.forEach(x -> setToken(x, indexOfToken, newMask.get(indexOfToken)));
//                // меняем маску текущего шага
//                if (newMask.get(i1).type == NO_TOKEN)
//                    setNewEmptyToken(maskCur, i1);
//            }
//        }
        return maskCur;
    }

    /**
     * Активирует Join, если это возможно, возвращая маску с удаленными\добавленным токенами,
     * изменяет результирующие маски
     * @param joinNode активируемые Join
     * @param mask маска шага
     * @param resultMasks результирующие маски шага
     * @return новая маска шага или -1, если эл-т не активирован
     */
//    private List<Token> activateJoin(ADNodesList.ADNode joinNode, List<Token> mask, List<List<Token>> resultMasks){
//        List<Token> maskCur = copyMask(mask);
//        List<Token> newMask = changeJoinMask(joinNode, maskCur);
//        if (newMask==null) return null;     // эл-т не удалось активировать
//
//        // изменяем маски в соответствии с новой
//        for (int i1 = 0; i1 < maskCur.length(); i1++) {
//            int indexOfToken = i1;
//            if (maskCur.charAt(i1) != newMask.charAt(i1)) {
//                // меняем результирующие маски
//                resultMasks.forEach(x -> x.setCharAt(indexOfToken, newMask.charAt(indexOfToken)));
//                // меняем маску текущего шага
//                if (newMask.charAt(i1) == NO_TOKEN)
//                    maskCur.setCharAt(i1, NO_TOKEN);
//            }
//        }
//
//        return maskCur;
//    }

    /**
     * Активирует элемент, если это возможно
     * из переданного массива индексов устанавливает индексы предшествующих элементов в -1, если эл-т был активирован
     * @return измененную маску, в кот добавлен новый активный элемент или -1, если элемент не был активирован, или
     * -2, если было нарушено условие безопасности (два токена в одном эл-те)
     */
//    private List<Token> changeJoinMask(ADNodesList.ADNode element, List<Token> mask){
//        List<Token> newMask = copyMask(mask);
//        int color = 0;
//        // все предшествующие элементы должны содержать токен одного цвета
//
//        for (int i = 0; i < element.prevSize(); i++) {
//            int idPrev = ((DiagramElement)element.getPrev(i).getValue()).petriId;
//            if (newMask.get(idPrev).type == NO_TOKEN)
//                return null;
////                if(i==0) color = colors.get(idPrev).peek(); // TODO
//                if(i==0) color = newMask.get(idPrev).peekLastColor();
//
////            if(colors.get(idPrev).peek()!=color)
//            if(newMask.get(idPrev).peekLastColor()!=color)
//                return null;
////            newMask.setCharAt(idPrev, NO_TOKEN);    // удаляем токены из предшествующих
//        }
//
//        // если все предыдущие элементы были активны, активируем текущий
//        List<Integer> newColors = new LinkedList<>();
//        // удаляем токены из предыдущих эл-в
//        for (int i = 0; i < element.prevSize(); i++) {
//            int idPrev = ((DiagramElement)element.getPrev(i).getValue()).petriId;
//            if (i==0) {
//                newColors = newMask.get(idPrev).colors;
//                newColors.remove(newColors.size()-1);// удаляем последний цвет и сохраняем цвета
//            }
//            setNewEmptyToken(newMask, idPrev);
//        }
//        // устанавливаем новый токен и переписываем в него цвет, удалив один
//        setToken(newMask, ((DiagramElement) element.getValue()).petriId, NEW_TOKEN, newColors);
//
//        // связываем эл-т со стеком цветов и удаляем цвета из предыдущих эл-в
////        for (int i = 0; i < element.prevSize(); i++) {
////            int idPrev = ((DiagramElement) element.getPrev(i).getValue()).petriId;
////            if (i == 0) {
////                colors.get(idPrev).pop();
////                colors.put(((DiagramElement) element.getValue()).petriId, colors.get(idPrev));  // переносим цвета
////            }
////            colors.put(idPrev, new Stack<>());  // удаляем цвета
////        }
////        // если элемент активирован, устанавливаем в него токен
////        newMask.setCharAt(((DiagramElement)element.getValue()).petriId, NEW_TOKEN);
//        return newMask;
//    }

    private class Token{
        char type;
        List<Integer> colors;
        public Token(){
            type = NO_TOKEN;
            colors = new LinkedList<>();
        }
        public Token(char type, List<Integer> colors) {
            this.type = type;
            this.colors = new LinkedList<>(colors);
        }
        public Token(Token old){
            this.type = old.type;
            this.colors = new LinkedList<>(old.colors);
        }
        public int peekLastColor(){
            return colors.get(colors.size()-1);
        }
        public int pop(){
            int color = peekLastColor();
            colors.remove(colors.size()-1);
            return color;
        }
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
                        "который невозможно активировать";       // проверьте, что все переходы, ведущие в синхронизаторы могут быть активны одновременно TODO: вывод масок-тупиков
                case FINAL_COLOR_TOKEN: return "достигли конечное состояние с цветным токеном. Отсутствует парный синхронизатор";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
