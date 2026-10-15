package com.mycompany.prog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Message {

    public static final int MAX_LENGTH = 250;

    // JSON file messages are stored in (tests point this somewhere else)
    public static String storageFile = "messages.json";

    // Messages sent / stored while the program is running
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();

    private static final Random RANDOM = new Random();

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Normal constructor: generates a random ten-digit message ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Constructor that accepts a fixed ID (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // ---------- ID ----------

    /** Randomly generated ten-digit number (leading zeros allowed, e.g. 0012345678). */
    public static String generateMessageID() {
        long number = (long) (RANDOM.nextDouble() * 10_000_000_000L);
        return String.format("%010d", number);
    }

    /** Ensures the message ID is not more than ten characters (and is numeric). */
    public boolean checkMessageID() {
        return messageID != null
                && messageID.length() <= 10
                && messageID.matches("\\d+");
    }

    public String idGeneratedMessage() {
        return "Message ID generated: " + messageID;
    }

    // ---------- Recipient ----------

    /**
     * Recipient must start with an international code (+) followed by at most
     * ten digits of number (code is 1-3 digits), e.g. +27718693002.
     */
    public String checkRecipientCell() {
        if (recipient != null && Pattern.matches("^\\+\\d{1,3}\\d{1,10}$", recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    // ---------- Message length ----------

    public String checkMessageLength() {
        if (messageText != null && messageText.length() <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        int over = (messageText == null ? 0 : messageText.length()) - MAX_LENGTH;
        return "Message exceeds 250 characters by " + over + "; please reduce the size.";
    }

    // ---------- Hash ----------

    /** First two digits of ID : message number : FIRSTWORD + LASTWORD (all caps). */
    public String createMessageHash() {
        String[] words = messageText == null ? new String[0] : messageText.trim().split("\\s+");
        String first = "";
        String last = "";
        if (words.length > 0 && !words[0].isEmpty()) {
            first = clean(words[0]);
            last = clean(words[words.length - 1]);
        }
        String prefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    // Strips punctuation such as "tonight?" -> "tonight"
    private static String clean(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // ---------- Send / store / disregard ----------

    /**
     * 1 = Send, 2 = Disregard, 3 = Store to send later.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    /** Stores all stored messages in a JSON file. */
    public boolean storeMessage() {
        storedMessages.add(this);
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(storedMessages.get(i).toJson());
            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");
        try {
            Files.write(Paths.get(storageFile), json.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.err.println("Could not write JSON file: " + e.getMessage());
            return false;
        }
    }

    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escape(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escape(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escape(recipient) + "\",\n"
                + "    \"message\": \"" + escape(messageText) + "\"\n"
                + "  }";
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ---------- Reporting ----------

    /** Details in the order: Message ID, Message Hash, Recipient, Message. */
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    /** Returns all messages sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.getDetails()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    /** Clears the in-memory lists (used between unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
    }

    // ---------- Getters ----------

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
}
