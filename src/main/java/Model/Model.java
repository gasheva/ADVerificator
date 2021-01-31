package Model;

import debugging.Debug;
import org.xml.sax.SAXException;
import result.VerificationResult;
import verification.Level;
import verification.lexical.LexicalAnalizator;
import verification.syntax.SyntaxAnalizator;
import xmiparser.PetriNet;
import xmiparser.XmiParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Model {
    private ADNodesList adNodesList;

    /**
     * Метод, осуществляющий верификацию
     * @param path
     * @return
     */
    public int startVerification(String path){

        LexicalAnalizator lexicalAnalizator = new LexicalAnalizator(Level.EASY);
        lexicalAnalizator.setDiagramElements(adNodesList);
        lexicalAnalizator.check();

        SyntaxAnalizator syntaxAnalizator = new SyntaxAnalizator(Level.EASY);
        syntaxAnalizator.setDiagramElements(adNodesList);
        syntaxAnalizator.check();


        VerificationResult.mistakes.forEach(Debug::println);
//        VerificationResult.writeInFile("output.txt");


//        PetriNet petriNet = new PetriNet();
//        petriNet.petriCheck(adNodesList);
//
//        Debug.println("---Рубрика \"Ошибки\"---");
//        VerificationResult.mistakes.forEach(System.out::println);
        return 1;
    }

    public ADNodesList getAdNodesList() {
        return adNodesList;
    }

    public boolean writeFile(String path){
        VerificationResult.mistakes.forEach(Debug::println);
        VerificationResult.writeInFile("output.txt");   // TODO: path
        return true;
    }
    public boolean readFile(String path){
        adNodesList = new ADNodesList();
        XmiParser xmiParser = new XmiParser(adNodesList);
        xmiParser.setXmlFile(path);   // TODO: path
        try {
            xmiParser.Parse();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return false;
        }
//        Debug.println("----------------------");
//        for (int i = 0; i < adNodesList.size(); i++) {
//            Debug.println(adNodesList.get(i).getId() + " "+ adNodesList.get(i).getType());
//        }
//        Debug.println("----------------------");
        adNodesList.connect();
        adNodesList.print();
        return true;
    }
}
