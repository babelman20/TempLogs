import javax.swing.*;

public class Main {
    /* TODO
        - Get user authentication
        - Select start and end date/time for temperature data
        - Read temperature data
        - Import data as Google Sheet
     */
    private static LoginWindow login = new LoginWindow();
    public static void main(String args[]) {
        SwingUtilities.invokeLater(login);
    }
}
