import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {



    public static Map<String, List<List<String>>> createRuleMap(List<String> accountTypes, List<String> roleTypes, List<String> orgTypes) {

        Map<String, List<List<String>>> ruleMap = new HashMap<>();
        List<List<String>> allPairs = new ArrayList<>(); // To store pairs corresponding to "ALL"

        // Check if the lists have the same size
        if (accountTypes.size() != roleTypes.size() || orgTypes.size() != roleTypes.size()) {
            throw new IllegalArgumentException("Account types, role types, and org types lists must be of the same size.");
        }

        // First loop: Separate out "ALL" pairs and initialize entries for specific organizations
        for (int i = 0; i < orgTypes.size(); i++) {
            String orgType = orgTypes.get(i);
            String accountType = accountTypes.get(i);
            String roleType = roleTypes.get(i);

            List<String> accountRolePair = new ArrayList<>();
            accountRolePair.add(accountType);
            accountRolePair.add(roleType);

            if (orgType.equalsIgnoreCase("ALL")) {
                // Collect pairs marked with "ALL"
                allPairs.add(accountRolePair);
            } else {
                // Initialize an entry for each specific organization if not present
                ruleMap.computeIfAbsent(orgType, k -> new ArrayList<>()).add(accountRolePair);
            }
        }

        // Second step: Distribute "ALL" pairs to each specific organization
        for (String org : ruleMap.keySet()) {
            for (List<String> pair : allPairs) {
                ruleMap.get(org).add(new ArrayList<>(pair)); // Add a copy of each "ALL" pair
            }
        }

        return ruleMap;
    }

    public static List<List<String>> filterAccountRolePairs(Map<String, List<List<String>>> RuleMap,
                                                      List<String> accTypeData,
                                                      List<String> roleTypeData, List<String> orgNameData )
    {


        List<List<String>> validPairs = new ArrayList<>();

        // Iterate over the provided data to check each org-account-role combination
        for (int i = 0; i < accTypeData.size(); i++)
        {
            String orgType = orgNameData.get(i);
            String accountType = accTypeData.get(i);
            String roleType = roleTypeData.get(i);

            // Check if RuleMap contains the orgType and if the (accountType, roleType) pair exists for it
            if (RuleMap.containsKey(orgType))
            {
                List<List<String>> accountRolePairs = RuleMap.get(orgType);
                List<String> currentPair = List.of(accountType, roleType);

                // Check if the current pair [accountType, roleType] is present in accountRolePairs
                if (accountRolePairs.contains(currentPair)) {
                    // Add the valid [orgType, accountType, roleType] combination to validPairs
                    validPairs.add(List.of(orgType, accountType, roleType));
                }
            }
        }

        return validPairs;
    }

    public static double calculateTotalNetWorth(List<List<String>> validData, Map<List<String>, List<Double>> linkerMap) {
        double totalNetWorth = 0.0;

        // Iterate through each valid data entry
        for (List<String> validEntry : validData) {
            // Create a key list from the valid entry
            List<String> keyList = List.of(validEntry.get(0), validEntry.get(1), validEntry.get(2));

            // Check if the key exists in LinkerMap
            if (linkerMap.containsKey(keyList)) {
                // Retrieve the corresponding value list
                List<Double> valueList = linkerMap.get(keyList);

                // Ensure that the valueList contains the expected number of elements
                if (valueList.size() == 2) {
                    // Add the values to totalNetWorth

                    double shareHold = valueList.get(0);
                    double totalAccBal = valueList.get(1);

                    totalNetWorth += shareHold*totalAccBal*0.001;

                }
            }
        }

        return totalNetWorth;
    }

    public static void main(String[] args) {
        String excelFilePath1 = "D:\\JOB\\rule_engine3\\src\\main\\resources\\rules.xlsx";
        List<String> accountTypesRules = new ArrayList<>();
        List<String> roleTypesRules = new ArrayList<>();
        List<String> orgTypesRules = new ArrayList<>();
        Map<String, List<List<String>>> RuleMap;

        int accountTypeColRule = -1;
        int roleTypeColRule = -1;
        int orgTypeColRule = -1;


        try {
            FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath1));

            // Create Workbook instance for .xlsx file
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet

            /////////// taking dynamic values of accountTypeCol && roleTypeCol
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    if (cell.getCellType() == CellType.STRING) {
                        String header = cell.getStringCellValue();
                        if (header.equalsIgnoreCase("AccountType")) {
                            accountTypeColRule = cell.getColumnIndex();
                        } else if (header.equalsIgnoreCase("RoleType")) {
                            roleTypeColRule = cell.getColumnIndex();
                        }
                        else if (header.equalsIgnoreCase("OrganizationName")) {
                            orgTypeColRule = cell.getColumnIndex();
                        }
                    }
                }
            }
            System.out.println(orgTypeColRule);
            // Check if both columns were found
            if (accountTypeColRule == -1 || roleTypeColRule == -1 || orgTypeColRule == -1) {
                throw new IllegalArgumentException("Required columns 'AccountType' or 'RoleType' or 'Org Name 'not found in Rule Book header row.");
            }

            // Loop through each row in the sheet (starting from row 1 to skip headers)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Get the AccountType value from the first column
                    Cell accountCell = row.getCell(accountTypeColRule);
                    if (accountCell != null && accountCell.getCellType() == CellType.STRING) {
                        accountTypesRules.add(accountCell.getStringCellValue());
                    }

                    // Get the RoleType value from the second column
                    Cell roleCell = row.getCell(roleTypeColRule);
                    if (roleCell != null && roleCell.getCellType() == CellType.STRING) {
                        roleTypesRules.add(roleCell.getStringCellValue());
                    }

                    Cell orgNameCell = row.getCell(orgTypeColRule);
                    if(orgNameCell != null && orgNameCell.getCellType() == CellType.STRING){
                        orgTypesRules.add(orgNameCell.getStringCellValue());
                    }

                }
            }

            // Close the workbook and input stream
            workbook.close();
            fileInputStream.close();

        }
        catch (IOException e)
            {
                e.printStackTrace();
            }
        // Print the results
        System.out.println("Account Types: " + accountTypesRules);
        System.out.println("Role Types: " + roleTypesRules);
        System.out.println("Role Types: " + orgTypesRules);

        ////////////////////////////// Hash Map printing
        RuleMap = createRuleMap(accountTypesRules, roleTypesRules , orgTypesRules);
        System.out.println("Rule Map:");

        for(Map.Entry<String, List<List<String>>> entry : RuleMap.entrySet()){
            System.out.println(" " + entry.getKey() + " " + entry.getValue());
        }
        //////////////////////////////////////////////////Defining a list of list containing all the data row wise
        List<List<String>> AllData = new ArrayList<>();

        /////////////////////////////////////////////////////////////////////////////////Reading the Data.xlsx file /////////////////

        String excelFilePath2 = "D:\\JOB\\rule_engine3\\src\\main\\resources\\data.xlsx";
        List<String> orgNameData = new ArrayList<>();
        List<String> accTypeData = new ArrayList<>();
        List<String> roleTypeData = new ArrayList<>();
        List<Double> totalAccBalance = new ArrayList<>();
        List<Double> shareHolding = new ArrayList<>();

        int orgTypeCol = -1;
        int accountTypeCol = -1;
        int roleTypeCol = -1;
        int totalAccCol = -1;
        int shareholdCol = 5;


        try {
            FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath2));

            // Create Workbook instance for .xlsx file
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet

            /////////// taking dynamic values of accountTypeCol && roleTypeCol
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    if (cell.getCellType() == CellType.STRING) {
                        String header = cell.getStringCellValue();
                        if (header.equalsIgnoreCase("AccountType"))
                        {
                            accountTypeCol = cell.getColumnIndex();
                        } else if (header.equalsIgnoreCase("RoleType")) {
                            roleTypeCol = cell.getColumnIndex();
                        }
                        else if (header.equalsIgnoreCase("OrganizationName")) {
                            orgTypeCol = cell.getColumnIndex();
                        }
                        else if (header.equalsIgnoreCase("totalAccBalance")) {
                            totalAccCol = cell.getColumnIndex();

                        }
                        else if (header.equalsIgnoreCase("ShareHolding")) {
                            shareholdCol = cell.getColumnIndex();

                        }
                    }
                }
            }



            // Check if both columns were found
            if (accountTypeCol == -1 || roleTypeCol == -1 || orgTypeCol == -1) {
                throw new IllegalArgumentException("Required columns 'AccountType' or 'RoleType' or 'Org Name 'not found in header row.");
            }

            // Loop through each row in the sheet (starting from row 1 to skip headers)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Get the AccountType value from the first column
                    Cell accountCell = row.getCell(accountTypeCol);
                    if (accountCell != null && accountCell.getCellType() == CellType.STRING) {
                        accTypeData.add(accountCell.getStringCellValue());
                    }

                    // Get the RoleType value from the second column
                    Cell roleCell = row.getCell(roleTypeCol);
                    if (roleCell != null && roleCell.getCellType() == CellType.STRING) {
                        roleTypeData.add(roleCell.getStringCellValue());
                    }

                    Cell orgNameCell = row.getCell(orgTypeCol);
                    if(orgNameCell != null && orgNameCell.getCellType() == CellType.STRING){
                        orgNameData.add(orgNameCell.getStringCellValue());
                    }


                    Cell shareholdCell = row.getCell(shareholdCol);
                    if (shareholdCell != null) {
                        if (shareholdCell.getCellType() == CellType.NUMERIC) {
                            shareHolding.add(shareholdCell.getNumericCellValue());
                        }
                    }

                    Cell totalAccCell = row.getCell(totalAccCol);
                    if (totalAccCell != null) {
                        if (totalAccCell.getCellType() == CellType.NUMERIC) {
                            totalAccBalance.add(totalAccCell.getNumericCellValue());
                        }
                    }


                }
            }

            // Close the workbook and input stream
            workbook.close();
            fileInputStream.close();

            // Print the results
            System.out.println("Account Types Data: " + accTypeData);
            System.out.println("Role Types Data: " + roleTypeData);
            System.out.println("Org Types Data: " + orgNameData);
            System.out.println("shareHolding Data: " + shareHolding);
            System.out.println("totalAccBalance Data: " + totalAccBalance);

        } catch (IOException e) {
            e.printStackTrace();
        }


        List<List<String>> ValidData = filterAccountRolePairs(RuleMap, accTypeData,roleTypeData, orgNameData);
        System.out.println("Valid pairs " + ValidData);

        /////////////Map to org , acc , role to totalAccBal & shareholding

        Map<List<String>, List<Double>> LinkerMap1 = new HashMap<>();

        for (int i = 0; i < orgNameData.size(); i++) {
            // Key list containing orgName, accType, roleType
            List<String> keyList = Arrays.asList(orgNameData.get(i), accTypeData.get(i), roleTypeData.get(i));

            // Value list containing shareHolding and totalAccBalance
            List<Double> valueList = Arrays.asList(shareHolding.get(i), totalAccBalance.get(i));

            // Put the key-value pair in the map
            LinkerMap1.put(keyList, valueList);
        }

        // Print the contents of the map
        System.out.println("Map contents:");
        for (Map.Entry<List<String>, List<Double>> entry : LinkerMap1.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " -> Value: " + entry.getValue());
        }

        Double totalNetWorth = calculateTotalNetWorth(ValidData , LinkerMap1);
        System.out.println("Total NetWorth: " + totalNetWorth);





    }
}
