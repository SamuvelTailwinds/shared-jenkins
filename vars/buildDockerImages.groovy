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
            def contextPath = definition.contextPath
            def dockerfilePath = definition.get('dockerfilePath', 'Dockerfile')

            if (!baseImageName || !contextPath) {
                error "Each image definition must have 'imageName' and 'contextPath'."
            }

            def tag = "${IMAGE_TAG ?: 'latest'}"
            def fullImageName = "${dockerRegistry}/${baseImageName}:${tag}"

            echo "Building image: ${fullImageName} from context: ${contextPath} with Dockerfile: ${dockerfilePath}"

            // def buildArgsString = buildArgs.collect { "--build-arg ${it}" }.join(' ')
            sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
            sh "docker build -t ${fullImageName} -f ${contextPath}/${dockerfilePath} ."

        }
    }
}
