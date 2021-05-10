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
        Requests.setApiToken(args[0]);
        SwingUtilities.invokeLater(login);
        try {
            Thread.sleep(100);
            while (login.isRunning()) {
                    Thread.sleep(100);
            }
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("The window is closed");
    }
}
