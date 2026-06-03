package lab8.collectionItems;

public class Chapter implements Comparable<Chapter>{
    private String name; //Поле не может быть null, Строка не может быть пустой
    private String parentLegion;
    private Integer marinesCount; //Поле может быть null, Значение поля должно быть больше 0, Максимальное значение поля: 1000

    public Chapter(String name, String parentLegion, Integer marinesCount){
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name mustn't be empty");
        if (marinesCount < 0 || marinesCount > 1000)
            throw new IllegalArgumentException("marines count must be in range [0..1000] or null");

        this.name = name;
        this.parentLegion = parentLegion;
        this.marinesCount = marinesCount;
    }

    /**
     * comparing by name with default String comparison
     * @param o other chapter
     * @return 0 if equal. <0 if this less than other. >0 if this larger that other
     */
    @Override
    public int compareTo(Chapter o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "name='" + name + '\'' +
                ", parentLegion='" + parentLegion + '\'' +
                ", marinesCount=" + marinesCount +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getParentLegion() {
        return parentLegion;
    }

    public Integer getMarinesCount() {
        return marinesCount;
    }
}