package result;

import Model.ADNodesList;
import entities.BaseNode;
import entities.DiagramElement;
import verification.Level;

public class MistakeFactory {
    public static void createMistake(Level level, String mistake, ADNodesList.ADNode element){
        Mistakes.mistakes.add(new ElementMistake(mistake, level, element));
    }
    public static void createMistake(Level level, String mistake){
        Mistakes.mistakes.add(new GeneralMistake(mistake, level));
    }
    public static void createMistake(Level level, String mistake, BaseNode element){
        Mistakes.mistakes.add(new NotADNodeMistakes(mistake, level, element));
    }
}
