package com.babelman;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import org.json.simple.parser.ParseException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ApplicationGUI implements Runnable {

    private boolean running = false;
    private JFrame loginWindow;
    private JFrame dateWindow;
    private static JFrame progressWindow;
    private static JProgressBar progressBar;
    private final Date now = Calendar.getInstance().getTime();

    private void initialize() {
        loginWindow = new JFrame("Login");
        Container content = loginWindow.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = new GridBagConstraints();

        loginWindow.setSize(450, 300);
        loginWindow.setLocationRelativeTo(null);
        loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        JLabel title = new JLabel("Enter EasyLogCloud email and password");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 18));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        titlePanel.add(title, constraints);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        JTextField emailField = new JTextField(15);
        emailField.setText("");
        JLabel emailLabel = new JLabel("Email: ");
        emailLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setText("");
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        fieldsPanel.add(emailLabel, constraints);

        constraints.gridx = 1;
        fieldsPanel.add(emailField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        fieldsPanel.add(passwordLabel, constraints);

        constraints.gridx = 1;
        fieldsPanel.add(passwordField, constraints);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JButton okButton = new JButton("Ok");
        okButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        okButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (email.equals("")) {
                JLabel label = new JLabel("Please enter an email address");
                label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                JOptionPane.showMessageDialog(loginWindow, label, "Invalid Email", JOptionPane.ERROR_MESSAGE);
            } else if (password.equals("")) {
                JLabel label = new JLabel("Please enter a password");
                label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                JOptionPane.showMessageDialog(loginWindow, label, "Invalid Password", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Requests.getGUID(email, password);

                    if (Requests.isValidGUID()) {
                        loginWindow.dispose();

                        Requests.getDeviceInfo();

                        startDateWindow();
                    } else {
                        JOptionPane.showMessageDialog(loginWindow, "Login Error", "Invalid email or password", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException | ParseException exception) {
                        exception.printStackTrace();
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            running = false;
            Main.run = false;
            loginWindow.dispose();
            System.exit(0);
        });
        cancelButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        buttonPanel.add(okButton, constraints);

        constraints.gridy = 1;
        buttonPanel.add(cancelButton, constraints);

        content.add(titlePanel, constraints);

        content.add(fieldsPanel, constraints);

        content.add(buttonPanel, constraints);

        loginWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent windowEvent) {
                running = false;
                System.exit(0);
            }
        });

        loginWindow.setVisible(true);
    }

    private void startDateWindow() {
        dateWindow = new JFrame("Select Thermometer and Date Range");
        Container content = dateWindow.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = new GridBagConstraints();

        dateWindow.setSize(450, 300);
        dateWindow.setLocationRelativeTo(null);
        dateWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        JLabel title = new JLabel("Select a start/end date and a thermometer");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        titlePanel.add(title, constraints);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        JDatePickerImpl startPicker = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
        JLabel startLabel = new JLabel("Start Date: ");
        startLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        JDatePickerImpl endPicker = new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
        JLabel endLabel = new JLabel("End Date: ");
        endLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        JComboBox<String> devicePicker = new JComboBox<>(Requests.getDeviceNames());
        JLabel deviceLabel = new JLabel("Device :");
        deviceLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        fieldsPanel.add(startLabel, constraints);

        constraints.gridx = 1;
        fieldsPanel.add(startPicker, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        fieldsPanel.add(endLabel, constraints);

        constraints.gridx = 1;
        fieldsPanel.add(endPicker, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        fieldsPanel.add(deviceLabel, constraints);

        constraints.gridx = 1;
        fieldsPanel.add(devicePicker, constraints);


        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JButton okButton = new JButton("Confirm");
        okButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        okButton.addActionListener(e -> {
            Date start = (Date)startPicker.getModel().getValue();
            Date end = (Date)endPicker.getModel().getValue();
            String device = (devicePicker.getSelectedItem() != null) ? devicePicker.getSelectedItem().toString() : null;

            if (start.compareTo(end) >= 0) {
                JLabel label = new JLabel("End date must come after start date");
                label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                JOptionPane.showMessageDialog(dateWindow, label, "Invalid dates", JOptionPane.ERROR_MESSAGE);
            } else if (!end.before(now)) {
                JLabel label = new JLabel("End date must come before current date");
                label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                JOptionPane.showMessageDialog(dateWindow, label, "Invalid dates", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    dateWindow.dispose();
                    Main.setRun(device, start, end);
                    startProgressWindow();
                    running = false;
                    //Thread.sleep(10000);
                } catch (Exception exception) {
                    JLabel label = new JLabel("An error has occurred during data retrieval");
                    label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                    JOptionPane.showMessageDialog(dateWindow, label, "An Error Has Occurred", JOptionPane.ERROR_MESSAGE);
                    exception.printStackTrace();
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            running = false;
            Main.run = false;
            dateWindow.dispose();
            System.exit(0);
        });
        cancelButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        buttonPanel.add(okButton, constraints);

        constraints.gridy = 1;
        buttonPanel.add(cancelButton, constraints);

        content.add(titlePanel, constraints);

        content.add(fieldsPanel, constraints);

        content.add(buttonPanel, constraints);

        dateWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent windowEvent) {
                running = false;
                Main.run = false;
                System.exit(0);
            }
        });

        dateWindow.setVisible(true);
    }

    private void startProgressWindow() {
        progressWindow = new JFrame("Progress");
        Container content = progressWindow.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = new GridBagConstraints();

        progressWindow.setSize(350, 150);
        progressWindow.setLocationRelativeTo(null);
        progressWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        JLabel title = new JLabel("Loading...");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        titlePanel.add(title, constraints);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        progressBar = new JProgressBar(0, 100);

        constraints.gridx = 0;
        constraints.gridy = 0;
        fieldsPanel.add(progressBar, constraints);

        content.add(titlePanel, constraints);

        content.add(fieldsPanel, constraints);

        progressWindow.setUndecorated(true);
        progressWindow.setVisible(true);
    }

    public static void setProgress(int val) {
        progressBar.setValue(val);
        if (val == 100) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressWindow.dispose();
        }
    }

    @Override
    public void run() {
        running = true;
        initialize();
    }

    public boolean isRunning() {
        return running;
    }
}
