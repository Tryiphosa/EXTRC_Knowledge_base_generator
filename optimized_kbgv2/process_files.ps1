# Define the path to the JAR file and the directory containing the .txt files
$jarPath = "target\optimized_kbgv2-1.0-SNAPSHOT.jar"
$directoryPath = "MonteCarloSimulationOutput/"  # Update this to your actual directory path

# Get all .txt files in the directory
$files = Get-ChildItem -Path $directoryPath -Filter *.txt

# Loop through each file and run the command
foreach ($file in $files) {
    $filePath = $file.FullName
    Write-Host "Processing file: $filePath"
    Get-Content $filePath | java -cp $jarPath extrc.App
}
