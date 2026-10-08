param(
  [string]$Username = "demo_$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())",
  [string]$Password = 'StudyDemo2026!',
  [string]$Timezone = 'Australia/Sydney'
)

$ErrorActionPreference = 'Stop'

function Invoke-StudyApi {
  param(
    [Parameter(Mandatory)][string]$Method,
    [Parameter(Mandatory)][string]$Uri,
    [object]$Body,
    [hashtable]$Headers = @{}
  )
  $options = @{ Method = $Method; Uri = $Uri; Headers = $Headers }
  if ($null -ne $Body) {
    $options.ContentType = 'application/json'
    $options.Body = $Body | ConvertTo-Json -Depth 12
  }
  Invoke-RestMethod @options
}

$session = Invoke-StudyApi -Method Post -Uri 'http://localhost:8085/api/auth/register' -Body @{
  username = $Username
  password = $Password
  displayName = 'Demonstration Student'
  timezone = $Timezone
}
$headers = @{
  Authorization = "Bearer $($session.token)"
  'X-Study-Timezone' = $Timezone
}

$subject = Invoke-StudyApi -Method Post -Uri 'http://localhost:8081/api/subjects' -Headers $headers -Body @{
  code = 'CSCI318'
  name = 'Software Engineering Practices and Principles'
  creditPoints = 6
  weeklyStudyTargetMinutes = 300
  assessments = @()
}

$today = [DateTime]::Today
$due = $today.AddDays(21)
$assessment = Invoke-StudyApi -Method Post -Uri 'http://localhost:8082/api/assessments' -Headers $headers -Body @{
  subjectId = $subject.id
  title = 'Architecture Demonstration'
  type = 'Presentation'
  weighting = 30
  dueDate = $due.ToString('yyyy-MM-dd')
  estimatedMinutes = 420
  priority = 'HIGH'
  description = 'Explain DDD, event flows, Kafka Streams and the planning agent'
}

$sessionRecord = Invoke-StudyApi -Method Post -Uri 'http://localhost:8083/api/study-sessions' -Headers $headers -Body @{
  subjectId = $subject.id
  durationMinutes = 45
  studyDate = $today.ToString('yyyy-MM-dd')
  description = 'Prepared the event-driven architecture demonstration'
}

$workload = $null
$progress = $null
for ($attempt = 1; $attempt -le 20; $attempt++) {
  Start-Sleep -Milliseconds 500
  try {
    $workload = Invoke-StudyApi -Method Get -Uri 'http://localhost:8084/api/planning/workload' -Headers $headers
    $progress = Invoke-StudyApi -Method Get -Uri "http://localhost:8084/api/planning/progress/$($subject.id)" -Headers $headers
    if ($workload.incompleteAssessments -ge 1 -and $progress.studiedMinutes -ge 45) { break }
  } catch {
    if ($attempt -eq 20) { throw }
  }
}

[pscustomobject]@{
  Username = $Username
  Password = $Password
  SubjectId = $subject.id
  AssessmentId = $assessment.id
  StudySessionId = $sessionRecord.id
  ProjectedIncompleteAssessments = $workload.incompleteAssessments
  ProjectedStudyMinutes = $progress.studiedMinutes
}
