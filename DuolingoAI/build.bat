@echo off
mkdir bin
javac -cp .;sqlite-jdbc.jar;Tess4J.jar -d bin src\DuolingoAI.java
echo Compilation terminée.
jar cfm DuolingoAI.jar src\MANIFEST.MF -C bin .
echo JAR créé : DuolingoAI.jar
