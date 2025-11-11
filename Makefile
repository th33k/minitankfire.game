# Makefile for Tank Game Project

# Variables
CLASSPATH = server/target/classes;shared/target/classes;client/target/classes
SERVER_CLASS = com.tankgame.server.TankGameServer
CLIENT_CLASS = com.tankgame.client.TankGameClient

# Default target: build the project
all: build

# Build the entire project
build:
	mvn clean package

# Clean build artifacts
clean:
	mvn clean

# Run the server
server: build
	java -cp "$(CLASSPATH)" $(SERVER_CLASS)

# Run the client
client: build
	java -cp "$(CLASSPATH)" $(CLIENT_CLASS) $(ARGS)

# Run the web client server
client-ui:
	cd client && python -m http.server 3000

.PHONY: all build clean server client client-ui