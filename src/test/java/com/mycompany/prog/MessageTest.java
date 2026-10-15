package com.mycompany.prog;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    // Test data from the brief
    private static final String ID1 = "0012345678";
    private static final String RECIPIENT1 = "+27718693002";
    private static final String TEXT1 = "Hi Mike, can you join us for dinner tonight?";

    private static final String ID2 = "0187654321";
    private static final String RECIPIENT2 = "08575975889";
    private static final String TEXT2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    void setUp() {
        Message.resetAll();
        Message.storageFile = "target/test-messages.json";
    }

    // ---- Message length ----

    @Test
    void messageLengthSuccess() {
        Message m = new Message(ID1, 0, RECIPIENT1, TEXT1);
        assertEquals("Message ready to send.", m.checkMessageLength());
    }

    @Test
    void messageLengthFailure() {
        String tooLong = "a".repeat(260);
        Message m = new Message(ID1, 0, RECIPIENT1, tooLong);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                m.checkMessageLength());
    }

    // ---- Recipient number ----

    @Test
    void recipientSuccess() {
        Message m = new Message(ID1, 0, RECIPIENT1, TEXT1);
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    void recipientFailure() {
        Message m = new Message(ID2, 1, RECIPIENT2, TEXT2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                m.checkRecipientCell());
    }

    // ---- Message hash ----

    @Test
    void messageHashTestCase1() {
        Message m = new Message(ID1, 0, RECIPIENT1, TEXT1);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    void messageHashRemainingMessagesInLoop() {
        String[] ids = {ID1, ID2};
        String[] recipients = {RECIPIENT1, RECIPIENT2};
        String[] texts = {TEXT1, TEXT2};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], i, recipients[i], texts[i]);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    // ---- Message ID ----

    @Test
    void messageIDIsCreated() {
        Message m = new Message(1, RECIPIENT1, TEXT1);
        assertTrue(m.checkMessageID());
        assertEquals(10, m.getMessageID().length());
        assertEquals("Message ID generated: " + m.getMessageID(), m.idGeneratedMessage());
    }

    // ---- Send / disregard / store ----

    @Test
    void userSelectsSend() {
        Message m = new Message(ID1, 0, RECIPIENT1, TEXT1);
        assertEquals("Message successfully sent.", m.sentMessage(1));
    }

    @Test
    void userSelectsDisregard() {
        Message m = new Message(ID2, 1, RECIPIENT2, TEXT2);
        assertEquals("Press 0 to delete the message.", m.sentMessage(2));
    }

    @Test
    void userSelectsStore() {
        Message m = new Message(ID1, 0, RECIPIENT1, TEXT1);
        assertEquals("Message successfully stored.", m.sentMessage(3));
    }

    // ---- Totals ----

    @Test
    void totalMessagesSent() {
        Message m1 = new Message(ID1, 0, RECIPIENT1, TEXT1);
        Message m2 = new Message(ID2, 1, RECIPIENT2, TEXT2);
        m1.sentMessage(1);   // Send
        m2.sentMessage(2);   // Discard
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void printMessagesContainsSentMessage() {
        Message m1 = new Message(ID1, 0, RECIPIENT1, TEXT1);
        m1.sentMessage(1);
        String printed = Message.printMessages();
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertTrue(printed.contains(TEXT1));
    }
}