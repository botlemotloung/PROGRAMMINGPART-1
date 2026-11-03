package com.mycompany.prog;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageArraysTest {

    // Test data from the brief
    private static final String T1 = "Did you get the cake?";
    private static final String T2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String T3 = "Yohoooo, I am at your gate.";
    private static final String T4 = "It is dinner time!";
    private static final String T5 = "Ok, I am leaving without you.";

    private Message m1;
    private Message m2;
    private Message m3;
    private Message m4;
    private Message m5;

    @BeforeEach
    void setUp() throws IOException {
        Message.resetAll();
        Message.storageFile = "target/test-part3-messages.json";
        Files.deleteIfExists(Paths.get(Message.storageFile));
        Message.setDefaultSender("+27718693002");

        m1 = new Message("1234567890", 0, "+27834557896", T1);
        m2 = new Message("2345678901", 1, "+27838884567", T2);
        m3 = new Message("3456789012", 2, "+27834484567", T3);
        m4 = new Message("0838884567", 3, "0838884567", T4);   // developer entry
        m5 = new Message("5678901234", 4, "+27838884567", T5);

        // Populate the arrays through the same methods the app uses
        m1.sentMessage(1);   // Sent
        m2.sentMessage(3);   // Stored
        m3.sentMessage(2);   // Disregard
        m4.sentMessage(1);   // Sent
        m5.sentMessage(3);   // Stored
    }

    private static List<String> texts(List<Message> messages) {
        List<String> result = new ArrayList<>();
        for (Message m : messages) {
            result.add(m.getMessageText());
        }
        return result;
    }

    // ---- Arrays correctly populated ----

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(T1, T4), texts(Message.getSentMessages()));
    }

    @Test
    void disregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(T3), texts(Message.getDisregardedMessages()));
    }

    @Test
    void storedMessagesArrayReadFromJsonFile() {
        Message.resetAll();                       // empty the arrays in memory
        int loaded = Message.loadStoredMessages(); // read the JSON file back in
        assertEquals(2, loaded);
        assertEquals(List.of(T2, T5), texts(Message.getStoredMessages()));
    }

    @Test
    void messageIDAndMessageHashArraysCorrectlyPopulated() {
        assertEquals(List.of("1234567890", "2345678901", "3456789012", "0838884567", "5678901234"),
                Message.getMessageIDs());
        assertEquals(List.of("12:0:DIDCAKE", "23:1:WHERETIME", "34:2:YOHOOOOGATE", "08:3:ITTIME", "56:4:OKYOU"),
                Message.getMessageHashes());
    }

    // ---- Stored Messages menu features ----

    @Test
    void displayLongestMessage() {
        assertEquals(T2, Message.longestMessage(List.of(m1, m2, m3, m4)));
        assertEquals(T2, Message.longestStoredMessage());
    }

    @Test
    void searchForMessageID() {
        assertEquals("It is dinner time!", Message.findMessageByID("0838884567").getMessageText());
        assertEquals("Recipient: 0838884567\nMessage: It is dinner time!",
                Message.searchByMessageID("0838884567"));
    }

    @Test
    void searchMessagesForParticularRecipient() {
        assertEquals(List.of(T2, T5), texts(Message.findMessagesByRecipient("+27838884567")));
        assertEquals("Message: " + T2 + "\nMessage: " + T5,
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    void deleteMessageUsingMessageHash() {
        assertEquals("Message: \"" + T2 + "\" successfully deleted.",
                Message.deleteMessageByHash(m2.getMessageHash()));
        assertEquals(List.of(T5), texts(Message.getStoredMessages()));

        // The JSON file was updated too
        Message.resetAll();
        assertEquals(1, Message.loadStoredMessages());
    }

    @Test
    void displaySenderAndRecipientOfStoredMessages() {
        assertEquals("Sender: +27718693002 | Recipient: +27838884567\n"
                + "Sender: +27718693002 | Recipient: +27838884567",
                Message.displaySenderAndRecipient());
    }

    @Test
    void displayReportOfSentMessages() {
        String expected = "Message Hash: 12:0:DIDCAKE\n"
                + "Recipient: +27834557896\n"
                + "Message: Did you get the cake?\n\n"
                + "Message Hash: 08:3:ITTIME\n"
                + "Recipient: 0838884567\n"
                + "Message: It is dinner time!";
        assertEquals(expected, Message.sentMessagesReport());
    }

    @Test
    void displayReportOfStoredMessages() {
        String expected = "Message ID: 2345678901\n"
                + "Message Hash: 23:1:WHERETIME\n"
                + "Sender: +27718693002\n"
                + "Recipient: +27838884567\n"
                + "Message: " + T2 + "\n"
                + "Flag: Stored\n\n"
                + "Message ID: 5678901234\n"
                + "Message Hash: 56:4:OKYOU\n"
                + "Sender: +27718693002\n"
                + "Recipient: +27838884567\n"
                + "Message: " + T5 + "\n"
                + "Flag: Stored";
        assertEquals(expected, Message.storedMessagesReport());
    }
}