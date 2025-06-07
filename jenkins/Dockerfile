FROM jenkins/jenkins:lts

# Cambiar a usuario root para instalar dependencias
USER root

# Instalar Docker CLI
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli

# Instalar Node.js y npm
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

# Copiar lista de plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt

# Instalar plugins
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Volver al usuario jenkins
USER jenkins

# Copiar configuración como código
COPY casc.yaml /var/jenkins_home/casc.yaml

# Exponer puerto
EXPOSE 8080