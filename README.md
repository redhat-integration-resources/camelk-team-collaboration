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
 - Red Hat OpenShift 4.7
 - Red Hat Camel K 1.4 GA
 - Red Hat's Camel K GA, Red Hat's AMQ-Streams 1.7
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
  - https://developers.google.com/oauthplayground

### GreenMail

GreenMail supports both secure and non-secure SMTP, POP3 and IMAP. 
You can deploy an image in OCP using their docker image:

 - https://greenmail-mail-test.github.io/greenmail/#deploy_docker_standalone

Ensure there is a route to its web interface in 8080

You can connect your local email client to GreenMail by opening tunnels using the following commands: 

 - POP3 port:
   ```
   oc get pods -n demo-mail -o name | grep standalone | xargs -I {} oc port-forward -n demo-mail {} 3110
   ```
 - SMTP port:
   ```
   oc get pods -n demo-mail -o name | grep standalone | xargs -I {} oc port-forward -n demo-mail {} 3025
   ```

The following command line will run a script file that will create all the demo accounts for you by interacting with GreenMail's API:

    ./setup/mail.sh


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


### Camel K's Kamelets

Camel K comes with a catalogue of pre-defined Kamelets. However this demo defines 2 extra ones not included in the catalogue:

 - Non-secure Mail source (for simplicity purposes)
 - Mail format to JSON action (to simplify data manipulation)

You can deploy these Kamelets in Kubernetes or OpenShift using the CLI clients (kubectl or oc):
```
oc apply -f kamelet/mail-imap-insecure-source.kamelet.yaml
oc apply -f kamelet/mail-to-json.kamelet.yaml
```

### Kafka topics

The demo streams questions and answers in and out of Kafka using 2 topics. Use the following CLI commands to create have them created:

```
oc apply -f kafka/questions.yaml
oc apply -f kafka/answers.yaml
```


</br>

## Execution

> **Note**: The demo is implemented with simplistic logic. The demo is configured (hacked) to prevent duplicates using a long polling frequency value against the Google Sheets document. You're invited to complete the demo's logic to allow frequent polls and applying data caches and filters to make the demo more realistic. 

You can watch how the demo is executed in this video clip:

<iframe width="1076" height="605" src="https://www.youtube.com/embed/c8LpWE62LTE" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

The demo is composed of 5 Camel K elements

 - 2 KameletBindings (Stages 1 & 3)
 - 3 Camel K sources (Stages 2, 4 & 5) 

You can decide to have them all running at the same time, or deploying one at a time to allow your audience to better follow and understand the demo. 

1. Deploy Stage 1 with:

        oc apply -f kameletbinding/stage-1-sheets2kafka.yaml

2. Deploy Stage 2 with:

        kamel run camelk/stage-2-kafka2mail.xml -d camel-jackson

2. Deploy Stage 3 with:

        oc apply -f kameletbinding/stage-3-mail2kafka.yaml

2. Deploy Stage 4 with:

        kamel run camelk/stage-4-kafka2sheets.xml -d camel-jackson


2. Deploy Stage 5 with:

        kamel run --name stage5 camelk/java/HelperStage5.java camelk/stage-5-report2drive.xml -d camel-jackson -d camel-pdf
