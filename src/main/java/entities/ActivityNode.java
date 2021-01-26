package entities;

/**
 * Представляет собой узел активности в диаграмме UML
 * тэг <ownedNode>, тип xsi:type="uml:OpaqueAction"
 */
public class ActivityNode extends DiagramElement{
    private String name;    // содержит отображаемый на элементе текст

    public ActivityNode(String id, String inPartition, String name) {
        super(id, inPartition, name);
        this.name = name;
    }
    //region Getter-Setter
    /**
     * name содержит отображаемый на элементе текст
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    //endregion


}
