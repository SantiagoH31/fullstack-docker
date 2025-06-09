FROM jenkins/jenkins:lts

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

# Instalar sudo para el script de inicialización
RUN apt-get install -y sudo

# Crear grupo docker y agregar usuario jenkins
RUN groupadd -f docker && \
    usermod -aG docker jenkins

# Dar permisos sudo limitados al usuario jenkins para gestión de grupos
RUN echo "jenkins ALL=(root) NOPASSWD: /usr/sbin/groupmod, /usr/sbin/groupadd, /usr/sbin/usermod" >> /etc/sudoers

# Copiar lista de plugins y configuraciones
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY casc.yaml /usr/share/jenkins/ref/casc.yaml

# Instalar plugins
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copiar script de inicialización
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Habilitar configuración como código
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml

USER jenkins

EXPOSE 8080

# Usar el script personalizado como entrypoint
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]