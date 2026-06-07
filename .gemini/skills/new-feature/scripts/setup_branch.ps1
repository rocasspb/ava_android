param (
    [Parameter(Mandatory=$true)]
    [string]$FeatureName
)

$normalizedName = $FeatureName.ToLower() -replace '[^a-z0-9]+', '-' -replace '^-+|-+$', ''
$branchName = "feature/$normalizedName"

Write-Host "Setting up feature: $FeatureName"
Write-Host "Branch: $branchName"

$branchExists = git rev-parse --verify $branchName 2>$null

if ($lastExitCode -eq 0) {
    Write-Host "Branch $branchName already exists. Checking it out..."
    git checkout $branchName
} else {
    Write-Host "Creating and checking out branch $branchName..."
    git checkout -b $branchName
}

if ($lastExitCode -eq 0) {
    Write-Host "✅ Success: Now on branch $branchName"
} else {
    Write-Error "Failed to setup branch $branchName"
    exit 1
}
