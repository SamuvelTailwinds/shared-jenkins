def call(Map params) {
    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    if (!params.containsKey('targetHost') || !params.targetHost) {
        error "'targetHost' parameter is required and should specify the target VM's hostname or IP."
    }

    if (!params.containsKey('vmCredentials') || !params.vmCredentials) {
        error "'vmCredentials' parameter is required and should specify the Jenkins SSH credentials ID for the target VM."
    }

    if (!params.containsKey('artifactPath') || !params.artifactPath) {
        error "'artifactPath' parameter is required and should specify the path to the artifact to be deployed."
    }

    def targetHost = params.targetHost
    def vmCredentials = params.vmCredentials
    def artifactPath = params.artifactPath
    def releaseTag = params.releaseTag

    try {
        withCredentials([usernamePassword(
            credentialsId: params.registryCredentialsId,
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sshagent(credentials: [vmCredentials]) {
                sh """
                    # Copy artifact to target VM
                    scp -o StrictHostKeyChecking=no ${artifactPath} ubuntu@${targetHost}:/tmp/ || {
                        echo "Error: Failed to copy artifact to the target host."
                        exit 1
                    }

                    # SSH to target and deploy
                    ssh -o StrictHostKeyChecking=no ubuntu@${targetHost} << 'EOF'
                    set -e  # Exit on any error

                    echo "Starting deployment process..."
                    
                    # Unzip the artifact
                    sudo mkdir -p /tmp/majordomo
                    ls -a /tmp/majordomo/
                    sudo unzip /tmp/${releaseTag}-docker-compose.zip -d /tmp/majordomo || {
                        echo "Error: Failed to unzip artifact."
                        exit 1
                    }

                    cd /tmp/majordomo

                    # Update .env file
                    sudo sed -i "s|HOST_NAME=.*|HOST_NAME=${targetHost}|" .env || {
                        echo "Error: Failed to update .env file."
                        exit 1
                    }

                    # Docker login
                    sudo docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD || {
                        echo "Error: Docker login failed."
                        exit 1
                    }

                    # Start docker-compose
                    sudo docker-compose up -d || {
                        echo "Error: Failed to start docker-compose services."
                        exit 1
                    }

                    # Wait and verify services
                    sleep 60
                    if sudo docker-compose ps | grep -q 'Up'; then
                        echo "Services are running successfully."
                    else
                        echo "Services not running; attempting to restart..."
                        sudo docker-compose restart || {
                            echo "Error: Restarting services failed."
                            exit 1
                        }
                    fi

                    echo "Deployment completed successfully."
                    EOF
                """
            }
        }
    } catch (Exception e) {
        error "Deployment failed with the following error: ${e.message}"
    }
}
