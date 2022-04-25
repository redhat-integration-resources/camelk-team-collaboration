# camelk-team-collaboration

This repository contains all the sources showcased in the following  Red Hat Developers article:

 - https://developers.redhat.com/articles/2021/09/02/improve-cross-team-collaboration-camel-k

## Context

Camel K implements a cloud-native platform that helps cross-functional teams to collaborate. A Strategy team uses a Google Sheets document to list questions and concerns to be answered by other teams in the organisation. Camel K automatically distributes and collects answers from other departments to update the Google Sheets document.

## Prerequisites

This demo Camel K code requires the following dependencies:

 - A Kubernetes environment
 - A Camel K platform installed
 - A Kafka platform installed
 - Access to Google Sheets and Google Drive APIs
 - A Mail server (demo-testing server)

This demo has been tested using:
 - Red Hat OpenShift 4.9
 - Red Hat Camel K 1.6.5 GA
 - Red Hat Camel K client 1.6.0 GA
 - Red Hat's AMQ-Streams 1.8.4
 - Image of Greenmail in OpenShift \
    https://greenmail-mail-test.github.io/greenmail/#

</br>

## Preparations

### Google APIs

You need to setup API access to Google Sheets and Drive. You can start from here:
 - https://developers.google.com/sheets/api/guides/authorizing

Credentials comprise of a
 - clientId
 - clientSecret
 - refreshToken.
 
A handy resource for generating a long-lived refreshToken is the OAuth playground:
  - https://developers.google.com/oauthplayground \
    Authorise all APIs from the group:
    - Google Sheets API v4

<br>

 - Ensure you enable permissions to:
   - See, edit, create, and delete only the specific Google Drive files you use with this app.
   - See, edit, create, and delete all your Google Sheets spreadsheets.

### GreenMail

Create a namespace where the mail server will be deployed:

```
oc new-project demo-mail
```

GreenMail supports both secure and non-secure SMTP, POP3 and IMAP. 
You can deploy an image in OCP using their docker image:

 - https://greenmail-mail-test.github.io/greenmail/#deploy_docker_standalone

Ensure there is a route to its web interface in 8080:
 - From *OpenShift*'s option: \
      *Deploy Image*
 - Select the image: \
      `greenmail/standalone`
 - In the section: \
      *Show advanced Routing options*
 - Select: \
      `Target Port` \
      `8080`


> **Warning**: When deploying... it might fail to start due to timeout, logging the error:
> ```
> Exception in thread "main" java.lang.IllegalStateException: Could not start mail server smtps:0.0.0.0:3465, try to set server startup timeout > 2000 via ServerSetup.setServerStartupTimeout(timeoutInMs) or -Dgreenmail.startup.timeout 
> ```
> To resolve the problem:
> 1) try killing the pod until it starts successfully
> 2) or configure the timeout parameter as indicated by the error message

You can connect your local email client to GreenMail by opening tunnels using the following commands: 

 - POP3 port:
   ```
   ./scripts/setup/tunnel-pop.sh
   ```
 - SMTP port:
   ```
   ./scripts/setup/tunnel-smtp.sh
   ```

The following command line will run a script file that will create all the demo accounts for you by interacting with GreenMail's API:

    ./scripts/setup/mail.sh

> **Note: ** ensure the URL in the script is configured and pointing to your deployed mail server


### Google Sheet

> **Note**: The demo is implemented with simplistic logic. Camel K just looks for 3 rows in the spreadsheet using the range `B5:D7`. This is obviously unrealistic, but you're invited to complete the demo's logic to fully cover the entire spreadsheet.

Create a Google Sheets document and fill in 3 questions. Each row should contain:
 - Column B: the question ID, used by Camel K as a correlator (should be set to the row's ID)
 - Column C: the question.

Ensure the questions are entered in the range `B5:D7`.

Obtain the document's ID, and update Camel K's code accordingly.

### Google Drive

Obtain the target folder ID where Camel K will upload the PDF report generated from Stage-5. The demo uses the same folder where the spreadsheet is located.

You can find the folder ID in your browser's address bar.

### Camel K platform

Create a namespace where Camel K will be installed:

```
oc new-project demo-camelk
```

Install the Camel K operator for this namespace and create an Integration Platform using the default values.

> **Warning:** it's recommended to use the version referenced at the top of this guide to ensure Camel K and AMQ Streams are compatible.

### Camel K's Kamelets

Camel K comes with a catalogue of pre-defined Kamelets. However, not all of them are pre-loaded by the Operator.

This demo defines 3 extra ones not included in the catalogue:
 - Google Sheets source (based on Camel K's one: includes 'repeatCount' parameter)
 - Non-secure Mail source (for simplicity purposes)
 - Mail format to JSON action (to simplify data manipulation)

You can deploy these Kamelets in Kubernetes or OpenShift using the CLI clients (kubectl or oc). The repo includes a script file you can run:
```
./scripts/setup/kamelets.sh
```

### AMQ Streams platform

In the same Camel K namespace, install the AMQ Streams operator. 

> **Warning:** it's recommended to use the version referenced at the top of this guide to ensure Camel K and AMQ Streams are compatible.

Then create a Kafka instance using the default values

### Kafka topics

The demo will stream questions and answers in and out of Kafka using 2 topics. The repo includes a script that includes the creation of these Kafka topics. Run it with:

```
./scripts/demo/reset.sh
```


</br>

## Execution

> **Note**: The demo is implemented with simplistic logic. The demo is configured (hacked) to prevent duplicates using the parameter `repeatCount=1`. You're invited to complete the demo's logic to allow multiple polls and applying data caches and filters to make the demo more realistic. 

You can watch how the demo is executed in this video clip:

 - https://youtu.be/c8LpWE62LTE

The demo is composed of 5 Camel K elements

 - 2 KameletBindings (Stages 1 & 3)
 - 3 Camel K sources (Stages 2, 4 & 5) 

You can decide to have them all running at the same time, or deploying one at a time to allow your audience to better follow and understand the demo. 

1. Deploy Stage 1 with:
   ```
   ./scripts/demo/stage1.sh
   ```
2. Deploy Stage 2 with:
   ```
   ./scripts/demo/stage2.sh
   ```
   > **Warning:** on your 1st run, your email client might not fetch the emails. Trigger the action 'Purge all mails' from the GreenMail Web console and try again.   

2. Deploy Stage 3 with:
   ```
   ./scripts/demo/stage3.sh
   ```

2. Deploy Stage 4 with:
   ```
   ./scripts/demo/stage4.sh
   ```

2. Deploy Stage 5 with:
   ```
   ./scripts/demo/stage5.sh
   ```

You can reset the demo and start from zero by executing the following script:
```
./scripts/demo/reset.sh
```