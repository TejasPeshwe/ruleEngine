import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader2 {


    public static void main(String[] args) {
        String excelFilePath1 = "D:\\JOB\\rule_engine3\\src\\main\\resources\\rules.xlsx";
        List<String> accountTypes = new ArrayList<>();
        List<String> roleTypes = new ArrayList<>();



        Map<String, List<String>> accountRoleMap;


        try {
            System.out.println("Hello1");
            FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath1));

            // Create Workbook instance for .xlsx file
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet


            // Loop through each row in the sheet
            for (Row row : sheet) {
                System.out.println("Hello2");
                Cell firstCell = row.getCell(0); // Check the first cell of each row for header names
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String header = firstCell.getStringCellValue();

                    // Collect account types
                    if (header.equalsIgnoreCase("AccountType")) {
                        for (int i = 1; i < row.getLastCellNum(); i++) { // Start from column 1 to skip "Account Type" header
                            Cell cell = row.getCell(i);
                            if (cell != null && cell.getCellType() == CellType.STRING) {
                                accountTypes.add(cell.getStringCellValue());
                            }
                        }
                    }

                    // Collect role types
                    if (header.equalsIgnoreCase("RoleType")) {
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
            System.out.println("Hello");

            }
        catch (IOException e)
            {
                e.printStackTrace();
            }

        System.out.println("Account Types: " + accountTypes);
        System.out.println("Role Types: " + roleTypes);


    }
}
