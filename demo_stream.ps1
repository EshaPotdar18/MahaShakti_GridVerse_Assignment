# Demo script to stream telemetry events to the Fleet Telematics Service
param (
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " FLEET TELEMATICS STREAM & VALIDATION DEMO" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Check System Health
Write-Host "`n1. Checking System Health..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$BaseUrl/api/v1/health" -Method Get
Write-Host "Status: $($health.status) | Service: $($health.service)" -ForegroundColor Green

# 2. Fetch Initial Tenants & Vehicles
Write-Host "`n2. Retrieving Baseline System Data (Tenants & Authorized Vehicles)..." -ForegroundColor Yellow
$tenants = Invoke-RestMethod -Uri "$BaseUrl/api/v1/tenants" -Method Get
$tenants | Format-Table tenantId, name, status

# 3. Submit Valid Telemetry Stream Event for Tenant 1 (LogiX Corp)
Write-Host "`n3. Submitting VALID Telemetry Event for Tenant 1 (LogiX Corp)..." -ForegroundColor Yellow
$event1 = @{
    eventId = "evt-demo-" + [guid]::NewGuid().ToString()
    vehicleId = "VEH-LOGIX-101"
    tenantId = "TENANT-LOGIX-001"
    timestamp = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
    payload = @{
        speed = 78.5
        fuelLevel = 84.0
        latitude = 37.7749
        longitude = -122.4194
        engineStatus = "RUNNING"
        rpm = 2100
        odometer = 45210.0
    }
} | ConvertTo-Json -Depth 5

$response1 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/telemetry" -Method Post -ContentType "application/json" -Body $event1
Write-Host "Response: Status=$($response1.status) | EventId=$($response1.eventId) | Message=$($response1.message)" -ForegroundColor Green

# 4. Attempt Duplicate Event Submission (Data Deduplication Check)
Write-Host "`n4. Submitting DUPLICATE Telemetry Event (Same eventId: $($response1.eventId))..." -ForegroundColor Yellow
$responseDup = Invoke-RestMethod -Uri "$BaseUrl/api/v1/telemetry" -Method Post -ContentType "application/json" -Body $event1
Write-Host "Response: Status=$($responseDup.status) | Message=$($responseDup.message)" -ForegroundColor Magenta

# 5. Attempt Cross-Tenant Vehicle Submission (Ownership Validation Check)
Write-Host "`n5. Attempting ILLEGAL Telemetry Event (Tenant SWIFT submitting data for Vehicle owned by Tenant LOGIX)..." -ForegroundColor Yellow
$illegalEvent = @{
    eventId = "evt-illegal-" + [guid]::NewGuid().ToString()
    vehicleId = "VEH-LOGIX-101"
    tenantId = "TENANT-SWIFT-002"
    timestamp = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ")
    payload = @{
        speed = 50.0
        fuelLevel = 50.0
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/telemetry" -Method Post -ContentType "application/json" -Body $illegalEvent
} catch {
    Write-Host "REJECTED AS EXPECTED (403 Forbidden)! Details: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Attempt Future Timestamp Submission (Data Quality Check)
Write-Host "`n6. Attempting INVALID Telemetry Event (Future Timestamp)..." -ForegroundColor Yellow
$futureEvent = @{
    eventId = "evt-future-" + [guid]::NewGuid().ToString()
    vehicleId = "VEH-LOGIX-101"
    tenantId = "TENANT-LOGIX-001"
    timestamp = [DateTime]::UtcNow.AddHours(2).ToString("yyyy-MM-ddTHH:mm:ssZ")
    payload = @{
        speed = 60.0
        fuelLevel = 60.0
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/telemetry" -Method Post -ContentType "application/json" -Body $futureEvent
} catch {
    Write-Host "REJECTED AS EXPECTED (400 Bad Request)! Details: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Check Latest Vehicle Telemetry Snapshot
Write-Host "`n7. Fetching Latest Telemetry Snapshot for Vehicle VEH-LOGIX-101..." -ForegroundColor Yellow
$vehStatus = Invoke-RestMethod -Uri "$BaseUrl/api/v1/vehicles/VEH-LOGIX-101" -Method Get
Write-Host "Vehicle: $($vehStatus.vehicleId) | Owner: $($vehStatus.tenantName) | Speed: $($vehStatus.lastKnownSpeed) km/h | Fuel: $($vehStatus.lastKnownFuelLevel)%" -ForegroundColor Green

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host " DEMO COMPLETED SUCCESSFULLY!" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
