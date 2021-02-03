import MainForm.MainController;
import Model.ADNodesList;
import Model.Model;
import debugging.Debug;
import result.ElementMistake;
import result.Mistakes;
import result.VerificationResult;
import verification.Level;
import verification.lexical.LexicalAnalizator;
import verification.syntax.SyntaxAnalizator;
import xmiparser.PetriNet;
import xmiparser.XmiParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class App {
    public static void main(String[] args) {
//         MainController controller = new MainController(new Model());

        ADNodesList adNodesList = new ADNodesList();
        XmiParser xmiParser = new XmiParser(adNodesList);
        xmiParser.setXmlFile("C:\\Users\\DocGashe\\Documents\\Лекции\\ДиПломная\\Тестирование\\С координатами\\Бесконечный разветвитель.xmi");
        try {
            xmiParser.Parse();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        Debug.println("----------------------");
        for (int i = 0; i < adNodesList.size(); i++) {
            Debug.println(adNodesList.get(i).getId() + " "+ adNodesList.get(i).getType());
        }
        Debug.println("----------------------");
        adNodesList.connect();
        adNodesList.print();

        LexicalAnalizator lexicalAnalizator = new LexicalAnalizator(Level.EASY);
        lexicalAnalizator.setDiagramElements(adNodesList);
        lexicalAnalizator.check();

        SyntaxAnalizator syntaxAnalizator = new SyntaxAnalizator(Level.EASY);
        syntaxAnalizator.setDiagramElements(adNodesList);
        syntaxAnalizator.check();


//        VerificationResult.mistakes.forEach(Debug::println);
//        VerificationResult.writeInFile("output.txt");


        if (!Mistakes.mistakes.stream().anyMatch(x->x.getLevel()==Level.FATAL)) {
            PetriNet petriNet = new PetriNet();
            petriNet.petriCheck(adNodesList);
        }

        Debug.println("---Рубрика \"Ошибки\"---");
//        VerificationResult.mistakes.forEach(System.out::println);
        Mistakes.mistakes.forEach(x->{
            if (x instanceof ElementMistake)
                Debug.print(((ElementMistake)x).getDescr()+" "+((ElementMistake)x).getType());
            Debug.print(x.getLevel()+" "+x.getMistake());
            Debug.println("");
        });
    }
}
