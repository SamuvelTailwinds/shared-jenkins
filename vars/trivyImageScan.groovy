def call(Map params = [:]) {
    if (!params.containsKey('imageDefinitions') || !params.imageDefinitions) {
        error "'imageDefinitions' parameter is required and should provide a list of image definitions for Trivy image scanning."
    }

    def imageDefinitions = params.imageDefinitions
    String reportDir = params.get('reportDir', 'trivy-reports') // Default report directory

    try {
        sh "mkdir -p ${reportDir}"

        imageDefinitions.each { definition ->
            // Extract image definition details
            def dockerRegistry = definition.get('dockerRegistry', '')
            def baseImageName = definition.imageName
            def imageTag = definition.get('imageTag', 'latest')

            if (!baseImageName) {
                error "Each image definition must have 'imageName'."
            }

            // Construct full image name
            def fullImageName = "${dockerRegistry ? "${dockerRegistry}/" : ''}${baseImageName}:${imageTag}"

            // Generate report file name based on image name
            String sanitizedImageName = baseImageName.replaceAll(/[\/:]/, '_')
            String reportFile = "${reportDir}/${sanitizedImageName}-trivy-report.json"

            println "Scanning image: ${fullImageName}"

            // Run Trivy scan and save the report
            sh """
                trivy --debug image --cache-dir /tmp/trivy-cache --format json --output ${reportFile} ${fullImageName}
            """

            println "Trivy scan completed for ${baseImageName}. Report saved as ${reportFile}."
        }
    } catch (Exception e) {
        println "Error during Trivy scanning: ${e.message}"
        error "Trivy scanning failed."
    }
}
