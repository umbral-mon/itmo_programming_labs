package lab8.collectionItems;

import lab8.collectionItems.utils.IdGenerator;

import java.time.LocalDate;
import java.util.Objects;

public class SpaceMarine implements Comparable<SpaceMarine> {
    private Integer id; // Значение поля должно быть уникальным
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;
    private Double health;
    private AstartesCategory category;
    private Weapon weaponType;
    private MeleeWeapon meleeWeapon;
    private Chapter chapter;
    private String owner;

    public void setOwner(String s){
        owner = s;
    }

    public SpaceMarine(Integer id, String name, Coordinates coordinates, LocalDate creationDate,
                       Double health, AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter, String owner) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name must not be null or empty string");
        if (coordinates == null)
            throw new IllegalArgumentException("coordinates mustn't be null");
        if (health == null || health <= 0)
            throw new IllegalArgumentException("health must not be null and be > 0");
        if (weaponType == null)
            throw new IllegalArgumentException("weapon mustn't be null");
        if (owner == null || owner.isEmpty())
            throw new IllegalArgumentException("owner must not be null or empty string");

        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate != null ? creationDate : LocalDate.now();
        this.health = health;
        this.category = category;
        this.weaponType = weaponType;
        this.meleeWeapon = meleeWeapon;
        this.chapter = chapter;
        this.owner = owner;
    }

    public SpaceMarine(String name, Coordinates coordinates,
                       Double health, AstartesCategory category, Weapon weaponType,
                       MeleeWeapon meleeWeapon, Chapter chapter, String owner){
        this(IdGenerator.getInstance().generateId(), name, coordinates, LocalDate.now(),
                health, category, weaponType, meleeWeapon, chapter, owner);
    }

    public SpaceMarine(SpaceMarine other) {
        this(other.id, other.name, other.coordinates, other.creationDate, other.health,
                other.category, other.weaponType, other.meleeWeapon, other.chapter, other.owner);
    }

    /**
     * Обновляет изменяемые поля объекта из другого SpaceMarine.
     */
    public void update(SpaceMarine other) {
        this.health = other.health;
        this.category = other.category;
        this.weaponType = other.weaponType;
        this.meleeWeapon = other.meleeWeapon;
        this.chapter = other.chapter;
        this.name = other.name;
        this.coordinates = other.coordinates;
        this.creationDate = other.creationDate;
    }

    @Override
    public int compareTo(SpaceMarine o) {
        return Integer.compare(this.id, o.id);
    }

    public Integer getID() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public LocalDate getCreationDate() { return creationDate; }
    public Double getHealth() { return health; }
    public AstartesCategory getCategory() { return category; }
    public Weapon getWeaponType() { return weaponType; }
    public MeleeWeapon getMeleeWeapon() { return meleeWeapon; }
    public Chapter getChapter() { return chapter; }
    public String getOwner() { return owner; }

    @Override
    public String toString() {
        return "SpaceMarine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", health=" + health +
                ", category=" + category +
                ", weaponType=" + weaponType +
                ", meleeWeapon=" + meleeWeapon +
                ", chapter=" + chapter +
                ", owner='" + owner + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceMarine that = (SpaceMarine) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}