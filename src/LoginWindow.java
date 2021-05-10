import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class LoginWindow implements Runnable {

    private JFrame window;
    private JTextField emailField;
    private JPasswordField passwordField;

    private String userGUID;

    private void initialize() {
        window = new JFrame("UserGUIDRequest");
        Container content = window.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = new GridBagConstraints();

        window.setSize(450, 300);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        JLabel title = new JLabel("Enter EasyLogCloud email and password");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 18));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        titlePanel.add(title, constraints);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        emailField = new JTextField(15);
        emailField.setText("");
        JLabel emailLabel = new JLabel("Email: ");
        emailLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        passwordField = new JPasswordField(15);
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
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (email.equals("")) {
                    JOptionPane.showMessageDialog(window, "Please enter an email address", "Invalid email", 0);
                } else if (password.equals("")) {
                    JOptionPane.showMessageDialog(window, "Please enter a password", "Invalid password", 0);
                } else {
                    /*try {
                        userGUID = Requests.getUserGUID(email, password);
                    } catch (IOException exception) {
                        userGUID = null;
                        exception.printStackTrace();
                    }*/

                    if (userGUID != null) {
                        window.dispose();

                        //Requests.getLocationGUID();

                        //Requests.getDeviceInfo();

                        //DateSelection.start();
                    } else {
                        JOptionPane.showMessageDialog(window, "The userGUID was not found", "userGUID not found", 0);
                    }
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> window.dispose());
        cancelButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        constraints.gridx = 0;
        constraints.gridy = 0;
        buttonPanel.add(okButton, constraints);

        constraints.gridy = 1;
        buttonPanel.add(cancelButton, constraints);

        content.add(titlePanel, constraints);

        content.add(fieldsPanel, constraints);

        content.add(buttonPanel, constraints);
        window.setVisible(true);
    }

    @Override
    public void run() {
        initialize();
    }
}
