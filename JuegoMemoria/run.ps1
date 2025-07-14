# Script para ejecutar el Juego de Memoria con JavaFX

Write-Host "Verificando JavaFX..." -ForegroundColor Green

# Verificar si JavaFX ya está descargado
if (-not (Test-Path "javafx-sdk-21")) {
    Write-Host "Descargando JavaFX..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri "https://download2.gluonhq.com/openjfx/21/openjfx-21_windows-x64_bin-sdk.zip" -OutFile "javafx-sdk.zip"
        Write-Host "Extrayendo JavaFX..." -ForegroundColor Yellow
        Expand-Archive -Path "javafx-sdk.zip" -DestinationPath "." -Force
        Remove-Item "javafx-sdk.zip"
        Write-Host "JavaFX descargado y extraído correctamente." -ForegroundColor Green
    } catch {
        Write-Host "Error al descargar JavaFX: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "JavaFX ya está disponible." -ForegroundColor Green
}

# Verificar si las clases están compiladas
if (-not (Test-Path "target/classes/com/mycompany/juegomemoria/App.class")) {
    Write-Host "Compilando el proyecto..." -ForegroundColor Yellow
    try {
        # Crear directorio target si no existe
        if (-not (Test-Path "target/classes")) {
            New-Item -ItemType Directory -Path "target/classes" -Force
        }
        
        # Copiar recursos
        Copy-Item -Path "src/main/resources" -Destination "target/classes" -Recurse -Force
        
        # Compilar con JavaFX en el classpath
        $javafxPath = "javafx-sdk-21\lib"
        javac -cp "$javafxPath\*" -d "target/classes" src/main/java/com/mycompany/juegomemoria/*.java
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Proyecto compilado correctamente." -ForegroundColor Green
        } else {
            Write-Host "Error al compilar el proyecto." -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "Error durante la compilación: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "El proyecto ya está compilado." -ForegroundColor Green
}

Write-Host "Ejecutando el juego..." -ForegroundColor Green
try {
    java --module-path "javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp "target/classes" com.mycompany.juegomemoria.App
} catch {
    Write-Host "Error al ejecutar el juego: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} 