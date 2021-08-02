import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CardsManagerTest {

    @BeforeEach
    void setUp() {
        try {
            Settings.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void insertNewCard() {
        final CardsManager mgr = new CardsManager();
        mgr.insertNewCard("What's the first English letter?", "A");
        mgr.insertNewCard("Keep your friends close and...", "keep your enemies closer.");
        mgr.insertNewCard("My name is...", "Ido.");
        mgr.queue.refreshQueue();
        CardsManager.Card current;
        int count = 0;
        while ((current = mgr.queue.getNextCard()) != null) {
            switch (current.getQuestion()) {
                case "What's the first English letter?" -> {
                    assertEquals("A", current.getAnswer());
                    ++count;
                }
                case "Keep your friends close and..." -> {
                    assertEquals("keep your enemies closer.", current.getAnswer());
                    ++count;
                }
                case "My name is..." -> {
                    assertEquals("Ido.", current.getAnswer());
                    ++count;
                }
            }
        }
        assertTrue(count >= 3 && count % 3 == 0);
        try {
            mgr.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getNextCard() {
        final CardsManager mgr = new CardsManager();
        CardsManager.Card current = null, prev = null;
        while ((current = mgr.queue.getNextCard()) != null) {
            if (prev != null) {
                assertTrue(prev.compareTo(current) <= 0);
            }
            prev = current;
        }
        try {
            mgr.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateCard() {
        final CardsManager mgr = new CardsManager();
        CardsManager.Card current;
        int i = 0;
        while ((current = mgr.queue.getNextCard()) != null) {
            ++i;
            current.updateCard(String.format("Question %d.", i), String.format("Answer %d.", i));
        }
        mgr.queue.refreshQueue();
        i = 0;
        while ((current = mgr.queue.getNextCard()) != null) {
            ++i;
            assertEquals(String.format("Question %d.", i), current.getQuestion());
            assertEquals(String.format("Answer %d.", i), current.getAnswer());
        }
        try {
            mgr.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void cardInteractions() {
        final CardsManager mgr = new CardsManager();
        CardsManager.Card current;
        double successRate;
        Random rnd = new Random();

        int i = 0;
        while ((current = mgr.queue.getNextCard()) != null) {
            i = 1 + rnd.nextInt(10);
            switch (i % 3) {
                case 0: {
                    successRate = current.getSuccessRate();
                    for (int j = 0; j < i; ++j) {
                        current.interact(1);
                    }
                    assertTrue(Double.isNaN(successRate) || successRate <= current.getSuccessRate());
                }
                case 1: {
                    successRate = current.getSuccessRate();
                    for (int j = 0; j < i; ++j) {
                        current.interact(0.5);
                    }
                    assertTrue(Double.isNaN(successRate) ||
                            Math.abs(successRate - 0.5) >= Math.abs(current.getSuccessRate() - 0.5));
                }
                case 2: {
                    successRate = current.getSuccessRate();
                    for (int j = 0; j < i; ++j) {
                        current.interact(0);
                    }
                    assertTrue(Double.isNaN(successRate) ||successRate >= current.getSuccessRate());
                }
            }
        }
    }
}