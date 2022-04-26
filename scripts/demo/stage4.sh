kamel run --name stage4 camelk/java/HelperStage4.java camelk/stage-4-kafka2sheets.xml \
-d camel-jackson \
--property file:camelk/cfg/svc.properties \
--property file:camelk/cfg/secret.properties \