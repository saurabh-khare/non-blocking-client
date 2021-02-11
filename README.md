# Non blocking API client

It is basically a servlet exposed as a REST endpoint via Apache Sling framework. It in turn fires multiple HTTP calls in parallel
and caching response in memory based on OSGi configuration. It then sends a consolidated response to the invoker.

## Features
* Use of Service Contract pattern to define a Service Interface which should be implemented by all services
* A service represents a third party REST API and encapsulates the logic to connect to the endpoint using its own set of configuration
* Each service exposes a ServiceWorker which handles the request data and is called by the invoker.
* Response is cache in-memory using Google Guava library by passing the executor function to cache

## Use Case
Here the use case is for Lead Generation, where website visitors can express interest in certain products and can submit their information
by a contact form which will be treated as Lead by the website business owners. 
There are three steps which are necessary to generate a lead:
1. Validate the form submission using Google Recaptcha
2. Validate if user is entering valid email by performing verification with ZeroBounce service
3. Obtain auth token from Authentication service by passing client ID and client secret to be sent along with form data to 
   lead generation service
  
Instead of waiting for each step to complete and sending the response back to the client, we execute all three steps in parallel
by firing simultaneous HTTP calls and after verification in step 1 & 2, we send form data along with token received in step 3. 
Based on if submission is successful or not, we send back proper response to the client.
We also cache the response of ZeroBounce validation since we don't want to validate the same email again and again and consume
API calls thereby reducing billed usage of the service


## How to build

To build the bundle run in the project root directory the following command with Maven 3:

    mvn clean install


## Testing

    mvn clean test
