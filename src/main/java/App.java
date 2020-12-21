import Model.ADNodesList;
import debugging.Debug;
import result.VerificationResult;
import verification.Level;
import verification.lexical.LexicalAnalizator;
import verification.syntax.SyntaxAnalizator;
import xmiparser.XmiParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        ADNodesList adNodesList = new ADNodesList();
        XmiParser xmiParser = new XmiParser(adNodesList);
        xmiParser.setXmlFile("C:\\Users\\DocGashe\\Documents\\Лекции\\ДиПломная\\Тестирование\\Условные операторы подряд.xmi");
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

        LexicalAnalizator lexicalAnalizator = new LexicalAnalizator(Level.EASY);
        lexicalAnalizator.setDiagramElements(adNodesList);
        lexicalAnalizator.check();

        SyntaxAnalizator syntaxAnalizator = new SyntaxAnalizator(Level.EASY);
        syntaxAnalizator.setDiagramElements(adNodesList);
        syntaxAnalizator.check();


        VerificationResult.mistakes.forEach(Debug::println);
        VerificationResult.writeInFile("output.txt");
    }
}
