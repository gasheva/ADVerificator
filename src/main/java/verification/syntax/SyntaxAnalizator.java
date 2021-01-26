package verification.syntax;

import Model.ADNodesList;
import entities.*;
import result.Mistake;
import result.MistakeFactory;
import result.VerificationResult;
import verification.Level;

import java.util.List;

public class SyntaxAnalizator {
    private Level level;
    private ADNodesList diagramElements;
    private int initialCount = 0;
    private int finalCount = 0;
    private int activityCount = 0;

    public SyntaxAnalizator(Level level) {
        this.level = level;
    }

    //region Getter-Setter

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public ADNodesList getDiagramElements() {
        return diagramElements;
    }

    public void setDiagramElements(ADNodesList diagramElements) {
        this.diagramElements = diagramElements;
    }

    //endregion

    public void check() {
        for (int i = 0; i < diagramElements.size(); i++) {
            BaseNode currentNode = diagramElements.get(i);
            switch (diagramElements.get(i).getType()) {
                case FLOW:
                    break;
                case INITIAL_NODE:
                    checkIfInPartion((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    if (((DiagramElement)currentNode).outSize()==0)
                        MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_OUT.toString(), diagramElements.getNode(i));
//                        writeMistake(Level.HARD.toString(), currentNode.getType().toString(), "", MISTAKES.NO_OUT.toString());
                    checkInitial();
                    break;
                case FINAL_NODE:
                    checkIfInPartion((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    if (((DiagramElement)currentNode).inSize()==0)
                        MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_IN.toString(), diagramElements.getNode(i));
//                        writeMistake(Level.HARD.toString(), currentNode.getType().toString(), "", MISTAKES.NO_IN.toString());
                    checkFinal();
                    break;
                case FORK:
                    checkIfInPartion((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    checkInOut((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    checkFork((ForkNode)currentNode, diagramElements.getNode(i));
                    break;
                case JOIN:
                    checkIfInPartion((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    checkInOut((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    break;
                case MERGE:
                    checkIfInPartion((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    checkInOut((DiagramElement)currentNode, "", diagramElements.getNode(i));
                    break;
                case ACTIVITY:
                    checkIfInPartion((DiagramElement)currentNode, ((ActivityNode)currentNode).getName(), diagramElements.getNode(i));
                    checkInOut((DiagramElement)currentNode, ((ActivityNode)currentNode).getName(), diagramElements.getNode(i));
                    checkActivity((ActivityNode)diagramElements.get(i), diagramElements.getNode(i));
                    break;
                case DECISION:
                    checkIfInPartion((DiagramElement)currentNode, ((DecisionNode)currentNode).getQuestion(), diagramElements.getNode(i));
                    checkInOut((DiagramElement)currentNode, ((DecisionNode)currentNode).getQuestion(), diagramElements.getNode(i));
                    checkDecision((DecisionNode)diagramElements.get(i), diagramElements.getNode(i));
                    break;
                case STRANGE:
                    break;
            }
        }
        if (finalCount==0)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_FINAL.toString());
//            writeMistake(Level.HARD.toString()+" "+MISTAKES.NO_FINAL.toString());
        if (initialCount==0)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_INITIAL.toString());
//            writeMistake(Level.HARD.toString()+" "+MISTAKES.NO_INITIAL.toString());
        if (activityCount==0)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_ACTIVITIES.toString());
//            writeMistake(Level.HARD.toString()+" "+MISTAKES.NO_ACTIVITIES.toString());

        // проверка, что имена активностей уникальны
        List<ActivityNode> activities = diagramElements.getAllActivities();
        for (int i = 0; i < activities.size()-1; i++) {
            for (int j = i+1; j < activities.size(); j++) {
                if (activities.get(i).getName().equals(activities.get(j).getName()))
                    MistakeFactory.createMistake(Level.HARD, MISTAKES.REPEATED_ACT.toString(), diagramElements.getNode(i));
//                    writeMistake(Level.HARD.toString(), activities.get(i).getType().toString(), activities.get(i).getName(), MISTAKES.REPEATED_ACT.toString());
            }
        }
    }

    /**
     * Проверка, что элемент принадлежит какому-либо участнику
     * @param currentNode
     * @param name
     */
    private void checkIfInPartion(DiagramElement currentNode, String name, ADNodesList.ADNode node){
        if (currentNode.getInPartition().equals(""))
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_PARTION.toString(), node);
//            writeMistake(Level.HARD.toString(), currentNode.getType().toString(), name, MISTAKES.NO_PARTION.toString());
    }

    /**
     * Проверка, что имеется хотя бы один входящий\выходящий переход
     * @param currentNode
     * @param name
     */
    private void checkInOut(DiagramElement currentNode, String name, ADNodesList.ADNode node){
        if ((currentNode).inSize()==0)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_IN.toString(), node);
//            writeMistake(Level.HARD.toString(), currentNode.getType().toString(), name, MISTAKES.NO_IN.toString());
        if ((currentNode).outSize()==0)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.NO_OUT.toString(), node);
//            writeMistake(Level.HARD.toString(), currentNode.getType().toString(), name, MISTAKES.NO_OUT.toString());
    }

    private void checkFork(ForkNode fork, ADNodesList.ADNode node){
        for (int i = 0; i < fork.outSize(); i++) {
            ElementType elementType = diagramElements.get(((ControlFlow)diagramElements.get(fork.getOutId(i))).getTargets()).getType();
            if (elementType!=ElementType.ACTIVITY && elementType!= ElementType.DECISION)
                MistakeFactory.createMistake(Level.HARD, MISTAKES.OUT_NOT_IN_ACT.toString(), node);
//                writeMistake(level.toString(), fork.getType().toString(), "", MISTAKES.OUT_NOT_IN_ACT.toString());
        }
    }

    private void checkInitial(){
        initialCount++;
        if (initialCount>1) MistakeFactory.createMistake(Level.HARD, MISTAKES.MORE_THAN_ONE_INIT.toString());//writeMistake("["+Level.HARD.toString()+"] "+MISTAKES.MORE_THAN_ONE_INIT.toString());

    }
    private void checkFinal(){
        finalCount++;
    }
    private void checkActivity(ActivityNode activity, ADNodesList.ADNode node){
        activityCount++;
        // активность имеет больше одного выходящего перехода
        if(activity.outSize()>=2)
            MistakeFactory.createMistake(Level.HARD, MISTAKES.MORE_THAN_ONE_OUT.toString(), node);
//            writeMistake(Level.HARD.toString(), activity.getType().toString(), activity.getName(), MISTAKES.MORE_THAN_ONE_OUT.toString());
    }

    private void checkDecision(DecisionNode decision, ADNodesList.ADNode node){
        boolean checkAlt = true;
        // проверка, что альтернативы есть
        if (decision.alternativeSize()==0){
            MistakeFactory.createMistake(Level.HARD, MISTAKES.DO_NOT_HAVE_ALT.toString(), node);
//            writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.DO_NOT_HAVE_ALT.toString());
            checkAlt = false;
        }

        // проверка, что альтернатив больше одной
        if (checkAlt)
        if (decision.alternativeSize()==1){
            MistakeFactory.createMistake(Level.HARD, MISTAKES.ONLY_ONE_ALT.toString(), node);
//            writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.ONLY_ONE_ALT.toString());
        }

        // проверка, что альтернативы не ведут в один и тот же элемент
        if(checkAlt) {
            for (int i = 0; i < decision.outSize() - 1; i++) {
                for (int j = i + 1; j < decision.outSize(); j++) {
                    String targetId =((ControlFlow)diagramElements.get(decision.getOutId(i))).getTargets();
                    if (targetId.equals(((ControlFlow)diagramElements.get(decision.getOutId(j))).getTargets()))
                        MistakeFactory.createMistake(Level.HARD, MISTAKES.SAME_TARGET.toString(), node);
//                        writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.SAME_TARGET.toString());
                    if (diagramElements.get(targetId).getType()==ElementType.DECISION)
                        MistakeFactory.createMistake(Level.HARD, MISTAKES.NEXT_DECISION.toString(), node);
//                        writeMistake(level.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.NEXT_DECISION.toString());
                }
            }
            // проверка на последовательность условных операторов
            String targetId = ((ControlFlow)diagramElements.get(decision.getOutId(decision.outSize()-1))).getTargets();
            if (diagramElements.get(targetId).getType()==ElementType.DECISION)
                MistakeFactory.createMistake(Level.HARD, MISTAKES.NEXT_DECISION.toString(), node);
//                writeMistake(level.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.NEXT_DECISION.toString());

        }

    }

//    private void writeMistake(String mistake){
//        VerificationResult.mistakes.add(mistake);
//    }
//
//    private void writeMistake(String level, String elType, String name, String mistake){
//        VerificationResult.mistakes.add(level+" "+ elType+ " \""+name+"\": "+mistake);
//    }

    /**
     * Ошибки, которые могут возникнуть на данном этапе
     */
    private enum MISTAKES {
        MORE_THAN_ONE_INIT,
        NO_FINAL,
        NO_INITIAL,
        NO_ACTIVITIES,
        MORE_THAN_ONE_OUT,
        DO_NOT_HAVE_ALT,
        ONLY_ONE_ALT,
        NO_OUT,
        NO_IN,
        NO_PARTION,
        REPEATED_ACT,
        SAME_TARGET,
        OUT_NOT_IN_ACT,
        NEXT_DECISION;

        @Override
        public String toString() {
            switch (this){
                case MORE_THAN_ONE_INIT:
                    return "В диаграмме больше одного начального состояния";
                case NO_FINAL:
                    return "В диаграмме отсутствует финальное состояние";
                case NO_INITIAL:
                    return "В диаграмме отсутствует начальное состояние";
                case NO_ACTIVITIES:
                    return "В диаграмме отсутствуют активности";
                case MORE_THAN_ONE_OUT:
                    return "больше одного выходящего перехода";
                case DO_NOT_HAVE_ALT:
                    return "не имеет альтернатив";
                case ONLY_ONE_ALT:
                    return "всего одна альтернатива";
                case NO_IN:
                    return "отсутствует входящий переход";
                case NO_OUT:
                    return "отсутствует выходящий переход";
                case NO_PARTION:
                    return "не принадлежит никакому участнику";
                case REPEATED_ACT:
                    return "имя не уникально";
                case SAME_TARGET:
                    return "альтернативы ведут в один и тот же элемент";
                case OUT_NOT_IN_ACT:
                    return "переход ведет не в активность или в разветвитель";
                case NEXT_DECISION:
                    return "альтернатива ведет в условный переход";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
