package result;

import Model.ADNodesList;
import entities.BaseNode;
import entities.DiagramElement;
import entities.ElementType;
import verification.Level;

import java.util.LinkedList;
import java.util.List;

/**
 * Ошибки элементов, кот содержат предыдущие и последующие эл-ты (участвующие в проверке сетью Петри)
 */
public class ElementMistake extends Mistake{
    private final ADNodesList.ADNode element;

    public ElementMistake(String mistake, Level level, ADNodesList.ADNode element) {
        super(mistake, level);
        this.element = element;
        if (element.getValue() instanceof DiagramElement){
            throw new IllegalArgumentException("Элемент не был приведен к классу DiagramElement");
        }
    }

    public ElementType getElementType(){return element.getValue().getType();}
    public String getElementDescription(){return ((DiagramElement)element.getValue()).getDescription();}
    public List<String> getNextIds(){
        return element.getNextIds();
    }
    public List<String> getPrevIds(){
        return element.getPrevIds();
    }
}
