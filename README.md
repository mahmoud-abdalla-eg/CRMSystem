# CRM System

Consumer Relationship Management project for Java Programming.

This project includes:
- A Java Swing desktop CRM application.
- A React + Vite web view of the same CRM idea.

## Main Features

- Add, edit, delete, search, and filter customers.
- Track customer status: Lead, Prospect, Active, Inactive.
- Log customer interactions: Call, Email, Meeting, Note.
- View interactions by customer or interaction type.
- Dashboard with customer statistics and recent customers.
- Reports and settings pages.

## Run the Java Swing App

Double-click:

```text
run.bat
```

Or run manually:

```bash
javac -d bin -sourcepath src src\main\Main.java src\views\MainFrame.java src\models\*.java src\utils\*.java
java -cp bin main.Main
```

## Web View

The React + Vite web version is deployed here:

```text
https://crm-system-mahmoud.vercel.app
```

## Run the Tests

```bash
javac -d bin -sourcepath "src;test" src\models\Customer.java src\models\Interaction.java src\utils\DataStore.java test\CRMSystemTest.java
java -cp bin CRMSystemTest
```

Expected result:

```text
Passed: 4
Failed: 0
```

## Project Structure

```text
CRMSystem/
|-- src/
|   |-- main/Main.java
|   |-- models/Customer.java
|   |-- models/Interaction.java
|   |-- views/MainFrame.java
|   |-- utils/DataStore.java
|-- test/
|   |-- CRMSystemTest.java
|-- crm-web/
|   |-- src/main.jsx
|   |-- src/styles.css
|   |-- package.json
|-- run.bat
|-- README.md
```

## Notes

- Java version uses in-memory data storage with sample customers.
- Web version stores data in browser state while the page is open.
- The project focuses on GUI design, CRUD operations, filtering, and customer relationship workflows.

## Course Information

- Course: Java Programming
- Institution: Jinan University - International School
