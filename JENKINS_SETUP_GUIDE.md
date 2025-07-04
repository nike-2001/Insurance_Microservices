# Jenkins CI/CD Pipeline Setup Guide

## Overview
This guide provides comprehensive instructions for setting up and using the Jenkins CI/CD pipeline for the Insurance Project. The pipeline automates building, testing, Docker image creation, and deployment to multiple environments.

## 🏗️ **Pipeline Architecture**

### Pipeline Stages
1. **Checkout** - Clone the repository
2. **Code Quality Check** - SonarQube analysis and security scanning
3. **Build & Test** - Compile and test all microservices in parallel
4. **Package Applications** - Create JAR files for all services
5. **Build Docker Images** - Build and push Docker images to registry
6. **Deploy to Development** - Deploy to dev environment (develop branch)
7. **Integration Tests** - Run integration tests against deployed services
8. **Deploy to Staging** - Deploy to staging environment (main branch)
9. **Deploy to Production** - Manual approval for production deployment

### Services Covered
- Service Registry (Port: 8761)
- Config Server (Port: 9296)
- Cloud Gateway (Port: 9090)
- Policy Service (Port: 8082)
- Product Service (Port: 8083)
- Payment Service (Port: 8081)
- Claim Service (Port: 8084)

## 🚀 **Prerequisites**

### Required Software
- Jenkins 2.387+ with Pipeline plugin
- Docker Engine 20.10+
- Kubernetes CLI (kubectl)
- Maven 3.9.5+
- JDK 21
- Git

### Required Jenkins Plugins
```bash
# Install these plugins via Jenkins Plugin Manager
- Git plugin
- Maven Integration plugin
- Docker plugin
- Kubernetes plugin
- SonarQube Scanner plugin
- Email Extension plugin
- Pipeline plugin
- Blue Ocean plugin
- OWASP Dependency Check plugin
```

## ⚙️ **Jenkins Setup**

### 1. Install Jenkins
```bash
# For Ubuntu/Debian
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install jenkins

# For CentOS/RHEL
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo yum install jenkins
```

### 2. Configure Jenkins Tools
Navigate to **Manage Jenkins > Global Tool Configuration** and configure:

#### JDK Configuration
- **Name**: `JDK-21`
- **JAVA_HOME**: `/usr/lib/jvm/java-21-openjdk`

#### Maven Configuration
- **Name**: `Maven-3.9.5`
- **MAVEN_HOME**: `/opt/maven`

#### Git Configuration
- **Name**: `Default`
- **Path to Git executable**: `git`

### 3. Configure Credentials
Navigate to **Manage Jenkins > Credentials > System > Global credentials** and add:

#### Docker Registry Credentials
- **Kind**: Username with password
- **ID**: `docker-registry-credentials`
- **Username**: `nikhilkorrapati`
- **Password**: Your Docker registry password

#### Kubernetes Cluster Credentials
- **Kind**: Username with password
- **ID**: `k8s-cluster-credentials`
- **Username**: `k8s-admin`
- **Password**: Your Kubernetes service account token

#### SonarQube Token
- **Kind**: Secret text
- **ID**: `sonar-token`
- **Secret**: Your SonarQube token

### 4. Configure SonarQube
Navigate to **Manage Jenkins > Configure System** and add SonarQube server:

- **Name**: `SonarQube`
- **Server URL**: `http://your-sonarqube-server:9000`
- **Server authentication token**: Use the `sonar-token` credential

## 🔧 **Pipeline Configuration**

### 1. Create Pipeline Job
1. Go to **New Item** in Jenkins
2. Select **Pipeline**
3. Name it `insurance-project-pipeline`
4. Configure the following:

#### Pipeline Definition
- **Definition**: Pipeline script from SCM
- **SCM**: Git
- **Repository URL**: `https://github.com/your-org/insurance-project.git`
- **Credentials**: Select your Git credentials
- **Branch Specifier**: `*/main`
- **Script Path**: `Jenkinsfile`

#### Build Triggers
- **Poll SCM**: `H/5 * * * *` (every 5 minutes)
- **GitHub hook trigger for GITScm polling**: Checked

#### Pipeline Triggers
- **GitHub Push Trigger**: Enabled

### 2. Environment Variables
Set these environment variables in the pipeline:

```bash
DOCKER_REGISTRY=nikhilkorrapati
DOCKER_REGISTRY_SPRING=nikhilspring
K8S_NAMESPACE=insurance-project
VERSION=${BUILD_NUMBER}
```

## 🐳 **Docker Configuration**

### 1. Docker Registry Setup
Ensure your Docker registry is accessible and you have push permissions:

```bash
# Login to Docker registry
docker login -u nikhilkorrapati -p your-password

# Test push
docker pull hello-world
docker tag hello-world nikhilkorrapati/test:latest
docker push nikhilkorrapati/test:latest
```

### 2. Docker Build Context
The pipeline builds Docker images from each service directory:
- `service-registry/`
- `ConfigServer/`
- `CloudGateway/`
- `PolicyService/`
- `ProductService/`
- `PaymentService/`
- `ClaimService/`

## ☸️ **Kubernetes Configuration**

### 1. Cluster Access
Ensure kubectl is configured to access your Kubernetes cluster:

```bash
# Test cluster access
kubectl cluster-info
kubectl get nodes
```

### 2. Namespace Setup
The pipeline creates and uses the `insurance-project` namespace:

```bash
# Verify namespace exists
kubectl get namespace insurance-project
```

### 3. Service Account
Create a service account for Jenkins:

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: jenkins-sa
  namespace: insurance-project
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: jenkins-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: jenkins-sa
  namespace: insurance-project
```

## 📊 **Monitoring and Notifications**

### 1. Email Notifications
The pipeline sends email notifications for:
- **Success**: Build completion with service details
- **Failure**: Build failure with error details and console log link

### 2. SonarQube Integration
- Code quality analysis on every build
- Security vulnerability scanning
- Code coverage reporting

### 3. Build Artifacts
The pipeline generates:
- Test reports
- Security scan reports
- Docker images
- Deployment logs

## 🔄 **Branch Strategy**

### Development Workflow
1. **Feature branches** → **develop** → **main**
2. **develop branch**: Automatic deployment to development environment
3. **main branch**: Automatic deployment to staging, manual approval for production

### Pipeline Triggers
- **develop branch**: Full pipeline with dev deployment
- **main branch**: Full pipeline with staging deployment
- **Other branches**: Build and test only

## 🛠️ **Troubleshooting**

### Common Issues

#### 1. Docker Build Failures
```bash
# Check Docker daemon
sudo systemctl status docker

# Check Docker registry access
docker login -u nikhilkorrapati

# Check available disk space
df -h
```

#### 2. Kubernetes Deployment Failures
```bash
# Check cluster status
kubectl get nodes
kubectl get pods -n insurance-project

# Check deployment status
kubectl describe deployment claim-service-app -n insurance-project

# Check logs
kubectl logs deployment/claim-service-app -n insurance-project
```

#### 3. Maven Build Failures
```bash
# Check Maven installation
mvn --version

# Check Java version
java -version

# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository
```

#### 4. SonarQube Issues
```bash
# Check SonarQube server
curl -f http://your-sonarqube-server:9000/api/system/status

# Check authentication
curl -u your-token: http://your-sonarqube-server:9000/api/projects/search
```

### Debug Mode
Enable debug logging in Jenkins:
1. Go to **Manage Jenkins > System Log**
2. Add new log recorder
3. Set log level to **FINE** for pipeline-related loggers

## 📈 **Performance Optimization**

### 1. Parallel Execution
The pipeline uses parallel stages for:
- Building and testing services
- Packaging applications
- Building Docker images

### 2. Caching
- Maven dependencies cached in `.m2/repository`
- Docker layer caching enabled
- Kubernetes image pull policy set to `Always`

### 3. Resource Management
- Docker cleanup after builds
- Workspace cleanup after completion
- Resource limits in Kubernetes deployments

## 🔒 **Security Considerations**

### 1. Credential Management
- Use Jenkins credential store for sensitive data
- Rotate credentials regularly
- Use service accounts for Kubernetes access

### 2. Network Security
- Secure Jenkins with HTTPS
- Use private Docker registries
- Implement network policies in Kubernetes

### 3. Access Control
- Implement role-based access control
- Use Jenkins authorization matrix
- Audit pipeline executions

## 📚 **Additional Resources**

### Documentation
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Kubernetes Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)

### Tools
- [Blue Ocean](https://www.jenkins.io/doc/book/blueocean/) - Modern Jenkins UI
- [SonarQube](https://www.sonarqube.org/) - Code quality platform
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/) - Security scanning

---

## ✅ **Quick Start Checklist**

- [ ] Jenkins installed and configured
- [ ] Required plugins installed
- [ ] Tools configured (JDK, Maven, Git)
- [ ] Credentials added (Docker, Kubernetes, SonarQube)
- [ ] Pipeline job created
- [ ] Docker registry accessible
- [ ] Kubernetes cluster configured
- [ ] SonarQube server running
- [ ] First pipeline run successful

**Pipeline Status**: ✅ **Ready for Production Use** 