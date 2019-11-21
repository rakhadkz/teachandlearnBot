package com.shyngys.bot;



import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class App extends TelegramLongPollingBot {
    ReplyKeyboardMarkup button = new ReplyKeyboardMarkup();
    List<KeyboardRow> listButton;
    HashMap<Long, Integer> map = new HashMap<>();
    KeyboardRow keyboardRow = new KeyboardRow();
    String result;
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            botapi.registerBot(new App());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        Message m = update.getMessage();
        String text = m.getText();
        Long chat_id = m.getChatId();
        map.putIfAbsent(chat_id, 1);
        if (text.equals("/start")) sendMsg(m, "Hello, It is our bot");
        if (text.equals("I want to learn") || text.equals("/back")) map.put(chat_id, 2);
        if (text.equals("I want to teach")) map.put(chat_id, 5);
        if (text.equals("/main")) map.put(chat_id, 1);
        SendMessage sendMessage = new SendMessage().setChatId(m.getChatId());
        switch (map.get(chat_id)){
            case 1:
                showButton(sendMessage, learn_and_teachRow(), "Please, choose best one");
                map.put(chat_id, 2);
                break;
            case 2:
                showButton(sendMessage, subjectsRow(), "Please, choose the subject");
                map.put(chat_id, 3);
                break;
            case 3:
                sendMsg(m, getTeachers(text));
                showButton(sendMessage, back(), "Tap back to return");
                map.put(chat_id, 4);
                break;
            case 5:
                sendMsg(m, "What is your first name?");
                map.put(chat_id, 6);
                break;
            case 6:
                if (!text.equals("")){
                    sendMsg(m, "Your first name is  " + text);
                    setData("insert into teacher(id, firstName) values(" + chat_id + ", '" + text + "');");
                    sendMsg(m, "What is your last name?");
                    map.put(chat_id, 7);
                }else map.put(chat_id, 5);
                break;
            case 7:
                if (!text.equals("")){
                    sendMsg(m, "Your last name is  " + text);
                    setData("update teacher set lastName = '" + text + "' where id = " + chat_id + ";");
                    sendMsg(m, "What is your phone number?");
                    map.put(chat_id, 8);
                }else map.put(chat_id, 6);
                break;
            case 8:
                if (!text.equals("")){
                    sendMsg(m, "Your phone number is  " + text);
                    setData("update teacher set phoneNumber = '" + text + "' where id = " + chat_id + ";");
                    showButton(sendMessage, subjectsRow(), "What is your subject?");
                    map.put(chat_id, 9);
                }else map.put(chat_id, 7);
                break;
            case 9:
                if (!text.equals("")){
                    sendMsg(m, "Your subject is  " + text);
                    setData("update teacher set subject = '" + text + "' where id = " + chat_id + ";");
                    sendMsg(m, "What is your offer?");
                    map.put(chat_id, 10);
                }else map.put(chat_id, 8);
                break;
            case 10:
                if (!text.equals("")){
                    sendMsg(m, "Your offer is  " + text);
                    setData("update teacher set offer = '" + text + "' where id = " + chat_id + ";");
                    setData("update teacher set username = '@" + m.getChat().getUserName() + "' where id = " + chat_id + ";");
                    showButton(sendMessage, Main(), "Congratulations! You have been added to database of teachers");
                    map.put(chat_id, 1);
                }else map.put(chat_id, 9);
                break;
        }
    }
    private KeyboardRow subjectsRow() {
        keyboardRow.clear();
        getData(keyboardRow, "select * from subject;");
        keyboardRow.add(new KeyboardButton("/main"));
        return keyboardRow;
    }
    private KeyboardRow back(){
        keyboardRow.clear();
        keyboardRow.add(new KeyboardButton("/back"));
        return keyboardRow;
    }
    private KeyboardRow Main(){
        keyboardRow.clear();
        keyboardRow.add(new KeyboardButton("/main"));
        return keyboardRow;
    }
    private void showButton(SendMessage sendMessage, KeyboardRow row, String text){
        try {
            sendMessage.setText(getButton(row, sendMessage, text));
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(Message message, String text){
        SendMessage s = new SendMessage().setChatId(message.getChatId()).setText(text);
        try {
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return Bot.botName;
    }

    @Override
    public String getBotToken() {
        return Bot.botToken;
    }

    private void setData(String sql){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "123");

            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
    private void getData(KeyboardRow row, String sql){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "123");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while ( rs.next() ) {
                String name = rs.getString("name");
                row.add(new KeyboardButton(name));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
    private String getTeachers(String subject){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "123");
            c.setAutoCommit(false);


            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from teacher where subject = '" + subject + "';");
            result = "";
            int i = 0;
            while ( rs.next() ) {
                i++;
                String fname = rs.getString("firstName");
                String lname = rs.getString("lastName");
                String phoneNumber = rs.getString("phoneNumber");
                String username = rs.getString("username");
                String offer = rs.getString("offer");
                result += i + ")\n" + fname + " " + lname + " " + username + "\n" + phoneNumber + "\n" + offer + "\n\n";
            }
            if (result.equals("")) result = "Sorry, but no one is found";

            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return result;
    }
    private KeyboardRow teachersRow(String subject){
        keyboardRow.clear();
        getData(keyboardRow, "select * from teachers where subject = '" + subject + "';");
        return keyboardRow;
    }
    private KeyboardRow learn_and_teachRow(){
        keyboardRow.clear();
        keyboardRow.add(new KeyboardButton("I want to learn"));
        keyboardRow.add(new KeyboardButton("I want to teach"));
        return keyboardRow;
    }

    String getButton(KeyboardRow row,SendMessage s, String text){
        button.setSelective(true);
        button.setResizeKeyboard(true);
        button.setOneTimeKeyboard(true);
        listButton = new ArrayList<>();
        listButton.add(row);
        button.setKeyboard(listButton);
        s.setReplyMarkup(button);
        return text;
    }
}
