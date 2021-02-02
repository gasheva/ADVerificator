package result;

import Model.ADNodesList;
import entities.BaseNode;
import entities.DiagramElement;
import verification.Level;

public class MistakeFactory {
    /**
     * Создание ошибки, содержащей элемент диаграммы
     * @param level
     * @param mistake
     * @param element
     */
    public static void createMistake(Level level, String mistake, ADNodesList.ADNode element){
        Mistakes.mistakes.add(new ElementMistake(mistake, level, element, Mistakes.mistakes.size()));
    }

    /**
     * Созадние ошибки, не содержащей ссылки не на какой элемент
     * @param level
     * @param mistake
     */
    public static void createMistake(Level level, String mistake){
        Mistakes.mistakes.add(new GeneralMistake(mistake, level, Mistakes.mistakes.size()));
    }

    /**
     * Ошибки для переходов и для дорожек
     * @param level
     * @param mistake
     * @param element
     */
    public static void createMistake(Level level, String mistake, BaseNode element){
        Mistakes.mistakes.add(new NotADNodeMistakes(mistake, level, element, Mistakes.mistakes.size()));
    }
}
