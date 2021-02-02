package verification.lexical;

import Model.ADNodesList;
import debugging.Debug;
import entities.*;
import result.*;
import verification.Level;

import java.util.Arrays;

/**
 * Этап лексического анализа
 */
public class LexicalAnalizator {
    private Level level;
    private ADNodesList diagramElements;

    public LexicalAnalizator(Level level) {
        this.level = level;
    }

    //region Getter-Setter
    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
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
            switch (diagramElements.get(i).getType()) {
                case FLOW:
                    checkFlow((ControlFlow)diagramElements.get(i));
                    break;
                case FORK:
                    break;
                case JOIN:
                    break;
                case MERGE:
                    break;
                case ACTIVITY:
                    checkActivity((ActivityNode)diagramElements.get(i), diagramElements.getNode(i));
                    break;
                case DECISION:
                    checkDecision((DecisionNode)diagramElements.get(i), diagramElements.getNode(i));
                    break;
                case SWIMLANE:
                    checkSwimlane((Swimlane)diagramElements.get(i));
                    break;
                case STRANGE:
                    break;
            }
        }
    }

    private void checkFlow(ControlFlow flow) {
        boolean notCondButHaveMark = false;
        boolean isCond = false;
        // если это не условие, проверяем подпись
        if (diagramElements.get(flow.getTargets()).getType()!=ElementType.DECISION) {
            if (!flow.getText().equals("")) {
                notCondButHaveMark = true;
            }
        }else isCond = true;

        if (diagramElements.get(flow.getSrc()).getType()!=ElementType.DECISION) {
            if (!flow.getText().equals("")) {
                if(!isCond) MistakeFactory.createMistake(Level.HARD, MISTAKES.HAVE_MARK.toString()+" - \"" + flow.getText() + "\"", flow);
//                if(!isCond) writeMistake(Level.HARD.toString(), flow.getType().toString(), "", MISTAKES.HAVE_MARK.toString() + " - \"" + flow.getText() + "\"");
            }
            else if (notCondButHaveMark) MistakeFactory.createMistake(Level.HARD, MISTAKES.HAVE_MARK.toString() + " - \"" + flow.getText() + "\"", flow);//writeMistake(Level.HARD.toString(), flow.getType().toString(), "", MISTAKES.HAVE_MARK.toString() + " - \"" + flow.getText() + "\"");
        }
    }

    private void checkSwimlane(Swimlane swimlane) {
        // проверка на заглавную букву
        if ((!swimlane.getName().substring(0, 1).toUpperCase().equals(swimlane.getName().substring(0, 1)))){
            MistakeFactory.createMistake(Level.HARD, MISTAKES.SMALL_LETTER.toString(), swimlane);
//            writeMistake(Level.HARD.toString(), swimlane.getType().toString(), swimlane.getName(), MISTAKES.SMALL_LETTER.toString());
        }
    }
    private void checkActivity(ActivityNode activity, ADNodesList.ADNode node) {
        // проверка на заглавную букву
        if ((!activity.getName().substring(0, 1).toUpperCase().equals(activity.getName().substring(0, 1)))){
            MistakeFactory.createMistake(Level.HARD, MISTAKES.SMALL_LETTER.toString(), node);
//            writeMistake(Level.HARD.toString(), activity.getType().toString(), activity.getName(), MISTAKES.SMALL_LETTER.toString());
        }
        // получаем первое слово существительного и проверяем, что оно не заканчивается на ь или т
        String firstWord = activity.getName().split(" ")[0];
        Debug.println(firstWord);

        if (firstWord.endsWith("ь")&&!firstWord.endsWith("ль")||firstWord.endsWith("т"))
            MistakeFactory.createMistake(Level.EASY, MISTAKES.NOT_NOUN.toString(), node);
            //writeMistake(Level.EASY.toString(), activity.getType().toString(), activity.getName(), MISTAKES.NOT_NOUN.toString());
    }
    private void checkDecision(DecisionNode decision, ADNodesList.ADNode node){
        // добавляем вопрос для перехода
        BaseNode flowIn = diagramElements.get(decision.getInId(0));
        String quest = ((ControlFlow)flowIn).getText();
        decision.setQuestion(quest.trim());

        // добавляем альтернативы -> проходим по всем выходящим переходам и получаем подписи
        for (int i = 0; i < decision.outSize(); i++) {
            BaseNode flow = diagramElements.get(decision.getOutId(i));
            decision.addAlternative(((ControlFlow)flow).getText());
        }

        // проверяем подписи альтернатив, если их больше одной
        boolean checkAlt = decision.alternativeSize()>=2;

        // поиск совпадающих названий
        if (checkAlt)
        decision.findEqualAlternatives().forEach(x->MistakeFactory.createMistake(Level.HARD, MISTAKES.REPEATED_ALT.toString()+" - "+x, node));// writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.REPEATED_ALT.toString()+" - "+x)

        // проверка на альтернативу без подписи
        if(checkAlt)
            if(decision.findEmptyAlternative())
                MistakeFactory.createMistake(Level.HARD, MISTAKES.HAVE_EMPTY_ALT.toString(), node);
//                writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.HAVE_EMPTY_ALT.toString());

        // проверка, что альтернативы начинаются с заглавных букв
        if(checkAlt)
            for (int i = 0; i < decision.alternativeSize(); i++) {
                String alter = decision.getAlternative(i);
                if(!alter.equals(""))
                    if (!alter.substring(0, 1).toUpperCase().equals(alter.substring(0, 1)))
                        MistakeFactory.createMistake(level, " альтернатива \""+alter+"\""+MISTAKES.SMALL_LETTER.toString(), node);
//                        writeMistake(level.toString(), decision.getType().toString(), decision.getQuestion()+" альтернатива \""+alter+"\"", MISTAKES.SMALL_LETTER.toString());
            }


        boolean checkQuest = true;
        // проверка, что имеется условие
        if (decision.getQuestion().equals("")) {
            MistakeFactory.createMistake(Level.HARD, decision.getQuestion()+" "+MISTAKES.HAVE_NOT_QUEST.toString(), node);
//            writeMistake(Level.HARD.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.HAVE_NOT_QUEST.toString());
            checkQuest=false; // дальнейшие проверки условия не требуются (его нет)
        }

        // проверка на заглавную букву
        if (checkQuest)
        if ((!decision.getQuestion().substring(0, 1).toUpperCase().equals(decision.getQuestion().substring(0, 1)))){
            MistakeFactory.createMistake(level, decision.getQuestion()+" "+MISTAKES.SMALL_LETTER.toString(), node);
//            writeMistake(level.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.SMALL_LETTER.toString());
        }
        // заканчивается на знак вопроса
        if (checkQuest)
        if ((!decision.getQuestion().endsWith("?")))
            MistakeFactory.createMistake(level, decision.getQuestion()+" "+MISTAKES.END_WITH_QUEST.toString(), node);
//            writeMistake(level.toString(), decision.getType().toString(), decision.getQuestion(), MISTAKES.END_WITH_QUEST.toString());
    }

//    private void writeMistake(String level, String elType, String name, String mistake){
//        VerificationResult.mistakes.add(level+" "+ elType+ " \""+name+"\": "+mistake);
//    }

    /**
     * Ошибки, которые могут возникнуть на данном этапе
     */
    private enum MISTAKES {
        SMALL_LETTER,
        NOT_NOUN,
        FLOW_HAVE_MARK,
        HAVE_NOT_OUT_PARTION,
        END_WITH_QUEST,
        HAVE_NOT_QUEST,
        REPEATED_ALT,
        HAVE_EMPTY_ALT,
        HAVE_MARK;

        @Override
        public String toString() {
            switch (this) {
                case SMALL_LETTER:
                    return "имя начинается с маленькой буквы";
                case NOT_NOUN:
                    return "первое слово возможно не является именем существительным";
                case FLOW_HAVE_MARK:
                    return "переход имеет подпись, но не является альтернативой условного перехода";
                case HAVE_NOT_OUT_PARTION:
                    return "элемент не принадлежит никакому участнику";
                case END_WITH_QUEST:
                    return "нет знака вопроса";
                case HAVE_NOT_QUEST:
                    return "отсутствует условие";
                case REPEATED_ALT:
                    return "повторяется альтернатива";
                case HAVE_EMPTY_ALT:
                    return "не подписанная альтернатива";
                case HAVE_MARK:
                    return "имеет подпись, не являясь условием или альтернативой";
                default:
                    throw new IllegalArgumentException();
            }

        }
    }

}