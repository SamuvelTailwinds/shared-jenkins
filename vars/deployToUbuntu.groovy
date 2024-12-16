def call(Map params) {
    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    def targetHost = params.targetHost
    def vmCredentials = params.vmCredentials
    def artifactPath = params.artifactPath

    try {
        withCredentials([usernamePassword(
            credentialsId: params.registryCredentialsId,
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sshagent(credentials: ([vmCredentials])) {
                sh """
                    # Copy artifact to target VM
                    scp -o StrictHostKeyChecking=no ${artifactPath} ubuntu@${targetHost}:/tmp/

                    # SSH to target and deploy
                    ssh -o StrictHostKeyChecking=no ubuntu@${targetHost} << 'EOF'
                    echo "Starting deployment process..."

                    # Unzip the artifact
                    mkdir -p /tmp/majordomo
                    unzip /tmp/qa-testing-docker-compose.zip -d /tmp/majordomo
                    cd /tmp/majordomo

                    # Update .env file
                    sed -i "s|HOST_NAME=.*|HOST_NAME=${targetHost}}|" .env

                    # Docker login
                    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

                    # Start docker-compose
                    docker-compose up -d

                    # Wait and verify services
                    sleep 60
                    if docker-compose ps | grep -q 'Up'; then
                        echo "Services are running."
                    else
                        echo "Services not running; restarting..."
                        docker-compose restart
                    fi
                    EOF
                """
            }
        }
    } catch (Exception e) {
        error "Deployment failed: ${e.message}"
    }
}
