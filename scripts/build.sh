#!/bin/bash
echo "Building Mini Tank Fire Game..."

# Build server
cd server
mvn clean compile
cd ..

echo "Build complete!"