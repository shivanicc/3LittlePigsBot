package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private final Map<Long, String> userOrders = new HashMap<>();  // Store user noodle choice
    private final Map<Long, String> userAddOns = new HashMap<>();  // Store user add-ons
    private final Map<Long, String> userStates = new HashMap<>();  // Track user progress

    @Override
    public String getBotUsername() {
        return "ThreeLittlePigsBot";
    }

    @Override
    public String getBotToken() {
        return "7134832616:AAEbWGcpvTslaoRoB9YNUQHgQSXCtOOFgBs";  // Replace with your actual bot token
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            String userMessage = update.getMessage().getText();

            if (userMessage.equals("/start")) {
                sendText(chatId, "Hello lovelies! Are you ready to order? 😊\n\nPlease select a noodle option below.");
                sendNoodleOptions(chatId);
                userStates.put(chatId, "CHOOSING_NOODLE");
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            chatId = query.getMessage().getChatId();
            String data = query.getData();

            if (data.startsWith("noodle_")) {
                handleNoodleSelection(chatId, data);
                userStates.put(chatId, "CHOOSING_ADDON");
            } else if (data.startsWith("addon_")) {
                handleAddOnSelection(chatId, data);
                userStates.put(chatId, "CHOOSING_POPCORN");
            } else if (data.startsWith("popcorn_")) {
                handlePopcornChickenSelection(chatId, data);
            }
        }
    }

    private void sendText(Long chatId, String text) {
        SendMessage sm = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendNoodleOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🍜 *Select your Noodles:*");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createButton("Indomie ($2)", "noodle_1")));
        keyboard.add(List.of(createButton("Samyang (Original) ($3.50)", "noodle_2")));
        keyboard.add(List.of(createButton("Samyang (Carbonara) ($3.50)", "noodle_3")));
        keyboard.add(List.of(createButton("Maggi Curry ($2)", "noodle_4")));
        keyboard.add(List.of(createButton("Shin Ramyun ($3)", "noodle_5")));
        keyboard.add(List.of(createButton("No Noodles (Boooo)", "noodle_6")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleNoodleSelection(Long chatId, String callbackData) {
        String selectedNoodle = switch (callbackData) {
            case "noodle_1" -> "Indomie ($2)";
            case "noodle_2" -> "Samyang (Original) ($3.50)";
            case "noodle_3" -> "Samyang (Carbonara) ($3.50)";
            case "noodle_4" -> "Maggi Curry ($2)";
            case "noodle_5" -> "Shin Ramyun ($3)";
            case "noodle_6" -> "No Noodles (Boooo)";
            default -> null;
        };

        userOrders.put(chatId, selectedNoodle);
        sendText(chatId, "✅ You selected: *" + selectedNoodle + "*\nNow, would you like to add any extras?");
        sendAddOns(chatId);
    }

    private void sendAddOns(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🥢 *Would you like any add-ons?*\n(Click to select/unselect, then confirm)");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createButton(getToggleText(chatId, "addon_1", "Spring Onion (Free)"), "addon_1")));
        keyboard.add(List.of(createButton(getToggleText(chatId, "addon_2", "Chilli Padi (Free)"), "addon_2")));
        keyboard.add(List.of(createButton(getToggleText(chatId, "addon_3", "Egg (+$1)"), "addon_3")));
        keyboard.add(List.of(createButton("🚫 No Add-ons", "addon_no")));

        // ✅ Add a Confirm Selection button to proceed
        keyboard.add(List.of(createButton("✅ Confirm Add-ons", "addon_confirm")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAddOnSelection(Long chatId, String callbackData) {
        if (callbackData.equals("addon_confirm")) {
            // ✅ Finalize selection and move to next step
            sendText(chatId, "✅ Your final selection:\n"
                    + "🍜 Noodles: *" + userOrders.get(chatId) + "*\n"
                    + "➕ Add-ons: " + userAddOns.getOrDefault(chatId, "No add-ons selected.")
                    + "\n\nMoving to Popcorn Chicken selection... 🍗");

            sendPopcornChickenOptions(chatId);
            return;
        }

        if (callbackData.equals("addon_no")) {
            userAddOns.put(chatId, "No add-ons selected.");
        } else {
            Set<String> selectedAddOns = new HashSet<>(Arrays.asList(userAddOns.getOrDefault(chatId, "").split(",")));
            switch (callbackData) {
                case "addon_1" -> toggleSelection(selectedAddOns, "Spring Onion (Free)");
                case "addon_2" -> toggleSelection(selectedAddOns, "Chilli Padi (Free)");
                case "addon_3" -> toggleSelection(selectedAddOns, "Egg (+$1)");
            }
            userAddOns.put(chatId, String.join(", ", selectedAddOns));
        }

        // ✅ Keep the selection process but **DO NOT** re-send add-ons menu (avoid loop)
        sendAddOns(chatId);
    }


    private void toggleSelection(Set<String> selectedAddOns, String item) {
        if (selectedAddOns.contains(item)) {
            selectedAddOns.remove(item);
        } else {
            selectedAddOns.add(item);
        }
    }

    private String getToggleText(Long chatId, String callbackData, String displayText) {
        return userAddOns.getOrDefault(chatId, "").contains(displayText) ? "✅ " + displayText : displayText;
    }


    private void sendPopcornChickenOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🍗 *Would you like some Popcorn Chicken?*");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createButton("Small ($2)", "popcorn_1")));
        keyboard.add(List.of(createButton("Large ($5)", "popcorn_2")));
        keyboard.add(List.of(createButton("No Popcorn Chicken", "popcorn_no")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handlePopcornChickenSelection(Long chatId, String callbackData) {
        String selectedPopcorn = switch (callbackData) {
            case "popcorn_1" -> "Small Popcorn Chicken ($2)";
            case "popcorn_2" -> "Large Popcorn Chicken ($5)";
            case "popcorn_no" -> "No Popcorn Chicken selected.";
            default -> null;
        };

        sendText(chatId, "✅ Order Summary:\n"
                + "🍜 Noodles: *" + userOrders.get(chatId) + "*\n"
                + "➕ Add-ons: " + userAddOns.get(chatId) + "\n"
                + "🍗 Popcorn Chicken: " + selectedPopcorn
                + "\n\nThank you for your order! 🎉");
    }

    private void sendNuggetsOptions(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🍗 *Would you like some Nuggets?*");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createButton("1 ($0.50)", "nugget_1")));
        keyboard.add(List.of(createButton("2 ($1)", "nugget_2")));
        keyboard.add(List.of(createButton("3 ($1.50)", "nugget_3")));
        keyboard.add(List.of(createButton("5 ($2)", "nugget_4")));
        keyboard.add(List.of(createButton("No Nugget", "nugget_no")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleNuggetSelection(Long chatId, String callbackData) {
        String selectedPopcorn = switch (callbackData) {
            case "nugget_1" -> "1 Nugget ($0.50)";
            case "nugget_2" -> "2 Nugget ($1)";
            case "nugget_3" -> "3 Nugget ($1.50)";
            case "nugget_4" -> "4 Nugget ($2)";

            default -> null;
        };

        sendText(chatId, "✅ Order Summary:\n"
                + "🍜 Noodles: *" + userOrders.get(chatId) + "*\n"
                + "➕ Add-ons: " + userAddOns.get(chatId) + "\n"
                + "🍗 Popcorn Chicken: " + selectedPopcorn + "\n"
                + "🍗 Nuggets " + selectedPopcorn + "\n"
                + "\n\nThank you for your order! 🎉");
    }



    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
