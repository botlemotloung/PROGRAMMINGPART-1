package com.mycompany.prog;

import java.util.Scanner;

public class QuickChat {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // ---- Registration and login (Part 1) ----
        System.out.println("=== Register ===");
        Login login;
        String registerResult;
        do {
            System.out.print("First name: ");
            String first = scanner.nextLine().trim();
            System.out.print("Last name: ");
            String last = scanner.nextLine().trim();
            System.out.print("Username: ");
            String u = scanner.nextLine().trim();
            System.out.print("Password: ");
            String p = scanner.nextLine().trim();
            System.out.print("Cell number (e.g. +27718693002): ");
            String c = scanner.nextLine().trim();

            login = new Login(u, p, c, first, last);
            registerResult = login.registerUser();
            System.out.println(registerResult);
        } while (!registerResult.equals("User registered successfully."));

        System.out.println("\n=== Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Username: ");
            String u = scanner.nextLine().trim();
            System.out.print("Password: ");
            String p = scanner.nextLine().trim();
            loggedIn = login.loginUser(u, p);
            System.out.println(login.returnLoginStatus(loggedIn));
        }

        // ---- QuickChat (only reachable after a successful login) ----
        System.out.println("\nWelcome to QuickChat.");

        int maxMessages = readPositiveInt("How many messages do you wish to enter? ");
        int messagesEntered = 0;

        boolean running = true;
        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            int option = readInt("Choose an option: ");

            switch (option) {
                case 1:
                    if (messagesEntered >= maxMessages) {
                        System.out.println("You have already entered all " + maxMessages + " messages.");
                        break;
                    }
                    while (messagesEntered < maxMessages) {
                        System.out.println("\n--- Message " + (messagesEntered + 1) + " of " + maxMessages + " ---");
                        processMessage(messagesEntered);
                        messagesEntered++;
                    }
                    System.out.println("\nTotal messages sent: " + Message.returnTotalMessages());
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }
    }

    private static void processMessage(int messageNumber) {
        // Recipient
        String recipient;
        while (true) {
            System.out.print("Recipient cell number (with international code): ");
            recipient = scanner.nextLine().trim();
            String result = new Message(messageNumber, recipient, "x").checkRecipientCell();
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                break;
            }
        }

        // Message text
        String text;
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            text = scanner.nextLine();
            if (text.length() > Message.MAX_LENGTH) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(new Message(messageNumber, recipient, text).checkMessageLength());
            } else if (text.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
            } else {
                break;
            }
        }

        Message message = new Message(messageNumber, recipient, text);
        System.out.println(message.checkMessageLength());
        System.out.println(message.idGeneratedMessage());

        // Send / disregard / store
        while (true) {
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt("Choose an option: ");
            String result = message.sentMessage(choice);
            System.out.println(result);

            if (choice == 1) {
                System.out.println("\n" + message.getDetails());
                return;
            } else if (choice == 3) {
                return;
            } else if (choice == 2) {
                String input = scanner.nextLine().trim();
                if (input.equals("0")) {
                    System.out.println("Message deleted.");
                    return;
                }
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        int n;
        do {
            n = readInt(prompt);
            if (n <= 0) {
                System.out.println("Please enter a number greater than 0.");
            }
        } while (n <= 0);
        return n;
    }
}