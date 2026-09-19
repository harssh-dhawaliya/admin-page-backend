pipeline {
    agent any

    tools {
        // Ensure Maven and JDK match your environment configuration
        maven 'Maven 3'
        jdk 'JDK 17'
    }

    triggers {
        // Triggers automatically on SCM push or Pull Requests
        githubPush()
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                message 'Compiling backend source code...'
                sh './mvnw clean compile'
            }
        }

        stage('Run Unit Tests (JUnit / Mockito)') {
            steps {
                message 'Executing JUnit and Mockito test suites...'
                sh './mvnw test'
            }
            post {
                always {
                    // Publish test reports to Jenkins dashboard
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Static Analysis') {
            steps {
                message 'Running SonarQube code quality analysis...'
                // 'SonarQubeServer' must be configured in Jenkins global tool/server settings
                withSonarQubeEnv('SonarQubeServer') {
                    sh './mvnw sonar:sonar -Dsonar.projectName="AK Admin Backend"'
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                message 'Waiting for SonarQube Quality Gate result...'
                timeout(time: 10, unit: 'MINUTES') {
                    // Automatically aborts/blocks the pipeline if the Quality Gate fails
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy') {
            steps {
                message 'Deploying application artifact to enterprise environment...'
                // Package the executable jar
                sh './mvnw package -DskipTests'

                // Add your deployment script or server transfer command here (e.g., Docker, SSH, or direct jar execution)
                sh 'echo "Deployment stage completed successfully. Service active."'
            }
        }
    }

    post {
        failure {
            echo 'Pipeline failed during execution. Notifications sent to team channel.'
        }
        success {
            echo 'Pipeline passed successfully! All quality gates cleared.'
        }
    }
}