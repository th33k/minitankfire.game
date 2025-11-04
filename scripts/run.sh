#!/bin/bash
echo "Starting Mini Tank Fire Game..."

# Start server in background
cd server
mvn exec:java &
SERVER_PID=$!
cd ..

# Wait a bit for server to start
sleep 3

# Start client server in background
cd client
python3 -m http.server 3000 --bind 0.0.0.0 &
CLIENT_PID=$!
cd ..

# Wait a bit for client to start
sleep 2

# Open browser (if xdg-open is available)
if command -v xdg-open > /dev/null; then
    xdg-open http://localhost:3000
elif command -v open > /dev/null; then
    open http://localhost:3000
fi

echo "Game is running! Server on ws://localhost:8080, Client on http://localhost:3000"
echo "For LAN multiplayer: Tell friends to visit http://[YOUR_LAN_IP]:3000 and enter your LAN IP in the server address field"
echo "Press Ctrl+C to exit (servers will keep running)"

# Wait for user interrupt
trap "echo 'Stopping servers...'; kill $SERVER_PID $CLIENT_PID 2>/dev/null; exit" INT
wait