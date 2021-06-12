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

    public Card (String name, int value, URL image, Color color) {
        this.name = name;
        this.value = value;
        this.image = image;
        this.color = color;
    }

    public Card() {
    }

    // region <getter, setter, toString>
    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public URL getImage() {
        return image;
    }

    public Color getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setImage(URL image) {
        this.image = image;
    }

    public void setColor(Color color) {
        this.color = color;
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
