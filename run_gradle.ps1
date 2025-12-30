# Set Java 21 as the default JAVA_HOME
$env:JAVA_HOME = "C:\Users\Administrator\.jdks\azul-21.0.3"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java version
Write-Host "Java Version:" -ForegroundColor Green
java --version

# Verify Gradle version
Write-Host "\nGradle Version:" -ForegroundColor Green
.\gradlew --version

# Run clean build with stacktrace
Write-Host "\nRunning Gradle Clean Build..." -ForegroundColor Green
.\gradlew clean build --stacktrace