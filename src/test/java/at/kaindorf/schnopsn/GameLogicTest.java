package at.kaindorf.schnopsn;


import at.kaindorf.schnopsn.beans.Card;
import at.kaindorf.schnopsn.beans.Color;
import at.kaindorf.schnopsn.bl.GameLogic;
import at.kaindorf.schnopsn.bl.GameLogicTestClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {
    GameLogicTestClass gltc = new GameLogicTestClass();

    /*@DisplayName("GameLogic Test")
    @Test
    public void test(){
        int a = 6, b = 2;
        assertEquals(2, logic.test(a,b));
    }*/
    @DisplayName("makeCall2erSchnopsn Test")
    @Test
    public void makeCall2erSchnopsnTest() {
        List<Card> handcards = new ArrayList<>();
        try {
            handcards.add(new Card("Dame", 3, new URL("http://link"), Color.KREUZ, true));
            handcards.add(new Card("König", 4, new URL("http://link"), Color.KREUZ, true));
            handcards.add(new Card("Dame", 3, new URL("http://link"), Color.PICK, true));
            handcards.add(new Card("König", 4, new URL("http://link"), Color.PICK, true));
            handcards.add(new Card("Zehner", 10, new URL("http://link"), Color.HERZ, true));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Color trump = Color.HERZ;

        assertEquals(20, gltc.makeCall2erSchnopsn(handcards, trump));
    }
}
