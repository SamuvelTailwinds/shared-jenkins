{{/*
Define a common set of labels to be used in your chart
*/}}
{{- define "crud-app.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ .Chart.Version | quote }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Generate a full name for resources
*/}}
{{- define "crud-app.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Define the service account name
*/}}
{{- define "crud-app.serviceAccountName" -}}
{{- printf "%s-sa" (include "crud-app.fullname" .) -}}
{{- end -}}

{{/*
Define the name of the application
*/}}
{{- define "crud-app.name" -}}
{{ .Chart.Name }}
{{- end -}}
