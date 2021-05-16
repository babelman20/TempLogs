package com.babelman;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GoogleQuickstart {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SHEETS_SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final List<String> DRIVE_SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    //.DRIVE_METADATA_READONLY
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final List<String> SCOPES) throws IOException {
        // Load client secrets.
        InputStream in = GoogleQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void createNewSpreadsheet(Date startDate, Date endDate, Device device, ArrayList<DataElement> tempData, String shareUser) throws IOException, GeneralSecurityException {
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                System.err.println(e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
                System.out.println("Permission ID: " + permission.getId());
            }
        };
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        Spreadsheet requestBody = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                    .setTitle(device.getName().concat("_").concat(fmt.format(startDate)).concat("_").concat(fmt.format(endDate))));

        Credential driveCredential = getCredentials(httpTransport, DRIVE_SCOPES);
        Drive driveService = new Drive.Builder(httpTransport, jsonFactory, driveCredential).setApplicationName("MedicalTempLogs/0.1").build();

        Credential sheetsCredential = getCredentials(httpTransport, SHEETS_SCOPES);
        Sheets sheetsService = new Sheets.Builder(httpTransport, jsonFactory, sheetsCredential).setApplicationName("MedicalTempLogs/0.1").build();

        Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);

        Spreadsheet response = request.execute();
        ApplicationGUI.setProgress(60);

        System.out.println(response);
        System.out.println(response.getSpreadsheetId());

        List<List<Object>> values = getSheetValues(device, tempData);
        ValueRange body = new ValueRange().setValues(values);

        Sheets.Spreadsheets.Values.Update requestUpdate = sheetsService.spreadsheets().values()
                .update(response.getSpreadsheetId(), "A1", body).setValueInputOption("USER_ENTERED");

        UpdateValuesResponse result = requestUpdate.execute();
        ApplicationGUI.setProgress(70);

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setMergeCells(new MergeCellsRequest()
                    .setMergeType("MERGE_ROWS")
                    .setRange(new GridRange()
                        .setSheetId(0)
                        .setStartColumnIndex(0)
                        .setStartRowIndex(tempData.size()+2)
                        .setEndColumnIndex(2)
                        .setEndRowIndex(tempData.size()+4)
                    )
                )
        );

        BatchUpdateSpreadsheetRequest updateBody = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(response.getSpreadsheetId(), updateBody).execute();
        ApplicationGUI.setProgress(75);

        requests = new ArrayList<>();
        requests.add(new Request()
                .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                        .setProperties(new SheetProperties()
                                .setTitle("Report"))
                        .setFields("title")
                )
        );

        updateBody = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(response.getSpreadsheetId(), updateBody).execute();
        ApplicationGUI.setProgress(80);

        requests = new ArrayList<>();
        requests.add(new Request()
                .setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(0)
                                .setDimension("COLUMNS")
                                .setStartIndex(0)
                                .setEndIndex(1)
                        )
                        .setProperties(new DimensionProperties()
                                .setPixelSize(Math.max(device.getName().length(), device.getMacAddr().length())*16-120)
                        )
                        .setFields("pixelSize")
                )
        );
        updateBody = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(response.getSpreadsheetId(), updateBody).execute();
        ApplicationGUI.setProgress(90);

        BatchRequest batch = driveService.batch();
        Permission userPermission = new Permission()
                .setType("user")
                .setRole("writer")
                .setEmailAddress(shareUser);
        driveService.permissions().create(response.getSpreadsheetId(), userPermission)
                .setFields("id")
                .queue(batch, callback);
        batch.execute();
        ApplicationGUI.setProgress(100);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("https://docs.google.com/spreadsheets/d/".concat(response.getSpreadsheetId())));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<List<Object>> getSheetValues(Device device, ArrayList<DataElement> tempData) {
        List[] output = new List[tempData.size()+5];
        Object[] val = new Object[]{"", "", "Day", "8:00 AM", "7:00 PM", "Low", "High"};
        output[0] = Arrays.asList(val.clone());
        double high = -Double.MAX_VALUE, low = Double.MAX_VALUE;
        String triggered = "Not triggered";
        for (int i = 0; i < tempData.size(); i++) {
            DataElement de = tempData.get(i);
            Object[] temp = new Object[]{"", de.getDate(), de.getDayOfWeek(), de.getOpenTemp(),
                                            de.getCloseTemp(), de.getLowTemp(), de.getHighTemp()};
            output[i+1] = Arrays.asList(temp.clone());
            high = Double.max(high, tempData.get(i).getHighTemp());
            low = Double.min(low, tempData.get(i).getLowTemp());
            if (triggered.equals("Not triggered")) triggered = tempData.get(i).getAlarmStatus();
        }
        val = new Object[]{"", "", "", "", "", "", ""};
        output[tempData.size()+1] = Arrays.asList(val.clone());
        val = new Object[]{"Mac Adress: ".concat(device.getMacAddr()), "", "Start:", tempData.get(0).getDate(), "", "HIGH:", high};
        output[tempData.size()+2] = Arrays.asList(val.clone());
        val = new Object[]{"Device Name: ".concat(device.getName()), "", "End:", tempData.get(tempData.size()-1).getDate(), "", "LOW:", low};
        output[tempData.size()+3] = Arrays.asList(val.clone());
        val = new Object[]{"", "", "", "", "", "ALARM:", triggered};
        output[tempData.size()+4] = Arrays.asList(val.clone());

        return Arrays.asList(output.clone());
    }
}