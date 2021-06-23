package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.Card;
import at.kaindorf.schnopsn.beans.Color;
import at.kaindorf.schnopsn.beans.Game;
import at.kaindorf.schnopsn.beans.Player;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GameLogicTestClass {
    //When someone wants to call a 20er or 40er
    public String makeCall2erSchnopsn(List<Card> handCards, Color currentTrump) {
       // List<Card> handCards = game.getPlayerCardMap().get(player);
        //Becomes true if the first card is found
        boolean foundFirstCard = false;
        //To store the first card if it is found
        Card tempCard = null;

        //alle Colors der Handkarten hinzufügen
        Set<Color> colorSet = new TreeSet<>();
        for (Card card : handCards) {
            card.setPriority(false);
            colorSet.add(card.getColor());
        }
        for (Color color : colorSet) {
            for (Card card : handCards) {
                //adde erste Karte der Farbe
                if ((card.getName().equals("Dame") || card.getName().equals("König")) && card.getColor() == color && !foundFirstCard) {
                    foundFirstCard = true;
                    tempCard = card;
                    //Wenn erste Karte gefunden und diese die gleiche farbe hat
                } else if (foundFirstCard && card.getColor() == color) {
                    switch (tempCard.getName()) {
                        case "Dame":
                            if (card.getName().equals("König")) {
                                tempCard.setPriority(true);
                                card.setPriority(true);
                                if (currentTrump == color) {
                                    return "40er";
                                }
                                return "20er";
                            }
                            break;
                        case "König":
                            if (card.getName().equals("Dame")) {
                                tempCard.setPriority(true);
                                card.setPriority(true);
                                if (currentTrump == color) {
                                    return "40er";
                                }
                                return "20er";
                            }
                            break;
                    }
                }
            }
            foundFirstCard = false;
        }

        for (Card card : handCards) {
            card.setPriority(true);
        }
        return "";
    }
}
