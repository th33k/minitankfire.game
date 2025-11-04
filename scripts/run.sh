#!/bin/bash
echo "Starting Tank Game Server..."

# Start server in background
cd server
mvn exec:java &
SERVER_PID=$!
cd ..

# Wait a bit for server to start
sleep 5

# Start client web server in background
cd client
python3 -m http.server 3000 --bind 0.0.0.0 &
CLIENT_PID=$!
cd ..

# Wait a bit for client to start
sleep 2

# Open browser (if available)
if command -v xdg-open > /dev/null; then
    xdg-open http://localhost:3000
elif command -v open > /dev/null; then
    open http://localhost:3000
fi

echo "============================================"
echo "Tank Game is running!"
echo "============================================"
echo "Server running on:"
echo "  TCP: localhost:8080 (reliable connections)"
echo "  UDP: localhost:8081 (real-time updates)"
echo "  NIO: localhost:8082 (scalable connections)"
echo "Client web server: http://localhost:3000"
echo ""
echo "NOTE: Client needs to be updated to connect to new server endpoints"
echo "============================================"
echo "Press Ctrl+C to exit (servers will keep running)"

# Wait for user interrupt
trap "echo 'Stopping servers...'; kill $SERVER_PID $CLIENT_PID 2>/dev/null; exit" INT
wait