#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

# The topic that the receiver writes to
aws --endpoint-url=http://localstack-court-case-service:4566 sns create-topic --name hmpps-domain-events

echo "SNS Configured"
