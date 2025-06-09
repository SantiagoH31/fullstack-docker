#!/bin/bash
# jenkins/docker-entrypoint.sh

set -e

echo "=== Configurando permisos Docker para Jenkins ==="

# Verificar si el socket Docker existe
if [ -S "/var/run/docker.sock" ]; then
    echo "Docker socket encontrado"
    
    # Obtener el GID del grupo docker del host
    DOCKER_GID=$(stat -c '%g' /var/run/docker.sock)
    echo "Docker socket GID: $DOCKER_GID"
    
    # Verificar si ya existe un grupo con ese GID
    EXISTING_GROUP=$(getent group $DOCKER_GID | cut -d: -f1 || echo "")
    
    if [ "$EXISTING_GROUP" = "docker" ]; then
        echo "Grupo docker ya tiene el GID correcto: $DOCKER_GID"
    elif [ -n "$EXISTING_GROUP" ]; then
        echo "Eliminando grupo existente con GID $DOCKER_GID: $EXISTING_GROUP"
        sudo groupdel $EXISTING_GROUP || true
        echo "Modificando grupo docker a GID $DOCKER_GID"
        sudo groupmod -g $DOCKER_GID docker
    else
        echo "Modificando grupo docker a GID $DOCKER_GID"
        sudo groupmod -g $DOCKER_GID docker
    fi
    
    # Asegurar que jenkins está en el grupo docker
    echo "Agregando usuario jenkins al grupo docker"
    sudo usermod -aG docker jenkins
    
    # Verificar permisos
    echo "Verificando configuración:"
    echo "  - Usuario jenkins grupos: $(groups jenkins)"
    echo "  - Docker socket permisos: $(ls -la /var/run/docker.sock)"
    
    # Probar acceso a Docker
    if docker --version > /dev/null 2>&1; then
        echo "✅ Docker CLI accesible"
        if docker ps > /dev/null 2>&1; then
            echo "✅ Docker daemon accesible"
        else
            echo "⚠️  Docker CLI instalado pero daemon no accesible"
        fi
    else
        echo "❌ Docker CLI no accesible"
    fi
else
    echo "⚠️  Docker socket no encontrado en /var/run/docker.sock"
    echo "   Asegúrate de montar el socket en docker-compose.yml"
fi

echo "=== Iniciando Jenkins ==="

# Ejecutar Jenkins con el entrypoint original
exec /usr/bin/tini -- /usr/local/bin/jenkins.sh "$@"