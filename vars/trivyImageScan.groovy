def call(Map params = [:]) {
    // Validate required parameters
    if (!params.containsKey('imageName') || !params.imageName) {
        error "'imageName' parameter is required for Trivy image scanning."
    }

    String imageName = params.imageName
    // String dockerRegistry = params.dockerRegistry
    // String imageTag = params.get('imageTag', 'latest') // Default to 'latest' tag
    String reportDir = params.get('reportDir', 'trivy-reports') // Default report directory

    try {
        // Create report directory if it doesn't exist
        sh "mkdir -p ${reportDir}"

        // Construct full image name
        def fullImageName = "${imageName}:latest"

        // Generate report file name based on image name
        String sanitizedImageName = imageName.replaceAll(/[\/:]/, '_')
        String reportFile = "${reportDir}/${sanitizedImageName}-trivy-report.json"

        println "Scanning image: ${fullImageName}"

        // Run Trivy scan and save the report
        sh """
            trivy --debug image --cache-dir /tmp/trivy-cache --download-db-only --timeout 20m \
            --scanners vuln --severity HIGH,CRITICAL --format json --output ${reportFile} ${fullImageName}
        """

        println "Trivy scan completed for ${imageName}. Report saved as ${reportFile}."
    } catch (Exception e) {
        println "Error during Trivy scanning: ${e.message}"
        error "Trivy scanning failed."
    }
}
