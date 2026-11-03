package com.mycompany.prog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    public static final int MAX_LENGTH = 250;

    // JSON file messages are stored in (tests point this somewhere else)
    public static String storageFile = "messages.json";

    // Cell number of the logged-in user; every new message uses it as its sender
    private static String defaultSender = "Unknown";

    // ---------- Part 3 arrays (filled while the program runs, nothing hard-coded) ----------
    private static final List<Message> sentMessages = new ArrayList<>();         // Sent Message array
    private static final List<Message> disregardedMessages = new ArrayList<>();  // Disregarded Messages array
    private static final List<Message> storedMessages = new ArrayList<>();       // Stored Messages array (read from JSON)
    private static final List<String> messageHashes = new ArrayList<>();         // Message Hash array
    private static final List<String> messageIDs = new ArrayList<>();            // Message ID array

    private static final Random RANDOM = new Random();

    // A JSON string value: handles escaped quotes and backslashes
    private static final String JSON_STRING = "\"((?:[^\"\\\\]|\\\\.)*)\"";

    // One stored message object, in the order storeMessage() writes it (sender and flag optional)
    private static final Pattern JSON_OBJECT = Pattern.compile(
            "\\{\\s*\"messageID\"\\s*:\\s*" + JSON_STRING
            + "\\s*,\\s*\"messageNumber\"\\s*:\\s*(\\d+)"
            + "\\s*,\\s*\"messageHash\"\\s*:\\s*" + JSON_STRING
            + "\\s*,\\s*\"recipient\"\\s*:\\s*" + JSON_STRING
            + "\\s*,\\s*\"message\"\\s*:\\s*" + JSON_STRING
            + "(?:\\s*,\\s*\"sender\"\\s*:\\s*" + JSON_STRING + ")?"
            + "(?:\\s*,\\s*\"flag\"\\s*:\\s*" + JSON_STRING + ")?"
            + "\\s*\\}", Pattern.DOTALL);

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private String sender = defaultSender;
    private String flag = "";

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
     * Each choice also puts the message in the matching array.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                flag = "Sent";
                sentMessages.add(this);
                register();
                return "Message successfully sent.";
            case 2:
                flag = "Disregard";
                disregardedMessages.add(this);
                register();
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                register();
                return "Message successfully stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    /** Adds this message's ID and hash to the Message ID and Message Hash arrays. */
    private void register() {
        if (!messageIDs.contains(messageID)) {
            messageIDs.add(messageID);
            messageHashes.add(messageHash);
        }
    }

    /** Adds the message to the stored array and saves all stored messages to the JSON file. */
    public boolean storeMessage() {
        flag = "Stored";
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }
        return writeStoredFile();
    }

    private static boolean writeStoredFile() {
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
                + "    \"message\": \"" + escape(messageText) + "\",\n"
                + "    \"sender\": \"" + escape(sender) + "\",\n"
                + "    \"flag\": \"" + escape(flag) + "\"\n"
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

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- Reading the JSON file into the Stored Messages array ----------

    /**
     * Reads the JSON file into the stored messages array (replacing what was in it).
     * Returns the number of messages loaded.
     */
    public static int loadStoredMessages() {
        storedMessages.clear();
        Path path = Paths.get(storageFile);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            Matcher m = JSON_OBJECT.matcher(json);
            while (m.find()) {
                Message msg = new Message(unescape(m.group(1)), Integer.parseInt(m.group(2)),
                        unescape(m.group(4)), unescape(m.group(5)));
                if (m.group(6) != null) {
                    msg.sender = unescape(m.group(6));
                }
                msg.flag = m.group(7) != null ? unescape(m.group(7)) : "Stored";
                storedMessages.add(msg);
                msg.register();
            }
        } catch (IOException e) {
            System.err.println("Could not read JSON file: " + e.getMessage());
        }
        return storedMessages.size();
    }

    // ---------- Stored Messages menu features ----------

    /** a. Sender and recipient of all stored messages. */
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("Sender: ").append(m.sender).append(" | Recipient: ").append(m.recipient);
        }
        return sb.toString();
    }

    /** b. The longest message in the given array. */
    public static String longestMessage(List<Message> messages) {
        if (messages.isEmpty()) {
            return "No messages.";
        }
        Message longest = messages.get(0);
        for (int i = 1; i < messages.size(); i++) {
            if (messages.get(i).messageText.length() > longest.messageText.length()) {
                longest = messages.get(i);
            }
        }
        return longest.messageText;
    }

    public static String longestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        return longestMessage(storedMessages);
    }

    /** c. Finds a message by its ID in the sent, stored and disregarded arrays (null if not found). */
    public static Message findMessageByID(String id) {
        List<List<Message>> arrays = new ArrayList<>();
        arrays.add(sentMessages);
        arrays.add(storedMessages);
        arrays.add(disregardedMessages);
        for (List<Message> array : arrays) {
            for (Message m : array) {
                if (m.messageID.equals(id.trim())) {
                    return m;
                }
            }
        }
        return null;
    }

    public static String searchByMessageID(String id) {
        Message m = findMessageByID(id);
        if (m == null) {
            return "No message found with that ID.";
        }
        return "Recipient: " + m.recipient + "\nMessage: " + m.messageText;
    }

    /** d. All sent or stored messages for one recipient. */
    public static List<Message> findMessagesByRecipient(String recipient) {
        List<Message> found = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.recipient.equals(recipient.trim())) {
                found.add(m);
            }
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(recipient.trim())) {
                found.add(m);
            }
        }
        return found;
    }

    public static String searchByRecipient(String recipient) {
        List<Message> found = findMessagesByRecipient(recipient);
        if (found.isEmpty()) {
            return "No messages found for that recipient.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < found.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("Message: ").append(found.get(i).messageText);
        }
        return sb.toString();
    }

    /** e. Deletes a stored message using its hash and updates the JSON file. */
    public static String deleteMessageByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equalsIgnoreCase(hash.trim())) {
                storedMessages.remove(i);
                int index = messageIDs.indexOf(m.messageID);
                if (index >= 0) {
                    messageIDs.remove(index);
                    messageHashes.remove(index);
                }
                writeStoredFile();
                return "Message: \"" + m.messageText + "\" successfully deleted.";
            }
        }
        return "No stored message found with that hash.";
    }

    /** f. Report with the full details of all stored messages. */
    public static String storedMessagesReport() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append("Message ID: ").append(m.messageID).append("\n")
              .append("Message Hash: ").append(m.messageHash).append("\n")
              .append("Sender: ").append(m.sender).append("\n")
              .append("Recipient: ").append(m.recipient).append("\n")
              .append("Message: ").append(m.messageText).append("\n")
              .append("Flag: ").append(m.flag);
        }
        return sb.toString();
    }

    /** Report of all sent messages: Message Hash, Recipient, Message. */
    public static String sentMessagesReport() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            Message m = sentMessages.get(i);
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append("Message Hash: ").append(m.messageHash).append("\n")
              .append("Recipient: ").append(m.recipient).append("\n")
              .append("Message: ").append(m.messageText);
        }
        return sb.toString();
    }

    // ---------- Reporting (Part 2) ----------

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

    // ---------- Static helpers ----------

    public static void setDefaultSender(String sender) {
        defaultSender = sender;
    }

    /** Clears every array (used between unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }

    public static List<Message> getSentMessages() { return new ArrayList<>(sentMessages); }
    public static List<Message> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public static List<Message> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public static List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public static List<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    // ---------- Getters ----------

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
    public String getSender() { return sender; }
    public String getFlag() { return flag; }
}