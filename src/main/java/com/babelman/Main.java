package com.babelman;

import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Date;

public class Main {

    public static boolean run = true;
    private final static ApplicationGUI login = new ApplicationGUI();

    private static String apiKey, shareUser;

    private static Date start, end;
    private static String device;

    public static void main(String[] args) {
        try {
            readConfig();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            JLabel label = new JLabel("An error occurred reading the config file, it may be missing");
            label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
            JOptionPane.showMessageDialog(null, label, "Config Error", JOptionPane.ERROR_MESSAGE);
        }
        Requests.setApiToken(apiKey);//args[0]);
        SwingUtilities.invokeLater(login);

        try {
            Thread.sleep(100);
            while (login.isRunning()) {
                Thread.sleep(100);
            }
            Thread.sleep(50);
            Requests.getTempsTimes(device, start, end, shareUser);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            JLabel label = new JLabel("An error occurred communicating with EasyLogCloud servers");
            label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
            JOptionPane.showMessageDialog(null, label, "Communication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void readConfig() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(Main.class.getResourceAsStream("/config"));
        apiKey = ois.readObject().toString();
        shareUser = ois.readObject().toString();
        ois.close();
    }

    public static void setRun(String name, Date startDate, Date endDate) {
        device = name;
        start = startDate;
        end = endDate;
    }
}
