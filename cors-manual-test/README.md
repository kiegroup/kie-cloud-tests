# cors-manual-test
Helper project for manual testing of CORS headers in Kie server.
This project builds and deploys war file containing sample page and AJAX script calling Kie server REST endpoint, showing the response.
In case of error the popup dialog is shown with error code and message.

The application can be started using maven command `mvn clean install`. This command will build the application and start it using Cargo Maven plugin.
The application will be then available on [http://localhost:8080/cors-manual-test/](http://localhost:8080/cors-manual-test/)
