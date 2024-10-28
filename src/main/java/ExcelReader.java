import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    public static Map<String, List<String>> createAccountRoleMap(List<String> accountTypes, List<String> roleTypes) {
        Map<String, List<String>> accountRoleMap = new HashMap<>();

        // Check if the lists have the same size
        if (accountTypes.size() != roleTypes.size()) {
            throw new IllegalArgumentException("Account types and role types lists must be of the same size.");
        }

        // Map each account type to its corresponding role type
        for (int i = 0; i < accountTypes.size(); i++) {
            String accountType = accountTypes.get(i);
            String roleType = roleTypes.get(i);

            // Get the existing list of roles or create a new one if it doesn't exist
            List<String> roles = accountRoleMap.computeIfAbsent(accountType, k -> new ArrayList<>());

            // Append the role type to the existing list
            roles.add(roleType);
        }

        return accountRoleMap;
    }

    public static List<String> filterAccountRolePairs(Map<String, List<String>> accountRoleMap,
                                                      List<String> accTypeData,
                                                      List<String> roleTypeData) {
        List<String> validPairs = new ArrayList<>();

        // Ensure that both lists are of the same size
        if (accTypeData.size() != roleTypeData.size()) {
            throw new IllegalArgumentException("Account types and role types lists must be of the same size.");
        }

        // Iterate through both lists
        for (int i = 0; i < accTypeData.size(); i++) {
            String accountType = accTypeData.get(i);
            String roleType = roleTypeData.get(i);

            // Check if the account type exists in the mapping and if it has the role type
            if (accountRoleMap.containsKey(accountType) &&
                    accountRoleMap.get(accountType).contains(roleType)) {
                // Create a string representation of the pair and add it to validPairs
                validPairs.add("Account Type: " + accountType + ", Role Type: " + roleType);
            }
        }

        return validPairs;
    }

    public static void main(String[] args) {
        String excelFilePath = "D:\\JOB\\rule_engine3\\src\\main\\resources\\rules.xlsx";
        List<String> accountTypes = new ArrayList<>();
        List<String> roleTypes = new ArrayList<>();
        Map<String, List<String>> accountRoleMap;


        try
            {
            FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));

            // Create Workbook instance for .xlsx file
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet

            // Loop through each row in the sheet
            for (Row row : sheet) {
                Cell firstCell = row.getCell(0); // Check the first cell of each row for header names
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String header = firstCell.getStringCellValue();

                    // Collect account types
                    if (header.equalsIgnoreCase("Account Type")) {
                        for (int i = 1; i < row.getLastCellNum(); i++) { // Start from column 1 to skip "Account Type" header
                            Cell cell = row.getCell(i);
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                accountTypes.add(cell.getStringCellValue());
                            }
                        }
                    }

                    // Collect role types
                    if (header.equalsIgnoreCase("Role Type")) {
                        for (int i = 1; i < row.getLastCellNum(); i++) { // Start from column 1 to skip "Role Type" header
                            Cell cell = row.getCell(i);
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                roleTypes.add(cell.getStringCellValue());
                            }
                        }
                    }
                }
            }

            // Close the workbook and input stream
            workbook.close();
            fileInputStream.close();

            // Print the results
            System.out.println("Account Types: " + accountTypes);
            System.out.println("Role Types: " + roleTypes);

            }
        catch (IOException e)
            {
                e.printStackTrace();
            }

        System.out.println("Account Types: " + accountTypes);
        System.out.println("Role Types: " + roleTypes);

        /////////////////////////////////Reading the Data.xlsx file /////////////////

        excelFilePath = "D:\\JOB\\rule_engine3\\src\\main\\resources\\data.xlsx";
        List<String> accTypeData = new ArrayList<>();
        List<String> roleTypeData = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));

            // Create Workbook instance for .xlsx file
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet

            // Loop through each row in the sheet (starting from row 1 to skip headers)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Get the AccountType value from the first column
                    Cell accountCell = row.getCell(0);
                    if (accountCell != null && accountCell.getCellType() == CellType.STRING) {
                        accTypeData.add(accountCell.getStringCellValue());
                    }

                    // Get the RoleType value from the second column
                    Cell roleCell = row.getCell(1);
                    if (roleCell != null && roleCell.getCellType() == CellType.STRING) {
                        roleTypeData.add(roleCell.getStringCellValue());
                    }
                }
            }

            // Close the workbook and input stream
            workbook.close();
            fileInputStream.close();

            // Print the results
            System.out.println("Account Types: " + accTypeData);
            System.out.println("Role Types: " + roleTypeData);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ////////////////////////////// Hash Map printing
        accountRoleMap = createAccountRoleMap(accountTypes, roleTypes);
        System.out.println("Account Type to Role Type Mapping:");

        for (Map.Entry<String, List<String>> entry : accountRoleMap.entrySet())
        {
            System.out.println("Account Type: " + entry.getKey() + " -> Role Types: " + entry.getValue());
        }




    }
}
