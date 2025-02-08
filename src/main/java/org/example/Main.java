package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize Telegram Bots API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Register the bot
            Bot bot = new Bot();
            botsApi.registerBot(bot);

            System.out.println("Bot is running...");

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

