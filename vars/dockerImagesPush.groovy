def call(Map params) {
    if (!params.containsKey('imageDefinitions') || !params.imageDefinitions) {
        error "'imageDefinitions' parameter is required and should provide a list of image definitions."
    }

    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    def imageDefinitions = params.imageDefinitions

    withCredentials([usernamePassword(
        credentialsId: params.registryCredentialsId,
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )]) {
        imageDefinitions.each { definition ->
            def dockerRegistry = definition.dockerRegistry
            def baseImageName = definition.imageName
            def IMAGE_TAG = definition.imageTag

            if (!baseImageName) {
                error "Each image definition must have 'imageName'."
            }

            def tag = "${IMAGE_TAG ?: 'latest'}"
            def fullImageName = "${dockerRegistry}/${baseImageName}:${tag}"

            echo "Pushing image: ${fullImageName} to dockerhub"

            // def buildArgsString = buildArgs.collect { "--build-arg ${it}" }.join(' ')
            sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"

            echo "Pushing image: ${fullImageName}"
            sh "docker push ${fullImageName}"
        }
    }
}
