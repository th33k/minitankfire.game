#!/bin/bash
echo "Stopping Tank Game Servers..."

# Kill Java processes
pkill -f "mvn exec:java" 2>/dev/null || true
pkill -f "java.*Main" 2>/dev/null || true

# Kill Python web servers
pkill -f "python.*http.server" 2>/dev/null || true
pkill -f "python3.*http.server" 2>/dev/null || true

echo "Servers stopped."