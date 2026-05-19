$javaHome = "C:\Program Files\OpenLogic\jdk-17.0.18.8-hotspot"

if (-not (Test-Path "$javaHome\bin\java.exe")) {
    throw "JDK 17 was not found at $javaHome"
}

$env:JAVA_HOME = $javaHome
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

java -version
