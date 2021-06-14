package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;
import java.util.Objects;

public class Card {
    private String name;
    private int value;
    private URL image;
    private Color color;
    private boolean priority;

    public Card(String name, int value, URL image, Color color, boolean priority) {
        this.name = name;
        this.value = value;
        this.image = image;
        this.color = color;
        this.priority = priority;
    }

    public Card() {
    }

    // region <getter, setter, toString>


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public URL getImage() {
        return image;
    }

    public void setImage(URL image) {
        this.image = image;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value &&
                name.equals(card.name) &&
                color == card.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, color);
    }

    @Override
    public String toString() {
        return "Card: {" +
                "'name'='" + name + '\'' +
                ", 'value'=" + value +
                ", image=" + image +
                ", color=" + color +
                '}';
    }
    //endregion
}
