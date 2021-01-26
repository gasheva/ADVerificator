package xmiparser;

import Model.ADNodesList;
import debugging.Debug;
import entities.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;


public class XmiParser {
    private File xmlFile = null;
    private Element root = null;
    private ADNodesList adNodesList;

    public XmiParser(ADNodesList adNodesList) {
        this.adNodesList = adNodesList;
    }

    public void setXmlFile(String path) {
        this.xmlFile = new File(path);
    }

    public void Parse() throws ParserConfigurationException, SAXException, IOException {
        if (!xmlFile.exists()){
            System.out.println("[x] File is not exist");
            return; //null
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);

        findPackagedElement(document.getDocumentElement(), "packagedElement");
        traverse(root);
    }

    /**
    * Рекурсивный корневого узла
    */
    private void findPackagedElement(Node node, String searchingElement){
        if (node.getNodeName().equals(searchingElement)) {
            root = (Element) node;
        }
        else {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node currentNode = list.item(i);
                findPackagedElement(currentNode, searchingElement);
            }
        }
    }
    /**
     * Рекурсивный поиск подписи перехода
     */
    private void findLiteralString(Node node, String searchingElement){
        if (node.getNodeName().equals(searchingElement)) {
            literalString = (Element) node;
        }
        else {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node currentNode = list.item(i);
                findLiteralString(currentNode, searchingElement);
            }
        }
    }

    private Element literalString;
    private void traverse(Element packagedElement) {
        NodeList list = packagedElement.getChildNodes();
        // проходим по всем дочерним элементам и создаем объекты на их основе
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i).getNodeType()== Node.ELEMENT_NODE) {
                Element currentElement = (Element) list.item(i);
                if (currentElement.getNodeName().equals("ownedNode")){
                    DiagramElement nodeFromXMI = null;
                    switch (currentElement.getAttribute("xsi:type")){
                        // активность
                        case "uml:OpaqueAction":
                            nodeFromXMI = new ActivityNode(currentElement.getAttribute("xmi:id"),
                                    currentElement.getAttribute("inPartition"), currentElement.getAttribute("name"));
                            nodeFromXMI.setType(ElementType.ACTIVITY);
                            adNodesList.addLast(nodeFromXMI);
                            Debug.println(((ActivityNode)adNodesList.get(adNodesList.size()-1)).getInPartition());
                            break;
                        // узел инициализации
                        case "uml:InitialNode":
                            nodeFromXMI = new InitialNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"));
                            nodeFromXMI.setType(ElementType.INITIAL_NODE);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                        // конечное состояние
                        case "uml:ActivityFinalNode":
                            nodeFromXMI = new FinalNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"));
                            nodeFromXMI.setType(ElementType.FINAL_NODE);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                        // условный переход
                        case "uml:DecisionNode":
                            nodeFromXMI = new DecisionNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"), currentElement.getAttribute("question"));
                            nodeFromXMI.setType(ElementType.DECISION);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                        // узел слияния
                        case "uml:MergeNode":
                            nodeFromXMI = new MergeNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"));
                            nodeFromXMI.setType(ElementType.MERGE);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                        // разветвитель
                        case "uml:ForkNode":
                            nodeFromXMI = new ForkNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"));
                            nodeFromXMI.setType(ElementType.FORK);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                        // синхронизатор
                        case "uml:JoinNode":
                            nodeFromXMI = new JoinNode(currentElement.getAttribute("xmi:id"), currentElement.getAttribute("inPartition"));
                            nodeFromXMI.setType(ElementType.JOIN);
                            adNodesList.addLast(nodeFromXMI);
                            break;
                    }
                    // добавляем ид входящих и выходящих переходов
                    if (nodeFromXMI!=null) {
                        String idsIn = currentElement.getAttribute("incoming");
                        String idsOut = currentElement.getAttribute("outgoing");
                        nodeFromXMI.addIn(idsIn);
                        nodeFromXMI.addOut(idsOut);
                        int y=0;
                    }

                }
                // создаем переход
                else if (currentElement.getNodeName().equals("edge")){
                    if (currentElement.getAttribute("xsi:type").equals("uml:ControlFlow")){
                        // находим подпись перехода
                        findLiteralString(currentElement, "guard");
                        String mark = (literalString).getAttribute("value").trim();        // если подпись является "yes", значит это подпись по умолчанию
                        ControlFlow temp = new ControlFlow(currentElement.getAttribute("xmi:id"), mark.equals("true")?"":mark);
                        temp.setType(ElementType.FLOW);
                        temp.setSrc(currentElement.getAttribute("source"));
                        temp.setTargets(currentElement.getAttribute("target"));
                        adNodesList.addLast(temp);
                    }
                }
                // создаем дорожку
                else if (currentElement.getNodeName().equals("ownedGroup")){
                    if (currentElement.getAttribute("xsi:type").equals("uml:ActivityPartition")){
                        Swimlane temp = new Swimlane(currentElement.getAttribute("xmi:id"), ((Element)currentElement).getAttribute("name"));
                        temp.setType(ElementType.SWIMLANE);
                        adNodesList.addLast(temp);
                    }
                }

            }
        }
    }

}
