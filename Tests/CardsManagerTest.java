import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CardsManagerTest {

    @Test
    void insertNewCard() {
        final CardsManager queue = new CardsManager();
        queue.insertNewCard("What's the first English letter?", "A");
        queue.insertNewCard("Keep your friends close and...", "keep your enemies closer.");
        queue.insertNewCard("My name is...", "Ido.");
        queue.refreshQueue();
        CardsManager.Card current;
        int count = 0;
        while ((current = queue.getNextCard()) != null) {
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
            queue.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getNextCard() {
        final CardsManager queue = new CardsManager();
        CardsManager.Card current = null, prev = null;
        while ((current = queue.getNextCard()) != null) {
            if (prev != null) {
                assertTrue(prev.compareTo(current) <= 0);
            }
            prev = current;
        }
        try {
            queue.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateCard() {
        final CardsManager queue = new CardsManager();
        CardsManager.Card current;
        int i = 0;
        while ((current = queue.getNextCard()) != null) {
            ++i;
            current.updateCard(String.format("Question %d.", i), String.format("Answer %d.", i));
        }
        queue.refreshQueue();
        i = 0;
        while ((current = queue.getNextCard()) != null) {
            ++i;
            assertEquals(String.format("Question %d.", i), current.getQuestion());
            assertEquals(String.format("Answer %d.", i), current.getAnswer());
        }
        try {
            queue.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void cardInteractions() {
        final CardsManager queue = new CardsManager();
        CardsManager.Card current;
        double successRate;

        int i = 0;
        while ((current = queue.getNextCard()) != null) {
            ++i;
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