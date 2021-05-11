import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public class Main {
    /* TODO
        - Get user authentication
        - Select start and end date/time for temperature data
        - Read temperature data
        - Import data as Google Sheet
     */
    public static boolean run = true;
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

            while (login.isRunning() || login.isDateRunning()) {

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
