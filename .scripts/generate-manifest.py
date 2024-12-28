import sys
import json

manifestTemplateFilePath = sys.argv[1]
manifestFilePath = sys.argv[2]
versionName = sys.argv[3]
versionCode = sys.argv[4]
releaseNotesFilePath = sys.argv[5]

with open(manifestTemplateFilePath) as file:
    manifestTemplate = file.read()

with open(releaseNotesFilePath) as file:
    releaseNotes = file.read()

releaseNotes = json.dumps(releaseNotes.replace("\r", "").replace("\n", "\\n"))

manifest = manifestTemplate.replace("__VERSION_NAME__", versionName).replace("__VERSION_CODE__", versionCode).replace("__RELEASE_NOTES__", releaseNotes)

with open(manifestFilePath, "w") as file:
    file.write(manifest)
