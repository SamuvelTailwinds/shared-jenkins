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
            // def IMAGE_TAG_1 = definition.imageTag_1
            // def IMAGE_TAG_2 = definition.imageTag_2
            def contextPath = definition.contextPath
            def dockerfilePath = definition.dockerfilePath

            if (!baseImageName || !contextPath) {
                error "Each image definition must have 'imageName' and 'contextPath'."
            }

            // def tag = "${IMAGE_TAG ?: 'latest'}"
            def fullImageName = "${dockerRegistry}/${baseImageName}:latest"

            echo "Building image: ${baseImageName}:latest from context: ${contextPath} with Dockerfile: ${dockerfilePath}"

            // def buildArgsString = buildArgs.collect { "--build-arg ${it}" }.join(' ')
            // sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
            sh "docker build -f ${dockerfilePath} -t ${fullImageName} ${contextPath}"

            echo " ${baseImageName}:latest Image Build is ready"

        }
    }
}
