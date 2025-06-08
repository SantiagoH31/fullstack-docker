pipelineJob('fullstack-deploy') {
    displayName('Fullstack - Complete Deployment')
    description('Pipeline completo para desplegar toda la aplicación con Docker Compose')
    
    triggers {
        upstream('frontend-build-deploy,backend-build-deploy', 'SUCCESS')
    }
    
    definition {
        cps {
            script('''
pipeline {
    agent any
    
    environment {
        COMPOSE_PROJECT_NAME = 'fullstack-app'
    }
    
    stages {
        stage('Checkout Main Repo') {
            steps {
                git branch: 'main', 
                    credentialsId: 'github-credentials',
                    url: 'https://github.com/SantiagoH31/fullstack-docker.git'
            }
        }
        
        stage('Stop Existing Services') {
            steps {
                script {
                    sh """
                        docker compose down || true
                        docker system prune -f
                    """
                }
            }
        }
        
        stage('Deploy Services') {
            steps {
                script {
                    sh """
                        echo "=== Starting database ==="
                        docker compose up -d db
                        
                        echo "=== Waiting for database to be ready ==="
                        sleep 10
                        
                        echo "=== Starting backend ==="
                        docker compose up -d node-app
                        
                        echo "=== Waiting for backend to be ready ==="
                        sleep 5
                        
                        echo "=== Starting frontend ==="
                        docker compose up -d frontend
                        
                        echo "=== Checking services status ==="
                        docker compose ps
                    """
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sleep 30
                    sh """
                        echo "=== Health check ==="
                        curl -f http://localhost:3000/health || echo "Backend health check failed"
                        curl -f http://localhost:4200 || echo "Frontend health check failed"
                    """
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker compose logs --tail=50'
        }
        failure {
            sh 'docker compose down'
        }
    }
}
            ''')
        }
    }
}

// Job para monitoreo de logs
job('monitor-logs') {
    displayName('Monitor - Application Logs')
    description('Job para monitorear logs de la aplicación')
    
    triggers {
        cron('H/10 * * * *')
    }
    
    steps {
        shell('''
            echo "=== Checking running containers ==="
            docker ps
            
            echo "=== Recent logs from all services ==="
            docker compose logs --tail=20 --since=10m
            
            echo "=== Checking disk space ==="
            df -h
            
            echo "=== Docker system info ==="
            docker system df
        ''')
    }
}