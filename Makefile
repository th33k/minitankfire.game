.PHONY: build run clean server client

# Build the project
build:
	@echo "Building Mini Tank Fire Game..."
	cd server && mvn clean compile
	@echo "Build complete!"

# Run the game (server and client)
run: server client

# Start the server
server:
	@echo "Starting server..."
	cd server && mvn exec:java &
	@echo "Server started on ws://localhost:8080"

# Start the client
client:
	@echo "Starting client server..."
	cd client && python3 -m http.server 3000 --bind 0.0.0.0 &
	@echo "Client server started on http://localhost:3000"
	@echo "Open http://localhost:3000 in your browser"

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	cd server && mvn clean
	@echo "Clean complete!"

# Stop running processes (Unix-like systems)
stop:
	@echo "Stopping servers..."
	-pkill -f "mvn exec:java"
	-pkill -f "python3 -m http.server"
	@echo "Servers stopped"