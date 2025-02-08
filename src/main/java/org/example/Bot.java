package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    private final Map<Long, String> userOrders = new HashMap<>();  // Store user noodle choice
    private final Map<Long, String> userAddOns = new HashMap<>();  // Store user add-ons
    private final Map<Long, String> userStates = new HashMap<>(); // Track user progress


    @Override
    public String getBotUsername() {
        return "ThreeLittlePigsBot";
    }

    @Override
    public String getBotToken() {
        return "7134832616:AAEbWGcpvTslaoRoB9YNUQHgQSXCtOOFgBs";  // Replace with your bot token
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (userMessage.equals("/start")) {
                sendText(chatId, "Hello lovelies! Are you ready to order? 😊\n\nPlease select a noodle option below.");
                sendNoodleOptions(chatId);
                userStates.put(chatId, "CHOOSING_NOODLE");  // Set user state to noodle selection
            }
            else if (userStates.getOrDefault(chatId, "").equals("CHOOSING_NOODLE") && userMessage.matches("[1-6]")) {
                handleNoodleSelection(chatId, userMessage);
                userStates.put(chatId, "CHOOSING_ADDON");  // Move user to add-ons stage
            }
            else if (userStates.getOrDefault(chatId, "").equals("CHOOSING_ADDON") && userMessage.matches("[1-3](,[1-3])*|No")) {
                handleAddOnSelection(chatId, userMessage);
                userStates.put(chatId, "CHOOSING_POPCORN");  // Move user to popcorn selection
            }
            else {
                sendText(chatId, "❌ Invalid input. Please select a valid option.");
            }
        }
    }


    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNoodleOptions(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("🍜 *Select your Noodles:*\n"
                        + "1️⃣ Indomie ($2)\n"
                        + "2️⃣ Samyang (Original) ($3.50)\n"
                        + "3️⃣ Samyang (Carbonara) ($3.50)\n"
                        + "4️⃣ Maggi Curry ($2)\n"
                        + "5️⃣ Shin Ramyun ($3)\n"
                        + "6️⃣ No Noodles (Boooo) ❌\n\n"
                        + "Reply with the number of your choice.")
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void handleNoodleSelection(Long chatId, String choice) {
        if (userOrders.containsKey(chatId)) {
            sendText(chatId, "⚠️ You have already selected: *" + userOrders.get(chatId) + "*.\nIf you want to restart your order, type `/start`.");
            return;
        }

        String selectedNoodle = switch (choice) {
            case "1" -> "Indomie ($2)";
            case "2" -> "Samyang (Original) ($3.50)";
            case "3" -> "Samyang (Carbonara) ($3.50)";
            case "4" -> "Maggi Curry ($2)";
            case "5" -> "Shin Ramyun ($3)";
            case "6" -> "No Noodles (Boooo)";
            default -> null;
        };

        if (selectedNoodle == null) {
            sendText(chatId, "❌ Invalid selection. Please enter a number between 1-6.");
            return;
        }

        userOrders.put(chatId, selectedNoodle);  // Store the selected noodle
        sendText(chatId, "✅ You selected: *" + selectedNoodle + "*\nNow, would you like to add any extras?");
        sendAddOns(chatId);
    }


    public void sendAddOns(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("🥢 *Would you like any add-ons?*\n"
                        + "1️⃣ Spring Onion (free)\n"
                        + "2️⃣ Chilli Padi (free)\n"
                        + "3️⃣ Egg (+$1)\n\n"
                        + "Reply with the number(s) of your choices separated by commas (e.g., 1,2,3) or type 'No' to skip.")
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void handleAddOnSelection(Long chatId, String addOns) {
        String selectedAddOns;

        if (addOns.equalsIgnoreCase("No")) {
            selectedAddOns = "No add-ons selected.";
        } else {
            StringBuilder addOnList = new StringBuilder();
            String[] choices = addOns.split(",");

            for (String choice : choices) {
                switch (choice.trim()) {
                    case "1" -> addOnList.append("Spring Onion (free), ");
                    case "2" -> addOnList.append("Chilli Padi (free), ");
                    case "3" -> addOnList.append("Egg (+$1), ");
                    default -> {
                        sendText(chatId, "❌ Invalid add-on selection. Please enter a number between 1-3.");
                        return;
                    }
                }
            }

            selectedAddOns = addOnList.toString().replaceAll(", $", ""); // Remove trailing comma
        }

        userAddOns.put(chatId, selectedAddOns); // Store add-ons
        sendText(chatId, "✅ Your final selection:\n"
                + "🍜 Noodles: *" + userOrders.get(chatId) + "*\n"
                + "➕ Add-ons: " + selectedAddOns
                + "\n\nThank you for your order! 🍜");

        // Proceed to Popcorn Chicken selection
        sendPopcornChickenOptions(chatId);
    }

    public void sendPopcornChickenOptions(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("🍗 *Would you like some Popcorn Chicken?*\n"
                        + "1️⃣ Small ($2)\n"
                        + "2️⃣ Large ($5)\n\n"
                        + "Reply with the number of your choice or type 'No' to skip.")
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
