package result;

import Model.ADNodesList;
import entities.BaseNode;
import entities.ControlFlow;
import entities.Swimlane;
import verification.Level;

/**
 * Ошибки элементов, кот не содержат предыдущие и последующие эл-ты (не участвующие в проверке сетью Петри)
 * Н-р, Swimlane, Arrow
 */
public class NotADNodeMistakes extends Mistake{
    private BaseNode element;
    public NotADNodeMistakes(String mistake, Level level, BaseNode element, int id) {
        super(mistake, level, id);
        // добавление спецификации в ошибку
        if(element instanceof Swimlane)
            this.mistake = ((Swimlane)element).getName() +": "+ mistake;
        if(element instanceof ControlFlow)
            this.mistake = ((ControlFlow)element).getText() +": "+ mistake;
        this.element = element;
        // TODO: для стрелки - откуда и куда (подсветка), для дорожки - название
    }

    /**
     * Получить таргет ид, если данный элемент - переход
     * @return ид таргет элемента или -1, если это не переход
     */
    public String getNext(){
        if(element instanceof ControlFlow)
            return ((ControlFlow)element).getTargets();
        return "";
    }

    /**
     * Получить соурс ид, если данный элемент - переход
     * @return ид соурс элемента или пустая строка, если это не переход
     */
    public String getPrev(){
        if(element instanceof ControlFlow)
            return ((ControlFlow)element).getSrc();
        return "";
    }

}
