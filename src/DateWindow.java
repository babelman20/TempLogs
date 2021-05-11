

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static java.util.Calendar.DATE;

public class DateWindow implements Runnable {

    private boolean running = false;
    private JFrame window;

    private void initialize() {
        window = new JFrame("Select Thermometer and Date Range");
        Container content = window.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = new GridBagConstraints();

        window.setSize(450, 300);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
        JComboBox devicePicker = new JComboBox(Requests.getDeviceNames());
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
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date start = (Date)startPicker.getModel().getValue();
                Date end = (Date)endPicker.getModel().getValue();
                String device = devicePicker.getSelectedItem().toString();

                System.out.println(device);
                if (start.compareTo(end) >= 0) {
                    JLabel label = new JLabel("End date must come after start date");
                    label.setFont(new Font("Times New Roman", Font.PLAIN, 16));
                    JOptionPane.showMessageDialog(window, label, "Invalid dates", 0);
                } else {
                    try {
                        Requests.getTempsTimes(device, start, end);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    } catch (ParseException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = false;
                Main.run = false;
                window.dispose();
                System.exit(0);
            }
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

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent windowEvent) {
                running = false;
                Main.run = false;
                System.exit(0);
            }
        });

        window.setVisible(true);
    }

    protected boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        running = true;
        initialize();
    }
}
