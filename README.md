# Demo to Get Current Location

## Instructions

To create your own project based off this repo, follow the instructions below

1. Create a project in GCP (https://console.cloud.google.com)
2. Enable Maps SDK and generate API key
3. Add the API key to your local.properties file
4. Enable Secrets Grade Plugin
   - Refer to top level build.gradle and app level build.gradle file
   - Refer to AndroidManifest.xml file
5. Make sure to enable viewBindings and add the google dependencies
   - Refer to app level build.gradle file
6. Review the PlacesFragment.kt file to see how the current location request works