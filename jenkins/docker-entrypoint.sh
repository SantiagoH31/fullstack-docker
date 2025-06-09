#!/bin/bash

# Función para logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log "Iniciando configuración de Jenkins con Docker..."

# Verificar si el socket de Docker existe
if [ -S /var/run/docker.sock ]; then
    log "Socket de Docker encontrado"
    
    # Obtener el GID del socket de Docker del host
    DOCKER_SOCK_GID=$(stat -c '%g' /var/run/docker.sock)
    log "GID del socket Docker: $DOCKER_SOCK_GID"
    
    # Verificar si el grupo docker ya existe con el GID correcto
    if ! getent group $DOCKER_SOCK_GID >/dev/null; then
        log "Creando/modificando grupo docker con GID $DOCKER_SOCK_GID"
        # Si el grupo docker existe pero con diferente GID, modificarlo
        if getent group docker >/dev/null; then
            sudo groupmod -g $DOCKER_SOCK_GID docker
        else
            sudo groupadd -g $DOCKER_SOCK_GID docker
        fi
    else
        log "Grupo con GID $DOCKER_SOCK_GID ya existe"
        # Verificar si jenkins ya está en el grupo
        if ! id -nG jenkins | grep -qw docker; then
            log "Agregando jenkins al grupo docker"
            sudo usermod -aG docker jenkins
        fi
    fi
    
    # Asegurar que jenkins esté en el grupo correcto
    sudo usermod -aG $DOCKER_SOCK_GID jenkins
    
    # Verificar permisos
    if [ -r /var/run/docker.sock ] && [ -w /var/run/docker.sock ]; then
        log "Permisos de Docker socket configurados correctamente"
    else
        log "Advertencia: Permisos de Docker socket pueden no estar configurados correctamente"
    fi
    
    # Verificar que Docker CLI funciona
    if docker --version >/dev/null 2>&1; then
        log "Docker CLI funcionando correctamente"
    else
        log "Advertencia: Docker CLI no está funcionando"
    fi
else
    log "Advertencia: Socket de Docker no encontrado en /var/run/docker.sock"
fi

# Configurar variables de entorno para Jenkins
export JENKINS_OPTS="${JENKINS_OPTS:-}"
export JAVA_OPTS="${JAVA_OPTS:-} -Djenkins.install.runSetupWizard=false"

log "Iniciando Jenkins..."

# Ejecutar Jenkins con el entrypoint original
exec /usr/local/bin/jenkins.sh "$@"