def call(List commands) {
    if (!commands || !(commands instanceof List)) {
        error "'commands' parameter is required and must be a list of shell commands."
    }

    commands.each { cmd ->
        echo "Executing command: ${cmd}"
        sh "${cmd}"
    }
}
