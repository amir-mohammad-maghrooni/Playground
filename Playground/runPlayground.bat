@echo off
set JAVAFX_LIB=javafx-sdk-24.0.2\lib
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.web, -jar Playground-1.0.0.jar
pause