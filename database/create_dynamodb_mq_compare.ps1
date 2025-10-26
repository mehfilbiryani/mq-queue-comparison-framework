Param(
  [string]$Region = $env:AWS_REGION
)
if (-not $Region) { $Region = "ap-south-1" }  # change if needed

$TableMain = "mq_compare"
$TableCfg  = "program_config"

function Test-Table {
  param([string]$Name)
  $null = aws dynamodb describe-table --region $Region --table-name $Name 2>$null
  if ($LASTEXITCODE -eq 0) { return $true } else { return $false }
}

function Wait-Active {
  param([string]$Name)
  Write-Host "Waiting for table '$Name' to become ACTIVE..."
  aws dynamodb wait table-exists --region $Region --table-name $Name | Out-Null
  Write-Host "Table '$Name' is ACTIVE."
}

function Enable-TTL {
  param([string]$Name,[string]$Attr)
  Write-Host "Enabling TTL on '$Name' (attribute: $Attr)"
  aws dynamodb update-time-to-live --region $Region `
    --table-name $Name `
    --time-to-live-specification "Enabled=true, AttributeName=$Attr" | Out-Null
}

function New-MqCompare {
  Write-Host "Creating table '$TableMain' ..."
  aws dynamodb create-table --region $Region `
    --table-name $TableMain `
    --billing-mode PAY_PER_REQUEST `
    --attribute-definitions `
      AttributeName=PK,AttributeType=S `
      AttributeName=SK,AttributeType=S `
      AttributeName=GSI1PK,AttributeType=S `
      AttributeName=GSI1SK,AttributeType=N `
      AttributeName=GSI2PK,AttributeType=S `
      AttributeName=GSI2SK,AttributeType=N `
      AttributeName=GSI3PK,AttributeType=S `
      AttributeName=GSI3SK,AttributeType=S `
    --key-schema `
      AttributeName=PK,KeyType=HASH `
      AttributeName=SK,KeyType=RANGE `
    --global-secondary-indexes @"
[
  {
    "IndexName": "GSI1",
    "KeySchema": [
      {"AttributeName":"GSI1PK","KeyType":"HASH"},
      {"AttributeName":"GSI1SK","KeyType":"RANGE"}
    ],
    "Projection": {"ProjectionType":"ALL"}
  },
  {
    "IndexName": "GSI2",
    "KeySchema": [
      {"AttributeName":"GSI2PK","KeyType":"HASH"},
      {"AttributeName":"GSI2SK","KeyType":"RANGE"}
    ],
    "Projection": {"ProjectionType":"ALL"}
  },
  {
    "IndexName": "GSI3",
    "KeySchema": [
      {"AttributeName":"GSI3PK","KeyType":"HASH"},
      {"AttributeName":"GSI3SK","KeyType":"RANGE"}
    ],
    "Projection": {"ProjectionType":"ALL"}
  }
]
"@ `
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES `
    --tags Key=Project,Value=MQCompare Key=Env,Value=prod | Out-Null

  Wait-Active $TableMain
  Enable-TTL $TableMain "ttlEpoch"
}

function New-ProgramConfig {
  Write-Host "Creating table '$TableCfg' ..."
  aws dynamodb create-table --region $Region `
    --table-name $TableCfg `
    --billing-mode PAY_PER_REQUEST `
    --attribute-definitions `
      AttributeName=programId,AttributeType=S `
      AttributeName=version,AttributeType=N `
    --key-schema `
      AttributeName=programId,KeyType=HASH `
      AttributeName=version,KeyType=RANGE `
    --tags Key=Project,Value=MQCompare Key=Env,Value=prod | Out-Null

  Wait-Active $TableCfg
}

Write-Host "Region: $Region"
if (Test-Table $TableMain) { Write-Host "Table '$TableMain' exists. Skipping." } else { New-MqCompare }
if (Test-Table $TableCfg)  { Write-Host "Table '$TableCfg' exists. Skipping."  } else { New-ProgramConfig }

Write-Host "Done."
