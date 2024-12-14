def call(Map params = [:]) {
    boolean cleanupImages = params.get('cleanupImages', false)

    // Only proceed with cleanup if the flag is true
    if (cleanupImages) {
        try {
            println "Docker image cleanup started..."
            // Remove unused images (dangling images)
            sh 'docker image prune -f'

            // Optionally, remove all images (use with caution)
            // sh 'docker image rm $(docker images -aq) --force'

            println "Docker image cleanup completed."
        } catch (Exception e) {
            println "Error during Docker image cleanup: ${e.message}"
        }
    } else {
        println "Docker image cleanup skipped as cleanupImages is false."
    }
}
