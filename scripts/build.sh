#!/bin/bash
echo "Building Tank Game Server..."

# Build server
cd server
mvn clean compile
cd ..

echo "Build complete!"