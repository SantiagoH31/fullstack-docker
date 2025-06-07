# 🚀 Proyecto Fullstack con Jenkins CI/CD

Aplicación completa con **Angular Frontend**, **Node.js Backend**, **PostgreSQL** y **Jenkins** para CI/CD automático.

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Angular       │    │   Node.js       │    │   PostgreSQL    │
│   Frontend      │───▶│   Backend       │───▶│   Database      │
│   Port: 4200    │    │   Port: 3000    │    │   Port: 5432    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │    Jenkins      │
                    │   CI/CD Server  │
                    │   Port: 8080    │
                    └─────────────────┘
```

## 📦 Prerequisitos

- **Docker** >= 24.0
- **Docker Compose** >= 2.0
- **Git** >= 2.30
- **Acceso a GitHub** (para los submódulos)

### Verificar instalación:
```bash
docker --version
docker-compose --version
git --version
```

## 🛠️ Instalación

### 1. Clonar el repositorio principal
```bash
git clone https://github.com/tu-usuario/tu-proyecto-principal.git
cd tu-proyecto-principal
```

### 2. Inicializar y actualizar submódulos
```bash
# Inicializar submódulos
git submodule init

# Descargar contenido de los submódulos
git submodule update

# O hacer ambos en un comando
git submodule update --init --recursive
```

### 3. Verificar estructura
```bash
ls -la
# Deberías ver:
# - front-Docker/  (submódulo del frontend)
# - back-Docker/   (submódulo del backend)
# - jenkins/       (configuración CI/CD)
# - db/           (scripts de base de datos)
# - docker-compose.yml
```

## 🚀 Ejecución

### Inicio Completo
```bash
# Construir e iniciar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f
```

### Inicio por Etapas (Recomendado para primera vez)
```bash
# 1. Iniciar base de datos primero
docker-compose up -d db
sleep 10

# 2. Iniciar backend
docker-compose up -d node-app  
sleep 5

# 3. Iniciar frontend
docker-compose up -d frontend

# 4. Iniciar Jenkins
docker-compose up -d jenkins

# 5. Verificar estado
docker-compose ps
```

### 📍 URLs de Acceso
- **Frontend**: http://localhost:4200
- **Backend**: http://localhost:3000
- **Database**: localhost:5432
- **Jenkins**: http://localhost:8080

### 🔐 Credenciales por Defecto
- **Jenkins**: `admin` / `admin123` (o tu password del .env)
- **PostgreSQL**: `postgres` / `example`

## 🤖 Jenkins CI/CD

### Primera Configuración
1. **Acceder a Jenkins**: http://localhost:8080
2. **Login**: admin / admin123
3. Los jobs se crean automáticamente al iniciar

### Jobs Disponibles
```
📋 Jobs Creados Automáticamente:
├── seed-job                    # Crea todos los otros jobs
├── frontend-build-deploy       # Pipeline completo del frontend
├── frontend-simple-build       # Build simple del frontend  
├── backend-build-deploy        # Pipeline completo del backend
├── backend-simple-build        # Build simple del backend
├── fullstack-deploy           # Despliegue completo de la app
└── monitor-logs               # Monitoreo del sistema
```

### Flujo de CI/CD Automático
```
1. Push a front-Docker   →  frontend-build-deploy
2. Push a back-Docker    →  backend-build-deploy  
3. Ambos exitosos        →  fullstack-deploy (automático)
4. Aplicación actualizada →  Health checks
```

### Ejecutar Jobs Manualmente
1. Ve a Jenkins → Selecciona job
2. **"Build Now"**
3. Ve logs en **"Console Output"**

## 👨‍💻 Desarrollo

### Trabajar con Submódulos

#### Actualizar submódulos a última versión:
```bash
git submodule update --remote
```

#### Trabajar en un submódulo específico:
```bash
# Entrar al submódulo
cd front-Docker

# Crear/cambiar rama
git checkout -b mi-nueva-feature

# Hacer cambios...
git add .
git commit -m "Nueva funcionalidad"
git push origin mi-nueva-feature

# Volver al proyecto principal
cd ..

# Actualizar referencia del submódulo
git add front-Docker
git commit -m "Actualizar submódulo frontend"
git push
```

#### Clonar proyecto para otro desarrollador:
```bash
# Clonar con submódulos en un comando
git clone --recurse-submodules https://github.com/tu-usuario/tu-proyecto-principal.git

# O si ya clonaste:
git submodule update --init --recursive
```

### Desarrollo Local (sin Docker)
```bash
# Frontend
cd front-Docker
npm install
npm start     # http://localhost:4200

# Backend (en otra terminal)
cd back-Docker  
npm install
npm run dev   # http://localhost:3000
```

## 🔧 Troubleshooting

### Problema: Submódulos vacíos
```bash
# Solución:
git submodule update --init --recursive
```

### Problema: Jenkins no inicia
```bash
# Ver logs:
docker-compose logs jenkins

# Reiniciar:
docker-compose restart jenkins
```

### Problema: Jobs no se crean automáticamente
```bash
# Ejecutar seed-job manualmente:
# Jenkins → seed-job → Build Now
```

### Problema: Error de permisos de Docker
```bash
# Verificar permisos:
docker exec -it jenkins-server bash
groups jenkins  # Debe incluir 'docker'
```

### Problema: Base de datos no conecta
```bash
# Verificar estado:
docker-compose ps db

# Ver logs:
docker-compose logs db

# Reiniciar:
docker-compose restart db
```

### Problema: Puerto ocupado
```bash
# Ver qué usa el puerto:
lsof -i :8080
lsof -i :4200
lsof -i :3000

# Cambiar puertos en docker-compose.yml si es necesario
```

## 📚 Comandos Útiles

### Docker Compose
```bash
# Iniciar servicios
docker-compose up -d

# Parar servicios
docker-compose down

# Parar y eliminar volúmenes
docker-compose down -v

# Ver logs
docker-compose logs -f [servicio]

# Reconstruir imágenes
docker-compose build --no-cache

# Ver estado
docker-compose ps
```

### Git Submódulos
```bash
# Ver estado de submódulos
git submodule status

# Actualizar todos los submódulos
git submodule update --remote

# Actualizar un submódulo específico
git submodule update --remote front-Docker

# Push con submódulos
git push --recurse-submodules=on-demand
```

### Jenkins
```bash
# Reiniciar Jenkins
docker-compose restart jenkins

# Ver logs de Jenkins
docker-compose logs -f jenkins

# Acceder al contenedor
docker exec -it jenkins-server bash
```

### Limpiar Sistema
```bash
# Limpiar Docker
docker system prune -a

# Limpiar volúmenes
docker volume prune

# Reinicio completo
docker-compose down -v
docker system prune -a
docker-compose up -d
```


