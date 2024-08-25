# Define the path to the JAR file and the directory containing the .txt files
$jarPath = "target\optimized_kbgv2-1.0-SNAPSHOT.jar"
$directoryPath = "correctness/"  # Update this to your actual directory path

# Define the desired priority level
$priority = [System.Diagnostics.ProcessPriorityClass]::High

# Files to log errors and failed processes
$errorLogPath = "error_log.txt"
$failedProcessesLogPath = "failed_processes_log.txt"

# Get all .txt files in the directory
$files = Get-ChildItem -Path $directoryPath -Filter *.txt

# Loop through each file and run the command
foreach ($file in $files) {
    $filePath = $file.FullName
    Write-Host "Processing file: $filePath"
    
    try {
        # Start the process
        $process = Start-Process -FilePath "java" -ArgumentList "-cp `"$jarPath`" extrc.App" -RedirectStandardInput $filePath -NoNewWindow -PassThru

        # Ensure the process is still running before attempting to set priority
        Start-Sleep -Seconds 1  # Small delay to ensure the process is started

        try {
            # Set the priority
            $process.PriorityClass = $priority
        } catch {
            # Log error for setting priority
            Add-Content -Path $errorLogPath -Value ("Failed to set priority for {0}: {1}" -f $filePath, $_.Exception.Message)
        }

        # Wait for the process to exit
        $process.WaitForExit()

        # Check the exit code of the process
        if ($process.ExitCode -ne 0) {
            # Log failed processes with their exit codes
            Add-Content -Path $failedProcessesLogPath -Value ("Process for {0} failed with exit code {1}" -f $filePath, $process.ExitCode)
        }

    } catch {
        # Log error for starting process
        Add-Content -Path $errorLogPath -Value ("Failed to start process for {0}: {1}" -f $filePath, $_.Exception.Message)
    }
}
