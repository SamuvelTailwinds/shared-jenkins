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
        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
        imageDefinitions.each { definition ->
            def dockerRegistry = definition.dockerRegistry
            def baseImageName = definition.imageName
            def IMAGE_TAG_1 = definition.imageTag_1
            def IMAGE_TAG_2 = definition.imageTag_2

            if (!baseImageName) {
                error "Each image definition must have 'imageName'."
            }

            def fullImageName_1 = "${dockerRegistry}/${baseImageName}:${IMAGE_TAG_1}"
            def fullImageName_2 = "${dockerRegistry}/${baseImageName}:${IMAGE_TAG_2}"


            // def buildArgsString = buildArgs.collect { "--build-arg ${it}" }.join(' ')
            echo "Tagging images: ${fullImageName_1} to dockerhub"
            sh "docker tag ${dockerRegistry}/${baseImageName}:latest ${fullImageName_1}"
            echo "Pushing image: ${fullImageName_1}"
            sh "docker push ${fullImageName_1}"

            echo "Tagging images: ${fullImageName_2} to dockerhub"
            sh "docker tag ${dockerRegistry}/${baseImageName}:latest ${fullImageName_2}"
            echo "Pushing image: ${fullImageName_2}"
            sh "docker push ${fullImageName_2}"
        }
    }
}
