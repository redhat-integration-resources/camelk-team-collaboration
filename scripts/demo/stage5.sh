kamel run --name stage5 camelk/java/HelperStage5.java camelk/stage-5-report2drive.xml \
-d camel-jackson \
-d camel-pdf \
--property file:camelk/cfg/svc.properties \
--property file:camelk/cfg/secret.properties