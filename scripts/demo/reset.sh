oc delete kameletbinding stage1
oc delete it stage2
oc delete kameletbinding stage3
oc delete it stage4
oc delete it stage5

oc delete kt questions
oc delete kt answers

oc apply -f kafka/questions.yaml
oc apply -f kafka/answers.yaml
