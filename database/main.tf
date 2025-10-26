terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = ">= 5.0" }
  }
}

provider "aws" {
  region = var.region
}

variable "region" {
  type    = string
  default = "ap-south-1"
}

variable "env" {
  type    = string
  default = "prod"
}

# ── mq_compare table ────────────────────────────────────────────────────────────
resource "aws_dynamodb_table" "mq_compare" {
  name         = "mq_compare"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "PK"
  range_key    = "SK"

  attribute { name = "PK";     type = "S" }
  attribute { name = "SK";     type = "S" }
  attribute { name = "GSI1PK"; type = "S" }
  attribute { name = "GSI1SK"; type = "N" }
  attribute { name = "GSI2PK"; type = "S" }
  attribute { name = "GSI2SK"; type = "N" }
  attribute { name = "GSI3PK"; type = "S" }
  attribute { name = "GSI3SK"; type = "S" }

  global_secondary_index {
    name            = "GSI1"
    hash_key        = "GSI1PK"
    range_key       = "GSI1SK"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "GSI2"
    hash_key        = "GSI2PK"
    range_key       = "GSI2SK"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "GSI3"
    hash_key        = "GSI3PK"
    range_key       = "GSI3SK"
    projection_type = "ALL"
  }

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"

  ttl {
    attribute_name = "ttlEpoch"
    enabled        = true
  }

  tags = {
    Project = "MQCompare"
    Env     = var.env
  }
}

# ── program_config table ────────────────────────────────────────────────────────
resource "aws_dynamodb_table" "program_config" {
  name         = "program_config"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "programId"
  range_key    = "version"

  attribute { name = "programId"; type = "S" }
  attribute { name = "version";   type = "N" }

  tags = {
    Project = "MQCompare"
    Env     = var.env
  }
}

output "mq_compare_arn" {
  value = aws_dynamodb_table.mq_compare.arn
}

output "program_config_arn" {
  value = aws_dynamodb_table.program_config.arn
}
