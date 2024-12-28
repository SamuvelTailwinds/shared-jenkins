def call(Map params) {
    // Validate required parameters
    if (!params.containsKey('imageName') || !params.imageName) {
        error "'imageName' parameter is required and should specify the Docker image name."
    }
    if (!params.containsKey('imageTag') || !params.imageTag) {
        error "'imageTag' parameter is required and should specify the Docker Image Tag."
    }
    if (!params.containsKey('dockerRegistry') || !params.dockerRegistry) {
        error "'dockerRegistry' parameter is required and should specify the Docker Registry."
    }
    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    // Set default values for optional parameters
    def imageName = params.imageName
    def imageTag = params.imageTag
    def dockerRegistry = params.dockerRegistry

    def fullImageName = "${dockerRegistry}/${imageName}:${imageTag}"

    // Docker build and push process
    withCredentials([usernamePassword(
        credentialsId: params.registryCredentialsId,
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )]) {
        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"

        sh "docker tag ${imageName}:latest ${fullImageName} "

        echo "Pushing Docker image: ${fullImageName}"
        sh "docker push ${fullImageName}"
    }
}
