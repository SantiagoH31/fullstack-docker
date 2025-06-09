#!/bin/bash

# Script de inicialización para Jenkins con Docker

# Obtener el GID del socket de Docker del host
DOCKER_SOCK_GID=$(stat -c '%g' /var/run/docker.sock 2>/dev/null || echo "999")

echo "Docker socket GID: $DOCKER_SOCK_GID"

# Verificar si el grupo docker existe y ajustar su GID
if getent group docker > /dev/null 2>&1; then
    CURRENT_DOCKER_GID=$(getent group docker | cut -d: -f3)
    echo "Current docker group GID: $CURRENT_DOCKER_GID"
    
    if [ "$CURRENT_DOCKER_GID" != "$DOCKER_SOCK_GID" ]; then
        echo "Updating docker group GID to match socket..."
        sudo groupmod -g "$DOCKER_SOCK_GID" docker
    fi
else
    echo "Creating docker group with GID $DOCKER_SOCK_GID"
    sudo groupadd -g "$DOCKER_SOCK_GID" docker
fi

# Asegurar que jenkins esté en el grupo docker
sudo usermod -aG docker jenkins

# Verificar acceso al socket de Docker
echo "Testing Docker access..."
if docker version > /dev/null 2>&1; then
    echo "✓ Docker access successful"
else
    echo "✗ Docker access failed"
    echo "Socket permissions:"
    ls -la /var/run/docker.sock
    echo "Jenkins groups:"
    groups jenkins
fi

# Ejecutar el entrypoint original de Jenkins
exec /usr/local/bin/jenkins.sh "$@"