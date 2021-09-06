oc apply -f kafka/questions.yaml
oc apply -f kafka/answers.yaml
oc apply -f kameletbinding/stage-1-sheets2kafka.yaml
oc apply -f kameletbinding/stage-3-mail2kafka.yaml
