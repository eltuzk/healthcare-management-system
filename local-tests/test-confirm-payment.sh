#!/bin/bash

# ==============================================================================
# Standalone Curl Manual Test for the Confirm Manual Payment Endpoint
# ==============================================================================
# Make sure your local Spring Boot backend is running on http://localhost:8080.
# Update the variables below with your receptionist JWT token and appointment ID.
# ==============================================================================

APPOINTMENT_ID=1
TOKEN="YOUR_RECEPTIONIST_JWT_TOKEN"
RECEIPT_NUMBER="HD-APT-CURL-002"
NOTE="Thanh toan tien mat tai quay le tan (Test tu script curl)"

echo "=== SENDING MANUAL PAYMENT CONFIRMATION REQUEST ==="
curl -X POST "http://localhost:8080/api/appointments/$APPOINTMENT_ID/confirm-payment-manual" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     -d "{
       \"receiptNumber\": \"$RECEIPT_NUMBER\",
       \"note\": \"$NOTE\"
     }"

echo -e "\n\n=== TEST COMPLETED ==="
