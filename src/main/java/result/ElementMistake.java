package result;

import Model.ADNodesList;
import entities.DiagramElement;
import verification.Level;

import java.util.List;

/**
 * Ошибки элементов, кот содержат предыдущие и последующие эл-ты (участвующие в проверке сетью Петри)
 */
public class ElementMistake extends Mistake{
    private final ADNodesList.ADNode element;

    public ElementMistake(String mistake, Level level, ADNodesList.ADNode element, int id) {
        super(mistake, level, id);
        this.element = element;
        if (!(element.getValue() instanceof DiagramElement)){
            throw new IllegalArgumentException("Элемент не был приведен к классу DiagramElement");
        }
    }

    public int getElementPetriId(){
        return ((DiagramElement)element.getValue()).petriId;
    }
    public List<Integer> getNextPetriIds(){
        return element.getNextPetriIds();
    }
    public List<Integer> getPrevPetriIds(){
        return element.getPrevPetriIds();
    }

    public int getX() {
        return element.getValue().x;
    }
    public int getY() {
        return element.getValue().y;
    }
    public String getDescr(){
        return ((DiagramElement)element.getValue()).getDescription();
    }
    public String getType(){
        return (element.getValue()).getType().toString();
    }
}
