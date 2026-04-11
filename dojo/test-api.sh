#!/bin/bash

echo "Testing Event API..."
echo "===================="

curl -X POST http://localhost:8080/api/v1/event \
  -H "Content-Type: application/json" \
  -d @test-event.json \
  -w "\nHTTP Status: %{http_code}\n" \
  -v

echo ""
echo "===================="
echo "Test completed"
