pipeline {
    agent any
    
    environment {
        // Docker registry configuration
        DOCKER_REGISTRY = 'nikhilkorrapati'
        DOCKER_REGISTRY_SPRING = 'nikhilspring'
        
        // Application versions
        VERSION = "${env.BUILD_NUMBER}"
        
        // Service names and ports
        SERVICES = [
            'serviceregistry': ['port': '8761', 'registry': 'nikhilspring'],
            'configserver': ['port': '9296', 'registry': 'nikhilspring'],
            'cloudgateway': ['port': '9090', 'registry': 'nikhilspring'],
            'policyservice': ['port': '8082', 'registry': 'nikhilkorrapati'],
            'productservice': ['port': '8083', 'registry': 'nikhilkorrapati'],
            'paymentservice': ['port': '8081', 'registry': 'nikhilkorrapati'],
            'claimservice': ['port': '8084', 'registry': 'nikhilkorrapati']
        ]
        
        // Kubernetes configuration
        K8S_NAMESPACE = 'insurance-project'
        
        // Maven configuration
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }
    
    tools {
        maven 'Maven-3.9.5'
        jdk 'JDK-21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "Starting build for Insurance Project - Build #${env.BUILD_NUMBER}"
                checkout scm
            }
        }
        
        stage('Code Quality Check') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        expression { env.SONAR_TOKEN != null }
                    }
                    steps {
                        echo "Running SonarQube analysis..."
                        withSonarQubeEnv('SonarQube') {
                            sh """
                                mvn clean verify sonar:sonar \
                                    -Dsonar.projectKey=insurance-project \
                                    -Dsonar.projectName='Insurance Project' \
                                    -Dsonar.projectVersion=${VERSION}
                            """
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        echo "Running security scan with OWASP dependency check..."
                        sh """
                            mvn org.owasp:dependency-check-maven:check \
                                -Dformat=HTML \
                                -Dformat=JSON \
                                -DprettyPrint
                        """
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Service Registry') {
                    steps {
                        dir('service-registry') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Config Server') {
                    steps {
                        dir('ConfigServer') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Cloud Gateway') {
                    steps {
                        dir('CloudGateway') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Policy Service') {
                    steps {
                        dir('PolicyService') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Product Service') {
                    steps {
                        dir('ProductService') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Payment Service') {
                    steps {
                        dir('PaymentService') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
                
                stage('Claim Service') {
                    steps {
                        dir('ClaimService') {
                            sh 'mvn clean compile test'
                        }
                    }
                }
            }
        }
        
        stage('Package Applications') {
            parallel {
                stage('Package Service Registry') {
                    steps {
                        dir('service-registry') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Config Server') {
                    steps {
                        dir('ConfigServer') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Cloud Gateway') {
                    steps {
                        dir('CloudGateway') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Policy Service') {
                    steps {
                        dir('PolicyService') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Product Service') {
                    steps {
                        dir('ProductService') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Payment Service') {
                    steps {
                        dir('PaymentService') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                
                stage('Package Claim Service') {
                    steps {
                        dir('ClaimService') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build Service Registry Image') {
                    steps {
                        dir('service-registry') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY_SPRING}/serviceregistry:${VERSION} -t ${DOCKER_REGISTRY_SPRING}/serviceregistry:latest .
                                docker push ${DOCKER_REGISTRY_SPRING}/serviceregistry:${VERSION}
                                docker push ${DOCKER_REGISTRY_SPRING}/serviceregistry:latest
                            """
                        }
                    }
                }
                
                stage('Build Config Server Image') {
                    steps {
                        dir('ConfigServer') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY_SPRING}/configserver:${VERSION} -t ${DOCKER_REGISTRY_SPRING}/configserver:latest .
                                docker push ${DOCKER_REGISTRY_SPRING}/configserver:${VERSION}
                                docker push ${DOCKER_REGISTRY_SPRING}/configserver:latest
                            """
                        }
                    }
                }
                
                stage('Build Cloud Gateway Image') {
                    steps {
                        dir('CloudGateway') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY_SPRING}/cloudgateway:${VERSION} -t ${DOCKER_REGISTRY_SPRING}/cloudgateway:latest .
                                docker push ${DOCKER_REGISTRY_SPRING}/cloudgateway:${VERSION}
                                docker push ${DOCKER_REGISTRY_SPRING}/cloudgateway:latest
                            """
                        }
                    }
                }
                
                stage('Build Policy Service Image') {
                    steps {
                        dir('PolicyService') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/policyservice:${VERSION} -t ${DOCKER_REGISTRY}/policyservice:latest .
                                docker push ${DOCKER_REGISTRY}/policyservice:${VERSION}
                                docker push ${DOCKER_REGISTRY}/policyservice:latest
                            """
                        }
                    }
                }
                
                stage('Build Product Service Image') {
                    steps {
                        dir('ProductService') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/productservice:${VERSION} -t ${DOCKER_REGISTRY}/productservice:latest .
                                docker push ${DOCKER_REGISTRY}/productservice:${VERSION}
                                docker push ${DOCKER_REGISTRY}/productservice:latest
                            """
                        }
                    }
                }
                
                stage('Build Payment Service Image') {
                    steps {
                        dir('PaymentService') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/paymentservice:${VERSION} -t ${DOCKER_REGISTRY}/paymentservice:latest .
                                docker push ${DOCKER_REGISTRY}/paymentservice:${VERSION}
                                docker push ${DOCKER_REGISTRY}/paymentservice:latest
                            """
                        }
                    }
                }
                
                stage('Build Claim Service Image') {
                    steps {
                        dir('ClaimService') {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/claimservice:${VERSION} -t ${DOCKER_REGISTRY}/claimservice:latest .
                                docker push ${DOCKER_REGISTRY}/claimservice:${VERSION}
                                docker push ${DOCKER_REGISTRY}/claimservice:latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            parallel {
                stage('Deploy to Docker Compose (Dev)') {
                    steps {
                        echo "Deploying to development environment using Docker Compose..."
                        sh """
                            docker-compose down
                            docker-compose pull
                            docker-compose up -d
                            sleep 30
                            docker-compose ps
                        """
                    }
                }
                
                stage('Deploy to Kubernetes (Dev)') {
                    steps {
                        echo "Deploying to development Kubernetes cluster..."
                        sh """
                            kubectl apply -f K8s/namespace.yaml
                            kubectl apply -f K8s/ -n ${K8S_NAMESPACE}
                            kubectl rollout status deployment/mysql -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status statefulset/eureka -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/config-server-app -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/cloud-gateway-app -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/policy-service-app -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/product-service-app -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/payment-service-app -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl rollout status deployment/claim-service-app -n ${K8S_NAMESPACE} --timeout=300s
                        """
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                branch 'develop'
            }
            steps {
                echo "Running integration tests..."
                sh """
                    # Wait for services to be ready
                    sleep 60
                    
                    # Test service health endpoints
                    curl -f http://localhost:8761/actuator/health || exit 1
                    curl -f http://localhost:9296/actuator/health || exit 1
                    curl -f http://localhost:9090/actuator/health || exit 1
                    curl -f http://localhost:8082/actuator/health || exit 1
                    curl -f http://localhost:8083/actuator/health || exit 1
                    curl -f http://localhost:8081/actuator/health || exit 1
                    curl -f http://localhost:8084/actuator/health || exit 1
                    
                    echo "All services are healthy!"
                """
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                echo "Deploying to staging environment..."
                sh """
                    kubectl apply -f K8s/namespace.yaml
                    kubectl apply -f K8s/ -n ${K8S_NAMESPACE}-staging
                """
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
                beforeAgent true
            }
            input {
                message "Deploy to production?"
                ok "Deploy"
            }
            steps {
                echo "Deploying to production environment..."
                sh """
                    kubectl apply -f K8s/namespace.yaml
                    kubectl apply -f K8s/ -n ${K8S_NAMESPACE}-prod
                """
            }
        }
    }
    
    post {
        always {
            echo "Build completed with status: ${currentBuild.result}"
            sh """
                docker system prune -f
                docker image prune -f
            """
        }
        
        success {
            echo "Build successful! Insurance Project deployed successfully."
            emailext (
                subject: "Insurance Project Build #${env.BUILD_NUMBER} - SUCCESS",
                body: """
                    <h2>Insurance Project Build Successful</h2>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                    <p><strong>Commit:</strong> ${env.GIT_COMMIT}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    
                    <h3>Services Deployed:</h3>
                    <ul>
                        <li>Service Registry (Port: 8761)</li>
                        <li>Config Server (Port: 9296)</li>
                        <li>Cloud Gateway (Port: 9090)</li>
                        <li>Policy Service (Port: 8082)</li>
                        <li>Product Service (Port: 8083)</li>
                        <li>Payment Service (Port: 8081)</li>
                        <li>Claim Service (Port: 8084)</li>
                    </ul>
                """,
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        
        failure {
            echo "Build failed! Please check the logs for details."
            emailext (
                subject: "Insurance Project Build #${env.BUILD_NUMBER} - FAILED",
                body: """
                    <h2>Insurance Project Build Failed</h2>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                    <p><strong>Commit:</strong> ${env.GIT_COMMIT}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>Console Output:</strong> <a href="${env.BUILD_URL}console">Console Log</a></p>
                    
                    <p>Please check the build logs and fix the issues.</p>
                """,
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        
        cleanup {
            echo "Cleaning up workspace..."
            cleanWs()
        }
    }
} 