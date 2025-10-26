#!/usr/bin/env bash
set -euo pipefail

# ── Config ──────────────────────────────────────────────────────────────────────
AWS_REGION="${AWS_REGION:-ap-south-1}"      # change if needed
AWS_PROFILE_OPT="${AWS_PROFILE:+--profile $AWS_PROFILE}"  # uses AWS_PROFILE if set

TABLE_MAIN="mq_compare"
TABLE_CFG="program_config"

# ── Helpers ─────────────────────────────────────────────────────────────────────
has_table () {
  local tname="$1"
  if aws dynamodb describe-table --region "$AWS_REGION" $AWS_PROFILE_OPT \
       --table-name "$tname" >/dev/null 2>&1; then
    return 0
  else
    return 1
  fi
}

wait_active () {
  local tname="$1"
  echo "Waiting for table '$tname' to become ACTIVE..."
  aws dynamodb wait table-exists --region "$AWS_REGION" $AWS_PROFILE_OPT \
    --table-name "$tname"
  echo "Table '$tname' is ACTIVE."
}

enable_ttl () {
  local tname="$1"
  local attr="$2"
  echo "Enabling TTL on '$tname' (attribute: $attr)"
  aws dynamodb update-time-to-live --region "$AWS_REGION" $AWS_PROFILE_OPT \
    --table-name "$tname" \
    --time-to-live-specification "Enabled=true, AttributeName=$attr" >/dev/null
}

# ── Create mq_compare (single-table) ────────────────────────────────────────────
create_mq_compare () {
  echo "Creating table '$TABLE_MAIN' ..."
  aws dynamodb create-table --region "$AWS_REGION" $AWS_PROFILE_OPT \
    --table-name "$TABLE_MAIN" \
    --billing-mode PAY_PER_REQUEST \
    --attribute-definitions \
      AttributeName=PK,AttributeType=S \
      AttributeName=SK,AttributeType=S \
      AttributeName=GSI1PK,AttributeType=S \
      AttributeName=GSI1SK,AttributeType=N \
      AttributeName=GSI2PK,AttributeType=S \
      AttributeName=GSI2SK,AttributeType=N \
      AttributeName=GSI3PK,AttributeType=S \
      AttributeName=GSI3SK,AttributeType=S \
    --key-schema \
      AttributeName=PK,KeyType=HASH \
      AttributeName=SK,KeyType=RANGE \
    --global-secondary-indexes '[
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
    ]' \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES \
    --tags Key=Project,Value=MQCompare Key=Env,Value=prod >/dev/null

  wait_active "$TABLE_MAIN"
  enable_ttl "$TABLE_MAIN" "ttlEpoch"
}

# ── Create program_config (optional) ────────────────────────────────────────────
create_program_config () {
  echo "Creating table '$TABLE_CFG' ..."
  aws dynamodb create-table --region "$AWS_REGION" $AWS_PROFILE_OPT \
    --table-name "$TABLE_CFG" \
    --billing-mode PAY_PER_REQUEST \
    --attribute-definitions \
      AttributeName=programId,AttributeType=S \
      AttributeName=version,AttributeType=N \
    --key-schema \
      AttributeName=programId,KeyType=HASH \
      AttributeName=version,KeyType=RANGE \
    --tags Key=Project,Value=MQCompare Key=Env,Value=prod >/dev/null

  wait_active "$TABLE_CFG"
}

# ── Main ────────────────────────────────────────────────────────────────────────
echo "Region: ${AWS_REGION}  ${AWS_PROFILE:+(profile: $AWS_PROFILE)}"

if has_table "$TABLE_MAIN"; then
  echo "Table '$TABLE_MAIN' already exists. Skipping creation."
else
  create_mq_compare
fi

if has_table "$TABLE_CFG"; then
  echo "Table '$TABLE_CFG' already exists. Skipping creation."
else
  create_program_config
fi

echo "Done."
